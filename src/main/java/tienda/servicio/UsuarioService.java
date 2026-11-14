package tienda.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tienda.conexion.ConnectionProvider;
import tienda.dao.UsuarioDao;
import tienda.excepcion.NegocioException;

import java.sql.Connection;
import java.sql.SQLException;

public class UsuarioService {

    private static final Logger log = LogManager.getLogger(UsuarioService.class);

    private final ConnectionProvider proveedor;
    private final UsuarioDao usuarioDao = new UsuarioDao();

    public UsuarioService(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }


    public int registrar(String nombre, String email) throws SQLException {
        try (Connection con = proveedor.getConnection()) {
            int id = usuarioDao.insertar(con, nombre, email);
            log.info("Usuario registrado: id={}, nombre={}", id, nombre);
            return id;

        } catch (SQLException e) {

            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                log.warn("Violacion de integridad: mensaje='{}', SQLState={}, codigo={}",
                        e.getMessage(), e.getSQLState(), e.getErrorCode());
                throw new NegocioException("El email " + email + " ya esta registrado");
            }
            throw e;
        }
    }
}