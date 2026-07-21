-- V4: usuario admin inicial para poder operar el alta de personal/clientes
-- Contrasena temporal: cambiarla despues del primer login.

INSERT INTO usuario (correo, contrasena_hash, proveedor_auth, nombre, estatus)
VALUES (
    'admin@feelingpilates.com',
    '$2y$10$fsj5WiC2Md9MABSj1Ky9SuvHhobzXlgkHia.1RTL4WlsxFlRhNlSC',
    'local',
    'Administrador',
    'activo'
);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u CROSS JOIN rol r
WHERE u.correo = 'admin@feelingpilates.com' AND r.nombre = 'ADMIN';
