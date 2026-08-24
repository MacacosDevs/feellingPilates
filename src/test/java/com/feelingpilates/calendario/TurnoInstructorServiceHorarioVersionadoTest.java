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
import com.feelingpilates.ubicaciones.servicio.SalonLock;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalon;
import com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolver;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F2B.2: el horario de operacion es versionado en el tiempo, asi que un turno RECURRENTE (una regla
 * abierta al futuro, sin vigencia propia) ya no se valida contra "alguna" fila de horario, sino
 * contra TODAS las versiones que rigen de hoy en adelante, y solo si esas versiones cubren
 * {@code [hoy, +infinito)} sin huecos.
 *
 * <p>El UNIQUE(salon_id, dia_semana) sigue vigente en PostgreSQL, asi que los escenarios con varias
 * versiones del mismo salon/dia se ejercitan por mock: son el comportamiento que debera regir
 * cuando el UNIQUE se retire, no filas que hoy puedan existir en la base.
 */
class TurnoInstructorServiceHorarioVersionadoTest {

    private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ARIADNA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REFORMER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final LocalDate HOY = LocalDate.of(2026, 8, 23);
    /** 2026-08-24 es lunes: dia_semana = 1, el dia que usan los turnos de estos casos. */
    private static final LocalDate LUNES = LocalDate.of(2026, 8, 24);
    private static final LocalDate AGO_31 = LocalDate.of(2026, 8, 31);
    private static final LocalDate SEP_1 = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEP_15 = LocalDate.of(2026, 9, 15);
    private static final Clock RELOJ = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    private static final String FUERA_DE_HORARIO = "horario de atención";

    private TurnoInstructorRepository turnoRepository;
    private HorarioOperacionRepository horarioOperacionRepository;
    private SalonHorarioExcepcionRepository salonHorarioExcepcionRepository;
    private TurnoInstructorService service;
    private SalonLock salonLock;

    private Salon salon;

    @BeforeEach
    void preparar() {
        turnoRepository = mock(TurnoInstructorRepository.class);
        TurnoInstructorAsignacionRepository asignacionRepository = mock(TurnoInstructorAsignacionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        SalonRepository salonRepository = mock(SalonRepository.class);
        horarioOperacionRepository = mock(HorarioOperacionRepository.class);
        TipoActividadRepository tipoActividadRepository = mock(TipoActividadRepository.class);
        salonHorarioExcepcionRepository = mock(SalonHorarioExcepcionRepository.class);
        AutorizadorSalon autorizadorSalon = mock(AutorizadorSalon.class);
        salonLock = mock(SalonLock.class);

        salon = new Salon();
        salon.setId(SALON_ID);
        salon.setNombre("Juriquilla");

        TipoActividad reformer = new TipoActividad();
        reformer.setId(REFORMER_ID);
        reformer.setNombre("Reformer");

        Usuario ariadna = new Usuario();
        ariadna.setId(ARIADNA_ID);
        ariadna.setNombre("Ariadna");
        ariadna.setCorreo("ariadna@example.com");
        ariadna.getEspecialidades().add(reformer);
        Rol rol = new Rol();
        rol.setNombre(Rol.INSTRUCTOR);
        ariadna.getRoles().add(new UsuarioRol(ariadna, rol, salon));

        when(salonRepository.findById(SALON_ID)).thenReturn(Optional.of(salon));
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(ariadna));
        when(tipoActividadRepository.findAllById(any())).thenReturn(List.of(reformer));
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(any(), any()))
                .thenReturn(Optional.empty());
        when(turnoRepository.buscarRecurrentesPorSalonYDia(any(), anyShort())).thenReturn(List.of());
        when(turnoRepository.buscarExcepcionesPorSalonYFecha(any(), any())).thenReturn(List.of());
        when(turnoRepository.save(any(TurnoInstructor.class))).thenAnswer(i -> {
            TurnoInstructor turno = i.getArgument(0);
            if (turno.getId() == null) {
                turno.setId(UUID.randomUUID());
            }
            return turno;
        });
        when(asignacionRepository.saveAll(anyList()))
                .thenAnswer(i -> i.<List<TurnoInstructorAsignacion>>getArgument(0));

