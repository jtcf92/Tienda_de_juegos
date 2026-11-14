package tienda.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PagoDao {

    private static final Logger log = LogManager.getLogger(PagoDao.class);

    // Registra el pago simulado de una compra
    public void insertar(Connection con, int idCompra, BigDecimal importe) throws SQLException {
        String sql = "INSERT INTO pago (id_compra, importe, estado, fecha) VALUES (?, ?, 'PAGADO', ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            ps.setBigDecimal(2, importe);
            ps.setObject(3, LocalDateTime.now());
            ps.executeUpdate();
        }
        log.debug("Pago registrado compra={} importe={}", idCompra, importe);
    }

    // Marca el pago como reembolsado. Devuelve false si la compra no tenia pago
    public boolean marcarReembolsado(Connection con, int idCompra) throws SQLException {
        String sql = "UPDATE pago SET estado = 'REEMBOLSADO' WHERE id_compra = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            return ps.executeUpdate() == 1;
        }
    }
}