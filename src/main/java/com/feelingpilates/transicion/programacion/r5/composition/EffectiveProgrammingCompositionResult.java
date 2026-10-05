package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

/** Construction is private to this value; only the validated composition pipeline can issue it. */
public final class EffectiveProgrammingCompositionResult {
    private final EffectiveCompositionInput input;
    private final List<ProgrammingCandidateSnapshot> candidates;
    private final Map<ReferenciaOcurrencia,EffectiveCompositionBacking> backingByReference;
    private final List<EffectiveCompositionOmission> omissions;
    private final List<EffectiveCompositionSuppression> suppressions;
    private final String inputCommitment, resultContentFingerprint, resultSnapshotIdentity;
    private EffectiveProgrammingCompositionResult(EffectiveCompositionInput input,
            List<ProgrammingCandidateSnapshot> candidates, Map<ReferenciaOcurrencia,EffectiveCompositionBacking> backing,
            List<EffectiveCompositionOmission> omissions, List<EffectiveCompositionSuppression> suppressions,
            String inputCommitment, String content, String snapshot) {
        this.input=input; this.candidates=List.copyOf(candidates);
        this.backingByReference=Collections.unmodifiableMap(new LinkedHashMap<>(backing));
        this.omissions=List.copyOf(omissions); this.suppressions=List.copyOf(suppressions);
        this.inputCommitment=inputCommitment; this.resultContentFingerprint=content; this.resultSnapshotIdentity=snapshot;
    }
    // Package issuance still revalidates the entire result, so even a same-package caller cannot forge output.
    static EffectiveProgrammingCompositionResult issue(EffectiveCompositionInput input,
            List<ProgrammingCandidateSnapshot> candidates, Map<ReferenciaOcurrencia,EffectiveCompositionBacking> backing,
            List<EffectiveCompositionOmission> omissions, List<EffectiveCompositionSuppression> suppressions,
            String inputCommitment, String content, String snapshot) {
        var result=new EffectiveProgrammingCompositionResult(input,candidates,backing,omissions,suppressions,inputCommitment,content,snapshot);
        EffectiveProgrammingComposer.verifyResult(result);
        return result;
    }
    public LocalDate date() { return input.date(); }
    public String businessZoneId() { return input.businessZoneId(); }
    public String ruleVersion() { return input.ruleVersion(); }
    public EffectiveCompositionEnvelope envelope() { return input.envelope(); }
    public EffectiveCompositionEnvelope.Mode evidenceMode() { return input.envelope().mode(); }
    public EffectiveCompositionInput input() { return input; }
    public List<ProgrammingCandidateSnapshot> candidates() { return candidates; }
    public Map<ReferenciaOcurrencia,EffectiveCompositionBacking> backingByReference() { return backingByReference; }
    public List<EffectiveCompositionOmission> omissions() { return omissions; }
    public List<EffectiveCompositionSuppression> suppressions() { return suppressions; }
    public String inputCommitment() { return inputCommitment; }
    public String resultContentFingerprint() { return resultContentFingerprint; }
    public String resultSnapshotIdentity() { return resultSnapshotIdentity; }
}
