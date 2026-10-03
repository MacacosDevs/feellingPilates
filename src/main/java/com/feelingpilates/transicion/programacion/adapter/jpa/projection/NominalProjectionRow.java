package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/** Exact 23-column scalar projection; validity is checked all-or-nothing by the mapper. */
public record NominalProjectionRow(
        UUID assignmentSeriesId,
        UUID assignmentId,
        UUID assignmentBlockId,
        UUID blockId,
        UUID blockSeriesId,
        UUID salonId,
        UUID instructorId,
        UUID activityId,
        LocalTime assignmentStart,
        LocalTime assignmentEnd,
        LocalTime blockStart,
        LocalTime blockEnd,
        LocalDate assignmentFrom,
        LocalDate assignmentUntil,
        Boolean assignmentActive,
        LocalDate blockFrom,
        LocalDate blockUntil,
        Boolean blockActive,
        OffsetDateTime assignmentCreated,
        OffsetDateTime assignmentUpdated,
        OffsetDateTime blockCreated,
        OffsetDateTime blockUpdated,
        Short blockDay) { }
