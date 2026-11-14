package tienda.app;

import tienda.bd.CargarDatos;
import tienda.conexion.ConnectionProvider;
import tienda.conexion.ConnectionProviderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.bd.InicializadorDB;
import tienda.config.*;
import tienda.config.enums.ModoSimulacion;
import tienda.dao.JuegoDao;
import tienda.dao.UsuarioDao;
import tienda.excepcion.NegocioException;
import tienda.modelo.LineaPedido;
import tienda.servicio.CatalogoService;
import tienda.servicio.CompraService;
import tienda.servicio.InventarioService;
import tienda.servicio.UsuarioService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Arranque de la aplicacion");

        try {
            Configuracion config = new Configuracion("application.properties");

            try (ConnectionProvider proveedor = ConnectionProviderFactory.crear(config)) {
                InicializadorDB inicializador = new InicializadorDB(proveedor);
                inicializador.crearTablas();
                CargarDatos carga = new CargarDatos(proveedor);
                carga.cargar();
                JuegoDao juegoDao = new JuegoDao();
                InventarioService inventario = new InventarioService(proveedor);
                CompraService compras = new CompraService(proveedor,
                        config.getModoSimulacion() == ModoSimulacion.FALLO_TRANSACCION);

                // Reponer Halo Infinite (id 6), que tenia stock 0
                inventario.reponer(6, 5);

                // Compra correcta: 2 Zelda (id 1) + 1 Minecraft (id 5)
                int idCompra = compras.registrar(1,
                        List.of(new LineaPedido(1, 2), new LineaPedido(5, 1)));
                log.info("Compra creada con id {}", idCompra);

                // Compra rechazada: 10 Elden Ring (id 3), pero solo hay 2
                try {
                    compras.registrar(2, List.of(new LineaPedido(3,2 )));
                } catch (NegocioException e) {
                    log.warn("Compra rechazada por regla de negocio: {}", e.getMessage());
                }
                // Pagar y cancelar la compra correcta
                compras.pagar(idCompra);
                compras.cancelar(idCompra);

                // Segunda cancelacion: debe rechazarse sin devolver el stock otra vez
                try {
                    compras.cancelar(idCompra);
                } catch (NegocioException e) {
                    log.warn("Cancelacion rechazada: {}", e.getMessage());
                }

                // Catalogo: alta de juego y cambio de precio
                CatalogoService catalogo = new CatalogoService(proveedor);
                int idNuevo = catalogo.altaJuego("Hollow Knight Silksong", new BigDecimal("19.99"), 12, 3);
                catalogo.cambiarPrecio(idNuevo, new BigDecimal("17.99"));

                // Usuarios: alta correcta y email repetido (violacion de integridad)
                UsuarioService usuarios = new UsuarioService(proveedor);
                usuarios.registrar("Pedro Sanz", "pedro@correo.com");
                try {
                    usuarios.registrar("Ana Duplicada", "ana@correo.com");
                } catch (NegocioException e) {
                    log.warn("Alta rechazada: {}", e.getMessage());
                }


            }

        } catch (IOException e) {
            log.error("No se encuentra un fichero necesario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Configuracion no valida: {}", e.getMessage());
        } catch (SQLException e) {
            log.error("No se pudo conectar con la base de datos: {}", e.getMessage());
        }

        log.info("Fin de la aplicacion");
    }
}

