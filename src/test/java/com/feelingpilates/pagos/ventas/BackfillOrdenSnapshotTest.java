package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class BackfillOrdenSnapshotTest {
    OrdenVenta o=orden(UUID.randomUUID(),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID()),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID()),12345,RAW);
    List<FuenteHistoricaCompra.Fila> filas() {
        return o.compras().stream().map(c -> fila(c.id(),c.clienteId(),UUID.fromString(o.scopeKey().substring(13)),c.numeroLinea(),12345,"mxn")).toList();
    }
    FuenteHistoricaCompra.Fila fila(UUID id,UUID cliente,UUID grupo,Integer n,long monto,String moneda) {
        String raw="exact legacy dummy\n"+id+"|"+cliente+"|"+grupo+"|"+n+"|"+monto+"|"+moneda;
        return new FuenteHistoricaCompra.Fila(id,cliente,grupo,n,monto,moneda,raw,ContenidoSnapshotCanonico.sha256(raw),
                FuenteHistoricaCompra.Fila.hashObservacion(id,cliente,grupo,n,monto,moneda));
    }
    static class Memoria implements RepositorioOrdenSnapshot,InformeBackfillSnapshot.Puerto {
        Map<UUID,OrdenVenta> ordenes=new HashMap<>(); Map<UUID,InformeBackfillSnapshot> informes=new HashMap<>();
        int invocacionesFreeze;
        public CongelarOrdenSnapshot.Resultado congelar(OrdenVenta o,EvidenciaScope e) {
            invocacionesFreeze++;
            OrdenVenta anterior=ordenes.putIfAbsent(o.id(),o);
            if(anterior!=null && !anterior.payloadHash().equals(o.payloadHash())) throw new ConflictoSnapshot("payload");
            if(anterior==null) registrar(e.informe()); return new CongelarOrdenSnapshot.Resultado(anterior==null?o:anterior,anterior!=null);
        }
        public Optional<OrdenVenta> buscar(UUID id,UUID cliente) {return Optional.ofNullable(ordenes.get(id)).filter(o -> o.clienteId().equals(cliente));}
        public InformeBackfillSnapshot registrar(InformeBackfillSnapshot i) { informes.putIfAbsent(i.id(),i);return informes.get(i.id()); }
    }
    BackfillOrdenSnapshot.Resultado ejecutar(List<FuenteHistoricaCompra.Fila> filas,List<FuenteHistoricaCompra.Fuente> fuentes,Memoria m) {
        var g=new FuenteHistoricaCompra.Grupo(o.scopeKey(),filas,fuentes,S);
        return new BackfillOrdenSnapshot(scope -> g,new CongelarOrdenSnapshot(m),m).ejecutar(o.scopeKey());
    }
    @Test void T11_trustedContemporaneo12345PolicyYComposicionSinCatalogo() {
        Memoria m=new Memoria();var r=ejecutar(filas(),List.of(fuente(o,RAW)),m);
        assertThat(r.orden()).contains(o); assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.CONGELADA);
        assertThat(r.orden().orElseThrow().compras().getFirst().precioVenta().unidadesMinimas()).isEqualTo(12345);
        assertThat(r.orden().orElseThrow().compras().getFirst().componentes()).extracting(CompraComponenteSnapshot::cantidad).containsExactly(4,2);
        assertThat(m.ordenes).hasSize(1);
    }
    @Test void T11_StripeReceiptFinancieroSoloNoAutorizaContrato() {
        String raw="Stripe receipt 12345mxn sin términos";
        var receipt=new FuenteHistoricaCompra.Fuente(raw,ContenidoSnapshotCanonico.sha256(raw),null,null,S,null,null,false,null,List.of(),null,List.of(),List.of());
        Memoria m=new Memoria();var r=ejecutar(filas(),List.of(receipt),m);
        assertThat(r.orden()).isEmpty();assertThat(m.ordenes).isEmpty(); assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);
        assertThat(r.informe().fuenteRaw()).contains(raw); assertThat(r.informe().faltantes()).contains("membership","total.monedaIso");
        assertThat(r.informe().faltantes()).anyMatch(p -> p.endsWith(".politica")).anyMatch(p -> p.endsWith(".componentes"));
        assertThat(r.informe().faltantes()).anyMatch(p -> p.endsWith(".politica.recuperacion.politicaVersion"))
            .anyMatch(p -> p.endsWith(".politica.reembolso.politicaId")).anyMatch(p -> p.endsWith(".componentes[].cantidad"));
        assertThat(r.informe().fuenteRawHash()).isEqualTo(ContenidoSnapshotCanonico.sha256(r.informe().fuenteRaw()));
    }
    @Test void T12_sinFuenteConservaTodasFilasRawFaltantesYRechazados() {
        Memoria m=new Memoria();var r=ejecutar(filas(),List.of(),m);
        assertThat(r.informe().compraIds()).containsExactlyInAnyOrderElementsOf(o.compras().stream().map(Compra::id).toList());
        filas().forEach(f -> {
            assertThat(r.informe().fuenteRaw()).contains(f.raw());
            assertThat(r.informe().fuentesCanonicas()).contains(f.rawHash(),f.observacionHash(),f.id().toString());
        });
        assertThat(r.informe().conteoObservado()).isEqualTo(2);assertThat(r.informe().causas()).contains(InformeBackfillSnapshot.Causa.FUENTE_AUSENTE);
        assertThat(m.ordenes).isEmpty(); assertThat(m.informes).hasSize(1);
    }
    @Test void T12_grupoTruncadoNumeroGapClienteMonedaYOverflowRequierenRevision() {
        var f=fuente(o,RAW); var originales=filas(); var a=originales.getFirst();
        List<List<FuenteHistoricaCompra.Fila>> variantes=List.of(List.of(a),
            List.of(a,fila(originales.get(1).id(),a.clienteId(),a.grupoId(),3,12345,"mxn")),
            List.of(a,fila(originales.get(1).id(),UUID.randomUUID(),a.grupoId(),2,12345,"mxn")),
            List.of(a,fila(originales.get(1).id(),a.clienteId(),a.grupoId(),2,12345,"usd")),
            List.of(fila(a.id(),a.clienteId(),a.grupoId(),1,Long.MAX_VALUE,"mxn"),fila(originales.get(1).id(),a.clienteId(),a.grupoId(),2,Long.MAX_VALUE,"mxn")));
        List<InformeBackfillSnapshot.Causa> causas=List.of(InformeBackfillSnapshot.Causa.GRUPO_INCOMPLETO,InformeBackfillSnapshot.Causa.NUMERACION_INVALIDA,
            InformeBackfillSnapshot.Causa.CLIENTE_INCONSISTENTE,InformeBackfillSnapshot.Causa.MONEDA_INCONSISTENTE,InformeBackfillSnapshot.Causa.IMPORTE_INVALIDO);
        for(int i=0;i<variantes.size();i++) {Memoria m=new Memoria();var r=ejecutar(variantes.get(i),List.of(f),m);
            assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);assertThat(r.informe().causas()).contains(causas.get(i));assertThat(m.ordenes).isEmpty();}
    }
    @Test void T12_fuentesTrustedContradictoriasSinLatestWins() {
        var distinta=orden(o.clienteId(),UUID.fromString(o.scopeKey().substring(13)),o.compras().stream().map(Compra::id).toList(),o.compras().getFirst().productoFuenteId(),
            o.compras().getFirst().componentes().stream().map(CompraComponenteSnapshot::actividadId).toList(),12345,RAW+" discrepancia");
        for(List<FuenteHistoricaCompra.Fuente> fuentes:List.of(List.of(fuente(o,RAW),fuente(distinta,RAW+" discrepancia")),List.of(fuente(distinta,RAW+" discrepancia"),fuente(o,RAW)))) {
            Memoria m=new Memoria();var r=ejecutar(filas(),fuentes,m);assertThat(r.informe().causas()).contains(InformeBackfillSnapshot.Causa.FUENTE_CONTRADICTORIA);assertThat(m.ordenes).isEmpty();
        }
    }
    @Test void T12_cantidadDesconocidaYSobreAmbiguoSeConservanSinGuessing() {
        var f=new FuenteHistoricaCompra.Fuente(RAW,ContenidoSnapshotCanonico.sha256(RAW),"ticket-dummy",o.clienteId(),S,
            ProvenienciaSnapshot.Origen.CONTRATO_CONTEMPORANEO_VERIFICADO,"PN14_S2_1",true,null,List.of(),null,
            List.of("compras[1].componentes[1].cantidad"),List.of(InformeBackfillSnapshot.Causa.FUENTE_CONTRADICTORIA));
        Memoria m=new Memoria();var r=ejecutar(filas(),List.of(f),m);
        assertThat(r.informe().faltantes()).contains("compras[1].componentes[1].cantidad"); assertThat(r.informe().fuenteRaw()).contains(RAW);assertThat(m.ordenes).isEmpty();
    }
    @Test void T12_contratoEquivalenteNoOcultaMetadataTrustedIncompatibleEnNingunOrden() {
        var valida=fuente(o,RAW); var duplicada=fuente(o,RAW);
        String otroRaw=RAW+"\nsegunda evidencia exacta";
        var variantes=List.of(
            new FuenteHistoricaCompra.Fuente(RAW,valida.rawHash(),valida.referencia(),UUID.randomUUID(),S,valida.origen(),valida.reglaVersion(),true,o,valida.membership(),o.total(),List.of(),List.of()),
            new FuenteHistoricaCompra.Fuente(RAW,valida.rawHash(),"otra-referencia",valida.actorId(),S,valida.origen(),valida.reglaVersion(),true,o,valida.membership(),o.total(),List.of(),List.of()),
            new FuenteHistoricaCompra.Fuente(otroRaw,ContenidoSnapshotCanonico.sha256(otroRaw),valida.referencia(),valida.actorId(),S,valida.origen(),valida.reglaVersion(),true,o,valida.membership(),o.total(),List.of(),List.of()),
            new FuenteHistoricaCompra.Fuente(RAW,valida.rawHash(),valida.referencia(),valida.actorId(),S.plusSeconds(1),valida.origen(),valida.reglaVersion(),true,o,valida.membership(),o.total(),List.of(),List.of()),
            new FuenteHistoricaCompra.Fuente(RAW,valida.rawHash(),valida.referencia(),valida.actorId(),S,ProvenienciaSnapshot.Origen.ARCHIVO_VERSIONADO_VERIFICADO,valida.reglaVersion(),true,o,valida.membership(),o.total(),List.of(),List.of()));
        for (var incompatible:variantes) {
            assertThat(incompatible.contrato().payloadHash()).isEqualTo(valida.contrato().payloadHash());
            assertThat(incompatible.membership()).isEqualTo(valida.membership());
            assertThat(incompatible.total()).isEqualTo(valida.total());
            var ordenes=List.of(List.of(valida,incompatible),List.of(incompatible,valida),
                List.of(valida,duplicada,incompatible),List.of(valida,incompatible,duplicada),
                List.of(duplicada,valida,incompatible),List.of(duplicada,incompatible,valida),
                List.of(incompatible,valida,duplicada),List.of(incompatible,duplicada,valida));
            for (var fuentes:ordenes) {
                Memoria m=new Memoria(); var r=ejecutar(filas(),fuentes,m);
                assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);
                assertThat(r.informe().causas()).containsExactly(InformeBackfillSnapshot.Causa.FUENTE_CONTRADICTORIA);
                assertThat(r.informe().faltantes()).isEmpty();
                assertThat(r.orden()).isEmpty(); assertThat(r.replay()).isFalse();
                assertThat(r.congeladas()).isZero(); assertThat(r.requierenRevision()).isEqualTo(2);
                assertThat(m.invocacionesFreeze).isZero(); assertThat(m.ordenes).isEmpty(); assertThat(m.informes).hasSize(1);
                fuentes.forEach(f -> {
                    assertThat(r.informe().fuenteRaw()).contains(f.raw());
                    assertThat(r.informe().fuentesCanonicas()).contains(f.rawHash(),f.referencia(),f.actorId().toString());
                });
                filas().forEach(f -> assertThat(r.informe().fuenteRaw()).contains(f.raw()));
                assertThat(r.informe().fuenteRaw()).isEqualTo(ContenidoSnapshotCanonico.pares(Map.of(
                    "fuentes",fuentes.stream().map(FuenteHistoricaCompra.Fuente::raw).toList(),
                    "filas",filas().stream().map(FuenteHistoricaCompra.Fila::raw).toList())));
                assertThat(r.informe().fuenteRawHash()).isEqualTo(ContenidoSnapshotCanonico.sha256(r.informe().fuenteRaw()));
            }
        }
    }
    @Test void T12_duplicadosTrustedCompatiblesConservanElegibilidadYReplay() {
        var a=fuente(o,RAW); var b=fuente(o,RAW); Memoria m=new Memoria();
        var primero=ejecutar(filas(),List.of(a,b),m); var replay=ejecutar(filas(),List.of(b,a),m);
        assertThat(primero.orden()).contains(o); assertThat(primero.replay()).isFalse();
        assertThat(replay.orden()).contains(o); assertThat(replay.replay()).isTrue();
        assertThat(primero.informe().causas()).isEmpty(); assertThat(replay.informe().causas()).isEmpty();
        assertThat(primero.congeladas()).isEqualTo(2); assertThat(replay.replays()).isEqualTo(2);
        assertThat(m.invocacionesFreeze).isEqualTo(2); assertThat(m.ordenes).hasSize(1); assertThat(m.informes).hasSize(1);
    }
    @Test void T12_fuentePorCampoIncompatibleNoSeCongelaAunqueMetadataPrincipalCoincida() {
        for (boolean referenciaIncompatible:List.of(true,false)) {
            var compras=o.compras().stream().map(c -> {
                var p=c.proveniencia(); var campos=new ArrayList<>(p.campos()); var campo=campos.getFirst();
                campos.set(0,new ProvenienciaSnapshot.CampoFuente(campo.fieldPath(),
                    referenciaIncompatible?"otra-referencia":campo.referencia(),
                    referenciaIncompatible?campo.fuenteHash():ContenidoSnapshotCanonico.sha256("otra evidencia")));
                var contradictoria=new ProvenienciaSnapshot(p.origen(),p.referencia(),p.actorId(),p.evidenciaEn(),
                    p.fuenteRaw(),p.fuenteRawHash(),p.reglaVersion(),campos);
                return new Compra(c.id(),c.ordenId(),c.clienteId(),c.numeroLinea(),c.productoFuenteId(),c.nombreProducto(),
                    c.tipoProducto(),c.precioVenta(),c.politica(),c.componentes(),contradictoria,c.congeladoEn(),null);
            }).toList();
            var contrato=new OrdenVenta(o.id(),o.clienteId(),o.scopeKey(),o.total(),compras,o.congeladoEn(),null);
            var a=fuente(contrato,RAW); var b=fuente(contrato,RAW);
            for (var fuentes:List.of(List.of(a,b),List.of(b,a))) {
                Memoria m=new Memoria(); var r=ejecutar(filas(),fuentes,m);
                assertThat(r.informe().estado()).isEqualTo(InformeBackfillSnapshot.Estado.REQUIERE_REVISION);
                assertThat(r.informe().causas()).containsExactly(InformeBackfillSnapshot.Causa.FUENTE_CONTRADICTORIA);
                assertThat(r.orden()).isEmpty(); assertThat(m.invocacionesFreeze).isZero(); assertThat(m.ordenes).isEmpty();
            }
        }
    }
}
