package com.feelingpilates.transicion.programacion.detector;

public final class DetectorVocabulary {

    private DetectorVocabulary() {
    }

    public enum SourceSystem { LEGACY, NEW_DARK_LAUNCH }

    public enum SourceAtomType {
        RESERVA,
        LEGACY_RECURRENTE,
        LEGACY_EXCEPCION,
        LEGACY_CANCELACION,
        NEW_CANCELACION,
        NEW_REEMPLAZO,
        NEW_ADICION
    }

    public enum ReservationState { CONFIRMADA, CANCELADA }

    public enum CandidateType {
        NOMINAL_OCCURRENCE,
        RECURRENT_OCCURRENCE,
        REPLACEMENT_OCCURRENCE,
        ADDITION_OCCURRENCE
    }

    public enum TargetAtomType { EFFECTIVE_OCCURRENCE_REFERENCE }

    public enum RelationCardinality {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        ZERO_TO_ONE,
        ZERO_TO_MANY,
        ONE_TO_ZERO
    }

    public enum EvidenceDimension { DATE, SALON, INSTRUCTOR, ACTIVITY, RESERVED_SUBINTERVAL }

    public enum EvidenceKind {
        EXACT_IDENTITY,
        APPROVED_EQUIVALENCE_RULE,
        CONTAINMENT,
        FIELD_MATCH
    }

    public enum EligibilityStatus { ELIGIBLE, REJECTED }

    public enum RejectionReason {
        DATE_MISMATCH,
        SALON_MISMATCH,
        INSTRUCTOR_MISMATCH,
        ACTIVITY_MISMATCH,
        RESERVED_SUBINTERVAL_NOT_CONTAINED,
        F2D_AUTHORITY_CONFLICT
    }

    public enum MappingStatus { NO_CANDIDATES, UNIQUE_CANDIDATE, MULTIPLE_CANDIDATES }

    public enum ResultStatus {
        CANDIDATE_EVALUATION_COMPLETE,
        EXPECTED_ABSENCE,
        MISSING,
        UNSUPPORTED,
        DIVERGENT_INCOMPATIBLE
    }

    public enum EffectiveResultStatus {
        PRESENT,
        EXPECTED_ABSENCE,
        NOT_APPLICABLE,
        MISSING,
        DIVERGENT
    }

    public enum HistoryStatus { CURRENT_SNAPSHOT_ONLY, UNKNOWN_HISTORY, NOT_APPLICABLE }

    public enum AmbiguityStatus { AMBIGUOUS, NOT_AMBIGUOUS, NOT_APPLICABLE }

    public enum AmbiguityReason {
        MULTIPLE_ELIGIBLE_CANDIDATES,
        MULTIPLE_NOMINAL_TARGETS,
        MULTIPLE_ELIGIBLE_AND_NOMINAL_TARGETS,
        UNKNOWN_INTENT_MULTIPLE_PLAUSIBLE_INTERPRETATIONS,
        LEGACY_HISTORY_HAS_MULTIPLE_PLAUSIBLE_STATES
    }

    public enum SelectionStatus { NOT_SELECTED_BY_DETECTOR }

    public enum UnsupportedReason {
        UNKNOWN_INTENT,
        LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED
    }

    public enum ExpectedAbsenceReason { TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION }

    public enum BlockingStatus { BLOCKING, NON_BLOCKING }

    public enum BlockedCapability {
        MAPPING_SELECTION,
        CROSSWALK_PERSISTENCE,
        MATERIAL_RESOLVER,
        MIGRATION,
        CUTOVER
    }

    public enum F2DCompatibilityStatus { F2D_CONTRACT_COMPATIBLE, F2D_AUTHORITY_CONFLICT }

    public enum DetectionScenario {
        STANDARD_EVALUATION,
        REQUIRED_TARGET,
        LEGACY_EXCEPTION_UNKNOWN_INTENT,
        LEGACY_CANCELLATION_UNKNOWN_INTENT,
        LEGACY_HISTORY_REQUIRED,
        NEW_CANCELLATION,
        NEW_REPLACEMENT,
        NEW_ADDITION,
        RESERVATION_HISTORICAL_TARGET,
        INCOMPATIBLE_EVIDENCE
    }

    public enum InputErrorCode {
        REQUIRED_VALUE_MISSING,
        INVALID_INTERVAL,
        INVALID_SOURCE_SHAPE,
        INVALID_SCENARIO,
        INCOHERENT_COUNTS,
        DUPLICATE_CANDIDATE_IDENTITY
    }
}
