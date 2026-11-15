package tienda.dao;

import tienda.modelo.DetalleCompra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleCompraDao {

    // Guarda todas las lineas de una compra en un solo lote
    public void insertarTodos(Connection con, List<DetalleCompra> detalles) throws SQLException {
        String sql = "INSERT INTO detalle_compra (id_compra, id_juego, cantidad, precio_unitario, importe) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleCompra d : detalles) {
                ps.setInt(1, d.idCompra());
                ps.setInt(2, d.idJuego());
                ps.setInt(3, d.cantidad());
                ps.setBigDecimal(4, d.precioUnitario());
                ps.setBigDecimal(5, d.importe());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Devuelve todas las lineas de una compra
    public List<DetalleCompra> buscarPorCompra(Connection con, int idCompra) throws SQLException {
        String sql = "SELECT id_compra, id_juego, cantidad, precio_unitario, importe "
                + "FROM detalle_compra WHERE id_compra = ?";
        List<DetalleCompra> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleCompra(
                            rs.getInt("id_compra"),
                            rs.getInt("id_juego"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("importe")));
                }
            }
        }
        return lista;
    }
}