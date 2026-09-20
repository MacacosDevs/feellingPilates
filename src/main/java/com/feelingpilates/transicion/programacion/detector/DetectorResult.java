package com.feelingpilates.transicion.programacion.detector;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockingStatus.BLOCKING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.DIVERGENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.NOT_APPLICABLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.PRESENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.ELIGIBLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_AUTHORITY_CONFLICT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.MULTIPLE_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.NO_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.UNIQUE_CANDIDATE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.CANDIDATE_EVALUATION_COMPLETE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.DIVERGENT_INCOMPATIBLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.EXPECTED_ABSENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.MISSING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.UNSUPPORTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SelectionStatus.NOT_SELECTED_BY_DETECTOR;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_CANCELACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_EXCEPCION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_RECURRENTE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_ADICION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_CANCELACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_REEMPLAZO;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.LEGACY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.UNKNOWN_INTENT;

public record DetectorResult(
        String detectorRunIdentity,
        String evaluationIdentity,
        String detectorVersion,
        String ruleId,
        String ruleVersion,
        Instant evaluatedAt,
        SourceSnapshot source,
        DetectorVocabulary.TargetAtomType expectedTargetAtomType,
        DetectorVocabulary.RelationCardinality declaredRelationCardinality,
        List<DetectorCandidate> candidates,
        int generatedCandidateCount,
        int candidateCount,
        DetectorVocabulary.MappingStatus mappingStatus,
        DetectorVocabulary.ResultStatus resultStatus,
        DetectorVocabulary.EffectiveResultStatus effectiveResultStatus,
        OptionalInt nominalTargetCandidateCount,
        int effectiveOccurrenceCount,
        DetectorVocabulary.HistoryStatus historyStatus,
        DetectorVocabulary.AmbiguityStatus ambiguityStatus,
        Optional<DetectorVocabulary.AmbiguityReason> ambiguityReason,
        DetectorVocabulary.SelectionStatus selectionStatus,
        Optional<DetectorVocabulary.ExpectedAbsenceReason> expectedAbsenceReason,
        Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
        boolean blocking,
        DetectorVocabulary.BlockingStatus blockingStatus,
        List<DetectorVocabulary.BlockedCapability> blockedCapabilities,
        F2DCompatibilityResult f2dCompatibility,
        EvidenceProvenance provenance,
        String evidenceHash,
        String resultHash) {

    public DetectorResult {
        detectorRunIdentity = requiredText(detectorRunIdentity, "detectorRunIdentity");
        evaluationIdentity = requiredText(evaluationIdentity, "evaluationIdentity");
        detectorVersion = requiredText(detectorVersion, "detectorVersion");
        ruleId = requiredText(ruleId, "ruleId");
        ruleVersion = requiredText(ruleVersion, "ruleVersion");
        evaluatedAt = required(evaluatedAt, "evaluatedAt");
        source = required(source, "source");
        expectedTargetAtomType = required(expectedTargetAtomType, "expectedTargetAtomType");
        declaredRelationCardinality = required(
                declaredRelationCardinality, "declaredRelationCardinality");
        candidates = List.copyOf(required(candidates, "candidates"));
        mappingStatus = required(mappingStatus, "mappingStatus");
        resultStatus = required(resultStatus, "resultStatus");
        effectiveResultStatus = required(effectiveResultStatus, "effectiveResultStatus");
        nominalTargetCandidateCount = required(
                nominalTargetCandidateCount, "nominalTargetCandidateCount");
        historyStatus = required(historyStatus, "historyStatus");
        ambiguityStatus = required(ambiguityStatus, "ambiguityStatus");
        ambiguityReason = required(ambiguityReason, "ambiguityReason");
        selectionStatus = required(selectionStatus, "selectionStatus");
        expectedAbsenceReason = required(expectedAbsenceReason, "expectedAbsenceReason");
        unsupportedReason = required(unsupportedReason, "unsupportedReason");
        blockingStatus = required(blockingStatus, "blockingStatus");
        blockedCapabilities = List.copyOf(required(blockedCapabilities, "blockedCapabilities"));
        f2dCompatibility = required(f2dCompatibility, "f2dCompatibility");
        provenance = required(provenance, "provenance");
        evidenceHash = requiredText(evidenceHash, "evidenceHash");
        resultHash = requiredText(resultHash, "resultHash");

        int eligibleCount = (int) candidates.stream()
                .filter(candidate -> candidate.eligibilityStatus() == ELIGIBLE)
                .count();
        if (generatedCandidateCount != candidates.size() || candidateCount != eligibleCount
                || candidateCount > generatedCandidateCount || effectiveOccurrenceCount < 0
                || nominalTargetCandidateCount.stream().anyMatch(count -> count < 0)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INCOHERENT_COUNTS,
                    "result counts are inconsistent with retained candidates");
        }
        if (selectionStatus != NOT_SELECTED_BY_DETECTOR) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "detector-only results never select");
        }
        if ((ambiguityStatus == AMBIGUOUS) != ambiguityReason.isPresent()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "ambiguity reason is required exactly for ambiguous results");
        }
        if ((resultStatus == EXPECTED_ABSENCE) != expectedAbsenceReason.isPresent()
                || (resultStatus == UNSUPPORTED) != unsupportedReason.isPresent()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "semantic reasons must match their result status");
        }
        if (blocking != (blockingStatus == BLOCKING)
                || blocking != !blockedCapabilities.isEmpty()) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "blocking fields are inconsistent");
        }
        validateMapping(candidateCount, mappingStatus);
        validateAmbiguity(
                source, resultStatus, unsupportedReason, candidateCount, nominalTargetCandidateCount,
                ambiguityStatus, ambiguityReason, blocking);
        validateHistory(source, historyStatus, resultStatus, unsupportedReason, blocking);
        validateBlocking(resultStatus, f2dCompatibility, blocking);
        validateEffectiveResult(resultStatus, effectiveResultStatus, effectiveOccurrenceCount);
        validateSourceSemantics(
                source, nominalTargetCandidateCount, resultStatus, effectiveResultStatus,
                effectiveOccurrenceCount, unsupportedReason, expectedAbsenceReason);
    }

    private static void validateMapping(
            int candidateCount,
            DetectorVocabulary.MappingStatus mappingStatus) {
        DetectorVocabulary.MappingStatus expected = candidateCount == 0
                ? NO_CANDIDATES
                : candidateCount == 1 ? UNIQUE_CANDIDATE : MULTIPLE_CANDIDATES;
        if (mappingStatus != expected) {
            invalid("mapping status is inconsistent with eligible candidate count");
        }
    }

    private static void validateAmbiguity(
            SourceSnapshot source,
            DetectorVocabulary.ResultStatus resultStatus,
            Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
            int candidateCount,
            OptionalInt nominalTargetCandidateCount,
            DetectorVocabulary.AmbiguityStatus ambiguityStatus,
            Optional<DetectorVocabulary.AmbiguityReason> ambiguityReason,
            boolean blocking) {
        boolean eligibleMultiplicity = candidateCount > 1;
        boolean nominalMultiplicity = nominalTargetCandidateCount.stream().anyMatch(count -> count > 1);
        Optional<DetectorVocabulary.AmbiguityReason> expectedReason;
        if (eligibleMultiplicity && nominalMultiplicity) {
            expectedReason = Optional.of(
                    DetectorVocabulary.AmbiguityReason.MULTIPLE_ELIGIBLE_AND_NOMINAL_TARGETS);
        } else if (nominalMultiplicity) {
            expectedReason = Optional.of(DetectorVocabulary.AmbiguityReason.MULTIPLE_NOMINAL_TARGETS);
        } else if (eligibleMultiplicity) {
            expectedReason = Optional.of(source.sourceAtomType() == LEGACY_EXCEPCION
                    || source.sourceAtomType() == LEGACY_CANCELACION
                    ? DetectorVocabulary.AmbiguityReason.UNKNOWN_INTENT_MULTIPLE_PLAUSIBLE_INTERPRETATIONS
                    : resultStatus == UNSUPPORTED
                    && unsupportedReason.orElse(null) == LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED
                    ? DetectorVocabulary.AmbiguityReason.LEGACY_HISTORY_HAS_MULTIPLE_PLAUSIBLE_STATES
                    : DetectorVocabulary.AmbiguityReason.MULTIPLE_ELIGIBLE_CANDIDATES);
        } else {
            expectedReason = Optional.empty();
        }
        if (!ambiguityReason.equals(expectedReason)
                || expectedReason.isPresent() != (ambiguityStatus == AMBIGUOUS)) {
            invalid("ambiguity status and reason must match their reproducible facts");
        }
        if (ambiguityStatus == AMBIGUOUS && !blocking) {
            invalid("ambiguous results must be blocking");
        }
    }

    private static void validateHistory(
            SourceSnapshot source,
            DetectorVocabulary.HistoryStatus historyStatus,
            DetectorVocabulary.ResultStatus resultStatus,
            Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
            boolean blocking) {
        boolean unknownHistory = historyStatus == DetectorVocabulary.HistoryStatus.UNKNOWN_HISTORY;
        boolean unsupportedBecauseHistoryIsUnavailable = resultStatus == UNSUPPORTED
                && unsupportedReason.orElse(null) == LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED;
        if (unknownHistory != unsupportedBecauseHistoryIsUnavailable) {
            invalid("UNKNOWN_HISTORY requires exactly its history-related unsupported semantics");
        }
        if (unknownHistory && (source.sourceSystem() != LEGACY
                || source.sourceAtomType() != LEGACY_RECURRENTE
                || !blocking)) {
            invalid("unavailable legacy functional history must be a blocking legacy recurrent claim");
        }
    }

    private static void validateBlocking(
            DetectorVocabulary.ResultStatus resultStatus,
            F2DCompatibilityResult f2dCompatibility,
            boolean blocking) {
        boolean semanticBlocker = resultStatus == MISSING
                || resultStatus == UNSUPPORTED
                || resultStatus == DIVERGENT_INCOMPATIBLE;
        if ((semanticBlocker || f2dCompatibility.status() == F2D_AUTHORITY_CONFLICT) && !blocking) {
            invalid("semantic blockers and F2D authority conflicts must block");
        }
    }

    private static void validateEffectiveResult(
            DetectorVocabulary.ResultStatus resultStatus,
            DetectorVocabulary.EffectiveResultStatus effectiveResultStatus,
            int effectiveOccurrenceCount) {
        boolean valid = switch (resultStatus) {
            case CANDIDATE_EVALUATION_COMPLETE ->
                    (effectiveResultStatus == PRESENT && effectiveOccurrenceCount > 0)
                            || (effectiveResultStatus == NOT_APPLICABLE && effectiveOccurrenceCount == 0);
            case EXPECTED_ABSENCE -> effectiveResultStatus
                    == DetectorVocabulary.EffectiveResultStatus.EXPECTED_ABSENCE
                    && effectiveOccurrenceCount == 0;
            case MISSING -> effectiveResultStatus == DetectorVocabulary.EffectiveResultStatus.MISSING
                    && effectiveOccurrenceCount == 0;
            case UNSUPPORTED -> effectiveResultStatus == NOT_APPLICABLE && effectiveOccurrenceCount == 0;
            case DIVERGENT_INCOMPATIBLE -> effectiveResultStatus == DIVERGENT;
        };
        if (!valid) {
            invalid("result status and effective result status are incompatible");
        }
    }

    private static void validateSourceSemantics(
            SourceSnapshot source,
            OptionalInt nominalTargetCandidateCount,
            DetectorVocabulary.ResultStatus resultStatus,
            DetectorVocabulary.EffectiveResultStatus effectiveResultStatus,
            int effectiveOccurrenceCount,
            Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
            Optional<DetectorVocabulary.ExpectedAbsenceReason> expectedAbsenceReason) {
        DetectorValidation.validateSourceSystemAtom(source.sourceSystem(), source.sourceAtomType());
        boolean nominalRequired = source.sourceAtomType() == NEW_CANCELACION
                || source.sourceAtomType() == NEW_REEMPLAZO;
        if (nominalRequired != nominalTargetCandidateCount.isPresent()) {
            invalid("nominal target count presence is inconsistent with source atom type");
        }
        if ((source.sourceAtomType() == LEGACY_EXCEPCION
                || source.sourceAtomType() == LEGACY_CANCELACION)
                && (resultStatus != UNSUPPORTED || unsupportedReason.orElse(null) != UNKNOWN_INTENT)) {
            invalid("legacy exception and cancellation sources require UNSUPPORTED plus UNKNOWN_INTENT");
        }
        if (source.sourceAtomType() == NEW_CANCELACION) {
            int nominal = nominalTargetCandidateCount.orElseThrow();
            DetectorVocabulary.ResultStatus expected = nominal == 0 && effectiveOccurrenceCount == 0
                    ? MISSING
                    : nominal == 1 && effectiveOccurrenceCount == 0
                    ? EXPECTED_ABSENCE
                    : DIVERGENT_INCOMPATIBLE;
            if (resultStatus != expected) {
                invalid("new cancellation result is inconsistent with nominal/effective cardinality");
            }
        }
        if (source.sourceAtomType() == NEW_REEMPLAZO) {
            int nominal = nominalTargetCandidateCount.orElseThrow();
            DetectorVocabulary.ResultStatus expected = nominal == 0 && effectiveOccurrenceCount == 0
                    ? MISSING
                    : nominal == 1 && effectiveOccurrenceCount == 1
                    ? CANDIDATE_EVALUATION_COMPLETE
                    : DIVERGENT_INCOMPATIBLE;
            if (resultStatus != expected) {
                invalid("new replacement result is inconsistent with nominal/effective cardinality");
            }
        }
        if (source.sourceAtomType() == NEW_ADICION) {
            DetectorVocabulary.ResultStatus expected = effectiveOccurrenceCount == 0
                    ? MISSING
                    : effectiveOccurrenceCount == 1
                    ? CANDIDATE_EVALUATION_COMPLETE
                    : DIVERGENT_INCOMPATIBLE;
            if (resultStatus != expected) {
                invalid("new addition result is inconsistent with effective cardinality");
            }
        }
        if (source instanceof ReservationSourceSnapshot reservation
                && reservation.historicalProgrammingTarget().isPresent()) {
            DetectorVocabulary.EffectiveResultStatus current = reservation.historicalProgrammingTarget()
                    .orElseThrow().currentEffectiveOutcome();
            if (effectiveResultStatus != current
                    || (current == PRESENT && effectiveOccurrenceCount != 1)
                    || (current != PRESENT && effectiveOccurrenceCount != 0)) {
                invalid("historical target identity and current effective outcome are inconsistent");
            }
            if (current == DetectorVocabulary.EffectiveResultStatus.EXPECTED_ABSENCE
                    && (resultStatus != EXPECTED_ABSENCE || expectedAbsenceReason.isEmpty())) {
                invalid("suppressed historical targets require EXPECTED_ABSENCE and its reason");
            }
        }
    }

    private static void invalid(String message) {
        throw new DetectorInputInvalidException(
                DetectorVocabulary.InputErrorCode.INVALID_SCENARIO, message);
    }
}
