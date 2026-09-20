package com.feelingpilates.ubicaciones.dominio;

import java.time.LocalDate;

/**
 * Intervalo de fechas [desde, hasta] cerrado en ambos extremos.
 * {@code desde == null} representa -infinito; {@code hasta == null} representa +infinito.
 * Nunca se usa {@link LocalDate#MIN}/{@link LocalDate#MAX} como centinela: el infinito es
 * estado logico, no una fecha real, por lo que la aritmetica de fronteras solo opera cuando
 * el limite relevante es finito.
 */
public record RangoVigencia(LocalDate desde, LocalDate hasta) {

    public RangoVigencia {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "vigenteHasta (" + hasta + ") no puede ser anterior a vigenteDesde (" + desde + ")");
        }
    }

    public boolean contiene(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("fecha no puede ser null");
        }
        return (desde == null || !fecha.isBefore(desde))
                && (hasta == null || !fecha.isAfter(hasta));
    }

    public boolean intersecta(RangoVigencia otro) {
        boolean esteTerminaAntesDeQueEmpieceElOtro =
                hasta != null && otro.desde != null && hasta.isBefore(otro.desde);
        boolean otroTerminaAntesDeQueEmpieceEste =
                otro.hasta != null && desde != null && otro.hasta.isBefore(desde);
        return !esteTerminaAntesDeQueEmpieceElOtro && !otroTerminaAntesDeQueEmpieceEste;
    }

    public boolean esContiguoCon(RangoVigencia otro) {
        if (hasta != null && otro.desde != null && hasta.plusDays(1).isEqual(otro.desde)) {
            return true;
        }
        return otro.hasta != null && desde != null && otro.hasta.plusDays(1).isEqual(desde);
    }
}
