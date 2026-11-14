package tienda.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;
import tienda.dao.*;
import tienda.excepcion.NegocioException;
import tienda.modelo.Compra;
import tienda.modelo.DetalleCompra;
import tienda.modelo.Juego;
import tienda.modelo.LineaPedido;
import tienda.modelo.enums.EstadoCompra;
import tienda.modelo.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompraService {

    private static final Logger log = LogManager.getLogger(CompraService.class);

    private final ConnectionProvider proveedor;
    private final boolean simularFallo;

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final JuegoDao juegoDao = new JuegoDao();
    private final InventarioDao inventarioDao = new InventarioDao();
    private final CompraDao compraDao = new CompraDao();
    private final DetalleCompraDao detalleDao = new DetalleCompraDao();
    private final PagoDao pagoDao = new PagoDao();
    private final MovimientoStockDao movimientoDao = new MovimientoStockDao();

    public CompraService(ConnectionProvider proveedor, boolean simularFallo) {
        this.proveedor = proveedor;
        this.simularFallo = simularFallo;
    }


    public int registrar(int idUsuario, List<LineaPedido> lineas) throws SQLException {
        if (lineas.isEmpty()) {
            throw new NegocioException("La compra no tiene ningun juego");
        }

        try (Connection con = proveedor.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Comprobar las reglas de la tienda y calcular el total
                if (usuarioDao.buscarPorId(con, idUsuario) == null) {
                    throw new NegocioException("El usuario " + idUsuario + " no existe");
                }

                List<Juego> juegos = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;
                for (LineaPedido linea : lineas) {
                    if (linea.cantidad() <= 0) {
                        throw new NegocioException("Cantidad no valida para el juego " + linea.idJuego());
                    }
                    Juego juego = juegoDao.buscarPorId(con, linea.idJuego());
                    if (juego == null || !juego.activo()) {
                        throw new NegocioException("El juego " + linea.idJuego() + " no existe o no esta a la venta");
                    }
                    int stock = inventarioDao.obtenerStock(con, linea.idJuego());
                    if (stock < linea.cantidad()) {
                        throw new NegocioException("Stock insuficiente de '" + juego.titulo()
                                + "': hay " + stock + ", se piden " + linea.cantidad());
                    }
                    juegos.add(juego);
                    total = total.add(juego.precio().multiply(BigDecimal.valueOf(linea.cantidad())));
                }

                // 2. Insertar la compra y sus lineas (con el precio de ESTE momento)
                int idCompra = compraDao.insertar(con, idUsuario, total);

                List<DetalleCompra> detalles = new ArrayList<>();
                for (int i = 0; i < lineas.size(); i++) {
                    LineaPedido linea = lineas.get(i);
                    BigDecimal precio = juegos.get(i).precio();
                    BigDecimal importe = precio.multiply(BigDecimal.valueOf(linea.cantidad()));
                    detalles.add(new DetalleCompra(idCompra, linea.idJuego(), linea.cantidad(), precio, importe));
                }
                detalleDao.insertarTodos(con, detalles);

                // 3. Restar el stock y registrar los movimientos de VENTA
                for (LineaPedido linea : lineas) {
                    if (!inventarioDao.restarStock(con, linea.idJuego(), linea.cantidad())) {
                        throw new NegocioException("Stock insuficiente para el juego " + linea.idJuego());
                    }
                    movimientoDao.registrar(con, linea.idJuego(), idCompra, TipoMovimiento.VENTA, linea.cantidad());
                }

                // 4. Fallo controlado: despues de modificar varias tablas y ANTES del commit
                if (simularFallo) {
                    log.warn("FALLO_TRANSACCION: error provocado antes de confirmar la compra {}", idCompra);
                    throw new SQLException("Fallo simulado antes del commit");
                }

                con.commit();
                log.info("Compra {} registrada: usuario={}, lineas={}, total={}",
                        idCompra, idUsuario, lineas.size(), total);
                return idCompra;

            } catch (SQLException | NegocioException e) {
                con.rollback();
                log.warn("Compra revertida: {}", e.getMessage());
                throw e;
            }
        }
    }
    // Pago simulado: solo se puede pagar una compra PENDIENTE
    public void pagar(int idCompra) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            con.setAutoCommit(false);
            try {
                Compra compra = compraDao.buscarPorId(con, idCompra);
                if (compra == null) {
                    throw new NegocioException("La compra " + idCompra + " no existe");
                }
                if (compra.estado() != EstadoCompra.PENDIENTE) {
                    throw new NegocioException("La compra " + idCompra + " no se puede pagar: esta " + compra.estado());
                }

                pagoDao.insertar(con, idCompra, compra.total());
                compraDao.cambiarEstado(con, idCompra, EstadoCompra.PAGADA);

                con.commit();
                log.info("Compra {} pagada: importe={}", idCompra, compra.total());

            } catch (SQLException | NegocioException e) {
                con.rollback();
                log.warn("Pago revertido: {}", e.getMessage());
                throw e;
            }
        }
    }

    // Cancelacion: devuelve el stock y, si estaba pagada, reembolsa el pago
    public void cancelar(int idCompra) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            con.setAutoCommit(false);
            try {
                Compra compra = compraDao.buscarPorId(con, idCompra);
                if (compra == null) {
                    throw new NegocioException("La compra " + idCompra + " no existe");
                }
                if (compra.estado() == EstadoCompra.CANCELADA) {
                    throw new NegocioException("La compra " + idCompra + " ya estaba cancelada");
                }

                // Devolver cada linea al stock y registrar el movimiento
                for (DetalleCompra detalle : detalleDao.buscarPorCompra(con, idCompra)) {
                    inventarioDao.sumarStock(con, detalle.idJuego(), detalle.cantidad());
                    movimientoDao.registrar(con, detalle.idJuego(), idCompra,
                            TipoMovimiento.DEVOLUCION, detalle.cantidad());
                }

                // Si estaba pagada, el pago pasa a REEMBOLSADO
                if (compra.estado() == EstadoCompra.PAGADA) {
                    pagoDao.marcarReembolsado(con, idCompra);
                }

                compraDao.cambiarEstado(con, idCompra, EstadoCompra.CANCELADA);

                con.commit();
                log.info("Compra {} cancelada (estaba {})", idCompra, compra.estado());

            } catch (SQLException | NegocioException e) {
                con.rollback();
                log.warn("Cancelacion revertida: {}", e.getMessage());
                throw e;
            }
        }
    }
}