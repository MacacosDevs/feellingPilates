package com.feelingpilates.pagos.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.pagos.dto.ActividadPaqueteRequest;
import com.feelingpilates.pagos.dto.ActividadPaqueteResponse;
import com.feelingpilates.pagos.dto.ActualizarPaqueteRequest;
import com.feelingpilates.pagos.dto.CrearPaqueteRequest;
import com.feelingpilates.pagos.dto.PaqueteGestionResponse;
import com.feelingpilates.pagos.entidad.Paquete;
import com.feelingpilates.pagos.entidad.PaqueteActividad;
import com.feelingpilates.pagos.repositorio.PaqueteRepository;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaqueteGestionService {

    private final PaqueteRepository paqueteRepository;
    private final TipoActividadRepository tipoActividadRepository;

    public PaqueteGestionService(PaqueteRepository paqueteRepository, TipoActividadRepository tipoActividadRepository) {
        this.paqueteRepository = paqueteRepository;
        this.tipoActividadRepository = tipoActividadRepository;
    }

    @Transactional(readOnly = true)
    public List<PaqueteGestionResponse> listarTodos() {
        return paqueteRepository.findAllByOrderByActivoDescOrdenAsc().stream().map(this::aResponse).toList();
    }

    @Transactional
    public PaqueteGestionResponse crear(CrearPaqueteRequest request) {
        Paquete paquete = new Paquete();
        aplicarCampos(paquete, request.nombre(), request.descripcion(), request.precioCentavos(),
                request.vigenciaDias(), request.unitarioTexto(), request.destacado(), request.orden());
        reemplazarActividades(paquete, request.actividades());
        return aResponse(paqueteRepository.save(paquete));
    }

    @Transactional
    public PaqueteGestionResponse actualizar(UUID id, ActualizarPaqueteRequest request) {
        Paquete paquete = obtener(id);
        aplicarCampos(paquete, request.nombre(), request.descripcion(), request.precioCentavos(),
                request.vigenciaDias(), request.unitarioTexto(), request.destacado(), request.orden());
        paquete.setActivo(request.activo());
        reemplazarActividades(paquete, request.actividades());
        return aResponse(paqueteRepository.save(paquete));
    }

    @Transactional
    public PaqueteGestionResponse deshabilitar(UUID id) {
        Paquete paquete = obtener(id);
        paquete.setActivo(false);
        return aResponse(paqueteRepository.save(paquete));
    }

    @Transactional
    public PaqueteGestionResponse habilitar(UUID id) {
        Paquete paquete = obtener(id);
        paquete.setActivo(true);
        return aResponse(paqueteRepository.save(paquete));
    }

    private Paquete obtener(UUID id) {
        return paqueteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Paquete no encontrado"));
    }

    private void aplicarCampos(Paquete paquete, String nombre, String descripcion, int precioCentavos,
                                int vigenciaDias, String unitarioTexto, boolean destacado, int orden) {
        paquete.setNombre(nombre);
        paquete.setDescripcion(descripcion);
        paquete.setPrecioCentavos(precioCentavos);
        paquete.setVigenciaDias(vigenciaDias);
        paquete.setUnitarioTexto(unitarioTexto);
        paquete.setDestacado(destacado);
        paquete.setOrden(orden);
    }

    private void reemplazarActividades(Paquete paquete, List<ActividadPaqueteRequest> actividades) {
        paquete.getActividades().clear();
        for (ActividadPaqueteRequest a : actividades) {
            TipoActividad tipo = tipoActividadRepository.findById(a.tipoActividadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
            paquete.getActividades().add(new PaqueteActividad(paquete, tipo, a.cantidadClases()));
        }
    }

    private PaqueteGestionResponse aResponse(Paquete p) {
        return new PaqueteGestionResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioCentavos(),
                p.getVigenciaDias(),
                p.getUnitarioTexto(),
                p.isDestacado(),
                p.isActivo(),
                p.getOrden(),
                p.getActividades().stream()
                        .map(a -> new ActividadPaqueteResponse(
                                a.getTipoActividad().getId(), a.getTipoActividad().getNombre(), a.getCantidadClases()))
                        .toList(),
                p.getCreadoEn());
    }
}
