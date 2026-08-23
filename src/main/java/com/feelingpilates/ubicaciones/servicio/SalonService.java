package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.RangoVigencia;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class SalonService {

    /**
     * Cuarentena de F2B.2: los horarios de operacion ya son versionados en el tiempo, pero todavia
     * no existe el writer de versionado (F2B.3b). Reescribirlos desde el PUT de salon exigiria
     * inferir un {@code efectivoDesde} y borrar historia, asi que un cambio real se rechaza con
     * este codigo estable en vez de aplicarse silenciosamente.
     */
    static final String HORARIOS_REQUIEREN_VERSIONADO =
            "HORARIOS_REQUIEREN_VERSIONADO: los horarios de operación del salón se versionan en el "
                    + "tiempo y no pueden modificarse desde la actualización del salón";

    private final SalonRepository salonRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final TipoActividadRepository tipoActividadRepository;
    private final TipoRecursoRepository tipoRecursoRepository;
    private final SalonRecursoRepository salonRecursoRepository;
    private final MunicipioRepository municipioRepository;
    private final Clock reloj;

    public SalonService(
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            TipoActividadRepository tipoActividadRepository,
            TipoRecursoRepository tipoRecursoRepository,
            SalonRecursoRepository salonRecursoRepository,
            MunicipioRepository municipioRepository,
            Clock reloj) {
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.tipoActividadRepository = tipoActividadRepository;
        this.tipoRecursoRepository = tipoRecursoRepository;
        this.salonRecursoRepository = salonRecursoRepository;
        this.municipioRepository = municipioRepository;
        this.reloj = reloj;
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
        crearHorariosIniciales(salon, request.horarios());
        reemplazarRecursos(salon, request.recursos());
        return mapDetalle(salon);
    }

    public SalonDetalleResponse actualizar(UUID id, SalonRequest request) {
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salón no encontrado"));

        // Se valida ANTES de tocar la entidad: si el request trae un horario distinto, el rechazo
        // no debe dejar persistidos cambios parciales de los demas campos por dirty checking.
        validarHorariosSinCambios(salon.getId(), request.horarios());

        aplicarDatosBase(salon, request);
        salon = salonRepository.save(salon);
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

    /**
     * Alta de los horarios de un salon recien creado. No hay historia todavia que preservar, asi
     * que solo inserta. Conceptualmente distinto de actualizar horarios: aqui no existe version
     * previa que cerrar ni con la cual comparar.
     */
    private void crearHorariosIniciales(Salon salon, List<HorarioOperacionRequest> horarios) {
        if (horarios == null) return;

        for (HorarioOperacionRequest h : horarios) {
            validarRangoHorario(h);
            HorarioOperacion horario = new HorarioOperacion();
            horario.setSalon(salon);
            horario.setDiaSemana(h.diaSemana());
            horario.setHoraApertura(h.horaApertura());
            horario.setHoraCierre(h.horaCierre());
            horarioOperacionRepository.save(horario);
        }
    }

    /**
     * Cuarentena de compatibilidad con el frontend actual, que SIEMPRE envia {@code horarios} en el
     * PUT de salon (incluso al cambiar solo el telefono). No se puede rechazar sin mas todo request
     * con horarios, ni reescribirlos con el patron destructivo delete+insert de antes.
     *
     * <ul>
     *   <li>{@code null} -&gt; el request no habla de horarios: no se tocan.</li>
     *   <li>equivalente a la configuracion semanal efectiva de hoy -&gt; no-op; el resto del salon
     *       si se actualiza.</li>
     *   <li>distinto (incluida la lista vacia sobre un salon con horarios, que es la mutacion
     *       explicita "quitar todos") -&gt; se rechaza: requiere versionado explicito.</li>
     * </ul>
     *
     * La comparacion es semantica y por tanto independiente del orden de la lista.
     */
    private void validarHorariosSinCambios(UUID salonId, List<HorarioOperacionRequest> horarios) {
        if (horarios == null) return;

        Map<Short, RangoHorario> solicitado = canonicalizar(horarios);
        Map<Short, RangoHorario> actual = configuracionSemanalEfectiva(salonId, fechaNegocio());
        if (!solicitado.equals(actual)) {
            throw new ValidacionException(HORARIOS_REQUIEREN_VERSIONADO);
        }
    }

    /** Definicion funcional de un dia de atencion. El id de la fila no es identidad de negocio. */
    private record RangoHorario(LocalTime horaApertura, LocalTime horaCierre) {
    }

    private Map<Short, RangoHorario> canonicalizar(List<HorarioOperacionRequest> horarios) {
        Map<Short, RangoHorario> porDia = new LinkedHashMap<>();
        for (HorarioOperacionRequest h : horarios) {
            validarRangoHorario(h);
            RangoHorario previo = porDia.put(
                    h.diaSemana(), new RangoHorario(h.horaApertura(), h.horaCierre()));
            if (previo != null) {
                throw new ValidacionException(
                        "El día " + h.diaSemana() + " aparece más de una vez en los horarios enviados");
            }
        }
        return porDia;
    }

    private void validarRangoHorario(HorarioOperacionRequest h) {
        if (!h.horaCierre().isAfter(h.horaApertura())) {
            throw new ValidacionException("La hora de cierre debe ser posterior a la de apertura");
        }
    }

    private Map<Short, RangoHorario> configuracionSemanalEfectiva(UUID salonId, LocalDate fecha) {
        Map<Short, RangoHorario> porDia = new LinkedHashMap<>();
        for (HorarioOperacion horario : horariosVigentes(salonId, fecha)) {
            porDia.put(horario.getDiaSemana(), new RangoHorario(horario.getHoraApertura(), horario.getHoraCierre()));
        }
        return porDia;
    }

    /**
     * Versiones de horario del salon vigentes en {@code fecha}, a lo sumo una por dia de la semana.
     * Dos versiones vigentes el mismo dia es un estado imposible: hoy lo impide el
     * UNIQUE(salon_id, dia_semana) y despues de retirarlo lo impedira el versionado. Se falla
     * ruidosamente en vez de quedarse con la primera en silencio.
     */
    private List<HorarioOperacion> horariosVigentes(UUID salonId, LocalDate fecha) {
        List<HorarioOperacion> vigentes = horarioOperacionRepository.findBySalonIdOrderByDiaSemana(salonId).stream()
                .filter(h -> new RangoVigencia(h.getVigenteDesde(), h.getVigenteHasta()).contiene(fecha))
                .sorted(Comparator.comparing(HorarioOperacion::getDiaSemana))
                .toList();

        Map<Short, HorarioOperacion> porDia = new LinkedHashMap<>();
        for (HorarioOperacion horario : vigentes) {
            HorarioOperacion previo = porDia.put(horario.getDiaSemana(), horario);
            if (previo != null) {
                throw new IllegalStateException(
                        "Se encontró más de una versión vigente de horario_operacion para salón "
                                + salonId + ", día " + horario.getDiaSemana() + " en fecha " + fecha);
            }
        }
        return List.copyOf(porDia.values());
    }

    private LocalDate fechaNegocio() {
        return LocalDate.now(reloj);
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

        // El detalle sigue exponiendo UNA configuracion semanal, la efectiva hoy: ni vigencias ni
        // historial salen al contrato HTTP. Los dias sin version vigente simplemente no aparecen.
        List<HorarioOperacionResponse> horarios = horariosVigentes(salon.getId(), fechaNegocio()).stream()
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
