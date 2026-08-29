-- V18: excepciones puntuales al horario semanal de un salon: cerrado por
-- festivo, o horario especial solo para esa fecha.
CREATE TABLE salon_horario_excepcion (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salon_id        UUID NOT NULL REFERENCES salon (id),
    fecha           DATE NOT NULL,
    cerrado         BOOLEAN NOT NULL DEFAULT false,
    hora_apertura   TIME,
    hora_cierre     TIME,
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (
        (cerrado = true AND hora_apertura IS NULL AND hora_cierre IS NULL)
        OR (cerrado = false AND hora_apertura IS NOT NULL AND hora_cierre IS NOT NULL AND hora_cierre > hora_apertura)
    )
);

CREATE UNIQUE INDEX idx_salon_horario_excepcion_unica ON salon_horario_excepcion (salon_id, fecha) WHERE activo;
CREATE INDEX idx_salon_horario_excepcion_fecha ON salon_horario_excepcion (fecha);
