package com.feelingpilates.transicion.programacion.detector;

import java.util.List;
import java.util.Optional;

import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.LEGACY_HISTORY_HAS_MULTIPLE_PLAUSIBLE_STATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.MULTIPLE_ELIGIBLE_AND_NOMINAL_TARGETS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.MULTIPLE_ELIGIBLE_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.MULTIPLE_NOMINAL_TARGETS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.UNKNOWN_INTENT_MULTIPLE_PLAUSIBLE_INTERPRETATIONS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.NOT_AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockingStatus.BLOCKING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockingStatus.NON_BLOCKING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.INCOMPATIBLE_EVIDENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.LEGACY_CANCELLATION_UNKNOWN_INTENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.LEGACY_EXCEPTION_UNKNOWN_INTENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.LEGACY_HISTORY_REQUIRED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.NEW_ADDITION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.NEW_CANCELLATION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.NEW_REPLACEMENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.REQUIRED_TARGET;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.RESERVATION_HISTORICAL_TARGET;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.DetectionScenario.STANDARD_EVALUATION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.DIVERGENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.EXPECTED_ABSENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.MISSING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.NOT_APPLICABLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.PRESENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ExpectedAbsenceReason.TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_AUTHORITY_CONFLICT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.MULTIPLE_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.NO_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.UNIQUE_CANDIDATE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.CANDIDATE_EVALUATION_COMPLETE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.DIVERGENT_INCOMPATIBLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.UNSUPPORTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SelectionStatus.NOT_SELECTED_BY_DETECTOR;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_CANCELACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_EXCEPCION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_ADICION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_CANCELACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_REEMPLAZO;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.RESERVA;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.UNKNOWN_INTENT;

public final class DetectorClassifier {

    private static final List<DetectorVocabulary.BlockedCapability> ALL_BLOCKED = List.of(
            DetectorVocabulary.BlockedCapability.MAPPING_SELECTION,
            DetectorVocabulary.BlockedCapability.CROSSWALK_PERSISTENCE,
            DetectorVocabulary.BlockedCapability.MATERIAL_RESOLVER,
            DetectorVocabulary.BlockedCapability.MIGRATION,
            DetectorVocabulary.BlockedCapability.CUTOVER);

    public DetectorResult classify(DetectorEvaluationRequest request) {
        DetectorValidation.required(request, "request");
        validateSourceScenario(request.source(), request.classificationContext());

        int generated = request.candidateGeneration().generatedCandidateCount();
        int eligible = request.candidateGeneration().candidateCount();
        DetectorVocabulary.MappingStatus mapping = mappingStatus(eligible);
        Classification classification = semanticClassification(request, eligible);
        boolean nominalMultiplicity = request.classificationContext().nominalTargetCandidateCount()
                .stream().anyMatch(count -> count > 1);
        boolean ambiguous = eligible > 1 || nominalMultiplicity;
        Optional<DetectorVocabulary.AmbiguityReason> ambiguityReason = ambiguous
                ? Optional.of(ambiguityReason(request.classificationContext(), eligible, nominalMultiplicity))
                : Optional.empty();
        boolean blocking = classification.blocking()
                || ambiguous
                || request.candidateGeneration().f2dCompatibility().status() == F2D_AUTHORITY_CONFLICT;
        List<DetectorVocabulary.BlockedCapability> blocked = blocking ? ALL_BLOCKED : List.of();
        String resultHash = resultHash(request, mapping, classification, ambiguityReason, blocking);

        return new DetectorResult(
                request.detectorRunIdentity(),
                request.evaluationIdentity(),
                request.detectorVersion(),
                request.ruleId(),
                request.ruleVersion(),
                request.evaluatedAt(),
                request.source(),
                request.expectedTargetAtomType(),
                request.declaredRelationCardinality(),
                request.candidateGeneration().candidates(),
                generated,
                eligible,
                mapping,
                classification.resultStatus(),
                classification.effectiveResultStatus(),
                request.classificationContext().nominalTargetCandidateCount(),
                classification.effectiveOccurrenceCount(),
                request.classificationContext().historyStatus(),
                ambiguous ? AMBIGUOUS : NOT_AMBIGUOUS,
                ambiguityReason,
                NOT_SELECTED_BY_DETECTOR,
                classification.expectedAbsenceReason(),
                classification.unsupportedReason(),
                blocking,
                blocking ? BLOCKING : NON_BLOCKING,
                blocked,
                request.candidateGeneration().f2dCompatibility(),
                request.provenance(),
                request.evidenceHash(),
                resultHash);
    }

