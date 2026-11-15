package tienda.modelo;

import java.time.LocalDateTime;

public record Usuario(int id, String nombre, String email, LocalDateTime fechaRegistro) {
}
