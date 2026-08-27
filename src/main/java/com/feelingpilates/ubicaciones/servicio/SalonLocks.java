package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.ubicaciones.entidad.Salon;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Adquiere todos los salones deduplicados en orden UUID ascendente. */
@Component
public class SalonLocks {

    private final SalonLock salonLock;

    public SalonLocks(SalonLock salonLock) {
        this.salonLock = salonLock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<Salon> adquirirOrdenados(Collection<UUID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("La colección de salones es obligatoria");
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .map(salonLock::adquirir)
                .toList();
    }
}
