package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dto.EspacioRequest;
import com.feelingpilates.ubicaciones.dto.EspacioResponse;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionRequest;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionResponse;
import com.feelingpilates.ubicaciones.dto.MaquinaItem;
import com.feelingpilates.ubicaciones.dto.MaquinaItemResponse;
import com.feelingpilates.ubicaciones.dto.SalonDetalleResponse;
import com.feelingpilates.ubicaciones.dto.SalonRequest;
import com.feelingpilates.ubicaciones.dto.TipoActividadResponse;
import com.feelingpilates.ubicaciones.entidad.Espacio;
import com.feelingpilates.ubicaciones.entidad.EspacioMaquina;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Municipio;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.entidad.TipoMaquina;
import com.feelingpilates.ubicaciones.repositorio.EspacioMaquinaRepository;
import com.feelingpilates.ubicaciones.repositorio.EspacioRepository;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.MunicipioRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoMaquinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class SalonService {

    private final SalonRepository salonRepository;
    private final EspacioRepository espacioRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final TipoMaquinaRepository tipoMaquinaRepository;
    private final EspacioMaquinaRepository espacioMaquinaRepository;
    private final MunicipioRepository municipioRepository;

    public SalonService(
            SalonRepository salonRepository,
            EspacioRepository espacioRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            TipoActividadRepository tipoActividadRepository,
            TipoMaquinaRepository tipoMaquinaRepository,
            EspacioMaquinaRepository espacioMaquinaRepository,
            MunicipioRepository municipioRepository) {
        this.salonRepository = salonRepository;
        this.espacioRepository = espacioRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.tipoMaquinaRepository = tipoMaquinaRepository;
        this.espacioMaquinaRepository = espacioMaquinaRepository;
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
        reemplazarEspacios(salon, request.espacios());
        return mapDetalle(salon);
    }

    public SalonDetalleResponse actualizar(UUID id, SalonRequest request) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));
        aplicarDatosBase(salon, request);
        salon = salonRepository.save(salon);
        reemplazarHorarios(salon, request.horarios());
        reemplazarEspacios(salon, request.espacios());
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
        salon.setTiposActividad(new java.util.HashSet<>(tipos));
    }

    private void reemplazarHorarios(Salon salon, List<HorarioOperacionRequest> horarios) {
        horarioOperacionRepository.deleteBySalonId(salon.getId());
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

    private void reemplazarEspacios(Salon salon, List<EspacioRequest> espacios) {
        for (Espacio existente : espacioRepository.findBySalonIdAndActivoTrue(salon.getId())) {
            espacioMaquinaRepository.deleteByEspacioId(existente.getId());
        }
        espacioRepository.deleteBySalonId(salon.getId());

        if (espacios == null) return;

        for (EspacioRequest e : espacios) {
            Espacio espacio = new Espacio();
            espacio.setSalon(salon);
            espacio.setNombre(e.nombre());
            espacio.setCapacidad(e.capacidad());
            espacio.setPermitePareja(e.permitePareja());
            espacio = espacioRepository.save(espacio);

            List<MaquinaItem> maquinas = e.maquinas() == null ? List.of() : e.maquinas();
            for (MaquinaItem m : maquinas) {
                TipoMaquina tipoMaquina = tipoMaquinaRepository.findById(m.tipoMaquinaId())
                        .orElseThrow(() -> new ValidacionException("Tipo de máquina inválido"));
                espacioMaquinaRepository.save(new EspacioMaquina(espacio, tipoMaquina, m.cantidad()));
            }
        }
    }

    private SalonDetalleResponse mapDetalle(Salon salon) {
        Municipio.Id municipioId = new Municipio.Id(salon.getEstadoId(), salon.getMunicipioId());
        Municipio municipio = municipioRepository.findById(municipioId)
                .orElseThrow(() -> new ResourceNotFoundException("Municipio no encontrado"));

        List<TipoActividadResponse> tiposActividad = salon.getTiposActividad().stream()
                .map(t -> new TipoActividadResponse(t.getId(), t.getNombre(), t.getDescripcion(), t.isActivo()))
                .sorted((a, b) -> a.nombre().compareTo(b.nombre()))
                .toList();

        List<HorarioOperacionResponse> horarios = horarioOperacionRepository.findBySalonIdOrderByDiaSemana(salon.getId())
                .stream()
                .map(h -> new HorarioOperacionResponse(h.getId(), h.getDiaSemana(), h.getHoraApertura(), h.getHoraCierre()))
                .toList();

        List<Espacio> espacios = espacioRepository.findBySalonIdAndActivoTrue(salon.getId());
        Map<UUID, List<EspacioMaquina>> maquinasPorEspacio = espacios.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Espacio::getId,
                        e -> espacioMaquinaRepository.findByEspacioId(e.getId())));

        List<EspacioResponse> espaciosResponse = espacios.stream()
                .map(e -> new EspacioResponse(
                        e.getId(), e.getNombre(), e.getCapacidad(), e.isPermitePareja(),
                        maquinasPorEspacio.get(e.getId()).stream()
                                .map(em -> new MaquinaItemResponse(
                                        em.getTipoMaquina().getId(), em.getTipoMaquina().getNombre(), em.getCantidad()))
                                .toList()))
                .toList();

        return new SalonDetalleResponse(
                salon.getId(), salon.getNombre(),
                salon.getEstadoId(), municipio.getEstado().getNombre(),
                salon.getMunicipioId(), municipio.getNombre(),
                salon.getTelefono(), salon.getCalle(), salon.getNumeroExterior(), salon.getNumeroInterior(),
                salon.getColonia(), salon.getCodigoPostal(), salon.getReferencias(), salon.getDireccion(),
                salon.getLatitud(), salon.getLongitud(),
                salon.isActivo(), tiposActividad, horarios, espaciosResponse);
    }
}
