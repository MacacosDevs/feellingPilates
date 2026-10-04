package com.feelingpilates.transicion.programacion.read;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Operational rejection: never a successful empty read or detector decision. */
public final class NominalReadFailure extends RuntimeException {
    public enum Category { INVALID_INPUT, TRANSACTION_CONTEXT_INVALID, MALFORMED_PROJECTION,
        DUPLICATE_SERIES_ON_DATE, CARDINALITY_OR_BACKING_MISMATCH, DATABASE_READ_FAILURE }
    private final Category category;
    private final LocalDate fecha;
    private final List<UUID> physicalIds;
    public NominalReadFailure(Category category, LocalDate fecha, List<UUID> physicalIds, Throwable cause) {
        super("R3 nominal read rejected: " + category, cause);
        this.category = java.util.Objects.requireNonNull(category);
        this.fecha = fecha;
        this.physicalIds = List.copyOf(physicalIds);
    }
    public NominalReadFailure(Category category, LocalDate fecha) { this(category, fecha, List.of(), null); }
    public Category category() { return category; }
    public LocalDate fecha() { return fecha; }
    public List<UUID> physicalIds() { return physicalIds; }
}
