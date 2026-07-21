-- V9: catalogo de ubicaciones (estado -> municipio -> salon).
-- Usa las claves oficiales INEGI para estado/municipio en vez de UUID, para poder
-- cruzar despues con otros catalogos (paqueteria, direcciones, etc). Este catalogo
-- estado/municipio es generico y lo reutilizara tambien el modulo de padel.

CREATE TABLE estado (
    id      SMALLINT PRIMARY KEY,      -- clave INEGI de entidad federativa
    nombre  VARCHAR(100) NOT NULL
);

CREATE TABLE municipio (
    id          SMALLINT NOT NULL,     -- clave INEGI de municipio (unica dentro del estado)
    estado_id   SMALLINT NOT NULL REFERENCES estado (id),
    nombre      VARCHAR(150) NOT NULL,
    PRIMARY KEY (estado_id, id)
);

-- Salon: instalacion fisica de Feeling Pilates dentro de un municipio.
CREATE TABLE salon (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(150) NOT NULL,
    estado_id       SMALLINT NOT NULL,
    municipio_id    SMALLINT NOT NULL,
    direccion       VARCHAR(255),
    activo          BOOLEAN NOT NULL DEFAULT true,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (estado_id, municipio_id) REFERENCES municipio (estado_id, id)
);

CREATE INDEX idx_salon_municipio ON salon (estado_id, municipio_id);

INSERT INTO estado (id, nombre) VALUES (22, 'Querétaro');

INSERT INTO municipio (id, estado_id, nombre) VALUES
    (14, 22, 'Querétaro'),
    (6,  22, 'Corregidora');

-- La ubicacion de un usuario_rol ahora referencia un salon concreto; una fila por
-- salon asignado permite que un mismo usuario/rol tenga varias sedes.
ALTER TABLE usuario_rol RENAME COLUMN ubicacion_id TO salon_id;
ALTER TABLE usuario_rol ADD CONSTRAINT fk_usuario_rol_salon FOREIGN KEY (salon_id) REFERENCES salon (id);
