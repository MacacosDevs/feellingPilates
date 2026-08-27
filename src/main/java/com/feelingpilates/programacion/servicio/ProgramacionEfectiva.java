package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Resolver interno dark-launch; nunca consulta calendario, turnos ni reservas. */
@Service
@Transactional(readOnly = true)
public class ProgramacionEfectiva {

    private static final Comparator<OcurrenciaEfectiva> ORDEN = Comparator
            .comparing(OcurrenciaEfectiva::horaInicio)
            .thenComparing(OcurrenciaEfectiva::horaFin)
            .thenComparing(OcurrenciaEfectiva::instructorId)
            .thenComparing(OcurrenciaEfectiva::referencia);

    private final ProgramacionNominal nominal;
    private final AjusteProgramacionFechaRepository ajustes;
    private final AplicadorAjustesProgramacion aplicador;
    private final ProgramacionValidador validador;

    public ProgramacionEfectiva(
            ProgramacionNominal nominal,
            AjusteProgramacionFechaRepository ajustes,
            AplicadorAjustesProgramacion aplicador,
            ProgramacionValidador validador) {
        this.nominal = nominal;
        this.ajustes = ajustes;
        this.aplicador = aplicador;
        this.validador = validador;
    }

    public List<OcurrenciaEfectiva> porSalonYFecha(UUID salonId, LocalDate fecha) {
        return resolverGlobal(fecha).stream()
                .filter(o -> salonId.equals(o.salonId()))
                .sorted(ORDEN)
                .toList();
    }

    public List<OcurrenciaEfectiva> porInstructorYFecha(UUID instructorId, LocalDate fecha) {
        return resolverGlobal(fecha).stream()
                .filter(o -> instructorId.equals(o.instructorId()))
                .sorted(ORDEN)
                .toList();
    }

    public List<OcurrenciaEfectiva> resolverGlobal(LocalDate fecha) {
        List<OcurrenciaEfectiva> candidatos = aplicador.aplicar(
                nominal.todasEnFecha(fecha),
                ajustes.findAllByFechaAndActivoTrueOrderById(fecha));
        return validador.filtrarFailClosedYValidar(candidatos).stream()
                .sorted(ORDEN)
                .toList();
    }
}
