-- Ejecutar una sola vez sobre una base existente.
ALTER TABLE usuario
    ADD COLUMN rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE';

UPDATE usuario
SET rol = 'ADMIN'
WHERE usuario = 'admin';
