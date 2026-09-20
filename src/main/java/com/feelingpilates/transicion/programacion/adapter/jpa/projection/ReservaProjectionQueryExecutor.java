package com.feelingpilates.transicion.programacion.adapter.jpa.projection;

import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReservationReadException;
import com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode;
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
import java.util.Objects;
import java.util.UUID;

public final class ReservaProjectionQueryExecutor {

    private final EntityManager entityManager;
    private final ReadSnapshotContext.ProjectionCatalogVersion catalogo;
    private final String sqlPorIdentidades;
    private final String sqlPorScope;

    public ReservaProjectionQueryExecutor(EntityManager entityManager) {
        this(entityManager, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1);
    }

    public ReservaProjectionQueryExecutor(
            EntityManager entityManager,
            ReadSnapshotContext.ProjectionCatalogVersion catalogo) {
        this(entityManager, catalogo, catalogo.sqlByIds(), catalogo.sqlByScope(),
                catalogo.statementIdByIds(), catalogo.statementIdByScope());
    }

    private ReservaProjectionQueryExecutor(
            EntityManager entityManager,
            ReadSnapshotContext.ProjectionCatalogVersion catalogo,
            String sqlPorIdentidades,
            String sqlPorScope,
            String idPorIdentidades,
            String idPorScope) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        catalogo.validarVinculoSql(
                sqlPorIdentidades, sqlPorScope, idPorIdentidades, idPorScope);
        this.sqlPorIdentidades = sqlPorIdentidades;
        this.sqlPorScope = sqlPorScope;
    }

    public ReadSnapshotContext.ProjectionCatalogVersion catalogo() {
        return catalogo;
    }

    public List<ReservaProjectionRow> consultarPorIdentidades(List<UUID> identidades) {
        if (identidades == null || identidades.isEmpty()) {
            throw new IllegalArgumentException("reservationIds must not be empty");
        }
        NativeQuery<?> consulta = entityManager.createNativeQuery(sqlPorIdentidades).unwrap(NativeQuery.class);
        consulta.setParameterList("reservationIds", identidades, UUID.class);
        return materializar(consulta.getResultList(), "READ_BY_RESERVATION_IDS");
    }

    public List<ReservaProjectionRow> consultarPorScope(
            List<UUID> salonIds, LocalDate desde, LocalDate hasta) {
        if (salonIds == null || salonIds.isEmpty() || desde == null || hasta == null) {
            throw new IllegalArgumentException("bounded scope is required");
        }
        NativeQuery<?> consulta = entityManager.createNativeQuery(sqlPorScope).unwrap(NativeQuery.class);
        consulta.setParameterList("salonIds", salonIds, UUID.class);
        consulta.setParameter("desde", desde, LocalDate.class);
        consulta.setParameter("hasta", hasta, LocalDate.class);
        return materializar(consulta.getResultList(), "READ_BY_SCOPE");
    }

    private List<ReservaProjectionRow> materializar(List<?> resultados, String operacion) {
        List<ReservaProjectionRow> filas = new ArrayList<>(resultados.size());
        for (int indice = 0; indice < resultados.size(); indice++) {
            Object resultado = resultados.get(indice);
            if (!(resultado instanceof Object[] valores) || valores.length != 10) {
                throw filaInvalida(operacion, indice + 1, null,
                        new IllegalArgumentException("Unexpected R1 reservation projection shape"));
            }
            filas.add(new ReservaProjectionRow(
                    convertir(() -> uuid(valores[0]), operacion, indice + 1, "id"),
                    convertir(() -> texto(valores[1]), operacion, indice + 1, "estado"),
                    convertir(() -> fecha(valores[2]), operacion, indice + 1, "fecha"),
                    convertir(() -> uuid(valores[3]), operacion, indice + 1, "salon_id"),
                    convertir(() -> uuid(valores[4]), operacion, indice + 1, "instructor_id"),
                    convertir(() -> uuid(valores[5]), operacion, indice + 1, "tipo_actividad_id"),
                    convertir(() -> hora(valores[6]), operacion, indice + 1, "hora_inicio"),
                    convertir(() -> hora(valores[7]), operacion, indice + 1, "hora_fin"),
                    convertir(() -> instante(valores[8]), operacion, indice + 1, "creado_en"),
                    convertir(() -> instante(valores[9]), operacion, indice + 1, "actualizado_en")));
        }
        return List.copyOf(filas);
    }

    private <T> T convertir(Conversion<T> conversion, String operacion, int ordinal, String columna) {
        try {
            return conversion.convertir();
        } catch (RuntimeException excepcion) {
            throw filaInvalida(operacion, ordinal, columna, excepcion);
        }
    }

    private ReservationReadException filaInvalida(
            String operacion, int ordinal, String columna, RuntimeException causa) {
        java.util.Map<String, String> contexto = new java.util.LinkedHashMap<>();
        contexto.put("operation", operacion);
        contexto.put("physicalRowOrdinal", Integer.toString(ordinal));
        if (columna != null) contexto.put("physicalColumn", columna);
        return new ReservationReadException(
                ReservationReadFailureCode.ADAPTER_INPUT_INVALID, contexto, causa);
    }

    @FunctionalInterface
    private interface Conversion<T> {
        T convertir();
    }

    private UUID uuid(Object valor) {
        if (valor == null || valor instanceof UUID) {
            return (UUID) valor;
        }
        return UUID.fromString(valor.toString());
    }

    private String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private LocalDate fecha(Object valor) {
        if (valor == null || valor instanceof LocalDate) {
            return (LocalDate) valor;
        }
        if (valor instanceof Date fechaSql) {
            return fechaSql.toLocalDate();
        }
        return LocalDate.parse(valor.toString());
    }

    private LocalTime hora(Object valor) {
        if (valor == null || valor instanceof LocalTime) {
            return (LocalTime) valor;
        }
        if (valor instanceof Time horaSql) {
            return horaSql.toLocalTime();
        }
        return LocalTime.parse(valor.toString());
    }

    private OffsetDateTime instante(Object valor) {
        if (valor == null || valor instanceof OffsetDateTime) {
            return (OffsetDateTime) valor;
        }
        if (valor instanceof Instant instant) {
            return instant.atOffset(ZoneOffset.UTC);
        }
        if (valor instanceof Timestamp timestamp) {
            return timestamp.toInstant().atOffset(ZoneOffset.UTC);
        }
        if (valor instanceof LocalDateTime fechaHora) {
            return fechaHora.atOffset(ZoneOffset.UTC);
        }
        return OffsetDateTime.parse(valor.toString());
    }
}
