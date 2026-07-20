-- V2: datos iniciales de roles y permisos

INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN',      'Administrador del sistema, acceso total'),
    ('PERSONAL',   'Personal operativo de sede (recepcion, caja, check-in)'),
    ('INSTRUCTOR', 'Instructor de clases'),
    ('CLIENTE',    'Cliente que reserva y asiste a clases');

INSERT INTO permiso (codigo, descripcion) VALUES
    ('usuario.leer',        'Ver listado y detalle de usuarios'),
    ('usuario.administrar', 'Crear, suspender y reactivar usuarios'),
    ('rol.asignar',         'Asignar o quitar roles a usuarios'),
    ('reserva.checkin',     'Registrar check-in de una reserva'),
    ('pago.caja',           'Registrar pagos en caja'),
    ('clase.gestionar',     'Gestionar horarios y clases propias');

-- ADMIN: todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p WHERE r.nombre = 'ADMIN';

-- PERSONAL: operacion de sede
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p
    ON p.codigo IN ('usuario.leer', 'reserva.checkin', 'pago.caja')
WHERE r.nombre = 'PERSONAL';

-- INSTRUCTOR: gestion de sus clases
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p
    ON p.codigo IN ('clase.gestionar', 'reserva.checkin')
WHERE r.nombre = 'INSTRUCTOR';

-- CLIENTE: sin permisos administrativos (sus acciones se cubren con autenticacion simple)
