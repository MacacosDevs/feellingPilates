package com.feelingpilates.transicion.programacion.adapter.jpa.mapper;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.ReservaProjectionRow;
import com.feelingpilates.transicion.programacion.detector.DetectorVocabulary;
import com.feelingpilates.transicion.programacion.detector.EvidenceProvenance;
import com.feelingpilates.transicion.programacion.detector.ReservationSourceSnapshot;
import com.feelingpilates.transicion.programacion.detector.ReservedSubinterval;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import com.feelingpilates.transicion.programacion.read.ReservationReadException;
import com.feelingpilates.transicion.programacion.read.ReservationReadFailureCode;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class ReservaProjectionMapper {

    private final ReadSnapshotContext.ProjectionCatalogVersion catalogo;
    private final UnaryOperator<ReservationSourceSnapshot> verificadorSalida;

    public ReservaProjectionMapper() {
        this(ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1);
    }

    public ReservaProjectionMapper(ReadSnapshotContext.ProjectionCatalogVersion catalogo) {
        this(catalogo, catalogo.mapperContract(), catalogo.mapperVersion());
    }

    private ReservaProjectionMapper(
            ReadSnapshotContext.ProjectionCatalogVersion catalogo,
            String contratoMapper,
            String versionMapper) {
        this(catalogo, contratoMapper, versionMapper, UnaryOperator.identity());
    }

    private ReservaProjectionMapper(
            ReadSnapshotContext.ProjectionCatalogVersion catalogo,
            String contratoMapper,
            String versionMapper,
            UnaryOperator<ReservationSourceSnapshot> verificadorSalida) {
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        catalogo.validarVinculoMapper(contratoMapper, versionMapper);
        this.verificadorSalida = Objects.requireNonNull(verificadorSalida, "verificadorSalida");
    }

    public ReadSnapshotContext.ProjectionCatalogVersion catalogo() {
        return catalogo;
    }

    public ReservationSourceSnapshot mapear(
            ReservaProjectionRow fila,
            int ordinal,
            ReadSnapshotContext contexto,
            String operacion,
            byte[] alcance,
            String executionProvenanceId,
            String logicalSnapshotId,
            ReadSnapshotIdentifiers.SesionCalculo calculos) {
        Objects.requireNonNull(calculos, "calculos");
        try {
            validarCatalogo(contexto);
            validarFilaFisica(fila, ordinal, operacion);
            DetectorVocabulary.ReservationState estado = DetectorVocabulary.ReservationState.valueOf(fila.state());
            byte[] proyeccion = calculos.proyeccionCanonica(fila);
            String sourceFingerprint = calculos.calcularHuellaFuente(contexto, fila, proyeccion);
            String snapshotIdentity = calculos.calcularIdentidadSnapshot(
                    contexto, fila.reservationId(), logicalSnapshotId, executionProvenanceId, sourceFingerprint);

            String creado = ReadSnapshotIdentifiers.instante(fila.createdAtTechnical());
            String actualizado = ReadSnapshotIdentifiers.instante(fila.updatedAtTechnical());
            Map<String, String> observables = Map.of(
                    "createdAtTechnical", creado,
                    "updatedAtTechnical", actualizado);
            String scopeCanonical = ReadSnapshotIdentifiers.decodificarUtf8(alcance);
            String contextoNegocio = ReadSnapshotIdentifiers.decodificarUtf8(
                    ReadSnapshotIdentifiers.secuencia(List.of(
                            "F2E-R1-BUSINESS-CONTEXT-V2".getBytes(StandardCharsets.UTF_8),
                            contexto.businessZone().getId().getBytes(StandardCharsets.UTF_8),
                            alcance.clone())));
            Map<String, String> campos = camposProvenance(
                    fila, contexto, operacion, scopeCanonical, executionProvenanceId,
                    logicalSnapshotId, sourceFingerprint, snapshotIdentity, creado, actualizado);
            EvidenceProvenance provenance = new EvidenceProvenance(
                    contexto.sourceName(), contexto.schemaFingerprint(),
                    List.of(fila.reservationId().toString()), catalogo.projectionContractId(),
                    catalogo.projectionContractVersion(),
                    contextoNegocio, campos);

            return verificadorSalida.apply(new ReservationSourceSnapshot(
                    fila.reservationId(), estado, fila.date(), fila.salonId(), fila.instructorId(),
                    fila.activityId(), new ReservedSubinterval(fila.start(), fila.end()), snapshotIdentity,
                    sourceFingerprint, observables, Optional.empty(), provenance));
        } catch (ReservationReadException excepcion) {
            throw excepcion;
        } catch (IllegalArgumentException excepcion) {
            throw new ReservationReadException(
                    ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                    Map.of("operation", operacion, "physicalRowOrdinal", Integer.toString(ordinal)),
                    excepcion);
        }
    }

    public void validarFilaFisica(ReservaProjectionRow fila, int ordinal, String operacion) {
        if (fila == null) throw invalida(operacion, ordinal, "id", null);
        validarNoNulo(fila.reservationId(), operacion, ordinal, "id");
        validarNoNulo(fila.state(), operacion, ordinal, "estado");
        validarNoNulo(fila.date(), operacion, ordinal, "fecha");
        validarNoNulo(fila.salonId(), operacion, ordinal, "salon_id");
        validarNoNulo(fila.instructorId(), operacion, ordinal, "instructor_id");
        validarNoNulo(fila.activityId(), operacion, ordinal, "tipo_actividad_id");
        validarNoNulo(fila.start(), operacion, ordinal, "hora_inicio");
        validarNoNulo(fila.end(), operacion, ordinal, "hora_fin");
        validarNoNulo(fila.createdAtTechnical(), operacion, ordinal, "creado_en");
        validarNoNulo(fila.updatedAtTechnical(), operacion, ordinal, "actualizado_en");
        validarConversion(() -> DetectorVocabulary.ReservationState.valueOf(fila.state()),
                operacion, ordinal, "estado");
        validarConversion(() -> ReadSnapshotIdentifiers.hora(fila.start()),
                operacion, ordinal, "hora_inicio");
        validarConversion(() -> ReadSnapshotIdentifiers.hora(fila.end()),
                operacion, ordinal, "hora_fin");
        validarConversion(() -> ReadSnapshotIdentifiers.instante(fila.createdAtTechnical()),
                operacion, ordinal, "creado_en");
        validarConversion(() -> ReadSnapshotIdentifiers.instante(fila.updatedAtTechnical()),
                operacion, ordinal, "actualizado_en");
        if (!fila.end().isAfter(fila.start())) {
            throw invalida(operacion, ordinal, "hora_fin", null);
        }
    }

    private void validarCatalogo(ReadSnapshotContext contexto) {
        if (contexto == null || contexto.projectionCatalogVersion() != catalogo) {
            throw new IllegalStateException("F2E projection catalog binding not proven");
        }
    }

    private void validarNoNulo(Object valor, String operacion, int ordinal, String columna) {
        if (valor == null) throw invalida(operacion, ordinal, columna, null);
    }

    private void validarConversion(Runnable conversion, String operacion, int ordinal, String columna) {
        try {
            conversion.run();
        } catch (IllegalArgumentException excepcion) {
            throw invalida(operacion, ordinal, columna, excepcion);
        }
    }

    private ReservationReadException invalida(
            String operacion, int ordinal, String columna, Throwable causa) {
        return new ReservationReadException(
                ReservationReadFailureCode.ADAPTER_INPUT_INVALID,
                Map.of("operation", operacion,
                        "physicalRowOrdinal", Integer.toString(ordinal),
                        "physicalColumn", columna),
                causa);
    }

    private Map<String, String> camposProvenance(
            ReservaProjectionRow fila,
            ReadSnapshotContext contexto,
            String operacion,
            String alcance,
            String executionProvenanceId,
            String logicalSnapshotId,
            String sourceFingerprint,
            String snapshotIdentity,
            String creado,
            String actualizado) {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("activityId", fila.activityId().toString());
        campos.put("attemptIdentity", contexto.attemptIdentity());
        campos.put("businessZone", contexto.businessZone().getId());
        campos.put("createdAtTechnical", creado);
        campos.put("date", ReadSnapshotIdentifiers.fecha(fila.date()));
        campos.put("end", ReadSnapshotIdentifiers.hora(fila.end()));
        campos.put("executionProvenanceId", executionProvenanceId);
        campos.put("historicalProgrammingTarget", "ABSENT");
        campos.put("instructorId", fila.instructorId().toString());
        campos.put("logicalSnapshotId", logicalSnapshotId);
        campos.put("operation", operacion);
        campos.put("projectionCatalogVersion", contexto.projectionCatalogVersion().canonicalCatalogValue());
        campos.put("projectionContractId", catalogo.projectionContractId());
        campos.put("projectionContractVersion", catalogo.projectionContractVersion());
        campos.put("readerInvocationIdentity", contexto.readerInvocationIdentity());
        campos.put("reservationId", fila.reservationId().toString());
        campos.put("ruleCatalogVersion", contexto.ruleCatalogVersion());
        campos.put("runIdentity", contexto.runIdentity());
        campos.put("salonId", fila.salonId().toString());
        campos.put("scopeCanonical", alcance);
        campos.put("snapshotClaim", contexto.snapshotClaim().name());
        campos.put("snapshotEvidenceId", contexto.snapshotEvidenceId());
        campos.put("snapshotIdentity", snapshotIdentity);
        campos.put("sourceAtomType", catalogo.sourceAtomType());
        campos.put("sourceFingerprint", sourceFingerprint);
        campos.put("sourceSystem", catalogo.sourceSystem());
        campos.put("start", ReadSnapshotIdentifiers.hora(fila.start()));
        campos.put("state", fila.state());
        campos.put("statementObservationFingerprint", contexto.statementObservationFingerprint());
        campos.put("transactionAccessMode", "read only");
        campos.put("transactionIsolation", "read committed");
        campos.put("updatedAtTechnical", actualizado);
        if (campos.size() != 32) {
            throw new IllegalStateException("F2E identity/provenance consistency not proven");
        }
        return Map.copyOf(campos);
    }
}
