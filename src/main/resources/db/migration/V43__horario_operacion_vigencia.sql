-- V43: vigencia temporal futura de horario_operacion.
-- Aditivo. Las filas existentes quedan con vigente_desde = NULL y
-- vigente_hasta = NULL, que transicionalmente significa vigencia historica
-- abierta (horario legado sin limites temporales). No se elimina ni se
-- reemplaza el UNIQUE(salon_id, dia_semana) actual: F2A no habilita todavia
-- multiples horarios por salon/dia.

ALTER TABLE horario_operacion
    ADD COLUMN vigente_desde DATE,
    ADD COLUMN vigente_hasta DATE;

ALTER TABLE horario_operacion
    ADD CONSTRAINT chk_horario_operacion_vigencia
        CHECK (vigente_hasta IS NULL OR vigente_desde IS NULL OR vigente_hasta >= vigente_desde);
