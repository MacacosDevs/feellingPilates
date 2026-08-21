-- Separa el permiso unico 'venta.catalogo.gestionar' (crear+editar+deshabilitar)
-- en permisos independientes bajo el prefijo venta.catalogo.gestionar.*.
-- El UPDATE conserva el id del permiso, asi que las asignaciones existentes en
-- rol_permiso quedan bajo el nuevo codigo venta.catalogo.gestionar.editar;
-- los roles que necesiten crear/deshabilitar/ver el estado deben reasignarse
-- manualmente en la pantalla de Roles y permisos.
UPDATE permiso SET codigo = 'venta.catalogo.gestionar.editar', descripcion = 'Editar paquetes y clases del catálogo'
    WHERE codigo = 'venta.catalogo.gestionar';

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('venta.catalogo.gestionar.ver', 'Ver el estado (activo/inactivo) de paquetes y clases del catálogo', 'PAGOS'),
    ('venta.catalogo.gestionar.crear', 'Crear paquetes y clases en el catálogo', 'PAGOS'),
    ('venta.catalogo.gestionar.deshabilitar', 'Habilitar o deshabilitar paquetes y clases del catálogo', 'PAGOS');
