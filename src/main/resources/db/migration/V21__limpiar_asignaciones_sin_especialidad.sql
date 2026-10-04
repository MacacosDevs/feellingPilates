-- V21: el backfill de V20 cruzo instructores x actividades del bloque sin
-- validar especialidad (ese cruce no existia como restriccion antes). Ahora
-- que se valida, se quitan las combinaciones que ya eran inconsistentes:
-- un instructor asignado en un turno a una actividad que no tiene registrada
-- como especialidad (instructor_actividad).
DELETE FROM turno_instructor_asignacion tia
WHERE NOT EXISTS (
    SELECT 1 FROM instructor_actividad ia
    WHERE ia.usuario_id = tia.usuario_id AND ia.tipo_actividad_id = tia.tipo_actividad_id
);
