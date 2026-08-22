package com.feelingpilates.calendario;

import com.feelingpilates.calendario.dto.ReservaRequest;
import com.feelingpilates.calendario.dto.ReservaResponse;
import com.feelingpilates.calendario.entidad.Reserva;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.repositorio.ReservaRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaServiceCaracterizacionTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INSTRUCTOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CLIENTE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ACTIVIDAD_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID RESERVA_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final LocalDate LUNES = LocalDate.of(2026, 8, 24);

    private ReservaRepository reservaRepository;
    private TurnoInstructorRepository turnoRepository;
    private UsuarioRepository usuarioRepository;
    private ReservaService service;

    private Salon salon;
    private Usuario instructor;
    private Usuario cliente;
    private TipoActividad reformer;
    private TurnoInstructor recurrente;

    @BeforeEach
    void preparar() {
        reservaRepository = mock(ReservaRepository.class);
        turnoRepository = mock(TurnoInstructorRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        SalonRepository salonRepository = mock(SalonRepository.class);
        TipoActividadRepository tipoActividadRepository = mock(TipoActividadRepository.class);
        AutorizadorSalon autorizadorSalon = mock(AutorizadorSalon.class);

        salon = new Salon();
        salon.setId(SALON_ID);
        salon.setNombre("Juriquilla");

        reformer = new TipoActividad();
        reformer.setId(ACTIVIDAD_ID);
        reformer.setNombre("Reformer");
        reformer.setDuracionMinutos((short) 60);

        instructor = usuario(INSTRUCTOR_ID, "Ariadna");
        instructor.getEspecialidades().add(reformer);
        cliente = usuario(CLIENTE_ID, "Cliente");
        recurrente = turno(TurnoInstructor.Tipo.RECURRENTE, null, 8, 0, 12, 0);

        when(salonRepository.findById(SALON_ID)).thenReturn(Optional.of(salon));
        when(usuarioRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor));
        when(usuarioRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(tipoActividadRepository.findById(ACTIVIDAD_ID)).thenReturn(Optional.of(reformer));
        when(turnoRepository.buscarPuntualesPorInstructorSalonYFecha(INSTRUCTOR_ID, SALON_ID, LUNES))
                .thenReturn(List.of());
        when(turnoRepository.buscarRecurrentesPorInstructorSalonYDia(INSTRUCTOR_ID, SALON_ID, (short) 1))
                .thenReturn(List.of(recurrente));
        when(reservaRepository.existeTraslape(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), any(Reserva.Estado.class)))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocacion -> {
            Reserva reserva = invocacion.getArgument(0);
            reserva.setId(RESERVA_ID);
            return reserva;
        });

        service = new ReservaService(
                reservaRepository,
                turnoRepository,
                usuarioRepository,
                salonRepository,
                tipoActividadRepository,
                autorizadorSalon);
    }

    @Test
    void reservaConsumeTurnoRecurrenteDeLaFechaYCopiaSuInteraccionActual() {
        ReservaResponse resultado = service.crear(ACTOR_ID, request(LUNES, LocalTime.of(9, 0)));

        assertThat(resultado.id()).isEqualTo(RESERVA_ID);
        assertThat(resultado.salonId()).isEqualTo(SALON_ID);
        assertThat(resultado.instructorId()).isEqualTo(INSTRUCTOR_ID);
        assertThat(resultado.clienteId()).isEqualTo(CLIENTE_ID);
        assertThat(resultado.tipoActividadId()).isEqualTo(ACTIVIDAD_ID);
        assertThat(resultado.fecha()).isEqualTo(LUNES);
        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(resultado.horaFin()).isEqualTo(LocalTime.of(10, 0));
        assertThat(resultado.estado()).isEqualTo(Reserva.Estado.CONFIRMADA);
    }

    @Test
    void duracionDeActividadDefineElFinYDebeCaberEnElTurno() {
        reformer.setDuracionMinutos((short) 30);

        ReservaResponse resultado = service.crear(ACTOR_ID, request(LUNES, LocalTime.of(11, 30)));

        assertThat(resultado.horaFin()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void rechazaReservaCuyoRangoNoCabeEnElTurnoVigente() {
        assertThatThrownBy(() -> service.crear(ACTOR_ID, request(LUNES, LocalTime.of(11, 30))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no tiene turno disponible");
    }

    @Test
    void rechazaReservaSiLaActividadNoEsEspecialidadDelInstructor() {
        instructor.getEspecialidades().clear();

        assertThatThrownBy(() -> service.crear(ACTOR_ID, request(LUNES, LocalTime.of(9, 0))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no está capacitado");
    }

    @Test
    void excepcionDeFechaTienePrecedenciaSobreElRecurrente() {
        TurnoInstructor excepcion = turno(TurnoInstructor.Tipo.EXCEPCION, LUNES, 14, 0, 16, 0);
        when(turnoRepository.buscarPuntualesPorInstructorSalonYFecha(INSTRUCTOR_ID, SALON_ID, LUNES))
                .thenReturn(List.of(excepcion));

        ReservaResponse resultado = service.crear(ACTOR_ID, request(LUNES, LocalTime.of(14, 0)));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(14, 0));
        verify(turnoRepository, never())
                .buscarRecurrentesPorInstructorSalonYDia(INSTRUCTOR_ID, SALON_ID, (short) 1);
    }

    @Test
    void caracterizaLimitacionActualExcepcionSustituyeTodosLosRecurrentesDelInstructorEseDia() {
        TurnoInstructor excepcion = turno(TurnoInstructor.Tipo.EXCEPCION, LUNES, 14, 0, 16, 0);
        when(turnoRepository.buscarPuntualesPorInstructorSalonYFecha(INSTRUCTOR_ID, SALON_ID, LUNES))
                .thenReturn(List.of(excepcion));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, request(LUNES, LocalTime.of(9, 0))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no tiene turno disponible");
        verify(turnoRepository, never())
                .buscarRecurrentesPorInstructorSalonYDia(INSTRUCTOR_ID, SALON_ID, (short) 1);
    }

    @Test
    void cancelacionDeFechaTienePrecedenciaSobreExcepcionYRecurrente() {
        TurnoInstructor excepcion = turno(TurnoInstructor.Tipo.EXCEPCION, LUNES, 14, 0, 16, 0);
        TurnoInstructor cancelacion = turno(TurnoInstructor.Tipo.CANCELACION, LUNES, 0, 0, 23, 59);
        when(turnoRepository.buscarPuntualesPorInstructorSalonYFecha(INSTRUCTOR_ID, SALON_ID, LUNES))
                .thenReturn(List.of(excepcion, cancelacion));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, request(LUNES, LocalTime.of(14, 0))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no tiene turno disponible");
        verify(turnoRepository, never())
                .buscarRecurrentesPorInstructorSalonYDia(INSTRUCTOR_ID, SALON_ID, (short) 1);
    }

    @Test
    void losPuntualesSeConsultanPorLaFechaExactaSolicitada() {
        LocalDate siguienteLunes = LUNES.plusWeeks(1);
        when(turnoRepository.buscarPuntualesPorInstructorSalonYFecha(INSTRUCTOR_ID, SALON_ID, siguienteLunes))
                .thenReturn(List.of());

        ReservaResponse resultado = service.crear(ACTOR_ID, request(siguienteLunes, LocalTime.of(9, 0)));

        assertThat(resultado.fecha()).isEqualTo(siguienteLunes);
        verify(turnoRepository)
                .buscarPuntualesPorInstructorSalonYFecha(INSTRUCTOR_ID, SALON_ID, siguienteLunes);
        verify(turnoRepository)
                .buscarRecurrentesPorInstructorSalonYDia(INSTRUCTOR_ID, SALON_ID, (short) 1);
    }

    private ReservaRequest request(LocalDate fecha, LocalTime inicio) {
        return new ReservaRequest(SALON_ID, INSTRUCTOR_ID, CLIENTE_ID, ACTIVIDAD_ID, fecha, inicio);
    }

    private TurnoInstructor turno(
            TurnoInstructor.Tipo tipo, LocalDate fecha, int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        TurnoInstructor turno = new TurnoInstructor();
        turno.setId(UUID.randomUUID());
        turno.setSalon(salon);
        turno.setTipo(tipo);
        turno.setDiaSemana(tipo == TurnoInstructor.Tipo.RECURRENTE ? (short) 1 : null);
        turno.setFecha(tipo == TurnoInstructor.Tipo.RECURRENTE ? null : fecha);
        turno.setHoraInicio(LocalTime.of(horaInicio, minutoInicio));
        turno.setHoraFin(LocalTime.of(horaFin, minutoFin));
        turno.getInstructores().add(instructor);
        return turno;
    }

    private Usuario usuario(UUID id, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setCorreo(nombre.toLowerCase() + "@example.com");
        return usuario;
    }
}
