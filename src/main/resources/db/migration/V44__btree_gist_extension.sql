-- V44: habilita btree_gist, requerido por el EXCLUDE constraint de V45
-- sobre (salon_id, dia_semana, daterange(vigente_desde, vigente_hasta)).
-- No modifica horario_operacion ni ningun otro dato.

CREATE EXTENSION IF NOT EXISTS btree_gist;
