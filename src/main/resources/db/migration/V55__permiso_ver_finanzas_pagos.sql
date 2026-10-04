-- V33: permiso para ver informacion financiera de Stripe (payouts, balance).
-- Separado de 'pagos.reembolsar' porque son capacidades distintas: uno mueve
-- dinero (reembolsar), este solo lo consulta.
INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('pagos.ver_finanzas', 'Ver payouts y balance de Stripe', 'PAGOS');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN' AND p.codigo = 'pagos.ver_finanzas';
