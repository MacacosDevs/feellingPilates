package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonRequest;
import com.feelingpilates.ubicaciones.dto.SalonHorarioExcepcionResponse;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Excepciones puntuales al horario semanal de un salon: cerrado por festivo, u horario especial esa fecha. */
@Service
@Transactional
public class SalonHorarioExcepcionService {

    private final SalonHorarioExcepcionRepository excepcionRepository;
    private final SalonRepository salonRepository;
    private final AutorizadorSalon autorizadorSalon;

    public SalonHorarioExcepcionService(
            SalonHorarioExcepcionRepository excepcionRepository,
            SalonRepository salonRepository,
            AutorizadorSalon autorizadorSalon) {
        this.excepcionRepository = excepcionRepository;
        this.salonRepository = salonRepository;
        this.autorizadorSalon = autorizadorSalon;
    }

    @Transactional(readOnly = true)
    public List<SalonHorarioExcepcionResponse> listarPorRango(
            UUID actorId, UUID salonId, LocalDate desde, LocalDate hasta) {
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.leer", salonId);
        return excepcionRepository.findBySalonIdAndFechaBetweenAndActivoTrueOrderByFecha(salonId, desde, hasta)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public SalonHorarioExcepcionResponse guardar(
            UUID actorId, UUID salonId, GuardarExcepcionSalonRequest request) {
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonId);
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));

        if (!request.cerrado()) {
            if (request.horaApertura() == null || request.horaCierre() == null) {
                throw new ValidacionException("Un horario especial requiere hora de apertura y cierre");
            }
            if (!request.horaCierre().isAfter(request.horaApertura())) {
                throw new ValidacionException("La hora de cierre debe ser posterior a la de apertura");
            }
        }

        SalonHorarioExcepcion excepcion = excepcionRepository
                .findBySalonIdAndFechaAndActivoTrue(salonId, request.fecha())
                .orElseGet(SalonHorarioExcepcion::new);
        excepcion.setSalon(salon);
        excepcion.setFecha(request.fecha());
        excepcion.setCerrado(request.cerrado());
        excepcion.setHoraApertura(request.cerrado() ? null : request.horaApertura());
        excepcion.setHoraCierre(request.cerrado() ? null : request.horaCierre());
        excepcion.setActivo(true);

        return aResponse(excepcionRepository.save(excepcion));
    }

    public void eliminar(UUID actorId, UUID salonIdContextual, UUID id) {
        SalonHorarioExcepcion excepcion = excepcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Excepción no encontrada"));
        UUID salonIdReal = excepcion.getSalon().getId();
        if (!salonIdReal.equals(salonIdContextual)) {
            throw new ResourceNotFoundException("Excepción no encontrada");
        }
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonIdReal);
        excepcion.setActivo(false);
        excepcionRepository.save(excepcion);
    }

    private SalonHorarioExcepcionResponse aResponse(SalonHorarioExcepcion e) {
        return new SalonHorarioExcepcionResponse(e.getId(), e.getFecha(), e.isCerrado(), e.getHoraApertura(), e.getHoraCierre());
    }
}
