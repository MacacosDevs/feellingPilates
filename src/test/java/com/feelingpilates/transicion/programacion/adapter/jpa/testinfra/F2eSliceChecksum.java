package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import com.feelingpilates.transicion.programacion.read.ReservationScope;

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

    public record Resultado(int filas, String hashTabla, String hashSlice) { }

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
