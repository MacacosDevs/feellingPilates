-- V16: permiso para reembolsar compras. Reembolsar dinero real es una accion
-- delicada que solo el personal administrativo deberia poder hacer, no un
-- cliente sobre si mismo.
INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('pagos.reembolsar', 'Reembolsar una compra ya pagada', 'PAGOS');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN' AND p.codigo = 'pagos.reembolsar';
