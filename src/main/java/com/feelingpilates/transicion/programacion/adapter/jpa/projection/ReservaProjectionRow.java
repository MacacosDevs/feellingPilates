package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaProjectionRow(
        UUID reservationId,
        String state,
        LocalDate date,
        UUID salonId,
        UUID instructorId,
        UUID activityId,
        LocalTime start,
        LocalTime end,
        OffsetDateTime createdAtTechnical,
        OffsetDateTime updatedAtTechnical) {
}
