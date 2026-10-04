package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.EffectiveResultStatus.NOT_APPLICABLE;

public record HistoricalProgrammingTargetSnapshot(
        ReferenciaOcurrencia targetReference,
        DetectorVocabulary.EffectiveResultStatus currentEffectiveOutcome) {

    public HistoricalProgrammingTargetSnapshot {
        targetReference = required(targetReference, "targetReference");
        required(targetReference.tipo(), "targetReference.tipo");
        required(targetReference.id(), "targetReference.id");
        required(targetReference.fecha(), "targetReference.fecha");
        currentEffectiveOutcome = required(currentEffectiveOutcome, "currentEffectiveOutcome");
        if (currentEffectiveOutcome == NOT_APPLICABLE) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SOURCE_SHAPE,
                    "a demonstrated historical target requires an observable current outcome");
        }
    }
}
