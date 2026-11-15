package tienda.dao;

import tienda.dto.EstadoBdDTO;
import tienda.dto.JuegoVentasDTO;
import tienda.dto.LineaDetalleDTO;
import tienda.dto.StockBajoDTO;
import tienda.modelo.Juego;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDao {

    //  Catalogo filtrado por precio y PAGINADO en SQL
    public List<Juego> catalogo(Connection con, BigDecimal precioMin, BigDecimal precioMax,
                                int limite, int saltar) throws SQLException {
        String sql = "SELECT id_juego, titulo, precio, activo FROM juego "
                + "WHERE activo = TRUE AND precio BETWEEN ? AND ? "
                + "ORDER BY titulo LIMIT ? OFFSET ?";
        List<Juego> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, precioMin);
            ps.setBigDecimal(2, precioMax);
            ps.setInt(3, limite);
            ps.setInt(4, saltar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(JuegoDao.mapear(rs));
                }
            }
        }
        return lista;
    }

    //  Consulta propia: juegos de una plataforma (relacion N:M)
    public List<Juego> juegosPorPlataforma(Connection con, String plataforma) throws SQLException {
        String sql = "SELECT j.id_juego, j.titulo, j.precio, j.activo FROM juego j "
                + "JOIN juego_plataforma jp ON jp.id_juego = j.id_juego "
                + "JOIN plataforma p ON p.id_plataforma = jp.id_plataforma "
                + "WHERE p.nombre = ? AND j.activo = TRUE ORDER BY j.titulo";
        List<Juego> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, plataforma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(JuegoDao.mapear(rs));
                }
            }
        }
        return lista;
    }


    public List<LineaDetalleDTO> detalleCompra(Connection con, int idCompra) throws SQLException {
        String sql = "SELECT u.nombre, c.fecha, c.estado, j.titulo, d.cantidad, d.precio_unitario, d.importe "
                + "FROM compra c "
                + "JOIN usuario u ON u.id_usuario = c.id_usuario "
                + "JOIN detalle_compra d ON d.id_compra = c.id_compra "
                + "JOIN juego j ON j.id_juego = d.id_juego "
                + "WHERE c.id_compra = ? ORDER BY j.titulo";
        List<LineaDetalleDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LineaDetalleDTO(
                            rs.getString("nombre"),
                            rs.getObject("fecha", LocalDateTime.class),
                            rs.getString("estado"),
                            rs.getString("titulo"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("importe")));
                }
            }
        }
        return lista;
    }

    //  Resumen comercial: los mas vendido con limite
    public List<JuegoVentasDTO> masVendidos(Connection con, int cuantos) throws SQLException {
        String sql = "SELECT j.titulo, SUM(d.cantidad) AS unidades, SUM(d.importe) AS ingresos "
                + "FROM detalle_compra d "
                + "JOIN compra c ON c.id_compra = d.id_compra "
                + "JOIN juego j ON j.id_juego = d.id_juego "
                + "WHERE c.estado <> 'CANCELADA' "
                + "GROUP BY j.id_juego, j.titulo "
                + "ORDER BY unidades DESC, j.titulo LIMIT ?";
        List<JuegoVentasDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cuantos);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new JuegoVentasDTO(
                            rs.getString("titulo"),
                            rs.getInt("unidades"),
                            rs.getBigDecimal("ingresos")));
                }
            }
        }
        return lista;
    }

    //  Control de existencias: juegos por debajo de su stock minimo
    public List<StockBajoDTO> stockBajo(Connection con) throws SQLException {
        String sql = "SELECT j.titulo, i.stock, i.stock_minimo FROM inventario i "
                + "JOIN juego j ON j.id_juego = i.id_juego "
                + "WHERE i.stock < i.stock_minimo ORDER BY i.stock, j.titulo";
        List<StockBajoDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new StockBajoDTO(
                        rs.getString("titulo"),
                        rs.getInt("stock"),
                        rs.getInt("stock_minimo")));
            }
        }
        return lista;
    }

    //  Procedimiento almacenado
    public JuegoVentasDTO ventasPorProcedimiento(Connection con, int idJuego) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{call sp_ventas_juego(?, ?, ?, ?)}")) {
            cs.setInt(1, idJuego);                          // IN
            cs.registerOutParameter(2, Types.VARCHAR);      // OUT titulo
            cs.registerOutParameter(3, Types.INTEGER);      // OUT unidades
            cs.registerOutParameter(4, Types.DECIMAL);      // OUT ingresos
            cs.execute();
            return new JuegoVentasDTO(cs.getString(2), cs.getInt(3), cs.getBigDecimal(4));
        }
    }
    // Cuenta compras, detalles y ventas, y el stock de un juego, en UNA consulta
    public EstadoBdDTO estadoBd(Connection con, int idJuego) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM compra) AS compras, "
                + "(SELECT COUNT(*) FROM detalle_compra) AS detalles, "
                + "(SELECT COUNT(*) FROM movimiento_stock WHERE tipo = 'VENTA') AS ventas, "
                + "(SELECT stock FROM inventario WHERE id_juego = ?) AS stock";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new EstadoBdDTO(
                        rs.getInt("compras"),
                        rs.getInt("detalles"),
                        rs.getInt("ventas"),
                        rs.getInt("stock"));
            }
        }
    }
}