CREATE PROCEDURE sp_ventas_juego(
    IN  p_id_juego INT,
    OUT p_titulo   VARCHAR(100),
    OUT p_unidades INT,
    OUT p_ingresos DECIMAL(10,2))
BEGIN
    SELECT titulo INTO p_titulo FROM juego WHERE id_juego = p_id_juego;

    SELECT COALESCE(SUM(d.cantidad), 0), COALESCE(SUM(d.importe), 0)
    INTO p_unidades, p_ingresos
    FROM detalle_compra d
    JOIN compra c ON c.id_compra = d.id_compra
    WHERE d.id_juego = p_id_juego AND c.estado <> 'CANCELADA';
END