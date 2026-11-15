package tienda.conexion;

import tienda.config.Configuracion;
import tienda.config.enums.ModoConexion;

import java.sql.SQLException;

public class ConnectionProviderFactory {

    public static ConnectionProvider crear(Configuracion config) throws SQLException {
        if (config.getModoConexion() == ModoConexion.POOL) {
            return new HikariConnectionProvider(config);
        }
        return new DriverManagerConnectionProvider(config);
    }
}