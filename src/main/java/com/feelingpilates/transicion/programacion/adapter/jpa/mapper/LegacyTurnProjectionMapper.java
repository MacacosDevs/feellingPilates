package com.feelingpilates.transicion.programacion.adapter.jpa.mapper;

import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyAssignmentRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnMemberRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.detector.DetectorVocabulary;
import com.feelingpilates.transicion.programacion.detector.EvidenceProvenance;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.LegacyAdapterInputInvalid;
import com.feelingpilates.transicion.programacion.read.LegacyAdapterRejection;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadSet;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class LegacyTurnProjectionMapper {

    private static final String MEMBERS = "R2_LEGACY_MEMBERS_V1";
    private static final String ASSIGNMENTS = "R2_LEGACY_ASSIGNMENTS_V1";
    private static final DateTimeFormatter TIME_MICROS = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter INSTANT_MICROS =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
    private static final Set<String> TYPES = Set.of("RECURRENTE", "EXCEPCION", "CANCELACION");
    private static final Set<String> ANOMALY_MARKERS = Set.of(
            "ABSENT_MEMBER", "ABSENT_ASSIGNMENT", "ABSENT_ACTIVITY",
            "NON_MEMBER_ASSIGNMENT", "INCOMPLETE_RANGE", "RANGE_OUTSIDE_TURN");

    private final LegacyTurnProjectionCatalog catalog;

    public LegacyTurnProjectionMapper(LegacyTurnProjectionCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        if (catalog != LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1
                || !"LegacyTurnProjectionMapper/V1".equals(catalog.mapperContract())) {
            throw new IllegalStateException("R2 mapper catalog binding not proven");
        }
    }

    public LegacyTurnProjectionCatalog catalog() {
        return catalog;
    }

    public List<UUID> usableParentTurnIds(List<LegacyTurnMemberRow> memberRows) {
        if (memberRows == null) throw invalidCaller();
        return memberRows.stream().filter(Objects::nonNull).map(LegacyTurnMemberRow::turnId)
                .filter(Objects::nonNull).distinct().sorted().toList();
    }

    public LegacyTurnReadSet mapear(
            List<LegacyTurnMemberRow> memberRows,
            List<LegacyAssignmentRow> assignmentRows,
            LegacyTurnReadContext context,
            LegacyTurnScope scope) {
        if (memberRows == null || assignmentRows == null || context == null || scope == null
                || context.projectionCatalogVersion() != catalog.version()
                || !context.scopeCanonical().equals(scope.canonical())) {
            throw invalidCaller();
        }
        if (memberRows.isEmpty()) {
            if (!assignmentRows.isEmpty()) {
                throw new LegacyAdapterInputInvalid(List.of(rejectionInvariant(
                        ASSIGNMENTS, scope, "ORPHAN_TURN_HEADER", List.of(), 1, assignmentRows.size())));
            }
            return new LegacyTurnReadSet(List.of());
        }

        List<LegacyAdapterRejection> rejections = new ArrayList<>();
        Map<UUID, List<IndexedMember>> membersByTurn = new HashMap<>();
        for (int index = 0; index < memberRows.size(); index++) {
            LegacyTurnMemberRow row = memberRows.get(index);
            if (row == null || row.turnId() == null) {
                rejections.add(rejectionInput(MEMBERS, scope, "INVALID_REQUIRED_FIELD", List.of(), index + 1, 1));
            } else {
                membersByTurn.computeIfAbsent(row.turnId(), ignored -> new ArrayList<>())
                        .add(new IndexedMember(index + 1, row));
            }
        }

        Map<UUID, List<IndexedAssignment>> assignmentsByTurn = new HashMap<>();
        for (int index = 0; index < assignmentRows.size(); index++) {
            LegacyAssignmentRow row = assignmentRows.get(index);
            if (row == null || row.turnId() == null) {
                rejections.add(rejectionInput(ASSIGNMENTS, scope, "INVALID_REQUIRED_FIELD", List.of(), index + 1, 1));
            } else if (!membersByTurn.containsKey(row.turnId())) {
                rejections.add(rejectionInvariant(ASSIGNMENTS, scope, "ORPHAN_TURN_HEADER",
                        safeIds(row.turnId(), row.instructorId(), row.activityId()), index + 1, 1));
            } else {
                assignmentsByTurn.computeIfAbsent(row.turnId(), ignored -> new ArrayList<>())
                        .add(new IndexedAssignment(index + 1, row));
            }
        }

        List<Generated> generated = new ArrayList<>();
        List<UUID> turns = membersByTurn.keySet().stream()
                .sorted(LegacyTurnReadContext.UUID_UNSIGNED).toList();
        for (UUID turnId : turns) {
            List<IndexedMember> physicalMembers = membersByTurn.get(turnId);
            List<IndexedAssignment> physicalAssignments = assignmentsByTurn.getOrDefault(turnId, List.of());
            mapTurn(turnId, physicalMembers, physicalAssignments, context, scope, generated, rejections);
        }

        if (!rejections.isEmpty()) {
            throw new LegacyAdapterInputInvalid(List.copyOf(rejections));
        }

        generated.sort(Generated.ORDER);
        List<GenericSourceSnapshot> sources = generated.stream().map(Generated::source).toList();
        if (new HashSet<>(sources.stream().map(GenericSourceSnapshot::sourceIdentity).toList()).size()
                != sources.size()) {
            throw new LegacyAdapterInputInvalid(List.of(rejectionInvariant(
                    MEMBERS, scope, "DUPLICATE_LOGICAL_ATOM", List.of(), 1, 2)));
        }
        return new LegacyTurnReadSet(sources);
    }

    public String logicalReadSetFingerprint(LegacyTurnReadSet readSet) {
        List<byte[]> entries = readSet.sources().stream().map(source ->
                LegacyTurnReadContext.secuenciaTextos(
                        source.sourceSystem().name(), source.sourceAtomType().name(),
                        source.sourceIdentity(), source.sourceFingerprint()))
                .sorted(Arrays::compareUnsigned).toList();
        List<byte[]> parts = new ArrayList<>();
        parts.add(LegacyTurnReadContext.utf8("F2E-READSET-V1"));
        parts.addAll(entries);
        return LegacyTurnReadContext.sha256(LegacyTurnReadContext.secuencia(parts));
    }

    private void mapTurn(
            UUID turnId,
            List<IndexedMember> physicalMembers,
            List<IndexedAssignment> physicalAssignments,
            LegacyTurnReadContext context,
            LegacyTurnScope scope,
            List<Generated> generated,
            List<LegacyAdapterRejection> rejections) {
        LegacyTurnMemberRow header = physicalMembers.getFirst().row();
        boolean headerValid = validHeader(header, scope);
        boolean headersCompatible = physicalMembers.stream().allMatch(indexed -> sameHeader(header, indexed.row()));

        List<UUID> members = physicalMembers.stream().map(IndexedMember::row)
                .map(LegacyTurnMemberRow::memberInstructorId).filter(Objects::nonNull)
                .distinct().sorted(LegacyTurnReadContext.UUID_UNSIGNED).toList();
        long nullMemberRows = physicalMembers.stream().filter(row -> row.row().memberInstructorId() == null).count();
        boolean membershipShapeValid = (members.isEmpty() && nullMemberRows == 1 && physicalMembers.size() == 1)
                || (!members.isEmpty() && nullMemberRows == 0);

        Map<UUID, Long> memberMultiplicity = new LinkedHashMap<>();
        physicalMembers.stream().map(IndexedMember::row).map(LegacyTurnMemberRow::memberInstructorId)
                .filter(Objects::nonNull).forEach(member -> memberMultiplicity.merge(member, 1L, Long::sum));
        memberMultiplicity.entrySet().stream().filter(entry -> entry.getValue() > 1).forEach(entry ->
                rejections.add(rejectionInvariant(MEMBERS, scope, "DUPLICATE_PHYSICAL_ROW",
                        safeIds(turnId, entry.getKey()), physicalMembers.getFirst().ordinal(),
                        Math.toIntExact(entry.getValue()))));

        Map<AssignmentKey, List<IndexedAssignment>> assignmentsByKey = new LinkedHashMap<>();
        List<IndexedAssignment> invalidAssignmentKeys = new ArrayList<>();
        for (IndexedAssignment indexed : physicalAssignments) {
            LegacyAssignmentRow row = indexed.row();
            if (row.instructorId() == null || row.activityId() == null) {
                invalidAssignmentKeys.add(indexed);
            } else {
                assignmentsByKey.computeIfAbsent(new AssignmentKey(row.turnId(), row.instructorId(), row.activityId()),
                        ignored -> new ArrayList<>()).add(indexed);
            }
        }
        invalidAssignmentKeys.forEach(indexed -> rejections.add(rejectionInput(
                ASSIGNMENTS, scope, "INVALID_REQUIRED_FIELD", safeIds(turnId), indexed.ordinal(), 1)));
        assignmentsByKey.entrySet().stream().filter(entry -> entry.getValue().size() > 1).forEach(entry ->
                rejections.add(rejectionInvariant(ASSIGNMENTS, scope, "DUPLICATE_PHYSICAL_ROW",
                        safeIds(entry.getKey().turnId(), entry.getKey().instructorId(), entry.getKey().activityId()),
                        entry.getValue().getFirst().ordinal(), entry.getValue().size())));

        List<IndexedAssignment> uniqueAssignments = assignmentsByKey.values().stream()
                .map(List::getFirst).sorted(Comparator.comparing((IndexedAssignment row) -> row.row().instructorId(),
                                LegacyTurnReadContext.UUID_UNSIGNED)
                        .thenComparing(row -> row.row().activityId(), LegacyTurnReadContext.UUID_UNSIGNED)).toList();
        List<Atom> atoms = atoms(members, uniqueAssignments);

        boolean turnRejected = !headerValid || !headersCompatible || !membershipShapeValid;
        String turnMarker = !headerValid ? "INVALID_REQUIRED_FIELD"
                : !headersCompatible ? "DUPLICATE_LOGICAL_ATOM" : "INVALID_SOURCE_TYPE";
        if (turnRejected) {
            for (int index = 0; index < atoms.size(); index++) {
                Atom atom = atoms.get(index);
                rejections.add(rejectionInput(MEMBERS, scope, turnMarker,
                        safeIds(turnId, atom.instructorId(), atom.activityId()),
                        physicalMembers.getFirst().ordinal(), 1));
            }
            return;
        }

        Set<AssignmentKey> duplicated = assignmentsByKey.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1).map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
        Set<UUID> memberSet = Set.copyOf(members);
        for (Atom atom : atoms) {
            if (atom.assignment() != null && duplicated.contains(new AssignmentKey(
                    turnId, atom.assignment().instructorId(), atom.assignment().activityId()))) {
                continue;
            }
            generated.add(generate(header, atom, memberSet, context, scope));
        }
    }

    private List<Atom> atoms(List<UUID> members, List<IndexedAssignment> assignments) {
        List<Atom> atoms = new ArrayList<>();
        if (members.isEmpty()) {
            if (assignments.isEmpty()) atoms.add(new Atom(null, null));
            else assignments.forEach(assignment -> atoms.add(new Atom(null, assignment.row())));
            return atoms;
        }
        Set<UUID> memberSet = Set.copyOf(members);
        for (UUID member : members) {
            List<IndexedAssignment> own = assignments.stream()
                    .filter(assignment -> member.equals(assignment.row().instructorId())).toList();
            if (own.isEmpty()) atoms.add(new Atom(member, null));
            else own.forEach(assignment -> atoms.add(new Atom(member, assignment.row())));
        }
        assignments.stream().filter(assignment -> !memberSet.contains(assignment.row().instructorId()))
                .forEach(assignment -> atoms.add(new Atom(null, assignment.row())));
        return atoms;
    }

    private Generated generate(
            LegacyTurnMemberRow header,
            Atom atom,
            Set<UUID> members,
            LegacyTurnReadContext context,
            LegacyTurnScope scope) {
        UUID instructor = atom.instructorId();
        UUID activity = atom.activityId();
        boolean hasMembers = !members.isEmpty();
        boolean isMember = instructor != null && members.contains(instructor);
        LinkedHashSet<String> markers = new LinkedHashSet<>();
        String kind;
        String identity;
        List<String> recordIds;
        if (atom.assignment() == null) {
            markers.add("ABSENT_ASSIGNMENT");
            markers.add("ABSENT_ACTIVITY");
            if (!hasMembers) markers.add("ABSENT_MEMBER");
            kind = hasMembers ? "GAP_ABSENT_ASSIGNMENT" : "GAP_ABSENT_MEMBER";
            String member = instructor == null ? "ABSENT" : instructor.toString();
            String marker = hasMembers ? "ABSENT_ASSIGNMENT" : "ABSENT_MEMBER";
            identity = "urn:f2e:legacy-gap:v1:turn=" + header.turnId()
                    + ":member=" + member + ":marker=" + marker;
            recordIds = instructor == null ? List.of(header.turnId().toString())
                    : List.of(header.turnId().toString(), instructor.toString());
        } else {
            kind = "ASSIGNMENT";
            identity = "urn:f2e:legacy-assignment:v1:turn=" + header.turnId()
                    + ":member=" + instructor + ":activity=" + activity;
            recordIds = List.of(header.turnId().toString(), instructor.toString(), activity.toString());
            if (!hasMembers) markers.add("ABSENT_MEMBER");
            if (!isMember) markers.add("NON_MEMBER_ASSIGNMENT");
        }

        Range range = range(header, atom.assignment(), markers);
        String membershipStatus = !hasMembers ? "NO_MEMBERS" : isMember ? "MEMBER" : "NON_MEMBER";
        String scenario = scenario(header.type(), markers);
        String markersString = LegacyTurnReadContext.markers(markers);
        String evidenceKey = identity;

        Map<String, String> observable = new LinkedHashMap<>();
        observable.put("turnId", header.turnId().toString());
        observable.put("type", header.type());
        observable.put("active", "true");
        observable.put("salonId", header.salonId().toString());
        observable.put("dayOfWeek", header.dayOfWeek() == null ? "NULL" : header.dayOfWeek().toString());
        observable.put("date", header.date() == null ? "NULL" : header.date().toString());
        observable.put("turnStart", time(header.turnStart()));
        observable.put("turnEnd", time(header.turnEnd()));
        observable.put("createdAtTechnical", instant(header.createdAtTechnical()));
        observable.put("updatedAtTechnical", instant(header.updatedAtTechnical()));
        observable.put("memberInstructorId", isMember ? instructor.toString() : "NULL");
        observable.put("instructorId", instructor == null ? "ABSENT" : instructor.toString());
        observable.put("activityId", activity == null ? "ABSENT" : activity.toString());
        observable.put("assignmentStartRaw", range.rawStart());
        observable.put("assignmentEndRaw", range.rawEnd());
        observable.put("effectiveStart", range.effectiveStart());
        observable.put("effectiveEnd", range.effectiveEnd());
        observable.put("rangeRule", range.rule());
        observable.put("atomKind", kind);
        observable.put("evidenceKey", evidenceKey);
        observable.put("membershipStatus", membershipStatus);
        observable.put("markers", markersString);
        observable.put("historyStatus", "CURRENT_SNAPSHOT_ONLY");
        observable.put("historyMarker", "LEGACY_FUNCTIONAL_VALIDITY_NOT_PERSISTED");
        observable.put("scenarioPolicy", scenario);
        observable.put("evaluationDate", scope.fecha().toString());
        observable = new LinkedHashMap<>(LegacyTurnReadContext.copyFields(observable, 26));

        DetectorVocabulary.SourceAtomType atomType = switch (header.type()) {
            case "RECURRENTE" -> DetectorVocabulary.SourceAtomType.LEGACY_RECURRENTE;
            case "EXCEPCION" -> DetectorVocabulary.SourceAtomType.LEGACY_EXCEPCION;
            case "CANCELACION" -> DetectorVocabulary.SourceAtomType.LEGACY_CANCELACION;
            default -> throw new IllegalStateException("Unknown legacy turn type");
        };
        String sourceFingerprint = hash(List.of(
                bytes("F2E-SOURCE-V1"), bytes(context.schemaFingerprint()),
                bytes(catalog.projectionContractId()), bytes(catalog.projectionContractVersion()),
                bytes("LEGACY"), bytes(atomType.name()), bytes(identity),
                LegacyTurnReadContext.mapa(observable)));
        String executionId = context.executionProvenanceId();
        String logicalId = context.logicalSnapshotId();
        String snapshotIdentity = LegacyTurnReadContext.hashSecuencia(
                "F2E-ATOM-SNAPSHOT-V1", logicalId, atomType.name(), identity, sourceFingerprint);

        Map<String, String> normalized = new LinkedHashMap<>(observable);
        normalized.put("runIdentity", context.runIdentity());
        normalized.put("attemptIdentity", context.attemptIdentity());
        normalized.put("businessZone", context.businessZone().getId());
        normalized.put("scopeCanonical", context.scopeCanonical());
        normalized.put("operation", "READ_FOR_DATE");
        normalized.put("projectionCatalogVersion", context.projectionCatalogVersion().name());
        normalized.put("projectionContractId", catalog.projectionContractId());
        normalized.put("projectionContractVersion", catalog.projectionContractVersion());
        normalized.put("ruleCatalogVersion", context.ruleCatalogVersion());
        normalized.put("snapshotClaim", context.snapshotClaim().name());
        normalized.put("snapshotEvidenceId", context.snapshotEvidenceId());
        normalized.put("executionProvenanceId", executionId);
        normalized.put("logicalSnapshotId", logicalId);
        normalized.put("sourceSystem", "LEGACY");
        normalized.put("sourceAtomType", atomType.name());
        normalized.put("sourceIdentity", identity);
        normalized.put("sourceFingerprint", sourceFingerprint);
        normalized.put("snapshotIdentity", snapshotIdentity);
        normalized.put("transactionIsolation", "repeatable read");
        normalized.put("transactionAccessMode", "read only");
        normalized = new LinkedHashMap<>(LegacyTurnReadContext.copyFields(normalized, 46));

        String businessContext = LegacyTurnReadContext.utf8Estricto(LegacyTurnReadContext.secuencia(List.of(
                bytes("F2E-R2-BUSINESS-CONTEXT-V1"), bytes(context.businessZone().getId()),
                scope.bytesCanonicos())));
        EvidenceProvenance provenance = new EvidenceProvenance(
                context.sourceName(), context.schemaFingerprint(), recordIds,
                catalog.projectionContractId(), catalog.projectionContractVersion(),
                businessContext, normalized);
        GenericSourceSnapshot source = new GenericSourceSnapshot(
                DetectorVocabulary.SourceSystem.LEGACY, atomType, identity, snapshotIdentity,
                sourceFingerprint, observable, provenance);
        return new Generated(header.turnId(), instructor, activity, evidenceKey, source);
    }

    private Range range(
            LegacyTurnMemberRow header, LegacyAssignmentRow assignment, Set<String> markers) {
        if (assignment == null) return new Range("ABSENT", "ABSENT", "ABSENT", "ABSENT", "NOT_APPLICABLE");
        LocalTime start = assignment.assignmentStartRaw();
        LocalTime end = assignment.assignmentEndRaw();
        String rawStart = start == null ? "NULL" : time(start);
        String rawEnd = end == null ? "NULL" : time(end);
        if (start == null && end == null) {
            markers.add("FULL_TURN_RANGE_FALLBACK");
            return new Range(rawStart, rawEnd, time(header.turnStart()), time(header.turnEnd()),
                    "LEGACY_FULL_TURN_FALLBACK");
        }
        if (start == null || end == null) {
            markers.add("INCOMPLETE_RANGE");
            return new Range(rawStart, rawEnd, "ABSENT", "ABSENT", "LEGACY_INCOMPLETE_RANGE");
        }
        if (!end.isAfter(start) || start.isBefore(header.turnStart()) || end.isAfter(header.turnEnd())) {
            markers.add("RANGE_OUTSIDE_TURN");
            return new Range(rawStart, rawEnd, rawStart, rawEnd, "LEGACY_RANGE_OUTSIDE_TURN");
        }
        markers.add("EXPLICIT_ASSIGNMENT_RANGE");
        return new Range(rawStart, rawEnd, rawStart, rawEnd, "LEGACY_EXPLICIT_ASSIGNMENT_RANGE");
    }

    private String scenario(String type, Set<String> markers) {
        return switch (type) {
            case "EXCEPCION" -> "LEGACY_EXCEPTION_UNKNOWN_INTENT";
            case "CANCELACION" -> "LEGACY_CANCELLATION_UNKNOWN_INTENT";
            case "RECURRENTE" -> markers.stream().anyMatch(ANOMALY_MARKERS::contains)
                    ? "INCOMPATIBLE_EVIDENCE" : "RECURRENT_CLAIM_DEPENDENT";
            default -> throw new IllegalStateException("Unknown legacy turn type");
        };
    }

    private boolean validHeader(LegacyTurnMemberRow row, LegacyTurnScope scope) {
        if (row == null || row.turnId() == null || !TYPES.contains(row.type())
                || !Boolean.TRUE.equals(row.active()) || row.salonId() == null
                || !scope.salonIds().contains(row.salonId()) || row.turnStart() == null || row.turnEnd() == null
                || !row.turnEnd().isAfter(row.turnStart()) || row.createdAtTechnical() == null
                || row.updatedAtTechnical() == null || !microAligned(row.createdAtTechnical())
                || !microAligned(row.updatedAtTechnical())) return false;
        return switch (row.type()) {
            case "RECURRENTE" -> row.dayOfWeek() != null && row.dayOfWeek() >= 0 && row.dayOfWeek() <= 6
                    && row.dayOfWeek() == scope.dayOfWeekDomingoCero() && row.date() == null;
            case "EXCEPCION", "CANCELACION" -> row.dayOfWeek() == null && scope.fecha().equals(row.date());
            default -> false;
        };
    }

    private boolean sameHeader(LegacyTurnMemberRow left, LegacyTurnMemberRow right) {
        return Objects.equals(left.turnId(), right.turnId()) && Objects.equals(left.type(), right.type())
                && Objects.equals(left.active(), right.active()) && Objects.equals(left.salonId(), right.salonId())
                && Objects.equals(left.dayOfWeek(), right.dayOfWeek()) && Objects.equals(left.date(), right.date())
                && Objects.equals(left.turnStart(), right.turnStart()) && Objects.equals(left.turnEnd(), right.turnEnd())
                && Objects.equals(left.createdAtTechnical(), right.createdAtTechnical())
                && Objects.equals(left.updatedAtTechnical(), right.updatedAtTechnical());
    }

    private String time(LocalTime value) {
        if (value.getNano() % 1_000 != 0) throw new IllegalArgumentException("Sub-microsecond R2 time");
        return TIME_MICROS.format(value);
    }

    private String instant(OffsetDateTime value) {
        if (!microAligned(value)) throw new IllegalArgumentException("Sub-microsecond R2 timestamp");
        return INSTANT_MICROS.format(value.withOffsetSameInstant(ZoneOffset.UTC));
    }

    private boolean microAligned(OffsetDateTime value) {
        return value.getNano() % 1_000 == 0;
    }

    private String hash(List<byte[]> parts) {
        return LegacyTurnReadContext.sha256(LegacyTurnReadContext.secuencia(parts));
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private LegacyAdapterInputInvalid invalidCaller() {
        return new LegacyAdapterInputInvalid(List.of(new LegacyAdapterRejection(
                LegacyAdapterRejection.Code.ADAPTER_INPUT_INVALID, MEMBERS, Map.of(),
                "INVALID_REQUIRED_FIELD", List.of(), 1, 1)));
    }

    private LegacyAdapterRejection rejectionInput(
            String query, LegacyTurnScope scope, String marker, List<String> ids, int ordinal, int count) {
        return new LegacyAdapterRejection(LegacyAdapterRejection.Code.ADAPTER_INPUT_INVALID,
                query, safeScope(scope), marker, ids, ordinal, count);
    }

    private LegacyAdapterRejection rejectionInvariant(
            String query, LegacyTurnScope scope, String marker, List<String> ids, int ordinal, int count) {
        return new LegacyAdapterRejection(LegacyAdapterRejection.Code.READ_SET_INVARIANT_VIOLATION,
                query, safeScope(scope), marker, ids, ordinal, count);
    }

    private Map<String, String> safeScope(LegacyTurnScope scope) {
        return Map.of("fecha", scope.fecha().toString(), "salonCount", Integer.toString(scope.salonIds().size()));
    }

    private List<String> safeIds(UUID... ids) {
        return Arrays.stream(ids).filter(Objects::nonNull).map(UUID::toString).toList();
    }

    private record IndexedMember(int ordinal, LegacyTurnMemberRow row) { }
    private record IndexedAssignment(int ordinal, LegacyAssignmentRow row) { }
    private record AssignmentKey(UUID turnId, UUID instructorId, UUID activityId) { }
    private record Atom(UUID memberId, LegacyAssignmentRow assignment) {
        UUID instructorId() { return assignment == null ? memberId : assignment.instructorId(); }
        UUID activityId() { return assignment == null ? null : assignment.activityId(); }
    }
    private record Range(String rawStart, String rawEnd, String effectiveStart, String effectiveEnd, String rule) { }
    private record Generated(UUID turnId, UUID instructorId, UUID activityId, String evidenceKey,
                             GenericSourceSnapshot source) {
        private static final Comparator<Generated> ORDER = Comparator
                .comparing(Generated::turnId, LegacyTurnReadContext.UUID_UNSIGNED)
                .thenComparing(Generated::instructorId, Comparator.nullsFirst(LegacyTurnReadContext.UUID_UNSIGNED))
                .thenComparing(Generated::activityId, Comparator.nullsFirst(LegacyTurnReadContext.UUID_UNSIGNED))
                .thenComparing(Generated::evidenceKey, LegacyTurnReadContext.UTF8_UNSIGNED);
    }
}
