package tienda.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;
import tienda.config.Configuracion;
import tienda.config.enums.ModoSimulacion;
import tienda.dto.EstadoBdDTO;
import tienda.dto.JuegoVentasDTO;
import tienda.excepcion.NegocioException;
import tienda.modelo.LineaPedido;
import tienda.servicio.CatalogoService;
import tienda.servicio.CompraService;
import tienda.servicio.ConsultaService;
import tienda.servicio.InventarioService;
import tienda.servicio.UsuarioService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class Simulador {

    private static final Logger log = LogManager.getLogger(Simulador.class);

    private final boolean modoFallo;
    private final CatalogoService catalogo;
    private final UsuarioService usuarios;
    private final InventarioService inventario;
    private final CompraService compras;
    private final ConsultaService consultas;

    public Simulador(Configuracion config, ConnectionProvider proveedor) {
        this.modoFallo = config.getModoSimulacion() == ModoSimulacion.FALLO_TRANSACCION;
        this.catalogo = new CatalogoService(proveedor);
        this.usuarios = new UsuarioService(proveedor);
        this.inventario = new InventarioService(proveedor);
        this.compras = new CompraService(proveedor, modoFallo);
        this.consultas = new ConsultaService(proveedor, config.getGestor());
    }

    // Recorrido completo de la tienda
    public void ejecutar() throws SQLException {
        seccionCatalogo();
        seccionInventario();
        if (modoFallo) {
            demostrarRollback();
        } else {
            seccionCompras();
        }
        seccionInformes();
    }

    // 1. Catalogo, clientes y violacion de integridad
    private void seccionCatalogo() throws SQLException {
        log.info("===== 1. CATALOGO Y CLIENTES =====");

        int idNuevo = catalogo.altaJuego("Hollow Knight Silksong", new BigDecimal("19.99"), 12, 3);
        catalogo.cambiarPrecio(idNuevo, new BigDecimal("17.99"));

        mostrar("Catalogo de 20 a 60 euros, pagina 1 (3 por pagina)",
                consultas.catalogo(new BigDecimal("20"), new BigDecimal("60"), 1, 3));
        mostrar("Catalogo de 20 a 60 euros, pagina 2 (3 por pagina)",
                consultas.catalogo(new BigDecimal("20"), new BigDecimal("60"), 2, 3));
        mostrar("Juegos de Nintendo Switch", consultas.juegosPorPlataforma("Nintendo Switch"));

        usuarios.registrar("Pedro Sanz", "pedro@correo.com");
        try {
            usuarios.registrar("Ana Duplicada", "ana@correo.com");
        } catch (NegocioException e) {
            log.warn("Alta rechazada: {}", e.getMessage());
        }
    }

    // 2. Reposicion y control de existencias
    private void seccionInventario() throws SQLException {
        log.info("===== 2. INVENTARIO =====");
        mostrar("Stock bajo antes de reponer", consultas.stockBajo());
        inventario.reponer(6, 5);
        mostrar("Stock bajo despues de reponer Halo Infinite", consultas.stockBajo());
    }

    // 3. Compras en modo NORMAL
    private void seccionCompras() throws SQLException {
        log.info("===== 3. COMPRAS =====");

        int idCompra = compras.registrar(1, List.of(new LineaPedido(1, 2), new LineaPedido(5, 1)));
        compras.pagar(idCompra);
        mostrar("Detalle de la compra " + idCompra, consultas.detalleCompra(idCompra));

        try {
            compras.registrar(2, List.of(new LineaPedido(3, 10)));
        } catch (NegocioException e) {
            log.warn("Compra rechazada: {}", e.getMessage());
        }

        compras.cancelar(idCompra);
        try {
            compras.cancelar(idCompra);
        } catch (NegocioException e) {
            log.warn("Cancelacion rechazada: {}", e.getMessage());
        }

        int idCompra2 = compras.registrar(1, List.of(new LineaPedido(2, 2), new LineaPedido(5, 3)));
        compras.pagar(idCompra2);

        mostrar("Historial del usuario 1", consultas.historial(1));
    }

    // 3 bis. Modo FALLO_TRANSACCION: comprobar con la BD que el rollback no deja nada
    private void demostrarRollback() throws SQLException {
        log.info("===== 3. COMPRA CON FALLO CONTROLADO =====");

        EstadoBdDTO antes = consultas.estadoBd(1);
        try {
            compras.registrar(1, List.of(new LineaPedido(1, 2)));
        } catch (SQLException e) {
            log.warn("La compra ha fallado como se esperaba: {}", e.getMessage());
        }
        EstadoBdDTO despues = consultas.estadoBd(1);

        log.info("Estado de la BD antes del fallo:   {}", antes);
        log.info("Estado de la BD despues del fallo: {}", despues);
        if (antes.equals(despues)) {
            log.info("Rollback correcto: no queda compra, ni detalles, ni movimientos, y el stock no ha cambiado");
        } else {
            log.error("El rollback NO ha funcionado: quedan cambios en la base de datos");
        }
    }

    // 4. Informes de gestion
    private void seccionInformes() throws SQLException {
        log.info("===== 4. INFORMES =====");
        mostrar("Juegos mas vendidos", consultas.masVendidos(3));
        mostrar("Juegos con stock bajo", consultas.stockBajo());

        JuegoVentasDTO ventas = consultas.ventasPorProcedimiento(5);
        if (ventas != null) {
            log.info("Procedimiento sp_ventas_juego(5): {}", ventas);
        }
    }

    // Muestra un titulo y cada elemento de una lista
    private void mostrar(String titulo, List<?> lista) {
        log.info("--- {} ---", titulo);
        if (lista.isEmpty()) {
            log.info("  (sin resultados)");
        }
        lista.forEach(elemento -> log.info("  {}", elemento));
    }
}