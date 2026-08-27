package com.feelingpilates.programacion.servicio;

import org.hibernate.exception.ConstraintViolationException;

public final class ConflictoAjusteProgramacionTranslator {

    public static final String TARGET_ACTIVO = "idx_programacion_ajuste_target_activo";
    public static final String AJUSTE_ID = "programacion_ajuste_fecha_pkey";
    private static final String UNIQUE_VIOLATION = "23505";

    private ConflictoAjusteProgramacionTranslator() {
    }

    public static <T> T traduciendoTarget(Escritura<T> escritura) {
        return traducir(escritura, TARGET_ACTIVO);
    }

    public static <T> T traduciendoAjusteId(Escritura<T> escritura) {
        return traducir(escritura, AJUSTE_ID);
    }

    private static <T> T traducir(Escritura<T> escritura, String constraint) {
        try {
            return escritura.ejecutar();
        } catch (RuntimeException error) {
            if (esViolacion(error, constraint)) {
                throw new ConflictoAjusteProgramacionException(
                        ProgramacionErrores.mensaje(
                                ProgramacionErrores.CONFLICTO_AJUSTE_PROGRAMACION,
                                "La identidad del ajuste ya fue utilizada"),
                        UNIQUE_VIOLATION,
                        constraint);
            }
            throw error;
        }
    }

    static boolean esViolacion(Throwable error, String constraint) {
        for (Throwable causa = error;
             causa != null && causa.getCause() != causa;
             causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException cve) {
                return UNIQUE_VIOLATION.equals(cve.getSQLState())
                        && constraint.equals(cve.getConstraintName());
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Escritura<T> {
        T ejecutar();
    }
}
