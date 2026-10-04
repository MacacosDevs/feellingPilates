-- Minimum persistence prerequisite selected by the closed reconciled R4 design.
-- Additive and initially empty; no reader/writer activation or assignment exclusion.
CREATE TABLE programacion_ajuste_fecha (
    id                          UUID NOT NULL DEFAULT gen_random_uuid(),
    tipo                        VARCHAR(16) NOT NULL,
    fecha                       DATE NOT NULL,
    asignacion_serie_id          UUID,
    salon_resultado_id           UUID,
    instructor_resultado_id      UUID,
    tipo_actividad_resultado_id  UUID,
    hora_inicio_resultado       TIME WITHOUT TIME ZONE,
    hora_fin_resultado          TIME WITHOUT TIME ZONE,
    activo                      BOOLEAN NOT NULL DEFAULT true,
    creado_en                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT programacion_ajuste_fecha_pkey PRIMARY KEY (id),
    CONSTRAINT fk_ajuste_fecha_salon FOREIGN KEY (salon_resultado_id) REFERENCES salon (id),
    CONSTRAINT fk_ajuste_fecha_instructor FOREIGN KEY (instructor_resultado_id) REFERENCES usuario (id),
    CONSTRAINT fk_ajuste_fecha_tipo_actividad FOREIGN KEY (tipo_actividad_resultado_id) REFERENCES tipo_actividad (id),
    CONSTRAINT chk_ajuste_fecha_tipo CHECK (tipo IN ('CANCELACION', 'REEMPLAZO', 'ADICION')),
    CONSTRAINT chk_ajuste_fecha_forma CHECK (
        (tipo = 'CANCELACION' AND asignacion_serie_id IS NOT NULL
            AND salon_resultado_id IS NULL AND instructor_resultado_id IS NULL
            AND tipo_actividad_resultado_id IS NULL
            AND hora_inicio_resultado IS NULL AND hora_fin_resultado IS NULL)
        OR (tipo = 'REEMPLAZO' AND asignacion_serie_id IS NOT NULL
            AND salon_resultado_id IS NOT NULL AND instructor_resultado_id IS NOT NULL
            AND tipo_actividad_resultado_id IS NOT NULL
            AND hora_inicio_resultado IS NOT NULL AND hora_fin_resultado IS NOT NULL)
        OR (tipo = 'ADICION' AND asignacion_serie_id IS NULL
            AND salon_resultado_id IS NOT NULL AND instructor_resultado_id IS NOT NULL
            AND tipo_actividad_resultado_id IS NOT NULL
            AND hora_inicio_resultado IS NOT NULL AND hora_fin_resultado IS NOT NULL)
    ),
    CONSTRAINT chk_ajuste_fecha_horas CHECK (
        (hora_inicio_resultado IS NULL AND hora_fin_resultado IS NULL)
        OR hora_fin_resultado > hora_inicio_resultado
    )
);

CREATE UNIQUE INDEX uq_ajuste_fecha_target_activo
    ON programacion_ajuste_fecha (asignacion_serie_id, fecha)
    WHERE activo AND tipo IN ('CANCELACION', 'REEMPLAZO');
CREATE INDEX idx_ajuste_fecha_fecha_activo
    ON programacion_ajuste_fecha (fecha) WHERE activo;
