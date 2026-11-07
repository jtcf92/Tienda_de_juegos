package tienda.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuracion {
    private final String gestor;

    public Configuracion(String rutaFichero) throws IOException {
        //El properties es lo que nos permite leer el fichero de configuracion que hice
        //Asignando una clave valor
        Properties props = new Properties();
        try(FileInputStream entrada = new FileInputStream(rutaFichero)) {
            props.load(entrada);
        }
        gestor = props.getProperty("database.engine");
    }

    public String getGestor() {
        return gestor;
    }
}
