package com.feelingpilates.programacion.dominio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record OcurrenciaEfectiva(
        LocalDate fecha,
        UUID salonId,
        UUID instructorId,
        UUID tipoActividadId,
        LocalTime horaInicio,
        LocalTime horaFin,
        Origen origen,
        ReferenciaOcurrencia referencia) {

    public enum Origen { RECURRENTE, REEMPLAZO, ADICION }
}
