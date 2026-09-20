-- Reestructura los permisos de Caja para que cada pantalla (Registrar venta,
-- Historial, Catalogo) tenga su propio permiso de "vista" (poder entrar),
-- separado de las acciones dentro de ella (registrar, ver, gestionar).
-- Los UPDATE conservan el mismo id de permiso, asi que las asignaciones ya
-- hechas en rol_permiso se mantienen automaticamente bajo el nuevo codigo.

UPDATE permiso SET codigo = 'pago.caja.vender.registrar', descripcion = 'Registrar una venta en caja'
    WHERE codigo = 'pago.caja';
UPDATE permiso SET codigo = 'pago.caja.historial.vista'
    WHERE codigo = 'pago.vista';
UPDATE permiso SET codigo = 'pago.caja.historial.gestionar'
    WHERE codigo = 'pago.caja.gestionar';
UPDATE permiso SET codigo = 'pago.caja.historial.ver.todos'
    WHERE codigo = 'pago.caja.ver.todos';
UPDATE permiso SET codigo = 'pago.caja.historial.ver.propio'
    WHERE codigo = 'pago.caja.ver.propio';
UPDATE permiso SET codigo = 'pago.caja.catalogo.gestionar'
    WHERE codigo = 'pagos.paquetes.gestionar';

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('pago.caja.vender.vista', 'Acceso a la vista de Registrar venta', 'PAGOS'),
    ('pago.caja.catalogo.vista', 'Acceso a la vista de Catálogo de paquetes', 'PAGOS');
