package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.exception.*;
import com.feelingpilates.pagos.dto.*;
import com.feelingpilates.pagos.entidad.*;
import com.feelingpilates.pagos.servicio.VentaService;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.usuarios.entidad.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ResourceLock(STRIPE_LOCK)
class VentaServicePN14Test {
    PagoFixture f; SalonRepository salones;VentaService service;Usuario actor;Salon sede;StripeScope stripe;
    @BeforeEach void setup(){
        f=new PagoFixture();stripe=new StripeScope();salones=mock(SalonRepository.class);service=new VentaService(f.paquetes,f.compras,f.usuarios,salones);
        actor=usuario(ACTOR,"Recepción dummy");sede=salon();rol(actor,Rol.PERSONAL,sede);
        when(f.usuarios.findById(ACTOR)).thenReturn(Optional.of(actor));
    }
    @AfterEach void restore(){stripe.close();}
    @Test void M07_LEGACY_NOT_TARGET_efectivoYTransferenciaPagadaInmediata() {
        for(String metodo:List.of("efectivo","transferencia")){
            clearInvocations(f.compras);OffsetDateTime before=OffsetDateTime.now();
            var r=service.registrarVenta(CLIENTE,PAQUETE,SALON,metodo,ACTOR);
            var captor=org.mockito.ArgumentCaptor.forClass(Compra.class);verify(f.compras).save(captor.capture());
            Compra c=captor.getValue();assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pagada);
            assertThat(c.getMetodoPago().name()).isEqualTo(metodo);assertThat(c.getUsuario()).isSameAs(f.usuario);
            assertThat(c.getRegistradaPor()).isSameAs(actor);assertThat(c.getSalon()).isSameAs(sede);
            assertThat(c.getPaquete()).isSameAs(f.paquete);assertThat(c.getMontoCentavos()).isEqualTo(12345);
            assertThat(c.getFechaExpiracion()).isBetween(before.plusDays(30),OffsetDateTime.now().plusDays(30));
            assertThat(c.getStripePaymentIntentId()).isNull();assertThat(c.getGrupoCompraId()).isNotNull();assertThat(c.getNumeroItem()).isEqualTo(1);
            assertThat(r.metodoPago()).isEqualTo(metodo);assertThat(r.estado()).isEqualTo("pagada");assertThat(r.montoCentavos()).isEqualTo(12345);
        }
    }
    @Test void M07_carritoUnaCompraPorUnidadGrupoCompartidoYTotal() {
        Paquete p2=paquete();p2.setId(UUID.randomUUID());p2.setPrecioCentavos(200);
        when(f.paquetes.findById(p2.getId())).thenReturn(Optional.of(p2));
        var r=service.registrarVentaCarrito(CLIENTE,SALON,"efectivo",List.of(new ItemCarritoRequest(PAQUETE,2),new ItemCarritoRequest(p2.getId(),1)),ACTOR);
        var cap=org.mockito.ArgumentCaptor.forClass(Compra.class);verify(f.compras,times(3)).save(cap.capture());
        assertThat(cap.getAllValues()).extracting(Compra::getNumeroItem).containsExactly(1,2,3);
        assertThat(cap.getAllValues()).allSatisfy(c->assertThat(c.getGrupoCompraId()).isEqualTo(r.grupoCompraId()));
        assertThat(r.grupoCompraId()).isNotNull();assertThat(r.totalCentavos()).isEqualTo(24890);
        assertThat(r.items()).extracting(VentaResponse::montoCentavos).containsExactly(12345,12345,200);
    }
    @Test void M07_metodoStripeOInvalidoRechazaSinLecturasNiSave() {
        for(String metodo:List.of("stripe","cash"))
            assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,metodo,ACTOR)).isInstanceOf(ValidacionException.class);
        verifyNoInteractions(f.compras,f.usuarios,f.paquetes,salones);
    }
    @Test void M07_paqueteMissingInactivoYActoresMissingSinSave() {
        when(f.paquetes.findById(PAQUETE)).thenReturn(Optional.empty());
        assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR)).isInstanceOf(ResourceNotFoundException.class);
        f.paquete.setActivo(false);when(f.paquetes.findById(PAQUETE)).thenReturn(Optional.of(f.paquete));
        assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR)).isInstanceOf(ResourceNotFoundException.class);
        when(f.usuarios.findById(ACTOR)).thenReturn(Optional.empty());
        assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR)).hasMessage("Usuario no encontrado");
        when(f.usuarios.findById(CLIENTE)).thenReturn(Optional.empty());
        assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR)).hasMessage("Cliente no encontrado");
        verify(f.compras,never()).save(any());
    }
    @Test void M07_sedesPropiasGlobalesYRechazoOtraSedeOInactiva() {
        assertThat(service.sedesDisponibles(ACTOR)).containsExactly(new SedeVentaResponse(SALON,"Sede dummy"));
        assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,UUID.randomUUID(),"efectivo",ACTOR)).isInstanceOf(ValidacionException.class);
        verify(f.compras,never()).save(any());
        for(String role:List.of(Rol.ADMIN,Rol.SUPER_ADMIN)){
            actor.getRoles().clear();rol(actor,role,null);
            SalonRepository.SalonProjection projection=mock(SalonRepository.SalonProjection.class);
            when(projection.getId()).thenReturn(SALON);when(projection.getNombre()).thenReturn("Sede dummy");
            when(salones.listarActivos()).thenReturn(List.of(projection));
            assertThat(service.sedesDisponibles(ACTOR)).containsExactly(new SedeVentaResponse(SALON,"Sede dummy"));
            when(salones.findById(SALON)).thenReturn(Optional.empty());
            assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR)).hasMessage("Sede no encontrada");
            sede.setActivo(false);when(salones.findById(SALON)).thenReturn(Optional.of(sede));
            assertThatThrownBy(()->service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR)).hasMessage("La sede seleccionada no está activa");
            verify(f.compras,never()).save(any());sede.setActivo(true);
            assertThat(service.registrarVenta(CLIENTE,PAQUETE,SALON,"efectivo",ACTOR).salonNombre()).isEqualTo("Sede dummy");
            clearInvocations(f.compras);
        }
    }
    @Test void M08_LEGACY_NOT_TARGET_refundSoloEstadoMotivoNoDinero() {
        Compra c=compra(f.paquete,null);c.setMetodoPago(Compra.MetodoPago.transferencia);c.setEstado(Compra.EstadoCompra.pagada);c.setFechaExpiracion(FECHA);
        when(f.compras.findById(COMPRA)).thenReturn(Optional.of(c));
        var r=service.reembolsarVenta(COMPRA,"Motivo dummy");
        assertThat(r.estado()).isEqualTo("reembolsada");assertThat(r.motivoEstado()).isEqualTo("Motivo dummy");
        assertThat(c.getMontoCentavos()).isEqualTo(12345);assertThat(c.getFechaExpiracion()).isEqualTo(FECHA);
        verify(f.compras).save(c);verifyNoInteractions(f.paquetes,f.usuarios,salones);
        assertThatThrownBy(()->service.reembolsarVenta(COMPRA,"otra")).hasMessage("Solo se puede reembolsar una venta pagada");
        c.setMetodoPago(Compra.MetodoPago.stripe);c.setEstado(Compra.EstadoCompra.pagada);
        assertThatThrownBy(()->service.reembolsarVenta(COMPRA,"otra")).isInstanceOf(ValidacionException.class);
        verify(f.compras,times(1)).save(c);
        assertThat(stripe.fake.requests).isEmpty();
    }
    @Test void M08_historialNombresActualesYNulosHistoricos() {
        Compra c=compra(f.paquete,null);c.setMetodoPago(Compra.MetodoPago.efectivo);
        when(f.compras.findByMetodoPagoInOrderByCreadoEnDesc(List.of(Compra.MetodoPago.efectivo,Compra.MetodoPago.transferencia))).thenReturn(List.of(c));
        c.getUsuario().setNombre("Cliente renombrado");f.paquete.setNombre("Producto renombrado");
        var r=service.historial().getFirst();assertThat(r.id()).isEqualTo(COMPRA);
        assertThat(r.clienteNombre()).isEqualTo("Cliente renombrado");assertThat(r.paqueteNombre()).isEqualTo("Producto renombrado");
        assertThat(r.registradaPorNombre()).isNull();assertThat(r.salonNombre()).isNull();assertThat(r.grupoCompraId()).isNull();
        assertThat(r.numeroItem()).isNull();assertThat(r.motivoEstado()).isNull();
        c.setRegistradaPor(actor);c.setSalon(sede);c.setGrupoCompraId(COMPRA);c.setNumeroItem(2);c.setMotivoEstado("motivo");
        var actual=service.historial().getFirst();assertThat(actual.registradaPorNombre()).isEqualTo("Recepción dummy");
        assertThat(actual.salonNombre()).isEqualTo("Sede dummy");assertThat(actual.numeroItem()).isEqualTo(2);
        assertThat(actual.grupoCompraId()).isEqualTo(COMPRA);assertThat(actual.motivoEstado()).isEqualTo("motivo");
    }
    @Test void M08_filtrosTrimRangoUTCInclusivoYDefaults() {
        Pageable page=PageRequest.of(1,5);
        when(f.compras.buscarVentas(anyList(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(Page.empty());
        service.historial("transferencia",SALON,"reembolsada",ACTOR,LocalDate.of(2026,1,2),LocalDate.of(2026,1,3),"  Cliente  ",page);
        verify(f.compras).buscarVentas(List.of(Compra.MetodoPago.efectivo,Compra.MetodoPago.transferencia),Compra.MetodoPago.transferencia,SALON,
            Compra.EstadoCompra.reembolsada,ACTOR,OffsetDateTime.parse("2026-01-02T00:00:00Z"),OffsetDateTime.parse("2026-01-03T23:59:59.999999999Z"),"Cliente",page);
        service.historial(" ",null," ",null,null,null," ",page);
        verify(f.compras).buscarVentas(List.of(Compra.MetodoPago.efectivo,Compra.MetodoPago.transferencia),null,null,null,null,
            OffsetDateTime.parse("1970-01-01T00:00:00Z"),OffsetDateTime.parse("9999-12-31T23:59:59Z"),null,page);
        clearInvocations(f.compras);
        assertThatThrownBy(()->service.historial("invalid",null,null,null,null,null,null,page)).hasMessage("Método de pago inválido");
        assertThatThrownBy(()->service.historial(null,null,"invalid",null,null,null,null,page)).hasMessage("Estado inválido");verifyNoInteractions(f.compras);
    }
}
