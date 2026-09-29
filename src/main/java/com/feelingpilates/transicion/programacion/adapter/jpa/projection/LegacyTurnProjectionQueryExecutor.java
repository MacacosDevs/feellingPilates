package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import com.feelingpilates.transicion.programacion.read.LegacyAdapterInputInvalid;
import com.feelingpilates.transicion.programacion.read.LegacyAdapterRejection;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import jakarta.persistence.EntityManager;
import org.hibernate.query.NativeQuery;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class LegacyTurnProjectionQueryExecutor {

    private final EntityManager entityManager;
    private final LegacyTurnProjectionCatalog catalog;

    public LegacyTurnProjectionQueryExecutor(EntityManager entityManager, LegacyTurnProjectionCatalog catalog) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        if (catalog != LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1) {
            throw new IllegalStateException("R2 projection catalog binding not proven");
        }
    }

    public LegacyTurnProjectionCatalog catalog() {
        return catalog;
    }

    public List<LegacyTurnMemberRow> consultarMiembros(LegacyTurnScope scope) {
        Objects.requireNonNull(scope, "scope");
        NativeQuery<?> query = nativeQuery("R2_LEGACY_MEMBERS_V1");
        query.setParameterList("salonIds", scope.salonIdsNaturales(), UUID.class);
        query.setParameter("active", Boolean.TRUE, Boolean.class);
        query.setParameter("recurrentType", "RECURRENTE", String.class);
        query.setParameter("dayOfWeek", scope.dayOfWeekDomingoCero(), Short.class);
        query.setParameter("exceptionType", "EXCEPCION", String.class);
        query.setParameter("cancellationType", "CANCELACION", String.class);
        query.setParameter("fecha", scope.fecha(), LocalDate.class);
        return materializarMiembros(query.getResultList(), scope);
    }

    public List<LegacyAssignmentRow> consultarAsignaciones(List<UUID> turnIds) {
        if (turnIds == null || turnIds.isEmpty() || turnIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Usable R2 parent turn IDs are required");
        }
        List<UUID> naturales = turnIds.stream().distinct().sorted().toList();
        NativeQuery<?> query = nativeQuery("R2_LEGACY_ASSIGNMENTS_V1");
        query.setParameterList("turnIds", naturales, UUID.class);
        return materializarAsignaciones(query.getResultList());
    }

    public String consultarAislamiento() {
        return scalarString("R2_TX_ISOLATION_V1");
    }

    public String consultarSoloLectura() {
        return scalarString("R2_TX_READ_ONLY_V1");
    }

    public ResourceIdentity consultarIdentidadRecurso() {
        List<?> rows = nativeQuery("R2_TX_RESOURCE_IDENTITY_V1").getResultList();
        if (rows.size() != 1 || !(rows.getFirst() instanceof Object[] values) || values.length != 2
                || !(values[0] instanceof String database) || database.isBlank()
                || !(values[1] instanceof String schema) || schema.isBlank()) {
            throw new IllegalStateException("R2 database resource identity not proven");
        }
        return new ResourceIdentity(database, schema);
    }

    public String consultarSnapshot() {
        return scalarString("R2_TX_SNAPSHOT_V1");
    }

    private String scalarString(String logicalId) {
        List<?> rows = nativeQuery(logicalId).getResultList();
        if (rows.size() != 1 || !(rows.getFirst() instanceof String value) || value.isBlank()) {
            throw new IllegalStateException("Invalid R2 transaction probe result: " + logicalId);
        }
        return value;
    }

    private NativeQuery<?> nativeQuery(String logicalId) {
        return entityManager.createNativeQuery(catalog.statement(logicalId).sql()).unwrap(NativeQuery.class);
    }

    private List<LegacyTurnMemberRow> materializarMiembros(List<?> results, LegacyTurnScope scope) {
        List<LegacyTurnMemberRow> rows = new ArrayList<>(results.size());
        for (int index = 0; index < results.size(); index++) {
            Object result = results.get(index);
            if (!(result instanceof Object[] values) || values.length != 11) {
                throw invalidProjection("R2_LEGACY_MEMBERS_V1", scope, index + 1, null,
                        new IllegalArgumentException("Unexpected R2 MEMBERS projection shape"));
            }
            try {
                rows.add(new LegacyTurnMemberRow(
                        uuid(values[0]), text(values[1]), bool(values[2]), uuid(values[3]),
                        shortValue(values[4]), date(values[5]), time(values[6]), time(values[7]),
                        timestamp(values[8]), timestamp(values[9]), uuid(values[10])));
            } catch (RuntimeException exception) {
                throw invalidProjection("R2_LEGACY_MEMBERS_V1", scope, index + 1, null, exception);
            }
        }
        return List.copyOf(rows);
    }

    private List<LegacyAssignmentRow> materializarAsignaciones(List<?> results) {
        List<LegacyAssignmentRow> rows = new ArrayList<>(results.size());
        for (int index = 0; index < results.size(); index++) {
            Object result = results.get(index);
            if (!(result instanceof Object[] values) || values.length != 5) {
                throw invalidProjection("R2_LEGACY_ASSIGNMENTS_V1", null, index + 1, null,
                        new IllegalArgumentException("Unexpected R2 ASSIGNMENTS projection shape"));
            }
            try {
                rows.add(new LegacyAssignmentRow(
                        uuid(values[0]), uuid(values[1]), uuid(values[2]), time(values[3]), time(values[4])));
            } catch (RuntimeException exception) {
                throw invalidProjection("R2_LEGACY_ASSIGNMENTS_V1", null, index + 1, null, exception);
            }
        }
        return List.copyOf(rows);
    }

    private LegacyAdapterInputInvalid invalidProjection(
            String queryId, LegacyTurnScope scope, int ordinal, String column, RuntimeException cause) {
        Map<String, String> safeScope = scope == null ? Map.of() : Map.of("fecha", scope.fecha().toString(),
                "salonCount", Integer.toString(scope.salonIds().size()));
        List<String> ids = column == null ? List.of() : List.of(column);
        return new LegacyAdapterInputInvalid(List.of(new LegacyAdapterRejection(
                LegacyAdapterRejection.Code.ADAPTER_INPUT_INVALID, queryId, safeScope,
                "INVALID_REQUIRED_FIELD", ids, ordinal, 1)), cause);
    }

    private UUID uuid(Object value) {
        if (value == null || value instanceof UUID) return (UUID) value;
        throw new IllegalArgumentException("Expected PostgreSQL uuid");
    }

    private String text(Object value) {
        if (value == null || value instanceof String) return (String) value;
        throw new IllegalArgumentException("Expected PostgreSQL varchar/name");
    }

    private Boolean bool(Object value) {
        if (value == null || value instanceof Boolean) return (Boolean) value;
        throw new IllegalArgumentException("Expected PostgreSQL boolean");
    }

    private Short shortValue(Object value) {
        if (value == null || value instanceof Short) return (Short) value;
        throw new IllegalArgumentException("Expected PostgreSQL smallint");
    }

    private LocalDate date(Object value) {
        if (value == null || value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof Date sqlDate) return sqlDate.toLocalDate();
        throw new IllegalArgumentException("Expected PostgreSQL date");
    }

    private LocalTime time(Object value) {
        if (value == null || value instanceof LocalTime) return (LocalTime) value;
        if (value instanceof Time sqlTime) return sqlTime.toLocalTime();
        throw new IllegalArgumentException("Expected PostgreSQL time");
    }

    private OffsetDateTime timestamp(Object value) {
        if (value == null || value instanceof OffsetDateTime) return (OffsetDateTime) value;
        if (value instanceof Instant instant) return instant.atOffset(ZoneOffset.UTC);
        if (value instanceof Timestamp timestamp) return timestamp.toInstant().atOffset(ZoneOffset.UTC);
        if (value instanceof LocalDateTime local) return local.atOffset(ZoneOffset.UTC);
        throw new IllegalArgumentException("Expected PostgreSQL timestamptz");
    }

    public record ResourceIdentity(String databaseName, String schemaName) { }
}
