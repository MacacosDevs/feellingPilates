-- Agrupa varias lineas de compra (Compra) confirmadas en un mismo checkout de
-- caja bajo un mismo ticket/grupo, y numera cada linea dentro del grupo.
ALTER TABLE compra ADD COLUMN grupo_compra_id UUID;
ALTER TABLE compra ADD COLUMN numero_item INTEGER;
CREATE INDEX idx_compra_grupo ON compra (grupo_compra_id);
