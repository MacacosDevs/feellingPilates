package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.pagos.dto.ItemCarritoRequest;
import com.feelingpilates.pagos.entidad.*;
import com.feelingpilates.pagos.repositorio.*;
import com.feelingpilates.pagos.servicio.VentaService;
import com.feelingpilates.ubicaciones.entidad.*;
import com.feelingpilates.usuarios.entidad.*;
import jakarta.persistence.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;

/** PostgreSQL/Flyway legacy schema only; every assertion reads real transactions. */
@DataJpaTest(properties="spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class,VentaService.class,CompraPersistenciaPN14Test.Config.class})
@Transactional(propagation=Propagation.NOT_SUPPORTED)
class CompraPersistenciaPN14Test {
    @Autowired CompraRepository compras;@Autowired PaqueteRepository paquetes;
    @Autowired VentaService ventas;@Autowired PlatformTransactionManager manager;@Autowired JdbcTemplate jdbc;
    @Autowired Observer observer;@PersistenceContext EntityManager em;
    UUID clienteId,actorId,paqueteId,salonId,inactivoId;
    <T>T tx(java.util.function.Supplier<T> action){
        TransactionTemplate template=new TransactionTemplate(manager);template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setTimeout(20);return template.execute(s->action.get());
    }
    @BeforeEach void fixtures(){
        observer.enabled=false;observer.rowsDuringSave=0;
        tx(()->{
            Salon s=new Salon();s.setNombre("pn14-"+UUID.randomUUID());
            var geo=jdbc.queryForMap("select estado_id, municipio_id from salon limit 1");
            s.setEstadoId(((Number)geo.get("estado_id")).shortValue());s.setMunicipioId(((Number)geo.get("municipio_id")).shortValue());em.persist(s);salonId=s.getId();
            Usuario c=new Usuario();c.setNombre("Cliente PN14 dummy");c.setCorreo(UUID.randomUUID()+"@pn14.invalid");em.persist(c);clienteId=c.getId();
            Usuario a=new Usuario();a.setNombre("Actor PN14 dummy");a.setCorreo(UUID.randomUUID()+"@pn14.invalid");em.persist(a);actorId=a.getId();
            Rol r=em.createQuery("select r from Rol r where r.nombre=:nombre",Rol.class).setParameter("nombre",Rol.PERSONAL).getSingleResult();
            UsuarioRol ur=new UsuarioRol(a,r,s);a.getRoles().add(ur);em.persist(ur);
            Paquete p=PN14Fixtures.paquete();p.setId(null);p.setCreadoEn(null);em.persist(p);paqueteId=p.getId();
            Paquete inactive=PN14Fixtures.paquete();inactive.setId(null);inactive.setCreadoEn(null);inactive.setActivo(false);em.persist(inactive);inactivoId=inactive.getId();
            em.flush();return null;
        });
    }
    Compra nueva(String key,String pi){
        Compra c=new Compra();c.setUsuario(em.getReference(Usuario.class,clienteId));c.setPaquete(em.getReference(Paquete.class,paqueteId));
        c.setMontoCentavos(31415);c.setIdempotencyKey(key);c.setStripePaymentIntentId(pi);return c;
    }
    @AfterEach void cleanup(){
        observer.enabled=false;
        if(clienteId!=null)tx(()->{
            jdbc.update("delete from compra where usuario_id=?",clienteId);
            jdbc.update("delete from usuario_rol where usuario_id=?",actorId);
            jdbc.update("delete from usuario where id in (?,?)",clienteId,actorId);
            jdbc.update("delete from paquete where id in (?,?)",paqueteId,inactivoId);
            jdbc.update("delete from salon where id=?",salonId);return null;
        });
    }
    @Test void M12_defaultsAsociacionesMontoYConsultasOrdenFiltrosCatalogo() {
        UUID group=UUID.randomUUID();String key="pn14-"+UUID.randomUUID(),pi="pi_pn14_"+UUID.randomUUID();
        UUID first=tx(()->{
            Compra c=nueva(key,pi);c.setEstado(Compra.EstadoCompra.pagada);c.setFechaExpiracion(OffsetDateTime.now().plusYears(1));
            c.setGrupoCompraId(group);c.setNumeroItem(2);return compras.saveAndFlush(c).getId();
        });
        UUID second=tx(()->{
            Compra c=nueva(null,null);c.setMetodoPago(Compra.MetodoPago.efectivo);c.setEstado(Compra.EstadoCompra.pagada);
            c.setSalon(em.getReference(Salon.class,salonId));c.setRegistradaPor(em.getReference(Usuario.class,actorId));
            c.setFechaExpiracion(OffsetDateTime.now().plusYears(2));c.setGrupoCompraId(group);c.setNumeroItem(1);
            return compras.saveAndFlush(c).getId();
        });
        UUID defaultId=tx(()->compras.saveAndFlush(nueva(null,null)).getId());
        tx(()->{
            Compra defaults=compras.findById(defaultId).orElseThrow();
            assertThat(defaults.getMoneda()).isEqualTo("mxn");assertThat(defaults.getMetodoPago()).isEqualTo(Compra.MetodoPago.stripe);
            assertThat(defaults.getEstado()).isEqualTo(Compra.EstadoCompra.pendiente);assertThat(defaults.getMontoCentavos()).isEqualTo(31415);
            assertThat(defaults.getUsuario().getNombre()).isEqualTo("Cliente PN14 dummy");assertThat(defaults.getPaquete().getId()).isEqualTo(paqueteId);
            assertThat(defaults.getCreadoEn()).isNotNull();assertThat(defaults.getActualizadoEn()).isNotNull();
            assertThat(defaults.getFechaExpiracion()).isNull();assertThat(defaults.getSalon()).isNull();
            assertThat(compras.findByIdempotencyKey(key).orElseThrow().getId()).isEqualTo(first);
            assertThat(compras.findByStripePaymentIntentId(pi).orElseThrow().getId()).isEqualTo(first);
            assertThat(compras.findByGrupoCompraIdOrderByNumeroItemAsc(group)).extracting(Compra::getId).containsExactly(second,first);
            assertThat(compras.findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(clienteId,Compra.EstadoCompra.pagada))
                .extracting(Compra::getId).containsExactly(second,first);
            assertThat(compras.findByUsuarioIdOrderByCreadoEnDesc(clienteId)).extracting(Compra::getId).containsExactly(defaultId,second,first);
            assertThat(compras.findByEstado(Compra.EstadoCompra.pendiente)).extracting(Compra::getId).contains(defaultId);
            assertThat(compras.findByMetodoPagoInOrderByCreadoEnDesc(List.of(Compra.MetodoPago.efectivo))).extracting(Compra::getId).contains(second).doesNotContain(first);
            OffsetDateTime exact=compras.findById(second).orElseThrow().getCreadoEn();
            var filtered=compras.buscarVentas(List.of(Compra.MetodoPago.efectivo,Compra.MetodoPago.transferencia),Compra.MetodoPago.efectivo,
                salonId,Compra.EstadoCompra.pagada,actorId,exact,exact,"cliente pn14",PageRequest.of(0,10));
            assertThat(filtered).extracting(Compra::getId).containsExactly(second);
            for(var method:List.of(Compra.MetodoPago.transferencia,Compra.MetodoPago.stripe))
                assertThat(compras.buscarVentas(List.of(Compra.MetodoPago.efectivo),method,salonId,null,null,exact,exact,null,PageRequest.of(0,10))).isEmpty();
            assertThat(compras.buscarVentas(List.of(Compra.MetodoPago.efectivo),null,UUID.randomUUID(),null,null,exact,exact,null,PageRequest.of(0,10))).isEmpty();
            assertThat(compras.buscarVentas(List.of(Compra.MetodoPago.efectivo),null,null,Compra.EstadoCompra.fallida,null,exact,exact,null,PageRequest.of(0,10))).isEmpty();
            assertThat(compras.buscarVentas(List.of(Compra.MetodoPago.efectivo),null,null,null,UUID.randomUUID(),exact,exact,null,PageRequest.of(0,10))).isEmpty();
            assertThat(compras.buscarVentas(List.of(Compra.MetodoPago.efectivo),null,null,null,null,exact,exact,"no-match-pn14",PageRequest.of(0,10))).isEmpty();
            assertThat(compras.buscarVentas(List.of(Compra.MetodoPago.efectivo),null,null,null,null,exact.plusSeconds(1),exact.plusSeconds(2),null,PageRequest.of(0,10))).isEmpty();
            assertThat(paquetes.findByActivoTrueOrderByCategoriaAscOrdenAsc()).extracting(Paquete::getId).contains(paqueteId).doesNotContain(inactivoId);
            assertThat(paquetes.findAllByOrderByActivoDescOrdenAsc()).extracting(Paquete::getId).contains(paqueteId,inactivoId);
            return null;
        });
    }
    @Test void M12_keyYPaymentIntentUniqueRechazanConRollbackIndependiente() {
        String key="pn14-"+UUID.randomUUID(),pi="pi_pn14_"+UUID.randomUUID();
        tx(()->compras.saveAndFlush(nueva(key,pi)));
        assertThatThrownBy(()->tx(()->compras.saveAndFlush(nueva(key,null)))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(()->tx(()->compras.saveAndFlush(nueva(null,pi)))).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(tx(()->jdbc.queryForObject("select count(*) from compra where usuario_id=?",Integer.class,clienteId))).isEqualTo(1);
    }
    @Test void M12_dosTransaccionesConcurrentesUnaCommitUnaConstraintUnaFila() throws Exception {
        String key="pn14-race-"+UUID.randomUUID();CyclicBarrier barrier=new CyclicBarrier(2);
        Set<Integer> connections=ConcurrentHashMap.newKeySet();ExecutorService pool=Executors.newFixedThreadPool(2);
        Callable<Throwable> insert=()->{
            try {tx(()->{
                jdbc.execute("set local lock_timeout='5s'");jdbc.execute("set local statement_timeout='10s'");
                connections.add(jdbc.queryForObject("select pg_backend_pid()",Integer.class));
                try{barrier.await(10,TimeUnit.SECONDS);}catch(Exception e){throw new IllegalStateException(e);}
                // Barrier precedes the potentially blocking flush, so winner can commit.
                return compras.saveAndFlush(nueva(key,null));
            });return null;}catch(Throwable e){return e;}
        };
        try {
            Future<Throwable> a=pool.submit(insert),b=pool.submit(insert);
            List<Throwable> results=Arrays.asList(a.get(25,TimeUnit.SECONDS),b.get(25,TimeUnit.SECONDS));
            assertThat(connections).hasSize(2);assertThat(results.stream().filter(Objects::isNull).count()).isEqualTo(1);
            Throwable loser=results.stream().filter(Objects::nonNull).findFirst().orElseThrow();
            assertThat(loser).isInstanceOf(DataIntegrityViolationException.class);
            Throwable root=loser;while(root.getCause()!=null)root=root.getCause();
            assertThat(root).isInstanceOf(java.sql.SQLException.class);assertThat(((java.sql.SQLException)root).getSQLState()).isEqualTo("23505");
            assertThat(tx(()->jdbc.queryForObject("select count(*) from compra where idempotency_key=?",Integer.class,key))).isEqualTo(1);
        } finally {pool.shutdownNow();assertThat(pool.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void M12_carritoProxiedInsertaPrimeraFilaPeroItemInvalidoRollbackDesdeNuevaTx() {
        assertThat(AopUtils.isAopProxy(ventas)).isTrue();observer.enabled=true;
        try {
            assertThatThrownBy(()->ventas.registrarVentaCarrito(clienteId,salonId,"efectivo",
                List.of(new ItemCarritoRequest(paqueteId,1),new ItemCarritoRequest(inactivoId,1)),actorId))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Paquete no encontrado");
            // Observer flushes the REAL repository insert and sees it on the same DB transaction.
            assertThat(observer.rowsDuringSave).isEqualTo(1);
            assertThat(tx(()->jdbc.queryForObject("select count(*) from compra where usuario_id=?",Integer.class,clienteId))).isZero();
        } finally {observer.enabled=false;}
    }
    static class Observer {
        @PersistenceContext EntityManager em;@Autowired JdbcTemplate jdbc;
        volatile boolean enabled;volatile int rowsDuringSave;
        void saved(Compra c){if(enabled){em.flush();rowsDuringSave=jdbc.queryForObject("select count(*) from compra where id=?",Integer.class,c.getId());}}
    }
    @TestConfiguration(proxyBeanMethods=false) static class Config {
        @Bean Observer observer(){return new Observer();}
        @Bean static BeanPostProcessor realInsertObserver(ObjectProvider<Observer> observer){
            return new BeanPostProcessor(){
                @Override public Object postProcessAfterInitialization(Object bean,String name){
                    if(!name.equals("compraRepository") || !(bean instanceof CompraRepository))return bean;
                    ProxyFactory proxy=new ProxyFactory(bean);
                    proxy.addAdvice((MethodInterceptor)i->{Object result=i.proceed();
                        if(i.getMethod().getName().equals("save") && result instanceof Compra c)observer.getObject().saved(c);
                        return result;});return proxy.getProxy();
                }
            };
        }
    }
}
