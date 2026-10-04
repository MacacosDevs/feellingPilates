package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import com.feelingpilates.transicion.programacion.read.NominalReadSnapshotContext;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Frozen five-statement R3 catalog, including the positional occurrence contract. */
public final class NominalProjectionCatalog {
    public static final String DATA_SQL = "SELECT a.serie_id, a.id, a.bloque_id, b.id, b.serie_id, b.salon_id, a.instructor_id, a.tipo_actividad_id, a.hora_inicio, a.hora_fin, b.hora_inicio, b.hora_fin, a.vigente_desde, a.vigente_hasta, a.activo, b.vigente_desde, b.vigente_hasta, b.activo, a.creado_en, a.actualizado_en, b.creado_en, b.actualizado_en, b.dia_semana FROM programacion_asignacion a JOIN programacion_bloque b ON b.id = a.bloque_id WHERE a.activo = :assignmentActive AND b.activo = :blockActive AND a.vigente_desde <= :fecha AND (a.vigente_hasta IS NULL OR :fecha <= a.vigente_hasta) AND b.vigente_desde <= :fecha AND (b.vigente_hasta IS NULL OR :fecha <= b.vigente_hasta) AND b.dia_semana = :dayOfWeek ORDER BY a.serie_id, a.id";
    public static final String DATA_ID = "3686fea10cebe37883cfb9b42e2877969ca8b87b8b11b677c8ee912740f340b5";
    public static final String ISOLATION_SQL = "SELECT current_setting('transaction_isolation')";
    public static final String ISOLATION_ID = "4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e";
    public static final String READ_ONLY_SQL = "SELECT current_setting('transaction_read_only')";
    public static final String READ_ONLY_ID = "9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7";
    public static final String RESOURCE_SQL = "SELECT current_database() AS database_name, current_schema() AS schema_name";
    public static final String RESOURCE_ID = "0ed00ba3ec87635658759a60f48bc9ea57df82a338b863f23be641fdf6b7b3ad";
    public static final String SNAPSHOT_SQL = "SELECT pg_current_snapshot()::text";
    public static final String SNAPSHOT_ID = "24f02692f50e880b77a78f9ea64501ce276b7b0e2a93c880a4c23e03bbe19aa0";
    public static final NominalProjectionCatalog R3_NOMINAL_V1 = new NominalProjectionCatalog();
    public static final List<String> JDBC_OCCURRENCES = List.of("assignmentActive", "blockActive",
            "fecha", "fecha", "fecha", "fecha", "dayOfWeek");
    private final Map<String, Statement> statements;
    private NominalProjectionCatalog() {
        Map<String, Statement> m = new LinkedHashMap<>();
        register(m, new Statement("R3_NOMINAL_ON_DATE_V1", DATA_ID, DATA_SQL, List.of(
                new Bind("assignmentActive", Boolean.class), new Bind("blockActive", Boolean.class),
                new Bind("fecha", java.time.LocalDate.class), new Bind("dayOfWeek", Short.class))));
        register(m, new Statement("R3_TX_ISOLATION_V1", ISOLATION_ID, ISOLATION_SQL, List.of()));
        register(m, new Statement("R3_TX_READ_ONLY_V1", READ_ONLY_ID, READ_ONLY_SQL, List.of()));
        register(m, new Statement("R3_TX_RESOURCE_IDENTITY_V1", RESOURCE_ID, RESOURCE_SQL, List.of()));
        register(m, new Statement("R3_TX_SNAPSHOT_V1", SNAPSHOT_ID, SNAPSHOT_SQL, List.of()));
        statements = Map.copyOf(m);
    }
    public NominalReadSnapshotContext.ProjectionCatalogVersion version() {
        return NominalReadSnapshotContext.ProjectionCatalogVersion.R3_NOMINAL_V1;
    }
    public Map<String, Statement> statements() { return statements; }
    public Statement statement(String logicalId) {
        Statement s = statements.get(logicalId);
        if (s == null) throw new IllegalArgumentException("Unknown R3 statement");
        return s;
    }
    public Map<String, String> statementIdsToLogicalIds() {
        Map<String, String> result = new LinkedHashMap<>();
        statements.values().forEach(s -> result.put(s.catalogId(), s.logicalId()));
        return Map.copyOf(result);
    }
    public static String positionalSql() {
        return DATA_SQL.replaceAll(":(assignmentActive|blockActive|fecha|dayOfWeek)", "?");
    }
    public static List<String> expectedManifest() {
        return List.of(ISOLATION_ID, READ_ONLY_ID, RESOURCE_ID, SNAPSHOT_ID, DATA_ID,
                ISOLATION_ID, READ_ONLY_ID, RESOURCE_ID, SNAPSHOT_ID);
    }
    private void register(Map<String, Statement> m, Statement s) {
        String positional = s.sql().replaceAll(":(assignmentActive|blockActive|fecha|dayOfWeek)", "?");
        byte[] b = positional.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(("F2E_SQL_CATALOG_ID_V1\n" + b.length + ":").getBytes(StandardCharsets.UTF_8));
            if (!s.catalogId().equals(java.util.HexFormat.of().formatHex(digest.digest(b))))
                throw new IllegalStateException("R3 catalog drift");
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        if (m.put(s.logicalId(), s) != null) throw new IllegalStateException("Duplicate R3 SQL");
    }
    public record Statement(String logicalId, String catalogId, String sql, List<Bind> binds) {
        public Statement { binds = List.copyOf(binds); }
    }
    public record Bind(String name, Class<?> javaType) { }
}
