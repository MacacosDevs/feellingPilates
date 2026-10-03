package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import jakarta.persistence.EntityManager;
import org.hibernate.query.NativeQuery;
import com.feelingpilates.transicion.programacion.read.NominalReadFailure;
import java.time.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

public final class NominalProjectionQueryExecutor {
    private final EntityManager entityManager;
    private final NominalProjectionCatalog catalog;
    public NominalProjectionQueryExecutor(EntityManager entityManager, NominalProjectionCatalog catalog) {
        this.entityManager = Objects.requireNonNull(entityManager);
        this.catalog = Objects.requireNonNull(catalog);
        if (catalog != NominalProjectionCatalog.R3_NOMINAL_V1) throw new IllegalStateException("R3 catalog binding");
    }
    public NominalProjectionCatalog catalog() { return catalog; }
    public List<NominalProjectionRow> consultarNominal(LocalDate fecha) {
        if (fecha == null) throw new NominalReadFailure(NominalReadFailure.Category.INVALID_INPUT, null);
        try {
            NativeQuery<?> q = nativeQuery("R3_NOMINAL_ON_DATE_V1");
            q.setParameter("assignmentActive", Boolean.TRUE, Boolean.class);
            q.setParameter("blockActive", Boolean.TRUE, Boolean.class);
            q.setParameter("fecha", fecha, LocalDate.class);
            q.setParameter("dayOfWeek", (short)(fecha.getDayOfWeek().getValue() % 7), Short.class);
            return materializar(q.getResultList(), fecha);
        } catch (NominalReadFailure e) { throw e; }
        catch (RuntimeException e) {
            throw new NominalReadFailure(NominalReadFailure.Category.DATABASE_READ_FAILURE, fecha, List.of(), e);
        }
    }
    public List<NominalProjectionRow> materializar(List<?> results, LocalDate fecha) {
        if (results == null) throw new NominalReadFailure(NominalReadFailure.Category.MALFORMED_PROJECTION, fecha);
        List<NominalProjectionRow> rows = new ArrayList<>();
        for (Object result : results) {
            try {
                if (!(result instanceof Object[] v) || v.length != 23) throw new IllegalArgumentException();
                rows.add(new NominalProjectionRow(
                        uuid(v[0]), uuid(v[1]), uuid(v[2]), uuid(v[3]), uuid(v[4]), uuid(v[5]), uuid(v[6]), uuid(v[7]), time(v[8]), time(v[9]), time(v[10]), time(v[11]), date(v[12]), date(v[13]), bool(v[14]), date(v[15]), date(v[16]), bool(v[17]), timestamp(v[18]), timestamp(v[19]), timestamp(v[20]), timestamp(v[21]), shortValue(v[22])));
            } catch (RuntimeException e) {
                throw new NominalReadFailure(NominalReadFailure.Category.MALFORMED_PROJECTION, fecha, List.of(), e);
            }
        }
        return List.copyOf(rows);
    }
    private NativeQuery<?> nativeQuery(String id) {
        if (id.equals("R3_NOMINAL_ON_DATE_V1")) {
            var factory = entityManager.getEntityManagerFactory().unwrap(org.hibernate.engine.spi.SessionFactoryImplementor.class);
            if (!(factory.getJdbcServices().getDialect() instanceof ScalarPostgresDialect))
                throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, null);
        }
        return entityManager.createNativeQuery(catalog.statement(id).sql()).unwrap(NativeQuery.class);
    }
    public String consultarAislamiento() { return scalarString("R3_TX_ISOLATION_V1"); }
    public String consultarSoloLectura() { return scalarString("R3_TX_READ_ONLY_V1"); }
    public String consultarSnapshot() { return scalarString("R3_TX_SNAPSHOT_V1"); }
    private String scalarString(String id) {
        List<?> rows = nativeQuery(id).getResultList();
        if (rows.size() != 1 || !(rows.getFirst() instanceof String s) || s.isBlank())
            throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, null);
        return s;
    }
    public ResourceIdentity consultarIdentidadRecurso() {
        List<?> rows = nativeQuery("R3_TX_RESOURCE_IDENTITY_V1").getResultList();
        if (rows.size() != 1 || !(rows.getFirst() instanceof Object[] v) || v.length != 2
                || !(v[0] instanceof String db) || db.isBlank() || !(v[1] instanceof String schema) || schema.isBlank())
            throw new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID, null);
        return new ResourceIdentity(db, schema);
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
        throw new IllegalArgumentException("Expected PostgreSQL timestamptz");
    }

    /** Isolated R3 scalar dialect: JDBC 4.2 LocalTime extraction preserves all PostgreSQL microseconds. */
    public static final class ScalarPostgresDialect extends org.hibernate.dialect.PostgreSQLDialect {
        public ScalarPostgresDialect() { super(org.hibernate.dialect.DatabaseVersion.make(16)); }
        @Override
        public org.hibernate.type.descriptor.jdbc.JdbcType resolveSqlTypeDescriptor(String columnTypeName,
                int jdbcTypeCode, int precision, int scale,
                org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry registry) {
            if (jdbcTypeCode == java.sql.Types.TIME)
                return org.hibernate.type.descriptor.jdbc.LocalTimeJdbcType.INSTANCE;
            return super.resolveSqlTypeDescriptor(columnTypeName, jdbcTypeCode, precision, scale, registry);
        }
    }
    public record ResourceIdentity(String databaseName, String schemaName) { }
}
