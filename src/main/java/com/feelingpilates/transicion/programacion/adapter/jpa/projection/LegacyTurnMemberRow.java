package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LegacyTurnMemberRow(
        UUID turnId,
        String type,
        Boolean active,
        UUID salonId,
        Short dayOfWeek,
        LocalDate date,
        LocalTime turnStart,
        LocalTime turnEnd,
        OffsetDateTime createdAtTechnical,
        OffsetDateTime updatedAtTechnical,
        UUID memberInstructorId) {
}
