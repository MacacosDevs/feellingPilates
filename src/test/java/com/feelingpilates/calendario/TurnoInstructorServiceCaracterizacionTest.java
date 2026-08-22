package com.feelingpilates.calendario;

import com.feelingpilates.calendario.dto.AsignacionInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorRequest;
import com.feelingpilates.calendario.dto.TurnoInstructorResponse;
import com.feelingpilates.calendario.entidad.TurnoInstructor;
import com.feelingpilates.calendario.entidad.TurnoInstructorAsignacion;
import com.feelingpilates.calendario.repositorio.TurnoInstructorAsignacionRepository;
import com.feelingpilates.calendario.repositorio.TurnoInstructorRepository;
import com.feelingpilates.calendario.servicio.TurnoInstructorService;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TurnoInstructorServiceCaracterizacionTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ARIADNA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ALBERTO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID REFORMER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID MAT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID TURNO_NUEVO_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final LocalDate LUNES = LocalDate.of(2026, 8, 24);

    private TurnoInstructorRepository turnoRepository;
    private TurnoInstructorAsignacionRepository asignacionRepository;
    private UsuarioRepository usuarioRepository;
    private SalonRepository salonRepository;
    private HorarioOperacionRepository horarioOperacionRepository;
    private TipoActividadRepository tipoActividadRepository;
    private SalonHorarioExcepcionRepository salonHorarioExcepcionRepository;
    private TurnoInstructorService service;
    private List<TurnoInstructorAsignacion> asignacionesGuardadas;

    private Salon salon;
    private Usuario ariadna;
    private Usuario alberto;
    private TipoActividad reformer;
    private TipoActividad mat;

    @BeforeEach
    void preparar() {
        turnoRepository = mock(TurnoInstructorRepository.class);
        asignacionRepository = mock(TurnoInstructorAsignacionRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        salonRepository = mock(SalonRepository.class);
        horarioOperacionRepository = mock(HorarioOperacionRepository.class);
        tipoActividadRepository = mock(TipoActividadRepository.class);
        salonHorarioExcepcionRepository = mock(SalonHorarioExcepcionRepository.class);
        AutorizadorSalon autorizadorSalon = mock(AutorizadorSalon.class);

        salon = salon(SALON_ID, "Juriquilla");
        reformer = actividad(REFORMER_ID, "Reformer");
        mat = actividad(MAT_ID, "Mat");
        ariadna = instructor(ARIADNA_ID, "Ariadna", salon, reformer, mat);
        alberto = instructor(ALBERTO_ID, "Alberto", salon, reformer);

        when(salonRepository.findById(SALON_ID)).thenReturn(Optional.of(salon));
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(ariadna));
        when(tipoActividadRepository.findAllById(any())).thenReturn(List.of(reformer));
        when(horarioOperacionRepository.findBySalonIdOrderByDiaSemana(SALON_ID))
                .thenReturn(List.of(horarioLunes(LocalTime.of(8, 0), LocalTime.of(20, 0))));
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(any(), any()))
                .thenReturn(Optional.empty());
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1)).thenReturn(List.of());
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(SALON_ID, LUNES)).thenReturn(List.of());
        when(turnoRepository.save(any(TurnoInstructor.class))).thenAnswer(invocacion -> {
            TurnoInstructor turno = invocacion.getArgument(0);
            if (turno.getId() == null) {
                turno.setId(TURNO_NUEVO_ID);
            }
            return turno;
        });
        when(asignacionRepository.saveAll(anyList())).thenAnswer(invocacion -> {
            List<TurnoInstructorAsignacion> filas = invocacion.getArgument(0);
            asignacionesGuardadas = List.copyOf(filas);
            return filas;
        });

        service = new TurnoInstructorService(
                turnoRepository,
                asignacionRepository,
                usuarioRepository,
                salonRepository,
                horarioOperacionRepository,
                tipoActividadRepository,
                salonHorarioExcepcionRepository,
                autorizadorSalon);
    }

    @Test
    void permiteBloqueDentroDelHorarioOperativo() {
        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(9, 0, 12, 0));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(resultado.horaFin()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void rechazaBloqueFueraDelHorarioOperativo() {
        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(7, 59, 10, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario de atención");
    }

    @Test
    void permiteInicioIgualALaApertura() {
        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(8, 0, 10, 0));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void permiteFinIgualAlCierre() {
        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(18, 0, 20, 0));

        assertThat(resultado.horaFin()).isEqualTo(LocalTime.of(20, 0));
    }

    @Test
    void horarioEspecialAmpliadoSustituyeAlSemanalParaEsaFecha() {
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(horarioEspecial(LocalTime.of(7, 0), LocalTime.of(22, 0))));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, excepcion(7, 0, 8, 0));

        assertThat(resultado.fecha()).isEqualTo(LUNES);
        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(7, 0));
    }

    @Test
    void horarioEspecialReducidoLimitaElBloqueParaEsaFecha() {
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(horarioEspecial(LocalTime.of(8, 0), LocalTime.of(14, 0))));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(13, 0, 15, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario especial");
    }

    @Test
    void salonCerradoPorExcepcionRechazaElBloqueDeEsaFecha() {
        SalonHorarioExcepcion cierre = new SalonHorarioExcepcion();
        cierre.setSalon(salon);
        cierre.setFecha(LUNES);
        cierre.setCerrado(true);
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(cierre));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(10, 0, 12, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cerrado");
    }

    @Test
    void permiteBloquesContiguosConSemanticaInicioInclusivoFinExclusivo() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 12, 0)));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(12, 0, 14, 0));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void rechazaTraslapeDeUnMinuto() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 12, 1)));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(12, 0, 14, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    @Test
    void rechazaBloqueCompletamenteContenidoEnOtro() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 14, 0)));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(10, 0, 12, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    @Test
    void rechazaBloqueQueContieneAUnExistente() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(10, 0, 12, 0)));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(8, 0, 14, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    @Test
    void permiteBloquesCompletamenteSeparados() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 10, 0)));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(12, 0, 14, 0));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void permiteVariosInstructoresConLaMismaActividadEnElMismoBloque() {
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(ariadna, alberto));
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(
                        asignacion(ARIADNA_ID, REFORMER_ID, null, null),
                        asignacion(ALBERTO_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, request);

        assertThat(resultado.instructores()).extracting(TurnoInstructorResponse.InstructorResumen::nombre)
                .containsExactly("Alberto", "Ariadna");
        assertThat(resultado.asignaciones())
                .allSatisfy(a -> assertThat(a.actividades())
                        .extracting(TurnoInstructorResponse.ActividadResumen::nombre)
                        .containsExactly("Reformer"));
    }

    @Test
    void conservaInstructorActividadRangoYPertenenciaAlTurnoEnLaAsignacion() {
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(asignacion(
                        ARIADNA_ID,
                        REFORMER_ID,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0))),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(14, 0));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, request);

        assertThat(resultado.asignaciones()).singleElement().satisfies(asignacion -> {
            assertThat(asignacion.instructorId()).isEqualTo(ARIADNA_ID);
            assertThat(asignacion.actividades()).singleElement()
                    .extracting(TurnoInstructorResponse.ActividadResumen::id)
                    .isEqualTo(REFORMER_ID);
            assertThat(asignacion.horaInicio()).isEqualTo(LocalTime.of(10, 0));
            assertThat(asignacion.horaFin()).isEqualTo(LocalTime.of(12, 0));
        });
        assertThat(asignacionesGuardadas).singleElement().satisfies(asignacion -> {
            assertThat(asignacion.getTurno().getId()).isEqualTo(TURNO_NUEVO_ID);
            assertThat(asignacion.getUsuario().getId()).isEqualTo(ARIADNA_ID);
            assertThat(asignacion.getTipoActividad().getId()).isEqualTo(REFORMER_ID);
        });
        verify(asignacionRepository).saveAll(anyList());
    }

    @Test
    void rechazaRangoDeAsignacionFueraDelTurno() {
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(asignacion(
                        ARIADNA_ID,
                        REFORMER_ID,
                        LocalTime.of(7, 59),
                        LocalTime.of(10, 0))),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(14, 0));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, request))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("dentro del horario del bloque");
    }

    @Test
    void rechazaActividadQueNoEsEspecialidadDelInstructor() {
        when(tipoActividadRepository.findAllById(any())).thenReturn(List.of(actividad(
                UUID.fromString("77777777-7777-7777-7777-777777777777"), "Cadillac")));
        UUID cadillacId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(asignacion(ARIADNA_ID, cadillacId, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, request))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no tiene la especialidad");
    }

    private TurnoInstructorRequest recurrente(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        return new TurnoInstructorRequest(
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(horaInicio, minutoInicio),
                LocalTime.of(horaFin, minutoFin));
    }

    private TurnoInstructorRequest excepcion(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        return new TurnoInstructorRequest(
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.EXCEPCION,
                null,
                LUNES,
                LocalTime.of(horaInicio, minutoInicio),
                LocalTime.of(horaFin, minutoFin));
    }

    private AsignacionInstructorRequest asignacion(
            UUID instructorId, UUID actividadId, LocalTime horaInicio, LocalTime horaFin) {
        return new AsignacionInstructorRequest(instructorId, List.of(actividadId), horaInicio, horaFin);
    }

    private HorarioOperacion horarioLunes(LocalTime apertura, LocalTime cierre) {
        HorarioOperacion horario = new HorarioOperacion();
        horario.setSalon(salon);
        horario.setDiaSemana((short) 1);
        horario.setHoraApertura(apertura);
        horario.setHoraCierre(cierre);
        return horario;
    }

    private SalonHorarioExcepcion horarioEspecial(LocalTime apertura, LocalTime cierre) {
        SalonHorarioExcepcion excepcion = new SalonHorarioExcepcion();
        excepcion.setSalon(salon);
        excepcion.setFecha(LUNES);
        excepcion.setCerrado(false);
        excepcion.setHoraApertura(apertura);
        excepcion.setHoraCierre(cierre);
        return excepcion;
    }

    private TurnoInstructor turnoExistente(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        TurnoInstructor turno = new TurnoInstructor();
        turno.setId(UUID.randomUUID());
        turno.setSalon(salon);
        turno.setTipo(TurnoInstructor.Tipo.RECURRENTE);
        turno.setDiaSemana((short) 1);
        turno.setHoraInicio(LocalTime.of(horaInicio, minutoInicio));
        turno.setHoraFin(LocalTime.of(horaFin, minutoFin));
        turno.getInstructores().add(ariadna);
        return turno;
    }

    private Salon salon(UUID id, String nombre) {
        Salon resultado = new Salon();
        resultado.setId(id);
        resultado.setNombre(nombre);
        return resultado;
    }

    private TipoActividad actividad(UUID id, String nombre) {
        TipoActividad resultado = new TipoActividad();
        resultado.setId(id);
        resultado.setNombre(nombre);
        return resultado;
    }

    private Usuario instructor(UUID id, String nombre, Salon salonInstructor, TipoActividad... especialidades) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setCorreo(nombre.toLowerCase() + "@example.com");
        usuario.getEspecialidades().addAll(List.of(especialidades));

        Rol rol = new Rol();
        rol.setNombre(Rol.INSTRUCTOR);
        usuario.getRoles().add(new UsuarioRol(usuario, rol, salonInstructor));
        return usuario;
    }
}
