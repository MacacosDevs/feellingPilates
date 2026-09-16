package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.pagos.entidad.*;
import com.feelingpilates.pagos.repositorio.*;
import com.feelingpilates.pagos.servicio.PagoService;
import com.feelingpilates.usuarios.entidad.*;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.feelingpilates.ubicaciones.entidad.*;
import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.ApiResource;
import com.stripe.net.StripeResponseGetter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/** Synthetic fixtures only: no provider transport and no target-domain rules. */
final class PN14Fixtures {
    static final String STRIPE_LOCK = "PN14_STRIPE_GLOBALS";
    static final String WHSEC = "whsec_pn14_dummy";
    static final UUID CLIENTE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID PAQUETE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID COMPRA = UUID.fromString("33333333-3333-3333-3333-333333333333");
    static final UUID ACTOR = UUID.fromString("44444444-4444-4444-4444-444444444444");
    static final UUID SALON = UUID.fromString("55555555-5555-5555-5555-555555555555");
    static final OffsetDateTime FECHA = OffsetDateTime.parse("2020-01-02T03:04:05Z");
    static Usuario usuario(UUID id, String nombre) {
        Usuario u = new Usuario(); u.setId(id); u.setNombre(nombre); u.setCorreo(id+"@pn14.invalid"); return u;
    }
    static Paquete paquete() {
        Paquete p = new Paquete(); p.setId(PAQUETE); p.setNombre("Pilates dummy");
        p.setCategoria(Paquete.CategoriaPaquete.pilates); p.setPrecioCentavos(12345); p.setVigenciaDias(30);
        p.setDescripcion("Descripción dummy"); p.setUnitarioTexto("4 clases"); p.setCreadoEn(FECHA); return p;
    }
    static Salon salon() { Salon s = new Salon(); s.setId(SALON); s.setNombre("Sede dummy"); return s; }
    static TipoActividad actividad() {
        TipoActividad a = new TipoActividad(); a.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        a.setNombre("Reformer dummy"); a.setDuracionMinutos((short)45); return a;
    }
    static void rol(Usuario u, String nombre, Salon sede) {
        Rol r = new Rol(); r.setNombre(nombre); u.getRoles().add(new UsuarioRol(u, r, sede));
    }
    static Compra compra(Paquete p, String pi) {
        Compra c = new Compra(); c.setId(COMPRA); c.setUsuario(usuario(CLIENTE,"Cliente dummy"));
        c.setPaquete(p); c.setMontoCentavos(12345); c.setStripePaymentIntentId(pi); c.setCreadoEn(FECHA); return c;
    }
    static PaymentIntent intent(String id, String estado) {
        PaymentIntent pi = new PaymentIntent(); pi.setId(id); pi.setClientSecret(id+"_secret_dummy"); pi.setStatus(estado); return pi;
    }
    static String evento(String tipo, String objeto, String version) {
        return "{\n  \"id\": \"evt_pn14_dummy\", \"object\": \"event\", \"api_version\": \""+version+
                "\", \"type\": \""+tipo+"\", \"data\": {\"object\": "+objeto+"}\n}";
    }
    static String firma(String raw) {
        try {
            long timestamp = Instant.now().getEpochSecond();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(WHSEC.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "t="+timestamp+",v1="+HexFormat.of().formatHex(mac.doFinal((timestamp+"."+raw).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new AssertionError(e); }
    }
    static RequestPostProcessor principal(String... permisos) {
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                new UsuarioAutenticado(ACTOR,"actor@pn14.invalid"),null,
                Arrays.stream(permisos).map(SimpleGrantedAuthority::new).toList()));
    }
    static final class PagoFixture {
        final PaqueteRepository paquetes = mock(PaqueteRepository.class);
        final CompraRepository compras = mock(CompraRepository.class);
        final UsuarioRepository usuarios = mock(UsuarioRepository.class);
        final Paquete paquete = paquete();
        final Usuario usuario = usuario(CLIENTE,"Cliente dummy");
        final PagoService service = new PagoService(paquetes,compras,usuarios,"pk_test_pn14_dummy",WHSEC,60);
        PagoFixture() {
            when(paquetes.findById(PAQUETE)).thenReturn(Optional.of(paquete));
            when(usuarios.findById(CLIENTE)).thenReturn(Optional.of(usuario));
            when(compras.save(any(Compra.class))).thenAnswer(i -> {
                Compra c = i.getArgument(0); if(c.getId()==null) c.setId(COMPRA);
                if(c.getCreadoEn()==null)c.setCreadoEn(FECHA); return c;
            });
        }
    }
    static final class StripeScope implements AutoCloseable {
        final StripeResponseGetter oldGetter = ApiResource.getGlobalResponseGetter();
        final String oldKey = Stripe.apiKey;
        final StripeResponseGetterPN14Fake fake = new StripeResponseGetterPN14Fake();
        StripeScope() { Stripe.apiKey="sk_test_pn14_dummy"; ApiResource.setGlobalResponseGetter(fake); }
        public void close() {
            try { ApiResource.setGlobalResponseGetter(oldGetter); } finally { Stripe.apiKey=oldKey; }
        }
    }
    private PN14Fixtures() {}
}
