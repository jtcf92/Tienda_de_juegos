package tienda;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tienda.bd.CargarDatos;
import tienda.bd.InicializadorDB;
import tienda.conexion.ConnectionProvider;
import tienda.config.enums.Gestor;
import tienda.dao.CompraDao;
import tienda.dao.InventarioDao;
import tienda.dao.JuegoDao;
import tienda.dao.UsuarioDao;
import tienda.dto.EstadoBdDTO;
import tienda.dto.JuegoVentasDTO;
import tienda.modelo.Compra;
import tienda.modelo.Juego;
import tienda.modelo.LineaPedido;
import tienda.modelo.Usuario;
import tienda.modelo.enums.EstadoCompra;
import tienda.servicio.CatalogoService;
import tienda.servicio.CompraService;
import tienda.servicio.ConsultaService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TiendaTest {

    private static final String URL_H2 =
            "jdbc:h2:./target/pruebas/tienda;MODE=MySQL;DATABASE_TO_LOWER=TRUE";

    private ConnectionProvider proveedor;

    // Antes de CADA prueba: base de datos limpia con los datos de ejemplo
    @BeforeEach
    void prepararBaseDeDatos() throws Exception {
        proveedor = new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(URL_H2, "sa", "");
            }

            @Override
            public void close() {
            }
        };
        new InicializadorDB(proveedor).crearTablas();
        new CargarDatos(proveedor).cargar();
    }

    //  Insercion y consulta
    @Test
    void insertarYConsultarUsuario() throws SQLException {
        UsuarioDao usuarioDao = new UsuarioDao();
        try (Connection con = proveedor.getConnection()) {
            int id = usuarioDao.insertar(con, "Prueba Test", "test@correo.com");
            Usuario usuario = usuarioDao.buscarPorId(con, id);

            assertNotNull(usuario);
            assertEquals("Prueba Test", usuario.nombre());
            assertEquals("test@correo.com", usuario.email());
        }
    }

    //  Modificacion
    @Test
    void cambiarPrecioModificaLaBaseDeDatos() throws SQLException {
        new CatalogoService(proveedor).cambiarPrecio(3, new BigDecimal("19.99"));

        try (Connection con = proveedor.getConnection()) {
            Juego juego = new JuegoDao().buscarPorId(con, 3);
            assertEquals(0, new BigDecimal("19.99").compareTo(juego.precio()));
        }
    }

    //  Claves y relaciones
    @Test
    void altaDeJuegoUsaElIdGeneradoEnElInventario() throws SQLException {
        int idNuevo = new CatalogoService(proveedor)
                .altaJuego("Juego de prueba", new BigDecimal("10.00"), 12, 3);

        try (Connection con = proveedor.getConnection()) {
            assertNotNull(new JuegoDao().buscarPorId(con, idNuevo));
            assertEquals(12, new InventarioDao().obtenerStock(con, idNuevo));
        }
    }

    //  Consulta y DTO
    @Test
    void masVendidosDevuelveUnidadesEIngresos() throws SQLException {
        new CompraService(proveedor, false).registrar(1, List.of(new LineaPedido(5, 3)));

        List<JuegoVentasDTO> ventas = new ConsultaService(proveedor, Gestor.H2).masVendidos(3);

        assertEquals(1, ventas.size());
        assertEquals("Minecraft", ventas.get(0).titulo());
        assertEquals(3, ventas.get(0).unidades());
        assertEquals(0, new BigDecimal("89.97").compareTo(ventas.get(0).ingresos()));
    }

    //  Commit
    @Test
    void compraCorrectaGuardaTodasLasTablas() throws SQLException {
        int idCompra = new CompraService(proveedor, false)
                .registrar(1, List.of(new LineaPedido(1, 2), new LineaPedido(5, 1)));

        // 1 compra, 2 detalles, 2 movimientos de venta, y Zelda pasa de 10 a 8
        EstadoBdDTO estado = new ConsultaService(proveedor, Gestor.H2).estadoBd(1);
        assertEquals(new EstadoBdDTO(1, 2, 2, 8), estado);

        try (Connection con = proveedor.getConnection()) {
            Compra compra = new CompraDao().buscarPorId(con, idCompra);
            assertEquals(EstadoCompra.PENDIENTE, compra.estado());
            assertEquals(0, new BigDecimal("149.97").compareTo(compra.total()));
        }
    }

    //  Rollback
    @Test
    void falloControladoDeshaceTodaLaCompra() throws SQLException {
        ConsultaService consultas = new ConsultaService(proveedor, Gestor.H2);
        CompraService comprasConFallo = new CompraService(proveedor, true);

        EstadoBdDTO antes = consultas.estadoBd(1);
        SQLException error = assertThrows(SQLException.class,
                () -> comprasConFallo.registrar(1, List.of(new LineaPedido(1, 2))));
        EstadoBdDTO despues = consultas.estadoBd(1);

        // Tiene que ser NUESTRO fallo simulado, no cualquier otro error
        assertEquals("Fallo simulado antes del commit", error.getMessage());
        assertEquals(antes, despues);
        assertEquals(new EstadoBdDTO(0, 0, 0, 10), despues);
    }}