package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.LegacyTurnJpaReader;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadPort;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadSet;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.postgresql.PGConnection;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public class LegacyTurnTransactionTestOwner {

    private final LegacyTurnReadPort reader;
    private final LegacyTurnProjectionQueryExecutor executor;
    private final LegacyTurnProjectionMapper mapper;
    private final F2eStatementPolicyInspector inspector;
    private final LegacyTurnJdbcCapture jdbcCapture;
    private final ContextRegistry contexts;
    private final LegacyTurnR2PostgresTestConfiguration.Descriptor descriptor;
    private final ConcurrentHashMap<String, InvocationState> registry = new ConcurrentHashMap<>();

    LegacyTurnTransactionTestOwner(
            LegacyTurnReadPort reader,
            LegacyTurnProjectionQueryExecutor executor,
            LegacyTurnProjectionMapper mapper,
            F2eStatementPolicyInspector inspector,
            LegacyTurnJdbcCapture jdbcCapture,
            ContextRegistry contexts,
            LegacyTurnR2PostgresTestConfiguration.Descriptor descriptor) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.inspector = Objects.requireNonNull(inspector, "inspector");
        this.jdbcCapture = Objects.requireNonNull(jdbcCapture, "jdbcCapture");
        this.contexts = Objects.requireNonNull(contexts, "contexts");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        validateDescriptor();
    }

    @Transactional(
            transactionManager = "f2eR2ReaderTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            readOnly = true)
    public Outcome inRepeatableReadOnly(Seed seed, LegacyTurnScope scope) {
        validateSeed(seed);
        Objects.requireNonNull(scope, "scope");
        TransactionReference initialTransaction = validateTransaction();
        String registryKey = LegacyTurnReadContext.hashSecuencia(
                "F2E-R2-RR-INVOCATION-KEY-V1", descriptor.fixtureIdentity(),
                seed.runIdentity(), seed.attemptIdentity());
        if (registry.putIfAbsent(registryKey, InvocationState.ACTIVE) != null) {
            throw new IllegalStateException("R2 read invocation cannot be reused");
        }
        AtomicBoolean success = new AtomicBoolean();
        CompletionEvidence completion = new CompletionEvidence();
        AtomicReference<F2eStatementPolicyInspector.Captura> sqlCaptureRef = new AtomicReference<>();
        AtomicReference<LegacyTurnJdbcCapture.Capture> jdbcRef = new AtomicReference<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (!success.get() || !jdbcCapture.isOpen(jdbcRef.get())
                        || TransactionSynchronizationManager.getResource(
                        descriptor.entityManagerFactory()) != initialTransaction.holder()
                        || !inspector.observarCaptura(sqlCaptureRef.get()).equals(completion.statementIds)
                        || !jdbcCapture.snapshot(jdbcRef.get()).equals(completion.jdbcObservations)) {
                    throw new IllegalStateException("R2 SQL/JDBC evidence lost before transaction completion");
                }
            }

            @Override
            public void afterCompletion(int status) {
                try {
                    F2eStatementPolicyInspector.Captura sql = sqlCaptureRef.get();
                    LegacyTurnJdbcCapture.Capture jdbc = jdbcRef.get();
                    if (sql != null && jdbc != null && jdbcCapture.isOpen(jdbc)) {
                        List<String> ids = inspector.cerrarCaptura(sql);
                        List<LegacyTurnJdbcCapture.StatementObservation> observed = jdbcCapture.close(jdbc);
                        completion.completed = status == STATUS_COMMITTED && success.get()
                                && ids.equals(completion.statementIds)
                                && observed.equals(completion.jdbcObservations);
                    }
                } finally {
                    if (sqlCaptureRef.get() != null) inspector.descartarCaptura(sqlCaptureRef.get());
                    if (jdbcRef.get() != null) jdbcCapture.discard(jdbcRef.get());
                    contexts.clear();
                    registry.put(registryKey, completion.completed
                            ? InvocationState.SUCCESS : InvocationState.ABORTED);
                }
            }
        });

        ConnectionReference initialConnection = connectionReference();
        validateNativeMetadata(initialConnection);
        String invocation = seed.runIdentity() + "/" + seed.attemptIdentity();
        F2eStatementPolicyInspector.Captura sqlCapture = inspector.abrirCaptura(invocation);
        LegacyTurnJdbcCapture.Capture jdbc = jdbcCapture.open(invocation);
        sqlCaptureRef.set(sqlCapture);
        jdbcRef.set(jdbc);
        try {
            jdbcCapture.bindResource(jdbc, this, descriptor.entityManagerFactory(),
                    initialTransaction.holder(), initialTransaction.session(), initialConnection.physical());
            String isolationInitial = executor.consultarAislamiento();
            String readOnlyInitial = executor.consultarSoloLectura();
            var resourceInitial = executor.consultarIdentidadRecurso();
            String snapshotInitial = executor.consultarSnapshot();
            validateProbes(isolationInitial, readOnlyInitial, resourceInitial);
            String snapshotEvidence = LegacyTurnReadContext.hashSecuencia(
                    "F2E-R2-RR-TEST-EVIDENCE-V1", descriptor.fixtureIdentity(),
                    seed.runIdentity(), seed.attemptIdentity(), "f2eR2ReaderTransactionManager",
                    "f2eR2ReaderPersistenceUnit", "repeatable read", "read only", snapshotInitial);
            LegacyTurnReadContext context = new LegacyTurnReadContext(
                    seed.runIdentity(), seed.attemptIdentity(), descriptor.sourceName(),
                    descriptor.schemaFingerprint(), LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,
                    seed.ruleCatalogVersion(), seed.businessZone(), scope.canonical(),
                    LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST, snapshotEvidence);
            contexts.bind(context, scope, initialTransaction.holder(), initialTransaction.session(),
                    descriptor.entityManagerFactory(), descriptor.sourceName(),
                    descriptor.schemaFingerprint(), snapshotEvidence);
            LegacyTurnReadSet readSet;
            try {
                readSet = reader.readForDate(context, scope);
            } finally {
                contexts.clear();
            }

            String isolationFinal = executor.consultarAislamiento();
            String readOnlyFinal = executor.consultarSoloLectura();
            var resourceFinal = executor.consultarIdentidadRecurso();
            String snapshotFinal = executor.consultarSnapshot();
            validateProbes(isolationFinal, readOnlyFinal, resourceFinal);
            if (!snapshotInitial.equals(snapshotFinal)
                    || !resourceInitial.equals(resourceFinal)
                    || !isolationInitial.equals(isolationFinal)
                    || !readOnlyInitial.equals(readOnlyFinal)) {
                throw new IllegalStateException("R2 repeatable-read snapshot consistency not proven");
            }
            ConnectionReference finalConnection = connectionReference();
            TransactionReference finalTransaction = validateTransaction();
            validateNativeMetadata(finalConnection);
            if (initialConnection.connection() != finalConnection.connection()
                    || initialConnection.physical() != finalConnection.physical()
                    || initialTransaction.holder() != finalTransaction.holder()
                    || initialTransaction.session() != finalTransaction.session()) {
                throw new IllegalStateException("R2 physical transaction resource changed");
            }
            List<String> statementIds = inspector.observarCaptura(sqlCapture);
            List<LegacyTurnJdbcCapture.StatementObservation> jdbcObservations = jdbcCapture.snapshot(jdbc);
            List<String> expectedManifest = new java.util.ArrayList<>(List.of(
                    LegacyTurnProjectionCatalog.ISOLATION_ID,
                    LegacyTurnProjectionCatalog.READ_ONLY_ID,
                    LegacyTurnProjectionCatalog.RESOURCE_ID,
                    LegacyTurnProjectionCatalog.SNAPSHOT_ID,
                    LegacyTurnProjectionCatalog.MEMBERS_ID));
            if (statementIds.contains(LegacyTurnProjectionCatalog.ASSIGNMENTS_ID)) {
                expectedManifest.add(LegacyTurnProjectionCatalog.ASSIGNMENTS_ID);
            }
            expectedManifest.addAll(List.of(LegacyTurnProjectionCatalog.ISOLATION_ID,
                    LegacyTurnProjectionCatalog.READ_ONLY_ID,
                    LegacyTurnProjectionCatalog.RESOURCE_ID,
                    LegacyTurnProjectionCatalog.SNAPSHOT_ID));
            if (statementIds.contains(LegacyTurnProjectionCatalog.ASSIGNMENTS_ID)
                    == readSet.sources().isEmpty()
                    || !statementIds.equals(expectedManifest)
                    || statementIds.size() != jdbcObservations.size()
                    || jdbcObservations.stream().anyMatch(observation ->
                    !observation.executeEntered() || !observation.executeCompleted() || observation.executeFailed()
                            || observation.transactionOwner() != this
                            || observation.factory() != descriptor.entityManagerFactory()
                            || observation.holder() != initialTransaction.holder()
                            || observation.session() != initialTransaction.session()
                            || observation.connection() != initialConnection.connection()
                            || observation.physicalConnection() != initialConnection.physical())) {
                throw new IllegalStateException("R2 native JDBC execution evidence not proven");
            }
            completion.statementIds = statementIds;
            completion.jdbcObservations = jdbcObservations;
            success.set(true);
            return new Outcome(readSet, context, snapshotInitial, snapshotFinal,
                    resourceInitial, resourceFinal, statementIds, jdbcObservations,
                    mapper.logicalReadSetFingerprint(readSet), initialConnection.physical(),
                    initialTransaction.holder(), initialTransaction.session(), this, completion);
        } catch (RuntimeException | Error failure) {
            inspector.descartarCaptura(sqlCapture);
            jdbcCapture.discard(jdbc);
            throw failure;
        }
    }

    public String state(Seed seed) {
        validateSeed(seed);
        String key = LegacyTurnReadContext.hashSecuencia(
                "F2E-R2-RR-INVOCATION-KEY-V1", descriptor.fixtureIdentity(),
                seed.runIdentity(), seed.attemptIdentity());
        InvocationState state = registry.get(key);
        return state == null ? null : state.name();
    }

    @Transactional(
            transactionManager = "f2eR2ReaderTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            readOnly = true)
    public void readWithForgedContext(Seed seed, LegacyTurnScope scope,
                                      UnaryOperator<LegacyTurnReadContext> forge) {
        validateSeed(seed);
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(forge, "forge");
        TransactionReference transaction = validateTransaction();
        ConnectionReference connection = connectionReference();
        F2eStatementPolicyInspector.Captura sql = inspector.abrirCaptura(seed.runIdentity() + "/forgery");
        LegacyTurnJdbcCapture.Capture jdbc = jdbcCapture.open(seed.runIdentity() + "/forgery");
        try {
            jdbcCapture.bindResource(jdbc, this, descriptor.entityManagerFactory(),
                    transaction.holder(), transaction.session(), connection.physical());
            String isolation = executor.consultarAislamiento();
            String readOnly = executor.consultarSoloLectura();
            var resource = executor.consultarIdentidadRecurso();
            String snapshot = executor.consultarSnapshot();
            validateProbes(isolation, readOnly, resource);
            String evidence = LegacyTurnReadContext.hashSecuencia(
                    "F2E-R2-RR-TEST-EVIDENCE-V1", descriptor.fixtureIdentity(),
                    seed.runIdentity(), seed.attemptIdentity(), "f2eR2ReaderTransactionManager",
                    "f2eR2ReaderPersistenceUnit", "repeatable read", "read only", snapshot);
            LegacyTurnReadContext trusted = new LegacyTurnReadContext(seed.runIdentity(),
                    seed.attemptIdentity(), descriptor.sourceName(), descriptor.schemaFingerprint(),
                    LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,
                    seed.ruleCatalogVersion(), seed.businessZone(), scope.canonical(),
                    LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST, evidence);
            contexts.bind(trusted, scope, transaction.holder(), transaction.session(),
                    descriptor.entityManagerFactory(), descriptor.sourceName(),
                    descriptor.schemaFingerprint(), evidence);
            reader.readForDate(forge.apply(trusted), scope);
            throw new IllegalStateException("Forged R2 context was accepted");
        } finally {
            contexts.clear();
            inspector.descartarCaptura(sql);
            jdbcCapture.discard(jdbc);
        }
    }

    private TransactionReference validateTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !Objects.equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel(),
                TransactionDefinition.ISOLATION_REPEATABLE_READ)) {
            throw new IllegalStateException("R2 REPEATABLE_READ/read-only transaction owner not proven");
        }
        Object resource = TransactionSynchronizationManager.getResource(descriptor.entityManagerFactory());
        if (!(resource instanceof EntityManagerHolder holder)) {
            throw new IllegalStateException("R2 transaction-bound EntityManagerHolder not proven");
        }
        Session session = holder.getEntityManager().unwrap(Session.class);
        if (!session.isJoinedToTransaction() || !session.isDefaultReadOnly()
                || session.getHibernateFlushMode() != FlushMode.MANUAL
                || !descriptor.entityManager().isJoinedToTransaction()) {
            throw new IllegalStateException("R2 transaction-bound Session not proven");
        }
        return new TransactionReference(holder, session);
    }

    private ConnectionReference connectionReference() {
        return descriptor.entityManager().unwrap(Session.class).doReturningWork(connection ->
                new ConnectionReference(connection, connection.unwrap(PGConnection.class)));
    }

    private void validateNativeMetadata(ConnectionReference reference) {
        try {
            if (!(reference.physical() instanceof Connection physical)) {
                throw new IllegalStateException("R2 PgConnection identity not a JDBC connection");
            }
            String url = physical.getMetaData().getURL();
            String principal = physical.getMetaData().getUserName();
            if (!descriptor.canonicalJdbcUrl().equals(url)
                    || !descriptor.credentialPrincipal().equals(principal)) {
                throw new IllegalStateException("R2 native connection metadata mismatch");
            }
        } catch (java.sql.SQLException exception) {
            throw new IllegalStateException("R2 native connection metadata unavailable", exception);
        }
    }

    private void validateProbes(
            String isolation,
            String readOnly,
            LegacyTurnProjectionQueryExecutor.ResourceIdentity resource) {
        if (!"repeatable read".equals(isolation) || !"on".equals(readOnly)
                || !descriptor.databaseName().equals(resource.databaseName())
                || !descriptor.schemaName().equals(resource.schemaName())) {
            throw new IllegalStateException("R2 PostgreSQL transaction probes rejected");
        }
    }

    private void validateDescriptor() {
        if (!descriptor.fixtureKey().matches("[a-z0-9][a-z0-9-]*")
                || !descriptor.fixtureIdentity().equals("fixture-r2-" + descriptor.fixtureKey())
                || !descriptor.sourceName().equals("fixture:postgres16:r2:" + descriptor.fixtureKey())
                || !descriptor.schemaFingerprint().matches("sha256:[0-9a-f]{64}")
                || descriptor.transactionManager().getEntityManagerFactory() != descriptor.entityManagerFactory()
                || descriptor.transactionManager().getDataSource() != descriptor.readerDataSource()
                || descriptor.inspector() != inspector) {
            throw new IllegalStateException("R2 test resource descriptor not proven");
        }
    }

    private void validateSeed(Seed seed) {
        if (seed == null || seed.runIdentity() == null || seed.runIdentity().isBlank()
                || seed.attemptIdentity() == null || seed.attemptIdentity().isBlank()
                || seed.ruleCatalogVersion() == null || seed.ruleCatalogVersion().isBlank()
                || seed.businessZone() == null) {
            throw new IllegalArgumentException("R2 transaction seed is invalid");
        }
    }

    public record Seed(String runIdentity, String attemptIdentity, String ruleCatalogVersion, ZoneId businessZone) { }

    public record Outcome(
            LegacyTurnReadSet readSet,
            LegacyTurnReadContext context,
            String snapshotInitial,
            String snapshotFinal,
            LegacyTurnProjectionQueryExecutor.ResourceIdentity resourceInitial,
            LegacyTurnProjectionQueryExecutor.ResourceIdentity resourceFinal,
            List<String> statementIds,
            List<LegacyTurnJdbcCapture.StatementObservation> jdbcObservations,
            String logicalReadSetFingerprint,
            Object physicalConnection,
            EntityManagerHolder holder,
            Session session,
            Object transactionOwner,
            CompletionEvidence completionEvidence) {
        public Outcome {
            statementIds = List.copyOf(statementIds);
            jdbcObservations = List.copyOf(jdbcObservations);
        }
        @Override
        public LegacyTurnReadSet readSet() {
            if (!completionEvidence.completed) {
                throw new IllegalStateException("R2 transaction completion evidence not proven");
            }
            return readSet;
        }
        public boolean completed() { return completionEvidence.completed; }
    }

    private record ConnectionReference(Connection connection, Object physical) { }
    private record TransactionReference(EntityManagerHolder holder, Session session) { }

    public static final class CompletionEvidence {
        private volatile boolean completed;
        private List<String> statementIds = List.of();
        private List<LegacyTurnJdbcCapture.StatementObservation> jdbcObservations = List.of();
    }

    public static final class ContextRegistry implements LegacyTurnJpaReader.ContextAuthority {
        private final ThreadLocal<Binding> current = new ThreadLocal<>();

        void bind(LegacyTurnReadContext context, LegacyTurnScope scope,
                  EntityManagerHolder holder, Session session, jakarta.persistence.EntityManagerFactory factory,
                  String sourceName,
                  String schemaFingerprint, String snapshotEvidence) {
            if (current.get() != null || !sourceName.equals(context.sourceName())
                    || !schemaFingerprint.equals(context.schemaFingerprint())
                    || !snapshotEvidence.equals(context.snapshotEvidenceId())) {
                throw new IllegalStateException("R2 trusted provenance could not be bound");
            }
            current.set(new Binding(context, scope.canonical(), holder, session, factory));
        }

        void clear() { current.remove(); }

        @Override
        public void verify(LegacyTurnReadContext context, LegacyTurnScope scope) {
            Binding binding = current.get();
            if (binding == null || binding.context() != context
                    || !binding.scopeCanonical().equals(scope.canonical())
                    || TransactionSynchronizationManager.getResource(binding.factory()) != binding.holder()
                    || binding.holder().getEntityManager().unwrap(Session.class) != binding.session()) {
                throw new IllegalStateException("R2 context provenance is not owner bound");
            }
        }

        private record Binding(LegacyTurnReadContext context, String scopeCanonical,
                               EntityManagerHolder holder, Session session,
                               jakarta.persistence.EntityManagerFactory factory) { }
    }

    private enum InvocationState { ACTIVE, SUCCESS, ABORTED }
}
