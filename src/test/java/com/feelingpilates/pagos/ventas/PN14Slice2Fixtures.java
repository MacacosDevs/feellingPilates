package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.dominio.*;
import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.infraestructura.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

/** Sólo dummy sintético; jamás prueba que existan sobres históricos reales. */
public final class PN14Slice2Fixtures {
    private PN14Slice2Fixtures() { }
    public static final Instant S=Instant.parse("2026-09-01T12:34:56.123456Z");
    public static final String RAW="Contrato histórico firmado dummy\nPilates á + Bacu 🌿\nimporte 12345 MXN; política v7/composición contemporánea";
    public static PoliticaComercialSnapshot politica() {
        return new PoliticaComercialSnapshot("PN14_S2_1","contrato-dummy","v7","America/Mexico_City",
            new PoliticaComercialSnapshot.Vigencia(PoliticaComercialSnapshot.Unidad.MESES,2,PoliticaComercialSnapshot.ExtensionAlcance.MISMA_ACTIVIDAD),
            new PoliticaComercialSnapshot.ReservaCancelacion(3,86400,2),
            new PoliticaComercialSnapshot.Recuperacion(PoliticaComercialSnapshot.Unidad.DIAS,5,"rec-dummy","r2"),
            new PoliticaComercialSnapshot.Reembolso(PoliticaComercialSnapshot.ReembolsoAlcance.PRODUCTO_ENTERO,false,"refund-dummy","f3"),2,
            new PoliticaComercialSnapshot.Terminos(false,false,false,PoliticaComercialSnapshot.Ancla.ACREDITACION_COMPLETADA,
                true,true,true,true,true,true,true,false,PoliticaComercialSnapshot.Resolucion.ADMIN,false,true,true,true,true,false,false,
                "clienteId+YearMonth(inicioSesion,zonaNegocio)+policyVersion"));
    }
    public static Compra compra(UUID id,UUID orden,UUID cliente,int numero,UUID producto,List<UUID> actividades,long monto,String raw,Instant instante) {
        var p=politica(); List<CompraComponenteSnapshot> cs=new ArrayList<>();
        for(int i=0;i<actividades.size();i++) cs.add(new CompraComponenteSnapshot(ContenidoSnapshotCanonico.componenteId(id,i+1),id,i+1,
                actividades.get(i),i==0?"Pilates á\n🌿":"Bacu histórico",i==0?4:2,p.version()));
        Map<String,Object> m=new HashMap<>(); m.put("id",id); m.put("ordenId",orden); m.put("clienteId",cliente); m.put("numeroLinea",numero);
        m.put("productoFuenteId",producto); m.put("nombreProducto","Mixto histórico ~NULL"); m.put("tipoProducto",Compra.TipoProducto.PAQUETE);
        m.put("precioVenta",new ImporteMonetario(monto,"MXN")); m.put("politica",p); m.put("componentes",cs);
        Set<String> paths=new HashSet<>(ContenidoSnapshotCanonico.paths(m));
        paths.addAll(List.of("scopeKey","membership","numeroLineas","total.unidadesMinimas","total.monedaIso","productoIdentidadAlternativa"));
        var provenance=new ProvenienciaSnapshot(ProvenienciaSnapshot.Origen.CONTRATO_CONTEMPORANEO_VERIFICADO,"ticket-dummy",cliente,S,
            raw,ContenidoSnapshotCanonico.sha256(raw),"PN14_S2_1",paths.stream().map(path -> new ProvenienciaSnapshot.CampoFuente(
                path,"ticket-dummy",ContenidoSnapshotCanonico.sha256(raw))).toList());
        return new Compra(id,orden,cliente,numero,producto,"Mixto histórico ~NULL",Compra.TipoProducto.PAQUETE,new ImporteMonetario(monto,"MXN"),p,cs,provenance,instante,null);
    }
    public static OrdenVenta orden(UUID cliente,UUID grupo,List<UUID> ids,UUID producto,List<UUID> actividades,long monto,String raw) {
        String scope=grupo==null?"LEGACY_COMPRA:"+ids.getFirst():"LEGACY_GRUPO:"+grupo; UUID orden=ContenidoSnapshotCanonico.ordenId(scope);
        List<Compra> compras=new ArrayList<>(); for(int i=0;i<ids.size();i++) compras.add(compra(ids.get(i),orden,cliente,i+1,producto,actividades,monto,raw,S));
        return new OrdenVenta(orden,cliente,scope,new ImporteMonetario(Math.multiplyExact(monto,ids.size()),"MXN"),compras,S,null);
    }
    public static FuenteHistoricaCompra.Fuente fuente(OrdenVenta o,String raw) {
        return new FuenteHistoricaCompra.Fuente(raw,ContenidoSnapshotCanonico.sha256(raw),"ticket-dummy",o.clienteId(),S,
            ProvenienciaSnapshot.Origen.CONTRATO_CONTEMPORANEO_VERIFICADO,"PN14_S2_1",true,o,o.compras().stream().map(Compra::id).toList(),o.total(),List.of(),List.of());
    }
    static final class Host {
        static final PostgreSQLContainer<?> PG=new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        static {
            PG.start();
            try(Connection c=DriverManager.getConnection(PG.getJdbcUrl(),PG.getUsername(),PG.getPassword()); Statement s=c.createStatement()) {
                s.execute("create database pn14_s2_template");
            } catch(SQLException ex) { throw new ExceptionInInitializerError(ex); }
            var f=migrar(url("pn14_s2_template"),null);
            assertThat(f.info().applied()).hasSize(52); assertThat(f.info().current().getVersion().toString()).isEqualTo("49");
            System.out.println("PN14_S2_FRESH PostgreSQL="+PG.getDockerImageName()+" version="+new JdbcTemplate(ds(url("pn14_s2_template"))).queryForObject("show server_version",String.class)+" migrations=52 max=49");
        }
        static String url(String db) { return PG.getJdbcUrl().substring(0,PG.getJdbcUrl().lastIndexOf('/')+1)+db; }
    }
    static DriverManagerDataSource ds(String url) { return new DriverManagerDataSource(url,Host.PG.getUsername(),Host.PG.getPassword()); }
    static Flyway migrar(String url,String target) {
        var config=Flyway.configure().dataSource(url,Host.PG.getUsername(),Host.PG.getPassword());
        if(target!=null) config.target(target); Flyway f=config.load(); f.migrate(); return f;
    }
    static String database(boolean template) {
        String nombre="pn14_s2_"+UUID.randomUUID().toString().replace("-","");
        try(Connection c=DriverManager.getConnection(Host.PG.getJdbcUrl(),Host.PG.getUsername(),Host.PG.getPassword());Statement s=c.createStatement()) {
            s.execute("create database "+nombre+(template?" with template pn14_s2_template":""));
        } catch(SQLException ex) { throw new IllegalStateException(ex); }
        return Host.url(nombre);
    }
    abstract static class Postgres {
        JdbcTemplate jdbc; DriverManagerDataSource dataSource; DataSourceTransactionManager manager;
        OrdenSnapshotJdbcAdapter repo; InformeBackfillJdbcAdapter informes; ConsultaHistoricaSnapshotJdbcAdapter consulta;
        UUID cliente,otroCliente,producto,grupo; List<UUID> actividades,ids; OrdenVenta propuesta;
        @BeforeEach void preparar() {
            dataSource=ds(database(true)); jdbc=new JdbcTemplate(dataSource); manager=new DataSourceTransactionManager(dataSource);
            repo=new OrdenSnapshotJdbcAdapter(jdbc,manager); informes=new InformeBackfillJdbcAdapter(jdbc,manager);
            consulta=new ConsultaHistoricaSnapshotJdbcAdapter(jdbc);
            cliente=UUID.randomUUID(); otroCliente=UUID.randomUUID(); producto=UUID.randomUUID(); grupo=UUID.randomUUID();
            for(UUID c:List.of(cliente,otroCliente)) jdbc.update("insert into usuario(id,nombre,correo) values (?,'cliente dummy',?)",c,c+"@pn14.invalid");
            jdbc.update("insert into paquete(id,nombre,precio_centavos,vigencia_dias) values (?,'Hoy99999',99999,9)",producto);
            actividades=List.of(UUID.randomUUID(),UUID.randomUUID());
            for(UUID a:actividades) jdbc.update("insert into tipo_actividad(id,nombre) values (?,?)",a,"actual-"+a);
            ids=List.of(UUID.randomUUID(),UUID.randomUUID());
            for(int i=0;i<ids.size();i++) insertar(ids.get(i),grupo,i+1,12345,"mxn",cliente);
            propuesta=orden(cliente,grupo,ids,producto,actividades,12345,RAW);
        }
        void insertar(UUID id,UUID g,Integer numero,int monto,String moneda,UUID c) {
            jdbc.update("insert into compra(id,usuario_id,paquete_id,grupo_compra_id,numero_item,monto_centavos,moneda,estado) values (?,?,?,?,?,?,?,'pagada')",
                id,c,producto,g,numero,monto,moneda);
        }
        FuenteHistoricaCompraJdbcAdapter origen(List<FuenteHistoricaCompra.Fuente> fuentes) {
            return new FuenteHistoricaCompraJdbcAdapter(jdbc,Map.of(propuesta.scopeKey(),fuentes),S);
        }
        FuenteHistoricaCompra.Grupo datos() { return origen(List.of(fuente(propuesta,RAW))).obtener(propuesta.scopeKey()); }
        RepositorioOrdenSnapshot.EvidenciaScope evidencia(OrdenVenta o) {
            var g=origen(List.of(fuente(o,o.compras().getFirst().proveniencia().fuenteRaw()))).obtener(o.scopeKey());
            var informe=BackfillOrdenSnapshot.informe(g,InformeBackfillSnapshot.Estado.CONGELADA,List.of(),List.of(),java.math.BigInteger.valueOf(o.total().unidadesMinimas()));
            return new RepositorioOrdenSnapshot.EvidenciaScope(g.filas(),o.compras().stream().map(Compra::id).toList(),o.total(),informe);
        }
        CongelarOrdenSnapshot.Resultado freeze() { return new CongelarOrdenSnapshot(repo).ejecutar(new CongelarOrdenSnapshot.Entrada(propuesta,evidencia(propuesta))); }
        BackfillOrdenSnapshot backfill(List<FuenteHistoricaCompra.Fuente> fuentes) { return new BackfillOrdenSnapshot(origen(fuentes),new CongelarOrdenSnapshot(repo),informes); }
        int count(String table) { return jdbc.queryForObject("select count(*) from "+table,Integer.class); }
        void vacio() { assertThat(count("orden_venta")).isZero(); assertThat(count("compra_componente_snapshot")).isZero();
            assertThat(jdbc.queryForObject("select count(*) from compra where orden_venta_id is not null",Integer.class)).isZero(); }
        void rechaza(String sql,Object... args) { assertThatThrownBy(() -> jdbc.update(sql,args)).isInstanceOf(org.springframework.dao.DataAccessException.class); }
        void prepararDirecto(OrdenVenta o) {
            jdbc.update("""
                insert into orden_venta(id,cliente_id,scope_key,moneda_iso,total_unidades_minimas,numero_lineas,estado_fundacion,
                congelado_en,payload_canonico,payload_hash,procedencia_canonica) values (?,?,?,?,?,?,'PREPARANDO',?,?,?,?)
                """,o.id(),o.clienteId(),o.scopeKey(),o.total().monedaIso(),o.total().unidadesMinimas(),o.compras().size(),Timestamp.from(o.congeladoEn()),
                ContenidoSnapshotCanonico.orden(o),o.payloadHash(),ContenidoSnapshotCanonico.pares(Map.of("compras",o.compras().stream().map(Compra::proveniencia).toList())));
        }
        Map<String,Object> bundleDirecto(Compra c) {
            var p=c.politica();Map<String,Object> m=new LinkedHashMap<>();
            m.put("orden_venta_id",c.ordenId());m.put("cliente_snapshot_id",c.clienteId());m.put("numero_linea",c.numeroLinea());
            m.put("producto_fuente_id",c.productoFuenteId());m.put("nombre_producto_snapshot",c.nombreProducto());m.put("tipo_producto_snapshot",c.tipoProducto().name());
            m.put("precio_venta_unidades_minimas",c.precioVenta().unidadesMinimas());m.put("moneda_snapshot_iso",c.precioVenta().monedaIso());
            m.put("congelado_en",Timestamp.from(c.congeladoEn()));m.put("contrato_canonico",ContenidoSnapshotCanonico.contrato(c));m.put("contrato_hash",c.contratoHash());
            m.put("politica_canonica",ContenidoSnapshotCanonico.politica(p));m.put("politica_hash",ContenidoSnapshotCanonico.sha256(ContenidoSnapshotCanonico.politica(p)));
            m.put("procedencia_canonica",ContenidoSnapshotCanonico.pares(ContenidoSnapshotCanonico.campos(c.proveniencia(),Set.of())));
            m.put("politica_esquema",p.esquema());m.put("politica_id",p.id());m.put("politica_version",p.version());m.put("zona_negocio",p.zonaNegocio());
            m.put("vigencia_unidad",p.vigencia().unidad().name());m.put("vigencia_cantidad",p.vigencia().cantidad());m.put("extension_alcance",p.vigencia().extensionAlcance().name());
            m.put("reserva_limite_post_vencimiento_dias",p.reservaCancelacion().limitePostVencimientoDias());m.put("cancelacion_anticipacion_segundos",p.reservaCancelacion().anticipacionSegundos());
            m.put("cancelacion_cuota_mensual",p.reservaCancelacion().cuotaMensual());m.put("recuperacion_unidad",p.recuperacion().unidad().name());m.put("recuperacion_cantidad",p.recuperacion().cantidad());
            m.put("recuperacion_politica_id",p.recuperacion().politicaId());m.put("recuperacion_politica_version",p.recuperacion().politicaVersion());
            m.put("reembolso_alcance",p.reembolso().alcance().name());m.put("reembolso_ventana_adicional",p.reembolso().ventanaAdicional());
            m.put("reembolso_politica_id",p.reembolso().politicaId());m.put("reembolso_politica_version",p.reembolso().politicaVersion());m.put("unidad_monetaria_exponente",p.unidadMonetariaExponente());return m;
        }
        void attachDirecto(UUID id,Map<String,Object> bundle) {
            List<Object> args=new ArrayList<>(bundle.values());args.add(id);
            jdbc.update("update compra set "+String.join(",",bundle.keySet().stream().map(k -> k+"=?").toList())+" where id=?",args.toArray());
        }
        void componentesDirecto(Compra c) {
            for(var a:c.componentes())jdbc.update("insert into compra_componente_snapshot values (?,?,?,?,?,?,?,?)",a.id(),c.id(),c.clienteId(),a.numero(),a.actividadId(),a.nombreActividad(),a.cantidad(),a.politicaVersion());
        }
        void selloDirecto() {jdbc.update("update orden_venta set estado_fundacion='CONGELADA' where id=?",propuesta.id());}
        void fallaTx(Runnable action,String mensaje) {
            Throwable ex=org.assertj.core.api.Assertions.catchThrowable(() -> new org.springframework.transaction.support.TransactionTemplate(manager).execute(s -> {action.run();return null;}));
            assertThat(ex).isNotNull();while(ex.getCause()!=null)ex=ex.getCause();assertThat(ex.getMessage()).contains(mensaje);vacio();
        }
    }
}
