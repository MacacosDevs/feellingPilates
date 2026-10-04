-- V41: modelo base aditivo de programacion.
-- Las tablas nacen vacias y coexisten con el calendario legado.

CREATE TABLE programacion_bloque (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    serie_id        UUID NOT NULL,
    salon_id        UUID NOT NULL REFERENCES salon (id),
    dia_semana      SMALLINT NOT NULL,
    hora_inicio     TIME WITHOUT TIME ZONE NOT NULL,
    hora_fin        TIME WITHOUT TIME ZONE NOT NULL,
    vigente_desde  DATE NOT NULL,
    vigente_hasta  DATE,
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (dia_semana BETWEEN 0 AND 6),
    CHECK (hora_fin > hora_inicio),
    CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)
);

CREATE INDEX idx_programacion_bloque_salon_dia_vigencia
    ON programacion_bloque (salon_id, dia_semana, vigente_desde);
CREATE INDEX idx_programacion_bloque_serie
    ON programacion_bloque (serie_id);

CREATE TABLE programacion_asignacion (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    serie_id            UUID NOT NULL,
    bloque_id           UUID NOT NULL REFERENCES programacion_bloque (id),
    instructor_id       UUID NOT NULL REFERENCES usuario (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    hora_inicio         TIME WITHOUT TIME ZONE NOT NULL,
    hora_fin            TIME WITHOUT TIME ZONE NOT NULL,
    vigente_desde      DATE NOT NULL,
    vigente_hasta      DATE,
    activo              BOOLEAN NOT NULL DEFAULT true,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (hora_fin > hora_inicio),
    CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)
);

CREATE INDEX idx_programacion_asignacion_bloque
    ON programacion_asignacion (bloque_id);
CREATE INDEX idx_programacion_asignacion_instructor_vigencia
    ON programacion_asignacion (instructor_id, vigente_desde);
CREATE INDEX idx_programacion_asignacion_serie
    ON programacion_asignacion (serie_id);
