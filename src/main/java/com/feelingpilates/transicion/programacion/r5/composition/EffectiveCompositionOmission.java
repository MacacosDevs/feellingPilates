package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

public record EffectiveCompositionOmission(ReferenciaOcurrencia reference, EffectiveCompositionBacking backing,
        Cause cause, EffectiveValidityEvidence support, String ruleVersion, String schemaFingerprint,
        EffectiveCompositionEnvelope envelope) {
    public enum Cause {
        SALON_INEXISTENTE_O_INACTIVO, SALON_NO_OPERATIVO_EN_FECHA, AJUSTE_FUERA_DE_HORARIO_EFECTIVO,
        INSTRUCTOR_INEXISTENTE_O_INACTIVO, ROL_INSTRUCTOR_AUSENTE, ACTIVIDAD_INEXISTENTE_O_INACTIVA,
        ESPECIALIDAD_AUSENTE, ACTIVIDAD_NO_OFRECIDA_POR_SALON
    }
}
