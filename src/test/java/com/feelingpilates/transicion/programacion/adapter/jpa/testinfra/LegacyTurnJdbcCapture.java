package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import org.postgresql.PGConnection;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class LegacyTurnJdbcCapture {

    private final ThreadLocal<Capture> current = new ThreadLocal<>();
    private volatile Barrier membersBarrier;

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
            throw new IllegalStateException("R2 JDBC capture cannot be nested or anonymous");
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
        if (!isOpen(capture)) throw new IllegalStateException("R2 JDBC capture ownership not proven");
    }

    private void verifyResource(Capture capture, Object physical) {
        requireOpen(capture);
        if (capture.factory == null || capture.holder == null || capture.session == null
                || capture.physical != physical
                || TransactionSynchronizationManager.getResource(capture.factory) != capture.holder
                || capture.holder.getEntityManager().unwrap(Session.class) != capture.session
                || !capture.session.isJoinedToTransaction()) {
            throw new IllegalStateException("R2 statement transaction resource chain changed");
        }
    }

    public void discard(Capture capture) {
        if (current.get() == capture) {
            capture.closed = true;
            current.remove();
        }
    }

    public void installMembersBarrier(CountDownLatch membersObserved, CountDownLatch continueAssignments) {
        membersBarrier = new Barrier(membersObserved, continueAssignments);
    }

    public void clearBarrier() {
        membersBarrier = null;
    }

    private Connection wrapConnection(Connection delegate) throws SQLException {
        Object physical = delegate.unwrap(PGConnection.class);
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, arguments) -> {
                    if (method.getName().startsWith("prepareStatement") && arguments != null
                            && arguments.length > 0 && arguments[0] instanceof String sql) {
                        Capture capture = current.get();
                        if (capture != null) verifyResource(capture, physical);
                        PreparedStatement statement = (PreparedStatement) invoke(delegate, method, arguments);
                        if (capture == null) return statement;
                        verifyResource(capture, physical);
                        StatementObservationMutable observation = new StatementObservationMutable(
                                capture.observations.size() + 1, sql, (Connection) proxy, physical, capture);
                        capture.observations.add(observation);
                        verifyResource(capture, physical);
                        return wrapStatement(statement, observation, capture);
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
                        Object value = arguments[1];
                        observation.binds.put(position, new BindObservation(
                                position, name, value == null ? "NULL" : value.getClass().getName(),
                                canonical(value), arguments.length > 2 ? canonical(arguments[2]) : null));
                    }
                    if (name.startsWith("execute")) {
                        verifyResource(capture, observation.physicalConnection);
                        observation.entered = true;
                        try {
                            Object result = invoke(delegate, method, arguments);
                            observation.completed = true;
                            verifyResource(capture, observation.physicalConnection);
                            awaitMembersBarrier(observation.sql);
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

    private void awaitMembersBarrier(String sql) {
        Barrier barrier = membersBarrier;
        if (barrier == null || !sql.contains("FROM public.turno_instructor t LEFT JOIN")) return;
        barrier.observed.countDown();
        try {
            if (!barrier.release.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("R2 concurrency barrier timed out");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("R2 concurrency barrier interrupted", exception);
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

    public static final class Capture {
        private final String invocationIdentity;
        private final Thread owner;
        private final List<StatementObservationMutable> observations = new ArrayList<>();
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
            binds = Map.copyOf(binds);
        }
    }

    private static final class StatementObservationMutable {
        private final int ordinal;
        private final String sql;
        private final Connection connection;
        private final Capture capture;
        private final Object physicalConnection;
        private final Map<Integer, BindObservation> binds = new LinkedHashMap<>();
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
            return new StatementObservation(ordinal, sql, binds, capture.transactionOwner, capture.factory,
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
