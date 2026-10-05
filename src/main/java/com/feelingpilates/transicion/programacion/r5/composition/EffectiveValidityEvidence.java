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
        salons=ordered(salons); instructors=ordered(instructors); activities=ordered(activities);
        hours=ordered(hours); roles=ordered(roles); specializations=ordered(specializations);
        offerings=ordered(offerings);
    }
    private static <T> Map<UUID,T> ordered(Map<UUID,T> supplied) {
        var sorted=new TreeMap<UUID,T>(EffectiveCompositionCanonicalizer.UUID_ORDER);sorted.putAll(Map.copyOf(supplied));
        return Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }
    public record RelationMetadata(String sourceName, String schemaFingerprint, String ruleVersion,
            String participantName, String participantExecutionIdentity, String scope, boolean complete, List<String> recordKeys) {
        public RelationMetadata { recordKeys=List.copyOf(recordKeys).stream().sorted((a,b)->compareRecordKey(scope,a,b)).toList(); }
    }
    static int compareRecordKey(String scope,String left,String right) {
        if(scope!=null&&scope.startsWith("ROLES/")) {
            String[] a=left.split("/",-1),b=right.split("/",-1);
            if(a.length==3&&b.length==3) {
                try {
                    int c=EffectiveCompositionCanonicalizer.UUID_ORDER.compare(UUID.fromString(a[0]),UUID.fromString(b[0]));
                    if(c!=0)return c;
                    c=EffectiveCompositionCanonicalizer.UUID_ORDER.compare(UUID.fromString(a[1]),UUID.fromString(b[1]));
                    if(c!=0)return c;
                    if(a[2].equals("ABSENT")||b[2].equals("ABSENT"))return a[2].equals(b[2])?0:a[2].equals("ABSENT")?-1:1;
                    return EffectiveCompositionCanonicalizer.UUID_ORDER.compare(UUID.fromString(a[2]),UUID.fromString(b[2]));
                } catch(IllegalArgumentException e) { /* Invalid keys remain intact for complete admission rejection. */ }
            }
        }
        return Arrays.compareUnsigned(EffectiveCompositionCanonicalizer.text(left),EffectiveCompositionCanonicalizer.text(right));
    }
    public enum PresenceKind { PRESENT, ABSENT }
    public record Presence<T>(PresenceKind kind, UUID queriedId, T value, RelationMetadata metadata) { }
    public record Relation<T>(RelationMetadata metadata, List<T> rows) {
        public Relation {
            var sorted=new ArrayList<T>(List.copyOf(rows));
            sorted.sort((a,b)->Arrays.compareUnsigned(EffectiveCompositionCanonicalizer.supportRowKey(a),EffectiveCompositionCanonicalizer.supportRowKey(b)));
            rows=List.copyOf(sorted);
        }
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
