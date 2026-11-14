package tienda.app;

import tienda.bd.CargarDatos;
import tienda.conexion.ConnectionProvider;
import tienda.conexion.ConnectionProviderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.bd.InicializadorDB;
import tienda.config.*;
import tienda.dao.JuegoDao;
import tienda.dao.UsuarioDao;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

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
                UsuarioDao usuarioDao = new UsuarioDao();
                try (Connection con = proveedor.getConnection()) {
                    log.info("Juego 3: {}", juegoDao.buscarPorId(con, 3));
                    juegoDao.actualizarPrecio(con, 3, new BigDecimal("39.99"));
                    log.info("Juego 3 con nuevo precio: {}", juegoDao.buscarPorId(con, 3));
                    log.info("Juego 99: {}", juegoDao.buscarPorId(con, 99));
                    log.info("Usuario 1: {}", usuarioDao.buscarPorId(con, 1));
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

