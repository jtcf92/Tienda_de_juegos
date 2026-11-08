package tienda.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuracion {
    private final Gestor gestor;
    private final ModoConexion modoConexion;
    private  final ModoSimulacion modoSimulacion;
    private final String url;
    private final String usuario;
    private final String password;
    private final int poolMaxSize;
    private final int poolTimeoutMs;
    public Configuracion(String rutaFichero) throws IOException {
        //El properties es lo que nos permite leer el fichero de configuracion que hice
        //Asignando una clave valor
        Properties props = new Properties();
        try(FileInputStream entrada = new FileInputStream(rutaFichero)) {
            props.load(entrada);
        }
        gestor = leerGestor(props);
        modoConexion = leerModoConexion(props);
        modoSimulacion=leerModoSimulacion(props);
        String gestorSeleccionado;
        if (gestor==Gestor.MYSQL){
            gestorSeleccionado="mysql";
        }else {
            gestorSeleccionado="h2";
        }
        url =comprobarValor(props,gestorSeleccionado+".url");
        usuario= comprobarValor(props,gestorSeleccionado+".user");
        password = props.getProperty(gestorSeleccionado+ ".password", "").trim();

        poolMaxSize = leerEnteroPositivo(props, "pool.maxSize", 5);
        poolTimeoutMs = leerEnteroPositivo(props, "pool.connectionTimeoutMs", 10000);
    }

    private Gestor leerGestor(Properties props){
        String texto= comprobarValor(props,"database.engine");
        try {
            return Gestor.valueOf(texto.toUpperCase());
        }catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Valor no valido en database.engine: '" + texto + "'. Usa MYSQL o H2");
        }
    }

        private  ModoConexion leerModoConexion(Properties props){
        String texto = comprobarValor(props,"connection.mode");
        try {
            return ModoConexion.valueOf(texto.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException( "Valor no valido en connection.mode: '" + texto + "'. Usa DRIVER_MANAGER o POOL");
        }

        }

        private ModoSimulacion leerModoSimulacion(Properties props){
        String texto = comprobarValor(props,"simulation.mode");
        try {
            return ModoSimulacion.valueOf(texto.toUpperCase());
        }catch (IllegalArgumentException iae){
            throw new IllegalArgumentException(
                    "Valor no valido en simulation.mode: '" + texto + "'. Usa NORMAL o FALLO_TRANSACCION");
        }

      }
    private String comprobarValor (Properties props, String clave){
        String valor =props.getProperty(clave);
        if (valor == null||valor.isBlank()){
            throw  new IllegalArgumentException("Falta la propiedad : "+clave);
        }
        //para quitar los espacios del inicio o final
        return valor.trim();
    }

    private int leerEnteroPositivo (Properties props, String clave,int valorPorDefecto){
    String texto = props.getProperty(clave);

    if (texto ==null|| texto.isBlank()){
        return valorPorDefecto;
    }
        int numero;
    try {
        numero=Integer.parseInt(clave);
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
                "La propiedad " + clave + " debe ser un numero entero: '" + texto.trim() + "'");
    }
    if (numero<=0){
        throw new IllegalArgumentException(
                "La propiedad " + clave + " debe ser mayor que 0: " + numero);
    }

return numero;
    }

    public Gestor getGestor() {
        return gestor;
    }

    public ModoConexion getModoConexion() {
        return modoConexion;
    }

    public ModoSimulacion getModoSimulacion() {
        return modoSimulacion;
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPassword() {
        return password;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public int getPoolTimeoutMs() {
        return poolTimeoutMs;
    }
}
