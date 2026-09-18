package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.interceptor.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class OrdenSnapshotTransaccionTest extends PN14Slice2Fixtures.Postgres {
    @Test void T08_proxyRealPrimeraRaizComponenteInsertadosSegundaFKFallaRollbackTodo() throws Exception {
        AtomicInteger observados=new AtomicInteger();
        var dsObservado=new org.springframework.jdbc.datasource.DelegatingDataSource(dataSource) {
            @Override public Connection getConnection() throws SQLException {
                Connection c=super.getConnection();
                return (Connection)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Connection.class},(p,m,a) -> {
                    try {
                        Object resultado=m.invoke(c,a);
                        if(m.getName().equals("prepareStatement") && ((String)a[0]).contains("insert into public.compra_componente_snapshot")) {
                            PreparedStatement ps=(PreparedStatement)resultado;
                            return java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{PreparedStatement.class},(q,pm,pa) -> {
                                try {
                                    Object pr=pm.invoke(ps,pa);
                                    if(pm.getName().equals("executeUpdate"))try(var st=c.createStatement();var r=st.executeQuery("select count(*) from compra_componente_snapshot")) {
                                        r.next();observados.set(r.getInt(1));
                                    }
                                    return pr;
                                }catch(java.lang.reflect.InvocationTargetException ex){throw ex.getCause();}
                            });
                        }
                        return resultado;
                    }catch(java.lang.reflect.InvocationTargetException ex){throw ex.getCause();}
                });
            }
        };
        var j=new org.springframework.jdbc.core.JdbcTemplate(dsObservado);
        var txm=new org.springframework.jdbc.datasource.DataSourceTransactionManager(dsObservado);
        var adapter=new com.feelingpilates.pagos.ventas.infraestructura.OrdenSnapshotJdbcAdapter(j,txm);
        var pf=new ProxyFactory(adapter); pf.setInterfaces(RepositorioOrdenSnapshot.class);
        var attrs=new NameMatchTransactionAttributeSource(); var attr=new RuleBasedTransactionAttribute();
        attr.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED); attrs.addTransactionalMethod("congelar",attr);
        pf.addAdvice(new TransactionInterceptor(txm,attrs)); var proxy=(RepositorioOrdenSnapshot)pf.getProxy(); assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        Compra segunda=compra(ids.get(1),propuesta.id(),cliente,2,producto,List.of(UUID.randomUUID()),12345,RAW,S);
        var invalida=new OrdenVenta(propuesta.id(),cliente,propuesta.scopeKey(),propuesta.total(),List.of(propuesta.compras().getFirst(),segunda),S,null);
        assertThatThrownBy(() -> proxy.congelar(invalida,evidencia(invalida))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(observados.get()).isEqualTo(2); vacio(); assertThat(count("informe_backfill_snapshot")).isZero();
        try(var c=dataSource.getConnection();var s=c.createStatement();var r=s.executeQuery("select count(*) from orden_venta")) {r.next();assertThat(r.getInt(1)).isZero();}
    }
    @Test void T08_PREPARANDOSinSelloNoCommitNiGrupoParcial() {
        var e=evidencia(propuesta);
        assertThatThrownBy(() -> new TransactionTemplate(manager).execute(s -> jdbc.update("""
            insert into orden_venta(id,cliente_id,scope_key,moneda_iso,total_unidades_minimas,numero_lineas,estado_fundacion,
            congelado_en,payload_canonico,payload_hash,procedencia_canonica) values (?,?,?,'MXN',24690,2,'PREPARANDO',?,?,?,'')
            """,propuesta.id(),cliente,propuesta.scopeKey(),Timestamp.from(S),ContenidoSnapshotCanonico.orden(propuesta),propuesta.payloadHash())))
            .isInstanceOf(RuntimeException.class).rootCause().hasMessageContaining("PREPARANDO");
        vacio();
        var incompleta=new RepositorioOrdenSnapshot.EvidenciaScope(e.filas(),List.of(ids.getFirst()),propuesta.total(),e.informe());
        assertThatThrownBy(() -> new CongelarOrdenSnapshot(repo).ejecutar(new CongelarOrdenSnapshot.Entrada(propuesta,incompleta))).isInstanceOf(IllegalArgumentException.class);
        vacio();
    }
    @Test void T08_SQLDeclararSoloUnaLineaDeGrupoCompletoNoEvadeMembership() {
        var c=propuesta.compras().getFirst();var parcial=new OrdenVenta(propuesta.id(),cliente,propuesta.scopeKey(),c.precioVenta(),List.of(c),S,null);
        fallaTx(() -> {prepararDirecto(parcial);attachDirecto(c.id(),bundleDirecto(c));componentesDirecto(c);selloDirecto();},"membership scope completo requerido");
        assertThat(count("informe_backfill_snapshot")).isZero();
    }
}
