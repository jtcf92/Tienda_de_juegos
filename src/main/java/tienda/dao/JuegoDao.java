package tienda.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.modelo.Juego;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JuegoDao {

    private static final Logger log = LogManager.getLogger(JuegoDao.class);


    public int insertar(Connection con, String titulo, BigDecimal precio) throws SQLException {
        String sql = "INSERT INTO juego (titulo, precio, activo) VALUES (?, ?, TRUE)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, titulo);
            ps.setBigDecimal(2, precio);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                int id = claves.getInt(1);
                log.debug("Juego insertado id={}", id);
                return id;
            }
        }
    }

    // SELECT de una fila: devuelve el juego, o null si no existe
    public Juego buscarPorId(Connection con, int idJuego) throws SQLException {
        String sql = "SELECT id_juego, titulo, precio, activo FROM juego WHERE id_juego = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        }
    }


    public boolean actualizarPrecio(Connection con, int idJuego, BigDecimal nuevoPrecio) throws SQLException {
        String sql = "UPDATE juego SET precio = ? WHERE id_juego = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoPrecio);
            ps.setInt(2, idJuego);
            return ps.executeUpdate() == 1;
        }
    }


    static Juego mapear(ResultSet rs) throws SQLException {
        return new Juego(
                rs.getInt("id_juego"),
                rs.getString("titulo"),
                rs.getBigDecimal("precio"),
                rs.getBoolean("activo"));
    }
}