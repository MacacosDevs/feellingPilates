package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.ReservaProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.detector.EvidenceProvenance;
import com.feelingpilates.transicion.programacion.detector.ReservationSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import com.feelingpilates.transicion.programacion.read.ReservationReadException;
import com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode;
import com.feelingpilates.transicion.programacion.read.ReservationScope;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaProjectionMapperTest {

    private static final UUID RESERVA = UUID.fromString("00000000-0000-4000-8000-000000000001");

    @Test
    void reproduceLosSeisVectoresV2YLaProyeccionCanonicaDe1210Bytes() {
        ReadSnapshotIdentifiers.SesionCalculo calculos = ReadSnapshotIdentifiers.nuevaSesionCalculo();
        String evidencia = calculos.calcularEvidenciaSnapshot("fixture-r1-a", "tx-boundary-0001");
        assertEquals("f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a8952877", evidencia);
        String observacion = calculos.calcularObservacionStatements(evidencia, "read committed", "read only", List.of(
                "4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e",
                "9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7",
                "dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b"));
        assertEquals("7f7e27a90a3efff6803bf4a8e40dbe4377d8bf1a754187cb14502f47af135838", observacion);
        ReadSnapshotContext contexto = contexto(evidencia, observacion);
        byte[] alcance = ReadSnapshotIdentifiers.alcancePorIdentidades(Set.of(RESERVA));
        String ejecucion = calculos.calcularProvenanceEjecucion(contexto, "READ_BY_RESERVATION_IDS", alcance);
        String logico = calculos.calcularSnapshotLogico(contexto, alcance);
        assertEquals("b081c98ef4b71b8395051b3a9be319cc7d149c9306109828a0fbcb94a7c70795", ejecucion);
        assertEquals("d5b95d6a6c3b9feeee9c2cefdf93a1229f065b783e2db7984b6c6957234b8e88", logico);

        ReservaProjectionRow fila = filaValida();
        byte[] proyeccion = calculos.proyeccionCanonica(fila);
        assertEquals(1210, proyeccion.length);
        String huellaFuente = calculos.calcularHuellaFuente(contexto, fila, proyeccion);
        String identidad = calculos.calcularIdentidadSnapshot(contexto, RESERVA, logico, ejecucion, huellaFuente);
        assertEquals("e98cb3c5c325bae4fd7a8541121e690d0e27e6d9fd81ab756add5ca0f9e99e3e", huellaFuente);
        assertEquals("5c27b72c6f6d7f183eec18d4e3d7b6383c9418f789b96485f892252c6a019d13", identidad);

        var snapshot = new ReservaProjectionMapper().mapear(
                fila, 1, contexto, "READ_BY_RESERVATION_IDS", alcance, ejecucion, logico, calculos);
        assertEquals(RESERVA, snapshot.reservationId());
        assertTrue(snapshot.historicalProgrammingTarget().isEmpty());
        assertEquals(Set.of("createdAtTechnical", "updatedAtTechnical"), snapshot.additionalObservableFields().keySet());
        assertEquals(32, snapshot.provenance().normalizedFields().size());
        assertEquals("ABSENT", snapshot.provenance().normalizedFields().get("historicalProgrammingTarget"));
    }

    @Test
    void rechazaFilaNulaEstadoDesconocidoYRangoNoPositivoComoInputDelAdapter() {
        ReadSnapshotIdentifiers.SesionCalculo calculos = ReadSnapshotIdentifiers.nuevaSesionCalculo();
        String evidencia = calculos.calcularEvidenciaSnapshot("fixture-r1-a", "tx-boundary-0001");
        String observacion = calculos.calcularObservacionStatements(evidencia, "read committed", "read only", List.of(
                "4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e",
                "9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7",
                "dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b"));
        ReadSnapshotContext contexto = contexto(evidencia, observacion);
        byte[] alcance = ReadSnapshotIdentifiers.alcancePorIdentidades(Set.of(RESERVA));
        String ejecucion = calculos.calcularProvenanceEjecucion(contexto, "READ_BY_RESERVATION_IDS", alcance);
        String logico = calculos.calcularSnapshotLogico(contexto, alcance);
        ReservaProjectionRow invalida = new ReservaProjectionRow(
                RESERVA, "DESCONOCIDA", LocalDate.of(2026, 9, 15),
                UUID.fromString("00000000-0000-4000-8000-000000000002"),
                UUID.fromString("00000000-0000-4000-8000-000000000003"),
                UUID.fromString("00000000-0000-4000-8000-000000000004"),
                LocalTime.of(9, 30), LocalTime.of(9, 30),
                OffsetDateTime.of(2026, 9, 1, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 9, 2, 15, 30, 0, 0, ZoneOffset.UTC));
        ReservationReadException error = assertThrows(ReservationReadException.class, () ->
                new ReservaProjectionMapper().mapear(
                        invalida, 1, contexto, "READ_BY_RESERVATION_IDS", alcance, ejecucion, logico, calculos));
        assertEquals(ReservationReadFailureCode.ADAPTER_INPUT_INVALID, error.failureCode());
        assertEquals("Reservation read input or projected row is invalid", error.getMessage());
    }

    @Test
    void readerClasificaIdInesperadoYFalloFisicoSinResultadosParciales() {
        jakarta.persistence.EntityManager entityManager = mock(jakarta.persistence.EntityManager.class);
        org.hibernate.query.NativeQuery<?> consulta = mock(org.hibernate.query.NativeQuery.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(consulta);
        when(consulta.unwrap(org.hibernate.query.NativeQuery.class)).thenReturn((org.hibernate.query.NativeQuery) consulta);
        when(consulta.setParameterList(org.mockito.ArgumentMatchers.eq("reservationIds"), anyList(),
                org.mockito.ArgumentMatchers.eq(UUID.class))).thenReturn((org.hibernate.query.NativeQuery) consulta);
        org.mockito.Mockito.doReturn(List.of((Object) filaComoArreglo(filaValida())))
                .when(consulta).getResultList();
        ReservaProjectionQueryExecutor ejecutor = new ReservaProjectionQueryExecutor(entityManager);
        ReservaJpaReader reader = new ReservaJpaReader(
                ejecutor, new ReservaProjectionMapper(), "fixture:postgres16:r1-a", "fixture-r1-a",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ReadSnapshotContext contexto = contexto(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        UUID solicitada = UUID.fromString("00000000-0000-4000-8000-000000000099");
        ReservationReadException invariante = assertThrows(ReservationReadException.class, () ->
                reader.readByReservationIds(contexto, Set.of(solicitada)));
        assertEquals(ReservationReadFailureCode.READ_SET_INVARIANT_VIOLATION, invariante.failureCode());

        jakarta.persistence.PersistenceException fisica = new jakarta.persistence.PersistenceException("driver failed");
        when(entityManager.createNativeQuery(anyString())).thenThrow(fisica);
        ReservationReadException acceso = assertThrows(ReservationReadException.class, () ->
                reader.readByReservationIds(contexto, Set.of(solicitada)));
        assertEquals(ReservationReadFailureCode.SOURCE_ACCESS_FAILURE, acceso.failureCode());
        assertSame(fisica, acceso.getCause());
    }

    @Test
    void readerPreservaMismaInstanciaDePolicyAunqueHibernateLaEnvuelva() {
        jakarta.persistence.EntityManager entityManager = mock(jakarta.persistence.EntityManager.class);
        ReservaProjectionQueryExecutor ejecutor = new ReservaProjectionQueryExecutor(entityManager);
        ReservaJpaReader reader = new ReservaJpaReader(
                ejecutor, new ReservaProjectionMapper(), "fixture:postgres16:r1-a", "fixture-r1-a",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        F2eSqlPolicyViolationException policy = new F2eSqlPolicyViolationException(
                F2eSqlPolicyViolationException.Reason.CATALOG_MISS,
                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
        when(entityManager.createNativeQuery(anyString()))
                .thenThrow(new jakarta.persistence.PersistenceException("wrapped", policy));
        RuntimeException propagada = assertThrows(RuntimeException.class, () ->
                reader.readByReservationIds(contexto(
                                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
                        Set.of(RESERVA)));
        assertSame(policy, propagada);
    }

    @Test
    void callerNoPuedeForjarSourceNameNiSchemaFingerprint() {
        jakarta.persistence.EntityManager entityManager = mock(jakarta.persistence.EntityManager.class);
        ReservaProjectionQueryExecutor ejecutor = new ReservaProjectionQueryExecutor(entityManager);
        ReservaJpaReader reader = new ReservaJpaReader(
                ejecutor, new ReservaProjectionMapper(), "fixture:postgres16:r1-a", "fixture-r1-a",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ReadSnapshotContext forjado = new ReadSnapshotContext(
                "run", "attempt", "invocation", "fixture:postgres16:forged",
                "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1, "rules",
                ZoneId.of("America/Mexico_City"), ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                reader.readByReservationIds(forjado, Set.of(RESERVA)));
        assertEquals("F2E reader resource provenance not proven", error.getMessage());
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    @Test
    void safeContextAplicaEsquemaCerradoDeValoresSinTransportarPayloadHostil() {
        List<Map<String, String>> validos = List.of(
                Map.of("operation", "READ_BY_RESERVATION_IDS"),
                Map.of("projectionContractId", "R1_RESERVA_PROJECTION"),
                Map.of("scopeKind", "BY_SCOPE"),
                Map.of("reservationIds", "00000000-0000-4000-8000-000000000001,"
                        + "10000000-0000-4000-8000-000000000002"),
                Map.of("fromDate", "2026-09-13"),
                Map.of("requestedCount", "0"),
                Map.of("physicalRowOrdinal", "1"),
                Map.of("physicalColumn", "salon_id"),
                Map.of("sqlCatalogStatementId", "a".repeat(64)),
                Map.of("sqlStateClass", "42"),
                Map.of("vendorErrorCode", "-1"));
        validos.forEach(contexto -> assertEquals(contexto,
                new ReservationReadException(
                        ReservationReadFailureCode.ADAPTER_INPUT_INVALID, contexto).safeContext()));

        List<Map.Entry<String, String>> hostiles = List.of(
                Map.entry("operation", "READ_ALL"),
                Map.entry("scopeKind", "EVERYTHING"),
                Map.entry("reservationIds", "10000000-0000-4000-8000-000000000002,"
                        + "00000000-0000-4000-8000-000000000001"),
                Map.entry("reservationIds", "not-a-uuid"),
                Map.entry("physicalColumn", "cliente_id"),
                Map.entry("sqlStateClass", "ABC"),
                Map.entry("sqlStateClass", "Ñ"),
                Map.entry("operation", "SELECT password FROM usuario"),
                Map.entry("physicalColumn", "jdbcPassword=secret"),
                Map.entry("operation", "A".repeat(4097)),
                Map.entry("operation", "READ_BY_SCOPE\nsecret"));
        for (Map.Entry<String, String> hostil : hostiles) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                    new ReservationReadException(
                            ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                            Map.of(hostil.getKey(), hostil.getValue())));
            assertEquals("Reservation read safe context is invalid", error.getMessage());
            assertFalse(error.getMessage().contains(hostil.getValue()));
        }
    }

    @Test
    void safeContextAplicaDominiosIntCanonicosYRechazaOverflow() {
        for (String clave : List.of("requestedCount", "returnedCount")) {
            for (String valido : List.of("0", "1", Integer.toString(Integer.MAX_VALUE))) {
                assertEquals(valido, new ReservationReadException(
                        ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                        Map.of(clave, valido)).safeContext().get(clave));
            }
            for (String invalido : List.of(
                    "-1", "+1", "01", " 1", "1 ", "1e1", "١",
                    Long.toString((long) Integer.MAX_VALUE + 1L), "9".repeat(4096))) {
                assertContextoSeguroInvalido(clave, invalido);
            }
        }

        for (String valido : List.of("1", Integer.toString(Integer.MAX_VALUE))) {
            assertEquals(valido, new ReservationReadException(
                    ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                    Map.of("physicalRowOrdinal", valido)).safeContext().get("physicalRowOrdinal"));
        }
        for (String invalido : List.of(
                "0", "-1", "+1", "01", Long.toString((long) Integer.MAX_VALUE + 1L))) {
            assertContextoSeguroInvalido("physicalRowOrdinal", invalido);
        }

        for (String valido : List.of(
                Integer.toString(Integer.MIN_VALUE), "-1", "0", "1", Integer.toString(Integer.MAX_VALUE))) {
            assertEquals(valido, new ReservationReadException(
                    ReservationReadFailureCode.SOURCE_ACCESS_FAILURE,
                    Map.of("vendorErrorCode", valido)).safeContext().get("vendorErrorCode"));
        }
        for (String invalido : List.of(
                Long.toString((long) Integer.MIN_VALUE - 1L),
                Long.toString((long) Integer.MAX_VALUE + 1L), "+1", "01", "-0")) {
            assertContextoSeguroInvalido("vendorErrorCode", invalido);
        }
    }

    @Test
    void safeContextSoportaListaCanonicaMayorAlAntiguoLimiteYConservaFalloTipado() {
        Set<UUID> identidades = java.util.stream.LongStream.rangeClosed(1, 512)
                .mapToObj(indice -> new UUID(0L, indice))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        String serializada = String.join(",", identidades.stream().map(UUID::toString).toList());
        assertTrue(serializada.length() > 4096);
        assertEquals(serializada, new ReservationReadException(
                ReservationReadFailureCode.SOURCE_RECORD_NOT_FOUND,
                Map.of("missingReservationIds", serializada)).safeContext().get("missingReservationIds"));

        ReservaProjectionQueryExecutor ejecutor = ejecutorConFilas(List.of());
        ReservationReadException error = assertThrows(ReservationReadException.class, () ->
                reader(ejecutor, new ReservaProjectionMapper()).readByReservationIds(contextoBasico(), identidades));
        assertEquals(ReservationReadFailureCode.SOURCE_RECORD_NOT_FOUND, error.failureCode());
        assertEquals(serializada, error.safeContext().get("reservationIds"));
        assertEquals(serializada, error.safeContext().get("missingReservationIds"));
        assertEquals("512", error.safeContext().get("requestedCount"));
        assertEquals("0", error.safeContext().get("returnedCount"));
    }

    @Test
    void limiteListaUuidDerivaDeLaRepresentacionStringSinInventarLimiteDeNegocio() throws Exception {
        var cardinalidad = ReservationReadException.class.getDeclaredField("CARDINALIDAD_MAXIMA_LISTA_UUID");
        var longitud = ReservationReadException.class.getDeclaredField("LONGITUD_MAXIMA_LISTA_UUID");
        var calcular = ReservationReadException.class.getDeclaredMethod("longitudListaUuid", long.class);
        cardinalidad.setAccessible(true);
        longitud.setAccessible(true);
        calcular.setAccessible(true);
        int maxima = cardinalidad.getInt(null);
        int longitudMaxima = longitud.getInt(null);
        assertEquals(58_040_098, maxima);
        assertEquals(2_147_483_625, longitudMaxima);
        assertEquals((long) longitudMaxima, calcular.invoke(null, (long) maxima));
        assertTrue((long) calcular.invoke(null, (long) maxima + 1L) > Integer.MAX_VALUE);
        assertContextoSeguroInvalido("reservationIds", "");
        assertContextoSeguroInvalido("reservationIds", RESERVA + "," + RESERVA);
    }

    @Test
    void validacionFisicaPrecedeScopeParaCadaNullEstadoPrecisionYRango() {
        ReservaProjectionRow base = filaValida();
        List<CasoFilaInvalida> casos = List.of(
                new CasoFilaInvalida(copiar(base, null, base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "id"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), null, base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "estado"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), null, base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "fecha"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), null,
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "salon_id"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), "DESCONOCIDA", base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "estado"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        null, base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "instructor_id"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), null, base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "tipo_actividad_id"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), null, base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "hora_inicio"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), null,
                        base.createdAtTechnical(), base.updatedAtTechnical()), "hora_fin"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.start(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "hora_fin"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start().withNano(1), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical()), "hora_inicio"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        null, base.updatedAtTechnical()), "creado_en"),
                new CasoFilaInvalida(copiar(base, base.reservationId(), base.state(), base.date(), base.salonId(),
                        base.instructorId(), base.activityId(), base.start(), base.end(),
                        base.createdAtTechnical(), base.updatedAtTechnical().withNano(1)), "actualizado_en"));

        for (CasoFilaInvalida caso : casos) {
            ReservaProjectionQueryExecutor ejecutor = ejecutorConFilas(List.of(caso.fila()));
            ReservaJpaReader reader = reader(ejecutor, new ReservaProjectionMapper());
            ReservationScope fueraDelScope = new ReservationScope(
                    Set.of(UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff")),
                    LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2));
            AtomicReference<Object> salida = new AtomicReference<>();
            ReservationReadException error = assertThrows(ReservationReadException.class, () ->
                    salida.set(reader.readByScope(contextoBasico(), fueraDelScope)), caso.columna());
            assertEquals(ReservationReadFailureCode.ADAPTER_INPUT_INVALID, error.failureCode());
            assertEquals(Map.of(
                    "operation", "READ_BY_SCOPE",
                    "physicalRowOrdinal", "1",
                    "physicalColumn", caso.columna()), error.safeContext());
            assertNull(salida.get());
        }
    }

    @Test
    void consistenciaFinalRechazaCampoNoRevisadoContextoExtraFaltanteValorEIdentidad() {
        ReservaProjectionRow fila = filaValida();
        ReadSnapshotContext contexto = contextoBasico();
        ReadSnapshotIdentifiers.SesionCalculo calculos = ReadSnapshotIdentifiers.nuevaSesionCalculo();
        byte[] alcance = ReadSnapshotIdentifiers.alcancePorIdentidades(Set.of(RESERVA));
        String ejecucion = calculos.calcularProvenanceEjecucion(
                contexto, "READ_BY_RESERVATION_IDS", alcance);
        String logico = calculos.calcularSnapshotLogico(contexto, alcance);
        ReservationSourceSnapshot valido = new ReservaProjectionMapper().mapear(
                fila, 1, contexto, "READ_BY_RESERVATION_IDS", alcance, ejecucion, logico, calculos);

        List<ReservationSourceSnapshot> hostiles = new ArrayList<>();
        hostiles.add(mutarProvenance(valido, camposCon(valido, "attemptIdentity", "attempt-falso"),
                valido.provenance().businessTimeContext()));
        hostiles.add(mutarProvenance(valido, valido.provenance().normalizedFields(), "contexto-falso"));
        Map<String, String> extra = new LinkedHashMap<>(valido.provenance().normalizedFields());
        extra.put("extra", "valor");
        hostiles.add(mutarProvenance(valido, extra, valido.provenance().businessTimeContext()));
        Map<String, String> faltante = new LinkedHashMap<>(valido.provenance().normalizedFields());
        faltante.remove("attemptIdentity");
        hostiles.add(mutarProvenance(valido, faltante, valido.provenance().businessTimeContext()));
        hostiles.add(mutarProvenance(valido, camposCon(valido, "state", "CANCELADA"),
                valido.provenance().businessTimeContext()));
        hostiles.add(new ReservationSourceSnapshot(
                valido.reservationId(), valido.state(), valido.date(), valido.salonId(),
                valido.instructorId(), valido.activityId(), valido.reservedSubinterval(), "f".repeat(64),
                valido.sourceFingerprint(), valido.additionalObservableFields(),
                valido.historicalProgrammingTarget(), valido.provenance()));

        for (ReservationSourceSnapshot hostil : hostiles) {
            ReservaProjectionQueryExecutor ejecutor = ejecutorConFilas(List.of(fila));
            ReservaProjectionMapper mapper = mapperHostil(ignorado -> hostil);
            ReservaJpaReader reader = reader(ejecutor, mapper);
            AtomicReference<Object> salida = new AtomicReference<>();
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    salida.set(reader.readByReservationIds(contexto, Set.of(RESERVA))));
            assertEquals("F2E identity/provenance consistency not proven", error.getMessage());
            assertNull(salida.get());
        }
    }

    @Test
    void construccionRechazaDriftSqlOrdenMapperDigestYReaderAntesDeSql() throws Exception {
        var catalogo = ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1;
        jakarta.persistence.EntityManager entityManager = mock(jakarta.persistence.EntityManager.class);
        var constructorExecutor = ReservaProjectionQueryExecutor.class.getDeclaredConstructor(
                jakarta.persistence.EntityManager.class, ReadSnapshotContext.ProjectionCatalogVersion.class,
                String.class, String.class, String.class, String.class);
        constructorExecutor.setAccessible(true);
        for (Object[] deriva : List.of(
                new Object[]{catalogo.sqlByIds().replace("r.estado", "r.fecha"), catalogo.sqlByScope(),
                        catalogo.statementIdByIds(), catalogo.statementIdByScope()},
                new Object[]{catalogo.sqlByIds().replace("r.id, r.estado", "r.estado, r.id"),
                        catalogo.sqlByScope(), catalogo.statementIdByIds(), catalogo.statementIdByScope()},
                new Object[]{catalogo.sqlByIds(), catalogo.sqlByScope(), "0".repeat(64),
                        catalogo.statementIdByScope()})) {
            InvocationTargetException error = assertThrows(InvocationTargetException.class, () ->
                    constructorExecutor.newInstance(entityManager, catalogo,
                            deriva[0], deriva[1], deriva[2], deriva[3]));
            assertEquals("F2E projection catalog binding not proven", error.getCause().getMessage());
        }
        var constructorMapper = ReservaProjectionMapper.class.getDeclaredConstructor(
                ReadSnapshotContext.ProjectionCatalogVersion.class, String.class, String.class);
        constructorMapper.setAccessible(true);
        InvocationTargetException mapperError = assertThrows(InvocationTargetException.class, () ->
                constructorMapper.newInstance(catalogo, "MapperHostil", "V1"));
        assertEquals("F2E projection catalog binding not proven", mapperError.getCause().getMessage());

        ReservaProjectionQueryExecutor ejecutorHostil = new ReservaProjectionQueryExecutor(entityManager);
        var campoCatalogo = ReservaProjectionQueryExecutor.class.getDeclaredField("catalogo");
        campoCatalogo.setAccessible(true);
        campoCatalogo.set(ejecutorHostil, null);
        assertThrows(IllegalStateException.class, () -> reader(ejecutorHostil, new ReservaProjectionMapper()));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    @Test
    void recomputacionDeReferenciaIndependienteCubreSeisPreimagenesV2() throws Exception {
        String reserva = "00000000-0000-4000-8000-000000000001";
        byte[] alcance = secuenciaReferencia(
                texto("F2E-R1-READ-SCOPE-V2"), texto("READ_BY_RESERVATION_IDS"),
                texto("1"), texto(reserva));
        String evidencia = hashReferencia(secuenciaReferencia(
                texto("F2E-R1-SINGLE-READER-TEST-EVIDENCE-V2"), texto("fixture-r1-a"),
                texto("tx-boundary-0001"), texto("f2eReaderTransactionManager"),
                texto("f2eReaderPersistenceUnit"), texto("read committed"), texto("read only")));
        assertEquals("f195883f1312824ab4cc5e6ff5a865a54fa7e1fb45dafe6cc49f5865a8952877", evidencia);

        String observacion = hashReferencia(secuenciaReferencia(
                texto("F2E-R1-STATEMENT-OBSERVATIONS-V2"), texto(evidencia),
                texto("read committed"), texto("read only"), texto("3"),
                texto("4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e"),
                texto("9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7"),
                texto("dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b")));
        assertEquals("7f7e27a90a3efff6803bf4a8e40dbe4377d8bf1a754187cb14502f47af135838", observacion);

        String ejecucion = hashReferencia(secuenciaReferencia(
                texto("F2E-R1-EXECUTION-PROVENANCE-V2"), texto("run-2026-09-13-001"),
                texto("attempt-01"), texto("reader-invocation-0001"),
                texto("READ_BY_RESERVATION_IDS"), texto("fixture:postgres16:r1-a"),
                texto("sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                texto("R1_RESERVA_PROJECTION/V1"), texto("F2D-RULE-CATALOG/V1"),
                texto("America/Mexico_City"), alcance, texto("SINGLE_READER_TEST"),
                texto(evidencia), texto(observacion)));
        assertEquals("b081c98ef4b71b8395051b3a9be319cc7d149c9306109828a0fbcb94a7c70795", ejecucion);

        String logico = hashReferencia(secuenciaReferencia(
                texto("F2E-R1-LOGICAL-SNAPSHOT-V2"), texto("fixture:postgres16:r1-a"),
                texto("sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                texto("R1_RESERVA_PROJECTION/V1"), texto("F2D-RULE-CATALOG/V1"),
                texto("America/Mexico_City"), alcance, texto("SINGLE_READER_TEST"), texto(evidencia)));
        assertEquals("d5b95d6a6c3b9feeee9c2cefdf93a1229f065b783e2db7984b6c6957234b8e88", logico);

        List<byte[]> campos = List.of(
                campoReferencia(1, "reservationId", "id", "UUID", "VALUE", reserva),
                campoReferencia(2, "state", "estado", "TEXT", "VALUE", "CONFIRMADA"),
                campoReferencia(3, "date", "fecha", "DATE", "VALUE", "2026-09-15"),
                campoReferencia(4, "salonId", "salon_id", "UUID", "VALUE",
                        "00000000-0000-4000-8000-000000000002"),
                campoReferencia(5, "instructorId", "instructor_id", "UUID", "VALUE",
                        "00000000-0000-4000-8000-000000000003"),
                campoReferencia(6, "activityId", "tipo_actividad_id", "UUID", "VALUE",
                        "00000000-0000-4000-8000-000000000004"),
                campoReferencia(7, "start", "hora_inicio", "TIME_MICROS", "VALUE", "08:30:00.000000"),
                campoReferencia(8, "end", "hora_fin", "TIME_MICROS", "VALUE", "09:30:00.000000"),
                campoReferencia(9, "createdAtTechnical", "creado_en", "TIMESTAMP_UTC_MICROS",
                        "VALUE", "2026-09-01T14:00:00.000000Z"),
                campoReferencia(10, "updatedAtTechnical", "actualizado_en", "TIMESTAMP_UTC_MICROS",
                        "VALUE", "2026-09-02T15:30:00.000000Z"),
                campoReferencia(11, "historicalProgrammingTarget", "NONE",
                        "OPTIONAL_HISTORICAL_TARGET", "ABSENT", ""));
        List<byte[]> partesProyeccion = new ArrayList<>(List.of(
                texto("F2E-R1-CANONICAL-PROJECTION-V2"), texto("R1_RESERVA_PROJECTION"),
                texto("V1"), texto("11")));
        partesProyeccion.addAll(campos);
        byte[] proyeccion = secuenciaReferencia(partesProyeccion.toArray(byte[][]::new));
        assertEquals(1210, proyeccion.length);
        String fuente = hashReferencia(secuenciaReferencia(
                texto("F2E-R1-SOURCE-FINGERPRINT-V2"), texto("fixture:postgres16:r1-a"),
                texto("sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                texto("R1_RESERVA_PROJECTION/V1"), texto("LEGACY"), texto("RESERVA"),
                texto(reserva), proyeccion));
        assertEquals("e98cb3c5c325bae4fd7a8541121e690d0e27e6d9fd81ab756add5ca0f9e99e3e", fuente);
        String snapshot = hashReferencia(secuenciaReferencia(
                texto("F2E-R1-SNAPSHOT-IDENTITY-V2"), texto(logico), texto(ejecucion),
                texto("R1_RESERVA_PROJECTION/V1"), texto("LEGACY"), texto("RESERVA"),
                texto(reserva), texto(fuente)));
        assertEquals("5c27b72c6f6d7f183eec18d4e3d7b6383c9418f789b96485f892252c6a019d13", snapshot);
    }

    @Test
    void colisionDigestConPreimagenDistintaFallaCerradoEnSesionReal() throws Exception {
        ReadSnapshotIdentifiers.SesionCalculo sesion = ReadSnapshotIdentifiers.nuevaSesionCalculo();
        String digest = sesion.calcularEvidenciaSnapshot("fixture-r1-a", "tx-boundary-collision");
        var campo = sesion.getClass().getDeclaredField("preimagenes");
        campo.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, byte[]> preimagenes = (Map<String, byte[]>) campo.get(sesion);
        preimagenes.put(digest, "preimagen-hostil-distinta".getBytes(StandardCharsets.UTF_8));
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                sesion.calcularEvidenciaSnapshot("fixture-r1-a", "tx-boundary-collision"));
        assertEquals("F2E identity hash collision detected", error.getMessage());
    }

    private Object[] filaComoArreglo(ReservaProjectionRow fila) {
        return new Object[] { fila.reservationId(), fila.state(), fila.date(), fila.salonId(),
                fila.instructorId(), fila.activityId(), fila.start(), fila.end(),
                fila.createdAtTechnical(), fila.updatedAtTechnical() };
    }

    private ReservaJpaReader reader(
            ReservaProjectionQueryExecutor ejecutor, ReservaProjectionMapper mapper) {
        return new ReservaJpaReader(
                ejecutor, mapper, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                "fixture:postgres16:r1-a", "fixture-r1-a",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ReservaProjectionQueryExecutor ejecutorConFilas(List<ReservaProjectionRow> filas) {
        jakarta.persistence.EntityManager entityManager = mock(jakarta.persistence.EntityManager.class);
        org.hibernate.query.NativeQuery consulta = mock(org.hibernate.query.NativeQuery.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(consulta);
        when(consulta.unwrap(org.hibernate.query.NativeQuery.class)).thenReturn(consulta);
        when(consulta.setParameterList(eq("reservationIds"), anyList(), eq(UUID.class))).thenReturn(consulta);
        when(consulta.setParameterList(eq("salonIds"), anyList(), eq(UUID.class))).thenReturn(consulta);
        when(consulta.setParameter(eq("desde"), any(), eq(LocalDate.class))).thenReturn(consulta);
        when(consulta.setParameter(eq("hasta"), any(), eq(LocalDate.class))).thenReturn(consulta);
        List<Object> resultados = filas.stream().map(this::filaComoArreglo).map(valor -> (Object) valor).toList();
        when(consulta.getResultList()).thenReturn(resultados);
        return new ReservaProjectionQueryExecutor(entityManager);
    }

    private ReservaProjectionMapper mapperHostil(
            UnaryOperator<ReservationSourceSnapshot> mutacion) {
        try {
            var constructor = ReservaProjectionMapper.class.getDeclaredConstructor(
                    ReadSnapshotContext.ProjectionCatalogVersion.class,
                    String.class, String.class, UnaryOperator.class);
            constructor.setAccessible(true);
            return constructor.newInstance(
                    ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                    "ReservaProjectionMapper", "V1", mutacion);
        } catch (ReflectiveOperationException excepcion) {
            throw new AssertionError(excepcion);
        }
    }

    private ReadSnapshotContext contextoBasico() {
        return contexto(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
    }

    private void assertContextoSeguroInvalido(String clave, String valor) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                new ReservationReadException(
                        ReservationReadFailureCode.ADAPTER_INPUT_INVALID, Map.of(clave, valor)));
        assertEquals("Reservation read safe context is invalid", error.getMessage());
        if (!valor.isEmpty()) assertFalse(error.getMessage().contains(valor));
    }

    private ReservaProjectionRow copiar(
            ReservaProjectionRow base,
            UUID reservationId,
            String state,
            LocalDate date,
            UUID salonId,
            UUID instructorId,
            UUID activityId,
            LocalTime start,
            LocalTime end,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        return new ReservaProjectionRow(
                reservationId, state, date, salonId, instructorId, activityId,
                start, end, createdAt, updatedAt);
    }

    private Map<String, String> camposCon(
            ReservationSourceSnapshot snapshot, String clave, String valor) {
        Map<String, String> campos = new LinkedHashMap<>(snapshot.provenance().normalizedFields());
        campos.put(clave, valor);
        return Map.copyOf(campos);
    }

    private ReservationSourceSnapshot mutarProvenance(
            ReservationSourceSnapshot original,
            Map<String, String> campos,
            String contextoNegocio) {
        EvidenceProvenance anterior = original.provenance();
        EvidenceProvenance provenance = new EvidenceProvenance(
                anterior.sourceName(), anterior.schemaFingerprint(), anterior.recordIds(),
                anterior.ruleId(), anterior.ruleVersion(), contextoNegocio, campos);
        return new ReservationSourceSnapshot(
                original.reservationId(), original.state(), original.date(), original.salonId(),
                original.instructorId(), original.activityId(), original.reservedSubinterval(),
                original.snapshotIdentity(), original.sourceFingerprint(),
                original.additionalObservableFields(), original.historicalProgrammingTarget(), provenance);
    }

    private byte[] campoReferencia(
            int posicion, String logico, String fisico, String tipo, String presencia, String valor) {
        return secuenciaReferencia(
                texto("F2E-R1-SOURCE-FIELD-V2"), texto(Integer.toString(posicion)), texto(logico),
                texto(fisico), texto(tipo), texto(presencia), texto(valor));
    }

    private byte[] secuenciaReferencia(byte[]... valores) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.writeBytes(texto(Integer.toString(valores.length)));
        salida.write(':');
        for (byte[] valor : valores) {
            salida.writeBytes(texto(Integer.toString(valor.length)));
            salida.write(':');
            salida.writeBytes(valor);
        }
        return salida.toByteArray();
    }

    private String hashReferencia(byte[] preimagen) throws Exception {
        return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(preimagen));
    }

    private byte[] texto(String valor) {
        return valor.getBytes(StandardCharsets.UTF_8);
    }

    private ReadSnapshotContext contexto(String evidencia, String observacion) {
        return new ReadSnapshotContext(
                "run-2026-09-13-001", "attempt-01", "reader-invocation-0001",
                "fixture:postgres16:r1-a",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                "F2D-RULE-CATALOG/V1", ZoneId.of("America/Mexico_City"),
                ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST, evidencia, observacion);
    }

    private ReservaProjectionRow filaValida() {
        return new ReservaProjectionRow(
                RESERVA, "CONFIRMADA", LocalDate.of(2026, 9, 15),
                UUID.fromString("00000000-0000-4000-8000-000000000002"),
                UUID.fromString("00000000-0000-4000-8000-000000000003"),
                UUID.fromString("00000000-0000-4000-8000-000000000004"),
                LocalTime.of(8, 30), LocalTime.of(9, 30),
                OffsetDateTime.of(2026, 9, 1, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 9, 2, 15, 30, 0, 0, ZoneOffset.UTC));
    }

    private record CasoFilaInvalida(ReservaProjectionRow fila, String columna) {
    }
}
