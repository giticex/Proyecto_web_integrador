DELIMITER //

CREATE PROCEDURE sp_registrar_devolucion(
    IN p_id_venta INT,
    IN p_id_producto INT,
    IN p_cantidad INT,
    IN p_motivo VARCHAR(255),
    IN p_monto_reembolso DECIMAL(10,2),
    IN p_numero_nc VARCHAR(50)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

-- 1. Insertar la Nota de Crédito
INSERT INTO devolucion (id_venta, id_producto, cantidad, motivo, monto_reembolsado, numero_nota_credito, fecha_registro)
VALUES (p_id_venta, p_id_producto, p_cantidad, p_motivo, p_monto_reembolso, p_numero_nc, NOW());

-- 2. Reingresar el stock al producto
UPDATE producto
SET stock = stock + p_cantidad
WHERE id_producto = p_id_producto;

COMMIT;
END //

DELIMITER ;