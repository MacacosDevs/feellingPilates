package com.feelingpilates.ubicaciones.dominio;

import java.time.LocalTime;

/**
 * Horario con el que realmente opera un salon en una fecha concreta, ya combinadas la plantilla
 * semanal versionada y las excepciones puntuales.
 *
 * <p>Tipo cerrado a proposito: {@link Estado#CERRADO} (el salon existe y ese dia esta cerrado por
 * excepcion) y {@link Estado#NO_OPERATIVO} (ese dia no hay horario configurado en absoluto) son
 * semanticas distintas y no deben colapsarse en un {@code Optional} vacio.
 *
 * <p>{@link Origen} acompaña al estado porque quien valida necesita saber si el horario que no se
 * respeta es el especial de esa fecha o el semanal, para poder explicarlo.
 */
public record HorarioEfectivo(Estado estado, Origen origen, LocalTime horaApertura, LocalTime horaCierre) {

    public enum Estado {
        ABIERTO,
        CERRADO,
        NO_OPERATIVO
    }

    public enum Origen {
        /** Lo decidio una {@code SalonHorarioExcepcion} activa para esa fecha exacta. */
        EXCEPCION,
        /** Lo decidio la version de {@code HorarioOperacion} vigente ese dia. */
        SEMANAL,
        /** Nada lo definio: el salon no opera esa fecha. */
        NINGUNO
    }

    public HorarioEfectivo {
        if (estado == Estado.ABIERTO && (horaApertura == null || horaCierre == null)) {
            throw new IllegalArgumentException("Un horario efectivo ABIERTO requiere apertura y cierre");
        }
        if (estado != Estado.ABIERTO && (horaApertura != null || horaCierre != null)) {
            throw new IllegalArgumentException("Solo un horario efectivo ABIERTO porta apertura y cierre");
        }
    }

    public static HorarioEfectivo abiertoPorExcepcion(LocalTime horaApertura, LocalTime horaCierre) {
        return new HorarioEfectivo(Estado.ABIERTO, Origen.EXCEPCION, horaApertura, horaCierre);
    }

    public static HorarioEfectivo abiertoPorHorarioSemanal(LocalTime horaApertura, LocalTime horaCierre) {
        return new HorarioEfectivo(Estado.ABIERTO, Origen.SEMANAL, horaApertura, horaCierre);
    }

    public static HorarioEfectivo cerrado() {
        return new HorarioEfectivo(Estado.CERRADO, Origen.EXCEPCION, null, null);
    }

    public static HorarioEfectivo noOperativo() {
        return new HorarioEfectivo(Estado.NO_OPERATIVO, Origen.NINGUNO, null, null);
    }

    public boolean estaAbierto() {
        return estado == Estado.ABIERTO;
    }

    public boolean estaCerrado() {
        return estado == Estado.CERRADO;
    }

    public boolean vieneDeExcepcion() {
        return origen == Origen.EXCEPCION;
    }

    /** {@code [inicio, fin]} cabe dentro del horario de atencion. Falso si no esta ABIERTO. */
    public boolean contiene(LocalTime inicio, LocalTime fin) {
        return estaAbierto() && !inicio.isBefore(horaApertura) && !fin.isAfter(horaCierre);
    }
}
