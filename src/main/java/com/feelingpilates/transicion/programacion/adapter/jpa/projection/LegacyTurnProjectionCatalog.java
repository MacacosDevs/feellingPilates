package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LegacyTurnProjectionCatalog {

    public static final String MEMBERS_SQL = "SELECT t.id AS turn_id, t.tipo AS turn_type, t.activo AS turn_active, t.salon_id AS salon_id, t.dia_semana AS day_of_week, t.fecha AS turn_date, t.hora_inicio AS turn_start, t.hora_fin AS turn_end, t.creado_en AS created_at_technical, t.actualizado_en AS updated_at_technical, m.usuario_id AS member_instructor_id FROM public.turno_instructor t LEFT JOIN public.turno_instructor_usuario m ON m.turno_id = t.id WHERE t.salon_id IN (:salonIds) AND t.activo = :active AND ((t.tipo = :recurrentType AND t.dia_semana = :dayOfWeek) OR ((t.tipo = :exceptionType OR t.tipo = :cancellationType) AND t.fecha = :fecha)) ORDER BY t.id, m.usuario_id NULLS FIRST";
    public static final String ASSIGNMENTS_SQL = "SELECT a.turno_id AS turn_id, a.usuario_id AS instructor_id, a.tipo_actividad_id AS activity_id, a.hora_inicio AS assignment_start_raw, a.hora_fin AS assignment_end_raw FROM public.turno_instructor_asignacion a WHERE a.turno_id IN (:turnIds) ORDER BY a.turno_id, a.usuario_id, a.tipo_actividad_id";
    public static final String ISOLATION_SQL = "SELECT current_setting('transaction_isolation')";
    public static final String READ_ONLY_SQL = "SELECT current_setting('transaction_read_only')";
    public static final String RESOURCE_SQL = "SELECT current_database() AS database_name, current_schema() AS schema_name";
    public static final String SNAPSHOT_SQL = "SELECT pg_current_snapshot()::text";

    public static final String MEMBERS_ID = "9852b6e9487a71cb47d76e834e82eceafcfbdd194b6e66d718f7da1ccdf3a769";
    public static final String ASSIGNMENTS_ID = "6b21c28ee8961f783e986704604791181aa175592abc0b73553fa321445ce219";
    public static final String ISOLATION_ID = "4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e";
    public static final String READ_ONLY_ID = "9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7";
    public static final String RESOURCE_ID = "0ed00ba3ec87635658759a60f48bc9ea57df82a338b863f23be641fdf6b7b3ad";
    public static final String SNAPSHOT_ID = "24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0";

    public static final LegacyTurnProjectionCatalog R2_LEGACY_TURN_V1 = new LegacyTurnProjectionCatalog();

    private final Map<String, Statement> statements;

    private LegacyTurnProjectionCatalog() {
        LinkedHashMap<String, Statement> catalog = new LinkedHashMap<>();
        register(catalog, new Statement("R2_LEGACY_MEMBERS_V1", MEMBERS_ID, MEMBERS_SQL,
                List.of(new Bind("salonIds", java.util.UUID.class, true),
                        new Bind("active", Boolean.class, false),
                        new Bind("recurrentType", String.class, false),
                        new Bind("dayOfWeek", Short.class, false),
                        new Bind("exceptionType", String.class, false),
                        new Bind("cancellationType", String.class, false),
                        new Bind("fecha", java.time.LocalDate.class, false))));
        register(catalog, new Statement("R2_LEGACY_ASSIGNMENTS_V1", ASSIGNMENTS_ID, ASSIGNMENTS_SQL,
                List.of(new Bind("turnIds", java.util.UUID.class, true))));
        register(catalog, new Statement("R2_TX_ISOLATION_V1", ISOLATION_ID, ISOLATION_SQL, List.of()));
        register(catalog, new Statement("R2_TX_READ_ONLY_V1", READ_ONLY_ID, READ_ONLY_SQL, List.of()));
        register(catalog, new Statement("R2_TX_RESOURCE_IDENTITY_V1", RESOURCE_ID, RESOURCE_SQL, List.of()));
        register(catalog, new Statement("R2_TX_SNAPSHOT_V1", SNAPSHOT_ID, SNAPSHOT_SQL, List.of()));
        if (catalog.size() != 6) throw new IllegalStateException("R2 SQL catalog must contain six statements");
        statements = Map.copyOf(catalog);
    }

    public LegacyTurnReadContext.ProjectionCatalogVersion version() {
        return LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1;
    }

    public String projectionContractId() { return "R2_LEGACY_TURN_PROJECTION"; }
    public String projectionContractVersion() { return "V1"; }
    public String mapperContract() { return "LegacyTurnProjectionMapper/V1"; }

    public Statement statement(String logicalId) {
        Statement statement = statements.values().stream()
                .filter(candidate -> candidate.logicalId().equals(logicalId)).findFirst().orElse(null);
        if (statement == null) throw new IllegalArgumentException("Unknown R2 SQL statement");
        return statement;
    }

    public Map<String, String> statementIdsToLogicalIds() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        statements.values().forEach(statement -> result.put(statement.catalogId(), statement.logicalId()));
        return Map.copyOf(result);
    }

    public Map<String, Statement> statements() { return statements; }

    private void register(Map<String, Statement> target, Statement statement) {
        String canonical = statement.sql().replaceAll("\\s+", " ");
        String canonicalForId = switch (statement.logicalId()) {
            case "R2_LEGACY_MEMBERS_V1" -> canonical.replaceAll("\\(:salonIds\\)", "(?*)")
                    .replace(":active", "?").replace(":recurrentType", "?")
                    .replace(":dayOfWeek", "?").replace(":exceptionType", "?")
                    .replace(":cancellationType", "?").replace(":fecha", "?");
            case "R2_LEGACY_ASSIGNMENTS_V1" -> canonical.replace("(:turnIds)", "(?*)");
            default -> canonical;
        };
        if (!statement.catalogId().equals(catalogId(canonicalForId))) {
            throw new IllegalStateException("R2 SQL catalog identity drift: " + statement.logicalId());
        }
        if (target.put(statement.logicalId(), statement) != null) {
            throw new IllegalStateException("Duplicate R2 SQL statement");
        }
    }

    private static String catalogId(String canonical) {
        byte[] sql = canonical.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(("F2E_SQL_CATALOG_ID_V1\n" + sql.length + ":").getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest.digest(sql));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record Statement(String logicalId, String catalogId, String sql, List<Bind> binds) {
        public Statement {
            binds = List.copyOf(binds);
        }
    }

    public record Bind(String name, Class<?> javaType, boolean collection) { }
}
