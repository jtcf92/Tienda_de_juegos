package tienda.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;
import tienda.dao.InventarioDao;
import tienda.dao.JuegoDao;
import tienda.excepcion.NegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class CatalogoService {

    private static final Logger log = LogManager.getLogger(CatalogoService.class);

    private final ConnectionProvider proveedor;
    private final JuegoDao juegoDao = new JuegoDao();
    private final InventarioDao inventarioDao = new InventarioDao();

    public CatalogoService(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }


    public int altaJuego(String titulo, BigDecimal precio, int stockInicial, int stockMinimo) throws SQLException {
        if (precio.compareTo(BigDecimal.ZERO) < 0 || stockInicial < 0 || stockMinimo < 0) {
            throw new NegocioException("Precio y stock no pueden ser negativos");
        }

        try (Connection con = proveedor.getConnection()) {
            con.setAutoCommit(false);
            try {
                int idJuego = juegoDao.insertar(con, titulo, precio);
                inventarioDao.insertar(con, idJuego, stockInicial, stockMinimo);

                con.commit();
                log.info("Juego dado de alta: id={}, titulo='{}', precio={}, stock={}",
                        idJuego, titulo, precio, stockInicial);
                return idJuego;

            } catch (SQLException e) {
                con.rollback();
                log.warn("Alta de juego revertida: {}", e.getMessage());
                throw e;
            }
        }
    }


    public void cambiarPrecio(int idJuego, BigDecimal nuevoPrecio) throws SQLException {
        if (nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("El precio no puede ser negativo");
        }

        try (Connection con = proveedor.getConnection()) {
            if (!juegoDao.actualizarPrecio(con, idJuego, nuevoPrecio)) {
                throw new NegocioException("El juego " + idJuego + " no existe");
            }
            log.info("Precio actualizado: {}", juegoDao.buscarPorId(con, idJuego));
        }
    }
}