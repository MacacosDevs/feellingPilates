-- V6: rol SUPER_ADMIN (control total, incluidos permisos futuros) y permiso
-- para gestionar los permisos de cada rol. SUPER_ADMIN nunca se asocia via
-- rol_permiso: su acceso total se resuelve dinamicamente en el backend
-- (AuthService), asi que automaticamente cubre cualquier permiso nuevo.

INSERT INTO rol (nombre, descripcion) VALUES
    ('SUPER_ADMIN', 'Control total del sistema; tiene todos los permisos automaticamente, incluidos los que se agreguen despues');

INSERT INTO permiso (codigo, descripcion) VALUES
    ('rol.permisos.gestionar', 'Agregar o quitar permisos de un rol (ADMIN, PERSONAL, INSTRUCTOR, CLIENTE)');

-- Cuenta inicial de SUPER_ADMIN. Cambiar la contrasena despues del primer login.
INSERT INTO usuario (correo, contrasena_hash, proveedor_auth, nombre, estatus)
VALUES (
    'superadmin@feelingpilates.com',
    '$2y$10$oIccT8UZqcxOSjc22pEO8OWopHrIdZzg2SMWw2PwRluiIJOORKO6q',
    'local',
    'Super Administrador',
    'activo'
);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u CROSS JOIN rol r
WHERE u.correo = 'superadmin@feelingpilates.com' AND r.nombre = 'SUPER_ADMIN';
