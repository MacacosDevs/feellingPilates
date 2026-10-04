package com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.postgresql.PGConnection;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

/** Captures the calls actually forwarded to pgjdbc, separately from native-query named bindings. */
public final class AdjustmentJdbcCapture {
    public enum Fault { WRONG_DATE, WRONG_SLOT, SWAPPED, OMITTED, WRONG_TYPE, ACTIVE_FALSE,
        INITIAL_PROBE, FINAL_PROBE, METADATA, COMMIT, ROLLBACK, SNAPSHOT, EXTRA_SQL, RESOURCE }
    public enum TrustKind { DATA_SOURCE, FACTORY, HOLDER, SHARED_ENTITY_MANAGER, SESSION, PG_CONNECTION }
    private final ThreadLocal<Map.Entry<TrustKind,Object>> trustOverride=new ThreadLocal<>();
    public void trust(TrustKind kind,Object actualDifferentResource) {
        if(kind==null) trustOverride.remove();else trustOverride.set(Map.entry(kind,Objects.requireNonNull(actualDifferentResource)));
    }
    private final ThreadLocal<Capture> current=new ThreadLocal<>();
    private final ThreadLocal<Fault> fault=new ThreadLocal<>();
    private final ThreadLocal<Capture> last=new ThreadLocal<>();
    private volatile Runnable beforeData;
    private final ThreadLocal<Boolean> observingMetadata=ThreadLocal.withInitial(()->false);
    private final ThreadLocal<Boolean> checkingNative=ThreadLocal.withInitial(()->false);
    public void fault(Fault f) { if(f==null) fault.remove(); else fault.set(f); }
    public void beforeData(Runnable barrier) { beforeData=barrier; }
    public Capture open(String invocation) {
        if(current.get()!=null) throw new IllegalStateException("nested capture");
        Capture c=new Capture(invocation,Thread.currentThread()); current.set(c); return c;
    }
    public Capture last() { return last.get(); }
    public void bind(Capture c, Object owner, DataSource ds,EntityManagerFactory factory,EntityManager em,
                     EntityManagerHolder holder,Session session,Connection connection) throws SQLException {
        require(c); c.owner=owner; c.ds=ds; c.factory=factory; c.em=em; c.holder=holder; c.session=session;
        c.connection=connection; c.physical=connection.unwrap(PGConnection.class);
        var override=trustOverride.get();if(override!=null) switch(override.getKey()) {
            case DATA_SOURCE -> c.ds=(DataSource)override.getValue();
            case FACTORY -> c.factory=(EntityManagerFactory)override.getValue();
            case HOLDER -> c.holder=(EntityManagerHolder)override.getValue();
            case SHARED_ENTITY_MANAGER -> c.em=(EntityManager)override.getValue();
            case SESSION -> c.session=(Session)override.getValue();
            case PG_CONNECTION -> c.physical=override.getValue();
        }
        verify(c);
    }
    public void verify(Capture c) {
        require(c);
        if(c.owner==null||c.ds==null||c.factory==null||c.em==null||c.holder==null||c.session==null||c.physical==null
                ||TransactionSynchronizationManager.getResource(c.factory)!=c.holder
                ||c.holder.getEntityManager().unwrap(Session.class)!=c.session ||((org.springframework.orm.jpa.EntityManagerProxy)c.em).getTargetEntityManager().unwrap(Session.class)!=c.session
                ||!c.session.isJoinedToTransaction() || fault.get()==Fault.RESOURCE)
            throw invalid(null,new IllegalStateException("physical owner/resource chain changed"));
        Object dataSourceHolder=TransactionSynchronizationManager.getResource(c.ds);
        if(!(dataSourceHolder instanceof org.springframework.jdbc.datasource.ConnectionHolder h)||h.getConnection()!=c.connection)
            throw invalid(null,new IllegalStateException("bound DataSource connection differs"));
        boolean previous=checkingNative.get();checkingNative.set(true);
        try { c.session.doWork(conn->{ if(conn!=c.connection||conn.unwrap(PGConnection.class)!=c.physical)
            throw invalid(null,new IllegalStateException("native PgConnection changed")); }); }
        finally { checkingNative.set(previous); }
    }
    private void require(Capture c) {
        if(current.get()!=c||c.closed||c.thread!=Thread.currentThread()) throw invalid(null,new IllegalStateException("capture ownership"));
    }
    public String observationSignature(Capture c) {
        require(c);List<String> parts=new ArrayList<>();
        for(var s:c.statements) { parts.add(s.id);parts.add(s.sql);parts.add(Boolean.toString(s.entered));parts.add(Boolean.toString(s.completed));parts.add(Boolean.toString(s.failed));
            parts.add(Integer.toString(System.identityHashCode(s.physical)));for(var b:s.setters) parts.add(b.toString()); }
        for(var n:c.named) parts.add(n.toString());for(var m:c.metadata) {parts.add(m.method());parts.add(m.value());parts.add(Integer.toString(System.identityHashCode(m.physical())));}
        return AdjustmentReadSnapshotContext.hash(parts.toArray(String[]::new));
    }
    public void observeMetadata(Connection connection,String url,String principal) throws SQLException {
        observingMetadata.set(true);
        try {
            Object physical=connection.unwrap(PGConnection.class);String actualUrl=connection.getMetaData().getURL(),actualPrincipal=connection.getMetaData().getUserName();
            Capture c=current.get();if(physical!=c.physical||!actualUrl.equals(url)||!actualPrincipal.equals(principal))
                throw invalid(c.fecha,new IllegalStateException("native metadata mismatch"));
        }
        finally { observingMetadata.remove(); }
    }
    public void close(Capture c, boolean success) {
        require(c); c.closed=true; c.success=success; last.set(c); current.remove();
    }
    public DataSource wrap(DataSource ds) {
        return new DelegatingDataSource(ds) {
            @Override public Connection getConnection() throws SQLException { return connection(super.getConnection()); }
            @Override public Connection getConnection(String user,String password) throws SQLException {
                return connection(super.getConnection(user,password));
            }
        };
    }
    private Connection connection(Connection nativeConnection) throws SQLException {
        Object physical=nativeConnection.unwrap(PGConnection.class);
        return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,m,a)-> {
            String n=m.getName(); Capture c=current.get();
            if(n.equals("equals")) return proxy==a[0]; if(n.equals("hashCode")) return System.identityHashCode(proxy);
            if(c!=null) {
                require(c);
                if(n.equals("unwrap")&&a[0]==PGConnection.class&&observingMetadata.get()&&!checkingNative.get()) {
                    verify(c);Object result=invoke(nativeConnection,m,a);c.metadata.add(new Metadata("unwrapPGConnection","SAME_NATIVE_RESOURCE",result));return result;
                }
                if(Set.of("setAutoCommit","setReadOnly","setTransactionIsolation","commit","rollback").contains(n)) {
                    if(c.physical!=null && c.physical!=physical) throw invalid(c.fecha,new IllegalStateException("lifecycle resource mismatch"));
                    c.lifecycle.add(n+":"+(a==null?"":Arrays.toString(a)));
                    if((n.equals("commit")&&fault.get()==Fault.COMMIT)||(n.equals("rollback")&&fault.get()==Fault.ROLLBACK))
                        throw new SQLException("injected completion failure","08006");
                }
                if(n.equals("createStatement")||n.equals("prepareCall")) throw new IllegalArgumentException("uncatalogued statement API");
                if(n.equals("getMetaData")) {
                    verify(c); DatabaseMetaData md=nativeConnection.getMetaData();
                    return Proxy.newProxyInstance(DatabaseMetaData.class.getClassLoader(),new Class<?>[]{DatabaseMetaData.class},(p,mm,aa)-> {
                        if(!Set.of("getURL","getUserName").contains(mm.getName())) throw new IllegalArgumentException("uncatalogued metadata");
                        verify(c); String value=String.valueOf(invoke(md,mm,aa));
                        if(fault.get()==Fault.METADATA) value+="_hostile";
                        c.metadata.add(new Metadata(mm.getName(),value,physical)); return value;
                    });
                }
                if(n.startsWith("prepareStatement")) {
                    verify(c); if(c.physical!=physical) throw invalid(null,new IllegalStateException("different connection"));
                    String sql=AdjustmentProjectionCatalog.normalize((String)a[0]);
                    String id=AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.logicalId(sql);
                    StatementObservation s=new StatementObservation(sql,id,physical); c.statements.add(s);
                    PreparedStatement ps=(PreparedStatement)invoke(nativeConnection,m,a);
                    return statement(ps,s,c);
                }
            }
            return invoke(nativeConnection,m,a);
        });
    }
    private PreparedStatement statement(PreparedStatement ps,StatementObservation s,Capture c) {
        return (PreparedStatement)Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),new Class<?>[]{PreparedStatement.class},(proxy,m,a)-> {
            String n=m.getName();
            if(n.equals("equals")) return proxy==a[0]; if(n.equals("hashCode")) return System.identityHashCode(proxy);
            if(n.startsWith("set")&&a!=null&&a.length>=2&&a[0] instanceof Integer) {
                verify(c); Object[] forwarded=a.clone(); Method setter=m;
                if(s.id.equals("R4_ADJUSTMENTS_ON_DATE_V1")) {
                    Fault f=fault.get(); int slot=(Integer)a[0];
                    if(f==Fault.OMITTED&&slot==2) return null;
                    if(f==Fault.WRONG_DATE&&slot==1) forwarded[1]=java.sql.Date.valueOf(((java.sql.Date)a[1]).toLocalDate().plusDays(1));
                    if(f==Fault.WRONG_SLOT&&slot==1) forwarded[0]=2;
                    if(f==Fault.SWAPPED) forwarded[0]=3-slot;
                    if(f==Fault.ACTIVE_FALSE&&slot==2) forwarded[1]=false;
                    if(f==Fault.WRONG_TYPE&&slot==1) {
                        setter=PreparedStatement.class.getMethod("setString",int.class,String.class);
                        forwarded=new Object[]{1,canonical(a[1])};
                    }
                }
                Bind bind=new Bind((Integer)forwarded[0],setter.getName(),forwarded[1]==null?"NULL":forwarded[1].getClass().getName(),
                        canonical(forwarded[1]),forwarded.length>2?canonical(forwarded[2]):null);
                s.setters.add(bind); s.binds.put(bind.slot(),bind);
                return invoke(ps,setter,forwarded);
            }
            if(n.startsWith("execute")) {
                verify(c); s.entered=true;
                try {
                    if((fault.get()==Fault.INITIAL_PROBE&&c.statements.size()==1)
                            ||(fault.get()==Fault.FINAL_PROBE&&c.statements.size()==17))
                        throw new SQLException("injected probe failure","57014");
                    if(s.id.equals("R4_ADJUSTMENTS_ON_DATE_V1")) {
                        verifyDataBindings(s,c.fecha);
                        Runnable barrier=beforeData; if(barrier!=null) barrier.run();
                    } else if(!s.setters.isEmpty()) throw new IllegalArgumentException("probe bind");
                    Object result=invoke(ps,m,a); s.completed=true; return result;
                } catch(Throwable e) { s.failed=true; throw e; }
            }
            return invoke(ps,m,a);
        });
    }
    public EntityManager wrapEntityManager(EntityManager em) {
        return (EntityManager)Proxy.newProxyInstance(EntityManager.class.getClassLoader(),new Class<?>[]{EntityManager.class,org.springframework.orm.jpa.EntityManagerProxy.class},(p,m,a)-> {
            Object result=invoke(em,m,a);
            if(m.getName().equals("createNativeQuery")&&result instanceof Query q) {
                String sql=(String)a[0];
                return Proxy.newProxyInstance(Query.class.getClassLoader(),new Class<?>[]{Query.class},(qp,qm,qa)-> {
                    if(qm.getName().equals("unwrap")&&qa[0]==NativeQuery.class) return nativeQuery(q.unwrap(NativeQuery.class),sql);
                    return invoke(q,qm,qa);
                });
            }
            return result;
        });
    }
    private NativeQuery<?> nativeQuery(NativeQuery<?> query,String sql) {
        return (NativeQuery<?>)Proxy.newProxyInstance(NativeQuery.class.getClassLoader(),new Class<?>[]{NativeQuery.class},(p,m,a)-> {
            Capture c=current.get(); require(c); verify(c);
            if(m.getName().equals("setParameter")&&a.length==3&&a[0] instanceof String name&&a[2] instanceof Class<?> type)
                c.named.add(new Named(sql,name,type.getName(),a[1].getClass().getName(),canonical(a[1])));
            Object result=invoke(query,m,a);
            if(m.getName().equals("getResultList") && fault.get()==Fault.SNAPSHOT && sql.contains("pg_current_snapshot")
                    && c.statements.size()>9) return List.of("hostile-snapshot");
            return result==query?p:result;
        });
    }
    public String expectedManifest(Capture c,LocalDate fecha,String url,String principal) {
        require(c); c.fecha=Objects.requireNonNull(fecha);
        List<String> parts=new ArrayList<>();
        for(String id:AdjustmentProjectionCatalog.manifest()) {
            parts.add(id); parts.add(AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.statement(id).positionalSql());
            parts.add(id.equals("R4_ADJUSTMENTS_ON_DATE_V1")?"1/DATE/"+fecha+"/2/BOOLEAN/true":"NO_BINDS");
        }
        parts.add("unwrapPGConnection/SAME_NATIVE_RESOURCE");parts.add("getURL/"+url);parts.add("getUserName/"+principal);
        parts.add("unwrapPGConnection/SAME_NATIVE_RESOURCE");parts.add("getURL/"+url);parts.add("getUserName/"+principal);
        return com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.decodificarUtf8(
                com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers.secuenciaTextos(parts.toArray(String[]::new)));
    }
    public void validate(Capture c,LocalDate fecha,String url,String principal) {
        verify(c);
        if(c.statements.size()!=17) throw database(fecha,new IllegalStateException("incomplete/extra statement manifest"));
        for(int i=0;i<17;i++) {
            var s=c.statements.get(i);
            if(!s.id.equals(AdjustmentProjectionCatalog.manifest().get(i))||s.physical!=c.physical
                    ||!s.entered||!s.completed||s.failed) throw database(fecha,new IllegalStateException("statement mismatch"));
            if(i==8) verifyDataBindings(s,fecha); else if(!s.setters.isEmpty()) throw database(fecha,new IllegalStateException("probe bindings"));
        }
        String dataSql=AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.statement("R4_ADJUSTMENTS_ON_DATE_V1").sql();
        if(!c.named.equals(List.of(new Named(dataSql,"fecha",LocalDate.class.getName(),LocalDate.class.getName(),fecha.toString()),
                new Named(dataSql,"active",Boolean.class.getName(),Boolean.class.getName(),"true"))))
            throw database(fecha,new IllegalStateException("native binding mismatch"));
        if(!c.metadata.equals(List.of(new Metadata("unwrapPGConnection","SAME_NATIVE_RESOURCE",c.physical),new Metadata("getURL",url,c.physical),new Metadata("getUserName",principal,c.physical),
                new Metadata("unwrapPGConnection","SAME_NATIVE_RESOURCE",c.physical),new Metadata("getURL",url,c.physical),new Metadata("getUserName",principal,c.physical))))
            throw invalid(fecha,new IllegalStateException("metadata/resource changed"));
    }
    public static void verifyDataBindings(StatementObservation s,LocalDate fecha) {
        if(s.binds.size()!=2||s.setters.size()!=2||!s.binds.containsKey(1)||!s.binds.containsKey(2))
            throw database(fecha,new IllegalStateException("missing/extra JDBC bind"));
        Bind date=s.binds.get(1),active=s.binds.get(2);
        boolean dateType=date.setter().equals("setDate")||date.setter().equals("setObject")&&"91".equals(date.jdbcType());
        boolean boolType=active.setter().equals("setBoolean")||active.setter().equals("setObject")&&"16".equals(active.jdbcType());
        if(!dateType||!boolType||fecha==null||!date.value().equals(fecha.toString())||!active.value().equals("true"))
            throw database(fecha,new IllegalStateException("actual JDBC DATE/BOOLEAN bind mismatch"));
    }
    private static Object invoke(Object target,Method m,Object[] a) throws Throwable {
        try { return m.invoke(target,a); } catch(InvocationTargetException e) { throw e.getCause(); }
    }
    private static String canonical(Object o) { return o instanceof java.sql.Date d?d.toLocalDate().toString():String.valueOf(o); }
    private static AdjustmentReadFailure invalid(LocalDate f,Throwable e) {
        return new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,f,List.of(),e);
    }
    private static AdjustmentReadFailure database(LocalDate f,Throwable e) {
        return new AdjustmentReadFailure(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,f,List.of(),e);
    }
    public record Bind(int slot,String setter,String runtimeType,String value,String jdbcType) { }
    public record Named(String sql,String name,String declaredType,String runtimeType,String value) { }
    public record Metadata(String method,String value,Object physical) { }
    public static final class StatementObservation {
        public final String sql,id; public final Object physical;
        public final Map<Integer,Bind> binds=new LinkedHashMap<>(); public final List<Bind> setters=new ArrayList<>();
        public boolean entered,completed,failed;
        StatementObservation(String sql,String id,Object physical) { this.sql=sql;this.id=id;this.physical=physical; }
    }
    public static final class Capture {
        public final String invocation; private final Thread thread;
        private boolean closed; public boolean success;
        private Object owner; private DataSource ds; private EntityManagerFactory factory; private EntityManager em;
        private EntityManagerHolder holder; private Session session; private Connection connection; private Object physical;
        private LocalDate fecha;
        public final List<StatementObservation> statements=new ArrayList<>();
        public final List<Named> named=new ArrayList<>(); public final List<Metadata> metadata=new ArrayList<>();
        public final List<String> lifecycle=new ArrayList<>();
        Capture(String id,Thread t) { invocation=id;thread=t; }
        public Object physical() { return physical; }
    }
}
