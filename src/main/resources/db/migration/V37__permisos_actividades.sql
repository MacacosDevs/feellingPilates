-- V37: modulo "Actividades" como categoria de permisos independiente de SALONES.
-- Sigue la convencion ya usada en calendario.gestionar / pago.caja.gestionar:
-- "gestionar" cubre crear/editar/desactivar, "leer" solo consulta.
-- No se ata a un rol especifico: se habilita por permiso, igual que el resto del sistema.

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('actividades.leer',      'Ver catalogo de actividades y sus recursos requeridos', 'ACTIVIDADES'),
    ('actividades.gestionar', 'Crear, editar y desactivar actividades y sus recursos requeridos', 'ACTIVIDADES');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN' AND p.codigo IN ('actividades.leer', 'actividades.gestionar');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'actividades.leer'
WHERE r.nombre = 'PERSONAL';
