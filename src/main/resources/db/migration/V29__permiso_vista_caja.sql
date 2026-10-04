-- Separa "poder entrar a la vista de Historial de caja" de "poder ver contenido
-- dentro de ella" (pago.caja.ver.propio/pago.caja.ver.todos). Sin este permiso
-- la vista ni siquiera aparece en el menu; con el pero sin ninguno de los otros
-- dos, la vista carga pero no muestra ningun registro.
INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('pago.vista', 'Acceso a la vista de Historial de caja', 'PAGOS');
