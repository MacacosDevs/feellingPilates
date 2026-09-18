package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class OrdenSnapshotIdempotenciaTest extends PN14Slice2Fixtures.Postgres {
    @Test void T09_replayConservaUUIDHashInstanteConteosSinNuevosInformes() {
        var primero=freeze(); var segundo=freeze();
        assertThat(primero.replay()).isFalse(); assertThat(segundo.replay()).isTrue();assertThat(segundo.orden()).isEqualTo(primero.orden());
        assertThat(count("orden_venta")).isEqualTo(1);assertThat(count("compra_componente_snapshot")).isEqualTo(4);
        assertThat(count("informe_backfill_snapshot")).isEqualTo(1);assertThat(count("compra")).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.tables where table_schema='public' and table_name in ('pago','acreditacion')",Integer.class)).isZero();
    }
    @Test void T09_contradiccionFallaCerradoWinnerIntactoYReporteSeparadoPreservaRaw() {
        freeze(); var original=propuesta;
        var distinta=orden(cliente,grupo,ids,producto,actividades,12345,RAW+"\ncorrección contradictoria");
        var e=evidencia(distinta);
        assertThatThrownBy(() -> repo.congelar(distinta,e)).isInstanceOf(RepositorioOrdenSnapshot.ConflictoSnapshot.class);
        assertThat(repo.buscar(original.id(),cliente)).contains(original); assertThat(count("informe_backfill_snapshot")).isEqualTo(1);
        propuesta=distinta; var r=backfill(List.of(fuente(distinta,RAW+"\ncorrección contradictoria"))).ejecutar(distinta.scopeKey());
        assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);
        assertThat(r.informe().causas()).contains(InformeBackfillSnapshot.Causa.PAYLOAD_CONTRADICTORIO);
        assertThat(r.informe().fuenteRaw()).contains("corrección contradictoria");assertThat(count("informe_backfill_snapshot")).isEqualTo(2);
        assertThat(repo.buscar(original.id(),cliente)).contains(original);
        var otra=backfill(List.of(fuente(distinta,RAW+"\ncorrección contradictoria"))).ejecutar(distinta.scopeKey());
        assertThat(otra.informe()).isEqualTo(r.informe());assertThat(count("informe_backfill_snapshot")).isEqualTo(2);
    }
    @Test void T09_lecturaExigeClienteCanonico() {
        freeze();assertThat(repo.buscar(propuesta.id(),otroCliente)).isEmpty(); assertThat(repo.buscar(propuesta.id(),cliente)).contains(propuesta);
    }
}
