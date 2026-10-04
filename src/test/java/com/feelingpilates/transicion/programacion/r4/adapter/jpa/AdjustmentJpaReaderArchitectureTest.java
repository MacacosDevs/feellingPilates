package com.feelingpilates.transicion.programacion.r4.adapter.jpa;
import com.feelingpilates.transicion.programacion.r4.read.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AdjustmentJpaReaderArchitectureTest {
    static final Set<String> PATHS=Set.of(
        "src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReader.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/mapper/AdjustmentProjectionMapper.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/projection/AdjustmentProjectionCatalog.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/projection/AdjustmentProjectionQueryExecutor.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/projection/AdjustmentProjectionRow.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentBackingSnapshot.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadFailure.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadPort.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSet.java",
        "src/main/java/com/feelingpilates/transicion/programacion/r4/read/AdjustmentReadSnapshotContext.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderArchitectureTest.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderPostgreSqlTest.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderRuntimeIsolationTest.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentJpaReaderTransactionTest.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionMapperTest.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/AdjustmentProjectionQueryExecutorTest.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentJdbcCapture.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentPostgresTestConfiguration.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentSliceChecksum.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentStatementPolicyInspector.java",
        "src/test/java/com/feelingpilates/transicion/programacion/r4/adapter/jpa/testinfra/AdjustmentTransactionTestOwner.java");
    // Only the ten frozen R5 production paths may consume these two immutable input contracts.
    private static final Set<String> R5_DTO_CONSUMERS = Set.of(
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveProgrammingComposer.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionInput.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionEnvelope.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveValidityEvidence.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveProgrammingCompositionResult.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionBacking.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionOmission.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionSuppression.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionFailure.java",
            "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionCanonicalizer.java");
    private static final Set<String> R5_INPUT_DTOS = Set.of("AdjustmentReadSet", "AdjustmentBackingSnapshot");
    @Test void exactTenMainElevenTestInventoryWithExtraMissingNegativeControls() throws Exception {
        Set<String> actual=new HashSet<>();
        for(String prefix:List.of("src/main/java/","src/test/java/")) {
            try(var files=Files.walk(Path.of(prefix+"com/feelingpilates/transicion/programacion/r4"))) {
                files.filter(p->p.toString().endsWith(".java")).forEach(p->actual.add(p.toString()));
            }
        }
        inventory(actual);assertEquals(10,PATHS.stream().filter(p->p.startsWith("src/main/")).count());
        var extra=new HashSet<>(actual);extra.add("src/main/java/com/feelingpilates/transicion/programacion/r4/Extra.java");
        assertThrows(AssertionError.class,()->inventory(extra));var missing=new HashSet<>(actual);missing.remove(actual.iterator().next());
        assertThrows(AssertionError.class,()->inventory(missing));
    }
    static void inventory(Set<String> actual) { assertEquals(PATHS,actual); }
    @Test void domainEdgesPlainClassesAndNoProductiveCaller() throws Exception {
        var names=PATHS.stream().filter(p->p.startsWith("src/main/")).map(p->Path.of(p).getFileName().toString().replace(".java","")).toList();
        try(var files=Files.walk(Path.of("src/main/java"))) {
            for(Path p:files.filter(p->p.toString().endsWith(".java")).toList()) {
                String code=Files.readString(p);
                if(PATHS.contains(p.toString())) {
                    checkEdges(code,p.toString().contains("/read/"));
                } else checkExternalCaller(p, code, new HashSet<>(names));
            }
        }
        assertEquals(1,AdjustmentReadPort.class.getDeclaredMethods().length);
        assertEquals(1,AdjustmentReadSnapshotContext.SnapshotClaim.values().length);
        assertEquals(1,AdjustmentReadSnapshotContext.ProjectionCatalogVersion.values().length);
    }
    static void checkEdges(String code,boolean domain) {
        for(String forbidden:List.of("@Component","@Service","@Repository","@Configuration","@Bean","@Scheduled",
                "ProgrammingCandidateSnapshot","NominalJpaReader","LegacyTurnJpaReader","ReservaJpaReader","JpaRepository",
                ".persist(",".merge(",".remove(",".flush(","DriverManager","JdbcTemplate",".getConnection(",".createEntityManager(",
                ".servicio.",".controlador.",".repositorio.",".pagos.",".notificaciones.")) assertFalse(code.contains(forbidden),forbidden);
        if(domain) for(String forbidden:List.of("import jakarta.persistence","import org.hibernate","import org.springframework","import java.sql",".adapter."))
            assertFalse(code.contains(forbidden),forbidden);
    }
    @Test void negativeStereotypeAndCallerControls() {
        for(String forbidden:List.of("@Component","@Service","@Repository","ProgrammingCandidateSnapshot",".flush("))
            assertThrows(AssertionError.class,()->checkEdges(forbidden,false));
        assertThrows(AssertionError.class,()->checkEdges("import jakarta.persistence.EntityManager;",true));
    }
    @Test void unknownSqlWritesLocksAndMultiStatementAreRejected() {
        var inspector=new AdjustmentStatementPolicyInspector();
        for(var s:AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.statements()) assertEquals(s.positionalSql(),inspector.inspect(s.positionalSql()));
        for(String sql:List.of("SELECT 1","SELECT * FROM programacion_ajuste_fecha","SELECT pg_current_snapshot()::text FOR UPDATE",
                "UPDATE programacion_ajuste_fecha SET activo=false","DELETE FROM programacion_ajuste_fecha","SELECT pg_current_snapshot()::text; SELECT 1"))
            assertThrows(RuntimeException.class,()->inspector.inspect(sql));
    }
    private static void checkExternalCaller(Path path, String code, Set<String> names) {
        Set<String> forbidden = new HashSet<>(names);
        if (R5_DTO_CONSUMERS.contains(path.toString())) {
            checkPureR5DtoConsumer(code);
            forbidden.removeAll(R5_INPUT_DTOS);
        }
        for (String name : forbidden)
            assertFalse(java.util.regex.Pattern.compile("\\b" + name + "\\b").matcher(code).find(), path + ":" + name);
    }
    private static void checkPureR5DtoConsumer(String code) {
        assertTrue(java.util.regex.Pattern.compile(
                "(?m)^\\s*package\\s+com\\.feelingpilates\\.transicion\\.programacion\\.r5\\.composition\\s*;")
                .matcher(code).find(), "exact R5 composition package required");
        for (String forbidden : List.of("org.springframework", "jakarta.persistence", "javax.persistence",
                "org.hibernate", "java.sql", "javax.sql", ".adapter.", ".repositorio.", ".entidad.",
                ".servicio.", ".controlador.", ".read.*", "Transactional", "JdbcTemplate", "DriverManager",
                "EntityManager", "DataSource", "TransactionTemplate", "JpaRepository"))
            assertFalse(code.contains(forbidden), "R5 DTO exception is pure only: " + forbidden);
    }
    @Test void r5ExceptionIsExactDtoOnlyAndDefaultDeny() {
        Set<String> names = PATHS.stream().filter(p -> p.startsWith("src/main/")).map(p -> Path.of(p).getFileName().toString().replace(".java", "")).collect(java.util.stream.Collectors.toSet());
        String packageLine = "package com.feelingpilates.transicion.programacion.r5.composition;\n";
        for (String consumer : R5_DTO_CONSUMERS) {
            for (String dto : R5_INPUT_DTOS)
                checkExternalCaller(Path.of(consumer), packageLine + "class Fixture { " + dto + " input; }", names);
            for (String name : names) if (!R5_INPUT_DTOS.contains(name))
                assertThrows(AssertionError.class, () -> checkExternalCaller(Path.of(consumer),
                        packageLine + "class Fixture { " + name + " dependency; }", names));
        }
        Path allowed = Path.of("src/main/java/com/feelingpilates/transicion/programacion/r5/composition/EffectiveCompositionInput.java");
        for (String path : List.of("src/main/java/com/feelingpilates/transicion/programacion/r5/composition/Extra.java",
                "src/main/java/com/feelingpilates/transicion/programacion/r5/composition/nested/EffectiveCompositionInput.java",
                "src/main/java/com/feelingpilates/transicion/programacion/r6/composition/EffectiveCompositionInput.java",
                "src/main/java/com/feelingpilates/productivo/EffectiveCompositionInput.java"))
            for (String dto : R5_INPUT_DTOS)
                assertThrows(AssertionError.class, () -> checkExternalCaller(Path.of(path),
                        packageLine + "class Fixture { " + dto + " input; }", names));
        assertThrows(AssertionError.class, () -> checkExternalCaller(allowed,
                "package com.feelingpilates.productivo; class Fixture {}", names));
        for (String forbidden : List.of("org.springframework", "jakarta.persistence", "javax.persistence",
                "org.hibernate", "java.sql", "javax.sql", ".adapter.", ".repositorio.", ".entidad.",
                ".servicio.", ".controlador.", ".read.*", "Transactional", "JdbcTemplate", "DriverManager",
                "EntityManager", "DataSource", "TransactionTemplate", "JpaRepository"))
            assertThrows(AssertionError.class, () -> checkExternalCaller(allowed, packageLine + forbidden, names));
    }
}
