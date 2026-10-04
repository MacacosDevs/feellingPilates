-- Elimina el permiso 'venta.reembolsar' junto con el endpoint de reembolso
-- de compras por Stripe (POST /api/pagos/compras/{id}/reembolso), que se
-- quita del backend porque Stripe aun no esta integrado y nada lo usa.
DELETE FROM rol_permiso WHERE permiso_id IN (SELECT id FROM permiso WHERE codigo = 'venta.reembolsar');
DELETE FROM permiso WHERE codigo = 'venta.reembolsar';
