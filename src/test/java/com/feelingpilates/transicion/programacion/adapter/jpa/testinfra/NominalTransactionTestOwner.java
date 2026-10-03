package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.NominalJpaReader;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.read.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.postgresql.PGConnection;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.UnaryOperator;

/** Individual test-only owner. No multi-reader or productive routing authority. */
public class NominalTransactionTestOwner {
    private final NominalProgrammingReadPort reader;
    private final NominalProjectionQueryExecutor executor;
    private final F2eStatementPolicyInspector inspector;
    private final JdbcCapture jdbc;
    private final ContextRegistry contexts;
    private final NominalPostgresTestConfiguration.Descriptor descriptor;
    private final ConcurrentHashMap<String,String> reservations = new ConcurrentHashMap<>();
    public NominalTransactionTestOwner(NominalProgrammingReadPort reader, NominalProjectionQueryExecutor executor,
            F2eStatementPolicyInspector inspector, JdbcCapture jdbc, ContextRegistry contexts,
            NominalPostgresTestConfiguration.Descriptor descriptor) {
        this.reader=reader; this.executor=executor; this.inspector=inspector; this.jdbc=jdbc;
        this.contexts=contexts; this.descriptor=descriptor;
        validateDescriptor();
    }
    @Transactional(transactionManager="f2eReaderTransactionManager", propagation=Propagation.REQUIRES_NEW,
            isolation=Isolation.REPEATABLE_READ, readOnly=true, timeout=45)
    public Outcome inRepeatableReadOnly(Seed seed, LocalDate fecha) {
        return execute(seed, fecha, UnaryOperator.identity(), Fault.NONE);
    }
    /** Negative test seam; never grants a substitute context owner authority. */
    @Transactional(transactionManager="f2eReaderTransactionManager", propagation=Propagation.REQUIRES_NEW,
            isolation=Isolation.REPEATABLE_READ, readOnly=true, timeout=45)
    public Outcome negative(Seed seed, LocalDate fecha, UnaryOperator<NominalReadSnapshotContext> forge, Fault fault) {
        return execute(seed, fecha, forge, fault);
    }
    public enum Fault { NONE, RESOURCE, SNAPSHOT, COMPLETION, ROLLBACK, EXTRA_SQL, EXTRA_JDBC, DIRECT_READER, SECOND_READ }
    private Outcome execute(Seed seed, LocalDate fecha, UnaryOperator<NominalReadSnapshotContext> forge, Fault fault) {
        validateSeed(seed,fecha);
        validateDescriptor();
        TransactionReference tx = transaction();
        ConnectionReference conn = connection();
        String key=NominalReadSnapshotContext.hash("F2E-R3-RESERVATION-V1",descriptor.fixtureIdentity(),
                seed.runIdentity(),seed.attemptIdentity());
        if (reservations.putIfAbsent(key,"ACTIVE") != null) throw invalid(fecha,null);
        Completion completion=new Completion();
        String invocation=UUID.randomUUID().toString();
        var sql=inspector.abrirCaptura(invocation);
        var cap=jdbc.open(invocation);
        AtomicBoolean ready=new AtomicBoolean();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try {
                    if (fault == Fault.COMPLETION) executor.consultarAislamiento();
                    if (!ready.get() || !jdbc.isOpen(cap)
                            || TransactionSynchronizationManager.getResource(descriptor.entityManagerFactory())!=tx.holder()
                            || !completion.statements.equals(inspector.observarCaptura(sql))
                            || !completion.observations.equals(jdbc.snapshot(cap))) throw invalid(fecha,null);
                    completion.commitGuardPassed=true;
                } catch (RuntimeException e) {
                    if(e instanceof NominalReadFailure f) throw f;
                    throw invalid(fecha,e);
                }
            }
            @Override public void afterCompletion(int status) {
                try {
                    if (jdbc.isOpen(cap)) {
                        completion.completed=status==STATUS_COMMITTED && ready.get() && completion.commitGuardPassed
                                && completion.statements.equals(inspector.cerrarCaptura(sql))
                                && completion.observations.equals(jdbc.close(cap));
                    }
                } finally {
                    inspector.descartarCaptura(sql); jdbc.discard(cap); contexts.clear();
                    reservations.put(key,completion.completed?"SUCCESS":"ABORTED");
                }
            }
        });
        try {
            jdbc.bindResource(cap,this,descriptor.entityManagerFactory(),tx.holder(),tx.session(),conn.physical());
            validateMetadata(conn, cap);
            Probe initial=probe();
            String physical=UUID.randomUUID().toString();
            String evidence=NominalReadSnapshotContext.hash("F2E-R3-SNAPSHOT-EVIDENCE-V1",key,invocation,
                    descriptor.sourceName(),descriptor.schemaFingerprint(),descriptor.databaseName(),
                    descriptor.schemaName(),descriptor.credentialPrincipal(),physical,
                    "f2eReaderTransactionManager", "repeatable read", "on", initial.snapshot());
            String commitment=NominalReadSnapshotContext.hash("F2E-R3-CAPTURE-COMMITMENT-V1",evidence,invocation,
                    String.join("/",NominalProjectionCatalog.expectedManifest()),fecha.toString(),
                    Short.toString((short)(fecha.getDayOfWeek().getValue()%7)));
            var trusted=new NominalReadSnapshotContext(seed.runIdentity(),seed.attemptIdentity(),fecha,
                    seed.businessZone(),seed.ruleCatalogVersion(),descriptor.sourceName(),descriptor.schemaFingerprint(),
                    descriptor.databaseName(),descriptor.schemaName(),descriptor.credentialPrincipal(),physical,
                    NominalReadSnapshotContext.ProjectionCatalogVersion.R3_NOMINAL_V1,invocation,
                    NominalReadSnapshotContext.SnapshotClaim.R3_INTERNAL_RR_TEST,evidence,commitment);
            contexts.bind(trusted,tx,conn,descriptor,jdbc,cap,initial);
            if (fault==Fault.RESOURCE) contexts.replacePhysicalForNegative();
            NominalProgrammingReadSet set;
            if (fault==Fault.DIRECT_READER) {
                var direct=new NominalJpaReader(executor,
                        new com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper(
                                NominalProjectionCatalog.R3_NOMINAL_V1),NominalProjectionCatalog.R3_NOMINAL_V1,contexts);
                set=direct.readNominalOnDate(trusted,fecha);
            } else set=reader.readNominalOnDate(forge.apply(trusted),fecha);
            if (fault==Fault.SECOND_READ) reader.readNominalOnDate(trusted,fecha);
            contexts.clear();
            if (fault==Fault.EXTRA_SQL) descriptor.entityManager().createNativeQuery("SELECT 1").getResultList();
            if (fault==Fault.EXTRA_JDBC) tx.session().doWork(c -> c.createStatement().execute("SELECT 1"));
            Probe fin=probe();
            if (fault==Fault.SNAPSHOT) fin=new Probe(fin.isolation(),fin.readOnly(),fin.resource(),fin.snapshot()+"/forged");
            if (!initial.equals(fin)) throw invalid(fecha,null);
            TransactionReference endTx=transaction(); ConnectionReference endConn=connection();
            validateMetadata(endConn,cap);
            if (tx.holder()!=endTx.holder() || tx.session()!=endTx.session()
                    || conn.connection()!=endConn.connection() || conn.physical()!=endConn.physical()) throw invalid(fecha,null);
            List<String> ids=inspector.observarCaptura(sql); var observed=jdbc.snapshot(cap);
            if (!ids.equals(NominalProjectionCatalog.expectedManifest()) || ids.size()!=observed.size()) throw invalid(fecha,null);
            for(int i=0;i<observed.size();i++) {
                var o=observed.get(i);
                if (o.ordinal()!=i+1 || !F2eStatementPolicyInspector.identificar(
                        F2eStatementPolicyInspector.normalizar(o.sql())).equals(ids.get(i))
                        || !o.executeEntered() || !o.executeCompleted() || o.executeFailed()
                        || o.transactionOwner()!=this || o.factory()!=descriptor.entityManagerFactory()
                        || o.holder()!=tx.holder() || o.session()!=tx.session() || o.connection()!=conn.connection()
                        || o.physicalConnection()!=conn.physical()) throw invalid(fecha,null);
            }
            assertJdbcBinding(observed.get(4),fecha);
            String observedCommitment=NominalReadSnapshotContext.hash("F2E-R3-CAPTURE-COMMITMENT-V1",evidence,invocation,
                    String.join("/",ids),fecha.toString(),Short.toString((short)(fecha.getDayOfWeek().getValue()%7)));
            if (!trusted.statementCaptureCommitment().equals(observedCommitment)) throw invalid(fecha,null);
            var metadata=jdbc.metadata(cap);
            if (metadata.size()!=4 || metadata.stream().anyMatch(m->m.physicalConnection()!=conn.physical())) throw invalid(fecha,null);
            completion.statements=ids; completion.observations=observed;
            ready.set(true);
            if(fault==Fault.ROLLBACK) org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            return new Outcome(set,trusted,initial.snapshot(),fin.snapshot(),ids,observed,metadata,
                    conn.physical(),tx.holder(),tx.session(),completion);
        } catch (RuntimeException e) {
            contexts.clear(); inspector.descartarCaptura(sql); jdbc.discard(cap);
            if(e instanceof NominalReadFailure f) throw f;
            throw new NominalReadFailure(NominalReadFailure.Category.DATABASE_READ_FAILURE,fecha,List.of(),e);
        }
    }
    /** Independent expectation, not inferred from observed setter calls. */
    public static void assertJdbcBinding(JdbcCapture.StatementObservation o, LocalDate fecha) {
        if (!o.sql().equals(NominalProjectionCatalog.positionalSql()) || o.binds().size()!=7
                || o.setterHistory().size()!=7 || !o.executeCompleted() || o.executeFailed())
            throw invalid(fecha,null);
        for(int slot=1;slot<=7;slot++) {
            var b=o.binds().get(slot);
            String method=slot<=2?"setBoolean":slot<=6?"setDate":"setShort";
            String type=slot<=2?"java.lang.Boolean":slot<=6?"java.sql.Date":"java.lang.Short";
            String value=slot<=2?"true":slot<=6?fecha.toString():Short.toString((short)(fecha.getDayOfWeek().getValue()%7));
            if (b==null || b.position()!=slot || !b.setter().equals(method) || !b.runtimeClass().equals(type)
                    || !b.canonicalValue().equals(value) || b.jdbcTypeArgument()!=null
                    || !o.setterHistory().contains(b)) throw invalid(fecha,null);
        }
    }
    private Probe probe() {
        String isolation=executor.consultarAislamiento(); String readOnly=executor.consultarSoloLectura();
        var resource=executor.consultarIdentidadRecurso(); String snapshot=executor.consultarSnapshot();
        if (!isolation.equals("repeatable read") || !readOnly.equals("on")
                || !resource.databaseName().equals(descriptor.databaseName())
                || !resource.schemaName().equals(descriptor.schemaName())) throw invalid(null,null);
        return new Probe(isolation,readOnly,resource,snapshot);
    }
    private TransactionReference transaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),
                        TransactionDefinition.ISOLATION_REPEATABLE_READ)) throw invalid(null,null);
        try {
            Object proxy=org.springframework.aop.framework.AopContext.currentProxy();
            if (!(proxy instanceof NominalTransactionTestOwner)
                    || !(proxy instanceof org.springframework.aop.framework.Advised advised)
                    || advised.getTargetSource().getTarget()!=this) throw invalid(null,null);
        } catch (NominalReadFailure e) { throw e; }
        catch (Exception e) { throw invalid(null,e); }
        Object r=TransactionSynchronizationManager.getResource(descriptor.entityManagerFactory());
        if (!(r instanceof EntityManagerHolder holder)) throw invalid(null,null);
        Session session=holder.getEntityManager().unwrap(Session.class);
        if (!session.isJoinedToTransaction() || !session.isDefaultReadOnly()
                || session.getHibernateFlushMode()!=FlushMode.MANUAL || !descriptor.entityManager().isJoinedToTransaction())
            throw invalid(null,null);
        if (!(TransactionSynchronizationManager.getResource(descriptor.readerDataSource())
                instanceof org.springframework.jdbc.datasource.ConnectionHolder ch)
               ) throw invalid(null,null);
        session.doWork(c -> {
            if (ch.getConnection() != c || ch.getConnection().unwrap(PGConnection.class) != c.unwrap(PGConnection.class))
                throw invalid(null,null);
        });
        return new TransactionReference(holder,session);
    }
    private ConnectionReference connection() {
        return descriptor.entityManager().unwrap(Session.class).doReturningWork(c->new ConnectionReference(c,c.unwrap(PGConnection.class)));
    }
    private void validateMetadata(ConnectionReference c, JdbcCapture.Capture cap) {
        try {
            if (!(c.physical() instanceof org.postgresql.jdbc.PgConnection)) throw invalid(null,null);
            jdbc.verifyBoundResource(cap,c.physical());
            String url=c.connection().getMetaData().getURL(), principal=c.connection().getMetaData().getUserName();
            if(!url.equals(descriptor.canonicalJdbcUrl()) || !principal.equals(descriptor.credentialPrincipal())) throw invalid(null,null);
        } catch(SQLException e) { throw invalid(null,e); }
    }
    private void validateDescriptor() {
        if(!descriptor.fixtureIdentity().equals("fixture-r3-"+descriptor.fixtureKey())
                || !descriptor.sourceName().equals("fixture:postgres16:r3:"+descriptor.fixtureKey())
                || !descriptor.schemaFingerprint().matches("sha256:[0-9a-f]{64}")
                || descriptor.transactionManager().getEntityManagerFactory()!=descriptor.entityManagerFactory()
                || descriptor.transactionManager().getDataSource()!=descriptor.readerDataSource()
                || descriptor.inspector()!=inspector) throw invalid(null,null);
    }
    private void validateSeed(Seed s, LocalDate fecha) {
        try {
            if(s==null || fecha==null || s.businessZone()==null) throw new IllegalArgumentException();
            for(String text:List.of(s.runIdentity(),s.attemptIdentity(),s.ruleCatalogVersion())) {
                if(text.isBlank() || text.indexOf('\0')>=0) throw new IllegalArgumentException();
                ReadSnapshotIdentifiers.secuenciaTextos(text);
            }
        } catch(RuntimeException e) { throw new NominalReadFailure(NominalReadFailure.Category.INVALID_INPUT,fecha,List.of(),e); }
    }
    private static NominalReadFailure invalid(LocalDate fecha,Throwable cause) {
        return new NominalReadFailure(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,fecha,List.of(),cause);
    }
    public String state(Seed s) {
        return reservations.get(NominalReadSnapshotContext.hash("F2E-R3-RESERVATION-V1",descriptor.fixtureIdentity(),s.runIdentity(),s.attemptIdentity()));
    }
    public record Seed(String runIdentity,String attemptIdentity,String ruleCatalogVersion,ZoneId businessZone) { }
    private record Probe(String isolation,String readOnly,NominalProjectionQueryExecutor.ResourceIdentity resource,String snapshot) { }
    private record TransactionReference(EntityManagerHolder holder,Session session) { }
    private record ConnectionReference(Connection connection,Object physical) { }
    private static final class Completion {
        volatile boolean completed;
        boolean commitGuardPassed;
        List<String> statements=List.of(); List<JdbcCapture.StatementObservation> observations=List.of();
    }
    public static final class Outcome {
        private final NominalProgrammingReadSet provisional;
        private final Completion completion;
        public final NominalReadSnapshotContext context;
        public final String snapshotInitial,snapshotFinal;
        public final List<String> statementIds;
        public final List<JdbcCapture.StatementObservation> jdbcObservations;
        public final List<JdbcCapture.MetadataObservation> metadataObservations;
        public final Object physicalConnection;
        public final EntityManagerHolder holder;
        public final Session session;
        private Outcome(NominalProgrammingReadSet set,NominalReadSnapshotContext context,String initial,String fin,
                List<String> ids,List<JdbcCapture.StatementObservation> observations,List<JdbcCapture.MetadataObservation> metadata,
                Object physical,EntityManagerHolder holder,Session session,Completion completion) {
            this.provisional=set;this.context=context;this.snapshotInitial=initial;this.snapshotFinal=fin;
            this.statementIds=List.copyOf(ids);this.jdbcObservations=List.copyOf(observations);
            this.metadataObservations=List.copyOf(metadata);this.physicalConnection=physical;
            this.holder=holder;this.session=session;this.completion=completion;
        }
        public NominalProgrammingReadSet readSet() {
            if(!completion.completed) throw invalid(context.fecha(),null);
            return provisional;
        }
        public boolean completed() { return completion.completed; }
    }
    public static final class ContextRegistry implements NominalJpaReader.ContextAuthority {
        private final ThreadLocal<Binding> current=new ThreadLocal<>();
        private void bind(NominalReadSnapshotContext context,TransactionReference tx,ConnectionReference conn,
                NominalPostgresTestConfiguration.Descriptor descriptor,JdbcCapture jdbc,JdbcCapture.Capture capture,Probe probe) {
            if(current.get()!=null) throw invalid(context.fecha(),null);
            current.set(new Binding(context,tx,conn,descriptor,jdbc,capture,probe,false));
        }
        private void clear() { current.remove(); }
        private void replacePhysicalForNegative() {
            Binding b=current.get();current.set(new Binding(b.context,b.tx,
                    new ConnectionReference(b.conn.connection,new Object()),b.descriptor,b.jdbc,b.capture,b.probe,false));
        }
        @Override public void verify(NominalReadSnapshotContext context,LocalDate fecha) {
            Binding b=current.get();
            if(b==null || b.context!=context || b.consumed || !context.fecha().equals(fecha)
                    || TransactionSynchronizationManager.getResource(b.descriptor.entityManagerFactory())!=b.tx.holder
                    || b.tx.holder.getEntityManager().unwrap(Session.class)!=b.tx.session) throw invalid(fecha,null);
            Object proxy;
            try { proxy=org.springframework.aop.framework.AopContext.currentProxy(); }
            catch(IllegalStateException e) { throw invalid(fecha,e); }
            if(!(proxy instanceof NominalProgrammingReadPort)) throw invalid(fecha,null);
            b.tx.session.doWork(c->{
                if(c!=b.conn.connection || c.unwrap(PGConnection.class)!=b.conn.physical) throw invalid(fecha,null);
                b.jdbc.verifyBoundResource(b.capture,b.conn.physical);
            });
            if(!context.sourceName().equals(b.descriptor.sourceName())
                    || !context.schemaFingerprint().equals(b.descriptor.schemaFingerprint())
                    || !context.databaseName().equals(b.probe.resource.databaseName())
                    || !context.schemaName().equals(b.probe.resource.schemaName())
                    || !context.principal().equals(b.descriptor.credentialPrincipal())
                    || !context.readerInvocationIdentity().equals(b.capture.invocationIdentity())) throw invalid(fecha,null);
            current.set(new Binding(b.context,b.tx,b.conn,b.descriptor,b.jdbc,b.capture,b.probe,true));
        }
        private record Binding(NominalReadSnapshotContext context,TransactionReference tx,ConnectionReference conn,
                NominalPostgresTestConfiguration.Descriptor descriptor,JdbcCapture jdbc,JdbcCapture.Capture capture,
                Probe probe,boolean consumed) { }
    }
