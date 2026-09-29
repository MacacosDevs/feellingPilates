package com.feelingpilates.transicion.programacion.read;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record LegacyTurnReadContext(
        String runIdentity,
        String attemptIdentity,
        String sourceName,
        String schemaFingerprint,
        ProjectionCatalogVersion projectionCatalogVersion,
        String ruleCatalogVersion,
        ZoneId businessZone,
        String scopeCanonical,
        SnapshotClaim snapshotClaim,
        String snapshotEvidenceId) {

    private static final Pattern SHA256_PREFIX = Pattern.compile("sha256:[0-9a-f]{64}");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    public static final Comparator<UUID> UUID_UNSIGNED = (left, right) ->
            Arrays.compareUnsigned(uuid16(left), uuid16(right));
    public static final Comparator<String> UTF8_UNSIGNED = (left, right) ->
            Arrays.compareUnsigned(utf8(left), utf8(right));

    public LegacyTurnReadContext {
        runIdentity = required(runIdentity, "runIdentity");
        attemptIdentity = required(attemptIdentity, "attemptIdentity");
        sourceName = required(sourceName, "sourceName");
        schemaFingerprint = required(schemaFingerprint, "schemaFingerprint");
        projectionCatalogVersion = Objects.requireNonNull(projectionCatalogVersion, "projectionCatalogVersion");
        ruleCatalogVersion = required(ruleCatalogVersion, "ruleCatalogVersion");
        businessZone = Objects.requireNonNull(businessZone, "businessZone");
        scopeCanonical = required(scopeCanonical, "scopeCanonical");
        snapshotClaim = Objects.requireNonNull(snapshotClaim, "snapshotClaim");
        snapshotEvidenceId = required(snapshotEvidenceId, "snapshotEvidenceId");
        if (!SHA256_PREFIX.matcher(schemaFingerprint).matches()
                || !SHA256.matcher(snapshotEvidenceId).matches()
                || projectionCatalogVersion != ProjectionCatalogVersion.R2_LEGACY_TURN_V1
                || snapshotClaim != SnapshotClaim.R2_INTERNAL_RR_TEST) {
            throw new IllegalArgumentException("Invalid R2 legacy read context");
        }
        utf8(runIdentity);
        utf8(attemptIdentity);
        utf8(sourceName);
        utf8(ruleCatalogVersion);
        utf8(scopeCanonical);
    }

    public String executionProvenanceId() {
        return hashSecuencia("F2E-EXECUTION-V1", runIdentity, attemptIdentity, sourceName,
                schemaFingerprint, projectionCatalogVersion.name(), ruleCatalogVersion,
                businessZone.getId(), scopeCanonical);
    }

    public String logicalSnapshotId() {
        return hashSecuencia("F2E-LOGICAL-SNAPSHOT-V1", executionProvenanceId(),
                snapshotClaim.name(), snapshotEvidenceId);
    }

    public static String hashSecuencia(String... valores) {
        List<byte[]> partes = Arrays.stream(valores).map(LegacyTurnReadContext::utf8).toList();
        return sha256(secuencia(partes));
    }

    public static byte[] mapa(Map<String, String> campos) {
        Objects.requireNonNull(campos, "campos");
        List<Map.Entry<String, String>> ordenadas = new ArrayList<>(campos.entrySet());
        if (ordenadas.stream().anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
                || entry.getValue() == null)
                || new java.util.HashSet<>(campos.keySet()).size() != campos.size()) {
            throw new IllegalArgumentException("Invalid R2 normalized fields");
        }
        ordenadas.sort((left, right) -> UTF8_UNSIGNED.compare(left.getKey(), right.getKey()));
        List<byte[]> partes = new ArrayList<>();
        partes.add(utf8("F2E-R2-NORMALIZED-FIELDS-V1"));
        partes.add(ascii(Integer.toString(ordenadas.size())));
        for (Map.Entry<String, String> entry : ordenadas) {
            partes.add(secuenciaTextos("F2E-R2-NORMALIZED-FIELD-V1", entry.getKey(), entry.getValue()));
        }
        return secuencia(partes);
    }

    public static String markers(Iterable<String> markers) {
        java.util.TreeSet<String> ordenados = new java.util.TreeSet<>(UTF8_UNSIGNED);
        for (String marker : markers) {
            if (marker == null || marker.isBlank() || !ordenados.add(marker)) {
                throw new IllegalArgumentException("Invalid R2 marker set");
            }
        }
        List<byte[]> partes = new ArrayList<>();
        partes.add(utf8("F2E-R2-MARKERS-V1"));
        partes.add(ascii(Integer.toString(ordenados.size())));
        ordenados.forEach(marker -> partes.add(utf8(marker)));
        return utf8Estricto(secuencia(partes));
    }

    public static byte[] secuenciaTextos(String... valores) {
        return secuencia(Arrays.stream(valores).map(LegacyTurnReadContext::utf8).toList());
    }

    public static byte[] secuencia(List<byte[]> valores) {
        Objects.requireNonNull(valores, "valores");
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        byte[] cantidad = ascii(Integer.toString(valores.size()) + ":");
        salida.writeBytes(cantidad);
        for (byte[] valor : valores) {
            Objects.requireNonNull(valor, "sequence value");
            salida.writeBytes(ascii(Integer.toString(valor.length) + ":"));
            salida.writeBytes(valor);
        }
        return salida.toByteArray();
    }

    public static byte[] utf8(String valor) {
        if (valor == null || valor.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Invalid UTF-8 scalar");
        }
        try {
            java.nio.ByteBuffer encoded = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap(valor));
            byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            return bytes;
        } catch (CharacterCodingException excepcion) {
            throw new IllegalArgumentException("Invalid UTF-8 scalar", excepcion);
        }
    }

    public static String utf8Estricto(byte[] valor) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(valor.clone())).toString();
        } catch (CharacterCodingException excepcion) {
            throw new IllegalArgumentException("Invalid UTF-8 bytes", excepcion);
        }
    }

    public static byte[] ascii(String valor) {
        if (valor == null || !StandardCharsets.US_ASCII.newEncoder().canEncode(valor)) {
            throw new IllegalArgumentException("Invalid ASCII scalar");
        }
        return valor.getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] uuid16(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits()).array();
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception excepcion) {
            throw new IllegalStateException("SHA-256 unavailable", excepcion);
        }
    }

    public static Map<String, String> copyFields(Map<String, String> fields, int expectedSize) {
        Map<String, String> copy = new LinkedHashMap<>(Objects.requireNonNull(fields, "fields"));
        if (copy.size() != expectedSize || copy.entrySet().stream().anyMatch(entry ->
                entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null)) {
            throw new IllegalArgumentException("Unexpected R2 normalized field set");
        }
        return Map.copyOf(copy);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    public enum ProjectionCatalogVersion { R2_LEGACY_TURN_V1 }

    public enum SnapshotClaim { R2_INTERNAL_RR_TEST }
}
