-- V42: politicas de confirmacion/reserva/materializacion del salon.
-- Aditivo. Los valores por defecto preservan el comportamiento actual: sin gate
-- de confirmacion, sin restriccion adicional de anticipacion, y las columnas de
-- configuracion todavia no consumida quedan NULL (no configuradas).

ALTER TABLE salon
    ADD COLUMN requiere_confirmacion_instructor       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN plazo_respuesta_confirmacion_horas      SMALLINT,
    ADD COLUMN anticipacion_maxima_reserva_horas       SMALLINT,
    ADD COLUMN anticipacion_minima_reserva_horas       SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN margen_materializacion_dias             SMALLINT;

ALTER TABLE salon
    ADD CONSTRAINT chk_salon_anticipacion_minima_no_negativa
        CHECK (anticipacion_minima_reserva_horas >= 0),
    ADD CONSTRAINT chk_salon_anticipacion_maxima_positiva
        CHECK (anticipacion_maxima_reserva_horas IS NULL OR anticipacion_maxima_reserva_horas > 0),
    ADD CONSTRAINT chk_salon_plazo_respuesta_confirmacion_no_negativo
        CHECK (plazo_respuesta_confirmacion_horas IS NULL OR plazo_respuesta_confirmacion_horas >= 0),
    ADD CONSTRAINT chk_salon_margen_materializacion_no_negativo
        CHECK (margen_materializacion_dias IS NULL OR margen_materializacion_dias >= 0),
    ADD CONSTRAINT chk_salon_confirmacion_requiere_plazo
        CHECK (NOT requiere_confirmacion_instructor OR plazo_respuesta_confirmacion_horas IS NOT NULL);
