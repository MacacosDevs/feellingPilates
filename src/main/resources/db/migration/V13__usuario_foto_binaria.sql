-- V13: guarda la foto de perfil directo en la base (bytea), sin servicio externo.
ALTER TABLE usuario ADD COLUMN foto_datos BYTEA;
ALTER TABLE usuario ADD COLUMN foto_tipo VARCHAR(50);
