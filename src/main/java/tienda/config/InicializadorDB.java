package tienda.config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializadorDB {
    Logger log = LogManager.getLogger(InicializadorDB.class);
    private final ConnectionProvider proveedor;

    public InicializadorDB(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }
    public void crearTablas() throws IOException,SQLException {
        String script = LeerRecurso("/schema.sql");

        String [] sentencias = script.split(";");
        try (Connection conector = proveedor.getConnection();
             Statement st = conector.createStatement()){
            for (String sentencia: sentencias) {
                String sql = sentencia.trim();
                if (!sql.isEmpty()){
                    st.execute(sql);
                }
            }

        log.info("Se ham creado las tablas correctamente");


        } catch (SQLException e) {
            log.error("Error a la hora de crear tablas");
            throw new RuntimeException(e);
        }

    }
    private String LeerRecurso(String ruta) throws IOException {
        InputStream lector = InicializadorDB.class.getResourceAsStream(ruta);
        return new String(lector.readAllBytes(), StandardCharsets.UTF_8);
    }
}
