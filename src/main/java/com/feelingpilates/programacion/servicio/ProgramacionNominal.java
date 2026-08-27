package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.repositorio.AsignacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Lee nominales sin aplicar horario operativo ni estado maestro. */
@Service
@Transactional(readOnly = true)
public class ProgramacionNominal {

    private final AsignacionRepository asignacionRepository;

    public ProgramacionNominal(AsignacionRepository asignacionRepository) {
        this.asignacionRepository = asignacionRepository;
    }

    public List<OcurrenciaNominal> todasEnFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("fecha no puede ser null");
        }
        short diaSemana = (short) (fecha.getDayOfWeek().getValue() % 7);
        return asignacionRepository.buscarNominalesDeFecha(fecha, diaSemana).stream()
                .map(p -> new OcurrenciaNominal(
                        fecha,
                        p.getSerieId(),
                        p.getAsignacionVersionId(),
                        p.getBloqueVersionId(),
                        p.getSalonId(),
                        p.getInstructorId(),
                        p.getTipoActividadId(),
                        p.getHoraInicio(),
                        p.getHoraFin()))
                .toList();
    }

    public List<OcurrenciaNominal> porSerieYFecha(UUID serieId, LocalDate fecha) {
        return todasEnFecha(fecha).stream()
                .filter(n -> serieId.equals(n.serieId()))
                .toList();
    }
}
