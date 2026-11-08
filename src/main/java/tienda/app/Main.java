package tienda.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.config.Configuracion;
import tienda.config.ConnectionProvider;
import tienda.config.DriverManagerConnectionProvider;
import tienda.config.HikariConnectionProvider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Arranque de la aplicacion");

        try {
            Configuracion config = new Configuracion("application.properties");

            try (ConnectionProvider proveedor = new HikariConnectionProvider(config);
                 Connection con = proveedor.getConnection();Connection con2 = proveedor.getConnection()) {

                DatabaseMetaData meta = con.getMetaData();
                log.info("Conectado a {} {}", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
            }

        } catch (IOException e) {
            log.error("No se encuentra el fichero de configuracion: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Configuracion no valida: {}", e.getMessage());
        } catch (SQLException e) {
            log.error("No se pudo conectar con la base de datos: {}", e.getMessage());
        }

        log.info("Fin de la aplicacion");
    }
}