    private Classification semanticClassification(DetectorEvaluationRequest request, int eligible) {
        ClassificationContext context = request.classificationContext();
        return switch (context.scenario()) {
            case LEGACY_EXCEPTION_UNKNOWN_INTENT, LEGACY_CANCELLATION_UNKNOWN_INTENT ->
                    unsupported(UNKNOWN_INTENT);
            case LEGACY_HISTORY_REQUIRED -> unsupported(LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED);
            case INCOMPATIBLE_EVIDENCE -> divergent(0);
            case REQUIRED_TARGET -> eligible == 0
                    ? missing(0)
                    : completed(eligible);
            case NEW_CANCELLATION -> classifyCancellation(context);
            case NEW_REPLACEMENT -> classifyReplacement(context);
            case NEW_ADDITION -> classifyAddition(context);
            case RESERVATION_HISTORICAL_TARGET -> classifyHistoricalTarget(
                    (ReservationSourceSnapshot) request.source());
            case STANDARD_EVALUATION -> completed(eligible);
        };
    }

    private Classification classifyCancellation(ClassificationContext context) {
        int nominal = context.nominalTargetCandidateCount().orElseThrow();
        int effective = context.effectiveOccurrenceCount().orElseThrow();
        if (nominal == 0) {
            return effective == 0 ? missing(0) : divergent(effective);
        }
        if (nominal != 1 || effective != 0) {
            return divergent(effective);
        }
        return new Classification(
                DetectorVocabulary.ResultStatus.EXPECTED_ABSENCE,
                EXPECTED_ABSENCE,
                0,
                Optional.of(TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION),
                Optional.empty(),
                false);
    }

    private Classification classifyReplacement(ClassificationContext context) {
        int nominal = context.nominalTargetCandidateCount().orElseThrow();
        int effective = context.effectiveOccurrenceCount().orElseThrow();
        if (nominal == 0) {
            return effective == 0 ? missing(0) : divergent(effective);
        }
        return nominal == 1 && effective == 1 ? completed(1) : divergent(effective);
    }

    private Classification classifyAddition(ClassificationContext context) {
        int effective = context.effectiveOccurrenceCount().orElseThrow();
        return effective == 1 ? completed(1) : effective == 0 ? missing(0) : divergent(effective);
    }

    private Classification classifyHistoricalTarget(ReservationSourceSnapshot source) {
        return switch (source.historicalProgrammingTarget().orElseThrow().currentEffectiveOutcome()) {
            case PRESENT -> completed(1);
            case EXPECTED_ABSENCE -> new Classification(
                    DetectorVocabulary.ResultStatus.EXPECTED_ABSENCE,
                    EXPECTED_ABSENCE,
                    0,
                    Optional.of(TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION),
                    Optional.empty(),
                    false);
            case MISSING -> missing(0);
            case DIVERGENT -> divergent(0);
            case NOT_APPLICABLE -> throw new IllegalStateException("validated historical outcome");
        };
    }

    private Classification completed(int effective) {
        return new Classification(
                CANDIDATE_EVALUATION_COMPLETE,
                effective > 0 ? PRESENT : NOT_APPLICABLE,
                effective,
                Optional.empty(),
                Optional.empty(),
                false);
    }

    private Classification missing(int effective) {
        return new Classification(
                DetectorVocabulary.ResultStatus.MISSING,
                MISSING,
                effective,
                Optional.empty(),
                Optional.empty(),
                true);
    }

    private Classification divergent(int effective) {
        return new Classification(
                DIVERGENT_INCOMPATIBLE,
                DIVERGENT,
                effective,
                Optional.empty(),
                Optional.empty(),
                true);
    }

    private Classification unsupported(DetectorVocabulary.UnsupportedReason reason) {
        return new Classification(
                UNSUPPORTED,
                NOT_APPLICABLE,
                0,
                Optional.empty(),
                Optional.of(reason),
                true);
    }

