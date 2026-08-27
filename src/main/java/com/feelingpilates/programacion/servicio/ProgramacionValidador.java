package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.ProgramacionInvarianteException;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalon;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Valida candidatos globales contra operación final, maestros e invariantes. */
@Component
public class ProgramacionValidador {

    private final HorarioEfectivoSalon horarioEfectivoSalon;
    private final SalonRepository salonRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoActividadRepository actividadRepository;
    private final ProgramacionDiagnostico diagnostico;

    public ProgramacionValidador(
            HorarioEfectivoSalon horarioEfectivoSalon,
            SalonRepository salonRepository,
            UsuarioRepository usuarioRepository,
            TipoActividadRepository actividadRepository,
            ProgramacionDiagnostico diagnostico) {
        this.horarioEfectivoSalon = horarioEfectivoSalon;
        this.salonRepository = salonRepository;
        this.usuarioRepository = usuarioRepository;
        this.actividadRepository = actividadRepository;
        this.diagnostico = diagnostico;
    }

    public List<OcurrenciaEfectiva> filtrarFailClosedYValidar(List<OcurrenciaEfectiva> candidatos) {
        List<OcurrenciaEfectiva> validos = new ArrayList<>();
        for (OcurrenciaEfectiva candidato : candidatos) {
            String causa = causaOmision(candidato);
            if (causa == null) {
                validos.add(candidato);
            } else {
                diagnostico.registrarOmision(new ProgramacionDiagnostico.Omision(
                        causa, candidato.referencia(), candidato.fecha(), candidato.salonId(),
                        candidato.instructorId(), candidato.tipoActividadId()));
            }
        }
        validarDuplicados(validos);
        validarSolapes(validos);
        return List.copyOf(validos);
    }

    /** Writer-time: el resultado creado/modificado no puede desaparecer por fail-closed. */
    public void validarMutacion(
            List<OcurrenciaEfectiva> candidatos, ReferenciaOcurrencia referenciaMutada) {
        if (referenciaMutada != null) {
            OcurrenciaEfectiva mutada = candidatos.stream()
                    .filter(o -> referenciaMutada.equals(o.referencia()))
                    .findFirst()
                    .orElseThrow(() -> new ValidacionException(ProgramacionErrores.mensaje(
                            ProgramacionErrores.AJUSTE_PROGRAMACION_FORMA_INVALIDA,
                            "La mutación no produjo una ocurrencia efectiva")));
            String causa = causaOmision(mutada);
            if (causa != null) {
                throw new ValidacionException(ProgramacionErrores.mensaje(
                        causa, "El resultado del ajuste no es válido en el estado actual"));
            }
        }
        filtrarFailClosedYValidar(candidatos);
    }

    private String causaOmision(OcurrenciaEfectiva candidato) {
        Salon salon = salonRepository.findById(candidato.salonId()).orElse(null);
        if (salon == null || !salon.isActivo()) {
            return "SALON_INEXISTENTE_O_INACTIVO";
        }
        HorarioEfectivo horario = horarioEfectivoSalon.resolver(candidato.salonId(), candidato.fecha());
        if (!horario.estaAbierto()) {
            return ProgramacionErrores.SALON_NO_OPERATIVO_EN_FECHA;
        }
        if (!horario.contiene(candidato.horaInicio(), candidato.horaFin())) {
            return ProgramacionErrores.AJUSTE_FUERA_DE_HORARIO_EFECTIVO;
        }
        Usuario instructor = usuarioRepository.findById(candidato.instructorId()).orElse(null);
        if (instructor == null || instructor.getEstatus() != Usuario.EstatusUsuario.activo) {
            return "INSTRUCTOR_INEXISTENTE_O_INACTIVO";
        }
        boolean rolInstructor = instructor.getRoles().stream()
                .anyMatch(ur -> Rol.INSTRUCTOR.equals(ur.getRol().getNombre())
                        && (ur.getSalon() == null || candidato.salonId().equals(ur.getSalon().getId())));
        if (!rolInstructor) {
            return "ROL_INSTRUCTOR_AUSENTE";
        }
        TipoActividad actividad = actividadRepository.findById(candidato.tipoActividadId()).orElse(null);
        if (actividad == null || !actividad.isActivo()) {
            return "ACTIVIDAD_INEXISTENTE_O_INACTIVA";
        }
        if (instructor.getEspecialidades().stream()
                .noneMatch(a -> candidato.tipoActividadId().equals(a.getId()))) {
            return "ESPECIALIDAD_AUSENTE";
        }
        return salon.getTiposActividad().stream()
                .anyMatch(a -> candidato.tipoActividadId().equals(a.getId()))
                ? null : "ACTIVIDAD_NO_OFRECIDA_POR_SALON";
    }

    private void validarDuplicados(List<OcurrenciaEfectiva> ocurrencias) {
        Set<ClaveOperativa> vistas = new HashSet<>();
        for (OcurrenciaEfectiva ocurrencia : ocurrencias) {
            ClaveOperativa clave = new ClaveOperativa(
                    ocurrencia.salonId(), ocurrencia.instructorId(), ocurrencia.tipoActividadId(),
                    ocurrencia.fecha(), ocurrencia.horaInicio(), ocurrencia.horaFin());
            if (!vistas.add(clave)) {
                throw new ProgramacionInvarianteException(
                        ProgramacionErrores.OCURRENCIA_EFECTIVA_DUPLICADA,
                        "Existe una ocurrencia efectiva duplicada", ocurrencia.referencia());
            }
        }
    }

    private void validarSolapes(List<OcurrenciaEfectiva> ocurrencias) {
        for (int i = 0; i < ocurrencias.size(); i++) {
            OcurrenciaEfectiva a = ocurrencias.get(i);
            for (int j = i + 1; j < ocurrencias.size(); j++) {
                OcurrenciaEfectiva b = ocurrencias.get(j);
                if (a.instructorId().equals(b.instructorId())
                        && a.horaInicio().isBefore(b.horaFin())
                        && b.horaInicio().isBefore(a.horaFin())) {
                    throw new ProgramacionInvarianteException(
                            ProgramacionErrores.INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA,
                            "El instructor tiene ocurrencias efectivas solapadas", b.referencia());
                }
            }
        }
    }

    private record ClaveOperativa(
            UUID salonId, UUID instructorId, UUID actividadId, LocalDate fecha,
            LocalTime inicio, LocalTime fin) {
    }
}
