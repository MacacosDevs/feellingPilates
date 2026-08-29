-- V18: instructoras semilla, con el mismo nombre/bio/calificacion que hoy estan
-- mockeados en la app movil (src/data/clases.ts), para que la pantalla de perfil
-- de instructora pueda leer bio, calificacion y redes sociales desde el backend
-- real en vez de datos fijos en el cliente. El horario de clases en si sigue
-- siendo mock hasta que exista el modulo de clases/reservas.
--
-- Se usan ids fijos (en vez de gen_random_uuid()) para poder referenciarlos desde
-- el cliente. Reutiliza el mismo hash de contrasena temporal que el admin inicial
-- (V4): son cuentas de datos, no pensadas para iniciar sesion todavia.

INSERT INTO usuario (id, correo, contrasena_hash, proveedor_auth, nombre, estatus) VALUES
    ('00000000-0000-4000-8000-000000000001', 'vane.torres@feelingpilates.com',
     '$2y$10$fsj5WiC2Md9MABSj1Ky9SuvHhobzXlgkHia.1RTL4WlsxFlRhNlSC', 'local', 'Vane Torres', 'activo'),
    ('00000000-0000-4000-8000-000000000002', 'ale@feelingpilates.com',
     '$2y$10$fsj5WiC2Md9MABSj1Ky9SuvHhobzXlgkHia.1RTL4WlsxFlRhNlSC', 'local', 'Ale', 'activo'),
    ('00000000-0000-4000-8000-000000000003', 'dany.casillas@feelingpilates.com',
     '$2y$10$fsj5WiC2Md9MABSj1Ky9SuvHhobzXlgkHia.1RTL4WlsxFlRhNlSC', 'local', 'Dany Casillas', 'activo'),
    ('00000000-0000-4000-8000-000000000004', 'rocio.fernandez@feelingpilates.com',
     '$2y$10$fsj5WiC2Md9MABSj1Ky9SuvHhobzXlgkHia.1RTL4WlsxFlRhNlSC', 'local', 'Rocío Fernández', 'activo');

INSERT INTO usuario_rol (usuario_id, rol_id, salon_id)
SELECT u.id,
       (SELECT id FROM rol WHERE nombre = 'INSTRUCTOR'),
       (SELECT id FROM salon ORDER BY creado_en LIMIT 1)
FROM usuario u
WHERE u.id IN (
    '00000000-0000-4000-8000-000000000001',
    '00000000-0000-4000-8000-000000000002',
    '00000000-0000-4000-8000-000000000003',
    '00000000-0000-4000-8000-000000000004'
);

INSERT INTO perfil_instructor (usuario_id, sobre_su_clase, calificacion_promedio, instagram_url) VALUES
    ('00000000-0000-4000-8000-000000000001',
     'Especialista en Reformer. Cree en el movimiento consciente como forma de fuerza y calma.',
     4.9, 'https://www.instagram.com/'),
    ('00000000-0000-4000-8000-000000000002',
     'Especialista en Barre Sculpt. Le apasiona ayudarte a tonificar con técnica y buena energía.',
     4.8, NULL),
    ('00000000-0000-4000-8000-000000000003',
     'Especialista en Mat Pilates. Enfocada en un método consciente, de bajo impacto y gran efectividad.',
     4.9, NULL),
    ('00000000-0000-4000-8000-000000000004',
     'Especialista en Bacu Fit. Rentas de máquina cortas y de alta intensidad, enfocadas en tonificación.',
     4.7, NULL);
