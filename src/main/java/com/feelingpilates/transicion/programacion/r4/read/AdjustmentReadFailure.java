package com.feelingpilates.transicion.programacion.r4.read;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public final class AdjustmentReadFailure extends RuntimeException {
    public enum Category { INVALID_INPUT, TRANSACTION_CONTEXT_INVALID, MALFORMED_PROJECTION,
        DUPLICATE_ACTIVE_TARGET_ON_DATE, CARDINALITY_OR_BACKING_MISMATCH, DATABASE_READ_FAILURE }
    private final Category category;
    private final LocalDate fecha;
    private final List<UUID> physicalIds;
    public AdjustmentReadFailure(Category category, LocalDate fecha) { this(category, fecha, List.of(), null); }
    public AdjustmentReadFailure(Category category, LocalDate fecha, List<UUID> ids, Throwable cause) {
        super(category.name(), cause); this.category=java.util.Objects.requireNonNull(category);
        this.fecha=fecha; this.physicalIds=List.copyOf(ids);
    }
    public Category category() { return category; }
    public LocalDate fecha() { return fecha; }
    public List<UUID> physicalIds() { return physicalIds; }
}
