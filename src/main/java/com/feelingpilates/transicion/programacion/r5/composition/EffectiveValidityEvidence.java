package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

/** Complete typed scalar relations. Missing entries never mean absent or empty. */
public record EffectiveValidityEvidence(Map<UUID, Presence<Salon>> salons,
        Map<UUID, Presence<Instructor>> instructors, Map<UUID, Presence<Activity>> activities,
        Map<UUID, Hours> hours, Map<UUID, Relation<RoleEdge>> roles,
        Map<UUID, Relation<SpecializationEdge>> specializations, Map<UUID, Relation<OfferingEdge>> offerings) {
    public EffectiveValidityEvidence {
        salons=Map.copyOf(salons); instructors=Map.copyOf(instructors); activities=Map.copyOf(activities);
        hours=Map.copyOf(hours); roles=Map.copyOf(roles); specializations=Map.copyOf(specializations);
        offerings=Map.copyOf(offerings);
    }
    public record RelationMetadata(String sourceName, String schemaFingerprint, String ruleVersion,
            String participantName, String participantExecutionIdentity, String scope, boolean complete, List<String> recordKeys) {
        public RelationMetadata { recordKeys=List.copyOf(recordKeys); }
    }
    public enum PresenceKind { PRESENT, ABSENT }
    public record Presence<T>(PresenceKind kind, UUID queriedId, T value, RelationMetadata metadata) { }
    public record Relation<T>(RelationMetadata metadata, List<T> rows) {
        public Relation { rows=List.copyOf(rows); }
    }
    public record Hours(Relation<DateException> exceptions, Relation<WeeklyHours> weekly) { }
    public record Salon(UUID id, Boolean activo) { }
    public record Instructor(UUID id, String estatus) { }
    public record Activity(UUID id, Boolean activo) { }
    public record DateException(UUID id, UUID salonId, LocalDate date, Boolean cerrado,
            LocalTime opening, LocalTime closing, Boolean activo) { }
    public record WeeklyHours(UUID id, UUID salonId, Short day, LocalTime opening, LocalTime closing,
            LocalDate validFrom, LocalDate validUntil) { }
    public record RoleEdge(UUID usuarioId, UUID rolId, String roleName, UUID nullableSalonId) { }
    public record SpecializationEdge(UUID usuarioId, UUID activityId) { }
    public record OfferingEdge(UUID salonId, UUID activityId) { }
}
