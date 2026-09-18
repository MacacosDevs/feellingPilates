package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class BackfillOrdenSnapshotPostgresTest extends PN14Slice2Fixtures.Postgres {
    @Test void T13_repeatBackfillMismosKeysHashesConteosImportesYReportePersistido() {
        var b=backfill(List.of(fuente(propuesta,RAW)));var x=b.ejecutar(propuesta.scopeKey());var y=b.ejecutar(propuesta.scopeKey());
        assertThat(x.replay()).isFalse();assertThat(y.replay()).isTrue();assertThat(x.orden()).isEqualTo(y.orden());assertThat(x.informe()).isEqualTo(y.informe());
        assertThat(x.congeladas()).isEqualTo(2);assertThat(x.replays()).isZero();assertThat(y.congeladas()).isZero();assertThat(y.replays()).isEqualTo(2);
        assertThat(x.informe().totalObservadoUnidadesMinimas()).isEqualTo(java.math.BigInteger.valueOf(24690));
        assertThat(count("orden_venta")).isEqualTo(1);assertThat(count("informe_backfill_snapshot")).isEqualTo(1);assertThat(count("compra_componente_snapshot")).isEqualTo(4);
    }
    @Test void T13_sinSobreReporteAppendReplayYCorrigidoSoloScopeNoCongelado() {
        var sin=backfill(List.of());var i=sin.ejecutar(propuesta.scopeKey()).informe();vacio();
        assertThat(i.estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);
        assertThat(sin.ejecutar(propuesta.scopeKey()).informe()).isEqualTo(i);assertThat(count("informe_backfill_snapshot")).isEqualTo(1);
        var corregido=backfill(List.of(fuente(propuesta,RAW))).ejecutar(propuesta.scopeKey());
        assertThat(corregido.orden()).contains(propuesta);assertThat(count("informe_backfill_snapshot")).isEqualTo(2);
        assertThat(jdbc.queryForObject("select fuente_raw from informe_backfill_snapshot where id=?",String.class,i.id())).isEqualTo(i.fuenteRaw());
        rechaza("delete from informe_backfill_snapshot where id=?",i.id());
        var original=propuesta;var distinto=orden(cliente,grupo,ids,producto,actividades,12345,RAW+" nuevo");propuesta=distinto;
        var rechazado=backfill(List.of(fuente(distinto,RAW+" nuevo"))).ejecutar(distinto.scopeKey());
        assertThat(rechazado.informe().causas()).contains(InformeBackfillSnapshot.Causa.PAYLOAD_CONTRADICTORIO);
        assertThat(repo.buscar(original.id(),cliente)).contains(original);assertThat(count("informe_backfill_snapshot")).isEqualTo(3);
    }
    @Test void T13_grupoTruncadoRawRechazadoSinNingunaRaizParcial() {
        jdbc.update("delete from compra where id=?",ids.get(1));var r=backfill(List.of(fuente(propuesta,RAW))).ejecutar(propuesta.scopeKey());
        assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);
        assertThat(r.informe().causas()).contains(InformeBackfillSnapshot.Causa.GRUPO_INCOMPLETO);vacio();
        assertThat(r.informe().conteoObservado()).isEqualTo(1);assertThat(r.informe().fuenteRaw()).contains(ids.getFirst().toString());
    }
    @Test void T13_legacyPuedeAgregarTardeGrupoSeReportaNoSeReescribeWinner() {
        freeze();insertar(UUID.randomUUID(),grupo,3,12345,"mxn",cliente);
        var r=backfill(List.of(fuente(propuesta,RAW))).ejecutar(propuesta.scopeKey());
        assertThat(r.informe().causas()).contains(InformeBackfillSnapshot.Causa.GRUPO_INCOMPLETO);
        assertThat(r.requierenRevision()).isEqualTo(3);assertThat(r.totalCongelado()).isEmpty();
        assertThat(repo.buscar(propuesta.id(),cliente)).contains(propuesta);assertThat(count("orden_venta")).isEqualTo(1);
        assertThat(count("informe_backfill_snapshot")).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from compra where orden_venta_id is null",Integer.class)).isEqualTo(1);
    }
    @Test void T13_concurrentBackfillConexionesIndependientesUnaOrdenYReporte() throws Exception {
        CyclicBarrier barrier=new CyclicBarrier(2);Set<Integer> pids=ConcurrentHashMap.newKeySet();
        var txm=new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource) {
            @Override protected void doBegin(Object tx,org.springframework.transaction.TransactionDefinition def) {
                super.doBegin(tx,def);pids.add(jdbc.queryForObject("select pg_backend_pid()",Integer.class));
                try{barrier.await(10,TimeUnit.SECONDS);}catch(Exception ex){throw new IllegalStateException(ex);}
            }
        };
        var r=new com.feelingpilates.pagos.ventas.infraestructura.OrdenSnapshotJdbcAdapter(jdbc,txm);
        var b=new BackfillOrdenSnapshot(origen(List.of(fuente(propuesta,RAW))),new CongelarOrdenSnapshot(r),informes);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<BackfillOrdenSnapshot.Resultado> job=() -> b.ejecutar(propuesta.scopeKey());
            var a=pool.submit(job);var c=pool.submit(job);var x=a.get(25,TimeUnit.SECONDS);var y=c.get(25,TimeUnit.SECONDS);
            assertThat(pids).hasSize(2);assertThat(x.orden()).contains(propuesta);assertThat(y.orden()).isEqualTo(x.orden());
            assertThat(x.informe()).isEqualTo(y.informe());assertThat(x.replay()).isNotEqualTo(y.replay());
            assertThat(count("orden_venta")).isEqualTo(1);assertThat(count("informe_backfill_snapshot")).isEqualTo(1);
            System.out.println("PN14_S2_T13 concurrent backfill PIDs="+pids+" report count=1 total=24690");
        } finally {pool.shutdownNow();assertThat(pool.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
    }
}
