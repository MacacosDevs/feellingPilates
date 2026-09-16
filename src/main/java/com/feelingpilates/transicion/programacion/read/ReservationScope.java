package com.feelingpilates.transicion.programacion.read;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record ReservationScope(Set<UUID> salonIds, LocalDate desde, LocalDate hasta) {

    public ReservationScope {
        if (salonIds == null || salonIds.isEmpty() || salonIds.stream().anyMatch(id -> id == null)) {
            throw scopeInvalido();
        }
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw scopeInvalido();
        }
        var ordenados = new LinkedHashSet<>(ReadSnapshotIdentifiers.ordenarUuid(salonIds));
        salonIds = Collections.unmodifiableSet(ordenados);
    }

    private static ReservationReadException scopeInvalido() {
        return new ReservationReadException(
                ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                java.util.Map.of("operation", "READ_BY_SCOPE", "scopeKind", "BY_SCOPE"));
    }
}
