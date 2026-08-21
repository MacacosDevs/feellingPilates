-- V38: la cantidad de equipamiento que consume una reserva ya no depende de un
-- "modo de consumo" por recurso, sino de cuantos participantes representa la
-- actividad misma (la mayoria son individuales, salvo casos como Duo Reformer).
-- equipamiento_requerido = participantes_por_reserva x cantidad_por_participante

ALTER TABLE tipo_actividad
    ADD COLUMN participantes_por_reserva SMALLINT NOT NULL DEFAULT 1
        CHECK (participantes_por_reserva >= 1);

ALTER TABLE actividad_recurso RENAME COLUMN cantidad TO cantidad_por_participante;
ALTER TABLE actividad_recurso DROP COLUMN modo_consumo;

-- "Permite actividades en pareja" era un flag informativo en el salon sin ninguna
-- logica atada (no filtraba ni validaba nada); esa responsabilidad ahora vive en
-- la actividad misma via participantes_por_reserva, asi que se retira para no
-- duplicar el concepto.
ALTER TABLE salon DROP COLUMN permite_pareja;
