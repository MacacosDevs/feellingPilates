package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.pagos.entidad.Compra;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceLock;
import java.time.OffsetDateTime;
import java.util.Optional;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ResourceLock(STRIPE_LOCK)
class PagoWebhookPN14Test {
    PagoFixture f;StripeScope stripe;
    @BeforeEach void setup(){ f=new PagoFixture();stripe=new StripeScope(); }
    @AfterEach void restore(){stripe.close();}
    String pi(String tipo,String version){return evento(tipo,"{\"id\":\"pi_webhook_dummy\",\"object\":\"payment_intent\"}",version);}
    void enviar(String raw){f.service.procesarWebhook(raw,firma(raw));}
    @Test void M03_HMACRealRawAlteradoYFirmaInvalidaNoMutan() {
        String raw=pi("payment_intent.succeeded",Stripe.API_VERSION);
        assertThatThrownBy(()->f.service.procesarWebhook(raw,"t=1,v1=bad"))
            .isInstanceOf(ValidacionException.class).hasMessage("Firma de webhook de Stripe inválida");
        String firma=firma(raw);
        assertThatThrownBy(()->f.service.procesarWebhook(raw+" ",firma)).isInstanceOf(ValidacionException.class);
        verifyNoInteractions(f.compras);
    }
    @Test void M04_succeededTipadoPendientePagadaDuplicadoNoRenueva() throws Exception {
        Compra c=compra(f.paquete,"pi_webhook_dummy");
        when(f.compras.findByStripePaymentIntentId("pi_webhook_dummy")).thenReturn(Optional.of(c));
        String raw=pi("payment_intent.succeeded",Stripe.API_VERSION);
        var event=Webhook.constructEvent(raw,firma(raw),WHSEC);
        assertThat(event.getDataObjectDeserializer().getObject()).hasValueSatisfying(o->assertThat(o).isInstanceOf(PaymentIntent.class));
        OffsetDateTime before=OffsetDateTime.now();enviar(raw);OffsetDateTime after=OffsetDateTime.now();
        assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pagada);
        assertThat(c.getFechaExpiracion()).isBetween(before.plusDays(30),after.plusDays(30));
        var expiration=c.getFechaExpiracion();enviar(raw);
        assertThat(c.getFechaExpiracion()).isEqualTo(expiration);verify(f.compras,times(1)).save(c);
    }
    @Test void M04_LEGACY_NOT_TARGET_failedDesdePagadaYSucceededPosteriorFallback() throws Exception {
        Compra c=compra(f.paquete,"pi_webhook_dummy");c.setEstado(Compra.EstadoCompra.pagada);c.setFechaExpiracion(FECHA);
        when(f.compras.findByStripePaymentIntentId("pi_webhook_dummy")).thenReturn(Optional.of(c));
        String failed=pi("payment_intent.payment_failed","2000-01-01");
        assertThat(Webhook.constructEvent(failed,firma(failed),WHSEC).getDataObjectDeserializer().getObject()).isEmpty();
        enviar(failed);assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.fallida);
        assertThat(c.getFechaExpiracion()).isEqualTo(FECHA);verify(f.compras).save(c);
        OffsetDateTime before=OffsetDateTime.now();enviar(pi("payment_intent.succeeded","2000-01-01"));
        assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pagada);
        assertThat(c.getFechaExpiracion()).isBetween(before.plusDays(30),OffsetDateTime.now().plusDays(30));
        verify(f.compras,times(2)).save(c);
    }
    @Test void M04_refundedEstadoSoloDuplicadoYPIAusente() {
        Compra c=compra(f.paquete,"pi_webhook_dummy");c.setEstado(Compra.EstadoCompra.pagada);c.setFechaExpiracion(FECHA);
        when(f.compras.findByStripePaymentIntentId("pi_webhook_dummy")).thenReturn(Optional.of(c));
        String raw=evento("charge.refunded","{\"id\":\"ch_pn14_dummy\",\"object\":\"charge\",\"payment_intent\":\"pi_webhook_dummy\"}",Stripe.API_VERSION);
        enviar(raw);assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.reembolsada);
        assertThat(c.getFechaExpiracion()).isEqualTo(FECHA);assertThat(c.getMontoCentavos()).isEqualTo(12345);
        enviar(raw);verify(f.compras,times(1)).save(c);clearInvocations(f.compras);
        enviar(evento("charge.refunded","{\"id\":\"ch_null_dummy\",\"object\":\"charge\",\"payment_intent\":null}","2000-01-01"));
        verifyNoInteractions(f.compras);
    }
    @Test void M04_compraDesconocidaYTipoIgnoradoNoGuardan() {
        enviar(pi("payment_intent.succeeded",Stripe.API_VERSION));
        enviar(pi("payment_intent.payment_failed",Stripe.API_VERSION));
        enviar(evento("charge.refunded","{\"object\":\"charge\",\"payment_intent\":\"pi_unknown_dummy\"}","2000-01-01"));
        verify(f.compras,never()).save(any());clearInvocations(f.compras);
        enviar(pi("payment_intent.created",Stripe.API_VERSION));verifyNoInteractions(f.compras);
    }
}
