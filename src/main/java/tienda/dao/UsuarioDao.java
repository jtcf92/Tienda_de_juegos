package tienda.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class UsuarioDao {

    private static final Logger log = LogManager.getLogger(UsuarioDao.class);

    public int insertar(Connection con, String nombre, String email) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email, fecha_registro) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setObject(3, LocalDateTime.now());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                int id = claves.getInt(1);
                log.debug("Usuario insertado id={}", id);
                return id;
            }
        }
    }

    public Usuario buscarPorId(Connection con, int idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre, email, fecha_registro FROM usuario WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getObject("fecha_registro", LocalDateTime.class));
    }
}