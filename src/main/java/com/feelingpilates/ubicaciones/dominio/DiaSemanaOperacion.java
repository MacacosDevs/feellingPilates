package com.feelingpilates.ubicaciones.dominio;

import java.time.DayOfWeek;

/**
 * Conversion compartida {@link DayOfWeek} -> convencion FeelingPilates/PostgreSQL:
 * domingo = 0, lunes = 1, ..., sabado = 6.
 */
public final class DiaSemanaOperacion {

    private DiaSemanaOperacion() {
    }

    public static short desde(DayOfWeek dayOfWeek) {
        return (short) (dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue());
    }
}
