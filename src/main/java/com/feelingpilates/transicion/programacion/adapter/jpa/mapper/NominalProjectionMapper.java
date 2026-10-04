package com.feelingpilates.transicion.programacion.adapter.jpa.mapper;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.read.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import java.time.LocalDate;
import java.util.*;

public final class NominalProjectionMapper {
    private final NominalProjectionCatalog catalog;
    public NominalProjectionMapper(NominalProjectionCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
        if (catalog != NominalProjectionCatalog.R3_NOMINAL_V1) throw new IllegalStateException("R3 catalog binding");
    }
    public NominalProjectionCatalog catalog() { return catalog; }
    public NominalProgrammingReadSet mapear(List<NominalProjectionRow> rows,
                                            NominalReadSnapshotContext context, LocalDate fecha) {
        if (rows == null || context == null || fecha == null || !fecha.equals(context.fecha()))
            throw new NominalReadFailure(NominalReadFailure.Category.INVALID_INPUT, fecha);
        // Validate the complete physical projection before any duplicate/cardinality classification or output.
        for (var row : rows) validate(row, fecha);
        var seen = new HashMap<UUID, NominalProjectionRow>();
        var physicalAssignments = new HashSet<UUID>();
        var physicalBlocks = new HashMap<UUID, List<Object>>();
        NominalProjectionRow previous = null;
        for (var r : rows) {
            var prior = seen.putIfAbsent(r.assignmentSeriesId(), r);
            if (prior != null) throw new NominalReadFailure(NominalReadFailure.Category.DUPLICATE_SERIES_ON_DATE,
                    fecha, List.of(prior.assignmentId(), prior.blockId(), r.assignmentId(), r.blockId()), null);
            List<Object> blockPayload = Arrays.asList(r.blockSeriesId(),r.salonId(),r.blockStart(),r.blockEnd(),
                    r.blockFrom(),r.blockUntil(),r.blockActive(),r.blockCreated(),r.blockUpdated(),r.blockDay());
            List<Object> priorBlock = physicalBlocks.putIfAbsent(r.blockId(),blockPayload);
            if (!physicalAssignments.add(r.assignmentId()) || (priorBlock != null && !priorBlock.equals(blockPayload)))
                throw new NominalReadFailure(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                        fecha, List.of(r.assignmentId(),r.blockId()), null);
            if (previous != null && compare(previous, r) >= 0)
                throw new NominalReadFailure(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH, fecha);
            previous = r;
        }
        List<ProgrammingCandidateSnapshot> candidates = new ArrayList<>();
        List<NominalBackingSnapshot> backing = new ArrayList<>();
        for (var r : rows) {
            var ref = new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION, r.assignmentSeriesId(), fecha);
            Map<String, String> physical = fields(r, context);
            String fingerprint = NominalReadSnapshotContext.hash("F2E-R3-PROJECTION-V1",
                    ReadSnapshotIdentifiers.decodificarUtf8(ReadSnapshotIdentifiers.mapaCanonico(physical)));
            Map<String,String> observable = Map.of("salonId", r.salonId().toString(),
                    "instructorId", r.instructorId().toString(), "activityId", r.activityId().toString(),
                    "start", ReadSnapshotIdentifiers.hora(r.assignmentStart()),
                    "end", ReadSnapshotIdentifiers.hora(r.assignmentEnd()), "fecha", fecha.toString());
            var provenance = new EvidenceProvenance(context.sourceName(), context.schemaFingerprint(),
                    List.of(r.assignmentId().toString(), r.blockId().toString()), "R3_NOMINAL_PROJECTION", "V1",
                    context.businessZone().getId() + "/" + fecha, physical);
            candidates.add(new ProgrammingCandidateSnapshot(ref, DetectorVocabulary.CandidateType.NOMINAL_OCCURRENCE,
                    NominalReadSnapshotContext.hash("F2E-R3-SNAPSHOT-V1", context.executionProvenanceId(), fingerprint),
                    NominalReadSnapshotContext.hash("F2E-R3-CANDIDATE-V1", ref.toString(),
                            ReadSnapshotIdentifiers.decodificarUtf8(ReadSnapshotIdentifiers.mapaCanonico(observable))),
                    r.salonId(), r.instructorId(), r.activityId(), r.assignmentStart(), r.assignmentEnd(), observable, provenance));
            backing.add(new NominalBackingSnapshot(ref, fecha, context.dayOfWeek(),
                    r.assignmentSeriesId(), r.assignmentId(), r.assignmentBlockId(), r.blockId(), r.blockSeriesId(), r.salonId(), r.instructorId(), r.activityId(), r.assignmentStart(), r.assignmentEnd(), r.blockStart(), r.blockEnd(), r.assignmentFrom(), r.assignmentUntil(), r.assignmentActive(), r.blockFrom(), r.blockUntil(), r.blockActive(), r.assignmentCreated(), r.assignmentUpdated(), r.blockCreated(), r.blockUpdated(), r.blockDay()));
        }
        return new NominalProgrammingReadSet(candidates, backing);
    }
    private void validate(NominalProjectionRow r, LocalDate fecha) {
        try {
            Objects.requireNonNull(r);
            Objects.requireNonNull(r.assignmentSeriesId());
            Objects.requireNonNull(r.assignmentId());
            Objects.requireNonNull(r.assignmentBlockId());
            Objects.requireNonNull(r.blockId());
            Objects.requireNonNull(r.blockSeriesId());
            Objects.requireNonNull(r.salonId());
            Objects.requireNonNull(r.instructorId());
            Objects.requireNonNull(r.activityId());
            Objects.requireNonNull(r.assignmentStart());
            Objects.requireNonNull(r.assignmentEnd());
            Objects.requireNonNull(r.blockStart());
            Objects.requireNonNull(r.blockEnd());
            Objects.requireNonNull(r.assignmentFrom());
            Objects.requireNonNull(r.assignmentActive());
            Objects.requireNonNull(r.blockFrom());
            Objects.requireNonNull(r.blockActive());
            Objects.requireNonNull(r.assignmentCreated());
            Objects.requireNonNull(r.assignmentUpdated());
            Objects.requireNonNull(r.blockCreated());
            Objects.requireNonNull(r.blockUpdated());
            Objects.requireNonNull(r.blockDay());
            if (!r.assignmentBlockId().equals(r.blockId()) || !Boolean.TRUE.equals(r.assignmentActive())
                    || !Boolean.TRUE.equals(r.blockActive()) || r.blockDay() != (short)(fecha.getDayOfWeek().getValue() % 7)
                    || !r.assignmentEnd().isAfter(r.assignmentStart()) || !r.blockEnd().isAfter(r.blockStart())
                    || r.assignmentStart().isBefore(r.blockStart()) || r.assignmentEnd().isAfter(r.blockEnd())
                    || !applicable(r.assignmentFrom(), r.assignmentUntil(), fecha)
                    || !applicable(r.blockFrom(), r.blockUntil(), fecha)) throw new IllegalArgumentException();
            ReadSnapshotIdentifiers.hora(r.assignmentStart()); ReadSnapshotIdentifiers.hora(r.assignmentEnd());
            ReadSnapshotIdentifiers.hora(r.blockStart()); ReadSnapshotIdentifiers.hora(r.blockEnd());
            ReadSnapshotIdentifiers.instante(r.assignmentCreated()); ReadSnapshotIdentifiers.instante(r.assignmentUpdated());
            ReadSnapshotIdentifiers.instante(r.blockCreated()); ReadSnapshotIdentifiers.instante(r.blockUpdated());
        } catch (RuntimeException e) {
            var ids = r == null ? List.<UUID>of() : java.util.stream.Stream.of(r.assignmentId(),r.blockId())
                    .filter(Objects::nonNull).toList();
            throw new NominalReadFailure(NominalReadFailure.Category.MALFORMED_PROJECTION, fecha, ids, e);
        }
    }
    private boolean applicable(LocalDate from, LocalDate until, LocalDate fecha) {
        return !from.isAfter(fecha) && (until == null || (!until.isBefore(from) && !until.isBefore(fecha)));
    }
    private int compare(NominalProjectionRow a, NominalProjectionRow b) {
        int c = compareUuid(a.assignmentSeriesId(), b.assignmentSeriesId());
        return c == 0 ? compareUuid(a.assignmentId(), b.assignmentId()) : c;
    }
    private int compareUuid(UUID a, UUID b) {
        return Arrays.compareUnsigned(java.nio.ByteBuffer.allocate(16).putLong(a.getMostSignificantBits())
                .putLong(a.getLeastSignificantBits()).array(), java.nio.ByteBuffer.allocate(16)
                .putLong(b.getMostSignificantBits()).putLong(b.getLeastSignificantBits()).array());
    }
    private Map<String,String> fields(NominalProjectionRow r, NominalReadSnapshotContext c) {
        Map<String,String> m = new LinkedHashMap<>();
        m.put("assignmentSeriesId", String.valueOf(r.assignmentSeriesId()));
        m.put("assignmentId", String.valueOf(r.assignmentId()));
        m.put("assignmentBlockId", String.valueOf(r.assignmentBlockId()));
        m.put("blockId", String.valueOf(r.blockId()));
        m.put("blockSeriesId", String.valueOf(r.blockSeriesId()));
        m.put("salonId", String.valueOf(r.salonId()));
        m.put("instructorId", String.valueOf(r.instructorId()));
        m.put("activityId", String.valueOf(r.activityId()));
        m.put("assignmentStart", ReadSnapshotIdentifiers.hora(r.assignmentStart()));
        m.put("assignmentEnd", ReadSnapshotIdentifiers.hora(r.assignmentEnd()));
        m.put("blockStart", ReadSnapshotIdentifiers.hora(r.blockStart()));
        m.put("blockEnd", ReadSnapshotIdentifiers.hora(r.blockEnd()));
        m.put("assignmentFrom", String.valueOf(r.assignmentFrom()));
        m.put("assignmentUntil", r.assignmentUntil() == null ? "NULL" : String.valueOf(r.assignmentUntil()));
        m.put("assignmentActive", String.valueOf(r.assignmentActive()));
        m.put("blockFrom", String.valueOf(r.blockFrom()));
        m.put("blockUntil", r.blockUntil() == null ? "NULL" : String.valueOf(r.blockUntil()));
        m.put("blockActive", String.valueOf(r.blockActive()));
        m.put("assignmentCreated", ReadSnapshotIdentifiers.instante(r.assignmentCreated()));
        m.put("assignmentUpdated", ReadSnapshotIdentifiers.instante(r.assignmentUpdated()));
        m.put("blockCreated", ReadSnapshotIdentifiers.instante(r.blockCreated()));
        m.put("blockUpdated", ReadSnapshotIdentifiers.instante(r.blockUpdated()));
        m.put("blockDay", String.valueOf(r.blockDay()));
        m.put("fecha", c.fecha().toString()); m.put("dayOfWeek", Short.toString(c.dayOfWeek()));
        m.put("snapshotEvidenceId", c.snapshotEvidenceId());
        m.put("executionProvenanceId", c.executionProvenanceId());
        m.put("statementCaptureCommitment", c.statementCaptureCommitment());
        return Map.copyOf(m);
    }
}
