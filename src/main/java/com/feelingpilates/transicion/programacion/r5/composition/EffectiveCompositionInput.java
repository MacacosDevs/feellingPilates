package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

import com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet;

/** Immutable supplied facts only. Construction acquires no resources. */
public record EffectiveCompositionInput(LocalDate date, String businessZoneId, String ruleVersion,
        EffectiveCompositionEnvelope envelope, NominalProgrammingReadSet nominalReadSet,
        AdjustmentReadSet adjustmentReadSet, EffectiveValidityEvidence validityEvidence) {
    public EffectiveCompositionInput {
        Objects.requireNonNull(date); Objects.requireNonNull(businessZoneId); Objects.requireNonNull(ruleVersion);
        Objects.requireNonNull(envelope); Objects.requireNonNull(nominalReadSet);
        Objects.requireNonNull(adjustmentReadSet); Objects.requireNonNull(validityEvidence);
    }
}
