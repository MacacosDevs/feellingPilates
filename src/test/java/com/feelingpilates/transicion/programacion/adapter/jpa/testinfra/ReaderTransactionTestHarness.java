package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.detector.ReservationSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import com.feelingpilates.transicion.programacion.read.ReservationReadPort;
import com.feelingpilates.transicion.programacion.read.ReservationScope;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public class ReaderTransactionTestHarness {

    private static final String ID_AISLAMIENTO = F2eStatementPolicyInspector.ID_AISLAMIENTO;
    private static final String ID_SOLO_LECTURA = F2eStatementPolicyInspector.ID_SOLO_LECTURA;
    private static final Pattern URL_JDBC_CANONICA = Pattern.compile(
            "jdbc:postgresql://([a-z0-9.-]+):([0-9]+)/([A-Za-z0-9_]+)");

    private final ReservationReadPort reader;
    private final F2eStatementPolicyInspector inspector;
    private final F2ePostgresTestConfiguration.DescriptorRecursoLector descriptor;
    private final DataSource readerDataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final JpaTransactionManager transactionManager;
    private final EntityManager entityManager;
    private final RegistroUnicidadIdentidades registroUnicidadIdentidades = new RegistroUnicidadIdentidades();
    private final AtomicReference<MutacionRecursoPrueba> mutacionRecursoPrueba =
            new AtomicReference<>(MutacionRecursoPrueba.NINGUNA);
    private final AtomicReference<PuntoFalloPrueba> puntoFalloPrueba =
            new AtomicReference<>(PuntoFalloPrueba.NINGUNO);
    private final AtomicReference<MutacionManifiestoPrueba> mutacionManifiestoPrueba =
            new AtomicReference<>(MutacionManifiestoPrueba.NINGUNA);
    private final AtomicReference<MutacionContextoPrueba> mutacionContextoPrueba =
            new AtomicReference<>(MutacionContextoPrueba.NINGUNA);
    private volatile ControlBloqueoPrueba controlBloqueoPrueba;
    private final AtomicReference<Long> preparacionesAntesSqlHostil = new AtomicReference<>();
    private final AtomicReference<Long> preparacionesDespuesSqlHostil = new AtomicReference<>();
    private final AtomicReference<String> estadoBeforeCommitPrueba = new AtomicReference<>();

    ReaderTransactionTestHarness(
            ReservationReadPort reader,
            F2eStatementPolicyInspector inspector,
            F2ePostgresTestConfiguration.DescriptorRecursoLector descriptor,
            DataSource readerDataSource,
            EntityManagerFactory entityManagerFactory,
            JpaTransactionManager transactionManager,
            EntityManager entityManager) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.inspector = Objects.requireNonNull(inspector, "inspector");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.readerDataSource = Objects.requireNonNull(readerDataSource, "readerDataSource");
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    @Transactional(
            transactionManager = "f2eReaderTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            readOnly = true)
    public List<ReservationSourceSnapshot> inSingleStatementReadOnly(
            SemillaLectura semilla,
            Set<UUID> reservationIds) {
        validarSemilla(semilla);
        if (reservationIds == null || reservationIds.isEmpty() || reservationIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("reservationIds must be non-empty and contain no nulls");
        }
        Set<UUID> copia = Set.copyOf(reservationIds);
        return ejecutar(semilla, Operacion.READ_BY_RESERVATION_IDS,
                F2eStatementPolicyInspector.ID_POR_IDENTIDADES,
                contexto -> reader.readByReservationIds(contexto, copia));
    }

    @Transactional(
            transactionManager = "f2eReaderTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            readOnly = true)
    public List<ReservationSourceSnapshot> inSingleStatementReadOnly(
            SemillaLectura semilla,
            ReservationScope scope) {
        validarSemilla(semilla);
        if (scope == null) {
            throw new IllegalArgumentException("scope is required");
        }
        return ejecutar(semilla, Operacion.READ_BY_SCOPE,
                F2eStatementPolicyInspector.ID_POR_SCOPE,
                contexto -> reader.readByScope(contexto, scope));
    }

    private List<ReservationSourceSnapshot> ejecutar(
            SemillaLectura semilla,
            Operacion operacion,
            String statementDatos,
            Lectura lectura) {
        mutacionRecursoPrueba.getAndSet(MutacionRecursoPrueba.NINGUNA);
        PuntoFalloPrueba puntoFallo = puntoFalloPrueba.getAndSet(PuntoFalloPrueba.NINGUNO);
        MutacionManifiestoPrueba mutacionManifiesto =
                mutacionManifiestoPrueba.getAndSet(MutacionManifiestoPrueba.NINGUNA);
        MutacionContextoPrueba mutacionContexto =
                mutacionContextoPrueba.getAndSet(MutacionContextoPrueba.NINGUNA);
        ReservaUnicidad reserva = registroUnicidadIdentidades.reservar(semilla, descriptor.identidadFuenteDatos);
        AtomicBoolean resultadoValidado = new AtomicBoolean();
        registrarFinalizacionTransaccion(reserva, resultadoValidado);
        F2eStatementPolicyInspector.Captura captura = null;
        try {
            if (puntoFallo == PuntoFalloPrueba.BLOQUEAR_MIENTRAS_ACTIVE) {
                bloquearMientrasActive();
            }
            if (puntoFallo == PuntoFalloPrueba.ANTES_DE_RECURSO_UNKNOWN) {
                throw new Error("F2E controlled unresolved interruption");
            }
            validarLabelsConfiables(descriptor.sourceName, descriptor.schemaFingerprint);
            ReferenciaConexion conexionInicial = probarRecurso();
            ReadSnapshotIdentifiers.SesionCalculo calculos = ReadSnapshotIdentifiers.nuevaSesionCalculo();
            String snapshotEvidenceId = calculos.calcularEvidenciaSnapshot(
                    descriptor.identidadFuenteDatos, semilla.transactionBoundaryIdentity());
            String aislamiento = "read committed";
            String modoAcceso = "read only";
            String statementComprometido = mutacionManifiesto == MutacionManifiestoPrueba.CATALOGO_INCORRECTO
                    ? (statementDatos.equals(F2eStatementPolicyInspector.ID_POR_IDENTIDADES)
                    ? F2eStatementPolicyInspector.ID_POR_SCOPE
                    : F2eStatementPolicyInspector.ID_POR_IDENTIDADES)
                    : statementDatos;
            List<String> manifiestoEsperado = List.of(
                    ID_AISLAMIENTO, ID_SOLO_LECTURA, statementComprometido);
            String huellaStatements = calculos.calcularObservacionStatements(
                    snapshotEvidenceId, aislamiento, modoAcceso, manifiestoEsperado);
            String evidenciaContexto = mutacionContexto == MutacionContextoPrueba.EVIDENCIA_SNAPSHOT
                    ? "0".repeat(64) : snapshotEvidenceId;
            String statementsContexto = mutacionContexto == MutacionContextoPrueba.HUELLA_STATEMENTS
                    ? "1".repeat(64) : huellaStatements;
            String invocacionContexto = mutacionContexto == MutacionContextoPrueba.READER_INVOCATION
                    ? semilla.readerInvocationIdentity() + "-hostile" : semilla.readerInvocationIdentity();
            ReadSnapshotContext contexto = new ReadSnapshotContext(
                    semilla.runIdentity(), semilla.attemptIdentity(), invocacionContexto,
                    mutacionContexto == MutacionContextoPrueba.SOURCE_LABEL
                            ? descriptor.sourceName + "-hostile" : descriptor.sourceName,
                    mutacionContexto == MutacionContextoPrueba.SCHEMA_LABEL
                            ? "sha256:" + "0".repeat(64) : descriptor.schemaFingerprint,
                    ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                    semilla.ruleCatalogVersion(), semilla.businessZone(), semilla.snapshotClaim(),
                    evidenciaContexto, statementsContexto);
            validarLabelsConfiables(contexto.sourceName(), contexto.schemaFingerprint());
            captura = inspector.abrirCaptura(semilla.readerInvocationIdentity());
            String aislamientoObservado;
            String accesoPostgres;
            if (mutacionManifiesto == MutacionManifiestoPrueba.ORDEN_INCORRECTO) {
                accesoPostgres = ejecutarProbe("SELECT current_setting('transaction_read_only')");
                aislamientoObservado = ejecutarProbe("SELECT current_setting('transaction_isolation')");
            } else {
                aislamientoObservado = ejecutarProbe("SELECT current_setting('transaction_isolation')");
                accesoPostgres = mutacionManifiesto == MutacionManifiestoPrueba.FALTA_PROBE
                        ? "on" : ejecutarProbe("SELECT current_setting('transaction_read_only')");
            }
            if (mutacionManifiesto == MutacionManifiestoPrueba.PROBE_EXTRA) {
                ejecutarProbe("SELECT current_setting('transaction_isolation')");
            }
            if (!aislamiento.equals(aislamientoObservado) || !"on".equals(accesoPostgres)) {
                throw new IllegalStateException("F2E reader transaction properties not proven");
            }
            if (puntoFallo == PuntoFalloPrueba.SQL_DESCONOCIDA_ANTES_DE_JDBC) {
                preparacionesAntesSqlHostil.set(dataSourceContabilizado().preparacionesJdbc());
                try {
                    entityManager.createNativeQuery("SELECT r.id FROM public.reserva r ORDER BY r.id").getResultList();
                } finally {
                    preparacionesDespuesSqlHostil.set(dataSourceContabilizado().preparacionesJdbc());
                }
            }
            List<ReservationSourceSnapshot> resultado = List.copyOf(lectura.leer(contexto));
            if (mutacionManifiesto == MutacionManifiestoPrueba.DATA_EXTRA) {
                lectura.leer(contexto);
            }
            if (puntoFallo == PuntoFalloPrueba.DESPUES_DE_IDENTIDADES_ABORTO) {
                throw new IllegalStateException("F2E controlled failure after identities");
            }
            List<String> observados = inspector.cerrarCaptura(captura);
            captura = null;
            if (!manifiestoEsperado.equals(observados)) {
                throw new IllegalStateException("F2E statement manifest not proven");
            }
            String huellaFinal = calculos.calcularObservacionStatements(
                    snapshotEvidenceId, aislamiento, modoAcceso, observados);
            if (!MessageDigest.isEqual(
                    contexto.statementObservationFingerprint().getBytes(StandardCharsets.US_ASCII),
                    huellaFinal.getBytes(StandardCharsets.US_ASCII))) {
                throw new IllegalStateException("F2E identity/provenance consistency not proven");
            }
            ReferenciaConexion conexionFinal = obtenerConexion();
            if (conexionFinal.conexionTransaccional() != conexionInicial.conexionTransaccional()
                    || conexionFinal.recursoFisico() != conexionInicial.recursoFisico()) {
                throw new IllegalStateException("F2E reader resource provenance not proven");
            }
            String evidenciaRecomputada = calculos.calcularEvidenciaSnapshot(
                    descriptor.identidadFuenteDatos, semilla.transactionBoundaryIdentity());
            if (!MessageDigest.isEqual(
                    contexto.snapshotEvidenceId().getBytes(StandardCharsets.US_ASCII),
                    evidenciaRecomputada.getBytes(StandardCharsets.US_ASCII))) {
                throw new IllegalStateException("F2E identity/provenance consistency not proven");
            }
            if (!contexto.readerInvocationIdentity().equals(semilla.readerInvocationIdentity())) {
                throw new IllegalStateException("F2E identity/provenance consistency not proven");
            }
            resultadoValidado.set(true);
            System.out.println("F2E R1 topology=descriptor/DS/EMF/TM/sharedEM/Session/physicalConnection labelsExact=true SQL_MANIFEST=" + observados);
            if (puntoFallo == PuntoFalloPrueba.ROLLBACK_ONLY_TRAS_RESULTADO) {
                entityManager.unwrap(Session.class).getTransaction().markRollbackOnly();
            }
            if (puntoFallo == PuntoFalloPrueba.FALLO_EN_BEFORE_COMMIT) registrarFalloBeforeCommit();
            if (puntoFallo == PuntoFalloPrueba.OBSERVAR_BEFORE_COMMIT) registrarObservacionBeforeCommit(reserva);
            return resultado;
        } catch (RuntimeException excepcion) {
            registroUnicidadIdentidades.consumirAbortada(reserva);
            throw excepcion;
        } catch (Error error) {
            registroUnicidadIdentidades.marcarDesconocida(reserva);
            throw error;
        } finally {
            if (captura != null) inspector.descartarCaptura(captura);
        }
    }

    private void registrarFinalizacionTransaccion(
            ReservaUnicidad reserva, AtomicBoolean resultadoValidado) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            registroUnicidadIdentidades.marcarDesconocida(reserva);
            throw new IllegalStateException("F2E reader transaction completion not observable");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED && resultadoValidado.get()) {
                    registroUnicidadIdentidades.consumirExito(reserva);
                } else if (status == STATUS_ROLLED_BACK) {
                    registroUnicidadIdentidades.consumirAbortada(reserva);
                } else {
                    registroUnicidadIdentidades.marcarDesconocida(reserva);
                }
            }
        });
    }

    private void validarLabelsConfiables(String sourceContexto, String schemaContexto) {
        try {
            Object destino = org.springframework.test.util.AopTestUtils.getUltimateTargetObject(reader);
            Map<String, String> labels = autoridadLabelsConfiables(destino);
            if (destino.getClass() != com.feelingpilates.transicion.programacion.adapter.jpa.ReservaJpaReader.class
                    || !descriptor.sourceName.equals(sourceContexto)
                    || !descriptor.schemaFingerprint.equals(schemaContexto)
                    || !descriptor.sourceName.equals(labels.get("sourceNameConfiable"))
                    || !descriptor.identidadFuenteDatos.equals(labels.get("identidadFuenteDatosConfiable"))
                    || !descriptor.schemaFingerprint.equals(labels.get("schemaFingerprintConfiable"))) {
                throw new IllegalStateException("F2E reader resource provenance not proven");
            }
        } catch (Exception fallo) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
    }

    // Enumerate the entire instance-String authority, not a subset selected by trusted names.
    private Map<String, String> autoridadLabelsConfiables(Object destino) throws ReflectiveOperationException {
        Map<String, java.lang.reflect.Field> campos = new LinkedHashMap<>();
        for (Class<?> tipo = destino.getClass(); tipo != null; tipo = tipo.getSuperclass()) {
            for (var campo : tipo.getDeclaredFields()) {
                int modificadores = campo.getModifiers();
                if (campo.getType() == String.class && !java.lang.reflect.Modifier.isStatic(modificadores)) {
                    // Reject hidden duplicates before any set/map can discard their multiplicity.
                    if (campos.putIfAbsent(campo.getName(), campo) != null
                            || !java.lang.reflect.Modifier.isPrivate(modificadores)
                            || !java.lang.reflect.Modifier.isFinal(modificadores)) {
                        throw new IllegalStateException("F2E reader resource provenance not proven");
                    }
                }
            }
        }
        if (!campos.keySet().equals(Set.of(
                "sourceNameConfiable", "identidadFuenteDatosConfiable", "schemaFingerprintConfiable"))) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        Map<String, String> labels = new LinkedHashMap<>();
        for (var entrada : campos.entrySet()) {
            entrada.getValue().setAccessible(true);
            labels.put(entrada.getKey(), (String) entrada.getValue().get(destino));
        }
        return labels;
    }

    private Object labelPrivado(Object destino, String nombre) throws ReflectiveOperationException {
        var campo = destino.getClass().getDeclaredField(nombre);
        if (campo.getType() != String.class || !java.lang.reflect.Modifier.isPrivate(campo.getModifiers())
                || !java.lang.reflect.Modifier.isFinal(campo.getModifiers())) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        campo.setAccessible(true);
        return campo.get(destino);
    }

    private void registrarFalloBeforeCommit() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                throw new IllegalStateException("F2E controlled transaction completion failure");
            }
        });
    }

    private void registrarObservacionBeforeCommit(ReservaUnicidad reserva) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                estadoBeforeCommitPrueba.set(registroUnicidadIdentidades.estadoAttempt(reserva));
            }
        });
    }

    private ReferenciaConexion probarRecurso() {
        Session sesion = entityManager.unwrap(Session.class);
        return sesion.doReturningWork(conexion -> {
            try {
                ObservacionRecurso observacion = new ObservacionRecurso(
                        readerDataSource, entityManagerFactory, transactionManager, entityManager,
                        sesion, conexion,
                        entityManager.isJoinedToTransaction(), sesion.isDefaultReadOnly(),
                        sesion.getHibernateFlushMode() == FlushMode.MANUAL,
                        dataSourceContabilizado().urlConfigurada(), conexion.getMetaData().getURL(),
                        conexion.getCatalog(), conexion.getSchema(),
                        conexion.getMetaData().getUserName());
                validarRecurso(observacion, conexion);
                return referenciaConexion(conexion);
            } finally {
                dataSourceContabilizado().limpiarMutacionMetadatos();
            }
        });
    }

    private ReferenciaConexion obtenerConexion() {
        return entityManager.unwrap(Session.class).doReturningWork(this::referenciaConexion);
    }

    private ReferenciaConexion referenciaConexion(Connection conexion) {
        try {
            return new ReferenciaConexion(
                    conexion, conexion.unwrap(org.postgresql.PGConnection.class));
        } catch (SQLException excepcion) {
            throw new IllegalStateException("F2E reader physical connection provenance not proven", excepcion);
        }
    }

    private void validarRecurso(ObservacionRecurso observacion, Connection conexionFisicaObservada) {
        validarUrlJdbcOriginal(observacion.urlConfigurada());
        String urlCanonica = validarUrlJdbcOriginal(observacion.urlMetadata());
        if (observacion.readerDataSource() != descriptor.readerDataSource
                || observacion.entityManagerFactory() != descriptor.entityManagerFactory
                || observacion.transactionManager() != descriptor.transactionManager
                || observacion.entityManager() != descriptor.entityManager
                || observacion.transactionManager() == null
                || observacion.transactionManager().getEntityManagerFactory() != descriptor.entityManagerFactory
                || observacion.transactionManager().getDataSource() != descriptor.readerDataSource
                || !observacion.joined()
                || observacion.session() == null
                || observacion.session() != observacion.entityManager().unwrap(Session.class)
                || !observacion.sessionReadOnly()
                || !observacion.flushManual()
                || observacion.connection() == null
                || observacion.connection() != conexionFisicaObservada
                || !descriptor.jdbcUrlCanonicaSinCredenciales.equals(urlCanonica)
                || !descriptor.databaseName.equals(observacion.database())
                || !descriptor.schemaName.equals(observacion.schema())
                || !descriptor.credentialPrincipal.equals(observacion.principal())
                || urlCanonica == null) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
    }

    private String validarUrlJdbcOriginal(String urlOriginal) {
        if (urlOriginal == null || !urlOriginal.equals(descriptor.jdbcUrlCanonicaSinCredenciales)
                || urlOriginal.contains("?") || urlOriginal.contains("#")
                || urlOriginal.contains("@") || urlOriginal.contains("%")) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        Matcher partes = URL_JDBC_CANONICA.matcher(urlOriginal);
        if (!partes.matches()) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        int puerto;
        try {
            puerto = Integer.parseInt(partes.group(2));
        } catch (NumberFormatException excepcion) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        if (puerto < 1 || puerto > 65_535 || !Integer.toString(puerto).equals(partes.group(2))
                || !partes.group(3).equals(descriptor.databaseName)) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
        return urlOriginal;
    }

    private String ejecutarProbe(String sql) {
        Object resultado = descriptor.entityManager.createNativeQuery(sql).getSingleResult();
        return Objects.toString(resultado, null);
    }

    private void validarSemilla(SemillaLectura semilla) {
        if (semilla == null || textoInvalido(semilla.runIdentity()) || textoInvalido(semilla.attemptIdentity())
                || textoInvalido(semilla.transactionBoundaryIdentity())
                || textoInvalido(semilla.readerInvocationIdentity())
                || textoInvalido(semilla.ruleCatalogVersion()) || semilla.businessZone() == null) {
            throw new IllegalArgumentException("Invalid R1 read seed");
        }
        if (semilla.snapshotClaim() != ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST) {
            throw new IllegalArgumentException("Unsupported R1 snapshot claim");
        }
    }

    private boolean textoInvalido(String valor) {
        return valor == null || valor.isBlank() || valor.indexOf('\0') >= 0;
    }

    private void configurarMutacionRecursoPrueba(String mutacion) {
        MutacionRecursoPrueba seleccionada = MutacionRecursoPrueba.valueOf(mutacion);
        if (seleccionada.name().startsWith("JDBC_URL")
                || seleccionada == MutacionRecursoPrueba.DATABASE
                || seleccionada == MutacionRecursoPrueba.SCHEMA
                || seleccionada == MutacionRecursoPrueba.PRINCIPAL) {
            dataSourceContabilizado().configurarMutacionMetadatos(mutacion);
        }
        mutacionRecursoPrueba.set(seleccionada);
    }

    private void configurarFalloPrueba(String punto) {
        puntoFalloPrueba.set(PuntoFalloPrueba.valueOf(punto));
    }

    private void configurarMutacionManifiestoPrueba(String mutacion) {
        mutacionManifiestoPrueba.set(MutacionManifiestoPrueba.valueOf(mutacion));
    }

    private void configurarMutacionContextoPrueba(String mutacion) {
        mutacionContextoPrueba.set(MutacionContextoPrueba.valueOf(mutacion));
    }

    private String urlJdbcLectorPrueba() {
        return dataSourceContabilizado().urlConfigurada();
    }

    private Map<String, String> evidenciaUrlMetadataPrueba() {
        F2eSelectOnlyRole.DataSourceLectorContabilizado dataSource = dataSourceContabilizado();
        return Map.of(
                "DESCRIPTOR", descriptor.jdbcUrlCanonicaSinCredenciales,
                "CONFIGURADA", dataSource.urlConfigurada(),
                "ORIGINAL", Objects.requireNonNull(dataSource.ultimaUrlMetadataOriginal(), "ORIGINAL"),
                "ENTREGADA", Objects.requireNonNull(dataSource.ultimaUrlMetadataEntregada(), "ENTREGADA"),
                "OBSERVACIONES", Long.toString(dataSource.observacionesMetadataDesdeMutacion()),
                "CONEXIONES_ENTREGADAS", Long.toString(dataSource.conexionesEntregadas()));
    }

    private long preparacionesJdbcPrueba() {
        return dataSourceContabilizado().preparacionesJdbc();
    }

    private Map<String, Long> evidenciaPreJdbcPrueba() {
        return Map.of(
                "ANTES", Objects.requireNonNull(preparacionesAntesSqlHostil.get(), "ANTES"),
                "DESPUES", Objects.requireNonNull(preparacionesDespuesSqlHostil.get(), "DESPUES"));
    }

    private String estadoBeforeCommitPrueba() {
        return estadoBeforeCommitPrueba.get();
    }

    private F2eSelectOnlyRole.DataSourceLectorContabilizado dataSourceContabilizado() {
        if (!(readerDataSource instanceof F2eSelectOnlyRole.DataSourceLectorContabilizado dataSource)) {
            throw new IllegalStateException("F2E reader DataSource instrumentation not proven");
        }
        return dataSource;
    }

    private void configurarBloqueoActivoPrueba() {
        controlBloqueoPrueba = new ControlBloqueoPrueba(new CountDownLatch(1), new CountDownLatch(1));
        puntoFalloPrueba.set(PuntoFalloPrueba.BLOQUEAR_MIENTRAS_ACTIVE);
    }

    private boolean esperarReservaActivaPrueba() {
        try {
            return controlBloqueoPrueba != null
                    && controlBloqueoPrueba.reservada().await(10, TimeUnit.SECONDS);
        } catch (InterruptedException interrupcion) {
            Thread.currentThread().interrupt();
            throw new AssertionError(interrupcion);
        }
    }

    private void liberarReservaActivaPrueba() {
        if (controlBloqueoPrueba != null) controlBloqueoPrueba.continuar().countDown();
    }

    private void bloquearMientrasActive() {
        ControlBloqueoPrueba control = Objects.requireNonNull(controlBloqueoPrueba, "controlBloqueoPrueba");
        control.reservada().countDown();
        try {
            if (!control.continuar().await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("F2E controlled ACTIVE latch timeout");
            }
        } catch (InterruptedException interrupcion) {
            Thread.currentThread().interrupt();
            throw new Error("F2E controlled unresolved interruption", interrupcion);
        }
    }

    private void sembrarColisionPrueba(String dominio, SemillaLectura semilla) {
        registroUnicidadIdentidades.sembrarColisionPrueba(dominio, semilla, descriptor.identidadFuenteDatos);
    }

    private Map<String, String> estadosPrueba(SemillaLectura semilla) {
        return registroUnicidadIdentidades.estadosPrueba(semilla, descriptor.identidadFuenteDatos);
    }

    public record SemillaLectura(
            String runIdentity,
            String attemptIdentity,
            String transactionBoundaryIdentity,
            String readerInvocationIdentity,
            String ruleCatalogVersion,
            ZoneId businessZone,
            ReadSnapshotContext.SnapshotClaim snapshotClaim) {
    }

    private enum Operacion { READ_BY_RESERVATION_IDS, READ_BY_SCOPE }

    private enum MutacionRecursoPrueba {
        NINGUNA,
        JDBC_URL,
        JDBC_URL_QUERY,
        JDBC_URL_QUERY_DUPLICADA,
        JDBC_URL_FRAGMENTO,
        JDBC_URL_USER_INFO,
        JDBC_URL_PORCENTAJE,
        JDBC_URL_DATABASE,
        JDBC_URL_HOST,
        JDBC_URL_PUERTO,
        DATABASE,
        SCHEMA,
        PRINCIPAL
    }

    private enum PuntoFalloPrueba {
        NINGUNO,
        BLOQUEAR_MIENTRAS_ACTIVE,
        ANTES_DE_RECURSO_UNKNOWN,
        DESPUES_DE_IDENTIDADES_ABORTO,
        ROLLBACK_ONLY_TRAS_RESULTADO,
        FALLO_EN_BEFORE_COMMIT,
        OBSERVAR_BEFORE_COMMIT,
        SQL_DESCONOCIDA_ANTES_DE_JDBC
    }

    private enum MutacionManifiestoPrueba {
        NINGUNA,
        FALTA_PROBE,
        PROBE_EXTRA,
        ORDEN_INCORRECTO,
        DATA_EXTRA,
        CATALOGO_INCORRECTO
    }

    private enum MutacionContextoPrueba {
        NINGUNA,
        SOURCE_LABEL,
        SCHEMA_LABEL,
        EVIDENCIA_SNAPSHOT,
        HUELLA_STATEMENTS,
        READER_INVOCATION
    }

    private record ControlBloqueoPrueba(CountDownLatch reservada, CountDownLatch continuar) {
    }

    private record ObservacionRecurso(
            DataSource readerDataSource,
            EntityManagerFactory entityManagerFactory,
            JpaTransactionManager transactionManager,
            EntityManager entityManager,
            Session session,
            Connection connection,
            boolean joined,
            boolean sessionReadOnly,
            boolean flushManual,
            String urlConfigurada,
            String urlMetadata,
            String database,
            String schema,
            String principal) {
    }

    private record ReferenciaConexion(Connection conexionTransaccional, Object recursoFisico) {
    }

    @FunctionalInterface
    private interface Lectura {
        List<ReservationSourceSnapshot> leer(ReadSnapshotContext contexto);
    }

    private enum EstadoIdentidad {
        ACTIVE,
        CONSUMED_SUCCESS,
        CONSUMED_ABORTED,
        COMPLETED_SUCCESS,
        OPEN_AFTER_ABORT,
        UNKNOWN
    }

    private static final class RegistroUnicidadIdentidades {
        private final Map<ClaveBytes, EstadoIdentidad> estados = new HashMap<>();
        private final Map<ClaveBytes, ClaveBytes> intentoActivoPorRun = new HashMap<>();

        synchronized ReservaUnicidad reservar(SemillaLectura semilla, String identidadFuenteDatos) {
            ClaveBytes run = clave("F2E-R1-UNIQUENESS-RUN-V2", identidadFuenteDatos, semilla.runIdentity());
            ClaveBytes attempt = clave("F2E-R1-UNIQUENESS-ATTEMPT-V2", identidadFuenteDatos,
                    semilla.runIdentity(), semilla.attemptIdentity());
            ClaveBytes boundary = clave("F2E-R1-UNIQUENESS-BOUNDARY-V2", identidadFuenteDatos,
                    semilla.runIdentity(), semilla.attemptIdentity(), semilla.transactionBoundaryIdentity());
            ClaveBytes invocation = clave("F2E-R1-UNIQUENESS-INVOCATION-V2", identidadFuenteDatos,
                    semilla.runIdentity(), semilla.attemptIdentity(), semilla.readerInvocationIdentity());
            EstadoIdentidad estadoRun = estados.get(run);
            boolean runAdmisible = estadoRun == null || estadoRun == EstadoIdentidad.OPEN_AFTER_ABORT;
            if (!runAdmisible || estados.containsKey(attempt) || estados.containsKey(boundary)
                    || estados.containsKey(invocation) || intentoActivoPorRun.containsKey(run)) {
                throw new IllegalStateException("F2E execution provenance identity reuse");
            }
            estados.put(run, EstadoIdentidad.ACTIVE);
            estados.put(attempt, EstadoIdentidad.ACTIVE);
            estados.put(boundary, EstadoIdentidad.ACTIVE);
            estados.put(invocation, EstadoIdentidad.ACTIVE);
            intentoActivoPorRun.put(run, attempt);
            return new ReservaUnicidad(run, attempt, boundary, invocation);
        }

        synchronized void consumirExito(ReservaUnicidad reserva) {
            exigirActiva(reserva);
            estados.put(reserva.attempt, EstadoIdentidad.CONSUMED_SUCCESS);
            estados.put(reserva.boundary, EstadoIdentidad.CONSUMED_SUCCESS);
            estados.put(reserva.invocation, EstadoIdentidad.CONSUMED_SUCCESS);
            estados.put(reserva.run, EstadoIdentidad.COMPLETED_SUCCESS);
            intentoActivoPorRun.remove(reserva.run);
        }

        synchronized void consumirAbortada(ReservaUnicidad reserva) {
            if (estados.get(reserva.attempt) != EstadoIdentidad.ACTIVE) return;
            estados.put(reserva.attempt, EstadoIdentidad.CONSUMED_ABORTED);
            estados.put(reserva.boundary, EstadoIdentidad.CONSUMED_ABORTED);
            estados.put(reserva.invocation, EstadoIdentidad.CONSUMED_ABORTED);
            estados.put(reserva.run, EstadoIdentidad.OPEN_AFTER_ABORT);
            intentoActivoPorRun.remove(reserva.run);
        }

        synchronized void marcarDesconocida(ReservaUnicidad reserva) {
            if (estados.get(reserva.attempt) != EstadoIdentidad.ACTIVE) return;
            estados.put(reserva.attempt, EstadoIdentidad.UNKNOWN);
            estados.put(reserva.boundary, EstadoIdentidad.UNKNOWN);
            estados.put(reserva.invocation, EstadoIdentidad.UNKNOWN);
            estados.put(reserva.run, EstadoIdentidad.UNKNOWN);
        }

        synchronized String estadoAttempt(ReservaUnicidad reserva) {
            EstadoIdentidad estado = estados.get(reserva.attempt);
            return estado == null ? "ABSENT" : estado.name();
        }

        private void exigirActiva(ReservaUnicidad reserva) {
            if (estados.get(reserva.run) != EstadoIdentidad.ACTIVE
                    || estados.get(reserva.attempt) != EstadoIdentidad.ACTIVE
                    || estados.get(reserva.boundary) != EstadoIdentidad.ACTIVE
                    || estados.get(reserva.invocation) != EstadoIdentidad.ACTIVE) {
                throw new IllegalStateException("F2E execution provenance identity reuse");
            }
        }

        private ClaveBytes clave(String dominio, String... componentes) {
            return new ClaveBytes(ReadSnapshotIdentifiers.claveUnicidad(dominio, componentes));
        }

        synchronized void sembrarColisionPrueba(
                String dominio, SemillaLectura semilla, String identidadFuenteDatos) {
            Map<String, ClaveBytes> claves = claves(semilla, identidadFuenteDatos);
            ClaveBytes clave = Objects.requireNonNull(claves.get(dominio), "dominio");
            estados.put(clave, EstadoIdentidad.CONSUMED_ABORTED);
        }

        synchronized Map<String, String> estadosPrueba(
                SemillaLectura semilla, String identidadFuenteDatos) {
            Map<String, ClaveBytes> claves = claves(semilla, identidadFuenteDatos);
            Map<String, String> resultado = new LinkedHashMap<>();
            claves.forEach((dominio, clave) -> resultado.put(
                    dominio, estados.containsKey(clave) ? estados.get(clave).name() : "ABSENT"));
            return Map.copyOf(resultado);
        }

        private Map<String, ClaveBytes> claves(
                SemillaLectura semilla, String identidadFuenteDatos) {
            Map<String, ClaveBytes> claves = new LinkedHashMap<>();
            claves.put("RUN", clave("F2E-R1-UNIQUENESS-RUN-V2",
                    identidadFuenteDatos, semilla.runIdentity()));
            claves.put("ATTEMPT", clave("F2E-R1-UNIQUENESS-ATTEMPT-V2",
                    identidadFuenteDatos, semilla.runIdentity(), semilla.attemptIdentity()));
            claves.put("BOUNDARY", clave("F2E-R1-UNIQUENESS-BOUNDARY-V2",
                    identidadFuenteDatos, semilla.runIdentity(), semilla.attemptIdentity(),
                    semilla.transactionBoundaryIdentity()));
            claves.put("INVOCATION", clave("F2E-R1-UNIQUENESS-INVOCATION-V2",
                    identidadFuenteDatos, semilla.runIdentity(), semilla.attemptIdentity(),
                    semilla.readerInvocationIdentity()));
            return claves;
        }
    }

    private record ReservaUnicidad(ClaveBytes run, ClaveBytes attempt, ClaveBytes boundary, ClaveBytes invocation) { }

    private static final class ClaveBytes {
        private final byte[] bytes;

        private ClaveBytes(byte[] bytes) { this.bytes = bytes.clone(); }

        @Override
        public boolean equals(Object otro) {
            return otro instanceof ClaveBytes clave && Arrays.equals(bytes, clave.bytes);
        }

        @Override
        public int hashCode() { return Arrays.hashCode(bytes); }
    }
}
