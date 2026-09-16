package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.pagos.controlador.PaqueteController;
import com.feelingpilates.pagos.dto.*;
import com.feelingpilates.pagos.entidad.*;
import com.feelingpilates.pagos.servicio.PaqueteGestionService;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.ubicaciones.repositorio.TipoActividadRepository;
import org.junit.jupiter.api.*;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class CatalogoPN14Test {
    PagoFixture f;TipoActividadRepository actividades;PaqueteGestionService service;TipoActividad actividad;
    @BeforeEach void setup(){f=new PagoFixture();actividades=mock(TipoActividadRepository.class);service=new PaqueteGestionService(f.paquetes,actividades);
        actividad=actividad();when(actividades.findById(actividad.getId())).thenReturn(Optional.of(actividad));
        when(f.paquetes.save(any(Paquete.class))).thenAnswer(i->{Paquete p=i.getArgument(0);p.setId(PAQUETE);p.setCreadoEn(FECHA);return p;});}
    @Test void M09_publicoConsultaSoloActivosDTOComposicionYCategoriaNull() {
        f.paquete.setCategoria(null);f.paquete.getActividades().add(new PaqueteActividad(f.paquete,actividad,4));
        when(f.paquetes.findByActivoTrueOrderByCategoriaAscOrdenAsc()).thenReturn(List.of(f.paquete));
        var r=new PaqueteController(f.paquetes).listar();
        assertThat(r).containsExactly(new PaqueteResponse(PAQUETE,null,"Pilates dummy","Descripción dummy",12345,30,"4 clases",false,
            List.of(new ActividadPaqueteResponse(actividad.getId(),"Reformer dummy",4))));
        verify(f.paquetes).findByActivoTrueOrderByCategoriaAscOrdenAsc();verify(f.paquetes,never()).findAll();verifyNoInteractions(f.compras);
    }
    @Test void M09_gestionCrearActualizarReemplazaActividadesYToggle() {
        var created=service.crear(new CrearPaqueteRequest("Nuevo","D",500,10,"U",true,3,List.of(new ActividadPaqueteRequest(actividad.getId(),2))));
        assertThat(created).isEqualTo(new PaqueteGestionResponse(PAQUETE,"Nuevo","D",500,10,"U",true,true,3,
            List.of(new ActividadPaqueteResponse(actividad.getId(),"Reformer dummy",2)),FECHA));
        var cap=org.mockito.ArgumentCaptor.forClass(Paquete.class);verify(f.paquetes).save(cap.capture());
        Paquete p=cap.getValue();assertThat(p.getCategoria()).isNull();assertThat(p.getActividades().getFirst().getPaquete()).isSameAs(p);
        when(f.paquetes.findById(PAQUETE)).thenReturn(Optional.of(p));
        TipoActividad otra=actividad();otra.setId(UUID.randomUUID());otra.setNombre("Otra dummy");when(actividades.findById(otra.getId())).thenReturn(Optional.of(otra));
        var updated=service.actualizar(PAQUETE,new ActualizarPaqueteRequest("Editado",null,700,7,null,false,false,1,List.of(new ActividadPaqueteRequest(otra.getId(),8))));
        assertThat(updated.nombre()).isEqualTo("Editado");assertThat(updated.precioCentavos()).isEqualTo(700);assertThat(updated.vigenciaDias()).isEqualTo(7);
        assertThat(updated.activo()).isFalse();assertThat(updated.actividades()).containsExactly(new ActividadPaqueteResponse(otra.getId(),"Otra dummy",8));
        assertThat(p.getActividades()).hasSize(1);assertThat(p.getActividades().getFirst().getPaquete()).isSameAs(p);
        assertThat(service.habilitar(PAQUETE).activo()).isTrue();assertThat(service.deshabilitar(PAQUETE).activo()).isFalse();
        when(f.paquetes.findAllByOrderByActivoDescOrdenAsc()).thenReturn(List.of(p));assertThat(service.listarTodos()).hasSize(1);
        verify(f.paquetes).findAllByOrderByActivoDescOrdenAsc();verifyNoInteractions(f.compras);
    }
    @Test void M09_actividadInexistenteYPaqueteInexistenteNoGuardan() {
        UUID missing=UUID.randomUUID();
        assertThatThrownBy(()->service.crear(new CrearPaqueteRequest("Nuevo",null,1,1,null,false,0,List.of(new ActividadPaqueteRequest(missing,1)))))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("Actividad no encontrada");
        assertThatThrownBy(()->service.habilitar(missing)).hasMessage("Paquete no encontrado");
        assertThatThrownBy(()->service.deshabilitar(missing)).hasMessage("Paquete no encontrado");verify(f.paquetes,never()).save(any());
    }
}
