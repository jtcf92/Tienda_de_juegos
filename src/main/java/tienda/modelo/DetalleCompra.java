package tienda.modelo;

import java.math.BigDecimal;

public record DetalleCompra(int idCompra, int idJuego, int cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}
