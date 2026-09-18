package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.nio.file.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class OrdenSnapshotMigracionTest extends PN14Slice2Fixtures.Postgres {
    @Test void T14_fresh52Max49ChecksumsPostgreSQLRealYFullCanonJavaSQLParity() {
        var f=migrar(dataSource.getUrl(),null);assertThat(f.info().applied()).hasSize(52);assertThat(f.info().current().getVersion().toString()).isEqualTo("49");
        assertThat(jdbc.queryForObject("show server_version",String.class)).startsWith("16.");f.validate();freeze();
        for(var c:propuesta.compras()) {
            assertThat(jdbc.queryForObject("select pn14_s2_politica(c) from compra c where id=?",String.class,c.id())).isEqualTo(ContenidoSnapshotCanonico.politica(c.politica()));
            assertThat(jdbc.queryForObject("select pn14_s2_contrato(c) from compra c where id=?",String.class,c.id())).isEqualTo(ContenidoSnapshotCanonico.contrato(c));
            assertThat(jdbc.queryForObject("select pn14_s2_hash(pn14_s2_contrato(c)) from compra c where id=?",String.class,c.id())).isEqualTo(c.contratoHash());
            for(var a:c.componentes())assertThat(jdbc.queryForObject("select pn14_s2_componente(a) from compra_componente_snapshot a where id=?",String.class,a.id()))
                .isEqualTo(ContenidoSnapshotCanonico.pares(ContenidoSnapshotCanonico.campos(a,Set.of())));
        }
        assertThat(jdbc.queryForObject("select pn14_s2_orden(o) from orden_venta o where id=?",String.class,propuesta.id())).isEqualTo(ContenidoSnapshotCanonico.orden(propuesta));
        assertThat(jdbc.queryForObject("select pn14_s2_uuid(?)",UUID.class,"PN14_S2_1:ORDEN:"+propuesta.scopeKey())).isEqualTo(propuesta.id());
        for(var c:Currency.getAvailableCurrencies()) {
            Integer observado=jdbc.queryForObject("select pn14_s2_iso_exponente(?)",Integer.class,c.getCurrencyCode());
            assertThat(observado).as(c.getCurrencyCode()).isEqualTo(c.getDefaultFractionDigits()<0?null:c.getDefaultFractionDigits());
        }
    }
    @Test void T14_encoderTiposUnicodeNulosYCanonInvalidoFallaSinNormalizar() {
        Map<String,Object> m=new HashMap<>();m.put("a","~NULL");m.put("á","🌿\n~NULL");m.put("z",null);
        String canon=ContenidoSnapshotCanonico.pares(m);
        assertThat(jdbc.queryForObject("select pn14_s2_obj(pn14_s2_parse(?))",String.class,canon)).isEqualTo(canon);
        assertThat(jdbc.queryForObject("select pn14_s2_hash(?)",String.class,canon)).isEqualTo(ContenidoSnapshotCanonico.sha256(canon));
        for(String mal:List.of("01:a8:STRING:x\n","1:a6:INT:01\n","1:a7:BOOL:xx\n","1:a10:NULL:OTRO\n","1:a8:STRING:x"))
            assertThatThrownBy(() -> jdbc.queryForObject("select pn14_s2_parse(?)::text",String.class,mal)).isInstanceOf(org.springframework.dao.DataAccessException.class);
    }
    @Test void T15_upgrade47PreservaTodasColumnasVariantesDefaultIndicesChecksumsYCrud() {
        String url=database(false);var old=migrar(url,"47");assertThat(old.info().applied()).hasSize(50);
        JdbcTemplate j=new JdbcTemplate(ds(url));
        UUID c=UUID.randomUUID(),p=UUID.randomUUID(),grupo=UUID.randomUUID(),actor=UUID.randomUUID();
        for(UUID id:List.of(c,actor))j.update("insert into usuario(id,nombre,correo) values (?,'dummy',?)",id,id+"@pn14.invalid");
        j.update("insert into paquete(id,nombre,precio_centavos,vigencia_dias) values (?,'legacy',1,30)",p);
        List<UUID> rows=new ArrayList<>();int n=0;
        for(String estado:List.of("pendiente","pagada","fallida","cancelada","reembolsada"))for(String metodo:List.of("stripe","efectivo","transferencia")) {
            UUID id=UUID.randomUUID();rows.add(id);n++;
            j.update("""
                insert into compra(id,usuario_id,paquete_id,monto_centavos,moneda,estado,metodo_pago,registrada_por_id,salon_id,
                grupo_compra_id,numero_item,motivo_estado,fecha_expiracion,stripe_payment_intent_id,idempotency_key,creado_en,actualizado_en)
                values (?,?,?,?,'mxn',?,?,?,(select id from salon limit 1),?,?,?,'2030-01-01',?,?, '2026-01-01T12:00:00.123456Z','2026-02-01T12:00:00.654321Z')
                """,id,c,p,n%2==0?0:12345,estado,metodo,n%2==0?actor:null,n%2==0?grupo:null,n%2==0?n:null,"motivo á\n"+n,
                metodo.equals("stripe")?"pi_dummy_"+id:null,"key-dummy-"+id);
        }
        UUID defaults=UUID.randomUUID();rows.add(defaults);j.update("insert into compra(id,usuario_id,paquete_id,monto_centavos) values (?,?,?,77)",defaults,c,p);
        for(int anomalía=0;anomalía<5;anomalía++) {
            UUID id=UUID.randomUUID();rows.add(id);
            j.update("insert into compra(id,usuario_id,paquete_id,monto_centavos,moneda,grupo_compra_id,numero_item) values (?,?,?,?,?,?,?)",
                id,c,p,List.of(-1,Integer.MIN_VALUE,Integer.MAX_VALUE,0,1).get(anomalía),List.of("zzz","","USD","mXn","mxn").get(anomalía),grupo,
                anomalía==0?null:anomalía==1?-1:anomalía==2?0:1);
        }
        var fingerprints=j.queryForList("select id,pn14_placeholder from compra".replace("pn14_placeholder","to_jsonb(compra)::text as fingerprint"));
        var columnas=j.queryForList("select column_name,data_type,column_default,is_nullable,udt_name from information_schema.columns where table_schema='public' and table_name='compra' order by ordinal_position");
        var indices=j.queryForList("select indexname,indexdef from pg_indexes where schemaname='public' and tablename='compra' order by indexname");
        var constraints=j.queryForList("select conname,pg_get_constraintdef(oid) as def from pg_constraint where conrelid='compra'::regclass order by conname");
        var checksums=j.queryForList("select version,checksum from flyway_schema_history where success order by installed_rank");
        var nuevo=migrar(url,null);assertThat(nuevo.info().applied()).hasSize(52);assertThat(nuevo.info().current().getVersion().toString()).isEqualTo("49");nuevo.validate();
        List<String> nombres=columnas.stream().map(x -> (String)x.get("column_name")).toList();
        String object="jsonb_build_object("+String.join(",",nombres.stream().map(k -> "'"+k+"',"+k).toList())+")::text";
        assertThat(j.queryForList("select id,"+object+" as fingerprint from compra")).containsExactlyInAnyOrderElementsOf(fingerprints);
        assertThat(j.queryForList("select column_name,data_type,column_default,is_nullable,udt_name from information_schema.columns where table_schema='public' and table_name='compra' order by ordinal_position").subList(0,columnas.size())).isEqualTo(columnas);
        assertThat(j.queryForList("select indexname,indexdef from pg_indexes where schemaname='public' and tablename='compra' order by indexname")).containsAll(indices);
        assertThat(j.queryForList("select conname,pg_get_constraintdef(oid) as def from pg_constraint where conrelid='compra'::regclass order by conname")).containsAll(constraints);
        assertThat(j.queryForList("select version,checksum from flyway_schema_history where success order by installed_rank").subList(0,50)).isEqualTo(checksums);
        assertThat(j.queryForObject("select count(*) from compra where orden_venta_id is not null",Integer.class)).isZero();
        for(String table:List.of("orden_venta","compra_componente_snapshot","informe_backfill_snapshot")) assertThat(j.queryForObject("select count(*) from "+table,Integer.class)).isZero();
        UUID after=UUID.randomUUID();j.update("insert into compra(id,usuario_id,paquete_id,monto_centavos) values (?,?,?,77)",after,c,p);
        assertThat(j.queryForMap("select estado,metodo_pago,moneda,monto_centavos from compra where id=?",after)).isEqualTo(j.queryForMap("select estado,metodo_pago,moneda,monto_centavos from compra where id=?",defaults));
        j.update("update compra set estado='reembolsada',motivo_estado='compat',monto_centavos=88 where id=?",after);assertThat(j.update("delete from compra where id=?",after)).isEqualTo(1);
        assertThat(j.queryForObject("select count(*) from compra",Integer.class)).isEqualTo(21);
        System.out.println("PN14_S2_UPGRADE V47=50 -> V49=52 legacyVariants=21 fingerprintsAllColumns=true oldChecksums=50 preserved PostgreSQL="+j.queryForObject("show server_version",String.class));
    }
}
