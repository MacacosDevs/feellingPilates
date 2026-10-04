package com.feelingpilates.transicion.programacion.read;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionRow;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ReadSnapshotIdentifiers {

    private static final DateTimeFormatter HORA_MICROS = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true)
            .toFormatter(java.util.Locale.ROOT);
    private static final DateTimeFormatter INSTANTE_MICROS = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true)
            .appendLiteral('Z')
            .toFormatter(java.util.Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private ReadSnapshotIdentifiers() {
    }

    public static SesionCalculo nuevaSesionCalculo() {
        return new SesionCalculo();
    }

    public static List<UUID> ordenarUuid(Collection<UUID> valores) {
        if (valores == null || valores.stream().anyMatch(valor -> valor == null)) {
            throw new IllegalArgumentException("UUID collection is invalid");
        }
        List<UUID> ordenados = new ArrayList<>(valores);
        ordenados.sort(ReadSnapshotIdentifiers::compararUuidSinSigno);
        return List.copyOf(ordenados);
    }

    public static byte[] alcancePorIdentidades(Set<UUID> identidades) {
        if (identidades == null || identidades.isEmpty() || identidades.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("reservationIds must be non-empty and contain no nulls");
        }
        List<byte[]> partes = new ArrayList<>();
        partes.add(utf8("F2E-R1-READ-SCOPE-V2"));
        partes.add(utf8("READ_BY_RESERVATION_IDS"));
        List<UUID> ordenadas = ordenarUuid(identidades);
        partes.add(utf8(Integer.toString(ordenadas.size())));
        ordenadas.forEach(id -> partes.add(utf8(id.toString())));
        return secuencia(partes);
    }

    public static byte[] alcancePorScope(ReservationScope scope) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is required");
        }
        List<byte[]> partes = new ArrayList<>();
        partes.add(utf8("F2E-R1-READ-SCOPE-V2"));
        partes.add(utf8("READ_BY_SCOPE"));
        List<UUID> ordenadas = ordenarUuid(scope.salonIds());
        partes.add(utf8(Integer.toString(ordenadas.size())));
        ordenadas.forEach(id -> partes.add(utf8(id.toString())));
        partes.add(utf8(fecha(scope.desde())));
        partes.add(utf8(fecha(scope.hasta())));
        return secuencia(partes);
    }

    public static byte[] claveUnicidad(String dominio, String... componentes) {
        List<byte[]> partes = new ArrayList<>();
        partes.add(utf8(textoRequerido(dominio, "dominio")));
        for (String componente : componentes) {
            partes.add(utf8(textoRequerido(componente, "componente")));
        }
        return secuencia(partes);
    }

    public static String decodificarUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException excepcion) {
            throw new IllegalArgumentException("Invalid UTF-8", excepcion);
        }
    }

    public static byte[] secuenciaTextos(String... valores) {
        return secuencia(Arrays.stream(valores).map(ReadSnapshotIdentifiers::utf8).toList());
    }

    public static byte[] secuencia(List<byte[]> valores) {
        if (valores == null || valores.stream().anyMatch(valor -> valor == null)) {
            throw new IllegalArgumentException("sequence values are required");
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.writeBytes(asciiDecimal(valores.size()));
        salida.write(':');
        for (byte[] valor : valores) {
            salida.writeBytes(asciiDecimal(valor.length));
            salida.write(':');
            salida.writeBytes(valor);
        }
        return salida.toByteArray();
    }

    public static byte[] mapaCanonico(Map<String, String> valores) {
        if (valores == null || valores.entrySet().stream().anyMatch(e ->
                e.getKey() == null || e.getValue() == null)) {
            throw new IllegalArgumentException("canonical map is invalid");
        }
        List<Map.Entry<byte[], byte[]>> entradas = valores.entrySet().stream()
                .map(e -> Map.entry(utf8(textoRequerido(e.getKey(), "key")), utf8(e.getValue())))
                .sorted((a, b) -> compararBytesSinSigno(a.getKey(), b.getKey()))
                .toList();
        for (int indice = 1; indice < entradas.size(); indice++) {
            if (Arrays.equals(entradas.get(indice - 1).getKey(), entradas.get(indice).getKey())) {
                throw new IllegalArgumentException("encoded duplicate canonical map key");
            }
        }
        List<byte[]> partes = new ArrayList<>();
        partes.add(utf8("F2E-R1-NORMALIZED-FIELDS-V2"));
        partes.add(asciiDecimal(entradas.size()));
        entradas.forEach(e -> partes.add(secuencia(List.of(
                utf8("F2E-R1-NORMALIZED-FIELD-V2"), e.getKey(), e.getValue()))));
        return secuencia(partes);
    }

    public static String hora(LocalTime valor) {
        if (valor == null || valor.getNano() % 1_000 != 0) {
            throw new IllegalArgumentException("LocalTime must have microsecond precision");
        }
        return HORA_MICROS.format(valor);
    }

    public static String instante(OffsetDateTime valor) {
        if (valor == null || valor.getNano() % 1_000 != 0) {
            throw new IllegalArgumentException("OffsetDateTime must have microsecond precision");
        }
        return INSTANTE_MICROS.format(valor.toInstant());
    }

    public static String fecha(LocalDate valor) {
        if (valor == null) {
            throw new IllegalArgumentException("LocalDate is required");
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(valor);
    }

    private static int compararUuidSinSigno(UUID izquierda, UUID derecha) {
        int mayores = Long.compareUnsigned(izquierda.getMostSignificantBits(), derecha.getMostSignificantBits());
        return mayores != 0 ? mayores
                : Long.compareUnsigned(izquierda.getLeastSignificantBits(), derecha.getLeastSignificantBits());
    }

    private static int compararBytesSinSigno(byte[] izquierda, byte[] derecha) {
        int limite = Math.min(izquierda.length, derecha.length);
        for (int indice = 0; indice < limite; indice++) {
            int comparacion = Integer.compare(Byte.toUnsignedInt(izquierda[indice]), Byte.toUnsignedInt(derecha[indice]));
            if (comparacion != 0) {
                return comparacion;
            }
        }
        return Integer.compare(izquierda.length, derecha.length);
    }

    private static byte[] asciiDecimal(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("unsigned decimal cannot be negative");
        }
        return Integer.toString(valor).getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] utf8(String valor) {
        if (valor == null || valor.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("text cannot be null or contain NUL");
        }
        try {
            ByteBuffer bytes = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(valor));
            byte[] salida = new byte[bytes.remaining()];
            bytes.get(salida);
            return salida;
        } catch (CharacterCodingException excepcion) {
            throw new IllegalArgumentException("text is not valid UTF-8", excepcion);
        }
    }

    private static String textoRequerido(String valor, String nombre) {
        if (valor == null || valor.isBlank() || valor.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(nombre + " is required");
        }
        return valor;
    }

    private static String sha256(byte[] preimagen) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(preimagen));
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("SHA-256 unavailable", excepcion);
        }
    }

    public static final class SesionCalculo {
        private final Map<String, byte[]> preimagenes = new HashMap<>();

        private SesionCalculo() {
        }

        public String calcularEvidenciaSnapshot(String identidadFuenteDatos, String transactionBoundaryIdentity) {
            return calcular(secuenciaTextos(
                    "F2E-R1-SINGLE-READER-TEST-EVIDENCE-V2",
                    textoRequerido(identidadFuenteDatos, "identidadFuenteDatos"),
                    textoRequerido(transactionBoundaryIdentity, "transactionBoundaryIdentity"),
                    "f2eReaderTransactionManager", "f2eReaderPersistenceUnit",
                    "read committed", "read only"));
        }

        public String calcularObservacionStatements(
                String snapshotEvidenceId,
                String aislamiento,
                String modoAcceso,
                List<String> identificadoresCatalogo) {
            if (identificadoresCatalogo == null || identificadoresCatalogo.isEmpty()) {
                throw new IllegalArgumentException("statement catalog identifiers are required");
            }
            List<byte[]> partes = new ArrayList<>();
            partes.add(utf8("F2E-R1-STATEMENT-OBSERVATIONS-V2"));
            partes.add(utf8(textoRequerido(snapshotEvidenceId, "snapshotEvidenceId")));
            partes.add(utf8(textoRequerido(aislamiento, "aislamiento")));
            partes.add(utf8(textoRequerido(modoAcceso, "modoAcceso")));
            partes.add(asciiDecimal(identificadoresCatalogo.size()));
            identificadoresCatalogo.forEach(id -> partes.add(utf8(textoRequerido(id, "catalogStatementId"))));
            return calcular(secuencia(partes));
        }

        public String calcularProvenanceEjecucion(
                ReadSnapshotContext contexto, String operacion, byte[] alcance) {
            return calcular(secuencia(List.of(
                    utf8("F2E-R1-EXECUTION-PROVENANCE-V2"),
                    utf8(contexto.runIdentity()), utf8(contexto.attemptIdentity()),
                    utf8(contexto.readerInvocationIdentity()), utf8(textoRequerido(operacion, "operacion")),
                    utf8(contexto.sourceName()), utf8(contexto.schemaFingerprint()),
                    utf8(contexto.projectionCatalogVersion().canonicalCatalogValue()),
                    utf8(contexto.ruleCatalogVersion()), utf8(contexto.businessZone().getId()),
                    alcance.clone(), utf8(contexto.snapshotClaim().name()),
                    utf8(contexto.snapshotEvidenceId()), utf8(contexto.statementObservationFingerprint()))));
        }

        public String calcularSnapshotLogico(ReadSnapshotContext contexto, byte[] alcance) {
            return calcular(secuencia(List.of(
                    utf8("F2E-R1-LOGICAL-SNAPSHOT-V2"), utf8(contexto.sourceName()),
                    utf8(contexto.schemaFingerprint()),
                    utf8(contexto.projectionCatalogVersion().canonicalCatalogValue()),
                    utf8(contexto.ruleCatalogVersion()), utf8(contexto.businessZone().getId()),
                    alcance.clone(), utf8(contexto.snapshotClaim().name()),
                    utf8(contexto.snapshotEvidenceId()))));
        }

        public String calcularHuellaFuente(
                ReadSnapshotContext contexto, ReservaProjectionRow fila, byte[] proyeccionCanonica) {
            ReadSnapshotContext.ProjectionCatalogVersion catalogo = contexto.projectionCatalogVersion();
            return calcular(secuencia(List.of(
                    utf8("F2E-R1-SOURCE-FINGERPRINT-V2"), utf8(contexto.sourceName()),
                    utf8(contexto.schemaFingerprint()),
                    utf8(catalogo.canonicalCatalogValue()),
                    utf8(catalogo.sourceSystem()), utf8(catalogo.sourceAtomType()),
                    utf8(fila.reservationId().toString()),
                    proyeccionCanonica.clone())));
        }

        public String calcularIdentidadSnapshot(
                ReadSnapshotContext contexto,
                UUID reservationId,
                String logicalSnapshotId,
                String executionProvenanceId,
                String sourceFingerprint) {
            ReadSnapshotContext.ProjectionCatalogVersion catalogo = contexto.projectionCatalogVersion();
            return calcular(secuenciaTextos(
                    "F2E-R1-SNAPSHOT-IDENTITY-V2", logicalSnapshotId, executionProvenanceId,
                    catalogo.canonicalCatalogValue(), catalogo.sourceSystem(), catalogo.sourceAtomType(),
                    reservationId.toString(), sourceFingerprint));
        }

        public byte[] proyeccionCanonica(ReservaProjectionRow fila) {
            ReadSnapshotContext.ProjectionCatalogVersion catalogo =
                    ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1;
            List<byte[]> campos = List.of(
                    campoCatalogo(catalogo, 1, "VALUE", utf8(fila.reservationId().toString())),
                    campoCatalogo(catalogo, 2, "VALUE", utf8(fila.state())),
                    campoCatalogo(catalogo, 3, "VALUE", utf8(fecha(fila.date()))),
                    campoCatalogo(catalogo, 4, "VALUE", utf8(fila.salonId().toString())),
                    campoCatalogo(catalogo, 5, "VALUE", utf8(fila.instructorId().toString())),
                    campoCatalogo(catalogo, 6, "VALUE", utf8(fila.activityId().toString())),
                    campoCatalogo(catalogo, 7, "VALUE", utf8(hora(fila.start()))),
                    campoCatalogo(catalogo, 8, "VALUE", utf8(hora(fila.end()))),
                    campoCatalogo(catalogo, 9, "VALUE", utf8(instante(fila.createdAtTechnical()))),
                    campoCatalogo(catalogo, 10, "VALUE", utf8(instante(fila.updatedAtTechnical()))),
                    campoCatalogo(catalogo, 11, "ABSENT", new byte[0]));
            List<byte[]> partes = new ArrayList<>();
            partes.add(utf8("F2E-R1-CANONICAL-PROJECTION-V2"));
            partes.add(utf8(catalogo.projectionContractId()));
            partes.add(utf8(catalogo.projectionContractVersion()));
            partes.add(utf8(Integer.toString(catalogo.logicalFields().size())));
            partes.addAll(campos);
            return secuencia(partes);
        }

        private byte[] campoCatalogo(
                ReadSnapshotContext.ProjectionCatalogVersion catalogo,
                int posicion,
                String presencia,
                byte[] valor) {
            int indice = posicion - 1;
            return campoFuente(posicion, catalogo.logicalFields().get(indice),
                    catalogo.physicalFields().get(indice), catalogo.typeTags().get(indice),
                    presencia, valor);
        }

        private byte[] campoFuente(
                int posicion, String logico, String fisico, String tipo, String presencia, byte[] valor) {
            return secuencia(List.of(
                    utf8("F2E-R1-SOURCE-FIELD-V2"), asciiDecimal(posicion), utf8(logico), utf8(fisico),
                    utf8(tipo), utf8(presencia), valor));
        }

        private String calcular(byte[] preimagen) {
            String huella = sha256(preimagen);
            byte[] anterior = preimagenes.putIfAbsent(huella, preimagen.clone());
            if (anterior != null && !MessageDigest.isEqual(anterior, preimagen)) {
                throw new IllegalStateException("F2E identity hash collision detected");
            }
            return huella;
        }
    }
}
