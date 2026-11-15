package tienda.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.modelo.enums.TipoMovimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

public class MovimientoStockDao {

    private static final Logger log = LogManager.getLogger(MovimientoStockDao.class);


    public void registrar(Connection con, int idJuego, Integer idCompra, TipoMovimiento tipo, int cantidad)
            throws SQLException {
        String sql = "INSERT INTO movimiento_stock (id_juego, id_compra, tipo, cantidad, fecha) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            if (idCompra == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, idCompra);
            }
            ps.setString(3, tipo.name());
            ps.setInt(4, cantidad);
            ps.setObject(5, LocalDateTime.now());
            ps.executeUpdate();
        }
        log.debug("Movimiento {} juego={} cantidad={} compra={}", tipo, idJuego, cantidad, idCompra);
    }
}