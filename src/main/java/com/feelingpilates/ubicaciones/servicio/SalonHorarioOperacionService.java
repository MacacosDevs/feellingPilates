package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dto.CerrarHorarioSalonRequest;
import com.feelingpilates.ubicaciones.dto.HorarioOperacionVersionResponse;
import com.feelingpilates.ubicaciones.dto.VersionarHorarioSalonRequest;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Capa de aplicacion HTTP de horarios versionados: scope por salon, mapeo DTO&lt;-&gt;comando,
 * traduccion de conflictos de estado a 409 y consulta de historial. No duplica reglas de dominio
 * (F2B.3b.2a, §13 del diseño): {@link VersionarHorarioOperacion} y {@link CerrarHorarioOperacion}
 * siguen siendo la unica fuente de verdad de la clasificacion temporal.
 *
 * <p>Sin {@code @Transactional(readOnly = true)} a nivel de clase: los writers abren su propia
 * transaccion {@code REQUIRED} y {@code SalonLock} exige una transaccion de escritura
 * (§23 del diseño).
 */
@Service
public class SalonHorarioOperacionService {

    private final AutorizadorSalon autorizadorSalon;
    private final SalonRepository salonRepository;
    private final HorarioOperacionRepository horarioOperacionRepository;
    private final VersionarHorarioOperacion versionarHorarioOperacion;
    private final CerrarHorarioOperacion cerrarHorarioOperacion;

    public SalonHorarioOperacionService(
            AutorizadorSalon autorizadorSalon,
            SalonRepository salonRepository,
            HorarioOperacionRepository horarioOperacionRepository,
            VersionarHorarioOperacion versionarHorarioOperacion,
            CerrarHorarioOperacion cerrarHorarioOperacion) {
        this.autorizadorSalon = autorizadorSalon;
        this.salonRepository = salonRepository;
        this.horarioOperacionRepository = horarioOperacionRepository;
        this.versionarHorarioOperacion = versionarHorarioOperacion;
        this.cerrarHorarioOperacion = cerrarHorarioOperacion;
    }

    public HorarioOperacionVersionResponse versionar(UUID actorId, UUID salonId, VersionarHorarioSalonRequest request) {
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonId);
        return traduciendoConflictosDeEstado(() -> aVersionResponse(versionarHorarioOperacion.ejecutar(
                new VersionarHorarioOperacion.VersionarHorario(
                        salonId, request.diaSemana(), request.efectivoDesde(),
                        request.horaApertura(), request.horaCierre()))));
    }

    public HorarioOperacionVersionResponse cerrar(UUID actorId, UUID salonId, CerrarHorarioSalonRequest request) {
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.administrar", salonId);
        return traduciendoConflictosDeEstado(() -> aVersionResponse(cerrarHorarioOperacion.ejecutar(
                new CerrarHorarioOperacion.CerrarHorario(salonId, request.diaSemana(), request.efectivoDesde()))));
    }

    @Transactional(readOnly = true)
    public List<HorarioOperacionVersionResponse> consultarHistorial(UUID actorId, UUID salonId, Short diaSemana) {
        // 1. scope: nada se lee antes de saber que el actor puede mirar este salon.
        autorizadorSalon.verificarAccesoSalon(actorId, "salon.leer", salonId);

        // 2. validacion sintactica del filtro, si viene informado.
        if (diaSemana != null && (diaSemana < 0 || diaSemana > 6)) {
            throw new ValidacionException(HorarioOperacionErrores.DIA_SEMANA_INVALIDO);
        }

        // 3. existencia del salon.
        if (!salonRepository.existsById(salonId)) {
            throw new ResourceNotFoundException("Salón no encontrado");
        }

        // 4. consulta.
        return horarioOperacionRepository.findVersionesOrdenadas(salonId, diaSemana).stream()
                .map(this::aVersionResponse)
                .toList();
    }

    /**
     * Traduce una {@code ValidacionException} whitelisted (choque con el historial o la
     * programacion) a {@code ConflictException}, conservando el mismo mensaje -y por tanto el
     * mismo codigo estable, que {@link GlobalExceptionHandler} extrae igual en ambos casos-.
     * Cualquier otra {@code ValidacionException} sigue siendo 400: la whitelist es cerrada
     * (F2B.3b.2a, §11.5 del diseño).
     */
    private <T> T traduciendoConflictosDeEstado(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (ValidacionException e) {
            if (HorarioOperacionErrores.esConflictoDeEstado(e.getMessage())) {
                throw new ConflictException(e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Mapea escalares ya cargados; nunca {@code getSalon()}: la entidad queda detached al salir de
     * la transaccion del writer y {@code salon} es {@code FetchType.LAZY} (F2B.3b.2a, §9 del
     * diseño).
     */
    private HorarioOperacionVersionResponse aVersionResponse(HorarioOperacion h) {
        return new HorarioOperacionVersionResponse(
                h.getDiaSemana(), h.getHoraApertura(), h.getHoraCierre(), h.getVigenteDesde(), h.getVigenteHasta());
    }
}
