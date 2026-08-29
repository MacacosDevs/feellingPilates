-- V30: ocurrencia concreta de una clase (fecha+hora+salon+instructor+
-- actividad+cupo), materializada bajo demanda a partir de los turnos de
-- instructor. turno_origen_id es solo trazabilidad (de que bloque salio),
-- nullable porque un turno se puede desactivar/borrar despues sin que eso
-- deba afectar clases ya materializadas.
CREATE TABLE clase (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turno_origen_id     UUID REFERENCES turno_instructor (id),
    salon_id            UUID NOT NULL REFERENCES salon (id),
    instructor_id       UUID NOT NULL REFERENCES usuario (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    fecha               DATE NOT NULL,
    hora_inicio         TIME NOT NULL,
    hora_fin            TIME NOT NULL,
    capacidad           SMALLINT NOT NULL CHECK (capacidad > 0),
    estado              VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA' CHECK (estado IN ('PROGRAMADA', 'CANCELADA')),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (hora_fin > hora_inicio),
    -- Hace segura la materializacion perezosa via ON CONFLICT DO NOTHING
    -- cuando dos requests concurrentes piden el mismo rango de fechas.
    UNIQUE (salon_id, instructor_id, tipo_actividad_id, fecha, hora_inicio)
);

CREATE INDEX idx_clase_fecha_salon ON clase (fecha, salon_id);
CREATE INDEX idx_clase_instructor_fecha ON clase (instructor_id, fecha);
