-- F2D.2: infraestructura interna dark-launch para ajustes puntuales de programacion.
-- No migra datos legacy ni corrige automaticamente programacion recurrente existente.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM programacion_asignacion a
        JOIN programacion_asignacion b
          ON a.id < b.id
         AND a.serie_id = b.serie_id
         AND a.activo
         AND b.activo
         AND daterange(a.vigente_desde, a.vigente_hasta, '[]')
             && daterange(b.vigente_desde, b.vigente_hasta, '[]')
    ) THEN
        RAISE EXCEPTION
            'Existen vigencias activas solapadas en programacion_asignacion; V47 no repara datos';
    END IF;
END
$$;

ALTER TABLE programacion_asignacion
    ADD CONSTRAINT ex_programacion_asignacion_serie_vigencia
        EXCLUDE USING gist (
            serie_id WITH =,
            daterange(vigente_desde, vigente_hasta, '[]') WITH &&
        )
        WHERE (activo);

CREATE TABLE programacion_ajuste_fecha (
    id                           UUID NOT NULL,
    tipo                         VARCHAR(16) NOT NULL,
    fecha                        DATE NOT NULL,
    asignacion_serie_id          UUID,
    salon_resultado_id           UUID REFERENCES salon (id),
    instructor_resultado_id      UUID REFERENCES usuario (id),
    tipo_actividad_resultado_id  UUID REFERENCES tipo_actividad (id),
    hora_inicio_resultado        TIME WITHOUT TIME ZONE,
    hora_fin_resultado           TIME WITHOUT TIME ZONE,
    activo                       BOOLEAN NOT NULL DEFAULT true,
    creado_en                    TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT programacion_ajuste_fecha_pkey PRIMARY KEY (id),

    CONSTRAINT chk_programacion_ajuste_tipo
        CHECK (tipo IN ('CANCELACION', 'REEMPLAZO', 'ADICION')),

    CONSTRAINT chk_programacion_ajuste_forma CHECK (
        (tipo = 'CANCELACION'
            AND asignacion_serie_id IS NOT NULL
            AND salon_resultado_id IS NULL
            AND instructor_resultado_id IS NULL
            AND tipo_actividad_resultado_id IS NULL
            AND hora_inicio_resultado IS NULL
            AND hora_fin_resultado IS NULL)
        OR
        (tipo = 'REEMPLAZO'
            AND asignacion_serie_id IS NOT NULL
            AND salon_resultado_id IS NOT NULL
            AND instructor_resultado_id IS NOT NULL
            AND tipo_actividad_resultado_id IS NOT NULL
            AND hora_inicio_resultado IS NOT NULL
            AND hora_fin_resultado IS NOT NULL)
        OR
        (tipo = 'ADICION'
            AND asignacion_serie_id IS NULL
            AND salon_resultado_id IS NOT NULL
            AND instructor_resultado_id IS NOT NULL
            AND tipo_actividad_resultado_id IS NOT NULL
            AND hora_inicio_resultado IS NOT NULL
            AND hora_fin_resultado IS NOT NULL)
    ),

    CONSTRAINT chk_programacion_ajuste_rango CHECK (
        hora_fin_resultado IS NULL
        OR hora_fin_resultado > hora_inicio_resultado
    )
);

CREATE UNIQUE INDEX idx_programacion_ajuste_target_activo
    ON programacion_ajuste_fecha (asignacion_serie_id, fecha)
    WHERE activo AND tipo IN ('CANCELACION', 'REEMPLAZO');

CREATE INDEX idx_programacion_ajuste_salon_fecha_activo
    ON programacion_ajuste_fecha (salon_resultado_id, fecha)
    WHERE activo AND salon_resultado_id IS NOT NULL;

CREATE INDEX idx_programacion_ajuste_instructor_fecha_activo
    ON programacion_ajuste_fecha (instructor_resultado_id, fecha)
    WHERE activo AND instructor_resultado_id IS NOT NULL;

CREATE INDEX idx_programacion_ajuste_fecha_activo
    ON programacion_ajuste_fecha (fecha)
    WHERE activo;
