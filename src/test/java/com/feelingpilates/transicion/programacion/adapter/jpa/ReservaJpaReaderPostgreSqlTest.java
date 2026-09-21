package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2ePostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSliceChecksum;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.ReaderTransactionTestHarness;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReservationScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(F2ePostgresTestConfiguration.class)
class ReservaJpaReaderPostgreSqlTest {

    @org.springframework.beans.factory.annotation.Autowired
    ReaderTransactionTestHarness harness;

    @org.springframework.beans.factory.annotation.Autowired
    @Qualifier("f2eReaderDataSource")
    DataSource readerDataSource;

    @org.springframework.beans.factory.annotation.Autowired
    @Qualifier("f2ePrivilegedDataSource")
    DataSource privilegedDataSource;

    @org.springframework.beans.factory.annotation.Autowired
    PostgreSQLContainer<?> container;

    @Test
    void leeProjectionNativaPorIdentidadesSinAlterarElSlice() {
        Set<java.util.UUID> ids = Set.of(F2ePostgresTestConfiguration.RESERVA_UNO);
        F2eSliceChecksum.Resultado antes = F2eSliceChecksum.calcularPorIdentidades(privilegedDataSource, ids);
        var resultado = harness.inSingleStatementReadOnly(semilla("ids-ok"), ids);
        F2eSliceChecksum.Resultado despues = F2eSliceChecksum.calcularPorIdentidades(privilegedDataSource, ids);

        assertEquals(antes, despues);
        evidenciaNoMutacion("GAP1_IDS", antes, despues, resultado);
        assertEquals(1, resultado.size());
        assertEquals(F2ePostgresTestConfiguration.RESERVA_UNO, resultado.getFirst().reservationId());
        assertEquals("CONFIRMADA", resultado.getFirst().state().name());
        assertTrue(resultado.getFirst().historicalProgrammingTarget().isEmpty());
        assertEquals(32, resultado.getFirst().provenance().normalizedFields().size());
        assertFalse(resultado.getFirst().provenance().normalizedFields().containsKey("cliente_id"));
    }

    @Test
    void leePorScopeAcotadoYDevuelveOrdenInmutable() {
        var uno = harness.inSingleStatementReadOnly(
                semilla("scope-discovery"), Set.of(F2ePostgresTestConfiguration.RESERVA_UNO));
        var fila = uno.getFirst();
        ReservationScope scope = new ReservationScope(
                Set.of(fila.salonId()), fila.date(), fila.date().plusDays(1));
        var congeladas = F2eSliceChecksum.congelarPorScope(privilegedDataSource, scope);
        var antes = F2eSliceChecksum.calcularPorScopeCongelado(privilegedDataSource, scope, congeladas);
        assertEquals(Set.of(F2ePostgresTestConfiguration.RESERVA_UNO,
                F2ePostgresTestConfiguration.RESERVA_DOS), congeladas);
        var resultado = harness.inSingleStatementReadOnly(semilla("scope-ok"), scope);
        var despues = F2eSliceChecksum.calcularPorScopeCongelado(privilegedDataSource, scope, congeladas);
        assertEquals(antes, despues);
        assertEquals(congeladas, F2eSliceChecksum.congelarPorScope(privilegedDataSource, scope));
        assertEquals(congeladas, resultado.stream().map(r -> r.reservationId()).collect(java.util.stream.Collectors.toSet()));
        evidenciaNoMutacion("GAP2_BY_SCOPE", antes, despues, resultado);
        assertEquals(2, resultado.size());
        assertEquals(F2ePostgresTestConfiguration.RESERVA_UNO, resultado.get(0).reservationId());
        assertEquals(F2ePostgresTestConfiguration.RESERVA_DOS, resultado.get(1).reservationId());
        assertThrows(UnsupportedOperationException.class, () -> resultado.add(fila));
    }

