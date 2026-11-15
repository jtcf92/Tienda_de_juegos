package tienda.bd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CargarDatos {

    private static final Logger log = LogManager.getLogger(CargarDatos.class);

    // Datos de un juego de ejemplo
    private record JuegoInicial(String titulo, String precio, int stock, int stockMinimo, String... plataformas) {}

    private final ConnectionProvider proveedor;

    public CargarDatos(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }

    // Carga todos los datos de ejemplo en UNA transaccion
    public void cargar() throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            con.setAutoCommit(false);                                   // empieza la transaccion
            try {
                Map<String, Integer> idsPlataformas = insertarPlataformas(con);
                int usuarios = insertarUsuarios(con);
                int juegos = insertarJuegos(con, idsPlataformas);

                con.commit();                                           // todo bien: se guarda todo
                log.info("Carga inicial completada: {} plataformas, {} usuarios, {} juegos",
                        idsPlataformas.size(), usuarios, juegos);

            } catch (SQLException e) {
                con.rollback();                                         // algo fallo: se deshace todo
                log.warn("Fallo en la carga inicial; se revierten todos los cambios");
                throw e;
            }
        }
    }

    // Una a una, porque necesitamos el id de cada plataforma
    private Map<String, Integer> insertarPlataformas(Connection con) throws SQLException {
        String[] nombres = {"PS5", "Nintendo Switch", "Xbox Series", "PC"};
        Map<String, Integer> ids = new HashMap<>();

        String sql = "INSERT INTO plataforma (nombre) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (String nombre : nombres) {
                ps.setString(1, nombre);
                ps.executeUpdate();

                try (ResultSet claves = ps.getGeneratedKeys()) {
                    claves.next();
                    ids.put(nombre, claves.getInt(1));
                }
            }
        }
        log.debug("Plataformas insertadas con sus ids: {}", ids);
        return ids;
    }

    // Por lotes, porque no necesitamos sus ids
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

    // Cada juego uno a uno (para saber su id); su inventario y sus plataformas, por lotes
    private int insertarJuegos(Connection con, Map<String, Integer> idsPlataformas) throws SQLException {
        JuegoInicial[] juegos = {
                new JuegoInicial("Zelda Tears of the Kingdom", "59.99", 10, 3, "Nintendo Switch"),
                new JuegoInicial("EA Sports FC 26", "69.99", 15, 5, "PS5", "Xbox Series", "PC"),
                new JuegoInicial("Elden Ring", "49.99", 2, 3, "PS5", "Xbox Series", "PC"),
                new JuegoInicial("Mario Kart 8 Deluxe", "49.99", 8, 3, "Nintendo Switch"),
                new JuegoInicial("Minecraft", "29.99", 20, 5, "PS5", "Xbox Series", "Nintendo Switch", "PC"),
                new JuegoInicial("Halo Infinite", "39.99", 0, 2, "Xbox Series", "PC")
        };

        String sqlJuego = "INSERT INTO juego (titulo, precio, activo) VALUES (?, ?, ?)";
        String sqlInventario = "INSERT INTO inventario (id_juego, stock, stock_minimo) VALUES (?, ?, ?)";
        String sqlPlataforma = "INSERT INTO juego_plataforma (id_juego, id_plataforma) VALUES (?, ?)";

        try (PreparedStatement psJuego = con.prepareStatement(sqlJuego, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psInventario = con.prepareStatement(sqlInventario);
             PreparedStatement psPlataforma = con.prepareStatement(sqlPlataforma)) {

            for (JuegoInicial juego : juegos) {
                // 1. Insertar el juego y recuperar su id
                psJuego.setString(1, juego.titulo());
                psJuego.setBigDecimal(2, new BigDecimal(juego.precio()));
                psJuego.setBoolean(3, true);
                psJuego.executeUpdate();

                int idJuego;
                try (ResultSet claves = psJuego.getGeneratedKeys()) {
                    claves.next();
                    idJuego = claves.getInt(1);
                }

                // 2. Su inventario, a la bolsa (relacion 1:1)
                psInventario.setInt(1, idJuego);
                psInventario.setInt(2, juego.stock());
                psInventario.setInt(3, juego.stockMinimo());
                psInventario.addBatch();

                // 3. Sus plataformas, a la bolsa (relacion N:M)
                for (String plataforma : juego.plataformas()) {
                    psPlataforma.setInt(1, idJuego);
                    psPlataforma.setInt(2, idsPlataformas.get(plataforma));
                    psPlataforma.addBatch();
                }
            }

            // 4. Enviar las dos bolsas
            psInventario.executeBatch();
            int[] relaciones = psPlataforma.executeBatch();
            log.debug("Juegos insertados: {}; relaciones juego-plataforma: {}", juegos.length, relaciones.length);
        }
        return juegos.length;
    }
}