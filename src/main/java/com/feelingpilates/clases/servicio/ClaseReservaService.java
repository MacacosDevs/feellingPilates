package com.feelingpilates.clases.servicio;

import com.feelingpilates.clases.dto.ClaseReservaResponse;
import com.feelingpilates.clases.entidad.Clase;
import com.feelingpilates.clases.entidad.ClaseReserva;
import com.feelingpilates.clases.repositorio.ClaseRepository;
import com.feelingpilates.clases.repositorio.ClaseReservaRepository;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClaseReservaService {

    // Deja escanear un poco antes de que empiece (llegar temprano es normal),
    // pero no antes que eso ni despues de que la clase ya termino.
    private static final long MINUTOS_ANTES_PERMITIDOS_PARA_CHECKIN = 30;

    private final ClaseReservaRepository claseReservaRepository;
    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClaseService claseService;

    public ClaseReservaService(
            ClaseReservaRepository claseReservaRepository,
            ClaseRepository claseRepository,
            UsuarioRepository usuarioRepository,
            ClaseService claseService) {
        this.claseReservaRepository = claseReservaRepository;
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
        this.claseService = claseService;
    }

    public ClaseReservaResponse reservar(UUID claseId, UUID clienteId) {
        // Bloquea la fila de la clase: dos reservas casi simultaneas para el ultimo
        // lugar no deben poder leer el mismo conteo de cupo antes de insertar.
        Clase clase = claseRepository.findConBloqueoById(claseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
        if (clase.getEstado() != Clase.Estado.PROGRAMADA) {
            throw new ValidacionException("Esta clase ya no está disponible");
        }
        if (LocalDateTime.of(clase.getFecha(), clase.getHoraInicio()).isBefore(LocalDateTime.now())) {
            throw new ValidacionException("Esta clase ya comenzó o ya pasó");
        }
        if (claseReservaRepository.existsByClaseIdAndClienteIdAndEstado(claseId, clienteId, ClaseReserva.Estado.CONFIRMADA)) {
            throw new ConflictException("Ya tienes una reserva en esta clase");
        }
        long ocupados = claseReservaRepository.countByClaseIdAndEstadoNot(claseId, ClaseReserva.Estado.CANCELADA);
        if (ocupados >= clase.getCapacidad()) {
            throw new ConflictException("La clase ya no tiene cupo disponible");
        }

        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ClaseReserva reserva = new ClaseReserva();
        reserva.setClase(clase);
        reserva.setCliente(cliente);
        // flush inmediato: @CreationTimestamp solo se rellena al hacer flush, y la
        // respuesta de este mismo request necesita mostrar creadoEn ya con valor.
        return aResponse(claseReservaRepository.saveAndFlush(reserva));
    }

    public void cancelar(UUID reservaId, UUID clienteId) {
        ClaseReserva reserva = claseReservaRepository.findById(reservaId)
                .filter(r -> r.getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        reserva.setEstado(ClaseReserva.Estado.CANCELADA);
        claseReservaRepository.save(reserva);
    }

    @Transactional(readOnly = true)
    public List<ClaseReservaResponse> listarMias(UUID clienteId) {
        return claseReservaRepository
                .findByClienteIdAndEstadoNotOrderByClase_FechaDescClase_HoraInicioDesc(clienteId, ClaseReserva.Estado.CANCELADA)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClaseReservaResponse> listarAsistentes(UUID claseId, UUID solicitanteId, boolean esStaff) {
        Clase clase = claseRepository.findById(claseId)
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
        if (!esStaff && !clase.getInstructor().getId().equals(solicitanteId)) {
            throw new ResourceNotFoundException("Clase no encontrada");
        }
        return claseReservaRepository.findByClaseIdOrderByCreadoEnAsc(claseId).stream()
                .filter(r -> r.getEstado() != ClaseReserva.Estado.CANCELADA)
                .map(this::aResponse)
                .toList();
    }

    // Idempotente: re-escanear una reserva ya ASISTIO devuelve su estado actual en vez
    // de fallar (el celular del instructor puede volver a leer el mismo QR sin querer).
    public ClaseReservaResponse checkin(UUID reservaId, UUID claseIdEscaneado, UUID instructorId, boolean esStaff) {
        ClaseReserva reserva = claseReservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        Clase clase = reserva.getClase();
        if (!clase.getId().equals(claseIdEscaneado)) {
            throw new ValidacionException("El código no corresponde a esta clase");
        }
        if (!esStaff && !clase.getInstructor().getId().equals(instructorId)) {
            throw new ResourceNotFoundException("Clase no encontrada");
        }
        if (reserva.getEstado() == ClaseReserva.Estado.ASISTIO) {
            return aResponse(reserva);
        }
        if (reserva.getEstado() == ClaseReserva.Estado.CANCELADA) {
            throw new ValidacionException("Esta reserva fue cancelada");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioVentana = LocalDateTime.of(clase.getFecha(), clase.getHoraInicio())
                .minusMinutes(MINUTOS_ANTES_PERMITIDOS_PARA_CHECKIN);
        LocalDateTime finVentana = LocalDateTime.of(clase.getFecha(), clase.getHoraFin());
        if (ahora.isBefore(inicioVentana) || ahora.isAfter(finVentana)) {
            throw new ValidacionException("Fuera de la ventana de check-in de esta clase");
        }

        reserva.setEstado(ClaseReserva.Estado.ASISTIO);
        reserva.setAsistioEn(OffsetDateTime.now());
        return aResponse(claseReservaRepository.save(reserva));
    }

    private ClaseReservaResponse aResponse(ClaseReserva r) {
        return new ClaseReservaResponse(
                r.getId(),
                r.getCliente().getId(), r.getCliente().getNombre(), r.getCliente().getCorreo(),
                r.getEstado().name(),
                r.getAsistioEn(),
                r.getCreadoEn(),
                claseService.aResponse(r.getClase()));
    }
}
