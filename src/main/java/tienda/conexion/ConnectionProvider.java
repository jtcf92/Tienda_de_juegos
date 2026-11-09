package tienda.conexion;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider extends  AutoCloseable {

    Connection getConnection() throws SQLException;

    @Override
    //Lo sobre escribo para evitar que lanse una excepcion
    void close();
}
