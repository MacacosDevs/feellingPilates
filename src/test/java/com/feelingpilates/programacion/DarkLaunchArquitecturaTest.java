package com.feelingpilates.programacion;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkLaunchArquitecturaTest {

    private static final Path PROGRAMACION = Path.of(
            "src/main/java/com/feelingpilates/programacion");

    @Test
    void programacionNoExponeControllersNiEndpoints() throws IOException {
        List<Path> fuentes = fuentesJava(PROGRAMACION);

        assertThat(fuentes).noneMatch(path -> path.toString().contains("controlador"));
        assertThat(contenido(fuentes))
                .doesNotContain("@RestController")
                .doesNotContain("@RequestMapping")
                .doesNotContain("org.springframework.web");
    }

    @Test
    void programacionNoDependeDeAutoridadesLegacyNiCutover() throws IOException {
        String fuentes = contenido(fuentesJava(PROGRAMACION));

        assertThat(fuentes)
                .doesNotContain("TurnoInstructor")
                .doesNotContain("Reserva")
                .doesNotContain("ImpactoAjustesEnExcepcionHorario")
                .doesNotContain("ValidadorImpactoExcepcionHorario")
                .doesNotContain("com.feelingpilates.calendario");
    }

    @Test
    void programacionNoUsaRelojGlobalNiMergeYV47NoUsaUpsert() throws IOException {
        String java = contenido(fuentesJava(PROGRAMACION));
        String migration = Files.readString(Path.of(
                "src/main/resources/db/migration/V47__programacion_ajustes_fecha.sql"));

        assertThat(java)
                .doesNotContain("LocalDate.now(")
                .doesNotContain("LocalTime.now(")
                .doesNotContain(".merge(");
        assertThat(migration.toLowerCase()).doesNotContain("on conflict");
    }

    private List<Path> fuentesJava(Path raiz) throws IOException {
        try (var paths = Files.walk(raiz)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }

    private String contenido(List<Path> fuentes) throws IOException {
        StringBuilder resultado = new StringBuilder();
        for (Path fuente : fuentes) {
            resultado.append(Files.readString(fuente)).append('\n');
        }
        return resultado.toString();
    }
}
