package tienda.app;

import tienda.config.Configuracion;

import java.io.IOException;

public class Main {
    static void main(String[] args) {
        try {
                Configuracion config = new Configuracion("application.properties");
            System.out.println("Gestor leido: " + config.getGestor());
        } catch (IOException e) {
            System.out.println("No se encuentra el fichero de configuracion: " + e.getMessage());
        }


    }
}
