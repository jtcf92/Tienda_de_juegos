package tienda.modelo;

import java.math.BigDecimal;

public record Juego(int id, String titulo, BigDecimal precio, boolean activo) {
}
