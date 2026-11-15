package tienda.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;
import tienda.dao.InventarioDao;
import tienda.dao.JuegoDao;
import tienda.dao.MovimientoStockDao;
import tienda.excepcion.NegocioException;
import tienda.modelo.enums.TipoMovimiento;

import java.sql.Connection;
import java.sql.SQLException;

public class InventarioService {

    private static final Logger log = LogManager.getLogger(InventarioService.class);

    private final ConnectionProvider proveedor;
    private final JuegoDao juegoDao = new JuegoDao();
    private final InventarioDao inventarioDao = new InventarioDao();
    private final MovimientoStockDao movimientoDao = new MovimientoStockDao();

    public InventarioService(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }

    // Suma unidades al stock y deja constancia en movimiento_stock
    public void reponer(int idJuego, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad a reponer debe ser mayor que 0");
        }

        try (Connection con = proveedor.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (juegoDao.buscarPorId(con, idJuego) == null) {
                    throw new NegocioException("El juego " + idJuego + " no existe");
                }
                inventarioDao.sumarStock(con, idJuego, cantidad);
                movimientoDao.registrar(con, idJuego, null, TipoMovimiento.REPOSICION, cantidad);

                con.commit();
                log.info("Reposicion: juego={} +{} unidades, stock actual={}",
                        idJuego, cantidad, inventarioDao.obtenerStock(con, idJuego));

            } catch (SQLException | NegocioException e) {
                con.rollback();
                log.warn("Reposicion revertida: {}", e.getMessage());
                throw e;
            }
        }
    }
}