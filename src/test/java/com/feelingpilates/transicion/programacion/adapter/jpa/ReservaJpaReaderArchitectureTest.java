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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Set<String> r1Main = Set.of(
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
        Set<String> r1Test = Set.of(
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
        Set<String> r2Main = Set.of(
                "src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadPort.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnScope.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadContext.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/LegacyTurnReadSet.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/LegacyAdapterRejection.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/LegacyAdapterInputInvalid.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnProjectionCatalog.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnMemberRow.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyAssignmentRow.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/LegacyTurnProjectionQueryExecutor.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/LegacyTurnProjectionMapper.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReader.java");
        Set<String> r2Test = Set.of(
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionMapperTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnProjectionQueryExecutorTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderPostgreSqlTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderTransactionTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderConcurrencyTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnJpaReaderRuntimeIsolationTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/LegacyTurnR2ArchitectureTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnR2PostgresTestConfiguration.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnTransactionTestOwner.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/LegacyTurnJdbcCapture.java");
        Set<String> r3Main = Set.of(
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReader.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/mapper/NominalProjectionMapper.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionCatalog.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionQueryExecutor.java",
                "src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa/projection/NominalProjectionRow.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/NominalBackingSnapshot.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadPort.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/NominalProgrammingReadSet.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadFailure.java",
                "src/main/java/com/feelingpilates/transicion/programacion/read/NominalReadSnapshotContext.java");
        Set<String> r3Test = Set.of(
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderArchitectureTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderPostgreSqlTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderRuntimeIsolationTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalJpaReaderTransactionTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionMapperTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/NominalProjectionQueryExecutorTest.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/NominalPostgresTestConfiguration.java",
                "src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa/testinfra/NominalTransactionTestOwner.java");
        assertTrue(java.util.Collections.disjoint(r1Main, r3Main));
        assertTrue(java.util.Collections.disjoint(r2Main, r3Main));
        assertTrue(java.util.Collections.disjoint(r1Test, r3Test));
        assertTrue(java.util.Collections.disjoint(r2Test, r3Test));
        assertTrue(java.util.Collections.disjoint(r1Main, r2Main));
        assertTrue(java.util.Collections.disjoint(r1Test, r2Test));
        Set<String> actualMain = archivosJava(
                Path.of("src/main/java/com/feelingpilates/transicion/programacion/read"),
                Path.of("src/main/java/com/feelingpilates/transicion/programacion/adapter/jpa"));
        Set<String> actualTest = archivosJava(
                Path.of("src/test/java/com/feelingpilates/transicion/programacion/adapter/jpa"));
        // R3 is a separate dark-launch lifecycle: absent on the predecessor base,
        // or present as its entire frozen inventory. Partial or unknown classes fail.
        verificarInventario(r1Main, r2Main, r3Main, r1Test, r2Test, r3Test, actualMain, actualTest);
        Set<String> baseMain = union(r1Main, r2Main);
        Set<String> baseTest = union(r1Test, r2Test);
        Set<String> fullMain = union(baseMain, r3Main);
        Set<String> fullTest = union(baseTest, r3Test);
        verificarInventario(r1Main, r2Main, r3Main, r1Test, r2Test, r3Test, baseMain, baseTest);
        verificarInventario(r1Main, r2Main, r3Main, r1Test, r2Test, r3Test, fullMain, fullTest);
        assertThrows(AssertionError.class, () -> verificarInventario(
                r1Main, r2Main, r3Main, r1Test, r2Test, r3Test,
                union(baseMain, Set.of(r3Main.iterator().next())), baseTest));
        assertThrows(AssertionError.class, () -> verificarInventario(
                r1Main, r2Main, r3Main, r1Test, r2Test, r3Test, fullMain, baseTest));
        assertThrows(AssertionError.class, () -> verificarInventario(
                r1Main, r2Main, r3Main, r1Test, r2Test, r3Test, baseMain, fullTest));
        assertThrows(AssertionError.class, () -> verificarInventario(
                r1Main, r2Main, r3Main, r1Test, r2Test, r3Test,
                union(fullMain, Set.of("src/main/java/UnapprovedR4Reader.java")), fullTest));
        Set<String> missingR1 = new java.util.HashSet<>(fullMain);
        missingR1.remove(r1Main.iterator().next());
        assertThrows(AssertionError.class, () -> verificarInventario(
                r1Main, r2Main, r3Main, r1Test, r2Test, r3Test, missingR1, fullTest));

        for (String archivo : actualMain) {
            String codigo = Files.readString(Path.of(archivo));
            for (String prohibido : List.of(
                    "@Component", "@Service", "@Repository", "@Configuration", "@Bean",
                    ".controller.", ".service.", ".scheduler.", ".listener.")) {
                assertFalse(codigo.contains(prohibido), archivo + ":" + prohibido);
            }
        }
    }

    private void verificarInventario(
            Set<String> r1Main, Set<String> r2Main, Set<String> r3Main,
            Set<String> r1Test, Set<String> r2Test, Set<String> r3Test,
            Set<String> actualMain, Set<String> actualTest) {
        boolean r3Present = !java.util.Collections.disjoint(actualMain, r3Main)
                || !java.util.Collections.disjoint(actualTest, r3Test);
        assertEquals(union(union(r1Main, r2Main), r3Present ? r3Main : Set.of()), actualMain);
        assertEquals(union(union(r1Test, r2Test), r3Present ? r3Test : Set.of()), actualTest);
        assertEquals(r1Main, intersection(actualMain, r1Main));
        assertEquals(r1Test, intersection(actualTest, r1Test));
        assertEquals(r2Main, intersection(actualMain, r2Main));
        assertEquals(r2Test, intersection(actualTest, r2Test));
        if (r3Present) {
            assertEquals(r3Main, intersection(actualMain, r3Main));
            assertEquals(r3Test, intersection(actualTest, r3Test));
        }
    }

    private Set<String> union(Set<String> left, Set<String> right) {
        Set<String> result = new java.util.LinkedHashSet<>(left);
        result.addAll(right);
        return Set.copyOf(result);
    }

    private Set<String> intersection(Set<String> left, Set<String> right) {
        Set<String> result = new java.util.LinkedHashSet<>(left);
        result.retainAll(right);
        return Set.copyOf(result);
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
