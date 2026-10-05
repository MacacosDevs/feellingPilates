package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.detector.EvidenceProvenance;
import com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;

/** Explicit V1 schemas. There is no reflective or arbitrary-object serialization. */
public final class EffectiveCompositionCanonicalizer {
    private EffectiveCompositionCanonicalizer() { }
    public static final Comparator<UUID> UUID_ORDER = (a,b) -> {
        int c=Long.compareUnsigned(a.getMostSignificantBits(),b.getMostSignificantBits());
        return c!=0?c:Long.compareUnsigned(a.getLeastSignificantBits(),b.getLeastSignificantBits());
    };
    public static final Comparator<ReferenciaOcurrencia> REFERENCE_ORDER = Comparator
            .comparing(ReferenciaOcurrencia::fecha).thenComparingInt(r->r.tipo().ordinal())
            .thenComparing(ReferenciaOcurrencia::id,UUID_ORDER);
    static byte[] text(String s) {
        if(s==null||s.indexOf('\0')>=0)throw new IllegalArgumentException("strict UTF8 text");
        try {
            var buffer=StandardCharsets.UTF_8.newEncoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT).encode(java.nio.CharBuffer.wrap(s));
            byte[] bytes=new byte[buffer.remaining()];buffer.get(bytes);return bytes;
        } catch(java.nio.charset.CharacterCodingException e) {throw new IllegalArgumentException("strict UTF8 text",e);}
    }
    public static byte[] sequence(byte[]... parts) { return ReadSnapshotIdentifiers.secuencia(Arrays.asList(parts)); }
    static byte[] framed(List<byte[]> parts) { return ReadSnapshotIdentifiers.secuencia(parts); }
    private static byte[] scalar(String type,String value) { return sequence(text("VALUE"),text(type),text(value)); }
    static byte[] record(String name,Object... fields) {
        var sorted=new TreeMap<String,Object>((a,b)->Arrays.compareUnsigned(text(a),text(b)));
        for(int i=0;i<fields.length;i+=2) sorted.put((String)fields[i],fields[i+1]);
        var parts=new ArrayList<byte[]>();
        parts.add(text("RECORD")); parts.add(text(name)); parts.add(text("V1")); parts.add(text(Integer.toString(sorted.size())));
        sorted.forEach((k,v)->{parts.add(text(k));parts.add(encode(v));});
        return framed(parts);
    }
    static List<?> sortedRows(List<?> rows) {
        return rows.stream().sorted((a,b)->Arrays.compareUnsigned(encode(a),encode(b))).toList();
    }
    public static byte[] encode(Object value) {
        try { return encodeValue(value); }
        catch (IllegalArgumentException e) {
            throw new EffectiveCompositionFailure(EffectiveCompositionFailure.Category.CANONICALIZATION_FAILURE,
                    null,List.of(),List.of(),"strict V1 scalar/schema",null,e);
        }
    }
    private static byte[] encodeValue(Object value) {
        if(value==null) return sequence(text("ABSENT"));
        if(value instanceof String v) return scalar("String",v);
        if(value instanceof UUID v) return scalar("UUID",v.toString());
        if(value instanceof LocalDate v) return scalar("LocalDate",v.toString());
        if(value instanceof LocalTime v) return scalar("LocalTime",ReadSnapshotIdentifiers.hora(v));
        if(value instanceof OffsetDateTime v) return scalar("Instant",ReadSnapshotIdentifiers.instante(v));
        if(value instanceof Boolean v) return scalar("Boolean",v.toString());
        if(value instanceof Short v) return scalar("Short",Short.toString(v));
        if(value instanceof Integer v) return scalar("Integer",Integer.toString(v));
        if(value instanceof Enum<?> v) return scalar(v.getDeclaringClass().getSimpleName(),v.name());
        if(value instanceof List<?> v) {
            var parts=new ArrayList<byte[]>();parts.add(text("LIST"));parts.add(text(Integer.toString(v.size())));
            for(var item:v) parts.add(encode(item));return framed(parts);
        }
        if(value instanceof Map<?,?> v) {
            var entries=new ArrayList<Map.Entry<?,?>>(v.entrySet());
            entries.sort((a,b)->Arrays.compareUnsigned(encode(a.getKey()),encode(b.getKey())));
            var parts=new ArrayList<byte[]>();parts.add(text("MAP"));parts.add(text(Integer.toString(v.size())));
            for(var e:entries) { parts.add(encode(e.getKey()));parts.add(encode(e.getValue())); }
            return framed(parts);
        }
        if(value instanceof ReferenciaOcurrencia v) return record("ReferenciaOcurrencia",
                "tipo", v.tipo(), "id", v.id(), "fecha", v.fecha());
        if(value instanceof EvidenceProvenance v) return record("EvidenceProvenance",
                "sourceName", v.sourceName(), "schemaFingerprint", v.schemaFingerprint(), "recordIds", v.recordIds(), "ruleId", v.ruleId(), "ruleVersion", v.ruleVersion(), "businessTimeContext", v.businessTimeContext(), "normalizedFields", v.normalizedFields());
        if(value instanceof GenericSourceSnapshot v) return record("GenericSourceSnapshot",
                "sourceSystem", v.sourceSystem(), "sourceAtomType", v.sourceAtomType(), "sourceIdentity", v.sourceIdentity(), "snapshotIdentity", v.snapshotIdentity(), "sourceFingerprint", v.sourceFingerprint(), "observableFields", v.observableFields(), "provenance", v.provenance());
        if(value instanceof ProgrammingCandidateSnapshot v) return record("ProgrammingCandidateSnapshot",
                "reference", v.reference(), "candidateType", v.candidateType(), "snapshotIdentity", v.snapshotIdentity(), "candidateFingerprint", v.candidateFingerprint(), "salonId", v.salonId(), "instructorId", v.instructorId(), "activityId", v.activityId(), "start", v.start(), "end", v.end(), "observableFields", v.observableFields(), "provenance", v.provenance());
        if(value instanceof AdjustmentBackingSnapshot v) return record("AdjustmentBackingSnapshot",
                "id", v.id(), "tipo", v.tipo(), "fecha", v.fecha(), "asignacionSerieId", v.asignacionSerieId(), "salonResultadoId", v.salonResultadoId(), "instructorResultadoId", v.instructorResultadoId(), "tipoActividadResultadoId", v.tipoActividadResultadoId(), "horaInicioResultado", v.horaInicioResultado(), "horaFinResultado", v.horaFinResultado(), "activo", v.activo(), "creadoEn", v.creadoEn(), "actualizadoEn", v.actualizadoEn());
        if(value instanceof AdjustmentReadSet v) return record("AdjustmentReadSet",
                "sources", v.sources(), "backing", v.backing());
        if(value instanceof EffectiveCompositionBacking v) return record("EffectiveCompositionBacking",
                "reference", v.reference(), "origin", v.origin(), "salonId", v.salonId(), "instructorId", v.instructorId(), "activityId", v.activityId(), "start", v.start(), "end", v.end(), "nominalAxis", v.nominalAxis(), "nominalCandidate", v.nominalCandidate(), "nominalBacking", v.nominalBacking(), "adjustmentAxis", v.adjustmentAxis(), "adjustmentSource", v.adjustmentSource(), "adjustmentBacking", v.adjustmentBacking(), "support", v.support());
        if(value instanceof EffectiveCompositionEnvelope v) return record("EffectiveCompositionEnvelope",
                "mode", v.mode(), "date", v.date(), "businessZoneId", v.businessZoneId(), "invocationIdentity", v.invocationIdentity(), "authorityVersion", v.authorityVersion(), "completionReceiptIdentity", v.completionReceiptIdentity(), "participants", v.participants());
        if(value instanceof EffectiveCompositionEnvelope.Participant v) return record("Participant",
                "sourceName", v.sourceName(), "schemaFingerprint", v.schemaFingerprint(), "ruleVersion", v.ruleVersion(), "readScope", v.readScope(), "executionIdentity", v.executionIdentity(), "snapshotIdentity", v.snapshotIdentity(), "physicalResourceIdentity", v.physicalResourceIdentity(), "transactionBoundaryIdentity", v.transactionBoundaryIdentity(), "statementCaptureIdentity", v.statementCaptureIdentity(), "completion", v.completion(), "runIdentity", v.runIdentity(), "attemptIdentity", v.attemptIdentity(), "ruleCatalogVersion", v.ruleCatalogVersion(), "databaseName", v.databaseName(), "schemaName", v.schemaName(), "principal", v.principal(), "projectionCatalogVersion", v.projectionCatalogVersion(), "readerInvocationIdentity", v.readerInvocationIdentity(), "snapshotClaim", v.snapshotClaim(), "snapshotEvidenceId", v.snapshotEvidenceId(), "statementCaptureCommitment", v.statementCaptureCommitment());
        if(value instanceof EffectiveCompositionInput v) return record("EffectiveCompositionInput",
                "date", v.date(), "businessZoneId", v.businessZoneId(), "ruleVersion", v.ruleVersion(), "envelope", v.envelope(), "nominalReadSet", v.nominalReadSet(), "adjustmentReadSet", v.adjustmentReadSet(), "validityEvidence", v.validityEvidence());
        if(value instanceof EffectiveCompositionOmission v) return record("EffectiveCompositionOmission",
                "reference", v.reference(), "backing", v.backing(), "cause", v.cause(), "support", v.support(), "ruleVersion", v.ruleVersion(), "schemaFingerprint", v.schemaFingerprint(), "envelope", v.envelope());
        if(value instanceof EffectiveCompositionSuppression v) return record("EffectiveCompositionSuppression",
                "reference", v.reference(), "nominalCandidate", v.nominalCandidate(), "nominalBacking", v.nominalBacking(), "adjustmentSource", v.adjustmentSource(), "adjustmentBacking", v.adjustmentBacking(), "ruleVersion", v.ruleVersion(), "schemaFingerprint", v.schemaFingerprint(), "envelope", v.envelope());
        if(value instanceof EffectiveValidityEvidence v) return record("EffectiveValidityEvidence",
                "salons", v.salons(), "instructors", v.instructors(), "activities", v.activities(), "hours", v.hours(), "roles", v.roles(), "specializations", v.specializations(), "offerings", v.offerings());
        if(value instanceof EffectiveValidityEvidence.RelationMetadata v) return record("RelationMetadata",
                "sourceName", v.sourceName(), "schemaFingerprint", v.schemaFingerprint(), "ruleVersion", v.ruleVersion(), "participantName", v.participantName(), "participantExecutionIdentity", v.participantExecutionIdentity(), "scope", v.scope(), "complete", v.complete(), "recordKeys", v.recordKeys().stream().sorted((a,b)->Arrays.compareUnsigned(text(a),text(b))).toList());
        if(value instanceof EffectiveValidityEvidence.Presence<?> v) return record("Presence",
                "kind", v.kind(), "queriedId", v.queriedId(), "value", v.value(), "metadata", v.metadata());
        if(value instanceof EffectiveValidityEvidence.Relation<?> v) return record("Relation",
                "metadata", v.metadata(), "rows", sortedRows(v.rows()));
        if(value instanceof EffectiveValidityEvidence.Hours v) return record("Hours",
                "exceptions", v.exceptions(), "weekly", v.weekly());
        if(value instanceof EffectiveValidityEvidence.Salon v) return record("Salon",
                "id", v.id(), "activo", v.activo());
        if(value instanceof EffectiveValidityEvidence.Instructor v) return record("Instructor",
                "id", v.id(), "estatus", v.estatus());
        if(value instanceof EffectiveValidityEvidence.Activity v) return record("Activity",
                "id", v.id(), "activo", v.activo());
        if(value instanceof EffectiveValidityEvidence.DateException v) return record("DateException",
                "id", v.id(), "salonId", v.salonId(), "date", v.date(), "cerrado", v.cerrado(), "opening", v.opening(), "closing", v.closing(), "activo", v.activo());
        if(value instanceof EffectiveValidityEvidence.WeeklyHours v) return record("WeeklyHours",
                "id", v.id(), "salonId", v.salonId(), "day", v.day(), "opening", v.opening(), "closing", v.closing(), "validFrom", v.validFrom(), "validUntil", v.validUntil());
        if(value instanceof EffectiveValidityEvidence.RoleEdge v) return record("RoleEdge",
                "usuarioId", v.usuarioId(), "rolId", v.rolId(), "roleName", v.roleName(), "nullableSalonId", v.nullableSalonId());
        if(value instanceof EffectiveValidityEvidence.SpecializationEdge v) return record("SpecializationEdge",
                "usuarioId", v.usuarioId(), "activityId", v.activityId());
        if(value instanceof EffectiveValidityEvidence.OfferingEdge v) return record("OfferingEdge",
                "salonId", v.salonId(), "activityId", v.activityId());
        if(value instanceof NominalBackingSnapshot v) return record("NominalBackingSnapshot",
                "reference", v.reference(), "fecha", v.fecha(), "dayOfWeek", v.dayOfWeek(), "assignmentSeriesId", v.assignmentSeriesId(), "assignmentId", v.assignmentId(), "assignmentBlockId", v.assignmentBlockId(), "blockId", v.blockId(), "blockSeriesId", v.blockSeriesId(), "salonId", v.salonId(), "instructorId", v.instructorId(), "activityId", v.activityId(), "assignmentStart", v.assignmentStart(), "assignmentEnd", v.assignmentEnd(), "blockStart", v.blockStart(), "blockEnd", v.blockEnd(), "assignmentFrom", v.assignmentFrom(), "assignmentUntil", v.assignmentUntil(), "assignmentActive", v.assignmentActive(), "blockFrom", v.blockFrom(), "blockUntil", v.blockUntil(), "blockActive", v.blockActive(), "assignmentCreated", v.assignmentCreated(), "assignmentUpdated", v.assignmentUpdated(), "blockCreated", v.blockCreated(), "blockUpdated", v.blockUpdated(), "blockDay", v.blockDay());
        if(value instanceof NominalProgrammingReadSet v) return record("NominalProgrammingReadSet",
                "candidates", v.candidates(), "backing", v.backing());
        throw new IllegalArgumentException("unsupported canonical schema");
    }
    static String closedHash(String... fields) {
        return digest(ReadSnapshotIdentifiers.secuenciaTextos(fields));
    }
    private static String digest(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch(java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    /** Per-composition collision registry, with no shared mutable state. */
    static final class Session {
        private final Map<String,byte[]> preimages=new HashMap<>();
        String hash(String domain,Object... values) {
            var parts=new ArrayList<byte[]>();parts.add(text(domain));
            for(var v:values) parts.add(encode(v));
            return register(digest(framed(parts)),framed(parts));
        }
        // Narrow test seam exercises collision rejection without replacing production SHA256.
        String register(String digest,byte[] preimage) {
            byte[] previous=preimages.putIfAbsent(digest,preimage.clone());
            if(previous!=null && !Arrays.equals(previous,preimage))
                throw new EffectiveCompositionFailure(EffectiveCompositionFailure.Category.CANONICALIZATION_FAILURE,
                        null,List.of(),List.of(),"same digest with different canonical preimage",null,null);
            return digest;
        }
    }
}
