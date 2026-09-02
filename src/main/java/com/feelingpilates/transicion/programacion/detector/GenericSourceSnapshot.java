package com.feelingpilates.transicion.programacion.detector;

import java.util.Map;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.RESERVA;

public record GenericSourceSnapshot(
        DetectorVocabulary.SourceSystem sourceSystem,
        DetectorVocabulary.SourceAtomType sourceAtomType,
        String sourceIdentity,
        String snapshotIdentity,
        String sourceFingerprint,
        Map<String, String> observableFields,
        EvidenceProvenance provenance) implements SourceSnapshot {

    public GenericSourceSnapshot {
        sourceSystem = required(sourceSystem, "sourceSystem");
        sourceAtomType = required(sourceAtomType, "sourceAtomType");
        DetectorValidation.validateSourceSystemAtom(sourceSystem, sourceAtomType);
        if (sourceAtomType == RESERVA) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SOURCE_SHAPE,
                    "Reserva must use ReservationSourceSnapshot");
        }
        sourceIdentity = requiredText(sourceIdentity, "sourceIdentity");
        snapshotIdentity = requiredText(snapshotIdentity, "snapshotIdentity");
        sourceFingerprint = requiredText(sourceFingerprint, "sourceFingerprint");
        observableFields = Map.copyOf(required(observableFields, "observableFields"));
        provenance = required(provenance, "provenance");
    }
}
