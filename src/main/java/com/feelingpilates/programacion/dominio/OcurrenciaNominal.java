package com.feelingpilates.programacion.dominio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record OcurrenciaNominal(
        LocalDate fecha,
        UUID serieId,
        UUID asignacionVersionId,
        UUID bloqueVersionId,
        UUID salonId,
        UUID instructorId,
        UUID tipoActividadId,
        LocalTime horaInicio,
        LocalTime horaFin) {
}
