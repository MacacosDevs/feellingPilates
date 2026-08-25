package com.feelingpilates.ubicaciones.dominio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Descripcion neutral del estado operativo que va a regir en {@code (salonId, fecha)}, para que los
 * modulos que dependen de {@code ubicaciones} puedan evaluar el impacto puntual sin que
 * {@code ubicaciones} conozca sus tipos ({@code TurnoInstructor}, {@code Reserva}).
 *
 * <p>Deliberadamente NO se reutiliza {@link HorarioEfectivo}: ese tipo modela "que pasa en una
 * fecha concreta" ya resuelta (con {@link HorarioEfectivo.Origen}), mientras que este record
 * describe un cambio <b>solicitado</b>, todavia no vigente. Es el analogo puntual de
 * {@link CambioHorarioOperacion}.
 *
 * <p>Invariante, igual que en {@link CambioHorarioOperacion}: solo {@link Estado#HORARIO_ESPECIAL}
 * porta horas.
 */
public record CambioExcepcionHorario(
        UUID salonId, LocalDate fecha, Estado estado, LocalTime horaApertura, LocalTime horaCierre) {

    public enum Estado {
        /** El dia no opera en absoluto en {@code fecha}. */
        CERRADO,
        /** El dia opera solo {@code [horaApertura, horaCierre]} en {@code fecha}. */
        HORARIO_ESPECIAL
    }

    public CambioExcepcionHorario {
        if (salonId == null) {
            throw new IllegalArgumentException("salonId no puede ser null");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("fecha no puede ser null");
        }
        if (estado == null) {
            throw new IllegalArgumentException("estado no puede ser null");
        }
        if (estado == Estado.HORARIO_ESPECIAL && (horaApertura == null || horaCierre == null)) {
            throw new IllegalArgumentException("Un cambio HORARIO_ESPECIAL requiere apertura y cierre");
        }
        if (estado != Estado.HORARIO_ESPECIAL && (horaApertura != null || horaCierre != null)) {
            throw new IllegalArgumentException("Solo un cambio HORARIO_ESPECIAL porta apertura y cierre");
        }
    }

    public static CambioExcepcionHorario cerrado(UUID salonId, LocalDate fecha) {
        return new CambioExcepcionHorario(salonId, fecha, Estado.CERRADO, null, null);
    }

    public static CambioExcepcionHorario horarioEspecial(
            UUID salonId, LocalDate fecha, LocalTime horaApertura, LocalTime horaCierre) {
        return new CambioExcepcionHorario(salonId, fecha, Estado.HORARIO_ESPECIAL, horaApertura, horaCierre);
    }

    /**
     * {@code [inicio, fin]} cabe en el estado resultante. Siempre falso si {@code fin <= inicio}
     * (intervalo vacio o invertido) o si {@link Estado#CERRADO}: un dia cerrado no admite ningun
     * intervalo. No es solape: exige contencion completa, igual que {@link HorarioEfectivo#contiene}
     * y {@link CambioHorarioOperacion#admite}.
     */
    public boolean admite(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            return false;
        }
        return estado == Estado.HORARIO_ESPECIAL && !inicio.isBefore(horaApertura) && !fin.isAfter(horaCierre);
    }
}
