package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class PoliticaSnapshotCanonicoTest {
    @Test void T04_politicaCompletaReferenciasVersionesSinDefaults() {
        var p=politica(); assertThat(p.vigencia().unidad()).isEqualTo(PoliticaComercialSnapshot.Unidad.MESES);
        assertThat(p.recuperacion().unidad()).isEqualTo(PoliticaComercialSnapshot.Unidad.DIAS);
        assertThat(p.reservaCancelacion().anticipacionSegundos()).isEqualTo(86400);
        assertThat(p.terminos().igualdadCutoffValida()).isTrue(); assertThat(p.reembolso().ventanaAdicional()).isFalse();
        assertThatThrownBy(() -> new PoliticaComercialSnapshot(p.esquema()," ",p.version(),p.zonaNegocio(),p.vigencia(),p.reservaCancelacion(),p.recuperacion(),p.reembolso(),2,p.terminos())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PoliticaComercialSnapshot(p.esquema(),p.id(),p.version(),p.zonaNegocio(),null,p.reservaCancelacion(),p.recuperacion(),p.reembolso(),2,p.terminos())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PoliticaComercialSnapshot.Reembolso(p.reembolso().alcance(),true,"a","v")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PoliticaComercialSnapshot.ReservaCancelacion(-1,0,0)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void T04_utf8TiposNulosLFLiteralesYOrdenExacto() {
        Map<String,Object> a=new HashMap<>(); a.put("á","🌿\n~NULL"); a.put("z",null); a.put("a","~NULL");
        String c=ContenidoSnapshotCanonico.pares(a);
        assertThat(c).startsWith("1:a12:STRING:~NULL\n1:z10:NULL:~NULL\n2:á");
        assertThat(c).contains("17:STRING:🌿\n~NULL\n");
        assertThat(ContenidoSnapshotCanonico.pares(new TreeMap<>(a))).isEqualTo(c);
        assertThatThrownBy(() -> ContenidoSnapshotCanonico.utf8("\ud800")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void T04_utc6SinRedondeoHashYSoloUnCampoCambiaHash() {
        assertThat(ContenidoSnapshotCanonico.instante(S)).isEqualTo("2026-09-01T12:34:56.123456Z");
        assertThatThrownBy(() -> ContenidoSnapshotCanonico.instante(S.plusNanos(1))).isInstanceOf(IllegalArgumentException.class);
        var p=politica(); var canon=ContenidoSnapshotCanonico.politica(p);
        assertThat(ContenidoSnapshotCanonico.leerRecord(canon,PoliticaComercialSnapshot.class)).isEqualTo(p);
        var nuevo=new PoliticaComercialSnapshot(p.esquema(),p.id(),"v8",p.zonaNegocio(),p.vigencia(),p.reservaCancelacion(),p.recuperacion(),p.reembolso(),2,p.terminos());
        assertThat(ContenidoSnapshotCanonico.sha256(canon)).isNotEqualTo(ContenidoSnapshotCanonico.sha256(ContenidoSnapshotCanonico.politica(nuevo)));
        assertThatThrownBy(() -> ContenidoSnapshotCanonico.leerRecord(canon.replace("INT:2","LONG:2"),PoliticaComercialSnapshot.class)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void T04_provenancePorCampoRawHashYDuplicadosObligatorios() {
        String h=ContenidoSnapshotCanonico.sha256(RAW); var campo=new ProvenienciaSnapshot.CampoFuente("id","ticket",h);
        assertThatThrownBy(() -> new ProvenienciaSnapshot(ProvenienciaSnapshot.Origen.CONTRATO_CONTEMPORANEO_VERIFICADO,"ticket",UUID.randomUUID(),S,RAW,h,"PN14_S2_1",List.of(campo,campo))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProvenienciaSnapshot(ProvenienciaSnapshot.Origen.CONTRATO_CONTEMPORANEO_VERIFICADO,"ticket",UUID.randomUUID(),S,RAW,"0".repeat(64),"PN14_S2_1",List.of(campo))).isInstanceOf(IllegalArgumentException.class);
        var p=new ProvenienciaSnapshot(ProvenienciaSnapshot.Origen.CONTRATO_CONTEMPORANEO_VERIFICADO,"ticket",UUID.randomUUID(),S,RAW,h,"PN14_S2_1",List.of(campo));
        assertThatThrownBy(() -> p.exigir(List.of("politica.version"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("politica.version");
    }
    @Test void T04_identidadesYCanonOrderRoundtripEstables() {
        var o=orden(UUID.randomUUID(),UUID.randomUUID(),List.of(UUID.randomUUID()),UUID.randomUUID(),List.of(UUID.randomUUID()),12345,RAW);
        assertThat(ContenidoSnapshotCanonico.leerOrden(ContenidoSnapshotCanonico.orden(o),o.payloadHash())).isEqualTo(o);
        assertThat(ContenidoSnapshotCanonico.ordenId(o.scopeKey())).isEqualTo(o.id());
        assertThat(o.id().version()).isEqualTo(3); assertThat(o.id().variant()).isEqualTo(2);
    }
}
