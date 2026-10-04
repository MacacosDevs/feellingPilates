package com.feelingpilates.transicion.programacion.read;

import java.time.ZoneId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record ReadSnapshotContext(
        String runIdentity,
        String attemptIdentity,
        String readerInvocationIdentity,
        String sourceName,
        String schemaFingerprint,
        ProjectionCatalogVersion projectionCatalogVersion,
        String ruleCatalogVersion,
        ZoneId businessZone,
        SnapshotClaim snapshotClaim,
        String snapshotEvidenceId,
        String statementObservationFingerprint) {

    private static final Pattern HUELLA = Pattern.compile("[0-9a-f]{64}");

    public ReadSnapshotContext {
        runIdentity = textoRequerido(runIdentity, "runIdentity");
        attemptIdentity = textoRequerido(attemptIdentity, "attemptIdentity");
        readerInvocationIdentity = textoRequerido(readerInvocationIdentity, "readerInvocationIdentity");
        sourceName = textoRequerido(sourceName, "sourceName");
        schemaFingerprint = textoRequerido(schemaFingerprint, "schemaFingerprint");
        projectionCatalogVersion = Objects.requireNonNull(projectionCatalogVersion, "projectionCatalogVersion");
        if (projectionCatalogVersion != ProjectionCatalogVersion.R1_RESERVA_V1) {
            throw new IllegalArgumentException("Unsupported R1 projection catalog");
        }
        ruleCatalogVersion = textoRequerido(ruleCatalogVersion, "ruleCatalogVersion");
        businessZone = Objects.requireNonNull(businessZone, "businessZone");
        if (snapshotClaim != SnapshotClaim.SINGLE_READER_TEST) {
            throw new IllegalArgumentException("Unsupported R1 snapshot claim");
        }
        snapshotEvidenceId = huellaRequerida(snapshotEvidenceId, "snapshotEvidenceId");
        statementObservationFingerprint = huellaRequerida(
                statementObservationFingerprint, "statementObservationFingerprint");
    }

    private static String textoRequerido(String valor, String nombre) {
        if (valor == null || valor.isBlank() || valor.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(nombre + " is required");
        }
        try {
            java.nio.charset.StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap(valor));
        } catch (java.nio.charset.CharacterCodingException excepcion) {
            throw new IllegalArgumentException(nombre + " is not valid UTF-8", excepcion);
        }
        return valor;
    }

    private static String huellaRequerida(String valor, String nombre) {
        if (valor == null || !HUELLA.matcher(valor).matches()) {
            throw new IllegalArgumentException(nombre + " must be a lower-case SHA-256 digest");
        }
        return valor;
    }

    public enum SnapshotClaim {
        SINGLE_READER_TEST
    }

    public enum ProjectionCatalogVersion {
        R1_RESERVA_V1(
                "R1_RESERVA_PROJECTION",
                "V1",
                "R1_RESERVA_PROJECTION/V1",
                "LEGACY",
                "RESERVA",
                "public.reserva",
                "R1_RESERVA_BY_IDS_V1",
                "R1_RESERVA_BY_SCOPE_V1",
                "ReservaProjectionMapper",
                "V1",
                "SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, "
                        + "r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, "
                        + "r.actualizado_en FROM public.reserva r WHERE r.id IN (:reservationIds) ORDER BY r.id",
                "SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, "
                        + "r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, "
                        + "r.actualizado_en FROM public.reserva r WHERE r.salon_id IN (:salonIds) "
                        + "AND r.fecha >= :desde AND r.fecha <= :hasta ORDER BY r.id",
                "dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b",
                "c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a",
                List.of("reservationId", "state", "date", "salonId", "instructorId", "activityId",
                        "start", "end", "createdAtTechnical", "updatedAtTechnical",
                        "historicalProgrammingTarget"),
                List.of("id", "estado", "fecha", "salon_id", "instructor_id", "tipo_actividad_id",
                        "hora_inicio", "hora_fin", "creado_en", "actualizado_en", "NONE"),
                List.of("UUID", "TEXT", "DATE", "UUID", "UUID", "UUID", "TIME_MICROS",
                        "TIME_MICROS", "TIMESTAMP_UTC_MICROS", "TIMESTAMP_UTC_MICROS",
                        "OPTIONAL_HISTORICAL_TARGET"));

        private final String projectionContractId;
        private final String projectionContractVersion;
        private final String canonicalCatalogValue;
        private final String sourceSystem;
        private final String sourceAtomType;
        private final String physicalTable;
        private final String statementByIds;
        private final String statementByScope;
        private final String mapperContract;
        private final String mapperVersion;
        private final String sqlByIds;
        private final String sqlByScope;
        private final String statementIdByIds;
        private final String statementIdByScope;
        private final List<String> logicalFields;
        private final List<String> physicalFields;
        private final List<String> typeTags;

        ProjectionCatalogVersion(
                String projectionContractId,
                String projectionContractVersion,
                String canonicalCatalogValue,
                String sourceSystem,
                String sourceAtomType,
                String physicalTable,
                String statementByIds,
                String statementByScope,
                String mapperContract,
                String mapperVersion,
                String sqlByIds,
                String sqlByScope,
                String statementIdByIds,
                String statementIdByScope,
                List<String> logicalFields,
                List<String> physicalFields,
                List<String> typeTags) {
            this.projectionContractId = projectionContractId;
            this.projectionContractVersion = projectionContractVersion;
            this.canonicalCatalogValue = canonicalCatalogValue;
            this.sourceSystem = sourceSystem;
            this.sourceAtomType = sourceAtomType;
            this.physicalTable = physicalTable;
            this.statementByIds = statementByIds;
            this.statementByScope = statementByScope;
            this.mapperContract = mapperContract;
            this.mapperVersion = mapperVersion;
            this.sqlByIds = sqlByIds;
            this.sqlByScope = sqlByScope;
            this.statementIdByIds = statementIdByIds;
            this.statementIdByScope = statementIdByScope;
            this.logicalFields = List.copyOf(logicalFields);
            this.physicalFields = List.copyOf(physicalFields);
            this.typeTags = List.copyOf(typeTags);
            validarAutoridadInterna();
        }

        public String projectionContractId() { return projectionContractId; }
        public String projectionContractVersion() { return projectionContractVersion; }
        public String canonicalCatalogValue() { return canonicalCatalogValue; }
        public String sourceSystem() { return sourceSystem; }
        public String sourceAtomType() { return sourceAtomType; }
        public String physicalTable() { return physicalTable; }
        public String statementByIds() { return statementByIds; }
        public String statementByScope() { return statementByScope; }
        public String mapperContract() { return mapperContract; }
        public String mapperVersion() { return mapperVersion; }
        public String sqlByIds() { return sqlByIds; }
        public String sqlByScope() { return sqlByScope; }
        public String statementIdByIds() { return statementIdByIds; }
        public String statementIdByScope() { return statementIdByScope; }
        public List<String> logicalFields() { return logicalFields; }
        public List<String> physicalFields() { return physicalFields; }
        public List<String> projectedPhysicalColumns() { return physicalFields.subList(0, 10); }
        public List<String> typeTags() { return typeTags; }

        public void validarVinculoSql(
                String sqlIdentidades,
                String sqlScope,
                String idIdentidades,
                String idScope) {
            if (!this.sqlByIds.equals(sqlIdentidades)
                    || !this.sqlByScope.equals(sqlScope)
                    || !this.statementIdByIds.equals(idIdentidades)
                    || !this.statementIdByScope.equals(idScope)
                    || !this.statementIdByIds.equals(identificarSql(sqlIdentidades))
                    || !this.statementIdByScope.equals(identificarSql(sqlScope))) {
                throw new IllegalStateException("F2E projection catalog binding not proven");
            }
        }

        public void validarVinculoMapper(String contrato, String version) {
            if (!mapperContract.equals(contrato) || !mapperVersion.equals(version)) {
                throw new IllegalStateException("F2E projection catalog binding not proven");
            }
        }

        private void validarAutoridadInterna() {
            if (logicalFields.size() != 11 || physicalFields.size() != 11 || typeTags.size() != 11
                    || !"NONE".equals(physicalFields.get(10))) {
                throw new IllegalStateException("F2E projection catalog binding not proven");
            }
            validarVinculoSql(sqlByIds, sqlByScope, statementIdByIds, statementIdByScope);
            validarVinculoMapper(mapperContract, mapperVersion);
        }

        private static String identificarSql(String sql) {
            String canonica = sql.replace(":reservationIds", "?")
                    .replace(":salonIds", "?")
                    .replace(":desde", "?")
                    .replace(":hasta", "?")
                    .replace("IN (?)", "IN (?*)");
            byte[] bytesSql = canonica.getBytes(StandardCharsets.UTF_8);
            byte[] prefijo = ("F2E_SQL_CATALOG_ID_V1\n" + bytesSql.length + ":")
                    .getBytes(StandardCharsets.UTF_8);
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(prefijo);
                return java.util.HexFormat.of().formatHex(digest.digest(bytesSql));
            } catch (NoSuchAlgorithmException excepcion) {
                throw new IllegalStateException("SHA-256 unavailable", excepcion);
            }
        }
    }
}
