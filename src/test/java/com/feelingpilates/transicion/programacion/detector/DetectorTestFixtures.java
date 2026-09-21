package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RelationCardinality.ZERO_TO_MANY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ReservationState.CONFIRMADA;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.LEGACY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.TargetAtomType.EFFECTIVE_OCCURRENCE_REFERENCE;

final class DetectorTestFixtures {

    static final LocalDate DATE = LocalDate.of(2026, 9, 7);
    static final UUID RESERVATION_ID = uuid(1);
    static final UUID SALON_ID = uuid(2);
    static final UUID INSTRUCTOR_ID = uuid(3);
    static final UUID ACTIVITY_ID = uuid(4);

    private DetectorTestFixtures() {
    }

    static UUID uuid(long suffix) {
        return new UUID(0L, suffix);
    }

    static EvidenceProvenance provenance(String recordId) {
        return new EvidenceProvenance(
                "fixture",
                "schema-v47",
                List.of(recordId),
                "reservation-candidate",
                "1",
                DATE.toString(),
                Map.of("record", recordId));
    }

    static ReservationSourceSnapshot reservation() {
        return reservation(RESERVATION_ID, LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    static ReservationSourceSnapshot reservation(UUID id, LocalTime start, LocalTime end) {
        return new ReservationSourceSnapshot(
                id,
                CONFIRMADA,
                DATE,
                SALON_ID,
                INSTRUCTOR_ID,
                ACTIVITY_ID,
                new ReservedSubinterval(start, end),
                "reservation-snapshot-" + id,
                "reservation-fingerprint-" + id,
                Map.of("source", "fixture"),
                provenance(id.toString()));
    }

    static ProgrammingCandidateSnapshot candidate(long suffix) {
        return candidate(suffix, RECURRENT_OCCURRENCE, SALON_ID, INSTRUCTOR_ID, ACTIVITY_ID,
                LocalTime.of(8, 0), LocalTime.of(12, 0));
    }

    static ProgrammingCandidateSnapshot candidate(
            long suffix,
            DetectorVocabulary.CandidateType type,
            UUID salonId,
            UUID instructorId,
            UUID activityId,
            LocalTime start,
            LocalTime end) {
        ReferenciaOcurrencia.Tipo referenceType = type == DetectorVocabulary.CandidateType.ADDITION_OCCURRENCE
                ? ReferenciaOcurrencia.Tipo.AJUSTE
                : ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION;
        return candidateWithReferenceType(
                suffix, type, referenceType, salonId, instructorId, activityId, start, end);
    }

    static ProgrammingCandidateSnapshot candidateWithReferenceType(
            long suffix,
            DetectorVocabulary.CandidateType type,
            ReferenciaOcurrencia.Tipo referenceType,
            UUID salonId,
            UUID instructorId,
            UUID activityId,
            LocalTime start,
            LocalTime end) {
        return new ProgrammingCandidateSnapshot(
                new ReferenciaOcurrencia(referenceType, uuid(100 + suffix), DATE),
                type,
                "candidate-snapshot-" + suffix,
                "candidate-fingerprint-" + suffix,
                salonId,
                instructorId,
                activityId,
                start,
                end,
                Map.of("candidate", Long.toString(suffix)),
                provenance("candidate-" + suffix));
    }

    static GenericSourceSnapshot genericSource(DetectorVocabulary.SourceAtomType type) {
        return new GenericSourceSnapshot(
                type.name().startsWith("NEW_")
                        ? DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH
                        : LEGACY,
                type,
                "source-" + type,
                "source-snapshot-" + type,
                "source-fingerprint-" + type,
                Map.of("type", type.name()),
                provenance("source-" + type));
    }

    static CandidateGenerationResult generation(int eligibleCount) {
        List<ProgrammingCandidateSnapshot> universe = java.util.stream.IntStream.range(0, eligibleCount)
                .mapToObj(index -> candidate(index + 1L))
                .toList();
        return new CandidateEvidenceGenerator().generate(reservation(), universe);
    }

    static DetectorEvaluationRequest request(
            SourceSnapshot source,
            CandidateGenerationResult generation,
            ClassificationContext context) {
        return new DetectorEvaluationRequest(
                "run-1",
                "evaluation-1",
                "detector-1",
                "rule-1",
                "1",
                Instant.parse("2026-09-01T12:00:00Z"),
                source,
                EFFECTIVE_OCCURRENCE_REFERENCE,
                ZERO_TO_MANY,
                generation,
                context,
                provenance("evaluation-1"),
                "evidence-hash");
    }
}
