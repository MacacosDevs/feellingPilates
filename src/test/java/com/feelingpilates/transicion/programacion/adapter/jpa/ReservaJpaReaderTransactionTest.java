package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.ReaderTransactionTestHarness;
import com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReservationReadException;
import com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode;
import com.feelingpilates.transicion.programacion.read.ReservationReadPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(F2ePostgresTestConfiguration.class)
class ReservaJpaReaderTransactionTest {

    @org.springframework.beans.factory.annotation.Autowired
    ReaderTransactionTestHarness harness;

    @org.springframework.beans.factory.annotation.Autowired
    @Qualifier("reservaJpaReader")
    ReservationReadPort reader;

    @org.springframework.beans.factory.annotation.Autowired
    org.springframework.context.ApplicationContext applicationContext;

    @org.springframework.beans.factory.annotation.Autowired
    com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector inspector;

    @Test
    void readerFueraDelHarnessFallaMandatoryAntesDeSql() {
        ReadSnapshotContext contexto = new ReadSnapshotContext(
                "run-outside", "attempt-outside", "invocation-outside", "caller-forged",
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                "F2D-RULE-CATALOG/V1", ZoneId.of("America/Mexico_City"),
                ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        assertThrows(IllegalTransactionStateException.class, () ->
                reader.readByReservationIds(contexto, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
    }

    @Test
    void labelsInternosInconsistentesFallanAntesDeTodoStatement() {
        int i = 0;
        for (String mutacion : List.of("SOURCE_LABEL", "SCHEMA_LABEL")) {
            configurarContexto(mutacion);
            long antes = preparacionesJdbc();
            AtomicReference<Object> salida = new AtomicReference<>();
            var error = assertThrows(IllegalStateException.class, () -> salida.set(
                    harness.inSingleStatementReadOnly(semilla("labels-context-" + mutacion,
                            "attempt-01", "boundary-01", "invocation-01"),
                            Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
            assertEquals("F2E reader resource provenance not proven", error.getMessage());
            assertEquals(antes, preparacionesJdbc());
            assertNull(salida.get());
            i++;
        }
        assertEquals(2, i);
    }

    @Test
    void seamLocalRechazaLabelRenombradoAusenteOPublicoSinStatements() throws Exception {
        Object destinoHarness = AopTestUtils.getUltimateTargetObject(harness);
        Method lecturaLabel = destinoHarness.getClass().getDeclaredMethod("labelPrivado", Object.class, String.class);
        lecturaLabel.setAccessible(true);
        Object destinoReader = AopTestUtils.getUltimateTargetObject(reader);
        long antes = preparacionesJdbc();
        for (String nombre : List.of("sourceNameRenombrado", "labelAusente")) {
            var error = assertThrows(java.lang.reflect.InvocationTargetException.class,
                    () -> lecturaLabel.invoke(destinoHarness, destinoReader, nombre));
            assertTrue(error.getCause() instanceof NoSuchFieldException);
        }
        var error = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> lecturaLabel.invoke(destinoHarness, new LabelPublicoHostil(), "sourceNameConfiable"));
        assertTrue(error.getCause() instanceof IllegalStateException);
        assertEquals("F2E reader resource provenance not proven", error.getCause().getMessage());
        assertEquals(antes, preparacionesJdbc());
        System.out.println("F2E GAP4 missing/renamed/broadenedLabel negativeCases=3 JDBC_statements=0");
    }

    @Test
    void autoridadCompletaRechazaExtrasAliasesDuplicadosYShapesInvalidosSinStatements() throws Exception {
        Object destinoHarness = AopTestUtils.getUltimateTargetObject(harness);
        Method autoridad = destinoHarness.getClass().getDeclaredMethod("autoridadLabelsConfiables", Object.class);
        autoridad.setAccessible(true);
        long antes = preparacionesJdbc();
        Map<String, String> esperado = Map.of("sourceNameConfiable", "source",
                "identidadFuenteDatosConfiable", "fixture", "schemaFingerprintConfiable", "schema");
        assertEquals(esperado, autoridad.invoke(destinoHarness, new LabelsCanonicos()));
        assertEquals(esperado, autoridad.invoke(destinoHarness, new LabelsHeredados()));
        Object destinoReader = AopTestUtils.getUltimateTargetObject(reader);
        var reales = (Map<?, ?>) autoridad.invoke(destinoHarness, destinoReader);
        assertEquals(esperado.keySet(), reales.keySet());
        int negativos = 0;
        for (Object hostil : List.of(new LabelExtra(), new LabelAlias(), new LabelDuplicadoOculto(),
                new LabelExtraHeredado(), new LabelRenombrado(), new Object(),
                new LabelsPublicos(), new LabelsMutables())) {
            var error = assertThrows(java.lang.reflect.InvocationTargetException.class,
                    () -> autoridad.invoke(destinoHarness, hostil));
            assertTrue(error.getCause() instanceof IllegalStateException);
            assertEquals("F2E reader resource provenance not proven", error.getCause().getMessage());
            negativos++;
        }
        assertEquals(8, negativos);
        assertEquals(antes, preparacionesJdbc());
        System.out.println("F2E GAP4 complete instance-String authority exact=true extra/alias/hidden/inherited/renamed/missing/public/mutable negativeCases=8 JDBC_statements=0");
    }

    @Test
    void cadaLabelInyectadoAlReaderDebeSerExactoAntesDeProbes() throws Exception {
        Object destino = AopTestUtils.getUltimateTargetObject(reader);
        for (String nombre : List.of("sourceNameConfiable", "identidadFuenteDatosConfiable", "schemaFingerprintConfiable")) {
            var campo = destino.getClass().getDeclaredField(nombre);
            campo.setAccessible(true);
            Object original = campo.get(destino);
            for (Object hostil : new Object[]{null, original + "-broadened"}) {
                try {
                    campo.set(destino, hostil);
                    long antes = preparacionesJdbc();
                    AtomicReference<Object> salida = new AtomicReference<>();
                    var error = assertThrows(IllegalStateException.class, () -> salida.set(
                            harness.inSingleStatementReadOnly(semilla("labels-reader-" + nombre + "-" + (hostil == null),
                                    "attempt-01", "boundary-01", "invocation-01"),
                                    Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
                    assertEquals("F2E reader resource provenance not proven", error.getMessage());
                    assertEquals(antes, preparacionesJdbc());
                    assertNull(salida.get());
                } finally {
                    campo.set(destino, original);
                }
            }
        }
        System.out.println("F2E GAP4 exact injected/context labels negativeCases=8 JDBC_statements=0 output=absent");
    }

    @Test
    void grafoSpringUsaProxiesSeparadosUnSoloManagerYElInspectorExacto() {
        assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(harness));
        assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(reader));
        assertEquals(1, applicationContext.getBeanNamesForType(PlatformTransactionManager.class).length);
        assertEquals("f2eReaderTransactionManager",
                applicationContext.getBeanNamesForType(PlatformTransactionManager.class)[0]);
        var fabricaEntityManagers = applicationContext.getBean(
                "f2eReaderEntityManagerFactory", jakarta.persistence.EntityManagerFactory.class);
        var gestorTransacciones = applicationContext.getBean(
                "f2eReaderTransactionManager", org.springframework.orm.jpa.JpaTransactionManager.class);
        var dataSource = applicationContext.getBean("f2eReaderDataSource", javax.sql.DataSource.class);
        var inspector = applicationContext.getBean("f2eStatementPolicyInspector",
                com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector.class);
        assertEquals(fabricaEntityManagers, gestorTransacciones.getEntityManagerFactory());
        assertEquals(dataSource, gestorTransacciones.getDataSource());
        assertEquals("f2eReaderPersistenceUnit",
                ((org.springframework.orm.jpa.EntityManagerFactoryInfo) fabricaEntityManagers).getPersistenceUnitName());
        assertEquals(inspector, fabricaEntityManagers.unwrap(org.hibernate.SessionFactory.class)
                .getSessionFactoryOptions().getStatementInspector());
    }

    @Test
    void abortConsumeIdentidadesYPermiteSoloNuevoAttemptParaElMismoRun() {
        var faltante = java.util.UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
        ReservationReadException error = assertThrows(ReservationReadException.class, () ->
                harness.inSingleStatementReadOnly(
                        semilla("retry-run", "attempt-01", "boundary-01", "invocation-01"), Set.of(faltante)));
        assertEquals(ReservationReadFailureCode.SOURCE_RECORD_NOT_FOUND, error.failureCode());
        assertEquals(Map.of(
                "RUN", "OPEN_AFTER_ABORT",
                "ATTEMPT", "CONSUMED_ABORTED",
                "BOUNDARY", "CONSUMED_ABORTED",
                "INVOCATION", "CONSUMED_ABORTED"),
                estados(semilla("retry-run", "attempt-01", "boundary-01", "invocation-01")));
        assertThrows(IllegalStateException.class, () ->
                harness.inSingleStatementReadOnly(
                        semilla("retry-run", "attempt-01", "boundary-02", "invocation-02"),
                        Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        var resultado = harness.inSingleStatementReadOnly(
                semilla("retry-run", "attempt-02", "boundary-02", "invocation-02"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO));
        assertEquals(1, resultado.size());
    }

    @Test
    void cadaMismatchDeRecursoFallaEnElPuntoExactoSinSalidaAceptada() {
        List<String> mutacionesAntesDeProbes = List.of(
                "JDBC_URL", "DATABASE", "SCHEMA", "PRINCIPAL");
        int ordinal = 0;
        for (String mutacion : mutacionesAntesDeProbes) {
            long statementsAntes = statementsAceptados();
            configurarMutacion(mutacion);
            AtomicReference<Object> salidaAceptada = new AtomicReference<>();
            var semilla = semilla("resource-" + ordinal, "attempt-01", "boundary-01", "invocation-01");
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    salidaAceptada.set(harness.inSingleStatementReadOnly(
                            semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))), mutacion);
            assertEquals("F2E reader resource provenance not proven", error.getMessage(), mutacion);
            assertNull(salidaAceptada.get(), mutacion);
            assertEquals(statementsAntes, statementsAceptados(), mutacion);
            assertEquals("OPEN_AFTER_ABORT", estados(semilla).get("RUN"), mutacion);
            assertEquals("CONSUMED_ABORTED", estados(semilla).get("ATTEMPT"), mutacion);
            ordinal++;
        }
    }

    @Test
    void cambioDeConexionDespuesDePruebaDescartaResultadoYConsumeAbortado() {
        try (var contexto = F2ePostgresTestConfiguration.abrirContextoRecursoHostilPrueba(
                "CONEXION_FISICA_REAL")) {
            ReaderTransactionTestHarness harnessHostil = contexto.getBean(ReaderTransactionTestHarness.class);
            var inspectorHostil = contexto.getBean(
                    com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector.class);
            var configuracion = contexto.getBean(F2ePostgresTestConfiguration.class);
            var semilla = semilla("connection-switch", "attempt-01", "boundary-01", "invocation-01");
            long statementsAntes = statementsAceptados(inspectorHostil);
            AtomicReference<Object> salidaAceptada = new AtomicReference<>();
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    salidaAceptada.set(harnessHostil.inSingleStatementReadOnly(
                            semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
            assertEquals("F2E reader resource provenance not proven", error.getMessage());
            assertNull(salidaAceptada.get());
            assertTrue(configuracion.conexionesFisicasDistintasPrueba());
            assertTrue(configuracion.grafoHostilParticipantePrueba());
            assertEquals(statementsAntes + 3, statementsAceptados(inspectorHostil));
            assertEquals("OPEN_AFTER_ABORT", estados(harnessHostil, semilla).get("RUN"));
            assertEquals("CONSUMED_ABORTED", estados(harnessHostil, semilla).get("ATTEMPT"));
        }
    }

    @Test
    void grafosHostilesRealesLleganAlValidadorSinMutarObservacionRecurso() {
        List<String> tipos = List.of(
                "DATA_SOURCE_REAL", "ENTITY_MANAGER_FACTORY_REAL", "TRANSACTION_MANAGER_REAL",
                "ENTITY_MANAGER_REAL", "SESSION_REAL", "CONEXION_FISICA_REAL");
        int ordinal = 0;
        for (String tipo : tipos) {
            try (var contexto = F2ePostgresTestConfiguration.abrirContextoRecursoHostilPrueba(tipo)) {
                ReaderTransactionTestHarness harnessHostil = contexto.getBean(ReaderTransactionTestHarness.class);
                var inspectorHostil = contexto.getBean(
                        com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector.class);
                var configuracion = contexto.getBean(F2ePostgresTestConfiguration.class);
                long statementsAntes = statementsAceptados(inspectorHostil);
                AtomicReference<Object> salidaAceptada = new AtomicReference<>();
                var semilla = semilla("real-graph-" + ordinal,
                        "attempt-01", "boundary-01", "invocation-01");
                IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                        salidaAceptada.set(harnessHostil.inSingleStatementReadOnly(
                                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))), tipo);
                assertEquals("F2E reader resource provenance not proven", error.getMessage(), tipo);
                assertNull(salidaAceptada.get(), tipo);
                long incrementoEsperado = "CONEXION_FISICA_REAL".equals(tipo) ? 3L : 0L;
                assertEquals(statementsAntes + incrementoEsperado,
                        statementsAceptados(inspectorHostil), tipo);
                assertEquals("OPEN_AFTER_ABORT", estados(harnessHostil, semilla).get("RUN"), tipo);
                assertEquals("CONSUMED_ABORTED", estados(harnessHostil, semilla).get("ATTEMPT"), tipo);
                if ("CONEXION_FISICA_REAL".equals(tipo)) {
                    assertTrue(configuracion.conexionesFisicasDistintasPrueba());
                }
                assertTrue(configuracion.grafoHostilParticipantePrueba(), tipo);
            }
            ordinal++;
        }
    }

    @Test
    void colisionEnCadaDominioReservaTodoONada() {
        int ordinal = 0;
        for (String dominio : List.of("RUN", "ATTEMPT", "BOUNDARY", "INVOCATION")) {
            var semilla = semilla("collision-" + ordinal, "attempt-01", "boundary-01", "invocation-01");
            sembrarColision(dominio, semilla);
            long statementsAntes = statementsAceptados();
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    harness.inSingleStatementReadOnly(
                            semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)), dominio);
            assertEquals("F2E execution provenance identity reuse", error.getMessage(), dominio);
            assertEquals(statementsAntes, statementsAceptados(), dominio);
            Map<String, String> observados = estados(semilla);
            for (String candidato : List.of("RUN", "ATTEMPT", "BOUNDARY", "INVOCATION")) {
                assertEquals(candidato.equals(dominio) ? "CONSUMED_ABORTED" : "ABSENT",
                        observados.get(candidato), dominio + "/" + candidato);
            }
            ordinal++;
        }
    }

    @Test
    void exitoConsumeLosCuatroDominiosYBloqueaMismoONuevoAttempt() {
        var semilla = semilla("success-states", "attempt-01", "boundary-01", "invocation-01");
        assertEquals(1, harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
        assertEquals(Map.of(
                "RUN", "COMPLETED_SUCCESS",
                "ATTEMPT", "CONSUMED_SUCCESS",
                "BOUNDARY", "CONSUMED_SUCCESS",
                "INVOCATION", "CONSUMED_SUCCESS"), estados(semilla));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla("success-states", "attempt-02", "boundary-02", "invocation-02"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertEquals("COMPLETED_SUCCESS", estados(semilla).get("RUN"));
    }

    @Test
    void falloDespuesDeConstruirIdentidadesConservaAbortadoYExigeNuevoAttempt() {
        var semilla = semilla("after-identities", "attempt-01", "boundary-01", "invocation-01");
        configurarFallo("DESPUES_DE_IDENTIDADES_ABORTO");
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertEquals(Map.of(
                "RUN", "OPEN_AFTER_ABORT",
                "ATTEMPT", "CONSUMED_ABORTED",
                "BOUNDARY", "CONSUMED_ABORTED",
                "INVOCATION", "CONSUMED_ABORTED"), estados(semilla));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertEquals(1, harness.inSingleStatementReadOnly(
                semilla("after-identities", "attempt-02", "boundary-01", "invocation-01"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
    }

    @Test
    void interrupcionNoClasificadaDejaUnknownYBloqueaTodoElRun() {
        var semilla = semilla("unknown-run", "attempt-01", "boundary-01", "invocation-01");
        configurarFallo("ANTES_DE_RECURSO_UNKNOWN");
        assertThrows(Error.class, () -> harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertEquals(Map.of(
                "RUN", "UNKNOWN",
                "ATTEMPT", "UNKNOWN",
                "BOUNDARY", "UNKNOWN",
                "INVOCATION", "UNKNOWN"), estados(semilla));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla("unknown-run", "attempt-02", "boundary-02", "invocation-02"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertEquals("UNKNOWN", estados(semilla).get("RUN"));
    }

    @Test
    void falloPosteriorAMarcaDeExitoNoRevierteEstadosNiPermiteRetry() {
        var semilla = semilla("post-publication", "attempt-01", "boundary-01", "invocation-01");
        assertEquals(1, harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
        assertThrows(IllegalStateException.class, () -> {
            throw new IllegalStateException("F2E controlled failure after accepted escape");
        });
        assertEquals(Map.of(
                "RUN", "COMPLETED_SUCCESS",
                "ATTEMPT", "CONSUMED_SUCCESS",
                "BOUNDARY", "CONSUMED_SUCCESS",
                "INVOCATION", "CONSUMED_SUCCESS"), estados(semilla));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                semilla("post-publication", "attempt-02", "boundary-02", "invocation-02"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
    }

    @Test
    void urlMetadataHostilDeConexionRealParticipanteRechazaConAutoridadConfiguradaValida() {
        var semilla = semilla("real-hostile-metadata", "attempt-01", "boundary-01", "invocation-01");
        long statementsAntes = statementsAceptados();
        AtomicReference<Object> salida = new AtomicReference<>();
        configurarMutacion("JDBC_URL_QUERY");
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                salida.set(harness.inSingleStatementReadOnly(
                        semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
        assertEquals("F2E reader resource provenance not proven", error.getMessage());
        Map<String, String> evidencia = evidenciaUrlMetadata(harness);
        assertEquals(evidencia.get("DESCRIPTOR"), evidencia.get("CONFIGURADA"));
        assertEquals(evidencia.get("CONFIGURADA"), evidencia.get("ORIGINAL"));
        assertNotEquals(evidencia.get("CONFIGURADA"), evidencia.get("ENTREGADA"));
        assertEquals("1", evidencia.get("OBSERVACIONES"));
        assertTrue(Long.parseLong(evidencia.get("CONEXIONES_ENTREGADAS")) > 0);
        assertNull(salida.get());
        assertEquals(statementsAntes, statementsAceptados());
        assertEquals("OPEN_AFTER_ABORT", estados(semilla).get("RUN"));
        assertEquals("CONSUMED_ABORTED", estados(semilla).get("ATTEMPT"));
    }

    @Test
    void urlJdbcOriginalRechazaMatrizHostilCompletaAntesDeProbes() {
        List<String> mutaciones = List.of(
                "JDBC_URL_QUERY", "JDBC_URL_QUERY_DUPLICADA", "JDBC_URL_FRAGMENTO",
                "JDBC_URL_USER_INFO", "JDBC_URL_PORCENTAJE", "JDBC_URL_DATABASE",
                "JDBC_URL_HOST", "JDBC_URL_PUERTO");
        int ordinal = 0;
        for (String mutacion : mutaciones) {
            long statementsAntes = statementsAceptados();
            configurarMutacion(mutacion);
            AtomicReference<Object> salidaAceptada = new AtomicReference<>();
            var semilla = semilla("url-matrix-" + ordinal,
                    "attempt-01", "boundary-01", "invocation-01");
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    salidaAceptada.set(harness.inSingleStatementReadOnly(
                            semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))), mutacion);
            assertEquals("F2E reader resource provenance not proven", error.getMessage(), mutacion);
            assertNull(salidaAceptada.get(), mutacion);
            assertEquals(statementsAntes, statementsAceptados(), mutacion);
            Map<String, String> evidencia = evidenciaUrlMetadata(harness);
            assertEquals(evidencia.get("DESCRIPTOR"), evidencia.get("CONFIGURADA"), mutacion);
            assertEquals(evidencia.get("CONFIGURADA"), evidencia.get("ORIGINAL"), mutacion);
            assertNotEquals(evidencia.get("ORIGINAL"), evidencia.get("ENTREGADA"), mutacion);
            assertEquals("1", evidencia.get("OBSERVACIONES"), mutacion);
            ordinal++;
        }
    }

    @Test
    void urlHostilEnPrimeraObservacionYLuegoDeExitoSeRechazaDesdeDescriptor() {
        try (var contexto = new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                F2ePostgresTestConfiguration.class)) {
            ReaderTransactionTestHarness harnessAislado = contexto.getBean(ReaderTransactionTestHarness.class);
            var inspectorAislado = contexto.getBean(
                    com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector.class);

            configurarMutacion(harnessAislado, "JDBC_URL_QUERY");
            AtomicReference<Object> primeraSalida = new AtomicReference<>();
            assertThrows(IllegalStateException.class, () -> primeraSalida.set(
                    harnessAislado.inSingleStatementReadOnly(
                            semilla("url-first-hostile", "attempt-01", "boundary-01", "invocation-01"),
                            Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
            assertNull(primeraSalida.get());
            assertEquals(0L, statementsAceptados(inspectorAislado));
            Map<String, String> evidenciaPrimera = evidenciaUrlMetadata(harnessAislado);
            assertEquals(evidenciaPrimera.get("DESCRIPTOR"), evidenciaPrimera.get("CONFIGURADA"));
            assertEquals(evidenciaPrimera.get("CONFIGURADA"), evidenciaPrimera.get("ORIGINAL"));
            assertNotEquals(evidenciaPrimera.get("ORIGINAL"), evidenciaPrimera.get("ENTREGADA"));
            assertEquals("1", evidenciaPrimera.get("OBSERVACIONES"));

            assertEquals(1, harnessAislado.inSingleStatementReadOnly(
                    semilla("url-authorized", "attempt-01", "boundary-01", "invocation-01"),
                    Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
            long trasExito = statementsAceptados(inspectorAislado);
            configurarMutacion(harnessAislado, "JDBC_URL_FRAGMENTO");
            AtomicReference<Object> salidaPosterior = new AtomicReference<>();
            assertThrows(IllegalStateException.class, () -> salidaPosterior.set(
                    harnessAislado.inSingleStatementReadOnly(
                            semilla("url-later-hostile", "attempt-01", "boundary-01", "invocation-01"),
                            Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
            assertNull(salidaPosterior.get());
            assertEquals(trasExito, statementsAceptados(inspectorAislado));
            Map<String, String> evidenciaPosterior = evidenciaUrlMetadata(harnessAislado);
            assertEquals(evidenciaPosterior.get("DESCRIPTOR"), evidenciaPosterior.get("CONFIGURADA"));
            assertEquals(evidenciaPosterior.get("CONFIGURADA"), evidenciaPosterior.get("ORIGINAL"));
            assertNotEquals(evidenciaPosterior.get("ORIGINAL"), evidenciaPosterior.get("ENTREGADA"));
            assertEquals("1", evidenciaPosterior.get("OBSERVACIONES"));
        }
    }

    @Test
    void rollbackOnlyTrasBodyNormalNoMarcaExitoNiDejaEscaparResultado() {
        var semilla = semilla("rollback-only", "attempt-01", "boundary-01", "invocation-01");
        configurarFallo("ROLLBACK_ONLY_TRAS_RESULTADO");
        AtomicReference<Object> salida = new AtomicReference<>();
        assertThrows(RuntimeException.class, () -> salida.set(harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
        assertNull(salida.get());
        assertEquals(Map.of(
                "RUN", "OPEN_AFTER_ABORT",
                "ATTEMPT", "CONSUMED_ABORTED",
                "BOUNDARY", "CONSUMED_ABORTED",
                "INVOCATION", "CONSUMED_ABORTED"), estados(semilla));
    }

    @Test
    void falloRealEnBeforeCommitClasificaAbortTrasCruzarInterceptor() {
        var semilla = semilla("completion-failure", "attempt-01", "boundary-01", "invocation-01");
        configurarFallo("FALLO_EN_BEFORE_COMMIT");
        AtomicReference<Object> salida = new AtomicReference<>();
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                salida.set(harness.inSingleStatementReadOnly(
                        semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))));
        assertEquals("F2E controlled transaction completion failure", error.getMessage());
        assertNull(salida.get());
        assertEquals("OPEN_AFTER_ABORT", estados(semilla).get("RUN"));
        assertEquals("CONSUMED_ABORTED", estados(semilla).get("ATTEMPT"));
    }

    @Test
    void exitoPermaneceActiveEnBeforeCommitYSeConsumeSoloEnAfterCompletion() {
        var semilla = semilla("completion-success", "attempt-01", "boundary-01", "invocation-01");
        configurarFallo("OBSERVAR_BEFORE_COMMIT");
        assertEquals(1, harness.inSingleStatementReadOnly(
                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
        assertEquals("ACTIVE", invocarPrivado("estadoBeforeCommitPrueba", new Class<?>[]{}));
        assertEquals("COMPLETED_SUCCESS", estados(semilla).get("RUN"));
        assertEquals("CONSUMED_SUCCESS", estados(semilla).get("ATTEMPT"));
    }

    @Test
    void manifiestoRealRechazaFaltaExtraOrdenDataExtraYCatalogoIncorrecto() {
        int ordinal = 0;
        for (String mutacion : List.of(
                "FALTA_PROBE", "PROBE_EXTRA", "ORDEN_INCORRECTO", "DATA_EXTRA", "CATALOGO_INCORRECTO")) {
            var semilla = semilla("manifest-" + ordinal, "attempt-01", "boundary-01", "invocation-01");
            configurarManifiesto(mutacion);
            AtomicReference<Object> salida = new AtomicReference<>();
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    salida.set(harness.inSingleStatementReadOnly(
                            semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))), mutacion);
            assertEquals("F2E statement manifest not proven", error.getMessage(), mutacion);
            assertNull(salida.get(), mutacion);
            assertEquals("OPEN_AFTER_ABORT", estados(semilla).get("RUN"), mutacion);
            ordinal++;
        }
    }

    @Test
    void recomputeFinalRechazaEvidenciaStatementsEInvocacionConPreimagenInconsistente() {
        int ordinal = 0;
        for (String mutacion : List.of("EVIDENCIA_SNAPSHOT", "HUELLA_STATEMENTS", "READER_INVOCATION")) {
            var semilla = semilla("context-recompute-" + ordinal,
                    "attempt-01", "boundary-01", "invocation-01");
            configurarContexto(mutacion);
            AtomicReference<Object> salida = new AtomicReference<>();
            IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                    salida.set(harness.inSingleStatementReadOnly(
                            semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO))), mutacion);
            assertEquals("F2E identity/provenance consistency not proven", error.getMessage(), mutacion);
            assertNull(salida.get(), mutacion);
            assertEquals("OPEN_AFTER_ABORT", estados(semilla).get("RUN"), mutacion);
            ordinal++;
        }
    }

    @Test
    void sqlDesconocidaEsRechazadaAntesDePreparacionJdbcReal() {
        var semilla = semilla("pre-jdbc", "attempt-01", "boundary-01", "invocation-01");
        configurarFallo("SQL_DESCONOCIDA_ANTES_DE_JDBC");
        long preparacionesAntes = preparacionesJdbc();
        long statementsAntes = statementsAceptados();
        F2eSqlPolicyViolationException error = assertThrows(F2eSqlPolicyViolationException.class, () ->
                harness.inSingleStatementReadOnly(
                        semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        assertEquals(F2eSqlPolicyViolationException.Reason.CATALOG_MISS, error.reason());
        assertEquals(statementsAntes + 2, statementsAceptados());
        assertTrue(preparacionesJdbc() >= preparacionesAntes + 2);
        @SuppressWarnings("unchecked")
        Map<String, Long> evidencia = (Map<String, Long>) invocarPrivado(
                "evidenciaPreJdbcPrueba", new Class<?>[]{});
        assertEquals(evidencia.get("ANTES"), evidencia.get("DESPUES"));
        assertEquals("OPEN_AFTER_ABORT", estados(semilla).get("RUN"));
    }

    @Test
    void cierreDeApplicationContextTerminaNamespaceYElNuevoContextoAceptaMismasCadenas() {
        var semilla = semilla("namespace-run", "attempt-01", "boundary-01", "invocation-01");
        try (var contextoUno = new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                F2ePostgresTestConfiguration.class)) {
            var harnessUno = contextoUno.getBean(ReaderTransactionTestHarness.class);
            assertEquals(1, harnessUno.inSingleStatementReadOnly(
                    semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
            assertThrows(IllegalStateException.class, () -> harnessUno.inSingleStatementReadOnly(
                    semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
        }
        try (var contextoDos = new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                F2ePostgresTestConfiguration.class)) {
            var harnessDos = contextoDos.getBean(ReaderTransactionTestHarness.class);
            assertEquals(1, harnessDos.inSingleStatementReadOnly(
                    semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)).size());
        }
    }

    @Test
    void reservaConcurrenteDuplicadaTieneComoMaximoUnExito() throws Exception {
        var semilla = semilla("race-run", "attempt-race", "boundary-race", "invocation-race");
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger reutilizaciones = new AtomicInteger();
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            for (int indice = 0; indice < 2; indice++) {
                ejecutor.submit(() -> {
                    preparados.countDown();
                    try {
                        salida.await();
                        harness.inSingleStatementReadOnly(
                                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO));
                        exitos.incrementAndGet();
                    } catch (IllegalStateException esperada) {
                        if ("F2E execution provenance identity reuse".equals(esperada.getMessage())) {
                            reutilizaciones.incrementAndGet();
                        } else {
                            throw esperada;
                        }
                    } catch (InterruptedException interrupcion) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            assertTrue(preparados.await(5, TimeUnit.SECONDS));
            salida.countDown();
            ejecutor.shutdown();
            assertTrue(ejecutor.awaitTermination(30, TimeUnit.SECONDS));
        }
        assertEquals(1, exitos.get());
        assertEquals(1, reutilizaciones.get());
    }

    @Test
    void carrerasCoordinadasCubrenRunAttemptBoundaryEInvocation() throws Exception {
        List<List<ReaderTransactionTestHarness.SemillaLectura>> casos = List.of(
                List.of(
                        semilla("race-run-domain", "attempt-a", "boundary-a", "invocation-a"),
                        semilla("race-run-domain", "attempt-b", "boundary-b", "invocation-b")),
                List.of(
                        semilla("race-attempt-domain", "attempt-shared", "boundary-a", "invocation-a"),
                        semilla("race-attempt-domain", "attempt-shared", "boundary-b", "invocation-b")),
                List.of(
                        semilla("race-boundary-domain", "attempt-shared", "boundary-shared", "invocation-a"),
                        semilla("race-boundary-domain", "attempt-shared", "boundary-shared", "invocation-b")),
                List.of(
                        semilla("race-invocation-domain", "attempt-shared", "boundary-a", "invocation-shared"),
                        semilla("race-invocation-domain", "attempt-shared", "boundary-b", "invocation-shared")));
        for (List<ReaderTransactionTestHarness.SemillaLectura> caso : casos) {
            ResultadoCarrera resultado = ejecutarCarrera(caso.get(0), caso.get(1));
            assertEquals(1, resultado.exitos());
            assertEquals(1, resultado.reutilizaciones());
        }
    }

    @Test
    void runActiveBloqueaReuseYOtroAttemptHastaTransicionTerminal() throws Exception {
        var semilla = semilla("active-run", "attempt-01", "boundary-01", "invocation-01");
        invocarPrivado("configurarBloqueoActivoPrueba", new Class<?>[]{});
        try (var ejecutor = Executors.newSingleThreadExecutor()) {
            var futuro = ejecutor.submit(() -> harness.inSingleStatementReadOnly(
                    semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
            assertTrue((boolean) invocarPrivado("esperarReservaActivaPrueba", new Class<?>[]{}));
            assertEquals(Map.of(
                    "RUN", "ACTIVE",
                    "ATTEMPT", "ACTIVE",
                    "BOUNDARY", "ACTIVE",
                    "INVOCATION", "ACTIVE"), estados(semilla));
            assertThrows(IllegalStateException.class, () -> harness.inSingleStatementReadOnly(
                    semilla("active-run", "attempt-02", "boundary-02", "invocation-02"),
                    Set.of(F2ePostgresTestConfiguration.RESERVA_UNO)));
            invocarPrivado("liberarReservaActivaPrueba", new Class<?>[]{});
            assertEquals(1, futuro.get(30, TimeUnit.SECONDS).size());
        }
    }

    private ReaderTransactionTestHarness.SemillaLectura semilla(
            String run, String attempt, String boundary, String invocation) {
        return new ReaderTransactionTestHarness.SemillaLectura(
                run, attempt, boundary, invocation, "F2D-RULE-CATALOG/V1",
                ZoneId.of("America/Mexico_City"), ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST);
    }

    private ResultadoCarrera ejecutarCarrera(
            ReaderTransactionTestHarness.SemillaLectura primera,
            ReaderTransactionTestHarness.SemillaLectura segunda) throws Exception {
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger reutilizaciones = new AtomicInteger();
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            for (var semilla : List.of(primera, segunda)) {
                ejecutor.submit(() -> {
                    preparados.countDown();
                    try {
                        salida.await();
                        harness.inSingleStatementReadOnly(
                                semilla, Set.of(F2ePostgresTestConfiguration.RESERVA_UNO));
                        exitos.incrementAndGet();
                    } catch (IllegalStateException esperada) {
                        if ("F2E execution provenance identity reuse".equals(esperada.getMessage())) {
                            reutilizaciones.incrementAndGet();
                        } else {
                            throw esperada;
                        }
                    } catch (InterruptedException interrupcion) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            assertTrue(preparados.await(5, TimeUnit.SECONDS));
            salida.countDown();
            ejecutor.shutdown();
            assertTrue(ejecutor.awaitTermination(30, TimeUnit.SECONDS));
        }
        return new ResultadoCarrera(exitos.get(), reutilizaciones.get());
    }

    private void configurarMutacion(String mutacion) {
        invocarPrivado("configurarMutacionRecursoPrueba", new Class<?>[]{String.class}, mutacion);
    }

    private void configurarMutacion(ReaderTransactionTestHarness objetivo, String mutacion) {
        invocarPrivado(objetivo, "configurarMutacionRecursoPrueba",
                new Class<?>[]{String.class}, mutacion);
    }

    private void configurarFallo(String punto) {
        invocarPrivado("configurarFalloPrueba", new Class<?>[]{String.class}, punto);
    }

    private void configurarManifiesto(String mutacion) {
        invocarPrivado("configurarMutacionManifiestoPrueba", new Class<?>[]{String.class}, mutacion);
    }

    private void configurarContexto(String mutacion) {
        invocarPrivado("configurarMutacionContextoPrueba", new Class<?>[]{String.class}, mutacion);
    }

    private void sembrarColision(String dominio, ReaderTransactionTestHarness.SemillaLectura semilla) {
        invocarPrivado("sembrarColisionPrueba",
                new Class<?>[]{String.class, ReaderTransactionTestHarness.SemillaLectura.class}, dominio, semilla);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> estados(ReaderTransactionTestHarness.SemillaLectura semilla) {
        return estados(harness, semilla);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> estados(
            ReaderTransactionTestHarness objetivo,
            ReaderTransactionTestHarness.SemillaLectura semilla) {
        return (Map<String, String>) invocarPrivado(objetivo,
                "estadosPrueba", new Class<?>[]{ReaderTransactionTestHarness.SemillaLectura.class}, semilla);
    }

    private long statementsAceptados() {
        return statementsAceptados(inspector);
    }

    private long statementsAceptados(Object inspectorObjetivo) {
        try {
            Method metodo = inspectorObjetivo.getClass().getDeclaredMethod("statementsAceptadosPrueba");
            metodo.setAccessible(true);
            return (long) metodo.invoke(inspectorObjetivo);
        } catch (ReflectiveOperationException excepcion) {
            throw new AssertionError(excepcion);
        }
    }

    private long preparacionesJdbc() {
        return (long) invocarPrivado("preparacionesJdbcPrueba", new Class<?>[]{});
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> evidenciaUrlMetadata(ReaderTransactionTestHarness objetivo) {
        return (Map<String, String>) invocarPrivado(
                objetivo, "evidenciaUrlMetadataPrueba", new Class<?>[]{});
    }

    private Object invocarPrivado(String nombre, Class<?>[] tipos, Object... argumentos) {
        return invocarPrivado(harness, nombre, tipos, argumentos);
    }

    private Object invocarPrivado(
            ReaderTransactionTestHarness harnessObjetivo,
            String nombre,
            Class<?>[] tipos,
            Object... argumentos) {
        try {
            Object objetivo = AopTestUtils.getUltimateTargetObject(harnessObjetivo);
            Method metodo = objetivo.getClass().getDeclaredMethod(nombre, tipos);
            metodo.setAccessible(true);
            return metodo.invoke(objetivo, argumentos);
        } catch (ReflectiveOperationException excepcion) {
            throw new AssertionError(excepcion);
        }
    }

    private record ResultadoCarrera(int exitos, int reutilizaciones) {
    }

    private static final class LabelPublicoHostil {
        public final String sourceNameConfiable = "hostile";
    }

    private static class LabelsCanonicos {
        private static final String CONSTANTE_NO_LABEL = "constant";
        private final Object NO_STRING = new Object();
        private final String sourceNameConfiable = "source";
        private final String identidadFuenteDatosConfiable = "fixture";
        private final String schemaFingerprintConfiable = "schema";
    }

    private static final class LabelsHeredados extends LabelsCanonicos {
    }

    private static class LabelExtra extends LabelsCanonicos {
        private final String labelAdicional = "unauthorized";
    }

    private static final class LabelExtraHeredado extends LabelExtra {
    }

    private static final class LabelAlias extends LabelsCanonicos {
        private final String sourceAlias = "source";
    }

    private static final class LabelDuplicadoOculto extends LabelsCanonicos {
        private final String sourceNameConfiable = "source";
    }

    private static final class LabelRenombrado {
        private final String sourceNameRenombrado = "source";
        private final String identidadFuenteDatosConfiable = "fixture";
        private final String schemaFingerprintConfiable = "schema";
    }

    private static final class LabelsPublicos {
        public final String sourceNameConfiable = "source";
        private final String identidadFuenteDatosConfiable = "fixture";
        private final String schemaFingerprintConfiable = "schema";
    }

    private static final class LabelsMutables {
        private String sourceNameConfiable = "source";
        private final String identidadFuenteDatosConfiable = "fixture";
        private final String schemaFingerprintConfiable = "schema";
    }
}
