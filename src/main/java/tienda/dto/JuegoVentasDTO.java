package tienda.dto;

import java.math.BigDecimal;

//Un juego con su ventas
public record JuegoVentasDTO(String titulo, int unidades, BigDecimal ingresos) {
}