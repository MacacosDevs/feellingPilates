-- V11: gestion completa de salones - espacios, horarios de operacion,
-- catalogo de tipos de actividad y catalogo/inventario de maquinas.

-- Espacio fisico dentro de un salon (ej. "Sala Reformer 1"). Cada espacio define
-- su propio cupo y si el cupo permite acomodar actividades en pareja.
CREATE TABLE espacio (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salon_id        UUID NOT NULL REFERENCES salon (id),
    nombre          VARCHAR(150) NOT NULL,
    capacidad       SMALLINT NOT NULL,
    permite_pareja  BOOLEAN NOT NULL DEFAULT false,
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_espacio_salon ON espacio (salon_id);

-- Horario de operacion del salon por dia de la semana (0 = domingo ... 6 = sabado).
CREATE TABLE horario_operacion (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salon_id        UUID NOT NULL REFERENCES salon (id),
    dia_semana      SMALLINT NOT NULL CHECK (dia_semana BETWEEN 0 AND 6),
    hora_apertura   TIME NOT NULL,
    hora_cierre     TIME NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (salon_id, dia_semana),
    CHECK (hora_cierre > hora_apertura)
);

-- Catalogo global de tipos de actividad de pilates (Mat, Reformer, Cadillac, etc).
CREATE TABLE tipo_actividad (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Que tipos de actividad ofrece cada salon.
CREATE TABLE salon_tipo_actividad (
    salon_id            UUID NOT NULL REFERENCES salon (id),
    tipo_actividad_id   UUID NOT NULL REFERENCES tipo_actividad (id),
    PRIMARY KEY (salon_id, tipo_actividad_id)
);

-- Catalogo global de tipos de maquina/equipo (Reformer, Cadillac, Silla, Barril, Mat...).
CREATE TABLE tipo_maquina (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Inventario de maquinas por espacio: cuantas maquinas de cada tipo hay en un espacio.
CREATE TABLE espacio_maquina (
    espacio_id      UUID NOT NULL REFERENCES espacio (id),
    tipo_maquina_id UUID NOT NULL REFERENCES tipo_maquina (id),
    cantidad        SMALLINT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (espacio_id, tipo_maquina_id)
);

INSERT INTO tipo_actividad (nombre, descripcion) VALUES
    ('Mat', 'Pilates en colchoneta, sin maquinas'),
    ('Reformer', 'Clase en maquina Reformer'),
    ('Cadillac', 'Clase en maquina Cadillac/Trapecio'),
    ('Silla', 'Clase en Silla (Wunda Chair)'),
    ('Barril', 'Clase en Barril (Barrel)'),
    ('Circuito', 'Clase mixta rotando por varias maquinas');

INSERT INTO tipo_maquina (nombre, descripcion) VALUES
    ('Reformer', 'Maquina Reformer'),
    ('Cadillac', 'Maquina Cadillac/Trapecio'),
    ('Silla', 'Wunda Chair'),
    ('Barril', 'Barrel'),
    ('Colchoneta', 'Mat individual');

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('salon.leer',        'Ver detalle de salones, espacios, horarios y catalogos', 'SALONES'),
    ('salon.administrar', 'Crear y editar salones, espacios, horarios, actividades y maquinas', 'SALONES');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN' AND p.codigo IN ('salon.leer', 'salon.administrar');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'salon.leer'
WHERE r.nombre = 'PERSONAL';
