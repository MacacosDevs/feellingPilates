-- V38: agrega cuantos participantes representa una reserva de la actividad.
-- Este dato no modifica actividad_recurso.cantidad, que desde V36 representa
-- unidades totales del recurso consumidas por reserva.

ALTER TABLE tipo_actividad
    ADD COLUMN participantes_por_reserva SMALLINT NOT NULL DEFAULT 1
        CHECK (participantes_por_reserva >= 1);

-- salon.permite_pareja queda temporalmente como columna legacy. El Java actual no
-- la mapea y Duo Reformer se modela mediante actividad/recurso, pero retirarla no
-- es necesario para validar JPA y corresponde a una futura migracion contract.
