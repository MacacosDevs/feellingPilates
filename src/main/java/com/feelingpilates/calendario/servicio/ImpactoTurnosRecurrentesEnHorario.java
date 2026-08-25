package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacion;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoCambioHorarioOperacion;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter de {@code calendario} para la validacion inversa: dice que turnos RECURRENTES activos
 * quedarian fuera del horario si se aplicara un {@link CambioHorarioOperacion}.
 *
 * <p>Depende <b>solo del repositorio</b>, nunca de {@link TurnoInstructorService}. Es deliberado:
 * ese service arrastra autorizacion y validaciones ajenas al impacto, y ademas depende (via
 * {@code HorarioEfectivoSalon}) de {@code ubicaciones}, con lo que llamarlo desde el writer de
 * horario cerraria un ciclo de beans en Spring.
 *
 * <p>Un {@code TurnoInstructor} RECURRENTE no tiene vigencia propia: es una regla abierta al
 * futuro, asi que mientras siga activo se considera aplicable desde cualquier fecha.
 *
 * <p><b>Turno EXCEPCION queda fuera de la Politica A</b> y no se consulta aqui: vive en una fecha
 * concreta y la invariante "todo EXCEPCION activo cabe en el horario efectivo de su fecha" ya no
 * la mantenia el sistema antes de F2C.2 ({@code SalonHorarioExcepcionService.guardar} podia cerrar un
 * dia sin mirar los turnos). Desde F2C.2 esa invariante la mantiene el protocolo propio de
 * {@code SalonHorarioExcepcionService} ({@code SalonLock} compartido + validacion inversa puntual en
 * {@code ImpactoPuntualEnExcepcionHorario}), no este validador: incluirlo aqui duplicaria la
 * proteccion sobre el camino equivocado (el versionado semanal, no la excepcion puntual).
 */
@Component
public class ImpactoTurnosRecurrentesEnHorario implements ValidadorImpactoCambioHorarioOperacion {

    private final TurnoInstructorRepository turnoRepository;

    public ImpactoTurnosRecurrentesEnHorario(TurnoInstructorRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    /**
     * Con el cambio ABIERTO, conflicto es el turno cuyas horas no caben en la nueva apertura/cierre.
     * Con el cambio CERRADO, cualquier turno recurrente activo de ese salon/dia es conflicto: el
     * dia deja de operar y no hay horario en el que quepa.
     *
     * <p>Politica A: se acumulan y se devuelven para que el rechazo identifique los turnos
     * afectados. Nunca se recorta, degrada ni desactiva un turno.
     */
    @Override
    public List<ConflictoProgramacion> evaluar(CambioHorarioOperacion cambio) {
        List<TurnoInstructor> recurrentes = turnoRepository
                .buscarRecurrentesPorSalonYDia(cambio.salonId(), cambio.diaSemana());

        return recurrentes.stream()
                .filter(turno -> !cambio.admite(turno.getHoraInicio(), turno.getHoraFin()))
                .map(turno -> ConflictoProgramacion.turnoRecurrente(
                        turno.getId(), turno.getHoraInicio() + "-" + turno.getHoraFin()))
                .toList();
    }
}
