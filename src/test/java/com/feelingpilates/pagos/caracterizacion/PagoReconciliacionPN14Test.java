package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.pagos.entidad.Compra;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceLock;
import java.time.OffsetDateTime;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ResourceLock(STRIPE_LOCK)
class PagoReconciliacionPN14Test {
    PagoFixture f;StripeScope stripe;
    @BeforeEach void setup(){f=new PagoFixture();stripe=new StripeScope();}
    @AfterEach void restore(){stripe.close();}
    @Test void M05_reconciliaContinuaTrasErrorYConservaRecienteOSinPI() {
        Compra error=compra(f.paquete,"pi_error_dummy"),paid=compra(f.paquete,"pi_paid_dummy"),
            canceled=compra(f.paquete,"pi_cancel_dummy"),old=compra(f.paquete,"pi_old_dummy"),
            recent=compra(f.paquete,"pi_recent_dummy"),noPI=compra(f.paquete,null);
        recent.setCreadoEn(OffsetDateTime.now().plusHours(1));
        stripe.fake.failingIds.add("pi_error_dummy");
        for(var pair:Map.of("pi_paid_dummy","succeeded","pi_cancel_dummy","canceled","pi_old_dummy","processing","pi_recent_dummy","requires_payment_method").entrySet())
            stripe.fake.retrieved.put(pair.getKey(),intent(pair.getKey(),pair.getValue()));
        when(f.compras.findByEstado(Compra.EstadoCompra.pendiente)).thenReturn(List.of(error,paid,canceled,old,recent,noPI));
        OffsetDateTime before=OffsetDateTime.now();f.service.reconciliarComprasPendientes();
        verify(f.compras).findByEstado(Compra.EstadoCompra.pendiente);
        assertThat(paid.getEstado()).isEqualTo(Compra.EstadoCompra.pagada);
        assertThat(paid.getFechaExpiracion()).isBetween(before.plusDays(30),OffsetDateTime.now().plusDays(30));
        assertThat(canceled.getEstado()).isEqualTo(Compra.EstadoCompra.cancelada);
        assertThat(old.getEstado()).isEqualTo(Compra.EstadoCompra.cancelada);
        for(Compra c:List.of(error,recent,noPI)){assertThat(c.getEstado()).isEqualTo(Compra.EstadoCompra.pendiente);verify(f.compras,never()).save(same(c));}
        verify(f.compras).save(same(paid));verify(f.compras).save(same(canceled));verify(f.compras).save(same(old));
        assertThat(stripe.fake.requests).extracting(r->r.getPath()).containsExactly(
            "/v1/payment_intents/pi_error_dummy","/v1/payment_intents/pi_paid_dummy","/v1/payment_intents/pi_cancel_dummy",
            "/v1/payment_intents/pi_old_dummy","/v1/payment_intents/pi_recent_dummy");
    }
    @Test void M05_corteEstrictoEnVentanaAcotadaSinClaimIgualdadNow() {
        // Both fixtures bracket the moving 60-minute cutoff; equality with now is not controlled.
        OffsetDateTime captured=OffsetDateTime.now();
        Compra before=compra(f.paquete,"pi_before_dummy"),after=compra(f.paquete,"pi_after_dummy");
        before.setCreadoEn(captured.minusMinutes(61));after.setCreadoEn(captured.minusMinutes(59));
        stripe.fake.retrieved.put("pi_before_dummy",intent("pi_before_dummy","processing"));
        stripe.fake.retrieved.put("pi_after_dummy",intent("pi_after_dummy","processing"));
        when(f.compras.findByEstado(Compra.EstadoCompra.pendiente)).thenReturn(List.of(before,after));
        f.service.reconciliarComprasPendientes();
        assertThat(OffsetDateTime.now()).isBefore(captured.plusSeconds(30));
        assertThat(before.getEstado()).isEqualTo(Compra.EstadoCompra.cancelada);verify(f.compras).save(same(before));
        assertThat(after.getEstado()).isEqualTo(Compra.EstadoCompra.pendiente);verify(f.compras,never()).save(same(after));
    }
}
