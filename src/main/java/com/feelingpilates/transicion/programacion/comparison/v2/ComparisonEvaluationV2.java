package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionOmission;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.*;

/** Tagged immutable result: a V2 omission never masquerades as an old DetectorResult. */
public final class ComparisonEvaluationV2 {
    public enum Variant { CORE_V1, EXPLAINED_ADDITION_OMISSION_V2 }
    public record Outcome(int generatedCandidateCount, int candidateCount, MappingStatus mappingStatus,
            ResultStatus resultStatus, EffectiveResultStatus effectiveResultStatus,
            OptionalInt nominalTargetCandidateCount, int effectiveOccurrenceCount, HistoryStatus historyStatus,
            AmbiguityStatus ambiguityStatus, Optional<AmbiguityReason> ambiguityReason, SelectionStatus selectionStatus,
            Optional<ExpectedAbsenceReason> expectedAbsenceReason, Optional<UnsupportedReason> unsupportedReason,
            boolean blocking, BlockingStatus blockingStatus, List<BlockedCapability> blockedCapabilities,
            F2DCompatibilityResult f2dCompatibility) {
        public Outcome { blockedCapabilities = List.copyOf(blockedCapabilities); }
    }
    private final Variant variant;
    private final DetectorEvaluationRequest request;
    private final Optional<DetectorResult> coreResult;
    private final Optional<EffectiveCompositionOmission> omission;
    private final Outcome outcome;
    private final String axis, evidenceCommitment, resultCommitment;
    private ComparisonEvaluationV2(Variant variant, DetectorEvaluationRequest request, Optional<DetectorResult> coreResult,
            Optional<EffectiveCompositionOmission> omission, Outcome outcome, String axis, String evidenceCommitment) {
        this.variant = variant; this.request = request; this.coreResult = coreResult; this.omission = omission;
        this.outcome = outcome; this.axis = axis; this.evidenceCommitment = evidenceCommitment;
        ComparisonCanonicalizerV2.requiredText(axis); ComparisonCanonicalizerV2.requiredText(evidenceCommitment);
        this.resultCommitment = ComparisonCanonicalizerV2.hash("F2E-COMPARISON-EVALUATION-V2", variant, request,
                coreResult, outcome, axis, evidenceCommitment);
    }
    static ComparisonEvaluationV2 core(DetectorEvaluationRequest request, DetectorResult result, String axis, String evidence) {
        ComparisonFailureV2.require(new DetectorClassifier().classify(request).equals(result), "CLASSIFICATION", "exact original core result");
        return new ComparisonEvaluationV2(Variant.CORE_V1, request, Optional.of(result), Optional.empty(),
                fromCore(result), axis, evidence);
    }
    static ComparisonEvaluationV2 adjustmentCore(ComparisonInputV2 input, GenericSourceSnapshot source,
            DetectorEvaluationRequest request, DetectorResult result, String axis, String evidence) {
        ComparisonFailureV2.require(source.equals(request.source()) && new DetectorClassifier().classify(request).equals(result),
                "CLASSIFICATION", "original adjustment core result");
        var omission=OmissionAwareAdjustmentClassifierV2.omissionForSource(input,source);
        ComparisonFailureV2.require(omission.isEmpty() || source.sourceAtomType()==SourceAtomType.NEW_REEMPLAZO,
                "CLASSIFICATION", "only replacement omission uses old core variant");
        return new ComparisonEvaluationV2(Variant.CORE_V1,request,Optional.of(result),omission,fromCore(result),axis,evidence);
    }
    static ComparisonEvaluationV2 omittedAddition(ComparisonInputV2 input, GenericSourceSnapshot source) {
        var proof = OmissionAwareAdjustmentClassifierV2.additionOmission(input, source);
        var request = ComparisonCompatibilityV2.request(input, source,
                new CandidateGenerationResult(List.of(), F2DCompatibilityResult.compatible()), ClassificationContext.newAddition(0),
                RelationCardinality.ONE_TO_ONE, proof.commitment());
        var outcome = new Outcome(0, 0, MappingStatus.NO_CANDIDATES, ResultStatus.DIVERGENT_INCOMPATIBLE,
                EffectiveResultStatus.DIVERGENT, OptionalInt.empty(), 0, HistoryStatus.NOT_APPLICABLE,
                AmbiguityStatus.NOT_AMBIGUOUS, Optional.empty(), SelectionStatus.NOT_SELECTED_BY_DETECTOR,
                Optional.empty(), Optional.empty(), true, BlockingStatus.BLOCKING,
                List.of(BlockedCapability.MAPPING_SELECTION, BlockedCapability.CROSSWALK_PERSISTENCE,
                        BlockedCapability.MATERIAL_RESOLVER, BlockedCapability.MIGRATION, BlockedCapability.CUTOVER),
                F2DCompatibilityResult.compatible());
        return new ComparisonEvaluationV2(Variant.EXPLAINED_ADDITION_OMISSION_V2, request, Optional.empty(),
                Optional.of(proof.omission()), outcome, "EFFECTIVE", proof.commitment());
    }
    private static Outcome fromCore(DetectorResult r) {
        return new Outcome(r.generatedCandidateCount(), r.candidateCount(), r.mappingStatus(), r.resultStatus(),
                r.effectiveResultStatus(), r.nominalTargetCandidateCount(), r.effectiveOccurrenceCount(), r.historyStatus(),
                r.ambiguityStatus(), r.ambiguityReason(), r.selectionStatus(), r.expectedAbsenceReason(), r.unsupportedReason(),
                r.blocking(), r.blockingStatus(), r.blockedCapabilities(), r.f2dCompatibility());
    }
    public Variant variant() { return variant; }
    public DetectorEvaluationRequest request() { return request; }
    public Optional<DetectorResult> coreResult() { return coreResult; }
    public Optional<EffectiveCompositionOmission> omission() { return omission; }
    public Outcome outcome() { return outcome; }
    public String axis() { return axis; }
    public String evidenceCommitment() { return evidenceCommitment; }
    public String resultCommitment() { return resultCommitment; }
    public String evaluationIdentity() { return request.evaluationIdentity(); }
}
