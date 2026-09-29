package com.feelingpilates.transicion.programacion.read;

import java.util.List;
import java.util.Map;

public record LegacyAdapterRejection(
        Code code,
        String queryId,
        Map<String, String> safeScope,
        String marker,
        List<String> safeIds,
        int physicalRowOrdinal,
        int observedPhysicalRowCount) {

    public LegacyAdapterRejection {
        if (code == null || queryId == null || queryId.isBlank() || safeScope == null
                || marker == null || marker.isBlank() || safeIds == null
                || safeIds.stream().anyMatch(java.util.Objects::isNull)
                || physicalRowOrdinal < 1 || observedPhysicalRowCount < 1) {
            throw new IllegalArgumentException("Invalid R2 rejection");
        }
        safeScope = Map.copyOf(safeScope);
        safeIds = List.copyOf(safeIds);
    }

    public enum Code {
        ADAPTER_INPUT_INVALID,
        READ_SET_INVARIANT_VIOLATION
    }
}
