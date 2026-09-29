package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyAssignmentRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnMemberRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnJdbcCapture;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnR2PostgresTestConfiguration;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.LegacyTurnTransactionTestOwner;
import com.feelingpilates.transicion.programacion.read.LegacyAdapterInputInvalid;
import com.feelingpilates.transicion.programacion.read.LegacyAdapterRejection;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadSet;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTurnJpaReaderRuntimeIsolationTest {

    @Test
    void realDefaultAndProdContextsContainNoR2BeansOrProductiveCallers() {
        try (var postgres = new org.testcontainers.containers.PostgreSQLContainer<>(
                org.testcontainers.utility.DockerImageName.parse("postgres:16-alpine"))) {
            postgres.start();
            for (String profile : List.of("default", "prod")) {
                List<String> arguments = new java.util.ArrayList<>(List.of(
                        "--server.port=0", "--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword(),
                        "--logging.level.root=OFF", "--spring.jpa.show-sql=false",
                        "--spring.main.banner-mode=off"));
                if (profile.equals("prod")) arguments.add("--spring.profiles.active=prod");
                try (var context = new org.springframework.boot.builder.SpringApplicationBuilder(
                        com.feelingpilates.FeelingpilatesApplication.class)
                        .registerShutdownHook(false).run(arguments.toArray(String[]::new))) {
                    assertTrue(context.isActive());
                    for (String name : List.of(
                            "f2eR2PostgresContainer", "f2eR2PrivilegedDataSource", "f2eR2ReaderDataSource",
                            "f2eR2StatementPolicyInspector", "f2eR2ReaderEntityManagerFactory",
                            "f2eR2ReaderEntityManager", "f2eR2ReaderTransactionManager",
                            "legacyTurnProjectionQueryExecutor", "legacyTurnProjectionMapper",
                            "legacyTurnJpaReader", "legacyTurnTransactionTestOwner", "legacyTurnJdbcCapture")) {
                        assertFalse(context.containsBean(name), name);
                    }
                    for (Class<?> type : List.of(
                            LegacyTurnJpaReader.class, LegacyTurnProjectionMapper.class,
                            LegacyTurnProjectionQueryExecutor.class, LegacyTurnProjectionCatalog.class,
                            LegacyTurnMemberRow.class, LegacyAssignmentRow.class, LegacyTurnReadPort.class,
                            LegacyTurnReadContext.class, LegacyTurnScope.class, LegacyTurnReadSet.class,
                            LegacyAdapterInputInvalid.class, LegacyAdapterRejection.class,
                            LegacyTurnR2PostgresTestConfiguration.class, LegacyTurnTransactionTestOwner.class,
                            LegacyTurnJdbcCapture.class)) {
                        assertEquals(0, context.getBeanNamesForType(type).length, type.getName());
                    }
                    assertTrue(context.containsBean("turnoInstructorService"));
                    assertTrue(context.containsBean("reservaService"));
                }
            }
        }
    }

    @Test
    void r2PolicyAllowsOnlyItsSixExactStatements() {
        F2eStatementPolicyInspector inspector = F2eStatementPolicyInspector.paraR2(
                LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1);
        var capture = inspector.abrirCaptura("r2-policy");
        for (var statement : LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1.statements().values()) {
            String sql = statement.sql().replace("(:salonIds)", "(?)").replace("(:turnIds)", "(?)")
                    .replace(":active", "?").replace(":recurrentType", "?")
                    .replace(":dayOfWeek", "?").replace(":exceptionType", "?")
                    .replace(":cancellationType", "?").replace(":fecha", "?");
            inspector.inspect(sql);
        }
        assertEquals(6, inspector.cerrarCaptura(capture).size());

        var rejected = inspector.abrirCaptura("r2-policy-reject");
        assertThrows(RuntimeException.class, () -> inspector.inspect("SELECT 1"));
        inspector.descartarCaptura(rejected);
    }
}