        service = new TurnoInstructorService(
                turnoRepository,
                asignacionRepository,
                usuarioRepository,
                salonRepository,
                horarioOperacionRepository,
                tipoActividadRepository,
                new HorarioEfectivoSalon(
                        salonHorarioExcepcionRepository, new HorarioOperacionResolver(horarioOperacionRepository)),
                autorizadorSalon,
                salonLock,
                RELOJ);
    }

    // --- A: compatibilidad con los datos legados -------------------------------------------

    @Test
    void recurrenteConLaUnicaFilaLegadaNullNullSeAceptaComoAntes() {
        hayVersiones(horario(8, 20, null, null));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(9, 12));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void recurrenteConsultaLasVersionesDesdeHoyHaciaElFuturo() {
        hayVersiones(horario(8, 20, null, null));

        service.crear(ACTOR_ID, recurrente(9, 12));

        verify(horarioOperacionRepository).findVersionesQueIntersectan(SALON_ID, (short) 1, HOY, null);
    }

    // --- B: una version futura incompatible manda -------------------------------------------

    /**
     * 08-20 hasta agosto y 09-20 desde septiembre: un recurrente 08-09 cabe HOY pero dejaria de
     * caber en septiembre. Con {@code anyMatch} se aceptaria; con cobertura + {@code allMatch} no.
     */
    @Test
    void recurrenteSeRechazaSiUnaVersionFuturaYaNoLoAdmite() {
        hayVersiones(
                horario(8, 20, null, AGO_31),
                horario(9, 20, SEP_1, null));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(8, 9)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(FUERA_DE_HORARIO);
    }

    // --- C: gap futuro ----------------------------------------------------------------------

    /**
     * Ambas versiones admiten 09-12, pero entre el 1 y el 14 de septiembre el salon no tiene
     * horario para ese dia: la regla abierta quedaria sin respaldo en ese tramo.
     */
    @Test
    void recurrenteSeRechazaSiHayUnGapEntreVersionesAunqueTodasAdmitanElHorario() {
        hayVersiones(
                horario(8, 20, null, AGO_31),
                horario(8, 20, SEP_15, null));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(9, 12)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(FUERA_DE_HORARIO);
    }

    // --- D: la cobertura no llega a +infinito ------------------------------------------------

    @Test
    void recurrenteSeRechazaSiLaUltimaVersionTerminaEnFechaFinita() {
        hayVersiones(horario(8, 20, null, AGO_31));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(9, 12)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(FUERA_DE_HORARIO);
    }

    @Test
    void recurrenteSeRechazaSinNingunaVersionAplicable() {
        hayVersiones();

        assertThatThrownBy(() -> service.crear(ACTOR_ID, recurrente(9, 12)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(FUERA_DE_HORARIO);
    }

    // --- E: versiones contiguas compatibles ---------------------------------------------------

    @Test
    void recurrenteSeAceptaConDosVersionesContiguasQueLoAdmitenAmbas() {
        hayVersiones(
                horario(8, 20, null, AGO_31),
                horario(8, 20, SEP_1, null));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(9, 12));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void recurrenteSeAceptaSiLaVersionFuturaAmpliaElHorario() {
        hayVersiones(
                horario(8, 20, null, AGO_31),
                horario(7, 22, SEP_1, null));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, recurrente(9, 12));

        assertThat(resultado.horaFin()).isEqualTo(LocalTime.of(12, 0));
    }

    // --- F-I: turnos con fecha, via HorarioEfectivoSalon ---------------------------------------

    @Test
    void excepcionEnDiaCerradoSeRechaza() {
        SalonHorarioExcepcion cierre = new SalonHorarioExcepcion();
        cierre.setSalon(salon);
        cierre.setFecha(LUNES);
        cierre.setCerrado(true);
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(cierre));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(10, 12)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("cerrado");
    }

    @Test
    void excepcionConHorarioEspecialCompatibleSeAcepta() {
        hayExcepcionAbierta(7, 22);

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, excepcion(7, 9));

        assertThat(resultado.fecha()).isEqualTo(LUNES);
    }

    @Test
    void excepcionConHorarioEspecialIncompatibleSeRechaza() {
        hayExcepcionAbierta(8, 14);

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(13, 15)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario especial");
    }

    @Test
    void excepcionSinExcepcionRegistradaUsaLaVersionSemanalVigenteEnEsaFecha() {
        hayVigenteEn(LUNES, horario(8, 20, null, null));

        TurnoInstructorResponse resultado = service.crear(ACTOR_ID, excepcion(9, 12));

        assertThat(resultado.horaInicio()).isEqualTo(LocalTime.of(9, 0));
        verify(horarioOperacionRepository).findVigente(SALON_ID, (short) 1, LUNES);
    }

    /**
     * La ruta por fecha usa la version VIGENTE ese dia, no "cualquier fila del salon": una version
     * que ya expiro no puede seguir habilitando turnos.
     */
    @Test
    void excepcionSeRechazaSiLaVersionVigenteEsaFechaNoLoAdmite() {
        hayVigenteEn(LUNES, horario(10, 14, null, null));

        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(9, 12)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(FUERA_DE_HORARIO);
    }

    @Test
    void excepcionEnDiaSinHorarioNiExcepcionSeRechaza() {
        assertThatThrownBy(() -> service.crear(ACTOR_ID, excepcion(9, 12)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining(FUERA_DE_HORARIO);
    }

    // --- helpers ------------------------------------------------------------------------------

    private void hayVersiones(HorarioOperacion... versiones) {
        when(horarioOperacionRepository.findVersionesQueIntersectan(eq(SALON_ID), eq((short) 1), any(), any()))
                .thenReturn(List.of(versiones));
    }

    private void hayVigenteEn(LocalDate fecha, HorarioOperacion horario) {
        when(horarioOperacionRepository.findVigente(SALON_ID, (short) 1, fecha)).thenReturn(List.of(horario));
    }

    private void hayExcepcionAbierta(int apertura, int cierre) {
        SalonHorarioExcepcion excepcion = new SalonHorarioExcepcion();
        excepcion.setSalon(salon);
        excepcion.setFecha(LUNES);
        excepcion.setCerrado(false);
        excepcion.setHoraApertura(LocalTime.of(apertura, 0));
        excepcion.setHoraCierre(LocalTime.of(cierre, 0));
        when(salonHorarioExcepcionRepository.findBySalonIdAndFechaAndActivoTrue(SALON_ID, LUNES))
                .thenReturn(Optional.of(excepcion));
    }

    private HorarioOperacion horario(int apertura, int cierre, LocalDate vigenteDesde, LocalDate vigenteHasta) {
        HorarioOperacion horario = new HorarioOperacion();
        horario.setSalon(salon);
        horario.setDiaSemana((short) 1);
        horario.setHoraApertura(LocalTime.of(apertura, 0));
        horario.setHoraCierre(LocalTime.of(cierre, 0));
        horario.setVigenteDesde(vigenteDesde);
        horario.setVigenteHasta(vigenteHasta);
        return horario;
    }

    private TurnoInstructorRequest recurrente(int inicio, int fin) {
        return new TurnoInstructorRequest(
                List.of(new AsignacionInstructorRequest(ARIADNA_ID, List.of(REFORMER_ID), null, null)),
                SALON_ID, TurnoInstructor.Tipo.RECURRENTE, (short) 1, null,
                LocalTime.of(inicio, 0), LocalTime.of(fin, 0));
    }

    private TurnoInstructorRequest excepcion(int inicio, int fin) {
        return new TurnoInstructorRequest(
                List.of(new AsignacionInstructorRequest(ARIADNA_ID, List.of(REFORMER_ID), null, null)),
                SALON_ID, TurnoInstructor.Tipo.EXCEPCION, null, LUNES,
                LocalTime.of(inicio, 0), LocalTime.of(fin, 0));
    }
}
