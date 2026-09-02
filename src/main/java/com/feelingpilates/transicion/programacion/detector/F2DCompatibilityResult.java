package com.feelingpilates.transicion.programacion.detector;

import java.util.List;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_AUTHORITY_CONFLICT;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.F2DCompatibilityStatus.F2D_CONTRACT_COMPATIBLE;

public record F2DCompatibilityResult(
        DetectorVocabulary.F2DCompatibilityStatus status,
        boolean blocking,
        List<String> conflictDetails) {

    public F2DCompatibilityResult {
        status = required(status, "status");
        conflictDetails = List.copyOf(required(conflictDetails, "conflictDetails"));
        if (status == F2D_CONTRACT_COMPATIBLE && (blocking || !conflictDetails.isEmpty())) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "compatible F2D results cannot block or contain conflicts");
        }
        if (status == F2D_AUTHORITY_CONFLICT && (!blocking || conflictDetails.isEmpty())) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_SCENARIO,
                    "F2D authority conflicts must be blocking and explained");
        }
    }

    public static F2DCompatibilityResult compatible() {
        return new F2DCompatibilityResult(F2D_CONTRACT_COMPATIBLE, false, List.of());
    }

    public static F2DCompatibilityResult conflict(List<String> details) {
        return new F2DCompatibilityResult(F2D_AUTHORITY_CONFLICT, true, details);
    }
}
