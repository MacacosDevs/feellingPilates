-- V17: cada turno puede tener una actividad/clase asociada (opcional), tomada
-- de las especialidades del instructor (instructor_actividad).
ALTER TABLE turno_instructor ADD COLUMN tipo_actividad_id UUID REFERENCES tipo_actividad (id);
