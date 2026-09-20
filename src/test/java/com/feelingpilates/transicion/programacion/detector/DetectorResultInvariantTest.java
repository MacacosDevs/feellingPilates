package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.ACTIVITY_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.INSTRUCTOR_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.SALON_ID;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.candidateWithReferenceType;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.generation;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.genericSource;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.request;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.reservation;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.NOT_AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.MULTIPLE_ELIGIBLE_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityReason.MULTIPLE_NOMINAL_TARGETS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.AmbiguityStatus.AMBIGUOUS;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockedCapability.CROSSWALK_PERSISTENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockedCapability.CUTOVER;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockedCapability.MAPPING_SELECTION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockedCapability.MATERIAL_RESOLVER;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockedCapability.MIGRATION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockingStatus.BLOCKING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.BlockingStatus.NON_BLOCKING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.CandidateType.ADDITION_OCCURRENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.EXPECTED_ABSENCE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.PRESENT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.NO_CANDIDATES;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.MappingStatus.UNIQUE_CANDIDATE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.ResultStatus.CANDIDATE_EVALUATION_COMPLETE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_RECURRENTE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_EXCEPCION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.UnsupportedReason.UNKNOWN_INTENT;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetectorResultInvariantTest {

    private static final List<DetectorVocabulary.BlockedCapability> ALL_BLOCKED = List.of(
            MAPPING_SELECTION,
            CROSSWALK_PERSISTENCE,
            MATERIAL_RESOLVER,
            MIGRATION,
            CUTOVER);

    private final DetectorClassifier classifier = new DetectorClassifier();

    @Test
    void dosElegiblesNoAdmitenMappingUnicoNiAusenciaDeAmbiguedad() {
        DetectorResult valid = classifier.classify(
                request(reservation(), generation(2), ClassificationContext.standard()));

        assertInvalid(() -> copy(valid, null, null, UNIQUE_CANDIDATE, null, null, null,
                null, null, null, null, null, null, null, null));
        assertInvalid(() -> copy(valid, null, null, null, null, null, null,
                NOT_AMBIGUOUS, Optional.empty(), null, null, null, null, null, null));
    }

    @Test
    void multipleCandidatesYAuthorityConflictNoAdmitenBlockingFalse() {
        DetectorResult multiple = classifier.classify(
                request(reservation(), generation(2), ClassificationContext.standard()));
        assertInvalid(() -> copy(multiple, null, null, null, null, null, null,
                null, null, false, NON_BLOCKING, List.of(), null, null, null));

        ProgrammingCandidateSnapshot conflict = candidateWithReferenceType(
                1,
                ADDITION_OCCURRENCE,
                ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,
                SALON_ID,
                INSTRUCTOR_ID,
                ACTIVITY_ID,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));
        CandidateGenerationResult generated = new CandidateEvidenceGenerator()
                .generate(reservation(), List.of(conflict));
        DetectorResult authorityConflict = classifier.classify(
                request(reservation(), generated, ClassificationContext.standard()));

        assertInvalid(() -> copy(authorityConflict, null, null, null, null, null, null,
                null, null, false, NON_BLOCKING, List.of(), null, null, null));
    }

    @Test
    void resultStatusYEffectiveStatusIncompatiblesSeRechazan() {
        DetectorResult valid = classifier.classify(
                request(reservation(), generation(1), ClassificationContext.standard()));

        assertInvalid(() -> copy(valid, null, null, null, null, EXPECTED_ABSENCE, null,
                null, null, null, null, null, null, null, null));
    }

    @Test
    void conteosGeneradosYElegiblesDebenCoincidirConCandidatesRetenidos() {
        DetectorResult valid = classifier.classify(
                request(reservation(), generation(1), ClassificationContext.standard()));

        assertInvalid(() -> copy(valid, valid.generatedCandidateCount() + 1, null, null,
                null, null, null, null, null, null, null, null, null, null, null));
        assertInvalid(() -> copy(valid, null, 0, NO_CANDIDATES,
                null, null, null, null, null, null, null, null, null, null, null));
    }

    @Test
    void constructorPublicoNoPermiteBypassSemanticoLegacy() {
        DetectorResult legacy = classifier.classify(request(
                genericSource(LEGACY_EXCEPCION),
                generation(1),
                ClassificationContext.legacyExceptionUnknownIntent()));

        assertInvalid(() -> copy(legacy, null, null, null,
                CANDIDATE_EVALUATION_COMPLETE, PRESENT, 1,
                null, null, null, null, null, null, Optional.empty(), null));
    }

    @Test
    void multipleNominalTargetsReasonExigeMultiplicidadNominalReal() {
        DetectorResult oneNominal = classifier.classify(request(
                genericSource(DetectorVocabulary.SourceAtomType.NEW_CANCELACION),
                generation(0),
                ClassificationContext.newCancellation(1, 0)));
        DetectorResult zeroNominal = classifier.classify(request(
                genericSource(DetectorVocabulary.SourceAtomType.NEW_CANCELACION),
                generation(0),
                ClassificationContext.newCancellation(0, 0)));

        assertInvalid(() -> copy(oneNominal, null, null, null, null, null, null,
                AMBIGUOUS, Optional.of(MULTIPLE_NOMINAL_TARGETS), true, BLOCKING,
                ALL_BLOCKED, null, null, null));
        assertInvalid(() -> copy(zeroNominal, null, null, null, null, null, null,
                AMBIGUOUS, Optional.of(MULTIPLE_NOMINAL_TARGETS), null, null,
                null, null, null, null));
    }

    @Test
    void multipleEligibleCandidatesReasonExigeMultiplicidadElegibleReal() {
        DetectorResult oneEligible = classifier.classify(
                request(reservation(), generation(1), ClassificationContext.standard()));
        DetectorResult zeroEligible = classifier.classify(
                request(reservation(), generation(0), ClassificationContext.standard()));

        assertInvalid(() -> copy(oneEligible, null, null, null, null, null, null,
                AMBIGUOUS, Optional.of(MULTIPLE_ELIGIBLE_CANDIDATES), true, BLOCKING,
                ALL_BLOCKED, null, null, null));
        assertInvalid(() -> copy(zeroEligible, null, null, null, null, null, null,
                AMBIGUOUS, Optional.of(MULTIPLE_ELIGIBLE_CANDIDATES), true, BLOCKING,
                ALL_BLOCKED, null, null, null));
    }

    @Test
    void notAmbiguousNoAdmiteReasonYAmbiguousExigeCausaVerdadera() {
        DetectorResult multiple = classifier.classify(
                request(reservation(), generation(2), ClassificationContext.standard()));
        DetectorResult unique = classifier.classify(
                request(reservation(), generation(1), ClassificationContext.standard()));

        assertInvalid(() -> copy(multiple, null, null, null, null, null, null,
                NOT_AMBIGUOUS, multiple.ambiguityReason(), null, null, null, null, null, null));
        assertInvalid(() -> copy(unique, null, null, null, null, null, null,
                AMBIGUOUS, Optional.of(MULTIPLE_ELIGIBLE_CANDIDATES), true, BLOCKING,
                ALL_BLOCKED, null, null, null));
    }

    @Test
    void unknownHistoryNoAdmiteResultadoSoportadoNiReasonIncorrecta() {
        DetectorResult supported = classifier.classify(request(
                genericSource(LEGACY_RECURRENTE),
                generation(1),
                ClassificationContext.standard()));
        DetectorResult unknownHistory = classifier.classify(request(
                genericSource(LEGACY_RECURRENTE),
                generation(0),
                ClassificationContext.legacyHistoryRequired()));

        assertInvalid(() -> copy(supported, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, DetectorVocabulary.HistoryStatus.UNKNOWN_HISTORY));
        assertInvalid(() -> copy(unknownHistory, null, null, null, null, null, null,
                null, null, null, null, null, null, Optional.of(UNKNOWN_INTENT), null));
    }

    @Test
    void unknownHistoryEsSiempreBlocking() {
        DetectorResult valid = classifier.classify(request(
                genericSource(LEGACY_RECURRENTE),
                generation(0),
                ClassificationContext.legacyHistoryRequired()));

        assertInvalid(() -> copy(valid, null, null, null, null, null, null,
                null, null, false, NON_BLOCKING, List.of(), null, null, null));
    }

    @Test
    void casosValidosD09SiguenSiendoConstruibles() {
        DetectorResult currentSnapshot = classifier.classify(request(
                genericSource(LEGACY_RECURRENTE),
                generation(1),
                ClassificationContext.standard()));
        DetectorResult unknownHistory = classifier.classify(request(
                genericSource(LEGACY_RECURRENTE),
                generation(0),
                ClassificationContext.legacyHistoryRequired()));

        assertThatCode(() -> copy(currentSnapshot, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null)).doesNotThrowAnyException();
        assertThatCode(() -> copy(unknownHistory, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null)).doesNotThrowAnyException();
    }

    private void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(DetectorInputInvalidException.class);
    }

    private DetectorResult copy(
            DetectorResult base,
            Integer generatedCandidateCount,
            Integer candidateCount,
            DetectorVocabulary.MappingStatus mappingStatus,
            DetectorVocabulary.ResultStatus resultStatus,
            DetectorVocabulary.EffectiveResultStatus effectiveResultStatus,
            Integer effectiveOccurrenceCount,
            DetectorVocabulary.AmbiguityStatus ambiguityStatus,
            Optional<DetectorVocabulary.AmbiguityReason> ambiguityReason,
            Boolean blocking,
            DetectorVocabulary.BlockingStatus blockingStatus,
            List<DetectorVocabulary.BlockedCapability> blockedCapabilities,
            F2DCompatibilityResult f2dCompatibility,
            Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
            Optional<DetectorVocabulary.ExpectedAbsenceReason> expectedAbsenceReason) {
        return copy(base, generatedCandidateCount, candidateCount, mappingStatus, resultStatus,
                effectiveResultStatus, effectiveOccurrenceCount, ambiguityStatus, ambiguityReason,
                blocking, blockingStatus, blockedCapabilities, f2dCompatibility, unsupportedReason,
                expectedAbsenceReason, null, null);
    }

    private DetectorResult copy(
            DetectorResult base,
            Integer generatedCandidateCount,
            Integer candidateCount,
            DetectorVocabulary.MappingStatus mappingStatus,
            DetectorVocabulary.ResultStatus resultStatus,
            DetectorVocabulary.EffectiveResultStatus effectiveResultStatus,
            Integer effectiveOccurrenceCount,
            DetectorVocabulary.AmbiguityStatus ambiguityStatus,
            Optional<DetectorVocabulary.AmbiguityReason> ambiguityReason,
            Boolean blocking,
            DetectorVocabulary.BlockingStatus blockingStatus,
            List<DetectorVocabulary.BlockedCapability> blockedCapabilities,
            F2DCompatibilityResult f2dCompatibility,
            Optional<DetectorVocabulary.UnsupportedReason> unsupportedReason,
            Optional<DetectorVocabulary.ExpectedAbsenceReason> expectedAbsenceReason,
            OptionalInt nominalTargetCandidateCount,
            DetectorVocabulary.HistoryStatus historyStatus) {
        return new DetectorResult(
                base.detectorRunIdentity(),
                base.evaluationIdentity(),
                base.detectorVersion(),
                base.ruleId(),
                base.ruleVersion(),
                base.evaluatedAt(),
                base.source(),
                base.expectedTargetAtomType(),
                base.declaredRelationCardinality(),
                base.candidates(),
                generatedCandidateCount == null ? base.generatedCandidateCount() : generatedCandidateCount,
                candidateCount == null ? base.candidateCount() : candidateCount,
                mappingStatus == null ? base.mappingStatus() : mappingStatus,
                resultStatus == null ? base.resultStatus() : resultStatus,
                effectiveResultStatus == null ? base.effectiveResultStatus() : effectiveResultStatus,
                nominalTargetCandidateCount == null
                        ? base.nominalTargetCandidateCount() : nominalTargetCandidateCount,
                effectiveOccurrenceCount == null ? base.effectiveOccurrenceCount() : effectiveOccurrenceCount,
                historyStatus == null ? base.historyStatus() : historyStatus,
                ambiguityStatus == null ? base.ambiguityStatus() : ambiguityStatus,
                ambiguityReason == null ? base.ambiguityReason() : ambiguityReason,
                base.selectionStatus(),
                expectedAbsenceReason == null ? base.expectedAbsenceReason() : expectedAbsenceReason,
                unsupportedReason == null ? base.unsupportedReason() : unsupportedReason,
                blocking == null ? base.blocking() : blocking,
                blockingStatus == null ? base.blockingStatus() : blockingStatus,
                blockedCapabilities == null ? base.blockedCapabilities() : blockedCapabilities,
                f2dCompatibility == null ? base.f2dCompatibility() : f2dCompatibility,
                base.provenance(),
                base.evidenceHash(),
                base.resultHash());
    }
}
