package com.feelingpilates.transicion.programacion.read;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LegacyAdapterInputInvalid extends IllegalArgumentException {

    private final List<LegacyAdapterRejection> rejections;

    public LegacyAdapterInputInvalid(List<LegacyAdapterRejection> rejections) {
        this(rejections, null);
    }

    public LegacyAdapterInputInvalid(List<LegacyAdapterRejection> rejections, Throwable cause) {
        super("Legacy Turn adapter rejected " + (rejections == null ? 0 : rejections.size()) + " unit(s)", cause);
        if (rejections == null || rejections.isEmpty() || rejections.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("R2 rejection list must be non-empty");
        }
        this.rejections = List.copyOf(rejections);
    }

    public List<LegacyAdapterRejection> rejections() {
        return rejections;
    }

    static LegacyAdapterInputInvalid caller(String queryId, LocalDate fecha, Set<UUID> salonIds) {
        Map<String, String> scope = new LinkedHashMap<>();
        if (fecha != null) scope.put("fecha", fecha.toString());
        if (salonIds != null) {
            scope.put("validSalonCount", Long.toString(salonIds.stream().filter(java.util.Objects::nonNull).count()));
        }
        return new LegacyAdapterInputInvalid(List.of(new LegacyAdapterRejection(
                LegacyAdapterRejection.Code.ADAPTER_INPUT_INVALID, queryId, scope,
                "INVALID_REQUIRED_FIELD", List.of(), 1, 1)));
    }
}
