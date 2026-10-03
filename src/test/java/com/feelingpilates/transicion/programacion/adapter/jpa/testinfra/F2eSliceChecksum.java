package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import com.feelingpilates.transicion.programacion.read.ReservationScope;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class F2eSliceChecksum {

    private F2eSliceChecksum() {
    }

    public static Resultado calcularPorIdentidades(DataSource dataSource, Set<UUID> identidades) {
        return calcularCongelado(dataSource, identidades, scopePorIdentidadesPrueba(List.copyOf(identidades)));
    }

    public static Set<UUID> congelarPorScope(DataSource dataSource, ReservationScope scope) {
        List<UUID> salones = ReadSnapshotIdentifiers.ordenarUuid(scope.salonIds());
        String marcadores = String.join(",", java.util.Collections.nCopies(salones.size(), "?"));
        try (var conexion = dataSource.getConnection(); var consulta = conexion.prepareStatement(
                "SELECT id FROM public.reserva WHERE salon_id IN (" + marcadores
                        + ") AND fecha >= ? AND fecha <= ? ORDER BY id")) {
            for (int i = 0; i < salones.size(); i++) consulta.setObject(i + 1, salones.get(i));
            consulta.setObject(salones.size() + 1, scope.desde());
            consulta.setObject(salones.size() + 2, scope.hasta());
            try (var filas = consulta.executeQuery()) {
                Set<UUID> ids = new java.util.LinkedHashSet<>();
                while (filas.next()) ids.add(filas.getObject(1, UUID.class));
                return Set.copyOf(ids);
            }
        } catch (SQLException excepcion) {
            throw new IllegalStateException("Unable to freeze reservation slice scope", excepcion);
        }
    }

    public static Resultado calcularPorScopeCongelado(
            DataSource dataSource, ReservationScope scope, Set<UUID> identidadesCongeladas) {
        return calcularCongelado(dataSource, identidadesCongeladas, scopePorRangoPrueba(
                List.copyOf(scope.salonIds()), scope.desde(), scope.hasta()));
    }

    private static Resultado calcularCongelado(DataSource dataSource, Set<UUID> identidades, byte[] scope) {
        List<UUID> ordenadas = ReadSnapshotIdentifiers.ordenarUuid(identidades);
        String marcadores = String.join(",", java.util.Collections.nCopies(ordenadas.size(), "?"));
        String sql = "SELECT id,salon_id,instructor_id,cliente_id,tipo_actividad_id,fecha,hora_inicio,"
                + "hora_fin,estado,creado_en,actualizado_en FROM public.reserva WHERE "
                + (ordenadas.isEmpty() ? "FALSE" : "id IN (" + marcadores + ")") + " ORDER BY id";
        try (var conexion = dataSource.getConnection(); var consulta = conexion.prepareStatement(sql)) {
            for (int indice = 0; indice < ordenadas.size(); indice++) {
                consulta.setObject(indice + 1, ordenadas.get(indice));
            }
            try (ResultSet filas = consulta.executeQuery()) {
                List<FilaHash> hashes = hashesFilas(filas);
                String tabla = hashTablaPrueba("public.reserva", hashes);
                return new Resultado(hashes.size(), tabla, hashSlicePrueba(scope, Map.of("public.reserva", tabla)));
            }
        } catch (SQLException excepcion) {
            throw new IllegalStateException("Unable to calculate reservation slice checksum", excepcion);
        }
    }

    public static Set<UUID> congelarTurnosPorScope(DataSource dataSource, LegacyTurnScope scope) {
        List<UUID> salones = scope.salonIdsNaturales();
        String markers = String.join(",", java.util.Collections.nCopies(salones.size(), "?"));
        String sql = "SELECT id FROM public.turno_instructor WHERE salon_id IN (" + markers
                + ") AND activo = ? AND ((tipo = ? AND dia_semana = ?) OR "
                + "((tipo = ? OR tipo = ?) AND fecha = ?)) ORDER BY id";
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            int position = 1;
            for (UUID salon : salones) statement.setObject(position++, salon);
            statement.setBoolean(position++, true);
            statement.setString(position++, "RECURRENTE");
            statement.setShort(position++, scope.dayOfWeekDomingoCero());
            statement.setString(position++, "EXCEPCION");
            statement.setString(position++, "CANCELACION");
            statement.setObject(position, scope.fecha());
            try (ResultSet rows = statement.executeQuery()) {
                java.util.LinkedHashSet<UUID> ids = new java.util.LinkedHashSet<>();
                while (rows.next()) ids.add(rows.getObject(1, UUID.class));
                return Set.copyOf(ids);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to freeze legacy Turno scope", exception);
        }
    }

    public static ResultadoR2 calcularTurnosCongelado(
            DataSource dataSource, LegacyTurnScope scope, Set<UUID> frozenTurnIds) {
        List<UUID> ids = frozenTurnIds.stream().sorted(LegacyTurnReadContext.UUID_UNSIGNED).toList();
        String markers = ids.isEmpty() ? "" : String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        Map<String, List<FilaCompuesta>> rowsByTable = new java.util.LinkedHashMap<>();
        try (var connection = dataSource.getConnection()) {
            rowsByTable.put("public.turno_instructor", readTurnRows(connection, ids, markers));
            rowsByTable.put("public.turno_instructor_asignacion",
                    readAssignmentRows(connection, ids, markers));
            rowsByTable.put("public.turno_instructor_usuario", readMemberRows(connection, ids, markers));
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to calculate legacy Turno slice checksum", exception);
        }
        Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        Map<String, String> tableHashes = new java.util.LinkedHashMap<>();
        rowsByTable.forEach((table, rows) -> {
            counts.put(table, rows.size());
            tableHashes.put(table, hashTableR2(table, rows));
        });
        byte[] checksumScope = checksumScopeR2(scope, ids);
        List<byte[]> sliceParts = new ArrayList<>();
        sliceParts.add(bytes("F2E-R2-CHECKSUM-SLICE-V1"));
        sliceParts.add(checksumScope);
        sliceParts.add(bytes("3"));
        tableHashes.entrySet().stream().sorted((left, right) -> compararBytes(
                bytes(left.getKey()), bytes(right.getKey()))).forEach(entry ->
                sliceParts.add(ReadSnapshotIdentifiers.secuenciaTextos(
                        "F2E-R2-CHECKSUM-SLICE-TABLE-ENTRY-V1", entry.getKey(), entry.getValue())));
        return new ResultadoR2(Map.copyOf(counts), Map.copyOf(tableHashes),
                hash(ReadSnapshotIdentifiers.secuencia(sliceParts)), Set.copyOf(frozenTurnIds));
    }

    private static List<FilaCompuesta> readTurnRows(
            java.sql.Connection connection, List<UUID> ids, String markers) throws SQLException {
        String sql = "SELECT id,salon_id,tipo,dia_semana,fecha,hora_inicio,hora_fin,activo,creado_en,actualizado_en "
                + "FROM public.turno_instructor WHERE "
                + (ids.isEmpty() ? "FALSE" : "id IN (" + markers + ")") + " ORDER BY id";
        try (var statement = connection.prepareStatement(sql)) {
            bindIds(statement, ids);
            try (ResultSet rows = statement.executeQuery()) {
                List<FilaCompuesta> result = new ArrayList<>();
                while (rows.next()) {
                    UUID id = rows.getObject("id", UUID.class);
                    List<Campo> fields = List.of(
                            Campo.valor("id", "U", id.toString()),
                            Campo.valor("salon_id", "U", rows.getObject("salon_id", UUID.class).toString()),
                            Campo.valor("tipo", "S", rows.getString("tipo")),
                            nullable("dia_semana", "I", rows.getObject("dia_semana"), Object::toString),
                            nullable("fecha", "D", rows.getObject("fecha", LocalDate.class), LocalDate::toString),
                            Campo.valor("hora_inicio", "T", ReadSnapshotIdentifiers.hora(rows.getObject("hora_inicio", LocalTime.class))),
                            Campo.valor("hora_fin", "T", ReadSnapshotIdentifiers.hora(rows.getObject("hora_fin", LocalTime.class))),
                            Campo.valor("activo", "B", Boolean.toString(rows.getBoolean("activo"))),
                            Campo.valor("creado_en", "Z", ReadSnapshotIdentifiers.instante(rows.getObject("creado_en", OffsetDateTime.class))),
                            Campo.valor("actualizado_en", "Z", ReadSnapshotIdentifiers.instante(rows.getObject("actualizado_en", OffsetDateTime.class))));
                    result.add(new FilaCompuesta(List.of(id), hashRowR2(fields)));
                }
                return result;
            }
        }
    }

    private static List<FilaCompuesta> readMemberRows(
            java.sql.Connection connection, List<UUID> ids, String markers) throws SQLException {
        String sql = "SELECT turno_id,usuario_id FROM public.turno_instructor_usuario WHERE "
                + (ids.isEmpty() ? "FALSE" : "turno_id IN (" + markers + ")")
                + " ORDER BY turno_id,usuario_id";
        try (var statement = connection.prepareStatement(sql)) {
            bindIds(statement, ids);
            try (ResultSet rows = statement.executeQuery()) {
                List<FilaCompuesta> result = new ArrayList<>();
                while (rows.next()) {
                    UUID turn = rows.getObject("turno_id", UUID.class);
                    UUID user = rows.getObject("usuario_id", UUID.class);
                    List<Campo> fields = List.of(Campo.valor("turno_id", "U", turn.toString()),
                            Campo.valor("usuario_id", "U", user.toString()));
                    result.add(new FilaCompuesta(List.of(turn, user), hashRowR2(fields)));
                }
                return result;
            }
        }
    }

    private static List<FilaCompuesta> readAssignmentRows(
            java.sql.Connection connection, List<UUID> ids, String markers) throws SQLException {
        String sql = "SELECT turno_id,usuario_id,tipo_actividad_id,hora_inicio,hora_fin "
                + "FROM public.turno_instructor_asignacion WHERE "
                + (ids.isEmpty() ? "FALSE" : "turno_id IN (" + markers + ")")
                + " ORDER BY turno_id,usuario_id,tipo_actividad_id";
        try (var statement = connection.prepareStatement(sql)) {
            bindIds(statement, ids);
            try (ResultSet rows = statement.executeQuery()) {
                List<FilaCompuesta> result = new ArrayList<>();
                while (rows.next()) {
                    UUID turn = rows.getObject("turno_id", UUID.class);
                    UUID user = rows.getObject("usuario_id", UUID.class);
                    UUID activity = rows.getObject("tipo_actividad_id", UUID.class);
                    List<Campo> fields = List.of(
                            Campo.valor("turno_id", "U", turn.toString()),
                            Campo.valor("usuario_id", "U", user.toString()),
                            Campo.valor("tipo_actividad_id", "U", activity.toString()),
                            nullable("hora_inicio", "T", rows.getObject("hora_inicio", LocalTime.class), ReadSnapshotIdentifiers::hora),
                            nullable("hora_fin", "T", rows.getObject("hora_fin", LocalTime.class), ReadSnapshotIdentifiers::hora));
                    result.add(new FilaCompuesta(List.of(turn, user, activity), hashRowR2(fields)));
                }
                return result;
            }
        }
    }

    private static <T> Campo nullable(String name, String type, T value, java.util.function.Function<T, String> encoder) {
        return value == null ? Campo.nulo(name, type) : Campo.valor(name, type, encoder.apply(value));
    }

    private static void bindIds(java.sql.PreparedStatement statement, List<UUID> ids) throws SQLException {
        for (int index = 0; index < ids.size(); index++) statement.setObject(index + 1, ids.get(index));
    }

    private static String hashRowR2(List<Campo> fields) {
        List<byte[]> parts = new ArrayList<>();
        parts.add(bytes("F2E-R2-CHECKSUM-ROW-V1"));
        parts.add(bytes(Integer.toString(fields.size())));
        fields.forEach(field -> parts.add(ReadSnapshotIdentifiers.secuenciaTextos(
                "F2E-R2-CHECKSUM-FIELD-V1", field.nombre(), field.tipo(), field.presencia(), field.valor())));
        return hash(ReadSnapshotIdentifiers.secuencia(parts));
    }

    private static String hashTableR2(String table, List<FilaCompuesta> rows) {
        List<FilaCompuesta> ordered = new ArrayList<>(rows);
        ordered.sort((left, right) -> compareComposite(left.primaryKey(), right.primaryKey()));
        List<byte[]> parts = new ArrayList<>();
        parts.add(bytes("F2E-R2-CHECKSUM-TABLE-V1"));
        parts.add(bytes(table));
        parts.add(bytes(Integer.toString(ordered.size())));
        ordered.forEach(row -> parts.add(bytes(row.hash())));
        return hash(ReadSnapshotIdentifiers.secuencia(parts));
    }

    private static int compareComposite(List<UUID> left, List<UUID> right) {
        for (int index = 0; index < Math.min(left.size(), right.size()); index++) {
            int comparison = compararUuid(left.get(index), right.get(index));
            if (comparison != 0) return comparison;
        }
        return Integer.compare(left.size(), right.size());
    }

    private static byte[] checksumScopeR2(LegacyTurnScope scope, List<UUID> ids) {
        List<byte[]> parts = new ArrayList<>();
        parts.add(bytes("F2E-R2-CHECKSUM-SCOPE-V1"));
        parts.add(bytes("READ_FOR_DATE"));
        parts.add(scope.bytesCanonicos());
        parts.add(bytes(Integer.toString(ids.size())));
        ids.forEach(id -> parts.add(bytes(id.toString())));
        return ReadSnapshotIdentifiers.secuencia(parts);
    }

    public static String hashFilaPrueba(List<Campo> campos) {
        return hash(preimagenFilaPrueba(campos));
    }

    public static byte[] preimagenFilaPrueba(List<Campo> campos) {
        List<byte[]> partes = new ArrayList<>();
        partes.add(bytes("F2E_CHECKSUM_ROW_V1"));
        partes.add(bytes(Integer.toString(campos.size())));
        campos.forEach(campo -> partes.add(campo.bytesCanonicos()));
        return ReadSnapshotIdentifiers.secuencia(partes);
    }

    private static List<FilaHash> hashesFilas(ResultSet filas) throws SQLException {
        List<FilaHash> hashes = new ArrayList<>();
        while (filas.next()) {
            List<Campo> campos = List.of(
                    Campo.valor("id", "U", filas.getObject("id", UUID.class).toString()),
                    Campo.valor("salon_id", "U", filas.getObject("salon_id", UUID.class).toString()),
                    Campo.valor("instructor_id", "U", filas.getObject("instructor_id", UUID.class).toString()),
                    Campo.valor("cliente_id", "U", filas.getObject("cliente_id", UUID.class).toString()),
                    Campo.valor("tipo_actividad_id", "U", filas.getObject("tipo_actividad_id", UUID.class).toString()),
                    Campo.valor("fecha", "D", filas.getObject("fecha", LocalDate.class).toString()),
                    Campo.valor("hora_inicio", "T", ReadSnapshotIdentifiers.hora(filas.getObject("hora_inicio", LocalTime.class))),
                    Campo.valor("hora_fin", "T", ReadSnapshotIdentifiers.hora(filas.getObject("hora_fin", LocalTime.class))),
                    Campo.valor("estado", "S", filas.getString("estado")),
                    Campo.valor("creado_en", "Z", ReadSnapshotIdentifiers.instante(filas.getObject("creado_en", OffsetDateTime.class))),
                    Campo.valor("actualizado_en", "Z", ReadSnapshotIdentifiers.instante(filas.getObject("actualizado_en", OffsetDateTime.class))));
            hashes.add(new FilaHash(filas.getObject("id", UUID.class), hashFilaPrueba(campos)));
        }
        return hashes;
    }

    public static String hashTablaPrueba(String identidadTabla, List<FilaHash> filas) {
        return hash(preimagenTablaPrueba(identidadTabla, filas));
    }

    public static byte[] preimagenTablaPrueba(String identidadTabla, List<FilaHash> filas) {
        List<FilaHash> ordenadas = new ArrayList<>(filas);
        ordenadas.sort(Comparator.comparing(FilaHash::identidad, F2eSliceChecksum::compararUuid));
        List<byte[]> partes = new ArrayList<>();
        partes.add(bytes("F2E_CHECKSUM_TABLE_V1"));
        partes.add(bytes(identidadTabla));
        partes.add(bytes(Integer.toString(ordenadas.size())));
        ordenadas.forEach(fila -> partes.add(bytes(fila.hash())));
        return ReadSnapshotIdentifiers.secuencia(partes);
    }

    public static String hashSlicePorIdentidades(List<UUID> identidades, String hashTabla) {
        return hashSlicePrueba(
                scopePorIdentidadesPrueba(identidades), Map.of("public.reserva", hashTabla));
    }

    public static byte[] scopePorIdentidadesPrueba(List<UUID> identidades) {
        List<byte[]> scopePartes = new ArrayList<>();
        scopePartes.add(bytes("F2E_CHECKSUM_SCOPE_V1"));
        scopePartes.add(bytes("BY_RESERVATION_IDS"));
        List<UUID> ordenadas = ReadSnapshotIdentifiers.ordenarUuid(identidades);
        scopePartes.add(bytes(Integer.toString(ordenadas.size())));
        ordenadas.forEach(id -> scopePartes.add(bytes(id.toString())));
        return ReadSnapshotIdentifiers.secuencia(scopePartes);
    }

    public static byte[] scopePorRangoPrueba(
            List<UUID> salones, LocalDate desde, LocalDate hasta) {
        List<byte[]> partes = new ArrayList<>();
        partes.add(bytes("F2E_CHECKSUM_SCOPE_V1"));
        partes.add(bytes("BY_SCOPE"));
        List<UUID> ordenados = ReadSnapshotIdentifiers.ordenarUuid(salones);
        partes.add(bytes(Integer.toString(ordenados.size())));
        ordenados.forEach(id -> partes.add(bytes(id.toString())));
        partes.add(bytes(desde.toString()));
        partes.add(bytes(hasta.toString()));
        return ReadSnapshotIdentifiers.secuencia(partes);
    }

    public static String hashSlicePrueba(byte[] scope, Map<String, String> tablas) {
        return hash(preimagenSlicePrueba(scope, tablas));
    }

    public static byte[] preimagenSlicePrueba(byte[] scope, Map<String, String> tablas) {
        List<Map.Entry<String, String>> ordenadas = new ArrayList<>(tablas.entrySet());
        ordenadas.sort((izquierda, derecha) -> compararBytes(
                bytes(izquierda.getKey()), bytes(derecha.getKey())));
        List<byte[]> partes = new ArrayList<>();
        partes.add(bytes("F2E_CHECKSUM_SLICE_V1"));
        partes.add(scope.clone());
        partes.add(bytes(Integer.toString(ordenadas.size())));
        ordenadas.forEach(tabla -> partes.add(ReadSnapshotIdentifiers.secuenciaTextos(
                "F2E_CHECKSUM_SLICE_TABLE_ENTRY_V1", tabla.getKey(), tabla.getValue())));
        return ReadSnapshotIdentifiers.secuencia(partes);
    }

    private static int compararUuid(UUID izquierda, UUID derecha) {
        return compararBytes(bytesUuid(izquierda), bytesUuid(derecha));
    }

    private static byte[] bytesUuid(UUID uuid) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    private static int compararBytes(byte[] izquierda, byte[] derecha) {
        return java.util.Arrays.compareUnsigned(izquierda, derecha);
    }

    private static String hash(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception excepcion) {
            throw new IllegalStateException(excepcion);
        }
    }

    private static byte[] bytes(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8);
    }

    /** Re-evaluates both predicates on every invocation; never freezes physical membership. */
    public static ResultadoR3 calcularNominalDinamico(DataSource observer, LocalDate fecha) {
        java.util.Objects.requireNonNull(fecha);
        String blockPredicate = "b.activo = true AND b.vigente_desde <= ? AND "
                + "(b.vigente_hasta IS NULL OR ? <= b.vigente_hasta) AND b.dia_semana = ?";
        String blockSql = "SELECT b.id,b.serie_id,b.salon_id,b.dia_semana,b.hora_inicio,b.hora_fin,"
                + "b.vigente_desde,b.vigente_hasta,b.activo,b.creado_en,b.actualizado_en "
                + "FROM public.programacion_bloque b WHERE " + blockPredicate + " ORDER BY b.id";
        String assignmentSql = "SELECT a.id,a.serie_id,a.bloque_id,a.instructor_id,a.tipo_actividad_id,"
                + "a.hora_inicio,a.hora_fin,a.vigente_desde,a.vigente_hasta,a.activo,a.creado_en,a.actualizado_en "
                + "FROM public.programacion_asignacion a JOIN public.programacion_bloque b ON b.id=a.bloque_id "
                + "WHERE " + blockPredicate + " AND a.activo = true AND a.vigente_desde <= ? AND "
                + "(a.vigente_hasta IS NULL OR ? <= a.vigente_hasta) ORDER BY a.id";
        Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        Map<String, String> hashes = new java.util.LinkedHashMap<>();
        try (var c = observer.getConnection()) {
            c.setAutoCommit(false); c.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
            c.setReadOnly(true);
            for (String table : List.of("public.programacion_bloque", "public.programacion_asignacion")) {
                boolean block = table.endsWith("bloque");
                try (var q = c.prepareStatement(block ? blockSql : assignmentSql)) {
                    q.setObject(1, fecha); q.setObject(2, fecha);
                    q.setShort(3, (short)(fecha.getDayOfWeek().getValue() % 7));
                    if (!block) { q.setObject(4, fecha); q.setObject(5, fecha); }
                    try (var rs = q.executeQuery()) {
                        List<FilaHash> rows = new ArrayList<>();
                        while (rs.next()) {
                            List<Campo> fields = new ArrayList<>();
                            var md = rs.getMetaData();
                            for (int i = 1; i <= md.getColumnCount(); i++) {
                                String name = md.getColumnLabel(i);
                                String type = md.getColumnTypeName(i);
                                Object value;
                                String encoded;
                                switch (type) {
                                    case "uuid" -> { value = rs.getObject(i, UUID.class); encoded = value == null ? "" : value.toString(); }
                                    case "date" -> { value = rs.getObject(i, LocalDate.class); encoded = value == null ? "" : value.toString(); }
                                    case "time" -> { var v = rs.getObject(i, LocalTime.class); value=v;
                                        encoded=v == null ? "" : ReadSnapshotIdentifiers.hora(v); }
                                    case "timestamptz" -> { var v = rs.getObject(i, OffsetDateTime.class); value=v;
                                        encoded=v == null ? "" : ReadSnapshotIdentifiers.instante(v); }
                                    case "bool" -> { value=rs.getObject(i, Boolean.class); encoded=value == null ? "" : value.toString(); }
                                    case "int2" -> { value=rs.getObject(i, Short.class); encoded=value == null ? "" : value.toString(); }
                                    default -> throw new IllegalStateException("Unexpected R3 checksum scalar type");
                                }
                                fields.add(value == null ? Campo.nulo(name,type) : Campo.valor(name,type,encoded));
                            }
                            rows.add(new FilaHash(rs.getObject("id", UUID.class), hashFilaPrueba(fields)));
                        }
                        counts.put(table, rows.size()); hashes.put(table, hashTablaPrueba(table, rows));
                    }
                }
            }
            c.commit();
        } catch (SQLException e) { throw new IllegalStateException("R3 dynamic checksum failed", e); }
        byte[] scope = ReadSnapshotIdentifiers.secuenciaTextos("F2E-R3-DYNAMIC-SLICE-V1", fecha.toString(),
                Short.toString((short)(fecha.getDayOfWeek().getValue() % 7)));
        return new ResultadoR3(counts, hashes, hashSlicePrueba(scope, hashes));
    }
    public record ResultadoR3(Map<String,Integer> rowCounts, Map<String,String> tableHashes, String sliceHash) {
        public ResultadoR3 { rowCounts=Map.copyOf(rowCounts); tableHashes=Map.copyOf(tableHashes); }
    }

    public record Resultado(int filas, String hashTabla, String hashSlice) { }

    public record ResultadoR2(
            Map<String, Integer> rowCounts,
            Map<String, String> tableHashes,
            String sliceHash,
            Set<UUID> frozenTurnIds) {
        public ResultadoR2 {
            rowCounts = Map.copyOf(rowCounts);
            tableHashes = Map.copyOf(tableHashes);
            frozenTurnIds = Set.copyOf(frozenTurnIds);
        }
    }

    private record FilaCompuesta(List<UUID> primaryKey, String hash) {
        private FilaCompuesta {
            primaryKey = List.copyOf(primaryKey);
        }
    }

    public record FilaHash(UUID identidad, String hash) {
        public FilaHash {
            java.util.Objects.requireNonNull(identidad, "identidad");
            java.util.Objects.requireNonNull(hash, "hash");
        }
    }

    public record Campo(String nombre, String tipo, String presencia, String valor) {
        public static Campo valor(String nombre, String tipo, String valor) {
            return new Campo(nombre, tipo, "V", valor);
        }

        public static Campo nulo(String nombre, String tipo) {
            return new Campo(nombre, tipo, "N", "");
        }

        byte[] bytesCanonicos() {
            return ReadSnapshotIdentifiers.secuenciaTextos(
                    "F2E_CHECKSUM_FIELD_V1", nombre, tipo, presencia, valor == null ? "" : valor);
        }
    }
}
