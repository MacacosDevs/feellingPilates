package com.feelingpilates.transicion.programacion.detector;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;

public record CandidateEvidenceItem(
        DetectorVocabulary.EvidenceDimension dimension,
        DetectorVocabulary.EvidenceKind kind,
        String sourceValue,
        String candidateValue,
        boolean matches) {

    public CandidateEvidenceItem {
        dimension = required(dimension, "dimension");
        kind = required(kind, "kind");
        sourceValue = requiredText(sourceValue, "sourceValue");
        candidateValue = requiredText(candidateValue, "candidateValue");
    }
}
