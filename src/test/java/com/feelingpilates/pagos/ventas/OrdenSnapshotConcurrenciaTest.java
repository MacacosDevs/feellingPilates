package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.infraestructura.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.*;

class OrdenSnapshotConcurrenciaTest extends PN14Slice2Fixtures.Postgres {
    @Test void T10_dosConexionesPIDsBarreraAntesDeLockUnaWinnerYReplay() throws Exception {
        CyclicBarrier barrera=new CyclicBarrier(2); Set<Integer> pids=ConcurrentHashMap.newKeySet();
        var txm=new DataSourceTransactionManager(dataSource) {
            @Override protected void doBegin(Object tx,TransactionDefinition def) {
                super.doBegin(tx,def); pids.add(jdbc.queryForObject("select pg_backend_pid()",Integer.class));
                try{barrera.await(10,TimeUnit.SECONDS);}catch(Exception ex){throw new IllegalStateException(ex);}
            }
        };
        var adapter=new OrdenSnapshotJdbcAdapter(jdbc,txm); var e=evidencia(propuesta);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<CongelarOrdenSnapshot.Resultado> f=() -> adapter.congelar(propuesta,e);
            var a=pool.submit(f);var b=pool.submit(f);var x=a.get(25,TimeUnit.SECONDS);var y=b.get(25,TimeUnit.SECONDS);
            assertThat(pids).hasSize(2);assertThat(x.orden()).isEqualTo(y.orden());assertThat(x.replay()).isNotEqualTo(y.replay());
            assertThat(count("orden_venta")).isEqualTo(1);assertThat(count("compra_componente_snapshot")).isEqualTo(4);assertThat(count("informe_backfill_snapshot")).isEqualTo(1);
            System.out.println("PN14_S2_T10 freeze PIDs="+pids+" one winner, one exact replay");
        } finally {pool.shutdownNow();assertThat(pool.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void T10_legacyINSERTAntesDeTablaLockSeReleeYRechazaGrupoIncompleto() throws Exception {
        var e=evidencia(propuesta);CountDownLatch insertado=new CountDownLatch(1),liberar=new CountDownLatch(1),iniciado=new CountDownLatch(1);
        Set<Integer> pids=ConcurrentHashMap.newKeySet();ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<?> legacy=pool.submit(() -> new TransactionTemplate(manager).execute(s -> {
                pids.add(jdbc.queryForObject("select pg_backend_pid()",Integer.class));insertar(UUID.randomUUID(),grupo,3,12345,"mxn",cliente);insertado.countDown();
                try{assertThat(liberar.await(10,TimeUnit.SECONDS)).isTrue();}catch(InterruptedException ex){throw new IllegalStateException(ex);}return null;
            }));assertThat(insertado.await(10,TimeUnit.SECONDS)).isTrue();
            var txm=new DataSourceTransactionManager(dataSource) {
                @Override protected void doBegin(Object tx,TransactionDefinition def) { super.doBegin(tx,def);
                    pids.add(jdbc.queryForObject("select pg_backend_pid()",Integer.class));iniciado.countDown(); }
            };
            Future<Throwable> snapshot=pool.submit(() -> {try{new OrdenSnapshotJdbcAdapter(jdbc,txm).congelar(propuesta,e);return null;}catch(Throwable ex){return ex;}});
            assertThat(iniciado.await(10,TimeUnit.SECONDS)).isTrue();
            // Esperar una señal real pg_locks, no un sleep como prueba de carrera.
            long fin=System.nanoTime()+TimeUnit.SECONDS.toNanos(3); boolean esperando=false;
            while(System.nanoTime()<fin && !esperando) esperando=jdbc.queryForObject("select exists(select 1 from pg_locks where relation='compra'::regclass and mode='ShareRowExclusiveLock' and not granted)",Boolean.class);
            assertThat(esperando).isTrue();liberar.countDown();legacy.get(15,TimeUnit.SECONDS);
            assertThat(snapshot.get(15,TimeUnit.SECONDS)).isInstanceOf(RepositorioOrdenSnapshot.ConflictoSnapshot.class);
            assertThat(pids).hasSize(2);assertThat(count("compra")).isEqualTo(3);vacio();assertThat(count("informe_backfill_snapshot")).isZero();
            System.out.println("PN14_S2_T10 legacy INSERT PIDs="+pids+" table waiter observed; postlock membership=3 rejected");
        } finally {liberar.countDown();pool.shutdownNow();assertThat(pool.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void T10_lockTimeoutCeroEfectosYRetryAcotadoMismaClave() throws Exception {
        ExecutorService pool=Executors.newSingleThreadExecutor(); var e=evidencia(propuesta);
        try(var c=dataSource.getConnection()) {
            c.setAutoCommit(false);try(var st=c.createStatement()){st.execute("lock table compra in row exclusive mode");}
            Future<Throwable> f=pool.submit(() -> {try{repo.congelar(propuesta,e);return null;}catch(Throwable ex){return ex;}});
            assertThat(f.get(12,TimeUnit.SECONDS)).isInstanceOf(org.springframework.dao.DataAccessException.class);
            vacio(); assertThat(count("informe_backfill_snapshot")).isZero();c.rollback();
            assertThat(repo.congelar(propuesta,e).replay()).isFalse();assertThat(repo.congelar(propuesta,e).replay()).isTrue();
            assertThat(count("orden_venta")).isEqualTo(1);
        } finally {pool.shutdownNow();assertThat(pool.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
    }
}
