package tienda.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Una linea del detalle de una compra, con datos de 4 tablas
public record LineaDetalleDTO(String cliente, LocalDateTime fecha, String estado,
                              String titulo, int cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}