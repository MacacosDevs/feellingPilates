-- V15: calendario interno de instructores (sin integracion externa).
-- Especialidades del instructor, turnos asignados por salon/dia u override por
-- fecha, y reservas de clientes dentro de esos turnos.

-- Duracion por defecto de una clase de este tipo de actividad. Las actividades
-- ligadas a maquinas de uso limitado (ej. media hora) se crean con 30.
ALTER TABLE tipo_actividad ADD COLUMN duracion_minutos SMALLINT NOT NULL DEFAULT 60
    CHECK (duracion_minutos > 0);

-- Que actividades puede impartir cada instructor.
CREATE TABLE instructor_actividad (
    usuario_id          UUID NOT NULL REFERENCES usuario (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    PRIMARY KEY (usuario_id, tipo_actividad_id)
);

-- Turno de un instructor en un salon: recurrente por dia de la semana, o una
-- excepcion/cancelacion puntual que sobrescribe el turno recurrente en una
-- fecha especifica.
CREATE TABLE turno_instructor (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuario (id),
    salon_id        UUID NOT NULL REFERENCES salon (id),
    tipo            VARCHAR(20) NOT NULL CHECK (tipo IN ('RECURRENTE', 'EXCEPCION', 'CANCELACION')),
    dia_semana      SMALLINT CHECK (dia_semana BETWEEN 0 AND 6),
    fecha           DATE,
    hora_inicio     TIME NOT NULL,
    hora_fin        TIME NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (hora_fin > hora_inicio),
    CHECK (
        (tipo = 'RECURRENTE' AND dia_semana IS NOT NULL AND fecha IS NULL)
        OR (tipo IN ('EXCEPCION', 'CANCELACION') AND fecha IS NOT NULL AND dia_semana IS NULL)
    )
);

CREATE INDEX idx_turno_instructor_usuario_salon ON turno_instructor (usuario_id, salon_id);
CREATE INDEX idx_turno_instructor_fecha ON turno_instructor (fecha);

-- Reserva de un cliente con un instructor, dentro de un turno vigente.
CREATE TABLE reserva (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salon_id            UUID NOT NULL REFERENCES salon (id),
    instructor_id       UUID NOT NULL REFERENCES usuario (id),
    cliente_id          UUID NOT NULL REFERENCES usuario (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    fecha               DATE NOT NULL,
    hora_inicio         TIME NOT NULL,
    hora_fin            TIME NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA' CHECK (estado IN ('CONFIRMADA', 'CANCELADA')),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (hora_fin > hora_inicio)
);

CREATE INDEX idx_reserva_instructor_fecha ON reserva (instructor_id, fecha);
CREATE INDEX idx_reserva_cliente ON reserva (cliente_id);

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('calendario.leer',        'Ver turnos y disponibilidad de instructores', 'CALENDARIO'),
    ('calendario.administrar', 'Asignar especialidades y turnos a instructores', 'CALENDARIO'),
    ('reserva.administrar',    'Crear y cancelar reservas de clientes', 'CALENDARIO');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN' AND p.codigo IN ('calendario.leer', 'calendario.administrar', 'reserva.administrar');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'PERSONAL' AND p.codigo IN ('calendario.leer', 'reserva.administrar');
