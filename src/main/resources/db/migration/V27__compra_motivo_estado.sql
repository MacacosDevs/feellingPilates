-- Guarda el motivo cuando una venta de caja se cancela o se marca como reembolsada.
ALTER TABLE compra ADD COLUMN motivo_estado TEXT;
