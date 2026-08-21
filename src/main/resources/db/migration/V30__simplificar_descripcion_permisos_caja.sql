-- Descripciones mas cortas: se muestran tal cual en los mensajes de
-- "No tienes permiso: <descripcion>" del frontend.
UPDATE permiso SET descripcion = 'Ver el historial de ventas' WHERE codigo = 'pago.caja.ver.todos';
UPDATE permiso SET descripcion = 'Ver el historial de tus propias ventas' WHERE codigo = 'pago.caja.ver.propio';
