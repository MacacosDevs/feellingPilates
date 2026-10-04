-- Separa "ver historial de caja" (propio vs todos) y "gestionar/reembolsar" de
-- "vender" (pago.caja), que hasta hoy cubria las tres cosas con un solo permiso.
-- No se asigna a ningun rol aqui: se otorgan desde la gestion de roles/permisos.
INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('pago.caja.ver.propio', 'Ver el historial de las ventas de caja que uno mismo registró', 'PAGOS'),
    ('pago.caja.ver.todos', 'Ver el historial completo de ventas de caja de todos los cajeros', 'PAGOS'),
    ('pago.caja.gestionar', 'Reembolsar o cambiar el estado de una venta de caja', 'PAGOS');
