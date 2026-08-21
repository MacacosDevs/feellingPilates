-- Rename tipo_maquina -> tipo_recurso (machine/equipment catalog renamed to a broader "resource" concept)
ALTER TABLE tipo_maquina RENAME TO tipo_recurso;

-- Rename salon_maquina -> salon_recurso, and its FK column tipo_maquina_id -> tipo_recurso_id
ALTER TABLE salon_maquina RENAME TO salon_recurso;
ALTER TABLE salon_recurso RENAME COLUMN tipo_maquina_id TO tipo_recurso_id;

-- Dev-only clean slate: wipe existing catalog + everything that references it, per explicit
-- user approval. None of the parent tables below (turno_instructor, paquete, compra, reserva's
-- containing tables) are dropped or altered structurally - only rows that point at a
-- tipo_actividad row being deleted are cleared, so those rows lose their activity link but
-- the tables/records themselves remain.
DELETE FROM salon_recurso;
DELETE FROM salon_tipo_actividad;
DELETE FROM turno_instructor_asignacion;
DELETE FROM instructor_actividad;
DELETE FROM paquete_actividad;
DELETE FROM reserva;
DELETE FROM tipo_recurso;
DELETE FROM tipo_actividad;

-- New table: which resources an activity requires
CREATE TABLE actividad_recurso (
    tipo_actividad_id UUID NOT NULL REFERENCES tipo_actividad (id) ON DELETE CASCADE,
    tipo_recurso_id   UUID NOT NULL REFERENCES tipo_recurso (id) ON DELETE CASCADE,
    cantidad          SMALLINT NOT NULL CHECK (cantidad > 0),
    modo_consumo      VARCHAR(20) NOT NULL DEFAULT 'POR_PARTICIPANTE'
        CHECK (modo_consumo IN ('POR_PARTICIPANTE', 'POR_SESION')),
    PRIMARY KEY (tipo_actividad_id, tipo_recurso_id)
);
