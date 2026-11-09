package tienda.bd;


    import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
    import tienda.conexion.ConnectionProvider;


    import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

    public class CargarDatos {

        private static final Logger log = LogManager.getLogger(CargarDatos.class);

        private final ConnectionProvider proveedor;

        public CargarDatos(ConnectionProvider proveedor) {
            this.proveedor = proveedor;
        }

        // Carga todos los datos de ejemplo
        public void cargar() throws SQLException {
            try (Connection con = proveedor.getConnection()) {
                int plataformas = insertarPlataformas(con);
                int usuarios = insertarUsuarios(con);
                log.info("Carga inicial completada: {} plataformas, {} usuarios", plataformas, usuarios);
            }
        }

        private int insertarPlataformas(Connection con) throws SQLException {
            String[] nombres = {"PS5", "Nintendo Switch", "Xbox Series", "PC"};

          String sql ="INSERT INTO PLATAFORMA (nombre) values (?)";
            try(PreparedStatement preparedStatement=con.prepareStatement(sql)) {
                for (String nombre:nombres){
                        preparedStatement.setString(1,nombre);
                        preparedStatement.addBatch();
                }
                int [] filasAfectados =preparedStatement.executeBatch();
                log.debug("Se han cargado las plataformas, filas afectadas:{}",filasAfectados.length);
                return filasAfectados.length;
            }
        }

        private int insertarUsuarios(Connection con) throws SQLException {
            String[][] usuarios = {
                    {"Ana Garcia", "ana@correo.com"},
                    {"Luis Perez", "luis@correo.com"},
                    {"Marta Lopez", "marta@correo.com"},
                    {"Carlos Ruiz", "carlos@correo.com"}
            };
            LocalDateTime fechaRegistro = LocalDateTime.of(2026, 1, 10, 10, 0);

            String sql = "INSERT INTO usuario (nombre, email, fecha_registro) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (String[] usuario : usuarios) {
                    ps.setString(1, usuario[0]);
                    ps.setString(2, usuario[1]);
                    ps.setObject(3, fechaRegistro);
                    ps.addBatch();
                }
                int[] resultados = ps.executeBatch();
                log.debug("Lote de usuarios enviado: {} sentencias", resultados.length);
                return resultados.length;
            }
        }
}
