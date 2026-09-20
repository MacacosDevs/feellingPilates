package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.ubicaciones.dominio.DiaSemanaOperacion;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resuelve que version semanal de {@link HorarioOperacion} aplica para un salon en una fecha
 * concreta. No conoce {@code SalonHorarioExcepcion}: solo responde la plantilla semanal base,
 * sin excepciones puntuales (eso es HorarioEfectivoSalon, F2B.2).
 */
@Service
@Transactional(readOnly = true)
public class HorarioOperacionResolver {

    private final HorarioOperacionRepository horarioOperacionRepository;

    public HorarioOperacionResolver(HorarioOperacionRepository horarioOperacionRepository) {
        this.horarioOperacionRepository = horarioOperacionRepository;
    }

    public Optional<HorarioOperacion> resolver(UUID salonId, LocalDate fecha) {
        short diaSemana = DiaSemanaOperacion.desde(fecha.getDayOfWeek());
        List<HorarioOperacion> vigentes = horarioOperacionRepository.findVigente(salonId, diaSemana, fecha);

        if (vigentes.isEmpty()) {
            return Optional.empty();
        }
        if (vigentes.size() > 1) {
            throw new IllegalStateException(
                    "Se encontro mas de una version vigente de horario_operacion para salon "
                            + salonId + " en fecha " + fecha + " (" + vigentes.size() + " filas)");
        }
        return Optional.of(vigentes.get(0));
    }
}
