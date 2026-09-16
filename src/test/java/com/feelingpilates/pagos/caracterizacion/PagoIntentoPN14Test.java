package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.exception.*;
import com.feelingpilates.pagos.entidad.Compra;
import com.stripe.net.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceLock;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ResourceLock(STRIPE_LOCK)
class PagoIntentoPN14Test {
    PagoFixture f; StripeScope stripe;
    @BeforeEach void setup(){f=new PagoFixture();stripe=new StripeScope();}
    @AfterEach void restore(){stripe.close();}
    @Test void M01_montoServidorOpcionesMetadataYDosGuardados() {
        List<String> estados=new ArrayList<>();
        doAnswer(i->{Compra c=i.getArgument(0);if(c.getId()==null)c.setId(COMPRA);
            assertThat(c.getUsuario()).isSameAs(f.usuario);assertThat(c.getPaquete()).isSameAs(f.paquete);
            assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pendiente);
            assertThat(c.getMontoCentavos()).isEqualTo(12345);assertThat(c.getMoneda()).isEqualTo("mxn");
            estados.add(c.getStripePaymentIntentId());return c;}).when(f.compras).save(any(Compra.class));
        var r=f.service.crearIntentoPago(CLIENTE,PAQUETE,"key_dummy");
        assertThat(r.compraId()).isEqualTo(COMPRA);assertThat(r.clientSecret()).isEqualTo("pi_pn14_created_secret_dummy");
        assertThat(r.publishableKey()).isEqualTo("pk_test_pn14_dummy");
        assertThat(estados).containsExactly(null,"pi_pn14_created");
        assertThat(stripe.fake.requests).hasSize(1);
        ApiRequest req=stripe.fake.requests.getFirst();
        assertThat(req.getMethod()).isEqualTo(ApiResource.RequestMethod.POST);
        assertThat(req.getPath()).isEqualTo("/v1/payment_intents");
        assertThat(req.getParams()).containsEntry("amount",12345L).containsEntry("currency","mxn")
            .containsEntry("metadata",Map.of("compraId",COMPRA.toString()))
            .containsEntry("automatic_payment_methods",Map.of("enabled",true,"allow_redirects","never"));
        assertThat(req.getOptions().getIdempotencyKey()).isEqualTo("key_dummy");
    }
    @Test void M01_paqueteAusenteInactivoYUsuarioAusenteNoTienenEfectos() {
        when(f.paquetes.findById(PAQUETE)).thenReturn(Optional.empty());
        assertThatThrownBy(()->f.service.crearIntentoPago(CLIENTE,PAQUETE,null)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(f.usuarios,f.compras);
        f.paquete.setActivo(false);when(f.paquetes.findById(PAQUETE)).thenReturn(Optional.of(f.paquete));
        assertThatThrownBy(()->f.service.crearIntentoPago(CLIENTE,PAQUETE,null)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(f.usuarios,f.compras);
        f.paquete.setActivo(true);when(f.usuarios.findById(CLIENTE)).thenReturn(Optional.empty());
        assertThatThrownBy(()->f.service.crearIntentoPago(CLIENTE,PAQUETE,null)).isInstanceOf(ResourceNotFoundException.class);
        verify(f.compras,never()).save(any());assertThat(stripe.fake.requests).isEmpty();
    }
    @Test void M02_LEGACY_NOT_TARGET_reusoAntesDeValidarPayloadDistinto() {
        Compra c=compra(f.paquete,"pi_reused");
        when(f.compras.findByIdempotencyKey("same_dummy")).thenReturn(Optional.of(c));
        stripe.fake.retrieved.put("pi_reused",intent("pi_reused","succeeded"));
        var r=f.service.crearIntentoPago(UUID.randomUUID(),UUID.randomUUID(),"same_dummy");
        assertThat(r.compraId()).isEqualTo(COMPRA);assertThat(r.clientSecret()).isEqualTo("pi_reused_secret_dummy");
        assertThat(r.publishableKey()).isEqualTo("pk_test_pn14_dummy");
        verifyNoInteractions(f.paquetes,f.usuarios);verify(f.compras,never()).save(any());
        assertThat(stripe.fake.requests).hasSize(1);assertThat(stripe.fake.requests.getFirst().getMethod()).isEqualTo(ApiResource.RequestMethod.GET);
        assertThat(stripe.fake.requests.getFirst().getPath()).isEqualTo("/v1/payment_intents/pi_reused");
    }
    @Test void M02_nullYBlankCreanSinClaveStripe() {
        for(String key:Arrays.asList(null,"  ")){
            f.service.crearIntentoPago(CLIENTE,PAQUETE,key);
            assertThat(stripe.fake.requests.getLast().getOptions().getIdempotencyKey()).isNull();
        }
        verify(f.compras,never()).findByIdempotencyKey(any());verify(f.compras,times(4)).save(any());
        assertThat(stripe.fake.requests).hasSize(2);
    }
    @Test void M02_erroresCreateYRetrieveSeTraducenSinAsociacionNueva() {
        stripe.fake.failCreate=true;
        assertThatThrownBy(()->f.service.crearIntentoPago(CLIENTE,PAQUETE,"new_dummy"))
            .isInstanceOf(ValidacionException.class).hasMessage("No se pudo iniciar el pago: pn14 create rejected");
        verify(f.compras,times(1)).save(any());
        Compra c=compra(f.paquete,"pi_bad");when(f.compras.findByIdempotencyKey("old_dummy")).thenReturn(Optional.of(c));
        stripe.fake.failingIds.add("pi_bad");
        assertThatThrownBy(()->f.service.crearIntentoPago(CLIENTE,PAQUETE,"old_dummy"))
            .isInstanceOf(ValidacionException.class).hasMessage("No se pudo continuar el pago: pn14 retrieve rejected");
        verify(f.compras,times(1)).save(any());assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pendiente);
    }
}
