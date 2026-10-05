package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

public record EffectiveCompositionSuppression(ReferenciaOcurrencia reference,
        ProgrammingCandidateSnapshot nominalCandidate, NominalBackingSnapshot nominalBacking,
        GenericSourceSnapshot adjustmentSource, AdjustmentBackingSnapshot adjustmentBacking,
        String ruleVersion, String schemaFingerprint, EffectiveCompositionEnvelope envelope) { }
