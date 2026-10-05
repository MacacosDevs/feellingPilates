package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

/** Declared evidence is data, never a physical snapshot capability. */
public record EffectiveCompositionEnvelope(Mode mode, LocalDate date, String businessZoneId,
        String invocationIdentity, String authorityVersion, String completionReceiptIdentity,
        Map<String, Participant> participants) {
    public static final String SYNTHETIC = "NOT_APPLICABLE_SYNTHETIC";
    public enum Mode { SYNTHETIC_DESIGN_FIXTURE, R6_SUPPLIED_COHERENT_READ_FACTS }
    public enum Completion { SYNTHETIC, SUCCESSFUL }
    public record Participant(String sourceName, String schemaFingerprint, String ruleVersion,
            String readScope, String executionIdentity, String snapshotIdentity,
            String physicalResourceIdentity, String transactionBoundaryIdentity, String statementCaptureIdentity,
            Completion completion, String runIdentity, String attemptIdentity, String ruleCatalogVersion,
            String databaseName, String schemaName, String principal, String projectionCatalogVersion,
            String readerInvocationIdentity, String snapshotClaim, String snapshotEvidenceId,
            String statementCaptureCommitment) { }
    public EffectiveCompositionEnvelope {
        var sorted=new TreeMap<String,Participant>((a,b)->Arrays.compareUnsigned(EffectiveCompositionCanonicalizer.text(a),EffectiveCompositionCanonicalizer.text(b)));
        sorted.putAll(Map.copyOf(participants));participants=Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }
}
