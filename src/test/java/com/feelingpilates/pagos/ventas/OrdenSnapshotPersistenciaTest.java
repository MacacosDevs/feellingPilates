package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class OrdenSnapshotPersistenciaTest extends PN14Slice2Fixtures.Postgres {
    @Test void T05_guardasSQLRaizOrdenComponentesReporteYTruncate() {
        freeze(); var c=propuesta.compras().getFirst(); var a=c.componentes().getFirst();
        for(String columna:List.of("nombre_producto_snapshot","politica_version","contrato_hash","procedencia_canonica"))
            rechaza("update compra set "+columna+"='cambio' where id=?",c.id());
        rechaza("update compra set precio_venta_unidades_minimas=0 where id=?",c.id());
        rechaza("update compra set usuario_id=? where id=?",otroCliente,c.id());
        rechaza("update compra set id=? where id=?",UUID.randomUUID(),c.id());
        rechaza("delete from compra where id=?",c.id());
        rechaza("update orden_venta set estado_fundacion='PREPARANDO' where id=?",propuesta.id());
        rechaza("update orden_venta set payload_hash=payload_hash where id=?",propuesta.id());
        rechaza("delete from orden_venta where id=?",propuesta.id());
        rechaza("update compra_componente_snapshot set cantidad=99 where id=?",a.id());
        rechaza("delete from compra_componente_snapshot where id=?",a.id());
        rechaza("insert into compra_componente_snapshot values (?,?,?,3,?,'postseal',1,'v7')",ContenidoSnapshotCanonico.componenteId(c.id(),3),c.id(),cliente,UUID.randomUUID());
        rechaza("update informe_backfill_snapshot set estado='REPLAY'"); rechaza("delete from informe_backfill_snapshot");
        for(String table:List.of("orden_venta","compra","compra_componente_snapshot","informe_backfill_snapshot"))
            assertThatThrownBy(() -> jdbc.execute("truncate "+table+" cascade")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(count("orden_venta")).isEqualTo(1); assertThat(count("compra_componente_snapshot")).isEqualTo(4);
        assertThat(repo.buscar(propuesta.id(),cliente)).contains(propuesta);
    }
    @Test void T05_catalogoRenombradoDesactivadoNoReinterpretaSnapshot() {
        freeze(); jdbc.update("update paquete set nombre='nuevo',precio_centavos=99999,activo=false where id=?",producto);
        jdbc.update("update tipo_actividad set nombre=nombre||'-renombrada',activo=false where id in (?,?)",actividades.get(0),actividades.get(1));
        assertThat(repo.buscar(propuesta.id(),cliente)).contains(propuesta);
        assertThat(consulta.buscar(cliente,propuesta.id()).orElseThrow().lineas().getFirst().precioVenta().unidadesMinimas()).isEqualTo(12345);
        rechaza("delete from tipo_actividad where id=?",actividades.getFirst());
        rechaza("delete from usuario where id=?",cliente);
    }
    @Test void T06_constraintsFisicosCompuestosUniqueEnumsYCantidades() {
        assertThat(jdbc.queryForObject("select pg_get_constraintdef(oid) from pg_constraint where conname='pn14_s2_compra_orden_fk'",String.class))
            .contains("orden_venta_id, cliente_snapshot_id, moneda_snapshot_iso","ON DELETE RESTRICT");
        assertThat(jdbc.queryForObject("select count(*) from pg_constraint where conrelid='compra_componente_snapshot'::regclass and contype='f'",Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from pg_constraint where conrelid='compra_componente_snapshot'::regclass and contype='u'",Integer.class)).isEqualTo(2);
        for(String set:List.of("numero_linea=1","politica_version='v7'","producto_fuente_id=gen_random_uuid()","cliente_snapshot_id=usuario_id"))
            rechaza("update compra set "+set+" where id=?",ids.getFirst());
        freeze(); assertThat(jdbc.queryForObject("select count(*) from compra where cliente_snapshot_id=usuario_id and moneda_snapshot_iso='MXN'",Integer.class)).isEqualTo(2);
    }
    @Test void T06_SQLDirectoFKClienteMonedaUniqueLineasYCantidadesAntesDeSello() {
        var c=propuesta.compras().getFirst();
        // Igualdad usuario/snapshot válida, pero cliente de orden distinto: prueba FK compuesta.
        jdbc.update("update compra set usuario_id=? where id=?",otroCliente,c.id());
        fallaTx(() -> {prepararDirecto(propuesta);var m=bundleDirecto(c);m.put("cliente_snapshot_id",otroCliente);attachDirecto(c.id(),m);},"pn14_s2_compra_orden_fk");
        jdbc.update("update compra set usuario_id=? where id=?",cliente,c.id());
        jdbc.update("update compra set moneda='usd' where id=?",c.id());
        fallaTx(() -> {prepararDirecto(propuesta);var m=bundleDirecto(c);m.put("moneda_snapshot_iso","USD");attachDirecto(c.id(),m);},"pn14_s2_compra_orden_fk");
        jdbc.update("update compra set moneda='mxn' where id=?",c.id());
        fallaTx(() -> {prepararDirecto(propuesta);attachDirecto(c.id(),bundleDirecto(c));var segundo=bundleDirecto(propuesta.compras().get(1));segundo.put("numero_linea",1);attachDirecto(ids.get(1),segundo);},"pn14_s2_compra_linea_unique");
        for(int q:List.of(0,-1))fallaTx(() -> {prepararDirecto(propuesta);attachDirecto(c.id(),bundleDirecto(c));var a=c.componentes().getFirst();
            jdbc.update("insert into compra_componente_snapshot values (?,?,?,?,?,?,?,?)",a.id(),c.id(),cliente,1,a.actividadId(),a.nombreActividad(),q,"v7");},"cantidad_check");
        fallaTx(() -> {prepararDirecto(propuesta);attachDirecto(c.id(),bundleDirecto(c));var a=c.componentes().getFirst();
            jdbc.update("insert into compra_componente_snapshot values (?,?,?,?,?,?,?,?)",a.id(),c.id(),cliente,1,UUID.randomUUID(),a.nombreActividad(),1,"v7");},"actividad_id_fkey");
        fallaTx(() -> {prepararDirecto(propuesta);attachDirecto(c.id(),bundleDirecto(c));var a=c.componentes().getFirst();
            jdbc.update("insert into compra_componente_snapshot values (?,?,?,?,?,?,?,?)",a.id(),c.id(),otroCliente,1,a.actividadId(),a.nombreActividad(),1,"v7");},"compra_id_cliente_id_fkey");
    }
    @Test void T07_SQLCanonHashValidoPeroCamposTipadosDistintosNoPuedeSellar() {
        fallaTx(() -> {prepararDirecto(propuesta);for(var c:propuesta.compras()) {
            var m=bundleDirecto(c);m.put("vigencia_cantidad",3);attachDirecto(c.id(),m);componentesDirecto(c);
        }selloDirecto();},"policy/contrato/componentes incoherentes");
        fallaTx(() -> {prepararDirecto(propuesta);for(var c:propuesta.compras()) {
            var m=bundleDirecto(c);String mal=ContenidoSnapshotCanonico.contrato(c).replace("LONG:12345","LONG:54321");
            m.put("contrato_canonico",mal);m.put("contrato_hash",ContenidoSnapshotCanonico.sha256(mal));attachDirecto(c.id(),m);componentesDirecto(c);
        }selloDirecto();},"policy/contrato/componentes incoherentes");
        fallaTx(() -> {prepararDirecto(propuesta);var c=propuesta.compras().getFirst();var m=bundleDirecto(c);m.put("politica_hash","0".repeat(64));attachDirecto(c.id(),m);},"pn14_s2_bundle_completo");
        fallaTx(() -> {prepararDirecto(propuesta);var c=propuesta.compras().getFirst();var m=bundleDirecto(c);m.put("recuperacion_politica_id",null);attachDirecto(c.id(),m);},"pn14_s2_bundle_completo");
    }
    @Test void T07_componentesVaciosConGapActividadDuplicadaYAttachMontoDiscrepanteFail() {
        fallaTx(() -> {prepararDirecto(propuesta);for(var c:propuesta.compras())attachDirecto(c.id(),bundleDirecto(c));selloDirecto();},"policy/contrato/componentes incoherentes");
        fallaTx(() -> {prepararDirecto(propuesta);for(var c:propuesta.compras()) {
            attachDirecto(c.id(),bundleDirecto(c));var a=c.componentes().get(1);
            jdbc.update("insert into compra_componente_snapshot values (?,?,?,?,?,?,?,?)",a.id(),c.id(),cliente,2,a.actividadId(),a.nombreActividad(),a.cantidad(),"v7");
        }selloDirecto();},"policy/contrato/componentes incoherentes");
        fallaTx(() -> {prepararDirecto(propuesta);var c=propuesta.compras().getFirst();attachDirecto(c.id(),bundleDirecto(c));componentesDirecto(c);var a=c.componentes().getFirst();
            jdbc.update("insert into compra_componente_snapshot values (?,?,?,?,?,?,?,?)",ContenidoSnapshotCanonico.componenteId(c.id(),3),c.id(),cliente,3,a.actividadId(),"duplicada",1,"v7");},"compra_id_actividad_id_key");
        fallaTx(() -> {prepararDirecto(propuesta);var c=propuesta.compras().getFirst();var m=bundleDirecto(c);m.put("precio_venta_unidades_minimas",12346L);attachDirecto(c.id(),m);},"attach inválido");
    }
    @Test void T07_camposLegacySiguenWritableYCrudSinBundleNormal() {
        freeze();
        jdbc.update("update compra set estado='reembolsada',monto_centavos=7,moneda='usd',fecha_expiracion='2030-01-01',motivo_estado='legacy',actualizado_en=now() where id=?",ids.getFirst());
        assertThat(repo.buscar(propuesta.id(),cliente)).contains(propuesta);
        UUID nuevo=UUID.randomUUID(); insertar(nuevo,null,null,77,"mxn",otroCliente);
        jdbc.update("update compra set estado='cancelada',monto_centavos=88 where id=?",nuevo);
        assertThat(jdbc.queryForObject("select orden_venta_id is null from compra where id=?",Boolean.class,nuevo)).isTrue();
        assertThat(jdbc.update("delete from compra where id=?",nuevo)).isEqualTo(1);
    }
    @Test void T07_JPAUnicoMappingFinancieroPreservaColumnasSnapshot() {
        freeze();
        var factory=new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource); factory.setPackagesToScan("com.feelingpilates");
        factory.setJpaVendorAdapter(new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","validate","hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl"));
        factory.afterPropertiesSet(); var emf=factory.getObject(); var em=emf.createEntityManager();
        try {
            em.getTransaction().begin(); var c=em.find(com.feelingpilates.pagos.entidad.Compra.class,ids.getFirst());
            c.setEstado(com.feelingpilates.pagos.entidad.Compra.EstadoCompra.cancelada); c.setMontoCentavos(4321);
            c.setFechaExpiracion(java.time.OffsetDateTime.parse("2030-01-01T00:00:00Z"));c.setMotivoEstado("JPA legacy");
            em.getTransaction().commit();
            assertThat(jdbc.queryForObject("select estado from compra where id=?",String.class,ids.getFirst())).isEqualTo("cancelada");
            assertThat(repo.buscar(propuesta.id(),cliente)).contains(propuesta);
            em.getTransaction().begin(); UUID nuevo=UUID.randomUUID();
            var legacy=new com.feelingpilates.pagos.entidad.Compra(); legacy.setUsuario(em.getReference(com.feelingpilates.usuarios.entidad.Usuario.class,otroCliente));
            legacy.setPaquete(em.getReference(com.feelingpilates.pagos.entidad.Paquete.class,producto)); legacy.setMontoCentavos(77);
            em.persist(legacy); em.flush(); nuevo=legacy.getId(); em.getTransaction().commit();
            assertThat(jdbc.queryForObject("select orden_venta_id is null from compra where id=?",Boolean.class,nuevo)).isTrue();
            em.getTransaction().begin(); em.remove(em.find(com.feelingpilates.pagos.entidad.Compra.class,nuevo)); em.getTransaction().commit();
        } finally { if(em.getTransaction().isActive()) em.getTransaction().rollback();em.close();factory.destroy(); }
    }
}
