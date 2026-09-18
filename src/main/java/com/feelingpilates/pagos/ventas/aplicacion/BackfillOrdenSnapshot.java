package com.feelingpilates.pagos.ventas.aplicacion;

import com.feelingpilates.pagos.ventas.dominio.*;
import java.math.BigInteger;
import java.util.*;
import static com.feelingpilates.pagos.ventas.aplicacion.InformeBackfillSnapshot.*;

/** Invocación explícita; ninguna consulta al catálogo, fallback o ejecución al arrancar. */
public final class BackfillOrdenSnapshot {
    private final FuenteHistoricaCompra fuente;
    private final CongelarOrdenSnapshot congelar;
    private final Puerto informes;
    public BackfillOrdenSnapshot(FuenteHistoricaCompra fuente, CongelarOrdenSnapshot congelar, Puerto informes) {
        this.fuente=Objects.requireNonNull(fuente); this.congelar=Objects.requireNonNull(congelar); this.informes=Objects.requireNonNull(informes);
    }
    public record Resultado(InformeBackfillSnapshot informe, Optional<OrdenVenta> orden, boolean replay) {
        public Resultado { Objects.requireNonNull(informe); Objects.requireNonNull(orden); }
        public int congeladas() { return orden.isPresent() && !replay?orden.get().compras().size():0; }
        public int replays() { return replay?orden.orElseThrow().compras().size():0; }
        public int requierenRevision() { return informe.estado()==Estado.REQUIERE_REVISION?informe.conteoObservado():0; }
        public Optional<ImporteMonetario> totalCongelado() { return orden.map(OrdenVenta::total); }
    }
    public Resultado ejecutar(String scope) {
        var g=fuente.obtener(scope); Set<Causa> causas=new HashSet<>(); Set<String> faltantes=new HashSet<>();
        List<FuenteHistoricaCompra.Fuente> trusted=g.fuentes().stream().filter(FuenteHistoricaCompra.Fuente::verificada).toList();
        g.fuentes().forEach(f -> { faltantes.addAll(f.faltantes()); causas.addAll(f.causas()); });
        var f=trusted.stream().filter(x -> x.contrato()!=null).findFirst().orElse(null);
        OrdenVenta o=f==null?null:f.contrato();
        if (f==null || o==null) {
            causas.add(Causa.FUENTE_AUSENTE);
            faltantes.addAll(List.of("scopeKey","membership","numeroLineas","total.unidadesMinimas","total.monedaIso"));
            g.filas().forEach(r -> { faltantes.addAll(List.of("compras["+r.id()+"].productoFuenteId",
                    "compras["+r.id()+"].nombreProducto","compras["+r.id()+"].tipoProducto",
                    "compras["+r.id()+"].precioVenta","compras["+r.id()+"].politica","compras["+r.id()+"].componentes"));
                faltantesTipo("compras["+r.id()+"].politica",PoliticaComercialSnapshot.class,faltantes);
                faltantesTipo("compras["+r.id()+"].precioVenta",ImporteMonetario.class,faltantes);
                faltantesTipo("compras["+r.id()+"].componentes[]",CompraComponenteSnapshot.class,faltantes);
            });
        }
        if (trusted.stream().anyMatch(x -> x.contrato()==null || o==null || !x.contrato().payloadHash().equals(o.payloadHash())
                || !x.membership().equals(f.membership()) || !Objects.equals(x.total(),f.total()))) causas.add(Causa.FUENTE_CONTRADICTORIA);
        BigInteger suma=g.filas().stream().map(r -> BigInteger.valueOf(r.montoUnidadesMinimas())).reduce(BigInteger.ZERO,BigInteger::add);
        if (g.filas().isEmpty() || g.filas().stream().map(FuenteHistoricaCompra.Fila::id).distinct().count()!=g.filas().size()) causas.add(Causa.GRUPO_INCOMPLETO);
        if (g.filas().stream().map(FuenteHistoricaCompra.Fila::clienteId).distinct().count()>1) causas.add(Causa.CLIENTE_INCONSISTENTE);
        if (g.filas().stream().map(r -> r.monedaRaw().toUpperCase(Locale.ROOT)).distinct().count()>1) causas.add(Causa.MONEDA_INCONSISTENTE);
        if (suma.signum()<0 || suma.compareTo(BigInteger.valueOf(Long.MAX_VALUE))>0 || g.filas().stream().anyMatch(r -> r.montoUnidadesMinimas()<0)) causas.add(Causa.IMPORTE_INVALIDO);
        // Ningún sobre equivalente puede ocultar una contradicción de provenance de otro.
        for (var x:trusted) {
            if (x.contrato()!=null) validar(g,x,x.contrato(),causas);
        }
        InformeBackfillSnapshot i=informe(g,causas.isEmpty()?Estado.CONGELADA:Estado.REQUIERE_REVISION,faltantes,causas,suma);
        if (!faltantes.isEmpty() && causas.isEmpty()) {
            causas.add(Causa.FUENTE_AUSENTE); i=informe(g,Estado.REQUIERE_REVISION,faltantes,causas,suma);
        }
        if (!causas.isEmpty() || !faltantes.isEmpty()) return new Resultado(informes.registrar(i),Optional.empty(),false);
        try {
            var e=new RepositorioOrdenSnapshot.EvidenciaScope(g.filas(),f.membership(),f.total(),i);
            var r=congelar.ejecutar(new CongelarOrdenSnapshot.Entrada(o,e));
            return new Resultado(r.informe()==null?i:r.informe(),Optional.of(r.orden()),r.replay());
        } catch (RepositorioOrdenSnapshot.ConflictoSnapshot ex) {
            // El repositorio ya hizo rollback. El Puerto registra en otra transacción.
            causas.add(Causa.PAYLOAD_CONTRADICTORIO);
            return new Resultado(informes.registrar(informe(g,Estado.REQUIERE_REVISION,faltantes,causas,suma)),Optional.empty(),false);
        }
    }
    private static void faltantesTipo(String prefijo,Class<?> tipo,Set<String> paths) {
        for(var campo:tipo.getRecordComponents()) {
            if(campo.getType().isRecord())faltantesTipo(prefijo+"."+campo.getName(),campo.getType(),paths);
            else paths.add(prefijo+"."+campo.getName());
        }
    }
    private static void validar(FuenteHistoricaCompra.Grupo g,FuenteHistoricaCompra.Fuente f,OrdenVenta o,Set<Causa> c) {
        if (!o.scopeKey().equals(g.scopeKey()) || f.total()==null || !f.total().equals(o.total())
                || !new HashSet<>(f.membership()).equals(new HashSet<>(g.filas().stream().map(FuenteHistoricaCompra.Fila::id).toList()))
                || f.membership().size()!=g.filas().size() || o.compras().size()!=g.filas().size()) c.add(Causa.GRUPO_INCOMPLETO);
        for (var r:g.filas()) {
            var compra=o.compras().stream().filter(x -> x.id().equals(r.id())).findFirst().orElse(null);
            if (compra==null) { c.add(Causa.GRUPO_INCOMPLETO); continue; }
            if (!compra.clienteId().equals(r.clienteId())) c.add(Causa.CLIENTE_INCONSISTENTE);
            if (!compra.precioVenta().monedaIso().equals(r.monedaRaw().toUpperCase(Locale.ROOT))) c.add(Causa.MONEDA_INCONSISTENTE);
            if (compra.precioVenta().unidadesMinimas()!=r.montoUnidadesMinimas()) c.add(Causa.IMPORTE_INVALIDO);
            int numero=r.numeroItem()==null && r.grupoId()==null?1:r.numeroItem()==null?0:r.numeroItem();
            if (numero!=compra.numeroLinea()) c.add(Causa.NUMERACION_INVALIDA);
            String scope=r.grupoId()==null?"LEGACY_COMPRA:"+r.id():"LEGACY_GRUPO:"+r.grupoId();
            if (!scope.equals(g.scopeKey())) c.add(Causa.GRUPO_INCOMPLETO);
        }
        for (var compra:o.compras()) {
            var p=compra.proveniencia();
            if (!p.fuenteRaw().equals(f.raw()) || !p.fuenteRawHash().equals(f.rawHash()) || !p.referencia().equals(f.referencia())
                    || !p.actorId().equals(f.actorId()) || !p.evidenciaEn().equals(f.evidenciaEn()) || p.origen()!=f.origen())
                c.add(Causa.FUENTE_CONTRADICTORIA);
            if (!p.reglaVersion().equals(f.reglaVersion()) || p.campos().stream().anyMatch(campo ->
                    !campo.referencia().equals(f.referencia()) || !campo.fuenteHash().equals(f.rawHash())))
                c.add(Causa.FUENTE_CONTRADICTORIA);
        }
    }
    public static InformeBackfillSnapshot informe(FuenteHistoricaCompra.Grupo g,Estado estado,Collection<String> faltantes,Collection<Causa> causas,BigInteger suma) {
        String raw=ContenidoSnapshotCanonico.pares(Map.of("fuentes",g.fuentes().stream().map(FuenteHistoricaCompra.Fuente::raw).toList(),
                "filas",g.filas().stream().map(FuenteHistoricaCompra.Fila::raw).toList()));
        String fuentes=ContenidoSnapshotCanonico.pares(Map.of("fuentes",g.fuentes(),"filas",g.filas()));
        String h=ContenidoSnapshotCanonico.sha256(fuentes+raw);
        return new InformeBackfillSnapshot(identidad(g.scopeKey(),h),g.scopeKey(),h,estado,raw,ContenidoSnapshotCanonico.sha256(raw),fuentes,
                List.copyOf(faltantes),List.copyOf(causas),g.filas().stream().map(FuenteHistoricaCompra.Fila::id).toList(),g.filas().size(),
                suma.abs().toString().length()>20?null:suma,g.filas().isEmpty()?null:g.filas().getFirst().monedaRaw(),g.evidenciaEn(),"PN14_S2_1");
    }
}
