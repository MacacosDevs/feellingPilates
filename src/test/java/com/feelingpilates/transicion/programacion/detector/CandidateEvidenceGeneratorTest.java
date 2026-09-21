package com.feelingpilates.transicion.programacion.detector;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.ACTIVITY_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.INSTRUCTOR_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.SALON_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.candidate;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.reservation;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.uuid;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.ELIGIBLE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EligibilityStatus.REJECTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EvidenceDimension.RESERVED_SUBINTERVAL;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.RejectionReason.ACTIVITY_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;

class CandidateEvidenceGeneratorTest {

    private final CandidateEvidenceGenerator generator = new CandidateEvidenceGenerator();

    @Test
    void d04SeparaReservaTargetYSubintervaloSinColisionDeIdentidad() {
        ProgrammingCandidateSnapshot target = candidate(1);
        ReservationSourceSnapshot first = reservation(uuid(10), LocalTime.of(9, 0), LocalTime.of(10, 0));
        ReservationSourceSnapshot second = reservation(uuid(11), LocalTime.of(9, 0), LocalTime.of(10, 0));

        DetectorCandidate firstCandidate = generator.generate(first, List.of(target)).candidates().getFirst();
        DetectorCandidate secondCandidate = generator.generate(second, List.of(target)).candidates().getFirst();

        assertThat(first.sourceIdentity()).isNotEqualTo(second.sourceIdentity());
        assertThat(first.reservedSubinterval()).isEqualTo(second.reservedSubinterval());
        assertThat(firstCandidate.candidateTargetReference())
                .isEqualTo(secondCandidate.candidateTargetReference())
                .isNotEqualTo(first.reservationId());
        assertThat(firstCandidate.candidateIdentity()).isEqualTo(target.reference());
        assertThat(firstCandidate.matchingDimensions()).contains(RESERVED_SUBINTERVAL);
    }

    @Test
    void conservaTodosLosCandidatesIncluidoElRechazadoYDerivaAmbosConteos() {
        ProgrammingCandidateSnapshot a = candidate(1);
        ProgrammingCandidateSnapshot b = candidate(
                2,
                DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE,
                SALON_ID,
                INSTRUCTOR_ID,
                uuid(999),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));
        ProgrammingCandidateSnapshot c = candidate(3);

        CandidateGenerationResult result = generator.generate(reservation(), List.of(a, b, c));

        assertThat(result.candidates()).extracting(DetectorCandidate::candidateIdentity)
                .containsExactly(a.reference(), b.reference(), c.reference());
        assertThat(result.generatedCandidateCount()).isEqualTo(3);
        assertThat(result.candidateCount()).isEqualTo(2);
        DetectorCandidate rejected = result.candidates().get(1);
        assertThat(rejected.eligibilityStatus()).isEqualTo(REJECTED);
        assertThat(rejected.rejectionReasons()).containsExactly(ACTIVITY_MISMATCH);
        assertThat(rejected.candidateEvidence()).isNotEmpty();
        assertThat(rejected.provenance()).isEqualTo(b.provenance());
        assertThat(result.candidates()).filteredOn(candidate -> candidate.eligibilityStatus() == ELIGIBLE)
                .hasSize(2);
    }

    @Test
    void todosRechazadosMantieneCandidatesAunqueElConteoElegibleSeaCero() {
        ProgrammingCandidateSnapshot first = candidate(
                1,
                DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE,
                uuid(90),
                INSTRUCTOR_ID,
                ACTIVITY_ID,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));
        ProgrammingCandidateSnapshot second = candidate(
                2,
                DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE,
                SALON_ID,
                INSTRUCTOR_ID,
                ACTIVITY_ID,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0));

        CandidateGenerationResult result = generator.generate(reservation(), List.of(first, second));

        assertThat(result.generatedCandidateCount()).isEqualTo(2);
        assertThat(result.candidateCount()).isZero();
        assertThat(result.candidates()).isNotEmpty().allMatch(
                candidate -> candidate.eligibilityStatus() == REJECTED);
    }

    @Test
    void noMutaLaColeccionDeEntradaYOrdenaSoloParaSalidaDeterminista() {
        ProgrammingCandidateSnapshot first = candidate(1);
        ProgrammingCandidateSnapshot second = candidate(2);
        List<ProgrammingCandidateSnapshot> mutable = new ArrayList<>(List.of(second, first));
        List<ProgrammingCandidateSnapshot> before = List.copyOf(mutable);

        CandidateGenerationResult result = generator.generate(reservation(), mutable);
        mutable.clear();

        assertThat(before).containsExactly(second, first);
        assertThat(result.candidates()).extracting(DetectorCandidate::candidateIdentity)
                .containsExactly(first.reference(), second.reference());
        assertThat(generator.generate(reservation(), List.of(first, second))).isEqualTo(result);
    }
}
