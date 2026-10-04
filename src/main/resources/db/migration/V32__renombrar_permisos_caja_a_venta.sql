-- Renombra el modulo "Caja" a "Ventas": actualiza los codigos de permiso
-- existentes para reflejar la nueva convencion venta.<submodulo>.<accion>.
UPDATE permiso SET codigo = 'venta.registrar.vista' WHERE codigo = 'pago.caja.vender.vista';
UPDATE permiso SET codigo = 'venta.registrar.crear' WHERE codigo = 'pago.caja.vender.registrar';
UPDATE permiso SET codigo = 'venta.gestion.vista' WHERE codigo = 'pago.caja.historial.vista';
UPDATE permiso SET codigo = 'venta.gestion.gestionar' WHERE codigo = 'pago.caja.historial.gestionar';
UPDATE permiso SET codigo = 'venta.gestion.ver.todos' WHERE codigo = 'pago.caja.historial.ver.todos';
UPDATE permiso SET codigo = 'venta.gestion.ver.propio' WHERE codigo = 'pago.caja.historial.ver.propio';
UPDATE permiso SET codigo = 'venta.catalogo.vista' WHERE codigo = 'pago.caja.catalogo.vista';
UPDATE permiso SET codigo = 'venta.catalogo.gestionar' WHERE codigo = 'pago.caja.catalogo.gestionar';
UPDATE permiso SET codigo = 'venta.reembolsar' WHERE codigo = 'pagos.reembolsar';
