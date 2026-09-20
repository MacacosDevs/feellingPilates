package com.feelingpilates.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae el codigo estable de un mensaje de error con la convencion del proyecto
 * {@code "CODIGO: texto humano"}. Neutral: no conoce HTTP, ni modulos de dominio, ni
 * clasificaciones. Es el UNICO sitio del proyecto que parsea ese prefijo.
 */
public final class CodigoErrorExtractor {

    private static final Pattern CODIGO_ESTABLE = Pattern.compile("^([A-Z][A-Z0-9_]{2,}):");

    private CodigoErrorExtractor() {
    }

    /** @return el codigo estable, o {@code null} si el mensaje es null o no sigue la convencion. */
    public static String extraer(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        Matcher m = CODIGO_ESTABLE.matcher(mensaje);
        return m.find() ? m.group(1) : null;
    }
}
