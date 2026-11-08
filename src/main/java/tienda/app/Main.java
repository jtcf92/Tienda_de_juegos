package tienda.app;

import tienda.config.Configuracion;

import java.io.IOException;

public class Main {
    static void main(String[] args) {
        try {
                Configuracion config = new Configuracion("application.properties");
            System.out.println("Gestor:     " + config.getGestor());
            System.out.println("Conexiones: " + config.getModoConexion());
            System.out.println("Simulacion: " + config.getModoSimulacion());
            System.out.println("URL:        " + config.getUrl());
            System.out.println("Usuario:    " + config.getUsuario());
        } catch (IOException e) {
            System.out.println("No se encuentra el fichero de configuracion: " + e.getMessage());
        }
        catch (IllegalArgumentException iae){
            System.out.println("Error en la configuracion: "+iae.getMessage());
        }


    }
}
