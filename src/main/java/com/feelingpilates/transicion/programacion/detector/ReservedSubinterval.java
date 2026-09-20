package com.feelingpilates.transicion.programacion.detector;

import java.time.LocalTime;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;

public record ReservedSubinterval(LocalTime start, LocalTime end) {

    public ReservedSubinterval {
        start = required(start, "start");
        end = required(end, "end");
        if (!end.isAfter(start)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_INTERVAL,
                    "reserved interval must be positive and half-open");
        }
    }

    public boolean isContainedIn(LocalTime candidateStart, LocalTime candidateEnd) {
        required(candidateStart, "candidateStart");
        required(candidateEnd, "candidateEnd");
        return !start.isBefore(candidateStart) && !end.isAfter(candidateEnd);
    }
}
