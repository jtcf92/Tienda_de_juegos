package tienda.dto;

// Control de existencias: un juego con poco stock
public record StockBajoDTO(String titulo, int stock, int stockMinimo) {
}