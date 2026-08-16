-- V16: permisos granulares sobre el calendario de instructores.
-- Antes solo existia calendario.administrar (control total). Ahora se
-- distingue entre gestionar (control total), cancelar (solo cancelar turnos
-- existentes) y editar (mover/recortar el horario de un turno existente).

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('calendario.gestionar', 'Crear, mover y eliminar turnos de cualquier instructor', 'CALENDARIO'),
    ('calendario.cancelar',  'Cancelar turnos existentes de instructores, sin crear ni mover horarios', 'CALENDARIO'),
    ('calendario.editar',    'Mover o recortar el horario de turnos existentes de instructores', 'CALENDARIO');

-- Todo rol que ya tenia calendario.administrar recibe ademas calendario.gestionar
-- (union, no reemplazo, para no quitar acceso existente).
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT rp.rol_id, p.id
FROM rol_permiso rp
JOIN permiso admin_p ON admin_p.id = rp.permiso_id AND admin_p.codigo = 'calendario.administrar'
JOIN permiso p ON p.codigo = 'calendario.gestionar'
WHERE NOT EXISTS (
    SELECT 1 FROM rol_permiso existente WHERE existente.rol_id = rp.rol_id AND existente.permiso_id = p.id
);
