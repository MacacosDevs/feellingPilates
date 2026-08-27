package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.ProgramacionInvarianteException;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.HorarioEfectivoSalon;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgramacionValidadorTest {

    private static final UUID SALON = UUID.randomUUID();
    private static final UUID SALON_ORIGEN = UUID.randomUUID();
    private static final UUID INSTRUCTOR = UUID.randomUUID();
    private static final UUID ACTIVIDAD = UUID.randomUUID();
    private static final LocalDate FECHA = LocalDate.of(2028, 1, 3);

    private SalonRepository salones;
    private UsuarioRepository usuarios;
    private TipoActividadRepository actividades;
    private HorarioEfectivoSalon horarios;
    private ProgramacionDiagnostico diagnostico;
    private ProgramacionValidador validador;
    private Salon salon;
    private Usuario instructor;
    private TipoActividad actividad;

    @BeforeEach
    void preparar() {
        salones = mock(SalonRepository.class);
        usuarios = mock(UsuarioRepository.class);
        actividades = mock(TipoActividadRepository.class);
        horarios = mock(HorarioEfectivoSalon.class);
        diagnostico = mock(ProgramacionDiagnostico.class);
        salon = new Salon();
        salon.setId(SALON);
        salon.setActivo(true);
        actividad = new TipoActividad();
        actividad.setId(ACTIVIDAD);
        actividad.setActivo(true);
        salon.setTiposActividad(Set.of(actividad));
        instructor = new Usuario();
        instructor.setId(INSTRUCTOR);
        instructor.setEstatus(Usuario.EstatusUsuario.activo);
        instructor.setEspecialidades(Set.of(actividad));
        Rol rol = new Rol();
        rol.setNombre(Rol.INSTRUCTOR);
        instructor.setRoles(Set.of(new UsuarioRol(instructor, rol)));
        when(salones.findById(SALON)).thenReturn(Optional.of(salon));
        when(usuarios.findById(INSTRUCTOR)).thenReturn(Optional.of(instructor));
        when(actividades.findById(ACTIVIDAD)).thenReturn(Optional.of(actividad));
        when(horarios.resolver(SALON, FECHA)).thenReturn(
                HorarioEfectivo.abiertoPorHorarioSemanal(
                        LocalTime.of(8, 0), LocalTime.of(20, 0)));
        validador = new ProgramacionValidador(
                horarios, salones, usuarios, actividades, diagnostico);
    }

    @Test
    void resultadoValidoUsaHorarioDelSalonFinal() {
        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .hasSize(1);
        verify(horarios).resolver(SALON, FECHA);
    }

    @Test
    void instructorSuspendidoSeOmiteYDiagnosticaFailClosed() {
        instructor.setEstatus(Usuario.EstatusUsuario.suspendido);

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> "INSTRUCTOR_INEXISTENTE_O_INACTIVO".equals(o.causa())
                        && INSTRUCTOR.equals(o.instructorId())
                        && SALON.equals(o.salonId())
                        && ACTIVIDAD.equals(o.actividadId())));
    }

    @Test
    void salonInactivoSeOmiteFailClosed() {
        salon.setActivo(false);

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> "SALON_INEXISTENTE_O_INACTIVO".equals(o.causa())));
    }

    @Test
    void salonCerradoSeOmiteFailClosed() {
        when(horarios.resolver(SALON, FECHA)).thenReturn(HorarioEfectivo.cerrado());

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> ProgramacionErrores.SALON_NO_OPERATIVO_EN_FECHA.equals(o.causa())));
    }

    @Test
    void fueraDeHorarioSeOmiteFailClosed() {
        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(7, 8))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> ProgramacionErrores.AJUSTE_FUERA_DE_HORARIO_EFECTIVO.equals(o.causa())));
    }

    @Test
    void rolInstructorAusenteSeOmiteFailClosed() {
        instructor.setRoles(Set.of());

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> "ROL_INSTRUCTOR_AUSENTE".equals(o.causa())));
    }

    @Test
    void actividadInactivaSeOmiteFailClosed() {
        actividad.setActivo(false);

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> "ACTIVIDAD_INEXISTENTE_O_INACTIVA".equals(o.causa())));
    }

    @Test
    void especialidadAusenteSeOmiteFailClosed() {
        instructor.setEspecialidades(Set.of());

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> "ESPECIALIDAD_AUSENTE".equals(o.causa())));
    }

    @Test
    void ofertaDelSalonAusenteSeOmiteFailClosed() {
        salon.setTiposActividad(Set.of());

        assertThat(validador.filtrarFailClosedYValidar(List.of(efectiva(10, 11))))
                .isEmpty();
        verify(diagnostico).registrarOmision(org.mockito.ArgumentMatchers.argThat(
                o -> "ACTIVIDAD_NO_OFRECIDA_POR_SALON".equals(o.causa())));
    }

    @Test
    void writerFailClosedRechazaEnLugarDeOmitirLaMutacion() {
        instructor.setEstatus(Usuario.EstatusUsuario.suspendido);
        OcurrenciaEfectiva mutada = efectiva(10, 11);

        assertThatThrownBy(() -> validador.validarMutacion(
                List.of(mutada), mutada.referencia()))
                .isInstanceOf(com.feelingpilates.exception.ValidacionException.class)
                .hasMessageContaining("INSTRUCTOR_INEXISTENTE_O_INACTIVO");
    }

    @Test
    void adyacenciaEsValidaPeroSolapeGlobalFallaSinResultadoParcial() {
        assertThat(validador.filtrarFailClosedYValidar(
                List.of(efectiva(9, 10), efectiva(10, 11)))).hasSize(2);

        assertThatThrownBy(() -> validador.filtrarFailClosedYValidar(
                List.of(efectiva(9, 11), efectiva(10, 12))))
                .isInstanceOf(ProgramacionInvarianteException.class)
                .hasMessageContaining(ProgramacionErrores.INSTRUCTOR_CON_PROGRAMACION_TRASLAPADA);
    }

    @Test
    void duplicadoExactoEsInvarianteNoDeduplicacion() {
        OcurrenciaEfectiva a = efectiva(10, 11);
        OcurrenciaEfectiva b = efectiva(10, 11);

        assertThatThrownBy(() -> validador.filtrarFailClosedYValidar(List.of(a, b)))
                .isInstanceOf(ProgramacionInvarianteException.class)
                .hasMessageContaining(ProgramacionErrores.OCURRENCIA_EFECTIVA_DUPLICADA);
    }

    @Test
    void reemplazoUsaSoloHorarioFinalYNoConsultaOrigenCerrado() {
        OcurrenciaEfectiva reemplazo = new OcurrenciaEfectiva(
                FECHA, SALON, INSTRUCTOR, ACTIVIDAD,
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                OcurrenciaEfectiva.Origen.REEMPLAZO,
                new ReferenciaOcurrencia(
                        ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION, UUID.randomUUID(), FECHA));
        when(horarios.resolver(SALON_ORIGEN, FECHA)).thenReturn(HorarioEfectivo.cerrado());

        assertThat(validador.filtrarFailClosedYValidar(List.of(reemplazo)))
                .containsExactly(reemplazo);
        verify(horarios, org.mockito.Mockito.never()).resolver(SALON_ORIGEN, FECHA);
        verify(horarios).resolver(SALON, FECHA);
    }

    private OcurrenciaEfectiva efectiva(int inicio, int fin) {
        return new OcurrenciaEfectiva(
                FECHA, SALON, INSTRUCTOR, ACTIVIDAD, LocalTime.of(inicio, 0),
                LocalTime.of(fin, 0), OcurrenciaEfectiva.Origen.ADICION,
                new ReferenciaOcurrencia(
                        ReferenciaOcurrencia.Tipo.AJUSTE, UUID.randomUUID(), FECHA));
    }
}
