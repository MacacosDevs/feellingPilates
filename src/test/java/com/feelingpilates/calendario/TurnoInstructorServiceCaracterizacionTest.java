package com.feelingpilates.calendario;

import com.feelingpilates.calendario.dto.ActualizarTurnoRequest;
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
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private AutorizadorSalon autorizadorSalon;
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
        autorizadorSalon = mock(AutorizadorSalon.class);

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

    /**
     * P0 (Fase 1A.1): {@code TurnoInstructorService.crear} salta explícitamente
     * {@code validarDentroDeHorarioSalon} para tipo CANCELACION (comentario en el código: "cubre el
     * día completo... no tiene sentido validarlo contra el horario del salón"). Se prueba por
     * resultado: un rango claramente fuera del horario operativo (8:00-20:00) se acepta sin lanzar
     * excepción.
     */
    @Test
    void permiteCancelacionFueraDelHorarioOperativoDelSalonPorqueNoValidaHorario() {
        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, cancelacion(21, 0, 22, 0));

        assertThat(resultado.tipo()).isEqualTo(TurnoInstructor.Tipo.CANCELACION);
        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(21, 0));
        verify(horarioOperacionRepository, never()).findBySalonIdOrderByDiaSemana(any());
    }

    /**
     * P0 (Fase 1A.1): igual que arriba, pero para {@code validarSinTraslape}: una CANCELACION que
     * ocupa exactamente el mismo rango que un RECURRENTE existente se acepta sin lanzar "se cruza",
     * porque para tipo CANCELACION el método completo se salta.
     */
    @Test
    void permiteCancelacionQueSeTraslapaConUnRecurrenteExistentePorqueNoValidaTraslape() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 12, 0)));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, cancelacion(8, 0, 12, 0));

        assertThat(resultado.tipo()).isEqualTo(TurnoInstructor.Tipo.CANCELACION);
        verify(turnoRepository, never()).buscarExcepcionesPorSalonYFecha(any(), any());
    }

    /**
     * P0 (Fase 1A.1): dos instructores en el mismo bloque, cada uno con una actividad distinta.
     * Demuestra que la actividad no es propiedad/exclusividad del bloque: cada instructor conserva
     * su propia especialidad asignada.
     */
    @Test
    void permiteInstructoresConActividadesDistintasEnElMismoBloque() {
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(ariadna, alberto));
        when(tipoActividadRepository.findAllById(any())).thenReturn(List.of(reformer, mat));
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(
                        asignacion(ARIADNA_ID, MAT_ID, null, null),
                        asignacion(ALBERTO_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, request);

        assertThat(resultado.asignaciones())
                .extracting(
                        TurnoInstructorResponse.InstructorAsignacionResponse::instructorNombre,
                        a -> a.actividades().stream()
                                .map(TurnoInstructorResponse.ActividadResumen::nombre)
                                .toList())
                .containsExactlyInAnyOrder(
                        tuple("Ariadna", List.of("Mat")),
                        tuple("Alberto", List.of("Reformer")));
    }

    /**
     * P0 (Fase 1A.1): dentro de un mismo bloque (08:00-12:00), dos instructores cubren rangos
     * parciales distintos (08:00-10:00 y 10:00-12:00 respectivamente). Demuestra que el rango
     * pertenece a la asignación de cada instructor y no que todo instructor deba cubrir el turno
     * completo.
     */
    @Test
    void permiteRangosParcialesDistintosPorInstructorDentroDelMismoBloque() {
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(ariadna, alberto));
        when(tipoActividadRepository.findAllById(any())).thenReturn(List.of(reformer, mat));
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(
                        asignacion(ARIADNA_ID, MAT_ID, LocalTime.of(8, 0), LocalTime.of(10, 0)),
                        asignacion(ALBERTO_ID, REFORMER_ID, LocalTime.of(10, 0), LocalTime.of(12, 0))),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 1,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, request);

        assertThat(resultado.asignaciones())
                .extracting(
                        TurnoInstructorResponse.InstructorAsignacionResponse::instructorNombre,
                        TurnoInstructorResponse.InstructorAsignacionResponse::horaInicio,
                        TurnoInstructorResponse.InstructorAsignacionResponse::horaFin)
                .containsExactlyInAnyOrder(
                        tuple("Ariadna", LocalTime.of(8, 0), LocalTime.of(10, 0)),
                        tuple("Alberto", LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void rechazaIntervaloConHoraFinIgualAHoraInicio() {
        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(9, 0, 9, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("hora de fin debe ser posterior");
    }

    @Test
    void rechazaIntervaloConHoraFinAnteriorAHoraInicio() {
        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(10, 0, 9, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("hora de fin debe ser posterior");
    }

    /** P0 (Fase 1A.1): un rango idéntico a uno existente debe quedar rechazado por traslape. */
    @Test
    void rechazaRangoExactamenteIdenticoAUnoExistente() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 12, 0)));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(8, 0, 12, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    /**
     * P0 (Fase 1A.1): dos EXCEPCION de la misma fecha que se traslapan deben quedar rechazadas.
     * Coincide con la invariante física del salón (un espacio, un bloque a la vez) y se protege
     * como comportamiento válido.
     */
    @Test
    void rechazaDosExcepcionesDeLaMismaFechaQueSeTraslapan() {
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(SALON_ID, LUNES))
                .thenReturn(List.of(turnoExcepcionExistente(10, 0, 12, 0)));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(11, 0, 13, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    /**
     * P0 (Fase 1A.1): BUG documentado en la auditoría de Fase 1A (sección C): al crear una
     * EXCEPCION, {@code validarSinTraslape} la compara también contra los RECURRENTE de ese día de
     * la semana. Si la EXCEPCION pretende sustituir/ocupar el mismo horario que un recurrente
     * vigente, HOY es rechazada por "traslape" con ese mismo recurrente, aunque la intención sea
     * reemplazarlo para esa fecha puntual. Esto es una LIMITACION ACTUAL (bug conocido, no
     * corregido en esta fase), no la regla deseada del modelo futuro.
     */
    @Test
    void caracterizaLimitacionActualExcepcionQuePretendeSustituirElRecurrenteEsRechazadaPorTraslape() {
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(turnoExistente(8, 0, 12, 0)));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(8, 0, 12, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    /**
     * P1 (Fase 1A.1): protege el filtro {@code h.getDiaSemana() == diaSemana} en
     * {@code validarDentroDeHorarioSalon}. El salón sólo tiene HorarioOperacion para LUNES (1); un
     * turno recurrente para MARTES (2) no puede usar ese horario aunque exista para otro día.
     */
    @Test
    void rechazaTurnoParaUnDiaSinHorarioOperativoConfiguradoAunqueOtroDiaSiLoTenga() {
        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.RECURRENTE,
                (short) 2,
                null,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, request))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario de atención");
    }

    /** P1 (Fase 1A.1): un salón sin ningún HorarioOperacion configurado rechaza cualquier turno. */
    @Test
    void rechazaTurnoCuandoElSalonNoTieneNingunHorarioOperativoConfigurado() {
        when(horarioOperacionRepository.findBySalonIdOrderByDiaSemana(SALON_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(9, 0, 10, 0)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario de atención");
    }

    /**
     * P1 (Fase 1A.1): {@code actualizarTurno} pasa {@code excluirTurnoId = turno.getId()} a
     * {@code validarSinTraslape}. Conservar el propio rango al actualizar no debe considerarse
     * traslapado consigo mismo.
     */
    @Test
    void actualizarTurnoConservandoSuPropioRangoNoSeConsideraTraslapadoConsigoMismo() {
        TurnoInstructor existente = turnoExistente(8, 0, 12, 0);
        when(turnoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(existente));

        ActualizarTurnoRequest request = new ActualizarTurnoRequest(
                (short) 1,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)));

        TurnoInstructorResponse resultado = service.actualizarTurno(ACTOR_ID, existente.getId(), request);

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(8, 0));
        assertThat(resultado.horaFin()).isEqualTo(LocalTime.of(12, 0));
    }

    /**
     * P1 (Fase 1A.1): mover un turno a un rango que sí choca contra OTRO turno real (distinto id)
     * debe seguir siendo rechazado; la autoexclusión sólo aplica al propio turno.
     */
    @Test
    void actualizarTurnoRechazaTraslapeContraOtroTurnoRealDistinto() {
        TurnoInstructor aActualizar = turnoExistente(8, 0, 10, 0);
        TurnoInstructor otro = turnoExistente(10, 0, 12, 0);
        when(turnoRepository.findById(aActualizar.getId())).thenReturn(Optional.of(aActualizar));
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 1))
                .thenReturn(List.of(aActualizar, otro));

        ActualizarTurnoRequest request = new ActualizarTurnoRequest(
                (short) 1,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)));

        assertThatThrownBy(() -> service.actualizarTurno(ACTOR_ID, aActualizar.getId(), request))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cruza");
    }

    /**
     * P1 (Fase 1A.1): confirma la convención "domingo = 0" (ver
     * {@code TurnoInstructorService.diaSemanaIso}) para EXCEPCION/CANCELACION, cuyo día de la
     * semana se deriva de la fecha. Si se usara 7 en vez de 0, el HorarioOperacion configurado para
     * domingo (diaSemana=0) no se encontraría y el turno sería rechazado.
     */
    @Test
    void excepcionEnDomingoUsaDiaSemanaCeroParaValidarHorarioSemanal() {
        LocalDate domingo = LocalDate.of(2026, 8, 23);
        HorarioOperacion horarioDomingo = new HorarioOperacion();
        horarioDomingo.setSalon(salon);
        horarioDomingo.setDiaSemana((short) 0);
        horarioDomingo.setHoraApertura(LocalTime.of(9, 0));
        horarioDomingo.setHoraCierre(LocalTime.of(14, 0));
        when(horarioOperacionRepository.findBySalonIdOrderByDiaSemana(SALON_ID))
                .thenReturn(List.of(horarioDomingo));
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, domingo))
                .thenReturn(Optional.empty());
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(SALON_ID, domingo)).thenReturn(List.of());
        when(turnoRepository.buscarRecurrentesPorSalonYDia(SALON_ID, (short) 0)).thenReturn(List.of());

        TurnoInstructorRequest request = new TurnoInstructorRequest(
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.EXCEPCION,
                null,
                domingo,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, request);

        assertThat(resultado.fecha()).isEqualTo(domingo);
        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(10, 0));
    }

    /**
     * P1 (Fase 1A.1): protege la matriz de permisos por tipo en {@code crear} contra un cambio
     * accidental. No duplica la suite completa de {@code AutorizadorSalon}, sólo verifica qué
     * permisos se solicitan para cada tipo.
     */
    @Test
    void solicitaPermisoDeGestionarParaTurnoRecurrente() {
        service.crear(ACTOR_ID, recurrente(9, 0, 10, 0));

        ArgumentCaptor<String[]> permisos = ArgumentCaptor.forClass(String[].class);
        verify(autorizadorSalon).verificarAccesoSalon(eq(ACTOR_ID), eq(SALON_ID), permisos.capture());
        assertThat(permisos.getValue()).containsExactly("calendario.gestionar");
    }

    @Test
    void solicitaPermisoDeGestionarOEditarParaExcepcion() {
        service.crear(ACTOR_ID, excepcion(9, 0, 10, 0));

        ArgumentCaptor<String[]> permisos = ArgumentCaptor.forClass(String[].class);
        verify(autorizadorSalon).verificarAccesoSalon(eq(ACTOR_ID), eq(SALON_ID), permisos.capture());
        assertThat(permisos.getValue()).containsExactly("calendario.gestionar", "calendario.editar");
    }

    @Test
    void solicitaPermisoDeGestionarOCancelarParaCancelacion() {
        service.crear(ACTOR_ID, cancelacion(9, 0, 10, 0));

        ArgumentCaptor<String[]> permisos = ArgumentCaptor.forClass(String[].class);
        verify(autorizadorSalon).verificarAccesoSalon(eq(ACTOR_ID), eq(SALON_ID), permisos.capture());
        assertThat(permisos.getValue()).containsExactly("calendario.gestionar", "calendario.cancelar");
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

    private TurnoInstructorRequest cancelacion(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        return new TurnoInstructorRequest(
                List.of(asignacion(ARIADNA_ID, REFORMER_ID, null, null)),
                SALON_ID,
                TurnoInstructor.Tipo.CANCELACION,
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

    private TurnoInstructor turnoExcepcionExistente(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        TurnoInstructor turno = new TurnoInstructor();
        turno.setId(UUID.randomUUID());
        turno.setSalon(salon);
        turno.setTipo(TurnoInstructor.Tipo.EXCEPCION);
        turno.setFecha(LUNES);
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
