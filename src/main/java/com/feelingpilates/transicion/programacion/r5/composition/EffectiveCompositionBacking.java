package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

public record EffectiveCompositionBacking(ReferenciaOcurrencia reference, Origin origin,
        UUID salonId, UUID instructorId, UUID activityId, LocalTime start, LocalTime end,
        NominalAxis nominalAxis, ProgrammingCandidateSnapshot nominalCandidate, NominalBackingSnapshot nominalBacking,
        AdjustmentAxis adjustmentAxis, GenericSourceSnapshot adjustmentSource, AdjustmentBackingSnapshot adjustmentBacking,
        EffectiveValidityEvidence support) {
    public enum Origin { RECURRENT_OCCURRENCE, REPLACEMENT_OCCURRENCE, ADDITION_OCCURRENCE }
    public enum NominalAxis { PRESENT, NOT_APPLICABLE }
    public enum AdjustmentAxis { PRESENT, NOT_APPLICABLE }
}
