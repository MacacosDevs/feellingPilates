package com.feelingpilates.clases.servicio;

import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.entidad.TurnoInstructorAsignacion;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.calendario.servicio.TurnoInstructorService;
import com.feelingpilates.clases.repositorio.ClaseRepository;
import com.feelingpilates.usuarios.entidad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Materializa clases concretas (tabla {@code clase}) a partir de los turnos de
 * instructor vigentes de un salon en una fecha dada. Se llama bajo demanda desde
 * los endpoints de lectura (no hay job programado): cualquier rango de fechas
 * que se consulte, se genera, siempre, y es seguro llamarlo repetidas veces
 * (el UNIQUE de {@code clase} + ON CONFLICT DO NOTHING lo hace idempotente).
 */
@Service
@Transactional
public class ClaseGeneracionService {

    private final TurnoInstructorRepository turnoRepository;
    private final TurnoInstructorService turnoInstructorService;
    private final ClaseRepository claseRepository;

    public ClaseGeneracionService(
            TurnoInstructorRepository turnoRepository,
            TurnoInstructorService turnoInstructorService,
            ClaseRepository claseRepository) {
        this.turnoRepository = turnoRepository;
        this.turnoInstructorService = turnoInstructorService;
        this.claseRepository = claseRepository;
    }

    public void asegurarMaterializado(UUID salonId, LocalDate fecha) {
        short diaSemana = (short) diaSemanaIso(fecha.getDayOfWeek());
        List<TurnoInstructor> recurrentes = turnoRepository.buscarRecurrentesPorSalonYDia(salonId, diaSemana);
        List<TurnoInstructor> puntuales = turnoRepository.buscarPuntualesPorSalonYFecha(salonId, fecha);

        Set<Usuario> instructores = new LinkedHashSet<>();
        recurrentes.forEach(t -> instructores.addAll(t.getInstructores()));
        puntuales.forEach(t -> instructores.addAll(t.getInstructores()));

        for (Usuario instructor : instructores) {
            List<TurnoInstructor> recurrentesInstructor = recurrentes.stream()
                    .filter(t -> t.getInstructores().contains(instructor))
                    .toList();
            List<TurnoInstructor> puntualesInstructor = puntuales.stream()
                    .filter(t -> t.getInstructores().contains(instructor))
                    .toList();
            List<TurnoInstructor> vigentes = turnoInstructorService.resolverVigentes(puntualesInstructor, recurrentesInstructor);

            for (TurnoInstructor turno : vigentes) {
                for (TurnoInstructorAsignacion asignacion : turno.getAsignaciones()) {
                    if (!asignacion.getUsuario().equals(instructor)) {
                        continue;
                    }
                    LocalTime horaInicio = asignacion.getHoraInicio() != null ? asignacion.getHoraInicio() : turno.getHoraInicio();
                    LocalTime horaFin = asignacion.getHoraFin() != null ? asignacion.getHoraFin() : turno.getHoraFin();
                    claseRepository.insertarSiNoExiste(
                            turno.getId(),
                            salonId,
                            instructor.getId(),
                            asignacion.getTipoActividad().getId(),
                            fecha,
                            horaInicio,
                            horaFin,
                            asignacion.getTipoActividad().getCapacidadPredeterminada());
                }
            }
        }
    }

    /** DayOfWeek de java (1=lunes..7=domingo) al formato del sistema (0=domingo..6=sabado). */
    private int diaSemanaIso(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }
}
