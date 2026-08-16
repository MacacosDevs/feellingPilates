-- V19: un bloque de horario deja de pertenecer a un solo instructor y a una
-- sola actividad; ahora puede tener varios de cada uno.
CREATE TABLE turno_instructor_usuario (
    turno_id    UUID NOT NULL REFERENCES turno_instructor (id),
    usuario_id  UUID NOT NULL REFERENCES usuario (id),
    PRIMARY KEY (turno_id, usuario_id)
);
INSERT INTO turno_instructor_usuario (turno_id, usuario_id)
    SELECT id, usuario_id FROM turno_instructor;

CREATE INDEX idx_turno_instructor_usuario_usuario ON turno_instructor_usuario (usuario_id);

CREATE TABLE turno_instructor_actividad (
    turno_id            UUID NOT NULL REFERENCES turno_instructor (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    PRIMARY KEY (turno_id, tipo_actividad_id)
);
INSERT INTO turno_instructor_actividad (turno_id, tipo_actividad_id)
    SELECT id, tipo_actividad_id FROM turno_instructor WHERE tipo_actividad_id IS NOT NULL;

ALTER TABLE turno_instructor DROP COLUMN usuario_id;
ALTER TABLE turno_instructor DROP COLUMN tipo_actividad_id;
