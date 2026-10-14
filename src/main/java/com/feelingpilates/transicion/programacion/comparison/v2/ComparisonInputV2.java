package com.feelingpilates.transicion.programacion.comparison.v2;

import java.time.*;
import java.util.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveProgrammingCompositionResult;

/** Supplied immutable facts; construction grants no physical owner/admission authority. */
public record ComparisonInputV2(String runIdentity, String invocationIdentity, LocalDate date,
        String businessZoneId, Instant evaluatedAt, String ruleVersion, Mode mode,
        String completionReceiptIdentity, List<ReservationSourceSnapshot> reservations,
        List<GenericSourceSnapshot> legacy, NominalProgrammingReadSet nominalReadSet,
        AdjustmentReadSet adjustmentReadSet, EffectiveProgrammingCompositionResult effectiveResult) {
    public static final String RULE = "F2E-COMPARISON-COMPATIBILITY-V2";
    public enum Mode { SYNTHETIC_DESIGN_FIXTURE, OWNER_AUTHENTICATED_SUPPLIED }
    public ComparisonInputV2 {
        try {
            for (String text : List.of(runIdentity, invocationIdentity, businessZoneId, ruleVersion, completionReceiptIdentity))
                ComparisonCanonicalizerV2.requiredText(text);
            Objects.requireNonNull(date); Objects.requireNonNull(evaluatedAt); Objects.requireNonNull(mode);
            ZoneId.of(businessZoneId);
            ComparisonFailureV2.require(RULE.equals(ruleVersion), "INPUT", "fixed comparison rule");
            Objects.requireNonNull(nominalReadSet); Objects.requireNonNull(adjustmentReadSet); Objects.requireNonNull(effectiveResult);
            reservations = ordered(reservations); legacy = ordered(legacy);
        } catch (ComparisonFailureV2 e) { throw e; }
        catch (RuntimeException e) { throw new ComparisonFailureV2("INPUT", "invalid supplied comparison input", e); }
    }
    private static <T extends SourceSnapshot> List<T> ordered(List<T> values) {
        var copy = new ArrayList<>(List.copyOf(values));
        copy.sort(Comparator.comparing((T s) -> s.sourceAtomType().ordinal())
                .thenComparing(SourceSnapshot::sourceIdentity, ComparisonCanonicalizerV2.TEXT_ORDER));
        var seen = new HashSet<String>();
        for (T source : copy) ComparisonFailureV2.require(seen.add(source.sourceIdentity()), "INPUT", "duplicate source identity");
        return List.copyOf(copy);
    }
    public List<ProgrammingCandidateSnapshot> nominalCandidates() { return nominalReadSet.candidates(); }
    public List<GenericSourceSnapshot> adjustmentSources() { return adjustmentReadSet.sources(); }
    public List<ProgrammingCandidateSnapshot> effectiveCandidates() { return effectiveResult.candidates(); }
    public List<String> compositionBinding() {
        return List.of(effectiveResult.inputCommitment(), effectiveResult.resultContentFingerprint(), effectiveResult.resultSnapshotIdentity());
    }
}
