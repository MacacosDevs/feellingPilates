-- V5: separar el permiso de activar/desactivar usuarios del de administracion completa
-- ADMIN y PERSONAL podran activar/desactivar; solo ADMIN podra crear personal,
-- eliminar (borrado logico) y asignar roles (usuario.administrar / rol.asignar).

INSERT INTO permiso (codigo, descripcion) VALUES
    ('usuario.activar', 'Activar (reactivar) y desactivar (suspender) usuarios');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'usuario.activar'
WHERE r.nombre IN ('ADMIN', 'PERSONAL');
