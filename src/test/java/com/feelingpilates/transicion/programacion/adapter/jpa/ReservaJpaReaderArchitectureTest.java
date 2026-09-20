package com.feelingpilates.transicion.programacion.adapter.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservaJpaReaderArchitectureTest {

    @Test
    void readerProductivoPermanecePlainSinStereotypeNiReachabilityProductiva() {
        assertFalse(ReservaJpaReader.class.isAnnotationPresent(Component.class));
        assertFalse(ReservaJpaReader.class.isAnnotationPresent(Service.class));
        assertFalse(ReservaJpaReader.class.isAnnotationPresent(Repository.class));
    }

    @Test
    void codigoProductivoR1NoUsaEscapesDeRecursoNiMutacionJpa() throws Exception {
        Path raiz = Path.of("src/main/java/com/feelingpilates/transicion/programacion");
        String codigo = Files.walk(raiz)
                .filter(path -> path.toString().contains("adapter/jpa") || path.toString().contains("/read/"))
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> {
                    try { return Files.readString(path); }
                    catch (Exception excepcion) { throw new IllegalStateException(excepcion); }
                })
                .reduce("", String::concat);
        for (String prohibido : List.of(
                "DriverManager", "JdbcTemplate", ".getConnection(", ".createEntityManager(",
                "entityManager.persist(", "entityManager.merge(", "entityManager.remove(",
                "entityManager.flush(", "@Configuration", "@Bean")) {
            assertFalse(codigo.contains(prohibido), prohibido);
        }
        assertTrue(codigo.contains("transactionManager = \"f2eReaderTransactionManager\""));
        assertTrue(codigo.contains("propagation = Propagation.MANDATORY"));
    }

    @Test
    void contratoPublicoUsaCatalogoCerradoYTransactionManagerExplicito() throws Exception {
        assertEquals(1, com.feelingpilates.transicion.programacion.read.ReadSnapshotContext
                .ProjectionCatalogVersion.values().length);
        assertEquals(1, com.feelingpilates.transicion.programacion.read.ReadSnapshotContext
                .SnapshotClaim.values().length);
        for (String metodo : List.of("readByReservationIds", "readByScope")) {
            var firma = java.util.Arrays.stream(ReservaJpaReader.class.getMethods())
                    .filter(candidata -> candidata.getName().equals(metodo))
                    .findFirst().orElseThrow();
            Transactional transaccion = firma.getAnnotation(Transactional.class);
            assertEquals("f2eReaderTransactionManager", transaccion.transactionManager());
            assertEquals(Propagation.MANDATORY, transaccion.propagation());
            assertTrue(transaccion.readOnly());
        }
    }

    @Test
    void allowlistFisicaExactaRechazaClaseExtraFaltanteOStereotypeProductivo() throws Exception {
        Set<String> produccionEsperada = Set.of(
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReader.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/ReservaProjectionMapper.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/policy/F2eSqlPolicyViolationException.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionQueryExecutor.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/ReservaProjectionRow.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotContext.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/ReadSnapshotIdentifiers.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadException.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadFailureCode.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/ReservationReadPort.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/ReservationScope.java");
        Set<String> testsEsperados = Set.of(
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderArchitectureTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderPostgreSqlTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderRuntimeIsolationTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaJpaReaderTransactionTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/ReservaProjectionMapperTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2ePostgresTestConfiguration.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSelectOnlyRole.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eSliceChecksum.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/F2eStatementPolicyInspector.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/ReaderTransactionTestHarness.java");
        assertEquals(produccionEsperada, archivosJava(
                Path.of("src/main/java/com/feelingpilates/transicion/programacion/read"),
                Path.of("src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa")));
        assertEquals(testsEsperados, archivosJava(
                Path.of("src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa")));

        for (String archivo : produccionEsperada) {
            String codigo = Files.readString(Path.of(archivo));
            for (String prohibido : List.of(
                    "@Component", "@Service", "@Repository", "@Configuration", "@Bean",
                    ".controller.", ".service.", ".scheduler.", ".listener.")) {
                assertFalse(codigo.contains(prohibido), archivo + ":" + prohibido);
            }
        }
    }

    private Set<String> archivosJava(Path... raices) throws Exception {
        Set<String> encontrados = new java.util.LinkedHashSet<>();
        for (Path raiz : raices) {
            try (var paths = Files.walk(raiz)) {
                encontrados.addAll(paths.filter(path -> path.toString().endsWith(".java"))
                        .map(path -> path.toString().replace('\\', '/'))
                        .collect(Collectors.toSet()));
            }
        }
        return Set.copyOf(encontrados);
    }
}
