package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

import java.time.LocalDate;
import java.util.UUID;

public interface ProgramacionDiagnostico {

    void registrarOmision(Omision omision);

    record Omision(
            String causa,
            ReferenciaOcurrencia referencia,
            LocalDate fecha,
            UUID salonId,
            UUID instructorId,
            UUID actividadId) {
    }
}
