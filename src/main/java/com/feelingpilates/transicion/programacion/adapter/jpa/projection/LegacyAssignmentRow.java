package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import java.time.LocalTime;
import java.util.UUID;

public record LegacyAssignmentRow(
        UUID turnId,
        UUID instructorId,
        UUID activityId,
        LocalTime assignmentStartRaw,
        LocalTime assignmentEndRaw) {
}