public static final class JdbcCapture {

    private final ThreadLocal<Capture> current = new ThreadLocal<>();
    private volatile Barrier dataBarrier;
    private final ThreadLocal<BindingFault> bindingFault = new ThreadLocal<>();
    private final ThreadLocal<List<StatementObservation>> lastDiscarded = new ThreadLocal<>();
    public enum BindingFault { WRONG_DATE, WRONG_SLOT, WRONG_DAY, WRONG_TYPE, OMIT, SWAP }
    public void installBindingFault(BindingFault fault) { bindingFault.set(Objects.requireNonNull(fault)); }
    public void clearBindingFault() { bindingFault.remove(); }
    public List<StatementObservation> failureObservations() {
        return lastDiscarded.get() == null ? List.of() : lastDiscarded.get();
    }

    public DataSource wrap(DataSource delegate) {
        Objects.requireNonNull(delegate, "delegate");
        return new DelegatingDataSource(delegate) {
            @Override
            public Connection getConnection() throws SQLException {
                return wrapConnection(super.getConnection());
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return wrapConnection(super.getConnection(username, password));
            }
        };
    }

    public Capture open(String invocationIdentity) {
        if (invocationIdentity == null || invocationIdentity.isBlank() || current.get() != null) {
            throw new IllegalStateException("R3 JDBC capture cannot be nested or anonymous");
        }
        Capture capture = new Capture(invocationIdentity, Thread.currentThread());
        current.set(capture);
        return capture;
    }

