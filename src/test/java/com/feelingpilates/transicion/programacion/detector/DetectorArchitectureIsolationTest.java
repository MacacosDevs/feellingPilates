package com.feelingpilates.transicion.programacion.detector;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorArchitectureIsolationTest {

    private static final Path DETECTOR = Path.of(
            "src/main/java/com/feelingpilates/transicion/programacion/detector");

    @Test
    void productionSubtreeEsPureJavaSinFrameworksPersistenciaNiRuntimeWiring() throws IOException {
        String sources = sourceText();

        assertThat(sources)
                .doesNotContain("org.springframework")
                .doesNotContain("jakarta.persistence")
                .doesNotContain("javax.persistence")
                .doesNotContain("org.hibernate")
                .doesNotContain("java.sql")
                .doesNotContain("JpaRepository")
                .doesNotContain("EntityManager")
                .doesNotContain("@Service")
                .doesNotContain("@Component")
                .doesNotContain("@Repository")
                .doesNotContain("@Configuration")
                .doesNotContain("@Controller")
                .doesNotContain("@RestController")
                .doesNotContain("@Scheduled")
                .doesNotContain("@EventListener")
                .doesNotContain("ApplicationRunner")
                .doesNotContain("CommandLineRunner")
                .doesNotContain("com.feelingpilates.calendario")
                .doesNotContain("com.feelingpilates.programacion.servicio")
                .doesNotContain("com.feelingpilates.programacion.repositorio")
                .doesNotContain("com.feelingpilates.programacion.entidad");
    }

    @Test
    void resultContractNoDeclaraCamposDeSeleccionMaterial() {
        assertThat(List.of(DetectorResult.class.getDeclaredFields()))
                .extracting(field -> field.getName().toLowerCase())
                .noneMatch(name -> name.contains("selectedtarget")
                        || name.contains("selectedcandidate")
                        || name.contains("resolvedtarget")
                        || name.contains("winner")
                        || name.contains("finalmapping"));
    }

    @Test
    void productionTypesNoContienenEstadoEstaticoMutable() {
        List<Class<?>> types = List.of(
                CandidateEvidenceGenerator.class,
                CandidateGenerationResult.class,
                ClassificationContext.class,
                DetectorCandidate.class,
                DetectorClassifier.class,
                DetectorEvaluationRequest.class,
                DetectorResult.class,
                EvidenceProvenance.class,
                F2DAuthorityGuard.class,
                F2DCompatibilityResult.class,
                GenericSourceSnapshot.class,
                HistoricalProgrammingTargetSnapshot.class,
                ProgrammingCandidateSnapshot.class,
                ReservationSourceSnapshot.class,
                ReservedSubinterval.class);

        assertThat(types.stream().flatMap(type -> List.of(type.getDeclaredFields()).stream())
                .filter(field -> Modifier.isStatic(field.getModifiers())))
                .allMatch(field -> Modifier.isFinal(field.getModifiers()));
    }

    private String sourceText() throws IOException {
        StringBuilder text = new StringBuilder();
        try (var paths = Files.walk(DETECTOR)) {
            for (Path path : paths.filter(candidate -> candidate.toString().endsWith(".java"))
                    .sorted().toList()) {
                text.append(Files.readString(path)).append('\n');
            }
        }
        return text.toString();
    }
}
