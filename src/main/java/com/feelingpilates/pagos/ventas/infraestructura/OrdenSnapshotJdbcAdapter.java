package com.feelingpilates.pagos.ventas.infraestructura;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.util.*;

/** Único attach del bundle; se instancia manualmente. Nunca inserta una Compra legacy. */
public final class OrdenSnapshotJdbcAdapter implements RepositorioOrdenSnapshot {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaccion;
    public OrdenSnapshotJdbcAdapter(JdbcTemplate jdbc,PlatformTransactionManager manager) {
        this.jdbc=Objects.requireNonNull(jdbc); transaccion=new TransactionTemplate(Objects.requireNonNull(manager));
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); transaccion.setTimeout(15);
    }
    @Override public Optional<OrdenVenta> buscar(UUID orden,UUID cliente) {
        return jdbc.query("select payload_canonico,payload_hash from public.orden_venta where id=? and cliente_id=? and estado_fundacion='CONGELADA'",
                (r,n) -> ContenidoSnapshotCanonico.leerOrden(r.getString(1),r.getString(2)),orden,cliente).stream().findFirst();
    }
    @Override public CongelarOrdenSnapshot.Resultado congelar(OrdenVenta o,EvidenciaScope e) {
        return transaccion.execute(s -> {
            jdbc.execute("set local lock_timeout='5s'"); jdbc.execute("set local statement_timeout='10s'");
            jdbc.queryForObject("select pg_advisory_xact_lock(?) is null",Boolean.class,ContenidoSnapshotCanonico.advisory(o.scopeKey()));
            jdbc.execute("lock table public.compra in share row exclusive mode");
            // Ninguna lectura de membership precede el lock de tabla.
            var ganadores=jdbc.query("select payload_canonico,payload_hash from public.orden_venta where scope_key=? for update",
                    (r,n) -> ContenidoSnapshotCanonico.leerOrden(r.getString(1),r.getString(2)),o.scopeKey());
            var filas=FuenteHistoricaCompraJdbcAdapter.filas(jdbc,o.scopeKey(),true);
            validar(o,e,filas,!ganadores.isEmpty());
            if(!ganadores.isEmpty()) {
                OrdenVenta ganador=ganadores.getFirst();
                if(!ganador.id().equals(o.id()) || !ganador.payloadHash().equals(o.payloadHash())
                        || !ContenidoSnapshotCanonico.orden(ganador).equals(ContenidoSnapshotCanonico.orden(o)))
                    throw new ConflictoSnapshot("PAYLOAD_CONTRADICTORIO");
                return new CongelarOrdenSnapshot.Resultado(ganador,true,InformeBackfillJdbcAdapter.congelado(jdbc,o.scopeKey()));
            }
            jdbc.update("""
                insert into public.orden_venta(id,cliente_id,scope_key,moneda_iso,total_unidades_minimas,numero_lineas,
                estado_fundacion,congelado_en,payload_canonico,payload_hash,procedencia_canonica)
                values (?,?,?,?,?,?,'PREPARANDO',?,?,?,?)
                """,o.id(),o.clienteId(),o.scopeKey(),o.total().monedaIso(),o.total().unidadesMinimas(),o.compras().size(),
                Timestamp.from(o.congeladoEn()),ContenidoSnapshotCanonico.orden(o),o.payloadHash(),
                ContenidoSnapshotCanonico.pares(Map.of("compras",o.compras().stream().map(Compra::proveniencia).toList())));
            for(Compra c:o.compras()) adjuntar(c);
            jdbc.update("update public.orden_venta set estado_fundacion='CONGELADA' where id=?",o.id());
            // Validación antes del informe; constraints diferidas repiten la defensa al commit real.
            jdbc.queryForObject("select public.pn14_s2_validar_sello(?) is null",Boolean.class,o.id());
            var informe=InformeBackfillJdbcAdapter.insertarEnTransaccion(jdbc,e.informe());
            if(informe.estado()!=InformeBackfillSnapshot.Estado.CONGELADA) throw new ConflictoSnapshot("Informe previo rechazado bajo mismo source hash");
            return new CongelarOrdenSnapshot.Resultado(o,false,informe);
        });
    }
    private static void validar(OrdenVenta o,EvidenciaScope e,List<FuenteHistoricaCompra.Fila> filas,boolean replay) {
        Set<UUID> actual=new HashSet<>(filas.stream().map(FuenteHistoricaCompra.Fila::id).toList());
        if(actual.size()!=o.compras().size() || !actual.equals(new HashSet<>(e.membership()))
                || !e.totalAutoritativo().equals(o.total())) throw new ConflictoSnapshot("GRUPO_INCOMPLETO");
        Map<UUID,FuenteHistoricaCompra.Fila> anteriores=new HashMap<>(); e.filas().forEach(r -> anteriores.put(r.id(),r));
        for(var r:filas) {
            var c=o.compras().stream().filter(x -> x.id().equals(r.id())).findFirst().orElseThrow();
            var antes=anteriores.get(r.id());
            int numero=r.numeroItem()==null && r.grupoId()==null?1:r.numeroItem()==null?0:r.numeroItem();
            if(antes==null || !r.clienteId().equals(c.clienteId()) || numero!=c.numeroLinea()
                    || !r.monedaRaw().toUpperCase(Locale.ROOT).equals(c.precioVenta().monedaIso())
                    || !replay && (!r.observacionHash().equals(antes.observacionHash()) || r.montoUnidadesMinimas()!=c.precioVenta().unidadesMinimas()))
                throw new ConflictoSnapshot("Membership/importe/hash cambiado bajo lock");
        }
    }
    private void adjuntar(Compra c) {
        var p=c.politica(); String pc=ContenidoSnapshotCanonico.politica(p);
        int actualizado=jdbc.update("""
            update public.compra set orden_venta_id=?,cliente_snapshot_id=?,numero_linea=?,producto_fuente_id=?,
            nombre_producto_snapshot=?,tipo_producto_snapshot=?,precio_venta_unidades_minimas=?,moneda_snapshot_iso=?,
            congelado_en=?,contrato_canonico=?,contrato_hash=?,politica_canonica=?,politica_hash=?,procedencia_canonica=?,
            politica_esquema=?,politica_id=?,politica_version=?,zona_negocio=?,vigencia_unidad=?,vigencia_cantidad=?,
            extension_alcance=?,reserva_limite_post_vencimiento_dias=?,cancelacion_anticipacion_segundos=?,cancelacion_cuota_mensual=?,
            recuperacion_unidad=?,recuperacion_cantidad=?,recuperacion_politica_id=?,recuperacion_politica_version=?,
            reembolso_alcance=?,reembolso_ventana_adicional=?,reembolso_politica_id=?,reembolso_politica_version=?,unidad_monetaria_exponente=?
            where id=? and orden_venta_id is null
            """,c.ordenId(),c.clienteId(),c.numeroLinea(),c.productoFuenteId(),c.nombreProducto(),c.tipoProducto().name(),
            c.precioVenta().unidadesMinimas(),c.precioVenta().monedaIso(),Timestamp.from(c.congeladoEn()),ContenidoSnapshotCanonico.contrato(c),
            c.contratoHash(),pc,ContenidoSnapshotCanonico.sha256(pc),ContenidoSnapshotCanonico.pares(ContenidoSnapshotCanonico.campos(c.proveniencia(),Set.of())),
            p.esquema(),p.id(),p.version(),p.zonaNegocio(),p.vigencia().unidad().name(),p.vigencia().cantidad(),p.vigencia().extensionAlcance().name(),
            p.reservaCancelacion().limitePostVencimientoDias(),p.reservaCancelacion().anticipacionSegundos(),p.reservaCancelacion().cuotaMensual(),
            p.recuperacion().unidad().name(),p.recuperacion().cantidad(),p.recuperacion().politicaId(),p.recuperacion().politicaVersion(),
            p.reembolso().alcance().name(),p.reembolso().ventanaAdicional(),p.reembolso().politicaId(),p.reembolso().politicaVersion(),
            p.unidadMonetariaExponente(),c.id());
        if(actualizado!=1) throw new ConflictoSnapshot("Raíz ausente/previamente congelada");
        for(var a:c.componentes()) jdbc.update("""
            insert into public.compra_componente_snapshot(id,compra_id,cliente_id,numero,actividad_id,nombre_actividad,cantidad,politica_version)
            values (?,?,?,?,?,?,?,?)
            """,a.id(),c.id(),c.clienteId(),a.numero(),a.actividadId(),a.nombreActividad(),a.cantidad(),a.politicaVersion());
    }
}
