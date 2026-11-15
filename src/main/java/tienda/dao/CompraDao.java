package tienda.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.modelo.Compra;
import tienda.modelo.enums.EstadoCompra;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompraDao {

    private static final Logger log = LogManager.getLogger(CompraDao.class);

    // Crea una compra PENDIENTE y devuelve su id
    public int insertar(Connection con, int idUsuario, BigDecimal total) throws SQLException {
        String sql = "INSERT INTO compra (id_usuario, fecha, estado, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, EstadoCompra.PENDIENTE.name());
            ps.setBigDecimal(4, total);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                int id = claves.getInt(1);
                log.debug("Compra insertada id={}", id);
                return id;
            }
        }
    }

    public Compra buscarPorId(Connection con, int idCompra) throws SQLException {
        String sql = "SELECT id_compra, id_usuario, fecha, estado, total FROM compra WHERE id_compra = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        }
    }

    public void cambiarEstado(Connection con, int idCompra, EstadoCompra estado) throws SQLException {
        String sql = "UPDATE compra SET estado = ? WHERE id_compra = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idCompra);
            ps.executeUpdate();
        }
        log.debug("Compra id={} pasa a {}", idCompra, estado);
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        return new Compra(
                rs.getInt("id_compra"),
                rs.getInt("id_usuario"),
                rs.getObject("fecha", LocalDateTime.class),
                EstadoCompra.valueOf(rs.getString("estado")),
                rs.getBigDecimal("total"));
    }
    public List<Compra> buscarPorUsuario(Connection con, int idUsuario) throws SQLException {
        String sql = "SELECT id_compra, id_usuario, fecha, estado, total FROM compra "
                + "WHERE id_usuario = ? ORDER BY fecha DESC, id_compra DESC";
        List<Compra> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }
}