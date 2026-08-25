package com.feelingpilates.calendario.servicio;

import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.ReservaRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.ubicaciones.dominio.CambioExcepcionHorario;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacionPuntual;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoExcepcionHorario;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de {@code calendario} para la validacion inversa puntual: dice que objetos puntuales de
 * {@code (salonId, fecha)} quedarian fuera del horario si se aplicara un
 * {@link CambioExcepcionHorario}.
 *
 * <p>Un solo adapter para los dos objetos puntuales, porque ambas consultas comparten
 * {@code (salonId, fecha)} y viven en el mismo modulo; separarlas duplicaria el bean sin ganancia.
 *
 * <p><b>Turnos EXCEPCION</b> (activos): {@code TurnoInstructorRepository.buscarExcepcionesPorSalonYFecha}
 * ya filtra {@code activo = true AND tipo = 'EXCEPCION'}. No consulta RECURRENTE (la programacion
 * recurrente nunca bloquea una excepcion puntual) ni CANCELACION (marcador de "no atiende" el dia
 * completo, no un intervalo operativo real: nada puede quedar fuera de un horario que no valida).
 *
 * <p><b>Reservas CONFIRMADA</b>: {@code ReservaRepository.findBySalonIdAndFechaAndEstado} con
 * {@code CONFIRMADA}. Una reserva CANCELADA ya no compromete nada y no se consulta.
 *
 * <p>El adapter no decide la politica: solo reporta. El writer de {@code ubicaciones} decide
 * rechazar o continuar segun la lista devuelta.
 */
@Component
public class ImpactoPuntualEnExcepcionHorario implements ValidadorImpactoExcepcionHorario {

    private final TurnoInstructorRepository turnoRepository;
    private final ReservaRepository reservaRepository;

    public ImpactoPuntualEnExcepcionHorario(
            TurnoInstructorRepository turnoRepository, ReservaRepository reservaRepository) {
        this.turnoRepository = turnoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    public List<ConflictoProgramacionPuntual> evaluar(CambioExcepcionHorario cambio) {
        List<ConflictoProgramacionPuntual> conflictos = new ArrayList<>();

        List<TurnoInstructor> turnosExcepcion =
                turnoRepository.buscarExcepcionesPorSalonYFecha(cambio.salonId(), cambio.fecha());
        turnosExcepcion.stream()
                .filter(turno -> !cambio.admite(turno.getHoraInicio(), turno.getHoraFin()))
                .map(turno -> ConflictoProgramacionPuntual.turnoExcepcion(
                        turno.getId(), turno.getHoraInicio() + "-" + turno.getHoraFin()))
                .forEach(conflictos::add);

        List<Reserva> reservasConfirmadas = reservaRepository
                .findBySalonIdAndFechaAndEstado(cambio.salonId(), cambio.fecha(), Reserva.Estado.CONFIRMADA);
        reservasConfirmadas.stream()
                .filter(reserva -> !cambio.admite(reserva.getHoraInicio(), reserva.getHoraFin()))
                .map(reserva -> ConflictoProgramacionPuntual.reservaConfirmada(
                        reserva.getId(), reserva.getHoraInicio() + "-" + reserva.getHoraFin()))
                .forEach(conflictos::add);

        return conflictos;
    }
}
