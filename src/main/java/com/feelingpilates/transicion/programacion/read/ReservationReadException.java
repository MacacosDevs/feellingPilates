package com.feelingpilates.transicion.programacion.read;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ReservationReadException extends RuntimeException {

    private static final Set<String> CLAVES_SEGURAS = Set.of(
            "operation", "projectionContractId", "scopeKind", "reservationIds", "salonIds",
            "fromDate", "toDate", "requestedCount", "returnedCount", "missingReservationIds",
            "unexpectedReservationIds", "duplicateReservationIds", "physicalRowOrdinal",
            "physicalColumn", "sqlCatalogStatementId", "sqlStateClass", "vendorErrorCode");
    private static final Set<String> OPERACIONES = Set.of("READ_BY_RESERVATION_IDS", "READ_BY_SCOPE");
    private static final Set<String> TIPOS_SCOPE = Set.of("BY_RESERVATION_IDS", "BY_SCOPE");
    private static final Set<String> LISTAS_UUID = Set.of(
            "reservationIds", "salonIds", "missingReservationIds",
            "unexpectedReservationIds", "duplicateReservationIds");
    private static final Set<String> CONTEOS = Set.of("requestedCount", "returnedCount");
    private static final Set<String> FECHAS = Set.of("fromDate", "toDate");
    private static final Pattern HUELLA = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern CLASE_SQL_STATE = Pattern.compile("[0-9A-Z]{1,2}");
    private static final int LONGITUD_UUID_CANONICO = 36;
    private static final int LONGITUD_UUID_CON_SEPARADOR = LONGITUD_UUID_CANONICO + 1;
    private static final int CARDINALIDAD_MAXIMA_LISTA_UUID =
            (int) (((long) Integer.MAX_VALUE + 1L) / LONGITUD_UUID_CON_SEPARADOR);
    private static final int LONGITUD_MAXIMA_LISTA_UUID =
            (int) longitudListaUuid(CARDINALIDAD_MAXIMA_LISTA_UUID);

    private final ReservationReadFailureCode failureCode;
    private final Map<String, String> safeContext;

    public ReservationReadException(
            ReservationReadFailureCode failureCode,
            Map<String, String> safeContext) {
        this(failureCode, safeContext, null);
    }

    public ReservationReadException(
            ReservationReadFailureCode failureCode,
            Map<String, String> safeContext,
            Throwable cause) {
        super(mensaje(failureCode), cause);
        this.failureCode = failureCode;
        if (safeContext == null || safeContext.entrySet().stream().anyMatch(entry ->
                !valorSeguro(entry.getKey(), entry.getValue()))) {
            throw new IllegalArgumentException("Reservation read safe context is invalid");
        }
        this.safeContext = Map.copyOf(safeContext);
    }

    public ReservationReadFailureCode failureCode() {
        return failureCode;
    }

    public Map<String, String> safeContext() {
        return safeContext;
    }

    private static boolean valorSeguro(String clave, String valor) {
        if (!CLAVES_SEGURAS.contains(clave) || valor == null || valor.isEmpty()
                || contieneControl(valor)) {
            return false;
        }
        if ("operation".equals(clave)) return OPERACIONES.contains(valor);
        if ("projectionContractId".equals(clave)) return "R1_RESERVA_PROJECTION".equals(valor);
        if ("scopeKind".equals(clave)) return TIPOS_SCOPE.contains(valor);
        if (LISTAS_UUID.contains(clave)) return listaUuidCanonica(valor);
        if (FECHAS.contains(clave)) return fechaCanonica(valor);
        if (CONTEOS.contains(clave)) return enteroCanonicoEnDominio(valor, 0, Integer.MAX_VALUE);
        if ("physicalRowOrdinal".equals(clave)) {
            return enteroCanonicoEnDominio(valor, 1, Integer.MAX_VALUE);
        }
        if ("physicalColumn".equals(clave)) {
            return ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1
                    .projectedPhysicalColumns().contains(valor);
        }
        if ("sqlCatalogStatementId".equals(clave)) return HUELLA.matcher(valor).matches();
        if ("sqlStateClass".equals(clave)) return CLASE_SQL_STATE.matcher(valor).matches();
        if ("vendorErrorCode".equals(clave)) {
            return enteroCanonicoEnDominio(valor, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return false;
    }

    private static boolean listaUuidCanonica(String valor) {
        if (valor.length() > LONGITUD_MAXIMA_LISTA_UUID
                || (valor.length() + 1L) % LONGITUD_UUID_CON_SEPARADOR != 0L) {
            return false;
        }
        int cardinalidad = (int) ((valor.length() + 1L) / LONGITUD_UUID_CON_SEPARADOR);
        if (cardinalidad < 1 || cardinalidad > CARDINALIDAD_MAXIMA_LISTA_UUID) {
            return false;
        }
        try {
            UUID anterior = null;
            for (int indice = 0; indice < cardinalidad; indice++) {
                int inicio = indice * LONGITUD_UUID_CON_SEPARADOR;
                int fin = inicio + LONGITUD_UUID_CANONICO;
                if (fin > valor.length()
                        || (indice + 1 < cardinalidad && valor.charAt(fin) != ',')) {
                    return false;
                }
                String parte = valor.substring(inicio, fin);
                UUID actual = UUID.fromString(parte);
                if (!actual.toString().equals(parte)
                        || anterior != null && compararUuidUnsigned(anterior, actual) >= 0) {
                    return false;
                }
                anterior = actual;
            }
            return true;
        } catch (IllegalArgumentException excepcion) {
            return false;
        }
    }

    private static boolean enteroCanonicoEnDominio(String valor, int minimo, int maximo) {
        try {
            int numero = Integer.parseInt(valor);
            return numero >= minimo && numero <= maximo && Integer.toString(numero).equals(valor);
        } catch (NumberFormatException excepcion) {
            return false;
        }
    }

    private static int compararUuidUnsigned(UUID izquierda, UUID derecha) {
        int altos = Long.compareUnsigned(izquierda.getMostSignificantBits(), derecha.getMostSignificantBits());
        return altos != 0 ? altos
                : Long.compareUnsigned(izquierda.getLeastSignificantBits(), derecha.getLeastSignificantBits());
    }

    private static long longitudListaUuid(long cardinalidad) {
        if (cardinalidad < 1) return 0L;
        return cardinalidad * LONGITUD_UUID_CON_SEPARADOR - 1L;
    }

    private static boolean fechaCanonica(String valor) {
        try {
            return LocalDate.parse(valor).toString().equals(valor);
        } catch (DateTimeParseException excepcion) {
            return false;
        }
    }

    private static boolean contieneControl(String valor) {
        return valor.chars().anyMatch(caracter -> caracter < 0x20 || caracter == 0x7f);
    }

    private static String mensaje(ReservationReadFailureCode codigo) {
        if (codigo == null) {
            throw new IllegalArgumentException("failureCode is required");
        }
        return switch (codigo) {
            case ADAPTER_INPUT_INVALID -> "Reservation read input or projected row is invalid";
            case SOURCE_RECORD_NOT_FOUND -> "One or more requested reservation records were not found";
            case READ_SET_INVARIANT_VIOLATION -> "Reservation read-set invariant was violated";
            case SOURCE_ACCESS_FAILURE -> "Reservation source access failed";
        };
    }
}
