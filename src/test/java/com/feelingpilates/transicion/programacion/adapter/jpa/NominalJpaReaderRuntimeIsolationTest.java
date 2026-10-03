package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.read.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NominalJpaReaderRuntimeIsolationTest {

    @Test
    void realDefaultAndProdContextsContainNoR3BeansOrProductiveCallers() {
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
                            "f2eR3PostgresContainer", "f2eR3PrivilegedDataSource", "f2eR3ReaderDataSource",
                            "f2eR3StatementPolicyInspector", "f2eR3ReaderEntityManagerFactory",
                            "f2eR3ReaderEntityManager", "f2eReaderTransactionManager",
                            "nominalProjectionQueryExecutor", "nominalProjectionMapper",
                            "nominalJpaReader", "nominalTransactionTestOwner", "nominalJdbcCapture")) {
                        assertFalse(context.containsBean(name), name);
                    }
                    for (Class<?> type : List.of(
                            NominalJpaReader.class, NominalProjectionMapper.class,
                            NominalProjectionQueryExecutor.class, NominalProjectionCatalog.class,
                            NominalProjectionRow.class, NominalBackingSnapshot.class, NominalProgrammingReadPort.class,
                            NominalReadSnapshotContext.class, NominalReadFailure.class, NominalProgrammingReadSet.class,

                            NominalPostgresTestConfiguration.class, NominalTransactionTestOwner.class,
                            NominalTransactionTestOwner.JdbcCapture.class)) {
                        assertEquals(0, context.getBeanNamesForType(type).length, type.getName());
                    }
                    assertTrue(context.containsBean("turnoInstructorService"));
                    assertTrue(context.containsBean("reservaService"));
                }
            }
        }
    }

}
