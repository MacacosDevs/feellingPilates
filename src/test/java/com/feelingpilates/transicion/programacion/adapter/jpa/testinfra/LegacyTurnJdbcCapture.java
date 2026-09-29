package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import org.postgresql.PGConnection;
import org.springframework.jdbc.datasource.DelegatingDataSource;

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

    public List<StatementObservation> close(Capture capture) {
        if (current.get() != capture || capture.owner != Thread.currentThread() || capture.closed) {
            throw new IllegalStateException("R2 JDBC capture ownership not proven");
        }
        capture.closed = true;
        current.remove();
        return capture.observations.stream().map(StatementObservationMutable::snapshot).toList();
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
                        PreparedStatement statement = (PreparedStatement) invoke(delegate, method, arguments);
                        Capture capture = current.get();
                        if (capture == null) return statement;
                        if (capture.owner != Thread.currentThread() || capture.closed) {
                            throw new IllegalStateException("R2 JDBC statement outside capture owner");
                        }
                        StatementObservationMutable observation = new StatementObservationMutable(
                                capture.observations.size() + 1, sql, delegate, physical);
                        capture.observations.add(observation);
                        return wrapStatement(statement, observation);
                    }
                    if ("equals".equals(method.getName())) return proxy == arguments[0];
                    if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                    return invoke(delegate, method, arguments);
                });
    }

    private PreparedStatement wrapStatement(
            PreparedStatement delegate, StatementObservationMutable observation) {
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, (proxy, method, arguments) -> {
                    String name = method.getName();
                    if (name.startsWith("set") && arguments != null && arguments.length >= 2
                            && arguments[0] instanceof Integer position) {
                        Object value = arguments[1];
                        observation.binds.put(position, new BindObservation(
                                position, name, value == null ? "NULL" : value.getClass().getName(),
                                canonical(value), arguments.length > 2 ? canonical(arguments[2]) : null));
                    }
                    if (name.startsWith("execute")) {
                        observation.entered = true;
                        try {
                            Object result = invoke(delegate, method, arguments);
                            observation.completed = true;
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
        private final Object physicalConnection;
        private final Map<Integer, BindObservation> binds = new LinkedHashMap<>();
        private boolean entered;
        private boolean completed;
        private boolean failed;

        private StatementObservationMutable(int ordinal, String sql, Connection connection, Object physicalConnection) {
            this.ordinal = ordinal;
            this.sql = sql;
            this.connection = connection;
            this.physicalConnection = physicalConnection;
        }

        private StatementObservation snapshot() {
            return new StatementObservation(ordinal, sql, binds, connection, physicalConnection,
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
