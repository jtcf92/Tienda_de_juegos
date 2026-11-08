package tienda.config;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverManagerConnectionProvider implements ConnectionProvider {
    private static final Logger log = LogManager.getLogger(DriverManagerConnectionProvider.class);
    private final String url;
    private final String usuario;
    private final String password;

    public DriverManagerConnectionProvider(Configuracion conf) {
        url= conf.getUrl();
        usuario=conf.getUsuario();
        password=conf.getPassword();
        log.info("DriverManager inciado");
    }

    @Override
    public Connection getConnection() throws SQLException {
        log.debug("Abriendo conexion directa con DriverManager");
        return DriverManager.getConnection(url,usuario,password);
    }

    @Override
    public void close() {

    }
}
