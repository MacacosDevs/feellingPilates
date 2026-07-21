-- V3: invitaciones de usuario (alta de clientes via correo) y permiso de alta

INSERT INTO permiso (codigo, descripcion) VALUES
    ('usuario.crear.cliente', 'Dar de alta clientes nuevos (visita o invitacion)');

-- ADMIN y PERSONAL pueden dar de alta clientes (visita en sede)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'usuario.crear.cliente'
WHERE r.nombre IN ('ADMIN', 'PERSONAL');

CREATE TABLE invitacion_usuario (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuario (id),
    token           VARCHAR(128) NOT NULL UNIQUE,
    expira_en       TIMESTAMPTZ NOT NULL,
    usado_en        TIMESTAMPTZ,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invitacion_usuario_token ON invitacion_usuario (token);
CREATE INDEX idx_invitacion_usuario_usuario ON invitacion_usuario (usuario_id);
