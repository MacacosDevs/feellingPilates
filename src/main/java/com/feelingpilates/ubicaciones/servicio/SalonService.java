package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionRequest;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionResponse;
import com.feelingpilates.ubicaciones.dto.RecursoItem;
import com.feelingpilates.ubicaciones.dto.RecursoItemResponse;
import com.feelingpilates.ubicaciones.dto.SalonDetalleResponse;
import com.feelingpilates.ubicaciones.dto.SalonRequest;
import com.feelingpilates.ubicaciones.dto.TipoActividadResponse;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Municipio;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonRecurso;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.entidad.TipoRecurso;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.MunicipioRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRecursoRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoRecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SalonService {

    private final SalonRepository salonRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final TipoRecursoRepository tipoRecursoRepository;
    private final SalonRecursoRepository salonRecursoRepository;
    private final MunicipioRepository municipioRepository;

    public SalonService(
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            TipoActividadRepository tipoActividadRepository,
            TipoRecursoRepository tipoRecursoRepository,
            SalonRecursoRepository salonRecursoRepository,
            MunicipioRepository municipioRepository) {
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.tipoRecursoRepository = tipoRecursoRepository;
        this.salonRecursoRepository = salonRecursoRepository;
        this.municipioRepository = municipioRepository;
    }

    @Transactional(readOnly = true)
    public SalonDetalleResponse obtenerDetalle(UUID id) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));
        return mapDetalle(salon);
    }

    public SalonDetalleResponse crear(SalonRequest request) {
        Salon salon = new Salon();
        aplicarDatosBase(salon, request);
        salon = salonRepository.save(salon);
        reemplazarHorarios(salon, request.horarios());
        reemplazarRecursos(salon, request.recursos());
        return mapDetalle(salon);
    }

    public SalonDetalleResponse actualizar(UUID id, SalonRequest request) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));
        aplicarDatosBase(salon, request);
        salon = salonRepository.save(salon);
        reemplazarHorarios(salon, request.horarios());
        reemplazarRecursos(salon, request.recursos());
        return mapDetalle(salon);
    }

    private void aplicarDatosBase(Salon salon, SalonRequest request) {
        Municipio municipio = municipioRepository
                .findById(new Municipio.Id(request.estadoId(), request.municipioId()))
                .orElseThrow(() -> new ValidacionException("Estado/municipio inválido"));

        salon.setNombre(request.nombre());
        salon.setEstadoId(municipio.getEstado().getId());
        salon.setMunicipioId(municipio.getId().getId());
        salon.setDireccion(request.direccionCompleta());
        salon.setTelefono(request.telefono());
        salon.setCalle(request.calle());
        salon.setNumeroExterior(request.numeroExterior());
        salon.setNumeroInterior(request.numeroInterior());
        salon.setColonia(request.colonia());
        salon.setCodigoPostal(request.codigoPostal());
        salon.setReferencias(request.referencias());
        salon.setLatitud(request.latitud());
        salon.setLongitud(request.longitud());

        List<UUID> tipoActividadIds = request.tipoActividadIds() == null ? List.of() : request.tipoActividadIds();
        List<TipoActividad> tipos = tipoActividadRepository.findAllById(tipoActividadIds);
        if (tipos.size() != tipoActividadIds.size()) {
            throw new ValidacionException("Alguno de los tipos de actividad no existe");
        }
        salon.setTiposActividad(new HashSet<>(tipos));
    }

    private void reemplazarHorarios(Salon salon, List<HorarioOperacionRequest> horarios) {
        horarioOperacionRepository.deleteBySalonId(salon.getId());
        horarioOperacionRepository.flush();
        if (horarios == null) return;

        for (HorarioOperacionRequest h : horarios) {
            if (!h.horaCierre().isAfter(h.horaApertura())) {
                throw new ValidacionException("La hora de cierre debe ser posterior a la de apertura");
            }
            HorarioOperacion horario = new HorarioOperacion();
            horario.setSalon(salon);
            horario.setDiaSemana(h.diaSemana());
            horario.setHoraApertura(h.horaApertura());
            horario.setHoraCierre(h.horaCierre());
            horarioOperacionRepository.save(horario);
        }
    }

    private void reemplazarRecursos(Salon salon, List<RecursoItem> recursos) {
        salonRecursoRepository.deleteBySalonId(salon.getId());
        salonRecursoRepository.flush();
        if (recursos == null) return;

        for (RecursoItem r : recursos) {
            TipoRecurso tipoRecurso = tipoRecursoRepository.findById(r.tipoRecursoId())
                    .orElseThrow(() -> new ValidacionException("Tipo de recurso inválido"));
            salonRecursoRepository.save(new SalonRecurso(salon, tipoRecurso, r.cantidad()));
        }
    }

    private SalonDetalleResponse mapDetalle(Salon salon) {
        Municipio.Id municipioId = new Municipio.Id(salon.getEstadoId(), salon.getMunicipioId());
        Municipio municipio = municipioRepository.findById(municipioId)
                .orElseThrow(() -> new ResourceNotFoundException("Municipio no encontrado"));

        List<TipoActividadResponse> tiposActividad = salon.getTiposActividad().stream()
                .map(t -> new TipoActividadResponse(
                    t.getId(), t.getNombre(), t.getDescripcion(), t.isActivo(), t.getDuracionMinutos(),
                    t.getParticipantesPorReserva(), t.getEtiquetas()))
                .sorted((a, b) -> a.nombre().compareTo(b.nombre()))
                .toList();

        List<HorarioOperacionResponse> horarios = horarioOperacionRepository.findBySalonIdOrderByDiaSemana(salon.getId())
                .stream()
                .map(h -> new HorarioOperacionResponse(h.getId(), h.getDiaSemana(), h.getHoraApertura(), h.getHoraCierre()))
                .toList();

        List<RecursoItemResponse> recursos = salonRecursoRepository.findBySalonId(salon.getId()).stream()
                .map(sr -> new RecursoItemResponse(sr.getTipoRecurso().getId(), sr.getTipoRecurso().getNombre(), sr.getCantidad()))
                .toList();

        return new SalonDetalleResponse(
                salon.getId(), salon.getNombre(),
                salon.getEstadoId(), municipio.getEstado().getNombre(),
                salon.getMunicipioId(), municipio.getNombre(),
                salon.getTelefono(), salon.getCalle(), salon.getNumeroExterior(), salon.getNumeroInterior(),
                salon.getColonia(), salon.getCodigoPostal(), salon.getReferencias(), salon.getDireccion(),
                salon.getLatitud(), salon.getLongitud(),
                salon.isActivo(), tiposActividad, horarios, recursos);
    }
}
