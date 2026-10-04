package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSliceChecksum;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservaJpaReaderRuntimeIsolationTest {

    @Test
    void contextosRealesDefaultYProdArrancanSinNingunBeanR1() throws Exception {
        try (var postgres = new org.testcontainers.containers.PostgreSQLContainer<>(
                org.testcontainers.utility.DockerImageName.parse("postgres:16-alpine"))) {
            postgres.start();
            for (String perfil : List.of("default", "prod")) {
                List<String> argumentos = new java.util.ArrayList<>(List.of(
                        "--server.port=0", "--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword(),
                        "--logging.level.root=OFF", "--logging.level.org.hibernate.SQL=OFF",
                        "--logging.level.com.feelingpilates=OFF", "--spring.jpa.show-sql=false",
                        "--spring.main.banner-mode=off"));
                if (perfil.equals("prod")) argumentos.add("--spring.profiles.active=prod");
                // Only the actual product entry point is a source; no R1 test configuration is imported.
                try (var contexto = new org.springframework.boot.builder.SpringApplicationBuilder(
                        com.feelingpilates.FeelingpilatesApplication.class)
                        .registerShutdownHook(false).run(argumentos.toArray(String[]::new))) {
                    org.junit.jupiter.api.Assertions.assertTrue(contexto.isActive());
                    for (String nombre : List.of("f2ePostgresContainer", "f2ePrivilegedDataSource",
                            "f2eReaderDataSource", "f2eReaderEntityManagerFactory", "f2eReaderEntityManager",
                            "f2eReaderTransactionManager", "f2eStatementPolicyInspector",
                            "reservaProjectionQueryExecutor", "reservaProjectionMapper", "reservaJpaReader",
                            "readerTransactionTestHarness")) {
                        org.junit.jupiter.api.Assertions.assertFalse(contexto.containsBean(nombre), nombre);
                    }
                    for (Class<?> tipo : List.of(ReservaJpaReader.class,
                            com.feelingpilates.transicion.programacion.read.ReservationReadPort.class,
                            com.feelingpilates.transicion.programacion.read.ReadSnapshotContext.class,
                            com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.class,
                            com.feelingpilates.transicion.programacion.read.ReservationScope.class,
                            com.feelingpilates.transicion.programacion.read.ReservationReadException.class,
                            com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode.class,
                            F2eSqlPolicyViolationException.class,
                            com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionRow.class,
                            com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionQueryExecutor.class,
                            com.feelingpilates.transicion.programacion.adapter.jpa.mapper.ReservaProjectionMapper.class,
                            com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.ReaderTransactionTestHarness.class,
                            com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration.class,
                            F2eStatementPolicyInspector.class, F2eSliceChecksum.class,
                            com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSelectOnlyRole.class,
                            Class.forName("com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration$DescriptorRecursoLector"),
                            org.testcontainers.containers.PostgreSQLContainer.class)) {
                        assertEquals(0, contexto.getBeanNamesForType(tipo).length, tipo.getName());
                    }
                    org.junit.jupiter.api.Assertions.assertTrue(contexto.containsBean("turnoInstructorService"));
                    org.junit.jupiter.api.Assertions.assertTrue(contexto.containsBean("reservaService"));
                    System.out.println("F2E GAP5 realProductContext=" + perfil + " started=true R1_names=0 R1_types=0 legacyAuthorityPresent=true");
                }
            }
        }
    }

    @Test
    void normalizaLosVectoresSqlAYCAlMismoCatalogo() {
        String sqlA = "  SELECT\n r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, "
                + "r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en\t"
                + "FROM public.reserva r WHERE r.id IN (?, ?, ?) ORDER BY r.id  ";
        String sqlC = "SELECT  r.id,\r\n\tr.estado, r.fecha, r.salon_id, r.instructor_id, "
                + "r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en "
                + "FROM public.reserva r WHERE r.id IN (?1) ORDER BY r.id";
        String esperada = "SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, "
                + "r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en "
                + "FROM public.reserva r WHERE r.id IN (?*) ORDER BY r.id";
        assertEquals(esperada, F2eStatementPolicyInspector.normalizar(sqlA));
        assertEquals(esperada, F2eStatementPolicyInspector.normalizar(sqlC));
        assertEquals("dfa84c5db84f44c41adb82b0a58a2f080fc05a0612df15ababbd5dc064192a5b",
                F2eStatementPolicyInspector.identificar(esperada));
    }

    @Test
    void normalizaScopeConMarcadoresPostgresYReproduceLosCuatroIdsDeCatalogo() {
        String scope = "SELECT r.id, r.estado, r.fecha, r.salon_id, r.instructor_id, "
                + "r.tipo_actividad_id, r.hora_inicio, r.hora_fin, r.creado_en, r.actualizado_en "
                + "FROM public.reserva r WHERE r.salon_id IN ($1,$2) AND r.fecha >= $3 "
                + "AND r.fecha <= $4 ORDER BY r.id";
        String canonica = F2eStatementPolicyInspector.normalizar(scope);
        assertEquals("c04cd40e2e3b991de845e73df65b6c9988be10c21cc1ee71474ab848b1e8eb9a",
                F2eStatementPolicyInspector.identificar(canonica));
        assertEquals("4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e",
                F2eStatementPolicyInspector.identificar("SELECT current_setting('transaction_isolation')"));
        assertEquals("9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7",
                F2eStatementPolicyInspector.identificar("SELECT current_setting('transaction_read_only')"));
    }

    @Test
    void rechazaUnknownSelectDmlDenylistYNormalizacionAntesDeRetornarSql() {
        verificarRazon("SELECT r.id FROM public.reserva r ORDER BY r.id",
                F2eSqlPolicyViolationException.Reason.CATALOG_MISS);
        verificarRazon("INSERT INTO public.reserva(id) VALUES (?)",
                F2eSqlPolicyViolationException.Reason.STATEMENT_CLASS_DENIED);
        verificarRazon("SELECT nextval('reserva_seq')",
                F2eSqlPolicyViolationException.Reason.DENYLIST_VIOLATION);
        verificarRazon("SELECT 1; DROP TABLE public.reserva",
                F2eSqlPolicyViolationException.Reason.NORMALIZATION_REJECTED);
        verificarRazon("SELECT \uD800",
                F2eSqlPolicyViolationException.Reason.NORMALIZATION_REJECTED);
        verificarRazon("select r.id FROM public.reserva r ORDER BY r.id",
                F2eSqlPolicyViolationException.Reason.CATALOG_MISS);
    }

    @Test
    void capturaEsNoAnidableOrdenadaYNoFiltraEntreInvocaciones() {
        F2eStatementPolicyInspector inspector = new F2eStatementPolicyInspector();
        var captura = inspector.abrirCaptura("invocation-policy");
        assertThrows(IllegalStateException.class, () -> inspector.abrirCaptura("nested"));
        inspector.inspect("SELECT current_setting('transaction_isolation')");
        inspector.inspect("SELECT current_setting('transaction_read_only')");
        assertEquals(List.of(
                "4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e",
                "9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7"),
                inspector.cerrarCaptura(captura));
        assertThrows(IllegalStateException.class, () ->
                inspector.inspect("SELECT current_setting('transaction_isolation')"));
    }

    @Test
    void reproduceTodosLosVectoresChecksumAHaHIncluidosLosVacios() {
        UUID idA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID idA0 = UUID.fromString("00000000-0000-0000-0000-000000000000");
        String hashFilaA = F2eSliceChecksum.hashFilaPrueba(camposA());
        assertEquals("7f2438b5698a3d3ed9aec57674c64834da4aba252c08828768dbd148fa60006c", hashFilaA);

        var filaAbC = List.of(
                F2eSliceChecksum.Campo.valor("left", "S", "ab"),
                F2eSliceChecksum.Campo.valor("right", "S", "c"));
        var filaABc = List.of(
                F2eSliceChecksum.Campo.valor("left", "S", "a"),
                F2eSliceChecksum.Campo.valor("right", "S", "bc"));
        assertEquals("4:19:F2E_CHECKSUM_ROW_V11:242:5:21:F2E_CHECKSUM_FIELD_V14:left1:S1:V2:ab"
                        + "42:5:21:F2E_CHECKSUM_FIELD_V15:right1:S1:V1:c",
                texto(F2eSliceChecksum.preimagenFilaPrueba(filaAbC)));
        assertEquals("f3a481c27a51936e546068c1d24bf8014fbcb6b03e9fc44a4004b49d1bea8c95",
                F2eSliceChecksum.hashFilaPrueba(filaAbC));
        assertEquals("4:19:F2E_CHECKSUM_ROW_V11:241:5:21:F2E_CHECKSUM_FIELD_V14:left1:S1:V1:a"
                        + "43:5:21:F2E_CHECKSUM_FIELD_V15:right1:S1:V2:bc",
                texto(F2eSliceChecksum.preimagenFilaPrueba(filaABc)));
        assertEquals("7b3ae77aa7da771cc416c0863408ead4f483d21d35ba3a47d06155e05fe12ae1",
                F2eSliceChecksum.hashFilaPrueba(filaABc));

        var filaTablaA = new F2eSliceChecksum.FilaHash(idA, hashFilaA);
        String hashTablaPublica = F2eSliceChecksum.hashTablaPrueba("public.reserva", List.of(filaTablaA));
        assertEquals("4:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:164:" + hashFilaA,
                texto(F2eSliceChecksum.preimagenTablaPrueba("public.reserva", List.of(filaTablaA))));
        assertEquals("da4cad572663f11e0d5c8a4b6265729465089e13e940924f3958acac41e6ad1b",
                hashTablaPublica);

        String hashTablaAuditoria = F2eSliceChecksum.hashTablaPrueba(
                "audit.reserva_shadow", List.of(filaTablaA));
        assertEquals("4:21:F2E_CHECKSUM_TABLE_V120:audit.reserva_shadow1:164:" + hashFilaA,
                texto(F2eSliceChecksum.preimagenTablaPrueba("audit.reserva_shadow", List.of(filaTablaA))));
        assertEquals("71b3084caaf55b7ad11c58f3f513a98228e6bce0535466cc11f7d96be6ec2278",
                hashTablaAuditoria);

        String hashFilaA0 = F2eSliceChecksum.hashFilaPrueba(camposA0());
        assertEquals("ea5f926c634840690be3087662db5dab08f446323378b075037e6e59e318f3e0", hashFilaA0);
        var dosFilasInversas = List.of(
                filaTablaA, new F2eSliceChecksum.FilaHash(idA0, hashFilaA0));
        String preimagenDosFilas = "5:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:264:" + hashFilaA0
                + "64:" + hashFilaA;
        assertEquals(preimagenDosFilas,
                texto(F2eSliceChecksum.preimagenTablaPrueba("public.reserva", dosFilasInversas)));
        assertEquals("ee3953739cf5633cfe8869659be5b4d7075a7c74beb80be22dab90ae15eecedc",
                F2eSliceChecksum.hashTablaPrueba("public.reserva", dosFilasInversas));

        byte[] scopeIdentidades = F2eSliceChecksum.scopePorIdentidadesPrueba(List.of(idA));
        assertEquals("4:21:F2E_CHECKSUM_SCOPE_V118:BY_RESERVATION_IDS1:136:" + idA, texto(scopeIdentidades));
        assertEquals("4:21:F2E_CHECKSUM_SLICE_V189:" + texto(scopeIdentidades)
                        + "1:1122:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:"
                        + hashTablaPublica,
                texto(F2eSliceChecksum.preimagenSlicePrueba(
                        scopeIdentidades, Map.of("public.reserva", hashTablaPublica))));
        assertEquals("ff619ecac74d86a149cc9110c9ac205990fc5a87eb10e61ba64c5b1a6967f5be",
                F2eSliceChecksum.hashSlicePrueba(scopeIdentidades, Map.of("public.reserva", hashTablaPublica)));

        UUID salon = UUID.fromString("00000000-0000-0000-0000-000000000002");
        byte[] scopeRangoA = F2eSliceChecksum.scopePorRangoPrueba(
                List.of(salon), LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-02"));
        byte[] scopeRangoB = F2eSliceChecksum.scopePorRangoPrueba(
                List.of(salon), LocalDate.parse("2026-09-02"), LocalDate.parse("2026-09-02"));
        assertEquals("6b8c91ac4bb06fed2476d190b93e37a8cd75c2485a66033801bf01676e1f0b79",
                F2eSliceChecksum.hashSlicePrueba(scopeRangoA, Map.of("public.reserva", hashTablaPublica)));
        assertEquals("7b79a1f2403d6e63e9a012fdda6a4da39c422f5c559df2d5f38c61bd0c4b0db5",
                F2eSliceChecksum.hashSlicePrueba(scopeRangoB, Map.of("public.reserva", hashTablaPublica)));

        assertEquals("3:21:F2E_CHECKSUM_TABLE_V114:public.reserva1:0",
                texto(F2eSliceChecksum.preimagenTablaPrueba("public.reserva", List.of())));
        assertEquals("7cd08818f587c66f33716592705b5d716a19fd1ac434cb65e0defce21abeb916",
                F2eSliceChecksum.hashTablaPrueba("public.reserva", List.of()));
        assertEquals("775d6bf38eb09356818f772684ab50e4d0c3c3776e623c21f179a8f5368a8625",
                F2eSliceChecksum.hashSlicePrueba(scopeIdentidades, Map.of()));

        Map<String, String> tablasH = Map.of(
                "public.reserva", hashTablaPublica,
                "audit.reserva_shadow", hashTablaAuditoria);
        String preimagenH = "5:21:F2E_CHECKSUM_SLICE_V189:" + texto(scopeIdentidades)
                + "1:2128:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V120:audit.reserva_shadow64:"
                + hashTablaAuditoria
                + "122:3:33:F2E_CHECKSUM_SLICE_TABLE_ENTRY_V114:public.reserva64:"
                + hashTablaPublica;
        assertEquals(preimagenH, texto(F2eSliceChecksum.preimagenSlicePrueba(scopeIdentidades, tablasH)));
        assertEquals("aa10c3ce64e25734e671c9bc9e91555a714655036cf03b27425e88e6c67b99a7",
                F2eSliceChecksum.hashSlicePrueba(scopeIdentidades, tablasH));
        assertEquals("aa10c3ce64e25734e671c9bc9e91555a714655036cf03b27425e88e6c67b99a7",
                F2eSliceChecksum.hashSlicePrueba(scopeIdentidades, Map.of(
                        "audit.reserva_shadow", hashTablaAuditoria,
                        "public.reserva", hashTablaPublica)));
    }

    private List<F2eSliceChecksum.Campo> camposA() {
        return List.of(
                F2eSliceChecksum.Campo.valor("id", "U", "00000000-0000-0000-0000-000000000001"),
                F2eSliceChecksum.Campo.valor("salon_id", "U", "00000000-0000-0000-0000-000000000002"),
                F2eSliceChecksum.Campo.valor("instructor_id", "U", "00000000-0000-0000-0000-000000000003"),
                F2eSliceChecksum.Campo.valor("cliente_id", "U", "00000000-0000-0000-0000-000000000004"),
                F2eSliceChecksum.Campo.valor("tipo_actividad_id", "U", "00000000-0000-0000-0000-000000000005"),
                F2eSliceChecksum.Campo.valor("fecha", "D", "2026-09-02"),
                F2eSliceChecksum.Campo.valor("hora_inicio", "T", "08:30:00.000000"),
                F2eSliceChecksum.Campo.valor("hora_fin", "T", "09:15:30.123456"),
                F2eSliceChecksum.Campo.valor("estado", "S", "CONFIRMADA"),
                F2eSliceChecksum.Campo.valor("creado_en", "Z", "2026-09-02T14:00:00.000000Z"),
                F2eSliceChecksum.Campo.valor("actualizado_en", "Z", "2026-09-02T14:05:06.000007Z"));
    }

    private List<F2eSliceChecksum.Campo> camposA0() {
        var campos = new java.util.ArrayList<>(camposA());
        campos.set(0, F2eSliceChecksum.Campo.valor(
                "id", "U", "00000000-0000-0000-0000-000000000000"));
        campos.set(5, F2eSliceChecksum.Campo.valor("fecha", "D", "2026-09-01"));
        campos.set(6, F2eSliceChecksum.Campo.valor("hora_inicio", "T", "07:00:00.000000"));
        campos.set(7, F2eSliceChecksum.Campo.valor("hora_fin", "T", "08:00:00.000000"));
        return List.copyOf(campos);
    }

    private String texto(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void verificarRazon(String sql, F2eSqlPolicyViolationException.Reason razon) {
        F2eStatementPolicyInspector inspector = new F2eStatementPolicyInspector();
        var captura = inspector.abrirCaptura("policy-" + razon.name());
        F2eSqlPolicyViolationException error = assertThrows(
                F2eSqlPolicyViolationException.class, () -> inspector.inspect(sql));
        assertEquals(razon, error.reason());
        inspector.descartarCaptura(captura);
    }
}
