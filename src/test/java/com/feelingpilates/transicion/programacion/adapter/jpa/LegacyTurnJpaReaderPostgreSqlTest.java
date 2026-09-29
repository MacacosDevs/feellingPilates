package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSliceChecksum;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnR2PostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnTransactionTestOwner;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(LegacyTurnR2PostgresTestConfiguration.class)
class LegacyTurnJpaReaderPostgreSqlTest {

    @Autowired
    @Qualifier("legacyTurnTransactionTestOwner")
    LegacyTurnTransactionTestOwner owner;

    @Autowired
    @Qualifier("f2eR2PrivilegedDataSource")
    DataSource privileged;

    @Autowired
    @Qualifier("f2eR2ReaderDataSource")
    DataSource readerDataSource;

    @Autowired
    LegacyTurnR2PostgresTestConfiguration configuration;

    @Autowired
    PostgreSQLContainer<?> container;

    @Test
    void postgres16FlywayV1ToV46AndNoV47ArePhysicalFacts() throws Exception {
        assertTrue(container.getDockerImageName().startsWith("postgres:16-alpine"));
        assertEquals("46", configuration.flywayHead());
        assertEquals(49, configuration.appliedMigrations());
        try (var connection = privileged.getConnection();
             var statement = connection.prepareStatement(
                     "SELECT count(*) FROM public.flyway_schema_history WHERE version='47'");
             var rows = statement.executeQuery()) {
            assertTrue(rows.next());
            assertEquals(0, rows.getInt(1));
        }
    }

    @Test
    void selectOnlyRoleHasExactlyThreeLegacyTableSelectGrants() throws Exception {
        try (var connection = privileged.getConnection();
             var statement = connection.prepareStatement(
                     "SELECT table_name,privilege_type FROM information_schema.role_table_grants "
                             + "WHERE grantee=? ORDER BY table_name,privilege_type")) {
            statement.setString(1, configuration.readerPrincipal());
            try (var rows = statement.executeQuery()) {
                java.util.ArrayList<String> grants = new java.util.ArrayList<>();
                while (rows.next()) grants.add(rows.getString(1) + ":" + rows.getString(2));
                assertEquals(java.util.List.of(
                        "turno_instructor:SELECT",
                        "turno_instructor_asignacion:SELECT",
                        "turno_instructor_usuario:SELECT"), grants);
            }
        }
    }

    @Test
    void insertUpdateDeleteAndDdlAreDeniedWithSqlState42501() {
        assertDenied("INSERT INTO public.turno_instructor(id) VALUES (gen_random_uuid())");
        assertDenied("UPDATE public.turno_instructor SET activo=false WHERE false");
        assertDenied("DELETE FROM public.turno_instructor WHERE false");
        assertDenied("CREATE TABLE public.r2_forbidden_probe(id integer)");
    }

    @Test
    void beforeAndAfterThreeTableSliceChecksumsAreIdentical() {
        LegacyTurnScope scope = new LegacyTurnScope(
                Set.of(configuration.salonId()), LegacyTurnR2PostgresTestConfiguration.FECHA);
        Set<java.util.UUID> frozen = F2eSliceChecksum.congelarTurnosPorScope(privileged, scope);
        var before = F2eSliceChecksum.calcularTurnosCongelado(privileged, scope, frozen);
        var outcome = owner.inRepeatableReadOnly(new LegacyTurnTransactionTestOwner.Seed(
                "run-r2-checksum", "attempt-before-after", "rules-v1",
                ZoneId.of("America/Mexico_City")), scope);
        var after = F2eSliceChecksum.calcularTurnosCongelado(privileged, scope, frozen);

        assertEquals(frozen, F2eSliceChecksum.congelarTurnosPorScope(privileged, scope));
        assertEquals(before, after);
        assertEquals(Set.of("public.turno_instructor", "public.turno_instructor_usuario",
                "public.turno_instructor_asignacion"), before.tableHashes().keySet());
        assertEquals(3, before.rowCounts().get("public.turno_instructor"));
        assertFalse(outcome.readSet().sources().isEmpty());
        assertEquals(outcome.snapshotInitial(), outcome.snapshotFinal());
    }

    private void assertDenied(String sql) {
        try (var connection = readerDataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute(sql);
            throw new AssertionError("R2 SELECT-only role unexpectedly accepted write: " + sql);
        } catch (SQLException expected) {
            assertEquals("42501", expected.getSQLState(), sql);
        }
    }
}
