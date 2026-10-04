package com.feelingpilates.ubicaciones.dominio;

import java.util.UUID;

/**
 * Programacion existente que quedaria incompatible con un {@link CambioHorarioOperacion}.
 *
 * <p>Tipo neutral a proposito: lleva el ID y una descripcion corta, nunca la entidad de
 * {@code calendario} ni de {@code programacion}, para que {@code ubicaciones} no dependa de
 * ninguno de los dos modulos. {@code detalle} es texto de diagnostico para el mensaje de error
 * (p. ej. el rango horario que no cabe), no un DTO HTTP.
 */
public record ConflictoProgramacion(Origen origen, UUID id, String detalle) {

    public enum Origen {
        TURNO_RECURRENTE,
        BLOQUE_PROGRAMACION
    }

    public ConflictoProgramacion {
        if (origen == null) {
            throw new IllegalArgumentException("origen no puede ser null");
        }
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
    }

    public static ConflictoProgramacion turnoRecurrente(UUID id, String detalle) {
        return new ConflictoProgramacion(Origen.TURNO_RECURRENTE, id, detalle);
    }

    public static ConflictoProgramacion bloqueProgramacion(UUID id, String detalle) {
        return new ConflictoProgramacion(Origen.BLOQUE_PROGRAMACION, id, detalle);
    }

    @Override
    public String toString() {
        return origen + "[" + id + (detalle == null || detalle.isBlank() ? "" : " " + detalle) + "]";
    }
}