    public void bindResource(Capture capture, Object transactionOwner, EntityManagerFactory factory,
                             EntityManagerHolder holder, Session session, Object physical) {
        requireOpen(capture);
        capture.transactionOwner = Objects.requireNonNull(transactionOwner, "transactionOwner");
        capture.factory = Objects.requireNonNull(factory, "factory");
        capture.holder = Objects.requireNonNull(holder, "holder");
        capture.session = Objects.requireNonNull(session, "session");
        capture.physical = Objects.requireNonNull(physical, "physical");
        verifyResource(capture, physical);
    }

    public List<StatementObservation> snapshot(Capture capture) {
        requireOpen(capture);
        return capture.observations.stream().map(StatementObservationMutable::snapshot).toList();
    }

    public boolean isOpen(Capture capture) {
        return current.get() == capture && !capture.closed && capture.owner == Thread.currentThread();
    }

    public void verifyBoundResource(Capture capture, Object observedPhysical) {
        verifyResource(capture, observedPhysical);
    }

    public List<StatementObservation> close(Capture capture) {
        requireOpen(capture);
        capture.closed = true;
        current.remove();
        return capture.observations.stream().map(StatementObservationMutable::snapshot).toList();
    }

    private void requireOpen(Capture capture) {
        if (!isOpen(capture)) throw new IllegalStateException("R3 JDBC capture ownership not proven");
    }

