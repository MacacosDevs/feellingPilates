package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionQueryExecutor;
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

public class LegacyTurnTransactionTestOwner {

    private final LegacyTurnReadPort reader;
    private final LegacyTurnProjectionQueryExecutor executor;
    private final LegacyTurnProjectionMapper mapper;
    private final F2eStatementPolicyInspector inspector;
    private final LegacyTurnJdbcCapture jdbcCapture;
    private final LegacyTurnR2PostgresTestConfiguration.Descriptor descriptor;
    private final ConcurrentHashMap<String, InvocationState> registry = new ConcurrentHashMap<>();

    LegacyTurnTransactionTestOwner(
            LegacyTurnReadPort reader,
            LegacyTurnProjectionQueryExecutor executor,
            LegacyTurnProjectionMapper mapper,
            F2eStatementPolicyInspector inspector,
            LegacyTurnJdbcCapture jdbcCapture,
            LegacyTurnR2PostgresTestConfiguration.Descriptor descriptor) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.inspector = Objects.requireNonNull(inspector, "inspector");
        this.jdbcCapture = Objects.requireNonNull(jdbcCapture, "jdbcCapture");
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
        validateTransaction();
        String registryKey = LegacyTurnReadContext.hashSecuencia(
                "F2E-R2-RR-INVOCATION-KEY-V1", descriptor.fixtureIdentity(),
                seed.runIdentity(), seed.attemptIdentity());
        if (registry.putIfAbsent(registryKey, InvocationState.ACTIVE) != null) {
            throw new IllegalStateException("R2 read invocation cannot be reused");
        }
        AtomicBoolean success = new AtomicBoolean();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                registry.put(registryKey, status == STATUS_COMMITTED && success.get()
                        ? InvocationState.SUCCESS : InvocationState.ABORTED);
            }
        });

        ConnectionReference initialConnection = connectionReference();
        validateNativeMetadata(initialConnection);
        String invocation = seed.runIdentity() + "/" + seed.attemptIdentity();
        F2eStatementPolicyInspector.Captura sqlCapture = inspector.abrirCaptura(invocation);
        LegacyTurnJdbcCapture.Capture jdbc = jdbcCapture.open(invocation);
        try {
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

            LegacyTurnReadSet readSet = reader.readForDate(context, scope);

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
            validateNativeMetadata(finalConnection);
            if (initialConnection.connection() != finalConnection.connection()
                    || initialConnection.physical() != finalConnection.physical()) {
                throw new IllegalStateException("R2 physical transaction resource changed");
            }
            List<String> statementIds = inspector.cerrarCaptura(sqlCapture);
            List<LegacyTurnJdbcCapture.StatementObservation> jdbcObservations = jdbcCapture.close(jdbc);
            if (statementIds.size() != jdbcObservations.size()
                    || jdbcObservations.stream().anyMatch(observation ->
                    !observation.executeEntered() || !observation.executeCompleted() || observation.executeFailed()
                            || observation.physicalConnection() != initialConnection.physical())) {
                throw new IllegalStateException("R2 native JDBC execution evidence not proven");
            }
            success.set(true);
            return new Outcome(readSet, context, snapshotInitial, snapshotFinal,
                    resourceInitial, resourceFinal, statementIds, jdbcObservations,
                    mapper.logicalReadSetFingerprint(readSet), initialConnection.physical());
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

    private void validateTransaction() {
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
            Object physicalConnection) {
        public Outcome {
            statementIds = List.copyOf(statementIds);
            jdbcObservations = List.copyOf(jdbcObservations);
        }
    }

    private record ConnectionReference(Connection connection, Object physical) { }

    private enum InvocationState { ACTIVE, SUCCESS, ABORTED }
}
