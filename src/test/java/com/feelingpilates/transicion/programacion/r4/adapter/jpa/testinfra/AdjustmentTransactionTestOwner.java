package com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.r4.adapter.jpa.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.postgresql.PGConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.UnaryOperator;

/** Outer publication boundary and separate proxied REQUIRES_NEW owner, test-only. */
public final class AdjustmentTransactionTestOwner {
    private final ApplicationContext application; private final AdjustmentJdbcCapture jdbc;
    public AdjustmentTransactionTestOwner(ApplicationContext application,AdjustmentJdbcCapture jdbc) {
        this.application=application;this.jdbc=jdbc;
    }
    public record Seed(String runIdentity,String attemptIdentity,ZoneId businessZone,String ruleCatalogVersion) {
        public static Seed fresh() { return new Seed(UUID.randomUUID().toString(),UUID.randomUUID().toString(),ZoneId.of("America/Mexico_City"),"test-rules-v1"); }
    }
    public Outcome read(Seed seed,LocalDate fecha) { return read(seed,fecha,UnaryOperator.identity(),Mode.NORMAL); }
    public enum Mode { NORMAL, REPLAY, DIRECT, ROLLBACK, NO_COMPLETION, EXTRA_SQL, AFTER_COMMIT }
    public Outcome read(Seed seed,LocalDate fecha,UnaryOperator<AdjustmentReadSnapshotContext> forge,Mode mode) {
        if(seed==null||fecha==null||seed.businessZone()==null||forge==null||mode==null)
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.INVALID_INPUT,fecha);
        for(String s:Arrays.asList(seed.runIdentity(),seed.attemptIdentity(),seed.ruleCatalogVersion())) {
            try { if(s==null||s.isBlank()) throw new IllegalArgumentException(); ReadSnapshotIdentifiers.secuenciaTextos(s); }
            catch(RuntimeException e) { throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.INVALID_INPUT,fecha,List.of(),e); }
        }
        var cap=jdbc.open(UUID.randomUUID().toString()); boolean success=false;
        try {
            Inner inner=application.getBean("adjustmentInnerOwner",Inner.class);
            if(!org.springframework.aop.support.AopUtils.isAopProxy(inner)) throw invalid(fecha,null);
            Outcome provisional=inner.execute(seed,fecha,forge,mode,cap);
            if(!provisional.completion().committed||mode==Mode.NO_COMPLETION
                    ||!Objects.equals(provisional.completion().signature,jdbc.observationSignature(cap))) throw invalid(fecha,null);
            success=true; return provisional;
        } catch(AdjustmentReadFailure e) {
            if(e.fecha()==null) throw new AdjustmentReadFailure(e.category(),fecha,e.physicalIds(),e.getCause());
            throw e;
        }
        catch(RuntimeException e) { throw invalid(fecha,e); }
        finally { jdbc.close(cap,success); }
    }
    public record Outcome(AdjustmentReadSet set,AdjustmentReadSnapshotContext context,Completion completion) { }
    public static final class Completion { public boolean committed; public String signature; }
    public static final class Registry implements AdjustmentJpaReader.ContextAuthority {
        private final ThreadLocal<Reservation> current=new ThreadLocal<>();
        private void bind(Reservation r) { if(current.get()!=null) throw invalid(r.context.fecha(),null); current.set(r); }
        private void clear() { current.remove(); }
        @Override public void verify(AdjustmentReadSnapshotContext c,LocalDate fecha) {
            Reservation r=current.get();
            if(r==null||r.context!=c||r.used||r.owner==null||r.manager!=r.descriptor.manager()
                    ||!c.fecha().equals(fecha)||!TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                    ||!Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),Connection.TRANSACTION_REPEATABLE_READ))
                throw invalid(fecha,null);
            r.jdbc.verify(r.capture); r.used=true;
        }
    }
    private static final class Reservation {
        final AdjustmentReadSnapshotContext context;final Inner owner;final JpaTransactionManager manager;
        final Descriptor descriptor;final AdjustmentJdbcCapture jdbc;final AdjustmentJdbcCapture.Capture capture;boolean used;
        Reservation(AdjustmentReadSnapshotContext c,Inner o,Descriptor d,AdjustmentJdbcCapture j,AdjustmentJdbcCapture.Capture cap) {
            context=c;owner=o;manager=d.manager();descriptor=d;jdbc=j;capture=cap;
        }
    }
    public record Descriptor(String sourceName,String url,String database,String principal,DataSource ds,
                             EntityManagerFactory factory,EntityManager em,JpaTransactionManager manager,
                             List<List<List<String>>> schemaRows) {
        public Descriptor { schemaRows=schemaRows.stream().map(x->x.stream().map(List::copyOf).toList()).toList(); }
        public String fingerprint() { return schemaFingerprint(schemaRows); }
    }
    public static class Inner {
        private final AdjustmentReadPort reader;private final AdjustmentProjectionQueryExecutor executor;
        private final Descriptor descriptor;private final Registry registry;private final AdjustmentJdbcCapture jdbc;
        public Inner(AdjustmentReadPort reader,AdjustmentProjectionQueryExecutor executor,Descriptor descriptor,Registry registry,AdjustmentJdbcCapture jdbc) {
            this.reader=reader;this.executor=executor;this.descriptor=descriptor;this.registry=registry;this.jdbc=jdbc;
        }
        @Transactional(transactionManager="f2eReaderTransactionManager",propagation=Propagation.REQUIRES_NEW,
                isolation=Isolation.REPEATABLE_READ,readOnly=true,timeout=45)
        public Outcome execute(Seed seed,LocalDate fecha,UnaryOperator<AdjustmentReadSnapshotContext> forge,Mode mode,AdjustmentJdbcCapture.Capture cap) {
            if(!TransactionSynchronizationManager.isActualTransactionActive()||!TransactionSynchronizationManager.isSynchronizationActive()
                    ||!TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                    ||!Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),Connection.TRANSACTION_REPEATABLE_READ)
                    ||descriptor.manager().getEntityManagerFactory()!=descriptor.factory()||descriptor.manager().getDataSource()!=descriptor.ds())
                throw invalid(fecha,null);
            Object holder=TransactionSynchronizationManager.getResource(descriptor.factory());
            if(!(holder instanceof EntityManagerHolder h)) throw invalid(fecha,null);
            Session session=h.getEntityManager().unwrap(Session.class);
            Connection connection=session.doReturningWork(c->c);
            try { jdbc.bind(cap,this,descriptor.ds(),descriptor.factory(),descriptor.em(),h,session,connection); }
            catch(SQLException e) { throw invalid(fecha,e); }
            Completion completion=new Completion(); final boolean[] ready={false};
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void beforeCommit(boolean readOnly) {
                    if(!ready[0]||!readOnly) throw invalid(fecha,null);
                    jdbc.validate(cap,fecha,descriptor.url(),descriptor.principal());
                }
                @Override public void afterCommit() {
                    if(mode==Mode.AFTER_COMMIT) executor.probe("R4_TX_SNAPSHOT_V1");
                }
                @Override public void afterCompletion(int status) {
                    completion.committed=status==STATUS_COMMITTED&&ready[0]; registry.clear();
                }
            });
            try {
                metadata(connection,fecha); Probe initial=probe(fecha);
                String physical=UUID.randomUUID().toString();
                String evidence=AdjustmentReadSnapshotContext.hash("F2E-R4-SNAPSHOT-EVIDENCE-V1",cap.invocation,
                        seed.runIdentity(),seed.attemptIdentity(),"R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/"+fecha,
                        "f2eReaderTransactionManager",physical,descriptor.database(),"public",descriptor.principal(),
                        descriptor.fingerprint(),"repeatable read","on",initial.snapshot());
                String expected=jdbc.expectedManifest(cap,fecha,descriptor.url(),descriptor.principal());
                String commitment=AdjustmentReadSnapshotContext.hash("F2E-R4-STATEMENT-COMMITMENT-V1",cap.invocation,"R4_ADJUSTMENT_V1",expected);
                var context=new AdjustmentReadSnapshotContext(seed.runIdentity(),seed.attemptIdentity(),fecha,seed.businessZone(),
                        seed.ruleCatalogVersion(),descriptor.sourceName(),descriptor.fingerprint(),descriptor.database(),"public",
                        descriptor.principal(),physical,AdjustmentReadSnapshotContext.ProjectionCatalogVersion.R4_ADJUSTMENT_V1,
                        cap.invocation,AdjustmentReadSnapshotContext.SnapshotClaim.R4_INTERNAL_RR_TEST,evidence,commitment);
                registry.bind(new Reservation(context,this,descriptor,jdbc,cap));
                AdjustmentReadSet set;
                if(mode==Mode.DIRECT) {
                    var raw=new AdjustmentJpaReader(executor,new com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper.AdjustmentProjectionMapper(),
                            AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1,registry);
                    set=raw.readActiveAdjustmentsOnDate(context,fecha);
                } else set=reader.readActiveAdjustmentsOnDate(forge.apply(context),fecha);
                if(mode==Mode.REPLAY) reader.readActiveAdjustmentsOnDate(context,fecha);
                if(mode==Mode.EXTRA_SQL) executor.probe("UNKNOWN_SQL");
                Probe fin=probe(fecha); metadata(connection,fecha);
                if(!initial.equals(fin)) throw invalid(fecha,new IllegalStateException("snapshot/transaction descriptor drift"));
                jdbc.validate(cap,fecha,descriptor.url(),descriptor.principal());
                if(!commitment.equals(AdjustmentReadSnapshotContext.hash("F2E-R4-STATEMENT-COMMITMENT-V1",cap.invocation,"R4_ADJUSTMENT_V1",
                        jdbc.expectedManifest(cap,fecha,descriptor.url(),descriptor.principal())))) throw invalid(fecha,null);
                if(mode==Mode.ROLLBACK) { org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); }
                completion.signature=jdbc.observationSignature(cap);ready[0]=true; return new Outcome(set,context,completion);
            } catch(AdjustmentReadFailure e) { throw e; }
            catch(RuntimeException e) { throw database(fecha,e); }
            finally { registry.clear(); }
        }
        private void metadata(Connection connection,LocalDate fecha) {
            try { jdbc.observeMetadata(connection,descriptor.url(),descriptor.principal()); }
            catch(SQLException e) { throw database(fecha,e); }
        }
        private Probe probe(LocalDate fecha) {
            String isolation=scalar("R4_TX_ISOLATION_V1"),readOnly=scalar("R4_TX_READ_ONLY_V1");
            List<?> resources=executor.probe("R4_TX_RESOURCE_V1");
            if(resources.size()!=1||!(resources.getFirst() instanceof Object[] resource)||resource.length!=3)
                throw invalid(fecha,null);
            String snapshot=scalar("R4_TX_SNAPSHOT_V1");
            List<List<List<String>>> schema=new ArrayList<>();
            for(String id:schemaIds()) schema.add(canonicalRows(executor.probe(id)));
            if(!schema.equals(descriptor.schemaRows())) throw database(fecha,new IllegalStateException("canonical schema/history mismatch"));
            if(!isolation.equals("repeatable read")||!readOnly.equals("on")||!resource[0].equals(descriptor.database())
                    ||!resource[1].equals("public")||!resource[2].equals(descriptor.principal())) throw invalid(fecha,null);
            return new Probe(isolation,readOnly,Arrays.stream(resource).map(String::valueOf).toList(),snapshot,schemaFingerprint(schema));
        }
        private String scalar(String id) {
            List<?> r=executor.probe(id);
            if(r.size()!=1||!(r.getFirst() instanceof String s)||s.isBlank()) throw invalid(null,null);
            return s;
        }
    }
    private record Probe(String isolation,String readOnly,List<String> resource,String snapshot,String fingerprint) { }
    public static List<String> schemaIds() { return List.of("R4_SCHEMA_HISTORY_V1","R4_SCHEMA_COLUMNS_V1","R4_SCHEMA_CONSTRAINTS_V1","R4_SCHEMA_INDEXES_V1"); }
    public static List<List<String>> canonicalRows(List<?> rows) {
        List<List<String>> out=new ArrayList<>();
        for(Object row:rows) {
            Object[] scalars=row instanceof Object[] a?a:new Object[]{row};
            out.add(Arrays.stream(scalars).map(AdjustmentTransactionTestOwner::typedScalar).toList());
        }
        return List.copyOf(out);
    }
    public static String typedScalar(Object o) {
        if(o==null) return "SQL_NULL";
        if(o instanceof String s) return "TEXT/"+s;
        if(o instanceof Boolean b) return "BOOLEAN/"+b;
        if(o instanceof Number n) return "INTEGER/"+n;
        throw new IllegalArgumentException("unexpected schema scalar "+o.getClass());
    }
    public static String schemaFingerprint(List<List<List<String>>> sets) {
        List<String> fields=new ArrayList<>();fields.add("F2E-R4-SCHEMA-V1");
        for(int i=0;i<sets.size();i++) {
            fields.add(schemaIds().get(i));fields.add(Integer.toString(sets.get(i).size()));
            for(var row:sets.get(i)) { fields.add(Integer.toString(row.size()));fields.addAll(row); }
        }
        return "sha256:"+AdjustmentReadSnapshotContext.hash(fields.toArray(String[]::new));
    }
    private static AdjustmentReadFailure invalid(LocalDate f,Throwable e) { return new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,f,List.of(),e); }
    private static AdjustmentReadFailure database(LocalDate f,Throwable e) { return new AdjustmentReadFailure(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,f,List.of(),e); }
}
