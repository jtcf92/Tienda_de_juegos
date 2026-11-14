package tienda.dto;

// Foto del estado de la BD para comprobar el rollback
public record EstadoBdDTO(int compras, int detalles, int movimientosVenta, int stockJuego) {
}