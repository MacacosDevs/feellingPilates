package com.feelingpilates.transicion.programacion.read;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record LegacyTurnScope(Set<UUID> salonIds, LocalDate fecha) {

    public LegacyTurnScope {
        if (salonIds == null || salonIds.isEmpty() || salonIds.stream().anyMatch(java.util.Objects::isNull)
                || fecha == null) {
            throw LegacyAdapterInputInvalid.caller("R2_LEGACY_MEMBERS_V1", fecha, salonIds);
        }
        LinkedHashSet<UUID> copia = new LinkedHashSet<>(salonIdsNaturales(salonIds));
        if (copia.size() != salonIds.size()) {
            throw LegacyAdapterInputInvalid.caller("R2_LEGACY_MEMBERS_V1", fecha, salonIds);
        }
        salonIds = Collections.unmodifiableSet(copia);
    }

    public List<UUID> salonIdsNaturales() {
        return salonIdsNaturales(salonIds);
    }

    public List<UUID> salonIdsUnsigned() {
        return salonIds.stream().sorted(LegacyTurnReadContext.UUID_UNSIGNED).toList();
    }

    public short dayOfWeekDomingoCero() {
        return (short) (fecha.getDayOfWeek().getValue() % 7);
    }

    public byte[] bytesCanonicos() {
        java.util.ArrayList<byte[]> partes = new java.util.ArrayList<>();
        partes.add(LegacyTurnReadContext.utf8("F2E-R2-READ-SCOPE-V1"));
        partes.add(LegacyTurnReadContext.utf8("READ_FOR_DATE"));
        partes.add(LegacyTurnReadContext.ascii(Integer.toString(salonIds.size())));
        salonIdsUnsigned().forEach(id -> partes.add(LegacyTurnReadContext.utf8(id.toString())));
        partes.add(LegacyTurnReadContext.utf8(fecha.toString()));
        return LegacyTurnReadContext.secuencia(partes);
    }

    public String canonical() {
        return LegacyTurnReadContext.utf8Estricto(bytesCanonicos());
    }

    private static List<UUID> salonIdsNaturales(Set<UUID> ids) {
        return ids.stream().sorted().toList();
    }
}
