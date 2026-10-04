package com.feelingpilates.transicion.programacion.detector;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.DATE;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.generation;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.provenance;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.request;
import static com.feelingpilates.transicion.programacion.detector.DetectorTestFixtures.reservation;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_RECURRENTE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.LEGACY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetectorImmutabilityTest {

    @Test
    void snapshotsCopianColeccionesDefensivamente() {
        Map<String, String> fields = new HashMap<>();
        fields.put("date", DATE.toString());
        GenericSourceSnapshot snapshot = new GenericSourceSnapshot(
                LEGACY,
                LEGACY_RECURRENTE,
                "legacy-1",
                "snapshot-1",
                "fingerprint-1",
                fields,
                provenance("legacy-1"));
        fields.put("mutated", "true");

        assertThat(snapshot.observableFields()).doesNotContainKey("mutated");
        assertThatThrownBy(() -> snapshot.observableFields().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void resultadosYCandidatesNoExponenColeccionesMutables() {
        DetectorResult result = new DetectorClassifier().classify(
                request(reservation(), generation(2), ClassificationContext.standard()));

        assertThatThrownBy(() -> result.candidates().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.candidates().getFirst().candidateEvidence().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.candidates().getFirst().normalizedObservableFields().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.candidates().getFirst().matchingDimensions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.blockedCapabilities().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void candidateGenerationResultCopiaLaListaOriginal() {
        List<DetectorCandidate> mutable = new ArrayList<>(generation(1).candidates());
        CandidateGenerationResult result = new CandidateGenerationResult(
                mutable, F2DCompatibilityResult.compatible());
        mutable.clear();

        assertThat(result.candidates()).hasSize(1);
        assertThatThrownBy(() -> result.candidates().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
