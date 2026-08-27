package com.feelingpilates.programacion;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DarkLaunchIntegracionTest {

    @Test
    void serviciosLegacyNoConsumenAjustesNiProgramacionEfectiva() throws IOException {
        for (String ruta : new String[] {
                "src/main/java/com/feelingpilates/calendario/servicio/TurnoInstructorService.java",
                "src/main/java/com/feelingpilates/calendario/servicio/ReservaService.java",
                "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionService.java"
        }) {
            assertThat(Files.readString(Path.of(ruta)))
                    .as(ruta)
                    .doesNotContain("AjusteProgramacionFecha")
                    .doesNotContain("ProgramacionEfectiva")
                    .doesNotContain("AplicadorAjustesProgramacion");
        }
    }
}
