package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.calendario.dto.*;
import com.feelingpilates.calendario.entidad.*;
import com.feelingpilates.calendario.repositorio.*;
import com.feelingpilates.calendario.servicio.ReservaService;
import com.feelingpilates.exception.*;
import com.feelingpilates.seguridad.AutorizadorSalon;
import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.entidad.*;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import com.feelingpilates.ubicaciones.servicio.*;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import java.time.*;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ReservasPN14Test {
    static final LocalDate DIA=LocalDate.of(2026,8,24);
    ReservaRepository reservas;TurnoInstructorRepository turnos;UsuarioRepository usuarios;TipoActividadRepository actividades;
    AutorizadorSalon autorizador;SalonLock lock;HorarioEfectivoSalon horario;ReservaService service;
    Salon sede;Usuario instructor,cliente;TipoActividad actividad;TurnoInstructor turno;
    @BeforeEach void setup(){
        reservas=mock(ReservaRepository.class);turnos=mock(TurnoInstructorRepository.class);usuarios=mock(UsuarioRepository.class);
        actividades=mock(TipoActividadRepository.class);autorizador=mock(AutorizadorSalon.class);lock=mock(SalonLock.class);horario=mock(HorarioEfectivoSalon.class);
        service=new ReservaService(reservas,turnos,usuarios,actividades,autorizador,lock,horario);
        sede=salon();instructor=usuario(ACTOR,"Instructor dummy");cliente=usuario(CLIENTE,"Cliente dummy");actividad=actividad();instructor.getEspecialidades().add(actividad);
        turno=new TurnoInstructor();turno.setTipo(TurnoInstructor.Tipo.RECURRENTE);turno.setHoraInicio(LocalTime.of(8,0));turno.setHoraFin(LocalTime.of(12,0));
        when(lock.adquirir(SALON)).thenReturn(sede);when(usuarios.findById(ACTOR)).thenReturn(Optional.of(instructor));when(usuarios.findById(CLIENTE)).thenReturn(Optional.of(cliente));
        when(actividades.findById(actividad.getId())).thenReturn(Optional.of(actividad));
        when(horario.resolver(SALON,DIA)).thenReturn(HorarioEfectivo.abiertoPorHorarioSemanal(LocalTime.of(8,0),LocalTime.of(12,0)));
        when(turnos.buscarRecurrentesPorInstructorSalonYDia(ACTOR,SALON,(short)1)).thenReturn(List.of(turno));
        when(reservas.save(any(Reserva.class))).thenAnswer(i->{Reserva r=i.getArgument(0);r.setId(COMPRA);return r;});
    }
    ReservaRequest request(){return new ReservaRequest(SALON,ACTOR,CLIENTE,actividad.getId(),DIA,LocalTime.of(9,0));}
    @Test void M11_crearConfirmadaDuracionCompletaAutorizacionLockAntesLecturasYSave() {
        var r=service.crear(ACTOR,request());
        assertThat(r).isEqualTo(new ReservaResponse(COMPRA,SALON,"Sede dummy",ACTOR,"Instructor dummy",CLIENTE,"Cliente dummy",
            actividad.getId(),"Reformer dummy",DIA,LocalTime.of(9,0),LocalTime.of(9,45),Reserva.Estado.CONFIRMADA));
        var order=inOrder(autorizador,lock,usuarios,actividades,horario,turnos,reservas);
        order.verify(autorizador).verificarAccesoSalon(ACTOR,"reserva.administrar",SALON);
        order.verify(lock).adquirir(SALON);order.verify(usuarios).findById(ACTOR);order.verify(usuarios).findById(CLIENTE);
        order.verify(actividades).findById(actividad.getId());order.verify(horario).resolver(SALON,DIA);
        order.verify(turnos).buscarPuntualesPorInstructorSalonYFecha(ACTOR,SALON,DIA);
        order.verify(turnos).buscarRecurrentesPorInstructorSalonYDia(ACTOR,SALON,(short)1);
        order.verify(reservas).existeTraslape(ACTOR,DIA,LocalTime.of(9,0),LocalTime.of(9,45),Reserva.Estado.CONFIRMADA);
        order.verify(reservas).save(any(Reserva.class));
    }
    @Test void M11_horarioParcialEspecialidadTurnoYTraslapeRechazanSinSave() {
        when(horario.resolver(SALON,DIA)).thenReturn(HorarioEfectivo.abiertoPorExcepcion(LocalTime.of(9,0),LocalTime.of(9,30)));
        assertThatThrownBy(()->service.crear(ACTOR,request())).hasMessageContaining("fuera del horario");verify(reservas,never()).save(any());
        when(horario.resolver(SALON,DIA)).thenReturn(HorarioEfectivo.abiertoPorHorarioSemanal(LocalTime.of(8,0),LocalTime.of(12,0)));
        instructor.getEspecialidades().clear();assertThatThrownBy(()->service.crear(ACTOR,request())).hasMessageContaining("no está capacitado");verify(reservas,never()).save(any());
        instructor.getEspecialidades().add(actividad);turno.setHoraFin(LocalTime.of(9,30));
        assertThatThrownBy(()->service.crear(ACTOR,request())).hasMessageContaining("no tiene turno");verify(reservas,never()).save(any());
        turno.setHoraFin(LocalTime.of(12,0));when(reservas.existeTraslape(ACTOR,DIA,LocalTime.of(9,0),LocalTime.of(9,45),Reserva.Estado.CONFIRMADA)).thenReturn(true);
        assertThatThrownBy(()->service.crear(ACTOR,request())).hasMessageContaining("ya tiene una reserva");verify(reservas,never()).save(any());
    }
    @Test void M11_sinAccesoOEntidadNoLeeEfectivoNiPersiste() {
        doThrow(new AccessDeniedException("denied")).when(autorizador).verificarAccesoSalon(ACTOR,"reserva.administrar",SALON);
        assertThatThrownBy(()->service.crear(ACTOR,request())).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(lock,horario,usuarios,actividades,turnos,reservas);
        doNothing().when(autorizador).verificarAccesoSalon(ACTOR,"reserva.administrar",SALON);
        when(usuarios.findById(ACTOR)).thenReturn(Optional.empty());assertThatThrownBy(()->service.crear(ACTOR,request())).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(horario,turnos,reservas);
    }
    @Test void M11_LEGACY_NOT_TARGET_cancelarRepetidaGuardaOtraVezSinCredito() {
        Reserva r=new Reserva();r.setSalon(sede);r.setEstado(Reserva.Estado.CONFIRMADA);when(reservas.findById(COMPRA)).thenReturn(Optional.of(r));
        service.cancelar(ACTOR,COMPRA);assertThat(r.getEstado()).isEqualTo(Reserva.Estado.CANCELADA);
        service.cancelar(ACTOR,COMPRA);assertThat(r.getEstado()).isEqualTo(Reserva.Estado.CANCELADA);
        verify(reservas,times(2)).save(r);verify(autorizador,times(2)).verificarAccesoSalon(ACTOR,"reserva.administrar",SALON);
        verifyNoInteractions(lock,horario,usuarios,actividades,turnos);
        clearInvocations(reservas);doThrow(new AccessDeniedException("denied")).when(autorizador).verificarAccesoSalon(ACTOR,"reserva.administrar",SALON);
        assertThatThrownBy(()->service.cancelar(ACTOR,COMPRA)).isInstanceOf(AccessDeniedException.class);verify(reservas,never()).save(any());
    }
}
