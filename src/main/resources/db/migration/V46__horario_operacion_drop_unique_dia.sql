-- V46: retira el UNIQUE(salon_id, dia_semana) historico. A partir de aqui
-- la unica garantia de no-duplicidad/no-solape para horario_operacion es
-- ex_horario_operacion_vigencia (V45), que permite N versiones por
-- salon_id + dia_semana siempre que sus vigencias no se intersecten.
--
-- No se toca: PK, CHECK de horas, CHECK de dia_semana, CHECK de vigencia,
-- ni el EXCLUDE de V45.

ALTER TABLE horario_operacion
    DROP CONSTRAINT horario_operacion_salon_id_dia_semana_key;
