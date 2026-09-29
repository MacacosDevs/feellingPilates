package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTurnR2ArchitectureTest {

    @Test
    void readerIsPlainAndItsOnlyPortMethodIsMandatoryReadOnly() throws Exception {
        assertFalse(LegacyTurnJpaReader.class.isAnnotationPresent(Component.class));
        assertFalse(LegacyTurnJpaReader.class.isAnnotationPresent(Service.class));
        assertFalse(LegacyTurnJpaReader.class.isAnnotationPresent(Repository.class));
        assertEquals(List.of("readForDate"), java.util.Arrays.stream(LegacyTurnReadPort.class.getDeclaredMethods())
                .map(java.lang.reflect.Method::getName).toList());
        Transactional transactional = LegacyTurnJpaReader.class
                .getMethod("readForDate", LegacyTurnReadContext.class,
                        com.feelingpilates.transicion.programacion.read.LegacyTurnScope.class)
                .getAnnotation(Transactional.class);
        assertEquals("f2eR2ReaderTransactionManager", transactional.transactionManager());
        assertEquals(Propagation.MANDATORY, transactional.propagation());
        assertTrue(transactional.readOnly());
    }

    @Test
    void readContractsRemainPersistenceAgnosticAndCatalogsAreClosed() throws Exception {
        Path root = Path.of("src/main/java/com/feelingpilates/transicion/programacion/read");
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(candidate -> candidate.toString().endsWith(".java")).toList()) {
                String code = Files.readString(path);
                assertFalse(code.contains("jakarta.persistence"), path.toString());
                assertFalse(code.contains("org.hibernate"), path.toString());
                assertFalse(code.contains("org.springframework"), path.toString());
            }
        }
        assertEquals(1, LegacyTurnReadContext.ProjectionCatalogVersion.values().length);
        assertEquals(1, LegacyTurnReadContext.SnapshotClaim.values().length);
        assertEquals(6, LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1.statements().size());
    }

    @Test
    void productionR2HasNoWriterControllerPaymentNotificationOrCutoverDependency() throws Exception {
        List<Path> files;
        try (var paths = Files.walk(Path.of("src/main/java/com/feelingpilates/transicion/programacion"))) {
            files = paths.filter(path -> path.toString().contains("Legacy"))
                    .filter(path -> path.toString().endsWith(".java")).toList();
        }
        String code = files.stream().map(path -> {
            try { return Files.readString(path); }
            catch (Exception exception) { throw new IllegalStateException(exception); }
        }).reduce("", String::concat);
        for (String forbidden : List.of(
                "entityManager.persist(", "entityManager.merge(", "entityManager.remove(",
                "entityManager.flush(", ".controlador.", ".servicio.", ".pagos.",
                ".notificaciones.", "AjusteProgramacionFecha", "@Component", "@Service",
                "@Repository", "@Configuration", "@Bean", "REQUIRES_NEW")) {
            assertFalse(code.contains(forbidden), forbidden);
        }
    }
}
