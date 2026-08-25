package com.feelingpilates.ubicaciones.dominio;

import java.util.UUID;

/**
 * Programacion puntual existente que quedaria incompatible con un {@link CambioExcepcionHorario}.
 *
 * <p>Tipo neutral a proposito, analogo a {@link ConflictoProgramacion}: lleva el ID y una
 * descripcion corta, nunca la entidad de {@code calendario}, para que {@code ubicaciones} no
 * dependa de ese modulo. {@code detalle} es texto de diagnostico para el mensaje de error, no un
 * DTO HTTP.
 */
public record ConflictoProgramacionPuntual(Origen origen, UUID id, String detalle) {

    public enum Origen {
        TURNO_EXCEPCION,
        RESERVA_CONFIRMADA
    }

    public ConflictoProgramacionPuntual {
        if (origen == null) {
            throw new IllegalArgumentException("origen no puede ser null");
        }
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
    }

    public static ConflictoProgramacionPuntual turnoExcepcion(UUID id, String detalle) {
        return new ConflictoProgramacionPuntual(Origen.TURNO_EXCEPCION, id, detalle);
    }

    public static ConflictoProgramacionPuntual reservaConfirmada(UUID id, String detalle) {
        return new ConflictoProgramacionPuntual(Origen.RESERVA_CONFIRMADA, id, detalle);
    }

    @Override
    public String toString() {
        return origen + "[" + id + (detalle == null || detalle.isBlank() ? "" : " " + detalle) + "]";
    }
}
