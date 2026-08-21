-- V36: amplia el concepto maquina -> recurso conservando catalogo, UUID e inventario.
ALTER TABLE tipo_maquina RENAME TO tipo_recurso;
ALTER TABLE tipo_recurso RENAME CONSTRAINT tipo_maquina_pkey TO tipo_recurso_pkey;
ALTER TABLE tipo_recurso RENAME CONSTRAINT tipo_maquina_nombre_key
    TO tipo_recurso_nombre_key;

ALTER TABLE salon_maquina RENAME TO salon_recurso;
ALTER TABLE salon_recurso RENAME COLUMN tipo_maquina_id TO tipo_recurso_id;
ALTER TABLE salon_recurso RENAME CONSTRAINT salon_maquina_pkey TO salon_recurso_pkey;
ALTER TABLE salon_recurso RENAME CONSTRAINT salon_maquina_cantidad_check
    TO salon_recurso_cantidad_check;
ALTER TABLE salon_recurso RENAME CONSTRAINT salon_maquina_salon_id_fkey
    TO salon_recurso_salon_id_fkey;
ALTER TABLE salon_recurso RENAME CONSTRAINT salon_maquina_tipo_maquina_id_fkey
    TO salon_recurso_tipo_recurso_id_fkey;

-- Relacion aditiva. Nace vacia porque la historia previa no contiene un mapping
-- actividad -> maquina que permita poblarla sin inventar asociaciones.
-- cantidad representa unidades TOTALES consumidas por una reserva.
CREATE TABLE actividad_recurso (
    tipo_actividad_id UUID NOT NULL REFERENCES tipo_actividad (id) ON DELETE CASCADE,
    tipo_recurso_id   UUID NOT NULL REFERENCES tipo_recurso (id) ON DELETE CASCADE,
    cantidad          SMALLINT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (tipo_actividad_id, tipo_recurso_id)
);
