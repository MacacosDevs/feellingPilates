package com.feelingpilates.ubicaciones.dominio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Descripcion neutral del horario que va a regir en {@code (salonId, diaSemana)} desde
 * {@code efectivoDesde} hacia +infinito, para que los modulos que dependen de {@code ubicaciones}
 * puedan evaluar el impacto sin que {@code ubicaciones} conozca sus tipos.
 *
 * <p>Deliberadamente NO se reutiliza {@link HorarioEfectivo}: ese tipo modela "que pasa en una
 * fecha concreta" y porta {@link HorarioEfectivo.Origen#EXCEPCION}, semantica que aqui no aplica
 * y confundiria a los adapters. Este record modela una regla semanal abierta al futuro.
 *
 * <p>Invariante, igual que en {@link HorarioEfectivo}: solo {@link Estado#ABIERTO} porta horas.
 */
public record CambioHorarioOperacion(
        UUID salonId,
        short diaSemana,
        LocalDate efectivoDesde,
        Estado estado,
        LocalTime horaApertura,
        LocalTime horaCierre) {

    public enum Estado {
        /** El dia pasa a operar con {@code [horaApertura, horaCierre]} desde {@code efectivoDesde}. */
        ABIERTO,
        /** El dia deja de operar recurrentemente desde {@code efectivoDesde} hacia +infinito. */
        CERRADO
    }

    public CambioHorarioOperacion {
        if (salonId == null) {
            throw new IllegalArgumentException("salonId no puede ser null");
        }
        if (efectivoDesde == null) {
            throw new IllegalArgumentException("efectivoDesde no puede ser null");
        }
        if (estado == null) {
            throw new IllegalArgumentException("estado no puede ser null");
        }
        if (estado == Estado.ABIERTO && (horaApertura == null || horaCierre == null)) {
            throw new IllegalArgumentException("Un cambio ABIERTO requiere apertura y cierre");
        }
        if (estado != Estado.ABIERTO && (horaApertura != null || horaCierre != null)) {
            throw new IllegalArgumentException("Solo un cambio ABIERTO porta apertura y cierre");
        }
    }

    public static CambioHorarioOperacion abierto(
            UUID salonId, short diaSemana, LocalDate efectivoDesde,
            LocalTime horaApertura, LocalTime horaCierre) {
        return new CambioHorarioOperacion(
                salonId, diaSemana, efectivoDesde, Estado.ABIERTO, horaApertura, horaCierre);
    }

    public static CambioHorarioOperacion cerrado(UUID salonId, short diaSemana, LocalDate efectivoDesde) {
        return new CambioHorarioOperacion(salonId, diaSemana, efectivoDesde, Estado.CERRADO, null, null);
    }

    public boolean estaAbierto() {
        return estado == Estado.ABIERTO;
    }

    /**
     * {@code [inicio, fin]} cabe en el horario resultante. Siempre falso si el cambio es
     * {@link Estado#CERRADO}: un dia cerrado no admite ninguna programacion.
     */
    public boolean admite(LocalTime inicio, LocalTime fin) {
        return estaAbierto() && !inicio.isBefore(horaApertura) && !fin.isAfter(horaCierre);
    }
}
