INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('clase.reservar', 'Reservar y cancelar la propia reserva en una clase', 'CALENDARIO'),
    ('clase.checkin',  'Ver inscritos y pasar lista por QR en las propias clases', 'CALENDARIO');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre IN ('CLIENTE', 'ADMIN', 'PERSONAL') AND p.codigo = 'clase.reservar';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre IN ('INSTRUCTOR', 'ADMIN', 'PERSONAL') AND p.codigo = 'clase.checkin';
