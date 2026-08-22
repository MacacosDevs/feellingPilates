package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.entidad.Asignacion;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.AsignacionRepository;
import com.feelingpilates.programacion.repositorio.BloqueProgramacionRepository;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BloqueProgramacionServiceTest {

    private static final UUID SALON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTRO_SALON_ID = UUID.fromString("12121212-1212-1212-1212-121212121212");
    private static final UUID BLOQUE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ARIADNA_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ALBERTO_ID = UUID.fromString("34343434-3434-3434-3434-343434343434");
    private static final UUID REFORMER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID MAT_ID = UUID.fromString("45454545-4545-4545-4545-454545454545");
    private static final UUID SERIE_BLOQUE = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID SERIE_ASIGNACION = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final LocalDate ENERO = LocalDate.of(2027, 1, 1);
    private static final LocalDate DICIEMBRE = LocalDate.of(2027, 12, 31);

    private BloqueProgramacionRepository bloqueRepository;
    private AsignacionRepository asignacionRepository;
    private SalonRepository salonRepository;
    private HorarioOperacionRepository horarioOperacionRepository;
    private UsuarioRepository usuarioRepository;
    private TipoActividadRepository tipoActividadRepository;
    private BloqueProgramacionService service;

    private Salon salon;
    private TipoActividad reformer;
    private TipoActividad mat;
    private Usuario ariadna;
    private Usuario alberto;
    private BloqueProgramacion bloque;

    @BeforeEach
    void preparar() {
        bloqueRepository = mock(BloqueProgramacionRepository.class);
        asignacionRepository = mock(AsignacionRepository.class);
        salonRepository = mock(SalonRepository.class);
        horarioOperacionRepository = mock(HorarioOperacionRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        tipoActividadRepository = mock(TipoActividadRepository.class);

        reformer = actividad(REFORMER_ID, "Reformer");
        mat = actividad(MAT_ID, "Mat");
        salon = salon(SALON_ID, reformer, mat);
        ariadna = instructor(ARIADNA_ID, "Ariadna", reformer, mat);
        alberto = instructor(ALBERTO_ID, "Alberto", reformer, mat);
        bloque = bloque(BLOQUE_ID, SALON_ID, (short) 1, 8, 14, ENERO, DICIEMBRE);

        when(salonRepository.findById(SALON_ID)).thenReturn(Optional.of(salon));
        when(usuarioRepository.findById(ARIADNA_ID)).thenReturn(Optional.of(ariadna));
        when(usuarioRepository.findById(ALBERTO_ID)).thenReturn(Optional.of(alberto));
        when(tipoActividadRepository.findById(REFORMER_ID)).thenReturn(Optional.of(reformer));
        when(tipoActividadRepository.findById(MAT_ID)).thenReturn(Optional.of(mat));
        when(bloqueRepository.findById(BLOQUE_ID)).thenReturn(Optional.of(bloque));
        when(horarioOperacionRepository.findBySalonIdOrderByDiaSemana(SALON_ID))
                .thenReturn(List.of(horario((short) 1, 8, 20)));
        when(bloqueRepository.buscarTraslapesActivos(
                any(), anyShort(), any(), any(), any(), nullable(LocalDate.class)))
                .thenReturn(List.of());
        when(asignacionRepository.findByBloqueIdAndActivoTrueOrderByHoraInicio(any()))
                .thenReturn(List.of());
        when(asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                any(), anyShort(), any(), any(), any(), nullable(LocalDate.class)))
                .thenReturn(List.of());

        AtomicInteger secuencia = new AtomicInteger();
        when(bloqueRepository.save(any(BloqueProgramacion.class))).thenAnswer(invocacion -> {
            BloqueProgramacion guardado = invocacion.getArgument(0);
            guardado.setId(new UUID(0, 100 + secuencia.incrementAndGet()));
            return guardado;
        });
        when(asignacionRepository.save(any(Asignacion.class))).thenAnswer(invocacion -> {
            Asignacion guardada = invocacion.getArgument(0);
            guardada.setId(new UUID(0, 200 + secuencia.incrementAndGet()));
            return guardada;
        });

        service = new BloqueProgramacionService(
                bloqueRepository,
                asignacionRepository,
                salonRepository,
                horarioOperacionRepository,
                usuarioRepository,
                tipoActividadRepository);
    }

    @Test
    void creaBloqueValidoDentroDelHorario() {
        BloqueProgramacion resultado = service.crearBloque(crearBloque(9, 12, ENERO, DICIEMBRE));

        assertThat(resultado.getSalonId()).isEqualTo(SALON_ID);
        assertThat(resultado.getDiaSemana()).isEqualTo((short) 1);
        assertThat(resultado.getSerieId()).isEqualTo(SERIE_BLOQUE);
        assertThat(resultado.isActivo()).isTrue();
    }

    @Test
    void permiteInicioDelBloqueIgualALaApertura() {
        BloqueProgramacion resultado = service.crearBloque(crearBloque(8, 10, ENERO, DICIEMBRE));

        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void permiteFinDelBloqueIgualAlCierre() {
        BloqueProgramacion resultado = service.crearBloque(crearBloque(18, 20, ENERO, DICIEMBRE));

        assertThat(resultado.getHoraFin()).isEqualTo(LocalTime.of(20, 0));
    }

    @Test
    void rechazaBloqueFueraDelHorario() {
        assertThatThrownBy(() -> service.crearBloque(crearBloque(7, 10, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario de operación");
    }

    @Test
    void permiteBloquesContiguos() {
        BloqueProgramacion resultado = service.crearBloque(crearBloque(12, 14, ENERO, DICIEMBRE));

        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.NOON);
        verify(bloqueRepository).buscarTraslapesActivos(
                SALON_ID, (short) 1, LocalTime.NOON, LocalTime.of(14, 0), ENERO, DICIEMBRE);
    }

    @Test
    void rechazaTraslapePositivoEntreBloques() {
        when(bloqueRepository.buscarTraslapesActivos(
                SALON_ID, (short) 1, LocalTime.NOON, LocalTime.of(14, 0), ENERO, DICIEMBRE))
                .thenReturn(List.of(bloque(null, SALON_ID, (short) 1, 8, 13, ENERO, DICIEMBRE)));

        assertThatThrownBy(() -> service.crearBloque(crearBloque(12, 14, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("traslapa");
    }

    @Test
    void rechazaBloqueConFinIgualAlInicio() {
        assertThatThrownBy(() -> service.crearBloque(crearBloque(10, 10, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void rechazaBloqueConFinAnteriorAlInicio() {
        assertThatThrownBy(() -> service.crearBloque(crearBloque(11, 10, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void rechazaVigenciaDeBloqueInvalida() {
        assertThatThrownBy(() -> service.crearBloque(crearBloque(10, 12, DICIEMBRE, ENERO)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("vigencia final");
    }

    @Test
    void permiteMismoRangoDeBloqueConVigenciasDisjuntas() {
        BloqueProgramacion resultado = service.crearBloque(crearBloque(
                8, 12, LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 31)));

        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void rechazaMismoRangoDeBloqueConVigenciasIntersectadas() {
        when(bloqueRepository.buscarTraslapesActivos(
                SALON_ID, (short) 1, LocalTime.of(8, 0), LocalTime.NOON, ENERO, DICIEMBRE))
                .thenReturn(List.of(bloque));

        assertThatThrownBy(() -> service.crearBloque(crearBloque(8, 12, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("vigencia");
    }

    @Test
    void rechazaDiaDeSemanaFueraDelDominio() {
        BloqueProgramacionService.CrearBloque comando = new BloqueProgramacionService.CrearBloque(
                SERIE_BLOQUE, SALON_ID, (short) 7, LocalTime.of(8, 0), LocalTime.NOON, ENERO, DICIEMBRE);

        assertThatThrownBy(() -> service.crearBloque(comando))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("0 y 6");
    }

    @Test
    void creaAsignacionValidaConActividadUnica() {
        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE));

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getBloqueId()).isEqualTo(BLOQUE_ID);
        assertThat(resultado.getInstructorId()).isEqualTo(ARIADNA_ID);
        assertThat(resultado.getTipoActividadId()).isEqualTo(REFORMER_ID);
        assertThat(resultado.isActivo()).isTrue();
    }

    @Test
    void rechazaActividadInactiva() {
        reformer.setActivo(false);

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no está activo");
    }

    @Test
    void rechazaActividadNoOfrecidaPorElSalon() {
        salon.setTiposActividad(Set.of(mat));

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no ofrece");
    }

    @Test
    void rechazaInstructorSinEspecialidad() {
        ariadna.setEspecialidades(Set.of(mat));

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no tiene la especialidad");
    }

    @Test
    void rechazaRangoDeAsignacionFueraDelBloque() {
        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 7, 9, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("horario del bloque");
    }

    @Test
    void rechazaVigenciaDeAsignacionFueraDelBloque() {
        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO.minusDays(1), DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("vigencia de la asignación");
    }

    @Test
    void permiteDosSegmentosDisjuntosDelMismoInstructorYActividad() {
        when(asignacionRepository.findByBloqueIdAndActivoTrueOrderByHoraInicio(BLOQUE_ID))
                .thenReturn(List.of(asignacion(ARIADNA_ID, REFORMER_ID, 8, 10, ENERO, DICIEMBRE)));

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 12, 14, ENERO, DICIEMBRE));

        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.NOON);
        assertThat(resultado.getTipoActividadId()).isEqualTo(REFORMER_ID);
    }

    @Test
    void permiteSegmentosContiguosDelMismoInstructorYActividad() {
        when(asignacionRepository.findByBloqueIdAndActivoTrueOrderByHoraInicio(BLOQUE_ID))
                .thenReturn(List.of(asignacion(ARIADNA_ID, REFORMER_ID, 8, 10, ENERO, DICIEMBRE)));

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 10, 12, ENERO, DICIEMBRE));

        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void rechazaTraslapeDelMismoInstructorDentroDelBloque() {
        when(asignacionRepository.findByBloqueIdAndActivoTrueOrderByHoraInicio(BLOQUE_ID))
                .thenReturn(List.of(asignacion(ARIADNA_ID, REFORMER_ID, 8, 11, ENERO, DICIEMBRE)));

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, MAT_ID, 10, 12, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("traslapada en el bloque");
    }

    @Test
    void permiteDosInstructoresEnLaMismaHora() {
        when(asignacionRepository.findByBloqueIdAndActivoTrueOrderByHoraInicio(BLOQUE_ID))
                .thenReturn(List.of(asignacion(ALBERTO_ID, MAT_ID, 9, 11, ENERO, DICIEMBRE)));

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE));

        assertThat(resultado.getInstructorId()).isEqualTo(ARIADNA_ID);
    }

    @Test
    void permiteDosInstructoresConLaMismaActividadEnLaMismaHora() {
        when(asignacionRepository.findByBloqueIdAndActivoTrueOrderByHoraInicio(BLOQUE_ID))
                .thenReturn(List.of(asignacion(ALBERTO_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE)));

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE));

        assertThat(resultado.getTipoActividadId()).isEqualTo(REFORMER_ID);
    }

    @Test
    void rechazaConflictoGlobalDelInstructorEntreSalones() {
        when(asignacionRepository.buscarConflictosRecurrentesDelInstructor(
                ARIADNA_ID, (short) 1, LocalTime.of(10, 0), LocalTime.NOON, ENERO, DICIEMBRE))
                .thenReturn(List.of(asignacion(ARIADNA_ID, REFORMER_ID, 11, 13, ENERO, DICIEMBRE)));

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 10, 12, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("recurrente traslapada");
    }

    @Test
    void permiteMismoInstructorYHorarioGlobalConVigenciasDisjuntas() {
        LocalDate febrero = LocalDate.of(2028, 2, 1);
        LocalDate finFebrero = LocalDate.of(2028, 2, 29);
        bloque.setVigenteHasta(null);

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 10, 12, febrero, finFebrero));

        assertThat(resultado.getVigenteDesde()).isEqualTo(febrero);
        verify(asignacionRepository).buscarConflictosRecurrentesDelInstructor(
                ARIADNA_ID, (short) 1, LocalTime.of(10, 0), LocalTime.NOON, febrero, finFebrero);
    }

    @Test
    void permiteMismoInstructorEnSalonesDistintosSiLosDiasSonDistintos() {
        bloque.setDiaSemana((short) 2);

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 10, 12, ENERO, DICIEMBRE));

        assertThat(resultado.getInstructorId()).isEqualTo(ARIADNA_ID);
        verify(asignacionRepository).buscarConflictosRecurrentesDelInstructor(
                ARIADNA_ID, (short) 2, LocalTime.of(10, 0), LocalTime.NOON, ENERO, DICIEMBRE);
    }

    @Test
    void permiteInstructoresDistintosEnMismoHorarioGlobal() {
        when(usuarioRepository.findById(ALBERTO_ID)).thenReturn(Optional.of(alberto));

        Asignacion resultado = service.crearAsignacion(crearAsignacion(
                ALBERTO_ID, REFORMER_ID, 10, 12, ENERO, DICIEMBRE));

        assertThat(resultado.getInstructorId()).isEqualTo(ALBERTO_ID);
        verify(asignacionRepository).buscarConflictosRecurrentesDelInstructor(
                ALBERTO_ID, (short) 1, LocalTime.of(10, 0), LocalTime.NOON, ENERO, DICIEMBRE);
    }

    @Test
    void dosVersionesPuedenCompartirSerieIdConIdsDistintos() {
        BloqueProgramacion enero = service.crearBloque(crearBloque(
                8, 12, LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 31)));
        BloqueProgramacion febrero = service.crearBloque(crearBloque(
                9, 13, LocalDate.of(2028, 2, 1), null));

        assertThat(enero.getId()).isNotEqualTo(febrero.getId());
        assertThat(enero.getSerieId()).isEqualTo(SERIE_BLOQUE).isEqualTo(febrero.getSerieId());
    }

    @Test
    void asignacionModelaExactamenteUnaActividadSinColeccion() {
        List<Field> camposActividad = Arrays.stream(Asignacion.class.getDeclaredFields())
                .filter(campo -> campo.getName().toLowerCase().contains("actividad"))
                .toList();

        assertThat(camposActividad).singleElement().satisfies(campo -> {
            assertThat(campo.getName()).isEqualTo("tipoActividadId");
            assertThat(campo.getType()).isEqualTo(UUID.class);
        });
    }

    @Test
    void rechazaAsignacionSobreBloqueInactivo() {
        bloque.setActivo(false);

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no está activo");
    }

    @Test
    void rechazaInstructorSuspendido() {
        ariadna.setEstatus(Usuario.EstatusUsuario.suspendido);

        assertThatThrownBy(() -> service.crearAsignacion(crearAsignacion(
                ARIADNA_ID, REFORMER_ID, 9, 11, ENERO, DICIEMBRE)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("no está habilitado");
    }

    @Test
    void vigenciaAbiertaSeInterpretaComoInfinita() {
        assertThat(BloqueProgramacionService.vigenciasSeIntersectan(
                ENERO, null, LocalDate.of(2035, 1, 1), LocalDate.of(2035, 1, 31))).isTrue();
        assertThat(BloqueProgramacionService.vigenciasSeIntersectan(
                ENERO, ENERO.plusDays(5), ENERO.plusDays(6), null)).isFalse();
    }

    @Test
    void intervalosContiguosNoSeTraslapan() {
        assertThat(BloqueProgramacionService.rangosSeTraslapan(
                LocalTime.of(8, 0), LocalTime.NOON, LocalTime.NOON, LocalTime.of(14, 0))).isFalse();
        assertThat(BloqueProgramacionService.rangosSeTraslapan(
                LocalTime.of(8, 0), LocalTime.of(12, 1), LocalTime.NOON, LocalTime.of(14, 0))).isTrue();
    }

    private BloqueProgramacionService.CrearBloque crearBloque(
            int inicio, int fin, LocalDate vigenteDesde, LocalDate vigenteHasta) {
        return new BloqueProgramacionService.CrearBloque(
                SERIE_BLOQUE,
                SALON_ID,
                (short) 1,
                LocalTime.of(inicio, 0),
                LocalTime.of(fin, 0),
                vigenteDesde,
                vigenteHasta);
    }

    private BloqueProgramacionService.CrearAsignacion crearAsignacion(
            UUID instructorId,
            UUID actividadId,
            int inicio,
            int fin,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {
        return new BloqueProgramacionService.CrearAsignacion(
                SERIE_ASIGNACION,
                BLOQUE_ID,
                instructorId,
                actividadId,
                LocalTime.of(inicio, 0),
                LocalTime.of(fin, 0),
                vigenteDesde,
                vigenteHasta);
    }

    private Salon salon(UUID id, TipoActividad... actividades) {
        Salon resultado = new Salon();
        resultado.setId(id);
        resultado.setNombre(id.equals(OTRO_SALON_ID) ? "Cimatario" : "Juriquilla");
        resultado.setActivo(true);
        resultado.setTiposActividad(Set.of(actividades));
        return resultado;
    }

    private TipoActividad actividad(UUID id, String nombre) {
        TipoActividad resultado = new TipoActividad();
        resultado.setId(id);
        resultado.setNombre(nombre);
        resultado.setActivo(true);
        return resultado;
    }

    private Usuario instructor(UUID id, String nombre, TipoActividad... especialidades) {
        Usuario resultado = new Usuario();
        resultado.setId(id);
        resultado.setNombre(nombre);
        resultado.setCorreo(nombre.toLowerCase() + "@example.com");
        resultado.setEstatus(Usuario.EstatusUsuario.activo);
        resultado.setEspecialidades(Set.of(especialidades));

        Rol rol = new Rol();
        rol.setNombre(Rol.INSTRUCTOR);
        resultado.setRoles(Set.of(new UsuarioRol(resultado, rol)));
        return resultado;
    }

    private HorarioOperacion horario(short dia, int apertura, int cierre) {
        HorarioOperacion resultado = new HorarioOperacion();
        resultado.setDiaSemana(dia);
        resultado.setHoraApertura(LocalTime.of(apertura, 0));
        resultado.setHoraCierre(LocalTime.of(cierre, 0));
        return resultado;
    }

    private BloqueProgramacion bloque(
            UUID id,
            UUID salonId,
            short dia,
            int inicio,
            int fin,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {
        BloqueProgramacion resultado = new BloqueProgramacion();
        resultado.setId(id);
        resultado.setSerieId(SERIE_BLOQUE);
        resultado.setSalonId(salonId);
        resultado.setDiaSemana(dia);
        resultado.setHoraInicio(LocalTime.of(inicio, 0));
        resultado.setHoraFin(LocalTime.of(fin, 0));
        resultado.setVigenteDesde(vigenteDesde);
        resultado.setVigenteHasta(vigenteHasta);
        resultado.setActivo(true);
        return resultado;
    }

    private Asignacion asignacion(
            UUID instructorId,
            UUID actividadId,
            int inicio,
            int fin,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta) {
        Asignacion resultado = new Asignacion();
        resultado.setId(UUID.randomUUID());
        resultado.setSerieId(SERIE_ASIGNACION);
        resultado.setBloqueId(BLOQUE_ID);
        resultado.setInstructorId(instructorId);
        resultado.setTipoActividadId(actividadId);
        resultado.setHoraInicio(LocalTime.of(inicio, 0));
        resultado.setHoraFin(LocalTime.of(fin, 0));
        resultado.setVigenteDesde(vigenteDesde);
        resultado.setVigenteHasta(vigenteHasta);
        resultado.setActivo(true);
        return resultado;
    }
}
