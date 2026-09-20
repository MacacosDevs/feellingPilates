package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.ACTIVITY_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.INSTRUCTOR_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.SALON_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.candidate;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.candidateWithReferenceType;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.request;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.reservation;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.CandidateType.ADDITION_OCCURRENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_AUTHORITY_CONFLICT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_CONTRACT_COMPATIBLE;
import static org.assertj.core.api.Assertions.assertThat;

class F2DAuthorityGuardTest {

    private final F2DAuthorityGuard guard = new F2DAuthorityGuard();

    @Test
    void aceptaLasIdentidadesAprobadasDeSerieYAdicion() {
        ProgrammingCandidateSnapshot recurrent = candidate(1);
        ProgrammingCandidateSnapshot addition = candidate(
                2,
                ADDITION_OCCURRENCE,
                SALON_ID,
                INSTRUCTOR_ID,
                ACTIVITY_ID,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));

        assertThat(guard.evaluate(recurrent).status()).isEqualTo(F2D_CONTRACT_COMPATIBLE);
        assertThat(guard.evaluate(addition).status()).isEqualTo(F2D_CONTRACT_COMPATIBLE);
    }

    @Test
    void conflictoF2dBloqueaYNoReinterpretaLaReferencia() {
        ProgrammingCandidateSnapshot conflict = candidateWithReferenceType(
                1,
                ADDITION_OCCURRENCE,
                ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,
                SALON_ID,
                INSTRUCTOR_ID,
                ACTIVITY_ID,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));

        CandidateGenerationResult generation = new CandidateEvidenceGenerator()
                .generate(reservation(), List.of(conflict));
        DetectorResult classified = new DetectorClassifier().classify(
                request(reservation(), generation, ClassificationContext.standard()));

        assertThat(generation.f2dCompatibility().status()).isEqualTo(F2D_AUTHORITY_CONFLICT);
        assertThat(generation.f2dCompatibility().blocking()).isTrue();
        assertThat(generation.candidates().getFirst().candidateIdentity()).isEqualTo(conflict.reference());
        assertThat(generation.candidates().getFirst().rejectionReasons()).contains(
                DetectorVocabulary.RejectionReason.F2D_AUTHORITY_CONFLICT);
        assertThat(classified.f2dCompatibility().status()).isEqualTo(F2D_AUTHORITY_CONFLICT);
        assertThat(classified.blocking()).isTrue();
        assertThat(classified.resultStatus())
                .isEqualTo(DetectorVocabulary.ResultStatus.CANDIDATE_EVALUATION_COMPLETE);
    }
}
