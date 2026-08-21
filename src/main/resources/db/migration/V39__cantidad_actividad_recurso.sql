-- V39: la cantidad en actividad_recurso es el total que requiere la actividad
-- (ej. Duo Reformer = 2 Reformers), no "por participante" - se captura directo,
-- sin multiplicar por participantes_por_reserva. participantes_por_reserva
-- queda como dato informativo (ej. para el instructor), sin efecto en calculos.

ALTER TABLE actividad_recurso RENAME COLUMN cantidad_por_participante TO cantidad;
