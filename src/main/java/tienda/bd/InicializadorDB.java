package tienda.bd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializadorDB {

    private static final Logger log = LogManager.getLogger(InicializadorDB.class);
    private final ConnectionProvider proveedor;

    public InicializadorDB(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }

    // Borra y crea las tablas ejecutando schema.sql sentencia a sentencia
    public void crearTablas() throws IOException, SQLException {
        String script = leerRecurso("/schema.sql");
        String[] sentencias = script.split(";");

        try (Connection con = proveedor.getConnection();
             Statement st = con.createStatement()) {
            int ejecutadas = 0;
            for (String sentencia : sentencias) {
                String sql = sentencia.trim();
                if (!sql.isEmpty()) {
                    st.execute(sql);
                    ejecutadas++;
                }
            }
            log.info("Esquema creado: {} sentencias ejecutadas", ejecutadas);
        }
    }

    // Crea el procedimiento almacenado (solo MySQL)
    public void crearProcedimiento() throws IOException, SQLException {
        String sql = leerRecurso("/procedimiento_mysql.sql");
        try (Connection con = proveedor.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DROP PROCEDURE IF EXISTS sp_ventas_juego");
            st.execute(sql);
        }
        log.info("Procedimiento almacenado sp_ventas_juego creado");
    }

    // Lee un fichero de src/main/resources como texto
    private String leerRecurso(String ruta) throws IOException {
        try (InputStream lector = InicializadorDB.class.getResourceAsStream(ruta)) {
            if (lector == null) {
                throw new IOException("No se encuentra el recurso " + ruta);
            }
            return new String(lector.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}