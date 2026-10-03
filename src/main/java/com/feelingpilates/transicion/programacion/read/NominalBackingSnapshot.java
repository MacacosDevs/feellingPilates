package com.feelingpilates.transicion.programacion.read;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

public record NominalBackingSnapshot(
        ReferenciaOcurrencia reference, LocalDate fecha, Short dayOfWeek,
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
        Short blockDay) {
    public NominalBackingSnapshot {
        if (reference == null || fecha == null || dayOfWeek == null || !fecha.equals(reference.fecha())
                || reference.tipo() != ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION
                || !java.util.Objects.equals(reference.id(), assignmentSeriesId)
                || dayOfWeek != (short)(fecha.getDayOfWeek().getValue() % 7)
                || !java.util.Objects.equals(dayOfWeek, blockDay))
            throw new NominalReadFailure(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH, fecha);
        java.util.Objects.requireNonNull(assignmentSeriesId, "assignmentSeriesId");
        java.util.Objects.requireNonNull(assignmentId, "assignmentId");
        java.util.Objects.requireNonNull(assignmentBlockId, "assignmentBlockId");
        java.util.Objects.requireNonNull(blockId, "blockId");
        java.util.Objects.requireNonNull(blockSeriesId, "blockSeriesId");
        java.util.Objects.requireNonNull(salonId, "salonId");
        java.util.Objects.requireNonNull(instructorId, "instructorId");
        java.util.Objects.requireNonNull(activityId, "activityId");
        java.util.Objects.requireNonNull(assignmentStart, "assignmentStart");
        java.util.Objects.requireNonNull(assignmentEnd, "assignmentEnd");
        java.util.Objects.requireNonNull(blockStart, "blockStart");
        java.util.Objects.requireNonNull(blockEnd, "blockEnd");
        java.util.Objects.requireNonNull(assignmentFrom, "assignmentFrom");
        java.util.Objects.requireNonNull(assignmentActive, "assignmentActive");
        java.util.Objects.requireNonNull(blockFrom, "blockFrom");
        java.util.Objects.requireNonNull(blockActive, "blockActive");
        java.util.Objects.requireNonNull(assignmentCreated, "assignmentCreated");
        java.util.Objects.requireNonNull(assignmentUpdated, "assignmentUpdated");
        java.util.Objects.requireNonNull(blockCreated, "blockCreated");
        java.util.Objects.requireNonNull(blockUpdated, "blockUpdated");
        java.util.Objects.requireNonNull(blockDay, "blockDay");
    }
}
