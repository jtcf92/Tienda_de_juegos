package tienda.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.bd.CargarDatos;
import tienda.bd.InicializadorDB;
import tienda.conexion.ConnectionProvider;
import tienda.conexion.ConnectionProviderFactory;
import tienda.config.Configuracion;
import tienda.config.enums.Gestor;
import tienda.excepcion.NegocioException;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Arranque de la aplicacion");

        try {
            Configuracion config = new Configuracion("application.properties");

            try (ConnectionProvider proveedor = ConnectionProviderFactory.crear(config)) {
                prepararBaseDeDatos(config, proveedor);
                new Simulador(config, proveedor).ejecutar();
            }

        } catch (IOException e) {
            log.error("No se encuentra un fichero necesario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Configuracion no valida: {}", e.getMessage());
        } catch (SQLException e) {
            log.error("Error de base de datos: {}", e.getMessage());
        } catch (NegocioException e) {
            log.error("Operacion no permitida: {}", e.getMessage());
        }

        log.info("Fin de la aplicacion");
    }

    // Tablas, procedimiento (solo MySQL) y datos de ejemplo
    private static void prepararBaseDeDatos(Configuracion config, ConnectionProvider proveedor)
            throws IOException, SQLException {
        InicializadorDB inicializador = new InicializadorDB(proveedor);
        inicializador.crearTablas();
        if (config.getGestor() == Gestor.MYSQL) {
            inicializador.crearProcedimiento();
        }
        new CargarDatos(proveedor).cargar();
    }
}