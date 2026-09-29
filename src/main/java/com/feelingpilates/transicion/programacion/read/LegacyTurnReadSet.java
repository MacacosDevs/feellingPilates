package com.feelingpilates.transicion.programacion.read;

import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;

import java.util.HashSet;
import java.util.List;

public record LegacyTurnReadSet(List<GenericSourceSnapshot> sources) {

    public LegacyTurnReadSet {
        if (sources == null || sources.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("R2 sources are required");
        }
        sources = List.copyOf(sources);
        if (new HashSet<>(sources.stream().map(GenericSourceSnapshot::sourceIdentity).toList()).size()
                != sources.size()) {
            throw new IllegalArgumentException("Duplicate R2 source identity");
        }
    }
}
