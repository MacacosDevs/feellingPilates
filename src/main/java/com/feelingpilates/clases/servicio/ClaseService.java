package com.feelingpilates.clases.servicio;

import com.feelingpilates.clases.dto.ClaseResponse;
import com.feelingpilates.clases.entidad.Clase;
import com.feelingpilates.clases.repositorio.ClaseRepository;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClaseService {

    // Un rango sin tope permitiria a un cliente forzar materializar años de
    // clases en un solo request; 45 dias cubre de sobra cualquier calendario
    // mensual que muestre la app.
    private static final long RANGO_MAXIMO_DIAS = 45;

    private final ClaseRepository claseRepository;
    private final ClaseGeneracionService generacionService;
    private final SalonRepository salonRepository;

    public ClaseService(
            ClaseRepository claseRepository,
            ClaseGeneracionService generacionService,
            SalonRepository salonRepository) {
        this.claseRepository = claseRepository;
        this.generacionService = generacionService;
        this.salonRepository = salonRepository;
    }

    public List<ClaseResponse> listarPublico(LocalDate desde, LocalDate hasta, UUID salonId) {
        validarRango(desde, hasta);
        List<Salon> salones = salonId != null
                ? salonRepository.findById(salonId).map(List::of).orElse(List.of())
                : salonRepository.findByActivoTrue();

        for (Salon salon : salones) {
            for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
                generacionService.asegurarMaterializado(salon.getId(), fecha);
            }
        }

        List<UUID> salonIds = salones.stream().map(Salon::getId).toList();
        return claseRepository.findBySalonIdInAndFechaBetweenOrderByFechaAscHoraInicioAsc(salonIds, desde, hasta)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    public ClaseResponse obtener(UUID claseId) {
        Clase clase = claseRepository.findById(claseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
        return aResponse(clase);
    }

    public List<ClaseResponse> listarPorInstructor(UUID instructorId, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        // El instructor puede dar clases en mas de un salon: se materializa en todos los activos,
        // igual que el listado publico, y luego se filtra por instructor al leer.
        for (Salon salon : salonRepository.findByActivoTrue()) {
            for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
                generacionService.asegurarMaterializado(salon.getId(), fecha);
            }
        }
        return claseRepository.findByInstructorIdAndFechaBetweenOrderByFechaAscHoraInicioAsc(instructorId, desde, hasta)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new ValidacionException("El rango de fechas es inválido");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > RANGO_MAXIMO_DIAS) {
            throw new ValidacionException("El rango de fechas no puede superar " + RANGO_MAXIMO_DIAS + " días");
        }
    }

    // Sin modificador (package-private): ClaseReservaService, en este mismo paquete,
    // la reutiliza para embeber la clase dentro de ClaseReservaResponse.
    ClaseResponse aResponse(Clase c) {
        long ocupados = claseRepository.contarOcupados(c.getId());
        return new ClaseResponse(
                c.getId(),
                c.getSalon().getId(), c.getSalon().getNombre(),
                c.getTipoActividad().getId(), c.getTipoActividad().getNombre(),
                c.getInstructor().getId(), c.getInstructor().getNombre(),
                c.getFecha(), c.getHoraInicio(), c.getHoraFin(),
                c.getCapacidad(), (int) ocupados,
                c.getEstado().name());
    }
}
