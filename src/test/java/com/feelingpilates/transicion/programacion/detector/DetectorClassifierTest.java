package com.feelingpilates.transicion.programacion.detector;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.generation;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.genericSource;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.request;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.reservation;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.NOT_AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.MULTIPLE_NOMINAL_TARGETS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.EXPECTED_ABSENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.PRESENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ExpectedAbsenceReason.TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.HistoryStatus.CURRENT_SNAPSHOT_ONLY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.HistoryStatus.UNKNOWN_HISTORY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.MULTIPLE_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.NO_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.UNIQUE_CANDIDATE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.CANDIDATE_EVALUATION_COMPLETE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.DIVERGENT_INCOMPATIBLE;
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
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.UNKNOWN_INTENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetectorClassifierTest {

    private final DetectorClassifier classifier = new DetectorClassifier();

    @Test
    void ceroElegiblesSeClasificaPorContextoYNoComoMissingUniversal() {
        DetectorResult standard = classify(reservation(), 0, ClassificationContext.standard());
        DetectorResult expectedAbsence = classify(
                genericSource(NEW_CANCELACION), 0, ClassificationContext.newCancellation(1, 0));
        DetectorResult missing = classify(reservation(), 0, ClassificationContext.requiredTarget());
        DetectorResult unsupported = classify(
                genericSource(LEGACY_RECURRENTE), 0, ClassificationContext.legacyHistoryRequired());
        DetectorResult divergent = classify(
                reservation(), 0, ClassificationContext.incompatibleEvidence());

        assertThat(standard.resultStatus()).isEqualTo(CANDIDATE_EVALUATION_COMPLETE);
        assertThat(expectedAbsence.resultStatus()).isEqualTo(DetectorVocabulary.ResultStatus.EXPECTED_ABSENCE);
        assertThat(expectedAbsence.effectiveResultStatus()).isEqualTo(EXPECTED_ABSENCE);
        assertThat(expectedAbsence.expectedAbsenceReason())
                .contains(TARGET_NOMINAL_SUPPRESSED_BY_VALID_CANCELLATION);
        assertThat(missing.resultStatus()).isEqualTo(MISSING);
        assertThat(unsupported.resultStatus()).isEqualTo(UNSUPPORTED);
        assertThat(divergent.resultStatus()).isEqualTo(DIVERGENT_INCOMPATIBLE);
        assertThat(List.of(standard, expectedAbsence, missing, unsupported, divergent))
                .allMatch(result -> result.mappingStatus() == NO_CANDIDATES)
                .allMatch(result -> result.selectionStatus() == NOT_SELECTED_BY_DETECTOR);
    }

    @Test
    void cardinalidadesCeroUnoYMuchosNuncaSeleccionan() {
        DetectorResult zero = classify(reservation(), 0, ClassificationContext.standard());
        DetectorResult one = classify(reservation(), 1, ClassificationContext.standard());
        DetectorResult many = classify(reservation(), 2, ClassificationContext.standard());

        assertThat(zero.mappingStatus()).isEqualTo(NO_CANDIDATES);
        assertThat(one.mappingStatus()).isEqualTo(UNIQUE_CANDIDATE);
        assertThat(one.candidateCount()).isEqualTo(1);
        assertThat(one.candidates()).hasSize(1);
        assertThat(many.mappingStatus()).isEqualTo(MULTIPLE_CANDIDATES);
        assertThat(many.ambiguityStatus()).isEqualTo(AMBIGUOUS);
        assertThat(many.ambiguityReason()).isPresent();
        assertThat(many.blocking()).isTrue();
        assertThat(List.of(zero, one, many))
                .allMatch(result -> result.selectionStatus() == NOT_SELECTED_BY_DETECTOR);
    }

    @Test
    void resultadoFinalRetieneElegiblesYRechazadosConConteosDiferentes() {
        ProgrammingCandidateSnapshot eligibleA = DetectorTestFixtures.candidate(1);
        ProgrammingCandidateSnapshot rejectedB = DetectorTestFixtures.candidate(
                2,
                DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE,
                DetectorTestFixtures.SALON_ID,
                DetectorTestFixtures.INSTRUCTOR_ID,
                DetectorTestFixtures.uuid(999),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));
        ProgrammingCandidateSnapshot eligibleC = DetectorTestFixtures.candidate(3);
        CandidateGenerationResult generated = new CandidateEvidenceGenerator().generate(
                reservation(), List.of(eligibleA, rejectedB, eligibleC));

        DetectorResult result = classifier.classify(
                request(reservation(), generated, ClassificationContext.standard()));

        assertThat(result.candidates()).extracting(DetectorCandidate::candidateIdentity)
                .containsExactly(eligibleA.reference(), rejectedB.reference(), eligibleC.reference());
        assertThat(result.generatedCandidateCount()).isEqualTo(3);
        assertThat(result.candidateCount()).isEqualTo(2);
        assertThat(result.candidates().get(1).rejectionReasons())
                .contains(DetectorVocabulary.RejectionReason.ACTIVITY_MISMATCH);
    }

    @Test
    void resultadoFinalRetieneTodosLosRechazadosCuandoNoHayElegibles() {
        ProgrammingCandidateSnapshot rejectedA = DetectorTestFixtures.candidate(
                1,
                DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE,
                DetectorTestFixtures.uuid(90),
                DetectorTestFixtures.INSTRUCTOR_ID,
                DetectorTestFixtures.ACTIVITY_ID,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));
        ProgrammingCandidateSnapshot rejectedB = DetectorTestFixtures.candidate(
                2,
                DetectorVocabulary.CandidateType.RECURRENT_OCCURRENCE,
                DetectorTestFixtures.SALON_ID,
                DetectorTestFixtures.INSTRUCTOR_ID,
                DetectorTestFixtures.ACTIVITY_ID,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0));
        CandidateGenerationResult generated = new CandidateEvidenceGenerator().generate(
                reservation(), List.of(rejectedA, rejectedB));

        DetectorResult result = classifier.classify(
                request(reservation(), generated, ClassificationContext.requiredTarget()));

        assertThat(result.generatedCandidateCount()).isEqualTo(2);
        assertThat(result.candidateCount()).isZero();
        assertThat(result.candidates()).hasSize(2);
        assertThat(result.resultStatus()).isEqualTo(MISSING);
    }

    @Test
    void legacyExcepcionConIntencionDesconocidaPermaneceUnsupportedEnCeroUnoYMuchos() {
        assertLegacyUnknownIntent(LEGACY_EXCEPCION, ClassificationContext.legacyExceptionUnknownIntent());
    }

    @Test
    void legacyCancelacionConIntencionDesconocidaPermaneceUnsupportedEnCeroUnoYMuchos() {
        assertLegacyUnknownIntent(LEGACY_CANCELACION, ClassificationContext.legacyCancellationUnknownIntent());
    }

    @Test
    void d09DistingueSnapshotActualDeHistoriaNoPersistidaSinTimestampsInferidos() {
        DetectorResult current = classify(reservation(), 1, ClassificationContext.standard());
        DetectorResult unknown = classify(
                genericSource(LEGACY_RECURRENTE), 1, ClassificationContext.legacyHistoryRequired());

        assertThat(current.historyStatus()).isEqualTo(CURRENT_SNAPSHOT_ONLY);
        assertThat(current.resultStatus()).isEqualTo(CANDIDATE_EVALUATION_COMPLETE);
        assertThat(unknown.historyStatus()).isEqualTo(UNKNOWN_HISTORY);
        assertThat(unknown.resultStatus()).isEqualTo(UNSUPPORTED);
        assertThat(unknown.unsupportedReason()).contains(LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED);
        assertThat(unknown.source().observableFields()).doesNotContainKeys("vigenteDesde", "vigenteHasta");
    }

    @Test
    void formasNuevasPreservanCancelacionReemplazoYAdicionSinInferenciaLegacy() {
        DetectorResult cancellation = classify(
                genericSource(NEW_CANCELACION), 0, ClassificationContext.newCancellation(1, 0));
        DetectorResult cancellationWithoutTarget = classify(
                genericSource(NEW_CANCELACION), 0, ClassificationContext.newCancellation(0, 0));
        DetectorResult cancellationWithOutcome = classify(
                genericSource(NEW_CANCELACION), 1, ClassificationContext.newCancellation(1, 1));
        DetectorResult replacement = classify(
                genericSource(NEW_REEMPLAZO), 1, ClassificationContext.newReplacement(1, 1));
        DetectorResult addition = classify(
                genericSource(NEW_ADICION), 1, ClassificationContext.newAddition(1));

        assertThat(cancellation.resultStatus()).isEqualTo(DetectorVocabulary.ResultStatus.EXPECTED_ABSENCE);
        assertThat(cancellationWithoutTarget.resultStatus()).isEqualTo(MISSING);
        assertThat(cancellationWithOutcome.resultStatus()).isEqualTo(DIVERGENT_INCOMPATIBLE);
        assertThat(replacement.resultStatus()).isEqualTo(CANDIDATE_EVALUATION_COMPLETE);
        assertThat(replacement.effectiveResultStatus()).isEqualTo(PRESENT);
        assertThat(addition.resultStatus()).isEqualTo(CANDIDATE_EVALUATION_COMPLETE);
        assertThat(addition.effectiveResultStatus()).isEqualTo(PRESENT);
    }

    @Test
    void unsupportedSemanticoEsDistintoDeInputInvalid() {
        DetectorResult semantic = classify(
                genericSource(LEGACY_EXCEPCION), 1, ClassificationContext.legacyExceptionUnknownIntent());

        assertThat(semantic.resultStatus()).isEqualTo(UNSUPPORTED);
        assertThatThrownBy(() -> new ReservedSubinterval(LocalTime.NOON, LocalTime.NOON))
                .isInstanceOf(DetectorInputInvalidException.class)
                .extracting("code")
                .isEqualTo(DetectorVocabulary.InputErrorCode.INVALID_INTERVAL);
    }

    @Test
    void mismoInputProduceExactamenteElMismoResultadoSemantico() {
        DetectorEvaluationRequest input = request(reservation(), generation(2), ClassificationContext.standard());

        DetectorResult first = classifier.classify(input);
        DetectorResult second = classifier.classify(input);

        assertThat(second).isEqualTo(first);
        assertThat(second.resultHash()).isEqualTo(first.resultHash());
    }

    @Test
    void legacyEspecializadoNoPuedeBypassearUnknownIntentConScenarioGenerico() {
        assertThatThrownBy(() -> classify(
                genericSource(LEGACY_EXCEPCION), 0, ClassificationContext.standard()))
                .isInstanceOf(DetectorInputInvalidException.class)
                .extracting("code")
                .isEqualTo(DetectorVocabulary.InputErrorCode.INVALID_SCENARIO);
        assertThatThrownBy(() -> classify(
                genericSource(LEGACY_CANCELACION), 0, ClassificationContext.requiredTarget()))
                .isInstanceOf(DetectorInputInvalidException.class)
                .extracting("code")
                .isEqualTo(DetectorVocabulary.InputErrorCode.INVALID_SCENARIO);
    }

    @Test
    void sourceSystemYSourceAtomTypeIncompatiblesFallanCerrado() {
        assertThatThrownBy(() -> new GenericSourceSnapshot(
                NEW_DARK_LAUNCH,
                LEGACY_EXCEPCION,
                "legacy-exception",
                "snapshot",
                "fingerprint",
                Map.of(),
                DetectorTestFixtures.provenance("legacy-exception")))
                .isInstanceOf(DetectorInputInvalidException.class)
                .extracting("code")
                .isEqualTo(DetectorVocabulary.InputErrorCode.INVALID_SOURCE_SHAPE);
        assertThatThrownBy(() -> new GenericSourceSnapshot(
                LEGACY,
                NEW_CANCELACION,
                "new-cancellation",
                "snapshot",
                "fingerprint",
                Map.of(),
                DetectorTestFixtures.provenance("new-cancellation")))
                .isInstanceOf(DetectorInputInvalidException.class)
                .extracting("code")
                .isEqualTo(DetectorVocabulary.InputErrorCode.INVALID_SOURCE_SHAPE);
    }

    @Test
    void multiplicidadNominalDeCancelacionYReemplazoEsAmbiguaAunqueHayaCeroOUnoElegibles() {
        DetectorResult cancellation = classify(
                genericSource(NEW_CANCELACION), 0, ClassificationContext.newCancellation(2, 0));
        DetectorResult replacement = classify(
                genericSource(NEW_REEMPLAZO), 1, ClassificationContext.newReplacement(2, 1));

        assertThat(List.of(cancellation, replacement))
                .allMatch(result -> result.resultStatus() == DIVERGENT_INCOMPATIBLE)
                .allMatch(result -> result.ambiguityStatus() == AMBIGUOUS)
                .allMatch(result -> result.ambiguityReason().equals(Optional.of(MULTIPLE_NOMINAL_TARGETS)))
                .allMatch(DetectorResult::blocking)
                .allMatch(result -> result.selectionStatus() == NOT_SELECTED_BY_DETECTOR);
        assertThat(cancellation.mappingStatus()).isEqualTo(NO_CANDIDATES);
        assertThat(replacement.mappingStatus()).isEqualTo(UNIQUE_CANDIDATE);
    }

    @Test
    void d04TargetHistoricoSobreviveALaSupresionActualSinCandidateEfectivo() {
        ProgrammingCandidateSnapshot historical = DetectorTestFixtures.candidate(44);
        ReservationSourceSnapshot base = reservation();
        ReservationSourceSnapshot withHistoricalTarget = new ReservationSourceSnapshot(
                base.reservationId(),
                base.state(),
                base.date(),
                base.salonId(),
                base.instructorId(),
                base.activityId(),
                base.reservedSubinterval(),
                base.snapshotIdentity(),
                base.sourceFingerprint(),
                base.additionalObservableFields(),
                Optional.of(new HistoricalProgrammingTargetSnapshot(
                        historical.reference(), EXPECTED_ABSENCE)),
                base.provenance());

        DetectorResult result = classify(
                withHistoricalTarget, 0, ClassificationContext.reservationHistoricalTarget());

        assertThat(result.resultStatus()).isEqualTo(DetectorVocabulary.ResultStatus.EXPECTED_ABSENCE);
        assertThat(result.effectiveResultStatus()).isEqualTo(EXPECTED_ABSENCE);
        assertThat(result.resultStatus()).isNotEqualTo(MISSING);
        assertThat(result.candidateCount()).isZero();
        assertThat(((ReservationSourceSnapshot) result.source()).historicalProgrammingTarget())
                .get()
                .extracting(HistoricalProgrammingTargetSnapshot::targetReference)
                .isEqualTo(historical.reference());
        assertThat(((ReservationSourceSnapshot) result.source()).reservedSubinterval())
                .isEqualTo(new ReservedSubinterval(LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    private void assertLegacyUnknownIntent(
            DetectorVocabulary.SourceAtomType type,
            ClassificationContext context) {
        for (int count : List.of(0, 1, 2)) {
            DetectorResult result = classify(genericSource(type), count, context);
            assertThat(result.resultStatus()).isEqualTo(UNSUPPORTED);
            assertThat(result.unsupportedReason()).contains(UNKNOWN_INTENT);
            assertThat(result.selectionStatus()).isEqualTo(NOT_SELECTED_BY_DETECTOR);
            assertThat(result.blocking()).isTrue();
            if (count > 1) {
                assertThat(result.mappingStatus()).isEqualTo(MULTIPLE_CANDIDATES);
                assertThat(result.ambiguityStatus()).isEqualTo(AMBIGUOUS);
                assertThat(result.ambiguityReason()).isPresent();
            } else {
                assertThat(result.ambiguityStatus()).isEqualTo(NOT_AMBIGUOUS);
            }
        }
    }

    private DetectorResult classify(
            SourceSnapshot source,
            int eligibleCount,
            ClassificationContext context) {
        return classifier.classify(request(source, generation(eligibleCount), context));
    }
}
