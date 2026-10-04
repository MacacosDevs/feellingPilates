package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.NominalProjectionCatalog;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eStatementPolicyInspector;
import com.feelingpilates.transicion.programacion.read.NominalProgrammingReadPort;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import static org.junit.jupiter.api.Assertions.*;

class NominalJpaReaderArchitectureTest {
    private static final Set<String> MAIN = Set.of(
            "read/NominalProgrammingReadPort.java", "read/NominalReadSnapshotContext.java",
            "read/NominalProgrammingReadSet.java", "read/NominalBackingSnapshot.java", "read/NominalReadFailure.java",
            "adapter/jpa/NominalJpaReader.java", "adapter/jpa/mapper/NominalProjectionMapper.java",
            "adapter/jpa/projection/NominalProjectionCatalog.java", "adapter/jpa/projection/NominalProjectionQueryExecutor.java",
            "adapter/jpa/projection/NominalProjectionRow.java");
    private static final Set<String> FORBIDDEN = Set.of(
            "Component", "Service", "Repository", "Configuration", "Bean", "Scheduled", "EventListener",
            "ReservaJpaReader", "ReservationReadPort", "ReservationReadException", "ReadSnapshotContext",
            "LegacyTurnJpaReader", "LegacyTurnReadPort", "LegacyTurnReadSet", "LegacyTurnReadContext",
            "LegacyAdapterRejection", "AsignacionRepository", "BloqueProgramacion", "Asignacion",
            "AjusteProgramacionFecha", "DetectorReadCoordinator", "MULTI_READER_MVCC");
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
    private static final Set<String> R5_INPUT_DTOS = Set.of("NominalProgrammingReadSet", "NominalBackingSnapshot");
    @Test void exactR3InventoryAndPackageEdgesNoProductiveCallers() throws Exception {
        Path root=Path.of("src/main/java/com/feelingpilates/transicion/programacion");
        Set<String> actual=new HashSet<>();
        try(var paths=Files.walk(root)) {
            for(Path p:paths.filter(p->p.getFileName().toString().startsWith("Nominal")&&p.toString().endsWith(".java")).toList())
                actual.add(root.relativize(p).toString());
        }
        assertEquals(MAIN,actual);
        Set<String> names=new HashSet<>();for(String p:MAIN) names.add(Path.of(p).getFileName().toString().replace(".java",""));
        try(var paths=Files.walk(Path.of("src/main/java"))) {
            for(Path p:paths.filter(p->p.toString().endsWith(".java")).toList()) {
                String code=Files.readString(p);Set<String> symbols=symbols(code);
                if (p.startsWith(root)&&MAIN.contains(root.relativize(p).toString())) {
                    assertTrue(Collections.disjoint(symbols,FORBIDDEN),p.toString());
                    for(String prefix:List.of(".controlador.",".servicio.",".repositorio.",".pagos.",".notificaciones.",".composition."))
                        assertFalse(code.contains(prefix),p+":"+prefix);
                    if(root.relativize(p).toString().startsWith("read/")) {
                        assertFalse(code.contains("import jakarta.persistence"));assertFalse(code.contains("import org.hibernate"));
                        assertFalse(code.contains("import org.springframework"));assertFalse(code.contains(".adapter."));
                    }
                } else checkExternalCaller(p, code, names);
            }
        }
        assertEquals(1,NominalProgrammingReadPort.class.getDeclaredMethods().length);
    }
    @Test void negativeArchitecturalFixturesProveForbiddenSymbolsAndCallersAreDetected() {
        for(String name:FORBIDDEN) assertFalse(Collections.disjoint(symbols("class Bad { "+name+" dependency; }"),FORBIDDEN));
        assertTrue(symbols("class ProductiveCaller { NominalProgrammingReadPort reader; }").contains("NominalProgrammingReadPort"));
    }
    static Set<String> symbols(String text) {
        text=text.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("(?m)//.*$", " ");
        Matcher m=Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*").matcher(text);Set<String> result=new HashSet<>();
        while(m.find()) result.add(m.group());return result;
    }
    @Test void closedCatalogOutOfInvocationNestedUnknownAndWriteStatementsFail() {
        var inspector=F2eStatementPolicyInspector.paraR3(NominalProjectionCatalog.R3_NOMINAL_V1);
        assertThrows(IllegalStateException.class,()->inspector.inspect(NominalProjectionCatalog.ISOLATION_SQL));
        var capture=inspector.abrirCaptura("r3-policy");
        assertThrows(IllegalStateException.class,()->inspector.abrirCaptura("nested"));
        for(var s:NominalProjectionCatalog.R3_NOMINAL_V1.statements().values())
            inspector.inspect(s.sql().replaceAll(":(assignmentActive|blockActive|fecha|dayOfWeek)","?"));
        assertEquals(5,inspector.observarCaptura(capture).size());
        for(String forbidden:List.of("SELECT 1","UPDATE programacion_asignacion SET activo=false",
                "DELETE FROM programacion_bloque","SELECT * FROM programacion_bloque FOR UPDATE",
                "SELECT pg_current_snapshot()::text; SELECT 1","WITH x AS (SELECT 1) SELECT * FROM x"))
            assertThrows(RuntimeException.class,()->inspector.inspect(forbidden));
        assertEquals(5,inspector.cerrarCaptura(capture).size());
        assertThrows(IllegalStateException.class,()->inspector.observarCaptura(capture));
    }
    private static void checkExternalCaller(Path path, String code, Set<String> names) {
        Set<String> forbidden = new HashSet<>(names);
        if (R5_DTO_CONSUMERS.contains(path.toString())) {
            checkPureR5DtoConsumer(code);
            forbidden.removeAll(R5_INPUT_DTOS);
        }
        assertTrue(Collections.disjoint(symbols(code), forbidden), "Productive caller into R3: " + path);
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
        Set<String> names = MAIN.stream().map(p -> Path.of(p).getFileName().toString().replace(".java", "")).collect(java.util.stream.Collectors.toSet());
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
