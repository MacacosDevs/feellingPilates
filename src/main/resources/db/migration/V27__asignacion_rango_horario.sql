-- V22: un instructor puede cubrir todo el bloque (tiempo completo) o solo un
-- lapso dentro de el (por ejemplo, de un bloque de 8am a 12pm, un instructor
-- puede cubrir de 10am a 12pm). NULL en ambas columnas significa "tiempo
-- completo" (todo el rango del turno); si se define, aplica a todas las
-- filas de ese instructor en ese turno (una por actividad).
ALTER TABLE turno_instructor_asignacion
    ADD COLUMN hora_inicio TIME NULL,
    ADD COLUMN hora_fin    TIME NULL;