    private void verifyResource(Capture capture, Object physical) {
        requireOpen(capture);
        if (capture.factory == null || capture.holder == null || capture.session == null
                || capture.physical != physical
                || TransactionSynchronizationManager.getResource(capture.factory) != capture.holder
                || capture.holder.getEntityManager().unwrap(Session.class) != capture.session
                || !capture.session.isJoinedToTransaction()) {
            throw new IllegalStateException("R3 statement transaction resource chain changed");
        }
    }

    public void discard(Capture capture) {
        if (current.get() == capture) {
            lastDiscarded.set(capture.observations.stream().map(StatementObservationMutable::snapshot).toList());
            capture.closed = true;
            current.remove();
        }
    }

    public void installDataBarrier(CountDownLatch membersObserved, CountDownLatch continueAssignments) {
        dataBarrier = new Barrier(membersObserved, continueAssignments);
    }

    public void clearBarrier() {
        dataBarrier = null;
    }

    private Connection wrapConnection(Connection delegate) throws SQLException {
        Object physical = delegate.unwrap(PGConnection.class);
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, arguments) -> {
                    if (current.get() != null && (method.getName().equals("createStatement")
                            || method.getName().equals("prepareCall")))
                        throw new IllegalStateException("R3 non-prepared or callable SQL denied");
                    if (method.getName().startsWith("prepareStatement") && arguments != null
                            && arguments.length > 0 && arguments[0] instanceof String sql) {
                        Capture capture = current.get();
                        if (capture != null) {
                            verifyResource(capture, physical);
                            String id = F2eStatementPolicyInspector.identificar(F2eStatementPolicyInspector.normalizar(sql));
                            if (!NominalProjectionCatalog.R3_NOMINAL_V1.statementIdsToLogicalIds().containsKey(id))
                                throw new IllegalStateException("R3 JDBC catalog miss");
                        }
                        PreparedStatement statement = (PreparedStatement) invoke(delegate, method, arguments);
                        if (capture == null) return statement;
                        verifyResource(capture, physical);
                        StatementObservationMutable observation = new StatementObservationMutable(
                                capture.observations.size() + 1, sql, (Connection) proxy, physical, capture);
                        capture.observations.add(observation);
                        verifyResource(capture, physical);
                        return wrapStatement(statement, observation, capture);
                    }
                    if ("getMetaData".equals(method.getName()) && current.get() != null) {
                        Capture cap = current.get(); verifyResource(cap, physical);
                        var md = delegate.getMetaData();
                        return Proxy.newProxyInstance(java.sql.DatabaseMetaData.class.getClassLoader(),
                                new Class<?>[]{java.sql.DatabaseMetaData.class}, (mproxy, mm, ma) -> {
                                    verifyResource(cap, physical);
                                    if (!List.of("getURL", "getUserName").contains(mm.getName()))
                                        throw new IllegalStateException("R3 uncataloged metadata traversal");
                                    Object result = invoke(md, mm, ma);
                                    cap.metadata.add(new MetadataObservation(mm.getName(), String.valueOf(result), physical));
                                    return result;
                                });
                    }
                    if ("equals".equals(method.getName())) return proxy == arguments[0];
                    if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                    return invoke(delegate, method, arguments);
                });
    }

    private PreparedStatement wrapStatement(
            PreparedStatement delegate, StatementObservationMutable observation, Capture capture) {
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, (proxy, method, arguments) -> {
                    String name = method.getName();
                    if (name.startsWith("set") && arguments != null && arguments.length >= 2
                            && arguments[0] instanceof Integer position) {
                        verifyResource(capture, observation.physicalConnection);
                        Object[] forwardedArguments = arguments.clone();
                        java.lang.reflect.Method forwardedMethod = method;
                        BindingFault fault = bindingFault.get();
                        if (fault != null && observation.sql.equals(NominalProjectionCatalog.positionalSql())) {
                            if (fault == BindingFault.OMIT && position == 3) return null;
                            if (fault == BindingFault.WRONG_DATE && position == 3)
                                forwardedArguments[1] = java.sql.Date.valueOf(((java.sql.Date) arguments[1]).toLocalDate().plusDays(1));
                            if (fault == BindingFault.WRONG_DAY && position == 7) forwardedArguments[1] = (short)0;
                            if (fault == BindingFault.WRONG_SLOT && position == 3) forwardedArguments[0] = 4;
                            if (fault == BindingFault.SWAP && position == 3) forwardedArguments[0] = 7;
                            if (fault == BindingFault.SWAP && position == 7) forwardedArguments[0] = 3;
                            if (fault == BindingFault.WRONG_TYPE && position == 3) {
                                forwardedMethod = PreparedStatement.class.getMethod("setString", int.class, String.class);
                                forwardedArguments = new Object[]{3, canonical(arguments[1])};
                            }
                        }
                        Object forwarded = invoke(delegate, forwardedMethod, forwardedArguments);
                        Object value = forwardedArguments[1];
                        int forwardedPosition = (Integer)forwardedArguments[0];
                        observation.binds.put(forwardedPosition, new BindObservation(
                                forwardedPosition, forwardedMethod.getName(), value == null ? "NULL" : value.getClass().getName(),
                                canonical(value), forwardedArguments.length > 2 ? canonical(forwardedArguments[2]) : null));
                        observation.setterHistory.add(observation.binds.get(forwardedPosition));
                        return forwarded;
                    }
                    if (name.startsWith("execute")) {
                        verifyResource(capture, observation.physicalConnection);
                        observation.entered = true;
                        try {
                            Object result = invoke(delegate, method, arguments);
                            observation.completed = true;
                            verifyResource(capture, observation.physicalConnection);
                            awaitDataBarrier(observation.sql);
                            return result;
                        } catch (Throwable failure) {
                            observation.failed = true;
                            throw failure;
                        }
                    }
                    if ("equals".equals(name)) return proxy == arguments[0];
                    if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                    return invoke(delegate, method, arguments);
                });
    }

    private void awaitDataBarrier(String sql) {
        Barrier barrier = dataBarrier;
        if (barrier == null || !sql.contains("FROM programacion_asignacion a JOIN programacion_bloque")) return;
        barrier.observed.countDown();
        try {
            if (!barrier.release.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("R3 concurrency barrier timed out");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("R3 concurrency barrier interrupted", exception);
        }
    }

    private Object invoke(Object target, java.lang.reflect.Method method, Object[] arguments) throws Throwable {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private String canonical(Object value) {
        if (value == null) return "NULL";
        if (value instanceof java.sql.Date date) return date.toLocalDate().toString();
        if (value instanceof java.sql.Time time) return time.toLocalTime().toString();
        return String.valueOf(value);
    }

    public List<MetadataObservation> metadata(Capture capture) {
        requireOpen(capture); return List.copyOf(capture.metadata);
    }
    public record MetadataObservation(String method, String value, Object physicalConnection) { }

    public static final class Capture {
        private final String invocationIdentity;
        private final Thread owner;
        private final List<StatementObservationMutable> observations = new ArrayList<>();
        private final List<MetadataObservation> metadata = new ArrayList<>();
        private boolean closed;
        private Object transactionOwner;
        private EntityManagerFactory factory;
        private EntityManagerHolder holder;
        private Session session;
        private Object physical;

        private Capture(String invocationIdentity, Thread owner) {
            this.invocationIdentity = invocationIdentity;
            this.owner = owner;
        }

        public String invocationIdentity() { return invocationIdentity; }
    }

    public record BindObservation(
            int position, String setter, String runtimeClass, String canonicalValue, String jdbcTypeArgument) { }

    public record StatementObservation(
            int ordinal,
            String sql,
            Map<Integer, BindObservation> binds,
            List<BindObservation> setterHistory,
            Object transactionOwner,
            EntityManagerFactory factory,
            EntityManagerHolder holder,
            Session session,
            Connection connection,
            Object physicalConnection,
            boolean executeEntered,
            boolean executeCompleted,
            boolean executeFailed) {
        public StatementObservation {
            binds = Map.copyOf(binds); setterHistory = List.copyOf(setterHistory);
        }
    }

    private static final class StatementObservationMutable {
        private final int ordinal;
        private final String sql;
        private final Connection connection;
        private final Capture capture;
        private final Object physicalConnection;
        private final Map<Integer, BindObservation> binds = new LinkedHashMap<>();
        private final List<BindObservation> setterHistory = new ArrayList<>();
        private boolean entered;
        private boolean completed;
        private boolean failed;

        private StatementObservationMutable(int ordinal, String sql, Connection connection,
                                            Object physicalConnection, Capture capture) {
            this.ordinal = ordinal;
            this.sql = sql;
            this.connection = connection;
            this.physicalConnection = physicalConnection;
            this.capture = capture;
        }

        private StatementObservation snapshot() {
            return new StatementObservation(ordinal, sql, binds, setterHistory, capture.transactionOwner, capture.factory,
                    capture.holder, capture.session, connection, physicalConnection,
                    entered, completed, failed);
        }
    }

    private record Barrier(CountDownLatch observed, CountDownLatch release) {
        private Barrier {
            Objects.requireNonNull(observed, "observed");
            Objects.requireNonNull(release, "release");
        }
    }
}

}
