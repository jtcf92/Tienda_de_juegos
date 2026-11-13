package tienda.modelo;

import tienda.modelo.enums.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Compra(int id, int idUsuario, LocalDateTime fecha, EstadoCompra estado, BigDecimal total) {
}
