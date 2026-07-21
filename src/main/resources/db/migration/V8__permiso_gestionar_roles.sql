-- V8: permiso para crear roles nuevos y editar nombre/descripcion de un rol existente.
-- Distinto de rol.permisos.gestionar (que solo cambia QUE permisos tiene un rol).
-- De momento nadie lo tiene asignado via rol_permiso: solo SUPER_ADMIN lo obtiene,
-- de forma dinamica, igual que el resto de permisos.

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('rol.gestionar', 'Crear roles nuevos y editar su nombre y descripcion', 'ROLES');
