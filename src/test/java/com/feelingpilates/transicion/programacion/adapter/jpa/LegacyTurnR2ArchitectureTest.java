package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LegacyTurnR2ArchitectureTest {
    private static final Path MAIN = Path.of("src/main/java");
    private static final String ROOT = "com/feelingpilates/transicion/programacion/";
    private static final Set<String> R2_FILES = Set.of(
            ROOT + "read/LegacyTurnReadPort.java",
            ROOT + "read/LegacyTurnScope.java",
            ROOT + "read/LegacyTurnReadContext.java",
            ROOT + "read/LegacyTurnReadSet.java",
            ROOT + "read/LegacyAdapterRejection.java",
            ROOT + "read/LegacyAdapterInputInvalid.java",
            ROOT + "adapter/jpa/projection/LegacyTurnProjectionCatalog.java",
            ROOT + "adapter/jpa/projection/LegacyTurnMemberRow.java",
            ROOT + "adapter/jpa/projection/LegacyAssignmentRow.java",
            ROOT + "adapter/jpa/projection/LegacyTurnProjectionQueryExecutor.java",
            ROOT + "adapter/jpa/mapper/LegacyTurnProjectionMapper.java",
            ROOT + "adapter/jpa/LegacyTurnJpaReader.java");
    private static final Set<String> FORBIDDEN = Set.of(
            "Controller", "RestController", "RequestMapping", "GetMapping", "PostMapping",
            "PutMapping", "PatchMapping", "DeleteMapping", "Scheduled", "EnableScheduling",
            "CommandLineRunner", "ApplicationRunner", "EventListener", "JmsListener",
            "RabbitListener", "Component", "Service", "Repository", "Configuration", "Bean",
            "JpaRepository", "CrudRepository", "PagingAndSortingRepository",
            "TurnoInstructorRepository", "TurnoInstructorAsignacionRepository", "ReservaRepository",
            "TurnoInstructorService", "ReservaService", "AjusteProgramacionFecha",
            "AbstractRoutingDataSource", "RoutingDataSource", "Cutover", "FeatureFlag",
            "PagoService", "NotificacionService", "REQUIRES_NEW");
    private static final List<String> FORBIDDEN_PACKAGES = List.of(
            "org.springframework.web.", "org.springframework.scheduling.", "javax.ws.rs.",
            "org.springframework.data.", "org.springframework.boot.CommandLineRunner",
            "org.springframework.boot.ApplicationRunner", ".controlador.", ".servicio.",
            ".repositorio.", ".pagos.", ".notificaciones.", ".composition.",
            ".job.", ".jobs.", ".scheduler.", ".listener.",
            "com.feelingpilates.programacion.");

    @Test
    void readerHasOnlyMandatoryReadOnlyPortAndClosedCatalog() throws Exception {
        assertEquals(List.of("readForDate"), Arrays.stream(LegacyTurnReadPort.class.getDeclaredMethods())
                .map(java.lang.reflect.Method::getName).toList());
        Transactional tx = LegacyTurnJpaReader.class
                .getMethod("readForDate", LegacyTurnReadContext.class,
                        com.feelingpilates.transicion.programacion.read.LegacyTurnScope.class)
                .getAnnotation(Transactional.class);
        assertEquals("f2eR2ReaderTransactionManager", tx.transactionManager());
        assertEquals(Propagation.MANDATORY, tx.propagation());
        assertTrue(tx.readOnly());
        assertEquals(1, LegacyTurnReadContext.ProjectionCatalogVersion.values().length);
        assertEquals(1, LegacyTurnReadContext.SnapshotClaim.values().length);
        assertEquals(6, LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1.statements().size());
    }

    @Test
    void inspectEveryR2ProductionClassAndEveryPossibleProductiveCaller() throws Exception {
        for (String relative : R2_FILES) assertTrue(Files.isRegularFile(MAIN.resolve(relative)), relative);
        try (var files = Files.walk(MAIN)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String relative = MAIN.relativize(file).toString().replace('\\', '/');
                Set<String> names = symbols(Files.readString(file));
                if (R2_FILES.contains(relative)) {
                    assertFalse(forbidden(names), relative);
                    if (relative.contains("/read/")) {
                        assertFalse(names.stream().anyMatch(name -> name.startsWith("jakarta.persistence.")
                                || name.startsWith("org.hibernate.")
                                || name.startsWith("org.springframework.")), relative);
                    }
                } else {
                    assertFalse(callsR2(names), "Productive caller into R2: " + relative);
                }
            }
        }
    }

    @Test
    void negativeFixturesProveForbiddenCategoriesAndCallerDetection() {
        for (String dependency : List.of(
                "import org.springframework.web.bind.annotation.RestController;",
                "import org.springframework.scheduling.annotation.Scheduled;",
                "import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;",
                "import com.feelingpilates.calendario.servicio.TurnoInstructorService;",
                "import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;",
                "import com.feelingpilates.pagos.PagoService;",
                "import com.feelingpilates.notificaciones.NotificacionService;",
                "import com.feelingpilates.programacion.AjusteProgramacionFecha;",
                "import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaService;",
                "@org.springframework.stereotype.Service class Fixture {}",
                "class TurnoCutoverSwitch {}",
                "class Fixture { void forbidden() { entityManager.persist(row); } }",
                "class Fixture implements org.springframework.boot.CommandLineRunner {"
                        + " public void run(String... args) {} }")) {
            assertTrue(forbidden(symbols(dependency + "\nclass Probe {}")), dependency);
        }
        assertTrue(callsR2(symbols("class ProductiveCaller { LegacyTurnJpaReader reader; }")));
        assertFalse(forbidden(symbols("class Safe { String value = \"RestController\"; }")));
    }

    private boolean forbidden(Set<String> symbols) {
        return symbols.stream().anyMatch(name -> FORBIDDEN.stream().anyMatch(forbidden ->
                        name.equals(forbidden) || name.endsWith("." + forbidden))
                || FORBIDDEN_PACKAGES.stream().anyMatch(name::contains)
                || List.of("entityManager.persist", "entityManager.merge", "entityManager.remove",
                        "entityManager.flush").contains(name)
                || name.toLowerCase(Locale.ROOT).contains("cutover")
                || name.toLowerCase(Locale.ROOT).contains("routing"));
    }

    private boolean callsR2(Set<String> symbols) {
        return symbols.stream().anyMatch(symbol -> R2_FILES.stream()
                .map(path -> path.substring(path.lastIndexOf('/') + 1, path.length() - 5))
                .anyMatch(name -> symbol.equals(name) || symbol.endsWith("." + name)));
    }

    private Set<String> symbols(String source) {
        try {
            JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///Fixture.java"),
                    JavaFileObject.Kind.SOURCE) {
                @Override public CharSequence getCharContent(boolean ignored) { return source; }
            };
            JavacTask task = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(
                    null, null, null, List.of("-proc:none"), null, List.of(file));
            Set<String> names = new HashSet<>();
            TreeScanner<Void, Void> scanner = new TreeScanner<>() {
                @Override public Void visitClass(ClassTree node, Void ignored) {
                    names.add(node.getSimpleName().toString());
                    return super.visitClass(node, ignored);
                }
                @Override public Void visitMethod(MethodTree node, Void ignored) {
                    names.add(node.getName().toString());
                    return super.visitMethod(node, ignored);
                }
                @Override public Void visitIdentifier(IdentifierTree node, Void ignored) {
                    names.add(node.getName().toString());
                    return super.visitIdentifier(node, ignored);
                }
                @Override public Void visitMemberSelect(MemberSelectTree node, Void ignored) {
                    names.add(node.toString());
                    return super.visitMemberSelect(node, ignored);
                }
            };
            for (CompilationUnitTree unit : task.parse()) scanner.scan(unit, null);
            return names;
        } catch (Exception exception) {
            throw new IllegalStateException("R2 architecture source could not be parsed", exception);
        }
    }
}