    @Test
    void principalLectorPuedeSeleccionarReservaPeroInsertEsDenegado() throws Exception {
        Set<java.util.UUID> ids = Set.of(F2ePostgresTestConfiguration.RESERVA_UNO);
        var antes = F2eSliceChecksum.calcularPorIdentidades(privilegedDataSource, ids);
        Set<java.util.UUID> insertIds = Set.of(java.util.UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff"));
        var antesInsert = F2eSliceChecksum.calcularPorIdentidades(privilegedDataSource, insertIds);
        assertEquals(0, antesInsert.filas());
        SQLException error = assertThrows(SQLException.class, () -> {
            try (var conexion = readerDataSource.getConnection(); var sentencia = conexion.createStatement()) {
                conexion.setAutoCommit(false);
                try {
                    sentencia.executeUpdate("INSERT INTO public.reserva (id) VALUES "
                            + "('ffffffff-ffff-4fff-8fff-ffffffffffff')");
                } finally {
                    conexion.rollback();
                }
            }
        });
        assertEquals("42501", error.getSQLState());
        var resultado = harness.inSingleStatementReadOnly(semilla("denied-then-clean-read"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO));
        var despues = F2eSliceChecksum.calcularPorIdentidades(privilegedDataSource, ids);
        var despuesInsert = F2eSliceChecksum.calcularPorIdentidades(privilegedDataSource, insertIds);
        assertEquals(antes, despues);
        assertEquals(antesInsert, despuesInsert);
        assertEquals(1, resultado.size());
        System.out.println("F2E GAP1 PostgreSQL INSERT DENIED_ONLY SQLState=" + error.getSQLState());
        evidenciaNoMutacion("GAP1_DENIAL_THEN_READ", antes, despues, resultado);
        evidenciaNoMutacion("GAP1_INSERT_TARGET", antesInsert, despuesInsert, resultado);
    }

    @Test
    void grantsDelPrincipalSonExactamenteLecturaDeReservaSinCreateTempDmlSecuenciaOFuncion() throws Exception {
        String principal;
        String database;
        try (var conexion = readerDataSource.getConnection()) {
            principal = conexion.getMetaData().getUserName();
            database = conexion.getCatalog();
        }
        try (var conexion = privilegedDataSource.getConnection()) {
            assertTrue(privilegio(conexion,
                    "SELECT has_database_privilege(?, ?, 'CONNECT')", principal, database));
            assertFalse(privilegio(conexion,
                    "SELECT has_database_privilege(?, ?, 'TEMP')", principal, database));
            assertTrue(privilegio(conexion,
                    "SELECT has_schema_privilege(?, 'public', 'USAGE')", principal));
            assertFalse(privilegio(conexion,
                    "SELECT has_schema_privilege(?, 'public', 'CREATE')", principal));
            assertTrue(privilegio(conexion,
                    "SELECT has_table_privilege(?, 'public.reserva', 'SELECT')", principal));
            for (String privilegio : java.util.List.of("INSERT", "UPDATE", "DELETE", "TRUNCATE", "REFERENCES", "TRIGGER")) {
                assertFalse(privilegio(conexion,
                        "SELECT has_table_privilege(?, 'public.reserva', '" + privilegio + "')", principal));
            }
            assertFalse(privilegio(conexion,
                    "SELECT COALESCE(bool_or(has_sequence_privilege(?, c.oid, 'USAGE')), false) "
                            + "FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace "
                            + "WHERE n.nspname='public' AND c.relkind='S'", principal));
            assertFalse(privilegio(conexion,
                    "SELECT COALESCE(bool_or(has_function_privilege(?, p.oid, 'EXECUTE')), false) "
                            + "FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace "
                            + "WHERE n.nspname='public'", principal));
        }
    }

    @Test
    void hostRealCumpleImagenRedFlywayV46YJpaValidate() throws Exception {
        assertTrue(container.isRunning());
        assertEquals("postgres:16-alpine", container.getDockerImageName());
        assertTrue(container.getMappedPort(5432) > 0);
        try (var conexion = privilegedDataSource.getConnection();
             var consulta = conexion.prepareStatement(
                     "SELECT count(*), count(*) FILTER (WHERE success), "
                             + "(SELECT version FROM public.flyway_schema_history "
                             + "WHERE success AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1) "
                             + "FROM public.flyway_schema_history")) {
            try (var resultado = consulta.executeQuery()) {
                assertTrue(resultado.next());
                assertEquals(49, resultado.getInt(1));
                assertEquals(49, resultado.getInt(2));
                assertEquals("46", resultado.getString(3));
            }
        }
    }

    private boolean privilegio(java.sql.Connection conexion, String sql, String... parametros) throws Exception {
        try (var consulta = conexion.prepareStatement(sql)) {
            for (int indice = 0; indice < parametros.length; indice++) consulta.setString(indice + 1, parametros[indice]);
            try (var resultado = consulta.executeQuery()) {
                assertTrue(resultado.next());
                return resultado.getBoolean(1);
            }
        }
    }

    @Test
    void huellaInstaladaEsEstableYCoincideConProvenanceReal() {
        String estable = F2ePostgresTestConfiguration.calcularHuellaEsquema(privilegedDataSource);
        assertEquals(estable, F2ePostgresTestConfiguration.calcularHuellaEsquema(privilegedDataSource));
        assertTrue(estable.matches("sha256:[0-9a-f]{64}"));
        var lectura = harness.inSingleStatementReadOnly(semilla("fingerprint-installed"),
                Set.of(F2ePostgresTestConfiguration.RESERVA_UNO));
        assertEquals(estable, lectura.getFirst().provenance().schemaFingerprint());
        assertEquals(estable, F2ePostgresTestConfiguration.calcularHuellaEsquema(privilegedDataSource));
        System.out.println("F2E GAP3 live schema+Flyway V46 fingerprint=" + estable + " stable=true metadata=SELECT_ONLY");
    }

    @Test
    void seamPuroDetectaSchemaFlywayNullYEstadoConOrdenEstableSinJdbc() throws Exception {
        var componer = F2ePostgresTestConfiguration.class.getDeclaredMethod("componerHuellaMetadatos", java.util.List.class);
        componer.setAccessible(true);
        var base = metadatosSinteticos("text", "123", "true", null);
        String huella = (String) componer.invoke(null, base);
        assertEquals(huella, componer.invoke(null, metadatosSinteticos("text", "123", "true", null)));
        java.util.Collections.reverse(base.get(1));
        java.util.Collections.reverse(base.get(9));
        assertEquals(huella, componer.invoke(null, base));
        for (var cambio : java.util.List.of(
                metadatosSinteticos("varchar(20)", "123", "true", null),
                metadatosSinteticos("text", "124", "true", null),
                metadatosSinteticos("text", "123", "false", null),
                metadatosSinteticos("text", "123", "true", ""),
                metadatosSinteticos("text", null, "true", null))) {
            org.junit.jupiter.api.Assertions.assertNotEquals(huella, componer.invoke(null, cambio));
        }
        System.out.println("F2E GAP3 synthetic schema/checksum/state/null sensitivity=true orderingStable=true seamJdbc=0 successfulDbMutation=0");
    }

    private java.util.List<java.util.List<java.util.List<String>>> metadatosSinteticos(
            String tipo, String checksum, String success, String defecto) {
        java.util.List<java.util.List<java.util.List<String>>> catalogos = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) catalogos.add(new java.util.ArrayList<>());
        catalogos.get(1).add(java.util.Arrays.asList("reserva", "estado", "2", tipo, "true", defecto, "", ""));
        catalogos.get(1).add(java.util.List.of("reserva", "id", "1", "uuid", "true", "", "", ""));
        catalogos.get(9).add(java.util.Arrays.asList("46", "V46__fixture.sql", checksum, success));
        catalogos.get(9).add(java.util.Arrays.asList(null, "R__fixture.sql", null, "true"));
        return catalogos;
    }

    private void evidenciaNoMutacion(String caso, F2eSliceChecksum.Resultado antes,
            F2eSliceChecksum.Resultado despues,
            java.util.List<com.feelingpilates.transicion.programacion.detector.ReservationSourceSnapshot> resultado) {
        System.out.println("F2E " + caso + " BEFORE=" + antes + " AFTER=" + despues
                + " hashCountEqual=" + antes.equals(despues) + " statementObservationFingerprint="
                + resultado.getFirst().provenance().normalizedFields().get("statementObservationFingerprint"));
    }

    private ReaderTransactionTestHarness.SemillaLectura semilla(String sufijo) {
        return new ReaderTransactionTestHarness.SemillaLectura(
                "run-" + sufijo, "attempt-01", "boundary-" + sufijo, "invocation-" + sufijo,
                "F2D-RULE-CATALOG/V1", ZoneId.of("America/Mexico_City"),
                ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST);
    }
}
