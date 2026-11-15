package tienda.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventarioDao {

    private static final Logger log = LogManager.getLogger(InventarioDao.class);


    public void insertar(Connection con, int idJuego, int stock, int stockMinimo) throws SQLException {
        String sql = "INSERT INTO inventario (id_juego, stock, stock_minimo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            ps.setInt(2, stock);
            ps.setInt(3, stockMinimo);
            ps.executeUpdate();
        }
    }


    public int obtenerStock(Connection con, int idJuego) throws SQLException {
        String sql = "SELECT stock FROM inventario WHERE id_juego = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock");
                }
                return -1;
            }
        }
    }

    // Suma unidades (reposicion o devolucion)
    public void sumarStock(Connection con, int idJuego, int cantidad) throws SQLException {
        String sql = "UPDATE inventario SET stock = stock + ? WHERE id_juego = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idJuego);
            ps.executeUpdate();
        }
        log.debug("Stock +{} juego={}", cantidad, idJuego);
    }


    public boolean restarStock(Connection con, int idJuego, int cantidad) throws SQLException {
        String sql = "UPDATE inventario SET stock = stock - ? WHERE id_juego = ? AND stock >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idJuego);
            ps.setInt(3, cantidad);
            boolean restado = ps.executeUpdate() == 1;
            log.debug("Stock -{} juego={} realizado={}", cantidad, idJuego, restado);
            return restado;
        }
    }
}