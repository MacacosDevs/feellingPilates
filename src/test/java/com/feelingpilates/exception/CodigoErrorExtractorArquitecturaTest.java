package com.feelingpilates.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard de arquitectura: {@link CodigoErrorExtractor} debe seguir siendo el UNICO sitio del
 * proyecto que parsea el prefijo {@code "CODIGO: texto"}. Falla si {@link GlobalExceptionHandler}
 * o {@link com.feelingpilates.ubicaciones.servicio.HorarioOperacionErrores} vuelven a implementar
 * un parser local en lugar de delegar en {@code CodigoErrorExtractor.extraer(...)}.
 */
class CodigoErrorExtractorArquitecturaTest {

    private static final String GLOBAL_EXCEPTION_HANDLER =
            "src/main/java/com/feelingpilates/exception/GlobalExceptionHandler.java";
    private static final String HORARIO_OPERACION_ERRORES =
            "src/main/java/com/feelingpilates/ubicaciones/servicio/HorarioOperacionErrores.java";
    private static final String SALON_HORARIO_EXCEPCION_ERRORES =
            "src/main/java/com/feelingpilates/ubicaciones/servicio/SalonHorarioExcepcionErrores.java";

    private static final String[] MARCADORES_DE_PARSER_LOCAL = {
            "java.util.regex.Pattern",
            "java.util.regex.Matcher",
            "Pattern.compile",
    };

    @ParameterizedTest
    @ValueSource(strings = {GLOBAL_EXCEPTION_HANDLER, HORARIO_OPERACION_ERRORES, SALON_HORARIO_EXCEPCION_ERRORES})
    void noImplementaUnParserLocalDeCodigo(String rutaRelativa) {
        String contenido = leer(rutaRelativa);
        for (String marcador : MARCADORES_DE_PARSER_LOCAL) {
            assertThat(contenido)
                    .as("%s no debe contener parsing local ('%s'); debe delegar en CodigoErrorExtractor",
                            rutaRelativa, marcador)
                    .doesNotContain(marcador);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {GLOBAL_EXCEPTION_HANDLER, HORARIO_OPERACION_ERRORES, SALON_HORARIO_EXCEPCION_ERRORES})
    void delegaEnCodigoErrorExtractor(String rutaRelativa) {
        String contenido = leer(rutaRelativa);
        assertThat(contenido)
                .as("%s debe delegar la extraccion del codigo en CodigoErrorExtractor.extraer(...)", rutaRelativa)
                .contains("CodigoErrorExtractor.extraer(");
    }

    @Test
    void codigoErrorExtractorEsElUnicoQueUsaPatternCompileParaElPrefijoContractual() {
        String contenido = leer("src/main/java/com/feelingpilates/exception/CodigoErrorExtractor.java");
        assertThat(contenido).contains("Pattern.compile");
    }

    private static String leer(String rutaRelativa) {
        try {
            return Files.readString(Path.of(rutaRelativa));
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudo leer " + rutaRelativa + " (verificar que el working dir de la suite sea la raiz del modulo)",
                    e);
        }
    }
}
