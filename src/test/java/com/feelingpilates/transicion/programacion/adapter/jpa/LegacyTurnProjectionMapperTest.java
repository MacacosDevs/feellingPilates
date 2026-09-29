package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyAssignmentRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnMemberRow;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog;
import com.feelingpilates.transicion.programacion.read.LegacyAdapterInputInvalid;
import com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext;
import com.feelingpilates.transicion.programacion.read.LegacyTurnScope;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTurnProjectionMapperTest {

    private static final UUID SALON = uuid(10);
    private static final UUID TURN = uuid(20);
    private static final UUID MEMBER_A = uuid(30);
    private static final UUID MEMBER_B = uuid(31);
    private static final UUID NON_MEMBER = uuid(32);
    private static final UUID ACTIVITY_A = uuid(40);
    private static final UUID ACTIVITY_B = uuid(41);
    private static final LocalDate DATE = LocalDate.of(2026, 9, 28);

    private final LegacyTurnProjectionMapper mapper = new LegacyTurnProjectionMapper(
            LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1);
    private final LegacyTurnScope scope = new LegacyTurnScope(Set.of(SALON), DATE);

    @Test
    void zeroPhysicalRowsIsTheOnlyEmptySuccess() {
        assertTrue(mapper.mapear(List.of(), List.of(), context(), scope).sources().isEmpty());
    }

    @Test
    void zeroMembersZeroAssignmentsProducesOneAbsentMemberGap() {
        var result = mapper.mapear(List.of(member(null)), List.of(), context(), scope);
        assertEquals(1, result.sources().size());
        assertTrue(result.sources().getFirst().sourceIdentity().contains("member=ABSENT:marker=ABSENT_MEMBER"));
        assertEquals("GAP_ABSENT_MEMBER", result.sources().getFirst().observableFields().get("atomKind"));
        assertEquals(26, result.sources().getFirst().observableFields().size());
        assertEquals(46, result.sources().getFirst().provenance().normalizedFields().size());
    }

    @Test
    void zeroMembersNAssignmentsProducesNNonMemberAtoms() {
        var result = mapper.mapear(List.of(member(null)), List.of(
                assignment(NON_MEMBER, ACTIVITY_B, null, null),
                assignment(NON_MEMBER, ACTIVITY_A, LocalTime.of(9, 0), LocalTime.of(10, 0))),
                context(), scope);
        assertEquals(2, result.sources().size());
        assertTrue(result.sources().stream().allMatch(source ->
                "NO_MEMBERS".equals(source.observableFields().get("membershipStatus"))));
        assertEquals(ACTIVITY_A.toString(), result.sources().getFirst().observableFields().get("activityId"));
    }

    @Test
    void oneMemberZeroAssignmentsProducesOneGapAndOneMemberNProducesNAtoms() {
        var gap = mapper.mapear(List.of(member(MEMBER_A)), List.of(), context(), scope);
        assertEquals(1, gap.sources().size());
        assertEquals(List.of(TURN.toString(), MEMBER_A.toString()), gap.sources().getFirst().provenance().recordIds());

        var many = mapper.mapear(List.of(member(MEMBER_A)), List.of(
                assignment(MEMBER_A, ACTIVITY_A, null, null),
                assignment(MEMBER_A, ACTIVITY_B, LocalTime.of(8, 30), LocalTime.of(9, 30))),
                context(), scope);
        assertEquals(2, many.sources().size());
        assertEquals("LEGACY_FULL_TURN_FALLBACK", many.sources().getFirst().observableFields().get("rangeRule"));
        assertEquals("LEGACY_EXPLICIT_ASSIGNMENT_RANGE", many.sources().get(1).observableFields().get("rangeRule"));
    }

    @Test
    void cardinalityIsSumMaxOnePlusNonMembers() {
        var result = mapper.mapear(List.of(member(MEMBER_B), member(MEMBER_A)), List.of(
                assignment(MEMBER_A, ACTIVITY_A, null, null),
                assignment(MEMBER_A, ACTIVITY_B, null, null),
                assignment(NON_MEMBER, ACTIVITY_A, null, null)), context(), scope);
        assertEquals(4, result.sources().size());
        assertEquals("GAP_ABSENT_ASSIGNMENT", result.sources().stream()
                .filter(source -> MEMBER_B.toString().equals(source.observableFields().get("instructorId")))
                .findFirst().orElseThrow().observableFields().get("atomKind"));
        assertEquals("NON_MEMBER", result.sources().stream()
                .filter(source -> NON_MEMBER.toString().equals(source.observableFields().get("instructorId")))
                .findFirst().orElseThrow().observableFields().get("membershipStatus"));
    }

    @Test
    void invalidHeaderRejectsEveryAttemptedAtomWithoutPartialReadSet() {
        LegacyTurnMemberRow invalid = new LegacyTurnMemberRow(
                TURN, "RECURRENTE", true, SALON, (short) 1, null,
                LocalTime.of(8, 0), LocalTime.of(12, 0), null,
                OffsetDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC), MEMBER_A);
        LegacyAdapterInputInvalid failure = assertThrows(LegacyAdapterInputInvalid.class, () ->
                mapper.mapear(List.of(invalid), List.of(
                        assignment(MEMBER_A, ACTIVITY_A, null, null),
                        assignment(MEMBER_A, ACTIVITY_B, null, null),
                        assignment(NON_MEMBER, ACTIVITY_A, null, null)), context(), scope));
        assertEquals(3, failure.rejections().size());
        assertTrue(failure.rejections().stream().allMatch(rejection ->
                "INVALID_REQUIRED_FIELD".equals(rejection.marker())));
    }

    @Test
    void punctualUnknownIntentTakesPrecedenceOverStructuralAnomaly() {
        LegacyTurnMemberRow exception = member(null, "EXCEPCION");
        var result = mapper.mapear(List.of(exception), List.of(), context(), scope);
        assertEquals("LEGACY_EXCEPTION_UNKNOWN_INTENT",
                result.sources().getFirst().observableFields().get("scenarioPolicy"));
    }

    @Test
    void duplicatePhysicalAssignmentRejectsOneLogicalUnitWithMultiplicity() {
        LegacyAssignmentRow row = assignment(MEMBER_A, ACTIVITY_A, null, null);
        LegacyAdapterInputInvalid failure = assertThrows(LegacyAdapterInputInvalid.class, () ->
                mapper.mapear(List.of(member(MEMBER_A)), List.of(row, row), context(), scope));
        assertEquals(1, failure.rejections().size());
        assertEquals("DUPLICATE_PHYSICAL_ROW", failure.rejections().getFirst().marker());
        assertEquals(2, failure.rejections().getFirst().observedPhysicalRowCount());
    }

    @Test
    void readSetAndInputsAreDefensivelyCopiedAndFingerprintIsStable() {
        java.util.ArrayList<LegacyAssignmentRow> assignments = new java.util.ArrayList<>();
        assignments.add(assignment(MEMBER_A, ACTIVITY_A, null, null));
        var first = mapper.mapear(List.of(member(MEMBER_A)), assignments, context(), scope);
        assignments.clear();
        var second = mapper.mapear(List.of(member(MEMBER_A)),
                List.of(assignment(MEMBER_A, ACTIVITY_A, null, null)), context(), scope);
        assertEquals(first, second);
        assertEquals(mapper.logicalReadSetFingerprint(first), mapper.logicalReadSetFingerprint(second));
        assertThrows(UnsupportedOperationException.class, () -> first.sources().clear());
    }

    private LegacyTurnReadContext context() {
        return new LegacyTurnReadContext("run-r2", "attempt-r2", "fixture:postgres16:r2:test",
                "sha256:" + "a".repeat(64), LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,
                "rules-v1", ZoneId.of("America/Mexico_City"), scope.canonical(),
                LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST, "b".repeat(64));
    }

    private LegacyTurnMemberRow member(UUID member) {
        return member(member, "RECURRENTE");
    }

    private LegacyTurnMemberRow member(UUID member, String type) {
        return new LegacyTurnMemberRow(TURN, type, true, SALON,
                "RECURRENTE".equals(type) ? (short) 1 : null,
                "RECURRENTE".equals(type) ? null : DATE,
                LocalTime.of(8, 0), LocalTime.of(12, 0),
                OffsetDateTime.of(2026, 9, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC), member);
    }

    private LegacyAssignmentRow assignment(UUID instructor, UUID activity, LocalTime start, LocalTime end) {
        return new LegacyAssignmentRow(TURN, instructor, activity, start, end);
    }

    private static UUID uuid(long value) {
        return new UUID(0, value);
    }
}
