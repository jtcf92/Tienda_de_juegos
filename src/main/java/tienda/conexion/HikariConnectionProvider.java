package tienda.conexion;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import tienda.config.Configuracion;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionProvider implements ConnectionProvider {
    private static final Logger log = LogManager.getLogger(HikariConnectionProvider.class);
    private final HikariDataSource dataSource;

    public HikariConnectionProvider(Configuracion conf) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(conf.getUrl());
        hikariConfig.setUsername(conf.getUsuario());
        hikariConfig.setPassword(conf.getPassword());
        hikariConfig.setMaximumPoolSize(conf.getPoolMaxSize());
        hikariConfig.setConnectionTimeout(conf.getPoolTimeoutMs());
        hikariConfig.setPoolName("PoolTienda");
        try {
            dataSource = new HikariDataSource(hikariConfig);
        }catch (RuntimeException e) {
            throw new SQLException("No se pudo iniciar el pool de conexiones: " + e.getMessage(), e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        log.debug("Pidiendo una conexion prestada al pool");
        return dataSource.getConnection();
    }

    @Override
    public void close() {
dataSource.close();
        log.info("Pool de conexiones cerrado");
    }
}
