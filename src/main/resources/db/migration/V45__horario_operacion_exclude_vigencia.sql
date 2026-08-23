-- V45: agrega la garantia fisica de no-solape de vigencias para
-- horario_operacion, sin retirar todavia el UNIQUE(salon_id, dia_semana)
-- historico (eso ocurre en V46). Ambas constraints coexisten en esta
-- migracion para demostrar que no existe ventana sin garantia.
--
-- Semantica de limites (ver auditoria/fase-2b3a-persistencia-versionado-horario.md):
--   vigente_desde NULL -> limite inferior abierto.
--   vigente_hasta NULL -> limite superior abierto.
--   ambos NULL         -> rango universal (legado).
-- Los extremos de negocio son inclusivos, expresados aqui con
-- daterange(..., '[]') para que PostgreSQL los canonicalice a su forma
-- semiabierta interna [asc,desc).

ALTER TABLE horario_operacion
    ADD CONSTRAINT ex_horario_operacion_vigencia
        EXCLUDE USING gist (
            salon_id WITH =,
            dia_semana WITH =,
            daterange(vigente_desde, vigente_hasta, '[]') WITH &&
        );
