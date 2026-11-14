package tienda.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;
import tienda.config.enums.Gestor;
import tienda.dao.CompraDao;
import tienda.dao.ConsultaDao;
import tienda.dao.InventarioDao;
import tienda.dto.EstadoBdDTO;
import tienda.dto.JuegoVentasDTO;
import tienda.dto.LineaDetalleDTO;
import tienda.dto.StockBajoDTO;
import tienda.excepcion.NegocioException;
import tienda.modelo.Compra;
import tienda.modelo.Juego;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ConsultaService {

    private static final Logger log = LogManager.getLogger(ConsultaService.class);

    private final ConnectionProvider proveedor;
    private final Gestor gestor;
    private final ConsultaDao consultaDao = new ConsultaDao();
    private final CompraDao compraDao = new CompraDao();
    private final InventarioDao inventarioDao = new InventarioDao();
    public ConsultaService(ConnectionProvider proveedor, Gestor gestor) {
        this.proveedor = proveedor;
        this.gestor = gestor;
    }

    public List<Juego> catalogo(BigDecimal precioMin, BigDecimal precioMax, int pagina, int porPagina)
            throws SQLException {
        if (pagina < 1 || porPagina < 1) {
            throw new NegocioException("La pagina y el tamano deben ser mayores que 0");
        }
        int saltar = (pagina - 1) * porPagina;
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.catalogo(con, precioMin, precioMax, porPagina, saltar);
        }
    }

    public List<Juego> juegosPorPlataforma(String plataforma) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.juegosPorPlataforma(con, plataforma);
        }
    }

    public List<LineaDetalleDTO> detalleCompra(int idCompra) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.detalleCompra(con, idCompra);
        }
    }

    public List<Compra> historial(int idUsuario) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            return compraDao.buscarPorUsuario(con, idUsuario);
        }
    }

    public List<JuegoVentasDTO> masVendidos(int cuantos) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.masVendidos(con, cuantos);
        }
    }

    public List<StockBajoDTO> stockBajo() throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.stockBajo(con);
        }
    }

    // Solo MySQL: en H2 se avisa y no se ejecuta (pagina 11)
    public JuegoVentasDTO ventasPorProcedimiento(int idJuego) throws SQLException {
        if (gestor != Gestor.MYSQL) {
            log.warn("El procedimiento sp_ventas_juego es especifico de MySQL: no se ejecuta con {}", gestor);
            return null;
        }
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.ventasPorProcedimiento(con, idJuego);
        }
    }
    public EstadoBdDTO estadoBd(int idJuego) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            return consultaDao.estadoBd(con, idJuego);
        }
    }
}