    private DetectorVocabulary.MappingStatus mappingStatus(int eligible) {
        if (eligible == 0) {
            return NO_CANDIDATES;
        }
        return eligible == 1 ? UNIQUE_CANDIDATE : MULTIPLE_CANDIDATES;
    }

    private DetectorVocabulary.AmbiguityReason ambiguityReason(
            ClassificationContext context,
            int eligible,
            boolean nominalMultiplicity) {
        if (eligible > 1 && nominalMultiplicity) {
            return MULTIPLE_ELIGIBLE_AND_NOMINAL_TARGETS;
        }
        if (nominalMultiplicity) {
            return MULTIPLE_NOMINAL_TARGETS;
        }
        if (context.scenario() == LEGACY_EXCEPTION_UNKNOWN_INTENT
                || context.scenario() == LEGACY_CANCELLATION_UNKNOWN_INTENT) {
            return UNKNOWN_INTENT_MULTIPLE_PLAUSIBLE_INTERPRETATIONS;
        }
        if (context.scenario() == LEGACY_HISTORY_REQUIRED) {
            return LEGACY_HISTORY_HAS_MULTIPLE_PLAUSIBLE_STATES;
        }
        return MULTIPLE_ELIGIBLE_CANDIDATES;
    }

    private void validateSourceScenario(SourceSnapshot source, ClassificationContext context) {
        DetectorValidation.validateSourceSystemAtom(source.sourceSystem(), source.sourceAtomType());
        boolean compatible = switch (source.sourceAtomType()) {
            case LEGACY_EXCEPCION -> context.scenario() == LEGACY_EXCEPTION_UNKNOWN_INTENT;
            case LEGACY_CANCELACION -> context.scenario() == LEGACY_CANCELLATION_UNKNOWN_INTENT;
            case NEW_CANCELACION -> context.scenario() == NEW_CANCELLATION;
            case NEW_REEMPLAZO -> context.scenario() == NEW_REPLACEMENT;
            case NEW_ADICION -> context.scenario() == NEW_ADDITION;
            case LEGACY_RECURRENTE -> context.scenario() == STANDARD_EVALUATION
                    || context.scenario() == REQUIRED_TARGET
                    || context.scenario() == LEGACY_HISTORY_REQUIRED
                    || context.scenario() == INCOMPATIBLE_EVIDENCE;
            case RESERVA -> context.scenario() == STANDARD_EVALUATION
                    || context.scenario() == REQUIRED_TARGET
                    || context.scenario() == RESERVATION_HISTORICAL_TARGET
                    || context.scenario() == INCOMPATIBLE_EVIDENCE;
        };
        if (!compatible) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "source type " + source.sourceAtomType()
                            + " is incompatible with classification scenario " + context.scenario());
        }
        if (source instanceof ReservationSourceSnapshot reservation
                && (context.scenario() == RESERVATION_HISTORICAL_TARGET)
                != reservation.historicalProgrammingTarget().isPresent()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "historical reservation scenario and demonstrated target must be present together");
        }
    }

    private String resultHash(
            DetectorEvaluationRequest request,
            DetectorVocabulary.MappingStatus mapping,
            Classification classification,
            Optional<DetectorVocabulary.AmbiguityReason> ambiguityReason,
            boolean blocking) {
        String candidateHashes = request.candidateGeneration().candidates().stream()
                .map(DetectorCandidate::candidateEvidenceHash)
                .reduce("", (left, right) -> left + '|' + right);
        return SemanticHash.sha256(request.evaluationIdentity() + '|' + request.source().sourceFingerprint()
                + '|' + mapping + '|' + classification + '|' + ambiguityReason + '|'
                + blocking + '|' + request.candidateGeneration().f2dCompatibility().status()
                + candidateHashes);
    }

    private record Classification(
            DetectorVocabulary.ResultStatus resultStatus,
            DetectorVocabulary.EffectiveResultStatus effectiveResultStatus,
            int effectiveOccurrenceCount,
            Optional<DetectorVocabulary.ExpectedAbsenceReason> expectedAbsenceReason,
            Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
            boolean blocking) {
    }
}
