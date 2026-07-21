-- V7: categoria de permiso, para agrupar el catalogo en la UI (Usuarios, Roles, Pagos, etc.)
-- Cubre tambien los permisos agregados por V3, V5 y V6, que se crearon antes de esta columna.

ALTER TABLE permiso ADD COLUMN categoria VARCHAR(50);

UPDATE permiso SET categoria = 'USUARIOS' WHERE codigo IN ('usuario.leer', 'usuario.administrar', 'usuario.crear.cliente', 'usuario.activar');
UPDATE permiso SET categoria = 'ROLES'    WHERE codigo IN ('rol.asignar', 'rol.permisos.gestionar');
UPDATE permiso SET categoria = 'RESERVAS' WHERE codigo IN ('reserva.checkin');
UPDATE permiso SET categoria = 'PAGOS'    WHERE codigo IN ('pago.caja');
UPDATE permiso SET categoria = 'CLASES'   WHERE codigo IN ('clase.gestionar');

ALTER TABLE permiso ALTER COLUMN categoria SET NOT NULL;
