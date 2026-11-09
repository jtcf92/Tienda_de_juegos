package tienda.config;

import java.sql.SQLException;

public class ConnectionProviderFactory {

    public static ConnectionProvider crear(Configuracion config) throws SQLException {
        if (config.getModoConexion() == ModoConexion.POOL) {
            return new HikariConnectionProvider(config);
        }
        return new DriverManagerConnectionProvider(config);
    }
}