-- V40: etiquetas de busqueda libres por actividad, independientes del nombre.
-- Permiten que actividades con nombres distintos (ej. "Clase Duo reformer" y
-- "Clase reformer pilates") aparezcan juntas al buscar por un termino comun
-- (ej. "Reformer") en apps de cliente.

ALTER TABLE tipo_actividad ADD COLUMN etiquetas TEXT[] NOT NULL DEFAULT '{}';
