package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.ReservaProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionQueryExecutor;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionRow;
import com.feelingpilates.transicion.programacion.detector.ReservationSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import com.feelingpilates.transicion.programacion.read.ReservationReadException;
import com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode;
import com.feelingpilates.transicion.programacion.read.ReservationReadPort;
import com.feelingpilates.transicion.programacion.read.ReservationScope;
import jakarta.persistence.PersistenceException;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ReservaJpaReader implements ReservationReadPort {

    private static final String OPERACION_IDS = "READ_BY_RESERVATION_IDS";
    private static final String OPERACION_SCOPE = "READ_BY_SCOPE";

    private final ReservaProjectionQueryExecutor ejecutor;
    private final ReservaProjectionMapper mapper;
    private final ReadSnapshotContext.ProjectionCatalogVersion catalogo;
    private final String sourceNameConfiable;
    private final String identidadFuenteDatosConfiable;
    private final String schemaFingerprintConfiable;

    public ReservaJpaReader(
            ReservaProjectionQueryExecutor ejecutor,
            ReservaProjectionMapper mapper,
            String sourceNameConfiable,
            String identidadFuenteDatosConfiable,
            String schemaFingerprintConfiable) {
        this(ejecutor, mapper, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1,
                sourceNameConfiable, identidadFuenteDatosConfiable, schemaFingerprintConfiable);
    }

    public ReservaJpaReader(
            ReservaProjectionQueryExecutor ejecutor,
            ReservaProjectionMapper mapper,
            ReadSnapshotContext.ProjectionCatalogVersion catalogo,
            String sourceNameConfiable,
            String identidadFuenteDatosConfiable,
            String schemaFingerprintConfiable) {
        this.ejecutor = Objects.requireNonNull(ejecutor, "ejecutor");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        if (ejecutor.catalogo() != catalogo || mapper.catalogo() != catalogo) {
            throw new IllegalStateException("F2E projection catalog binding not proven");
        }
        this.sourceNameConfiable = textoRequerido(sourceNameConfiable, "sourceNameConfiable");
        this.identidadFuenteDatosConfiable = textoRequerido(
                identidadFuenteDatosConfiable, "identidadFuenteDatosConfiable");
        this.schemaFingerprintConfiable = textoRequerido(
                schemaFingerprintConfiable, "schemaFingerprintConfiable");
    }

    @Override
    @Transactional(
            transactionManager = "f2eReaderTransactionManager",
            propagation = Propagation.MANDATORY,
            readOnly = true)
    public List<ReservationSourceSnapshot> readByReservationIds(
            ReadSnapshotContext context,
            Set<UUID> reservationIds) {
        validarContexto(context, OPERACION_IDS);
        Set<UUID> copia = validarIdentidades(reservationIds);
        byte[] alcance = ReadSnapshotIdentifiers.alcancePorIdentidades(copia);
        List<UUID> ordenadas = ReadSnapshotIdentifiers.ordenarUuid(copia);
        Map<String, String> contextoSeguro = contextoIds(ordenadas);
        try {
            List<ReservaProjectionRow> filas = consultarIds(ordenadas, contextoSeguro);
            validarFilasPorIdentidades(filas, copia, contextoSeguro);
            return ensamblar(filas, context, OPERACION_IDS, alcance, contextoSeguro);
        } catch (RuntimeException excepcion) {
            throw propagar(excepcion, OPERACION_IDS, catalogo.statementIdByIds(), contextoSeguro);
        }
    }

    @Override
    @Transactional(
            transactionManager = "f2eReaderTransactionManager",
            propagation = Propagation.MANDATORY,
            readOnly = true)
    public List<ReservationSourceSnapshot> readByScope(
            ReadSnapshotContext context,
            ReservationScope scope) {
        validarContexto(context, OPERACION_SCOPE);
        if (scope == null) {
            throw invalido(OPERACION_SCOPE, Map.of("scopeKind", "BY_SCOPE"));
        }
        byte[] alcance = ReadSnapshotIdentifiers.alcancePorScope(scope);
        List<UUID> salones = ReadSnapshotIdentifiers.ordenarUuid(scope.salonIds());
        Map<String, String> contextoSeguro = contextoScope(scope, salones);
        try {
            List<ReservaProjectionRow> filas = consultarScope(scope, salones, contextoSeguro);
            validarFilasPorScope(filas, scope, contextoSeguro);
            return ensamblar(filas, context, OPERACION_SCOPE, alcance, contextoSeguro);
        } catch (RuntimeException excepcion) {
            throw propagar(excepcion, OPERACION_SCOPE, catalogo.statementIdByScope(), contextoSeguro);
        }
    }

    private List<ReservaProjectionRow> consultarIds(
            List<UUID> identidades, Map<String, String> contextoSeguro) {
        try {
            return ejecutor.consultarPorIdentidades(identidades);
        } catch (IllegalArgumentException excepcion) {
            throw new ReservationReadException(
                    ReservationReadFailureCode.ADAPTER_INPUT_INVALID, contextoSeguro, excepcion);
        }
    }

    private List<ReservaProjectionRow> consultarScope(
            ReservationScope scope, List<UUID> salones, Map<String, String> contextoSeguro) {
        try {
            return ejecutor.consultarPorScope(salones, scope.desde(), scope.hasta());
        } catch (IllegalArgumentException excepcion) {
            throw new ReservationReadException(
                    ReservationReadFailureCode.ADAPTER_INPUT_INVALID, contextoSeguro, excepcion);
        }
    }

    private List<ReservationSourceSnapshot> ensamblar(
            List<ReservaProjectionRow> filas,
            ReadSnapshotContext contexto,
            String operacion,
            byte[] alcance,
            Map<String, String> contextoSeguro) {
        ReadSnapshotIdentifiers.SesionCalculo calculos = ReadSnapshotIdentifiers.nuevaSesionCalculo();
        String executionProvenanceId = calculos.calcularProvenanceEjecucion(contexto, operacion, alcance);
        String logicalSnapshotId = calculos.calcularSnapshotLogico(contexto, alcance);
        List<ReservationSourceSnapshot> snapshots = new ArrayList<>(filas.size());
        for (int indice = 0; indice < filas.size(); indice++) {
            snapshots.add(mapper.mapear(
                    filas.get(indice), indice + 1, contexto, operacion, alcance,
                    executionProvenanceId, logicalSnapshotId, calculos));
        }
        if (snapshots.size() != filas.size()) {
            throw invariante(contextoSeguro, Map.of("returnedCount", Integer.toString(snapshots.size())));
        }
        for (int indice = 0; indice < filas.size(); indice++) {
            verificarConsistencia(
                    filas.get(indice), snapshots.get(indice), contexto, operacion, alcance,
                    executionProvenanceId, logicalSnapshotId, calculos);
        }
        snapshots.sort((a, b) -> ReadSnapshotIdentifiers.ordenarUuid(List.of(a.reservationId(), b.reservationId()))
                .getFirst().equals(a.reservationId()) ? (a.reservationId().equals(b.reservationId()) ? 0 : -1) : 1);
        return List.copyOf(snapshots);
    }

    private void verificarConsistencia(
            ReservaProjectionRow fila,
            ReservationSourceSnapshot snapshot,
            ReadSnapshotContext contexto,
            String operacion,
            byte[] alcance,
            String executionProvenanceId,
            String logicalSnapshotId,
            ReadSnapshotIdentifiers.SesionCalculo calculos) {
        ReadSnapshotIdentifiers.SesionCalculo verificador = ReadSnapshotIdentifiers.nuevaSesionCalculo();
        String executionRecomputado = verificador.calcularProvenanceEjecucion(contexto, operacion, alcance);
        String logicalRecomputado = verificador.calcularSnapshotLogico(contexto, alcance);
        byte[] proyeccion = verificador.proyeccionCanonica(fila);
        String sourceFingerprint = verificador.calcularHuellaFuente(contexto, fila, proyeccion);
        String snapshotIdentity = verificador.calcularIdentidadSnapshot(
                contexto, fila.reservationId(), logicalRecomputado, executionRecomputado, sourceFingerprint);
        Map<String, String> campos = snapshot.provenance().normalizedFields();
        Map<String, String> camposEsperados = camposEsperados(
                fila, contexto, operacion, alcance, executionRecomputado, logicalRecomputado,
                sourceFingerprint, snapshotIdentity);
        String contextoNegocioEsperado = ReadSnapshotIdentifiers.decodificarUtf8(
                ReadSnapshotIdentifiers.secuencia(List.of(
                        "F2E-R1-BUSINESS-CONTEXT-V2".getBytes(StandardCharsets.UTF_8),
                        contexto.businessZone().getId().getBytes(StandardCharsets.UTF_8),
                        alcance.clone())));
        boolean consistente = fila.reservationId().equals(snapshot.reservationId())
                && fila.state().equals(snapshot.state().name())
                && fila.date().equals(snapshot.date())
                && fila.salonId().equals(snapshot.salonId())
                && fila.instructorId().equals(snapshot.instructorId())
                && fila.activityId().equals(snapshot.activityId())
                && fila.start().equals(snapshot.reservedSubinterval().start())
                && fila.end().equals(snapshot.reservedSubinterval().end())
                && snapshot.historicalProgrammingTarget().isEmpty()
                && snapshot.additionalObservableFields().equals(Map.of(
                        "createdAtTechnical", ReadSnapshotIdentifiers.instante(fila.createdAtTechnical()),
                        "updatedAtTechnical", ReadSnapshotIdentifiers.instante(fila.updatedAtTechnical())))
                && snapshot.provenance().recordIds().equals(List.of(fila.reservationId().toString()))
                && snapshot.provenance().sourceName().equals(contexto.sourceName())
                && snapshot.provenance().schemaFingerprint().equals(contexto.schemaFingerprint())
                && snapshot.provenance().ruleId().equals(catalogo.projectionContractId())
                && snapshot.provenance().ruleVersion().equals(catalogo.projectionContractVersion())
                && snapshot.provenance().businessTimeContext().equals(contextoNegocioEsperado)
                && campos.equals(camposEsperados)
                && iguales(executionProvenanceId, executionRecomputado)
                && iguales(logicalSnapshotId, logicalRecomputado)
                && iguales(sourceFingerprint, snapshot.sourceFingerprint())
                && iguales(snapshotIdentity, snapshot.snapshotIdentity())
                && iguales(executionRecomputado, campos.get("executionProvenanceId"))
                && iguales(logicalRecomputado, campos.get("logicalSnapshotId"))
                && iguales(contexto.snapshotEvidenceId(), campos.get("snapshotEvidenceId"))
                && iguales(contexto.statementObservationFingerprint(), campos.get("statementObservationFingerprint"))
                && iguales(sourceFingerprint, campos.get("sourceFingerprint"))
                && iguales(snapshotIdentity, campos.get("snapshotIdentity"));
        if (!consistente) {
            throw new IllegalStateException("F2E identity/provenance consistency not proven");
        }
    }

    private Map<String, String> camposEsperados(
            ReservaProjectionRow fila,
            ReadSnapshotContext contexto,
            String operacion,
            byte[] alcance,
            String executionProvenanceId,
            String logicalSnapshotId,
            String sourceFingerprint,
            String snapshotIdentity) {
        Map<String, String> esperados = new LinkedHashMap<>();
        esperados.put("activityId", fila.activityId().toString());
        esperados.put("attemptIdentity", contexto.attemptIdentity());
        esperados.put("businessZone", contexto.businessZone().getId());
        esperados.put("createdAtTechnical", ReadSnapshotIdentifiers.instante(fila.createdAtTechnical()));
        esperados.put("date", ReadSnapshotIdentifiers.fecha(fila.date()));
        esperados.put("end", ReadSnapshotIdentifiers.hora(fila.end()));
        esperados.put("executionProvenanceId", executionProvenanceId);
        esperados.put("historicalProgrammingTarget", "ABSENT");
        esperados.put("instructorId", fila.instructorId().toString());
        esperados.put("logicalSnapshotId", logicalSnapshotId);
        esperados.put("operation", operacion);
        esperados.put("projectionCatalogVersion", catalogo.canonicalCatalogValue());
        esperados.put("projectionContractId", catalogo.projectionContractId());
        esperados.put("projectionContractVersion", catalogo.projectionContractVersion());
        esperados.put("readerInvocationIdentity", contexto.readerInvocationIdentity());
        esperados.put("reservationId", fila.reservationId().toString());
        esperados.put("ruleCatalogVersion", contexto.ruleCatalogVersion());
        esperados.put("runIdentity", contexto.runIdentity());
        esperados.put("salonId", fila.salonId().toString());
        esperados.put("scopeCanonical", ReadSnapshotIdentifiers.decodificarUtf8(alcance));
        esperados.put("snapshotClaim", contexto.snapshotClaim().name());
        esperados.put("snapshotEvidenceId", contexto.snapshotEvidenceId());
        esperados.put("snapshotIdentity", snapshotIdentity);
        esperados.put("sourceAtomType", catalogo.sourceAtomType());
        esperados.put("sourceFingerprint", sourceFingerprint);
        esperados.put("sourceSystem", catalogo.sourceSystem());
        esperados.put("start", ReadSnapshotIdentifiers.hora(fila.start()));
        esperados.put("state", fila.state());
        esperados.put("statementObservationFingerprint", contexto.statementObservationFingerprint());
        esperados.put("transactionAccessMode", "read only");
        esperados.put("transactionIsolation", "read committed");
        esperados.put("updatedAtTechnical", ReadSnapshotIdentifiers.instante(fila.updatedAtTechnical()));
        if (esperados.size() != 32) {
            throw new IllegalStateException("F2E identity/provenance consistency not proven");
        }
        return Map.copyOf(esperados);
    }

    private boolean iguales(String izquierda, String derecha) {
        return izquierda != null && derecha != null && MessageDigest.isEqual(
                izquierda.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                derecha.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private void validarContexto(ReadSnapshotContext contexto, String operacion) {
        if (contexto == null) {
            throw invalido(operacion, Map.of());
        }
        if (contexto.snapshotClaim() != ReadSnapshotContext.SnapshotClaim.SINGLE_READER_TEST) {
            throw new IllegalArgumentException("Unsupported R1 snapshot claim");
        }
        if (contexto.projectionCatalogVersion() != catalogo) {
            throw new IllegalStateException("F2E identity/provenance consistency not proven");
        }
        if (!sourceNameConfiable.equals(contexto.sourceName())
                || !schemaFingerprintConfiable.equals(contexto.schemaFingerprint())
                || identidadFuenteDatosConfiable.isBlank()) {
            throw new IllegalStateException("F2E reader resource provenance not proven");
        }
    }

    private Set<UUID> validarIdentidades(Set<UUID> identidades) {
        if (identidades == null || identidades.isEmpty() || identidades.stream().anyMatch(id -> id == null)) {
            throw invalido(OPERACION_IDS, Map.of("scopeKind", "BY_RESERVATION_IDS"));
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(ReadSnapshotIdentifiers.ordenarUuid(identidades)));
    }

    private void validarFilasPorIdentidades(
            List<ReservaProjectionRow> filas,
            Set<UUID> solicitadas,
            Map<String, String> contextoSeguro) {
        validarFilasFisicas(filas, OPERACION_IDS);
        Map<UUID, Integer> conteos = contar(filas);
        Set<UUID> inesperadas = new LinkedHashSet<>(conteos.keySet());
        inesperadas.removeAll(solicitadas);
        Set<UUID> duplicadas = duplicadas(conteos);
        if (!inesperadas.isEmpty() || !duplicadas.isEmpty()) {
            Map<String, String> extras = new LinkedHashMap<>();
            if (!inesperadas.isEmpty()) extras.put("unexpectedReservationIds", serializar(inesperadas));
            if (!duplicadas.isEmpty()) extras.put("duplicateReservationIds", serializar(duplicadas));
            throw invariante(contextoSeguro, extras);
        }
        Set<UUID> faltantes = new LinkedHashSet<>(solicitadas);
        faltantes.removeAll(conteos.keySet());
        if (!faltantes.isEmpty()) {
            Map<String, String> seguro = combinar(contextoSeguro, Map.of(
                    "missingReservationIds", serializar(faltantes),
                    "returnedCount", Integer.toString(filas.size())));
            throw new ReservationReadException(ReservationReadFailureCode.SOURCE_RECORD_NOT_FOUND, seguro);
        }
    }

    private void validarFilasPorScope(
            List<ReservaProjectionRow> filas,
            ReservationScope scope,
            Map<String, String> contextoSeguro) {
        validarFilasFisicas(filas, OPERACION_SCOPE);
        Set<UUID> duplicadas = duplicadas(contar(filas));
        boolean fuera = filas.stream().anyMatch(fila ->
                !scope.salonIds().contains(fila.salonId())
                || fila.date().isBefore(scope.desde()) || fila.date().isAfter(scope.hasta()));
        if (fuera || !duplicadas.isEmpty()) {
            Map<String, String> extras = duplicadas.isEmpty() ? Map.of()
                    : Map.of("duplicateReservationIds", serializar(duplicadas));
            throw invariante(contextoSeguro, extras);
        }
    }

    private Map<UUID, Integer> contar(List<ReservaProjectionRow> filas) {
        Map<UUID, Integer> conteos = new LinkedHashMap<>();
        for (ReservaProjectionRow fila : filas) {
            if (fila != null && fila.reservationId() != null) {
                conteos.merge(fila.reservationId(), 1, Integer::sum);
            }
        }
        return conteos;
    }

    private void validarFilasFisicas(List<ReservaProjectionRow> filas, String operacion) {
        if (filas == null) {
            throw new ReservationReadException(
                    ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                    Map.of("operation", operacion, "physicalRowOrdinal", "1", "physicalColumn", "id"));
        }
        for (int indice = 0; indice < filas.size(); indice++) {
            mapper.validarFilaFisica(filas.get(indice), indice + 1, operacion);
        }
    }

    private Set<UUID> duplicadas(Map<UUID, Integer> conteos) {
        Set<UUID> resultado = new LinkedHashSet<>();
        conteos.forEach((id, cuenta) -> { if (cuenta > 1) resultado.add(id); });
        return resultado;
    }

    private ReservationReadException invalido(String operacion, Map<String, String> extras) {
        return new ReservationReadException(
                ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                combinar(Map.of("operation", operacion,
                        "projectionContractId", catalogo.projectionContractId()), extras));
    }

    private ReservationReadException invariante(
            Map<String, String> base, Map<String, String> extras) {
        return new ReservationReadException(
                ReservationReadFailureCode.READ_SET_INVARIANT_VIOLATION, combinar(base, extras));
    }

    private Map<String, String> contextoIds(List<UUID> identidades) {
        return Map.of(
                "operation", OPERACION_IDS,
                "projectionContractId", catalogo.projectionContractId(),
                "scopeKind", "BY_RESERVATION_IDS",
                "reservationIds", serializar(identidades),
                "requestedCount", Integer.toString(identidades.size()),
                "sqlCatalogStatementId", catalogo.statementIdByIds());
    }

    private Map<String, String> contextoScope(ReservationScope scope, List<UUID> salones) {
        return Map.of(
                "operation", OPERACION_SCOPE,
                "projectionContractId", catalogo.projectionContractId(),
                "scopeKind", "BY_SCOPE",
                "salonIds", serializar(salones),
                "fromDate", scope.desde().toString(),
                "toDate", scope.hasta().toString(),
                "sqlCatalogStatementId", catalogo.statementIdByScope());
    }

    private String serializar(java.util.Collection<UUID> identidades) {
        return String.join(",", ReadSnapshotIdentifiers.ordenarUuid(identidades).stream().map(UUID::toString).toList());
    }

    private Map<String, String> combinar(Map<String, String> base, Map<String, String> extras) {
        Map<String, String> resultado = new LinkedHashMap<>(base);
        resultado.putAll(extras);
        return Map.copyOf(resultado);
    }

    private RuntimeException propagar(
            RuntimeException excepcion,
            String operacion,
            String catalogo,
            Map<String, String> contextoSeguro) {
        if (excepcion instanceof ReservationReadException lectura) {
            return lectura;
        }
        F2eSqlPolicyViolationException policy = encontrar(excepcion, F2eSqlPolicyViolationException.class);
        if (policy != null) {
            return policy;
        }
        Throwable fisica = encontrarFisica(excepcion);
        if (fisica != null) {
            Map<String, String> seguro = new LinkedHashMap<>(contextoSeguro);
            seguro.put("operation", operacion);
            seguro.put("sqlCatalogStatementId", catalogo);
            SQLException sql = encontrar(excepcion, SQLException.class);
            if (sql != null) {
                if (sql.getSQLState() != null && !sql.getSQLState().isEmpty()) {
                    String clase = sql.getSQLState().substring(0, Math.min(2, sql.getSQLState().length()));
                    if (clase.matches("[0-9A-Z]{1,2}")) seguro.put("sqlStateClass", clase);
                }
                seguro.put("vendorErrorCode", Integer.toString(sql.getErrorCode()));
            }
            return new ReservationReadException(
                    ReservationReadFailureCode.SOURCE_ACCESS_FAILURE, seguro, excepcion);
        }
        return excepcion;
    }

    private Throwable encontrarFisica(Throwable origen) {
        Throwable actual = origen;
        Set<Throwable> visitadas = Collections.newSetFromMap(new IdentityHashMap<>());
        while (actual != null && visitadas.add(actual)) {
            if (actual instanceof PersistenceException || actual instanceof HibernateException
                    || actual instanceof SQLException) {
                return actual;
            }
            actual = actual.getCause();
        }
        return null;
    }

    private <T extends Throwable> T encontrar(Throwable origen, Class<T> tipo) {
        Throwable actual = origen;
        Set<Throwable> visitadas = Collections.newSetFromMap(new IdentityHashMap<>());
        while (actual != null && visitadas.add(actual)) {
            if (tipo.isInstance(actual)) {
                return tipo.cast(actual);
            }
            actual = actual.getCause();
        }
        return null;
    }

    private String textoRequerido(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " is required");
        }
        return valor;
    }
}
