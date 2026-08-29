-- V20: cada instructor de un turno puede dar solo un subconjunto de las
-- actividades del bloque, no todas. Se reemplaza turno_instructor_actividad
-- (actividades planas del bloque) por una tabla que liga instructor+actividad.
CREATE TABLE turno_instructor_asignacion (
    turno_id            UUID NOT NULL REFERENCES turno_instructor (id),
    usuario_id           UUID NOT NULL REFERENCES usuario (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    PRIMARY KEY (turno_id, usuario_id, tipo_actividad_id)
);

-- Backfill: como hoy cualquier instructor del bloque puede dar cualquier
-- actividad del bloque, se parte de "todos dan todo" (cruce), editable luego.
INSERT INTO turno_instructor_asignacion (turno_id, usuario_id, tipo_actividad_id)
    SELECT tiu.turno_id, tiu.usuario_id, tia.tipo_actividad_id
    FROM turno_instructor_usuario tiu
    JOIN turno_instructor_actividad tia ON tia.turno_id = tiu.turno_id;

CREATE INDEX idx_turno_instructor_asignacion_usuario ON turno_instructor_asignacion (usuario_id);

DROP TABLE turno_instructor_actividad;
