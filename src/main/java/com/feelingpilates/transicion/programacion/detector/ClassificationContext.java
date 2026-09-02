package com.feelingpilates.transicion.programacion.detector;

import java.util.OptionalInt;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
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
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.HistoryStatus.CURRENT_SNAPSHOT_ONLY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.HistoryStatus.NOT_APPLICABLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.HistoryStatus.UNKNOWN_HISTORY;

public record ClassificationContext(
        DetectorVocabulary.DetectionScenario scenario,
        DetectorVocabulary.HistoryStatus historyStatus,
        OptionalInt nominalTargetCandidateCount,
        OptionalInt effectiveOccurrenceCount) {

    public ClassificationContext {
        scenario = required(scenario, "scenario");
        historyStatus = required(historyStatus, "historyStatus");
        nominalTargetCandidateCount = required(
                nominalTargetCandidateCount, "nominalTargetCandidateCount");
        effectiveOccurrenceCount = required(effectiveOccurrenceCount, "effectiveOccurrenceCount");
        if ((nominalTargetCandidateCount.isPresent() && nominalTargetCandidateCount.getAsInt() < 0)
                || (effectiveOccurrenceCount.isPresent() && effectiveOccurrenceCount.getAsInt() < 0)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INCOHERENT_COUNTS,
                    "semantic counts cannot be negative");
        }
        validateShape(scenario, historyStatus, nominalTargetCandidateCount, effectiveOccurrenceCount);
    }

    public static ClassificationContext standard() {
        return withoutCounts(STANDARD_EVALUATION, CURRENT_SNAPSHOT_ONLY);
    }

    public static ClassificationContext requiredTarget() {
        return withoutCounts(REQUIRED_TARGET, CURRENT_SNAPSHOT_ONLY);
    }

    public static ClassificationContext legacyExceptionUnknownIntent() {
        return withoutCounts(LEGACY_EXCEPTION_UNKNOWN_INTENT, CURRENT_SNAPSHOT_ONLY);
    }

    public static ClassificationContext legacyCancellationUnknownIntent() {
        return withoutCounts(LEGACY_CANCELLATION_UNKNOWN_INTENT, CURRENT_SNAPSHOT_ONLY);
    }

    public static ClassificationContext legacyHistoryRequired() {
        return withoutCounts(LEGACY_HISTORY_REQUIRED, UNKNOWN_HISTORY);
    }

    public static ClassificationContext newCancellation(int nominalTargets, int effectiveOccurrences) {
        return new ClassificationContext(
                NEW_CANCELLATION,
                NOT_APPLICABLE,
                OptionalInt.of(nominalTargets),
                OptionalInt.of(effectiveOccurrences));
    }

    public static ClassificationContext newReplacement(int nominalTargets, int effectiveOccurrences) {
        return new ClassificationContext(
                NEW_REPLACEMENT,
                NOT_APPLICABLE,
                OptionalInt.of(nominalTargets),
                OptionalInt.of(effectiveOccurrences));
    }

    public static ClassificationContext newAddition(int effectiveOccurrences) {
        return new ClassificationContext(
                NEW_ADDITION,
                NOT_APPLICABLE,
                OptionalInt.empty(),
                OptionalInt.of(effectiveOccurrences));
    }

    public static ClassificationContext incompatibleEvidence() {
        return withoutCounts(INCOMPATIBLE_EVIDENCE, CURRENT_SNAPSHOT_ONLY);
    }

    public static ClassificationContext reservationHistoricalTarget() {
        return withoutCounts(RESERVATION_HISTORICAL_TARGET, CURRENT_SNAPSHOT_ONLY);
    }

    private static ClassificationContext withoutCounts(
            DetectorVocabulary.DetectionScenario scenario,
            DetectorVocabulary.HistoryStatus historyStatus) {
        return new ClassificationContext(scenario, historyStatus, OptionalInt.empty(), OptionalInt.empty());
    }

    private static void validateShape(
            DetectorVocabulary.DetectionScenario scenario,
            DetectorVocabulary.HistoryStatus historyStatus,
            OptionalInt nominalTargets,
            OptionalInt effectiveOccurrences) {
        boolean countsExpected = scenario == NEW_CANCELLATION || scenario == NEW_REPLACEMENT;
        boolean additionCounts = scenario == NEW_ADDITION;
        if (countsExpected != (nominalTargets.isPresent() && effectiveOccurrences.isPresent())
                || (additionCounts && (nominalTargets.isPresent() || effectiveOccurrences.isEmpty()))
                || (!countsExpected && !additionCounts
                && (nominalTargets.isPresent() || effectiveOccurrences.isPresent()))) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "semantic counts do not match the classification scenario");
        }
        if ((scenario == LEGACY_HISTORY_REQUIRED) != (historyStatus == UNKNOWN_HISTORY)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "UNKNOWN_HISTORY belongs exactly to the legacy history scenario");
        }
    }
}
