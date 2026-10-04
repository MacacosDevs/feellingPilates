-- Renombra el submodulo "Catalogo" a "Servicios" dentro de Ventas.
-- Los UPDATE conservan el id del permiso, asi que las asignaciones existentes
-- en rol_permiso se mantienen bajo el nuevo codigo.
UPDATE permiso SET codigo = 'venta.servicios.vista', descripcion = 'Acceso a la vista de Servicios (paquetes y clases)'
    WHERE codigo = 'venta.catalogo.vista';
UPDATE permiso SET codigo = 'venta.servicios.gestionar.ver', descripcion = 'Ver el contenido general de los servicios (paquetes y clases)'
    WHERE codigo = 'venta.catalogo.gestionar.ver';
UPDATE permiso SET codigo = 'venta.servicios.gestionar.crear', descripcion = 'Crear paquetes y clases en Servicios'
    WHERE codigo = 'venta.catalogo.gestionar.crear';
UPDATE permiso SET codigo = 'venta.servicios.gestionar.editar', descripcion = 'Editar paquetes y clases de Servicios'
    WHERE codigo = 'venta.catalogo.gestionar.editar';
UPDATE permiso SET codigo = 'venta.servicios.gestionar.deshabilitar', descripcion = 'Habilitar o deshabilitar paquetes y clases de Servicios'
    WHERE codigo = 'venta.catalogo.gestionar.deshabilitar';
