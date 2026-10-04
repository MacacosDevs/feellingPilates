-- V29: cupo por defecto de una clase de este tipo de actividad. Mismo lugar
-- que ya usa duracion_minutos, porque el limite es por actividad (Bacu Fit
-- limitado por maquinas, Reformer/Barre/Mat por espacio), no por salon.
ALTER TABLE tipo_actividad ADD COLUMN capacidad_predeterminada SMALLINT NOT NULL DEFAULT 8
    CHECK (capacidad_predeterminada > 0);

UPDATE tipo_actividad SET capacidad_predeterminada = 4 WHERE nombre = 'Bacu Fit';
