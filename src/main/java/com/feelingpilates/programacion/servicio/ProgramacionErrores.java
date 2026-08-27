package com.feelingpilates.programacion.servicio;

public final class ProgramacionErrores {

    public static final String AJUSTE_PROGRAMACION_EN_EL_PASADO = "AJUSTE_PROGRAMACION_EN_EL_PASADO";
    public static final String AJUSTE_PROGRAMACION_FORMA_INVALIDA = "AJUSTE_PROGRAMACION_FORMA_INVALIDA";
    public static final String ASIGNACION_OBJETIVO_NO_EXISTE = "ASIGNACION_OBJETIVO_NO_EXISTE";
    public static final String AJUSTE_FUERA_DE_HORARIO_EFECTIVO = "AJUSTE_FUERA_DE_HORARIO_EFECTIVO";
    public static final String SALON_NO_OPERATIVO_EN_FECHA = "SALON_NO_OPERATIVO_EN_FECHA";
    public static final String INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA = "INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA";
    public static final String OCURRENCIA_EFECTIVA_DUPLICADA = "OCURRENCIA_EFECTIVA_DUPLICADA";
    public static final String CONFLICTO_AJUSTE_PROGRAMACION = "CONFLICTO_AJUSTE_PROGRAMACION";
    public static final String CONFLICTO_LOCK_SET_DESACTUALIZADO = "CONFLICTO_LOCK_SET_DESACTUALIZADO";
    public static final String AJUSTE_PROGRAMACION_NO_EXISTE = "AJUSTE_PROGRAMACION_NO_EXISTE";

    private ProgramacionErrores() {
    }

    public static String mensaje(String codigo, String detalle) {
        return codigo + ": " + detalle;
    }
}
