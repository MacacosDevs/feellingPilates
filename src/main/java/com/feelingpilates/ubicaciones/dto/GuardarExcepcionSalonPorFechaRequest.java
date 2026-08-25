package com.feelingpilates.ubicaciones.dto;

import java.time.LocalTime;

/** Cuerpo del {@code PUT .../excepciones-horario/por-fecha/{fecha}}: la fecha viene del path, no aquí. */
public record GuardarExcepcionSalonPorFechaRequest(
        boolean cerrado,
        LocalTime horaApertura,
        LocalTime horaCierre) {
}
