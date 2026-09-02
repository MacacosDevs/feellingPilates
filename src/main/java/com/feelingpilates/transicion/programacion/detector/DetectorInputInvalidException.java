package com.feelingpilates.transicion.programacion.detector;

import static java.util.Objects.requireNonNull;

public final class DetectorInputInvalidException extends IllegalArgumentException {

    private final DetectorVocabulary.InputErrorCode code;

    public DetectorInputInvalidException(DetectorVocabulary.InputErrorCode code, String message) {
        super(message);
        this.code = requireNonNull(code, "code");
    }

    public DetectorVocabulary.InputErrorCode code() {
        return code;
    }
}
