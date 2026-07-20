-- V1: esquema base de usuarios y RBAC

CREATE TABLE usuario (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correo           VARCHAR(255) NOT NULL UNIQUE,
    contrasena_hash  VARCHAR(255),                -- null si el usuario entra por OAuth
    proveedor_auth   VARCHAR(20)  NOT NULL DEFAULT 'local'
                     CHECK (proveedor_auth IN ('local', 'google')),
    nombre           VARCHAR(150) NOT NULL,
    telefono         VARCHAR(30),
    foto_url         VARCHAR(500),
    descripcion      TEXT,
    estatus          VARCHAR(20)  NOT NULL DEFAULT 'activo'
                     CHECK (estatus IN ('activo', 'suspendido', 'eliminado')),
    creado_en        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE rol (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(50)  NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE permiso (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE usuario_rol (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuario (id),
    rol_id          UUID NOT NULL REFERENCES rol (id),
    -- TODO: agregar FK a la tabla ubicacion cuando exista el modulo de sedes
    ubicacion_id    UUID,                         -- null = asignacion global
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuario_rol UNIQUE (usuario_id, rol_id, ubicacion_id)
);

CREATE INDEX idx_usuario_rol_usuario ON usuario_rol (usuario_id);

CREATE TABLE rol_permiso (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id          UUID NOT NULL REFERENCES rol (id),
    permiso_id      UUID NOT NULL REFERENCES permiso (id),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_rol_permiso UNIQUE (rol_id, permiso_id)
);

CREATE TABLE perfil_instructor (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id              UUID NOT NULL UNIQUE REFERENCES usuario (id),
    sobre_su_clase          TEXT,
    info_horarios           JSONB,
    calificacion_promedio   NUMERIC(3, 2),        -- reservado para futuro
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);
