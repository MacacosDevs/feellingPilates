package com.feelingpilates.transicion.programacion.r4.adapter.jpa;

import com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper.AdjustmentProjectionMapper;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdjustmentJpaReaderRuntimeIsolationTest {

    @Test
    void realDefaultAndProdContextsContainNoR4BeansOrProductiveCallers() {
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
                    for (String name : List.of("r4Container","r4Admin","r4DS","r4EMF","r4EM",
                            "f2eReaderTransactionManager","adjustmentReader","adjustmentInnerOwner","adjustmentOwner")) {
                        assertFalse(context.containsBean(name),name);
                    }
                    for (Class<?> type : List.of(
                            AdjustmentJpaReader.class, AdjustmentProjectionMapper.class,
                            AdjustmentProjectionQueryExecutor.class, AdjustmentProjectionCatalog.class,
                            AdjustmentProjectionRow.class, AdjustmentBackingSnapshot.class, AdjustmentReadPort.class,
                            AdjustmentReadSnapshotContext.class, AdjustmentReadFailure.class, AdjustmentReadSet.class,

                            AdjustmentPostgresTestConfiguration.class, AdjustmentTransactionTestOwner.class,
                            AdjustmentJdbcCapture.class)) {
                        assertEquals(0, context.getBeanNamesForType(type).length, type.getName());
                    }
                    assertTrue(context.containsBean("turnoInstructorService"));
                    assertTrue(context.containsBean("reservaService"));
                }
            }
        }
    }

}
