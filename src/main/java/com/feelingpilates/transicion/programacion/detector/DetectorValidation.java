package com.feelingpilates.transicion.programacion.detector;

import java.util.Objects;

import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.InputErrorCode.REQUIRED_VALUE_MISSING;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_CANCELACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_EXCEPCION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.LEGACY_RECURRENTE;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_ADICION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_CANCELACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.NEW_REEMPLAZO;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.RESERVA;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.LEGACY;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH;

final class DetectorValidation {

    private DetectorValidation() {
    }

    static <T> T required(T value, String name) {
        if (value == null) {
            throw new DetectorInputInvalidException(REQUIRED_VALUE_MISSING, name + " is required");
        }
        return value;
    }

    static String requiredText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new DetectorInputInvalidException(REQUIRED_VALUE_MISSING, name + " is required");
        }
        return value;
    }

    static boolean equal(Object left, Object right) {
        return Objects.equals(left, right);
    }

    static void validateSourceSystemAtom(
            DetectorVocabulary.SourceSystem sourceSystem,
            DetectorVocabulary.SourceAtomType sourceAtomType) {
        boolean legacyAtom = sourceAtomType == RESERVA
                || sourceAtomType == LEGACY_RECURRENTE
                || sourceAtomType == LEGACY_EXCEPCION
                || sourceAtomType == LEGACY_CANCELACION;
        boolean newAtom = sourceAtomType == NEW_CANCELACION
                || sourceAtomType == NEW_REEMPLAZO
                || sourceAtomType == NEW_ADICION;
        if ((sourceSystem == LEGACY && !legacyAtom)
                || (sourceSystem == NEW_DARK_LAUNCH && !newAtom)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SOURCE_SHAPE,
                    "source system " + sourceSystem + " is incompatible with source atom " + sourceAtomType);
        }
    }
}
