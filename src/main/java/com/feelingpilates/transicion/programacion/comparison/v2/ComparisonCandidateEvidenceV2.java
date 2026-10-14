package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import com.feelingpilates.transicion.programacion.detector.*;

/** Complete inspected pair, including excluded observations; never a selected mapping. */
public record ComparisonCandidateEvidenceV2(SourceSnapshot source, ProgrammingCandidateSnapshot target,
        String axis, Map<String, Observation> dimensions, boolean admitted) {
    public enum Match { MATCH, MISMATCH, UNOBSERVABLE }
    public record Observation(String sourceValue, String targetValue, Match match) {
        public Observation { Objects.requireNonNull(match); Objects.requireNonNull(sourceValue); Objects.requireNonNull(targetValue); }
    }
    public ComparisonCandidateEvidenceV2 {
        Objects.requireNonNull(source); Objects.requireNonNull(target); ComparisonCanonicalizerV2.requiredText(axis);
        dimensions = Map.copyOf(dimensions);
        ComparisonFailureV2.require(dimensions.keySet().equals(Set.of("date", "day", "salon", "instructor", "activity", "start", "end")),
                "CANDIDATE", "complete seven-dimension observation");
    }
}
