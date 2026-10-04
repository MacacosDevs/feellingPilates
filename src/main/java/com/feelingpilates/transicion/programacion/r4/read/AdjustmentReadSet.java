package com.feelingpilates.transicion.programacion.r4.read;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import java.util.*;
public record AdjustmentReadSet(List<GenericSourceSnapshot> sources, List<AdjustmentBackingSnapshot> backing) {
    public AdjustmentReadSet {
        try {
            sources=List.copyOf(sources); backing=List.copyOf(backing);
            if(sources.size()!=backing.size()) throw new IllegalArgumentException("cardinality");
            Set<UUID> seen=new HashSet<>(); AdjustmentBackingSnapshot previous=null;
            for(int i=0;i<sources.size();i++) {
                var s=sources.get(i); var b=backing.get(i);
                if(b.id()==null || b.fecha()==null || !seen.add(b.id()) || !Boolean.TRUE.equals(b.activo())
                        || !s.sourceIdentity().equals(b.id().toString())
                        || s.sourceSystem()!=DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH
                        || !s.sourceAtomType().name().equals("NEW_"+b.tipo())
                        || !s.observableFields().equals(b.normalizedFields())
                        || !s.provenance().normalizedFields().equals(b.normalizedFields())
                        || !s.provenance().recordIds().equals(List.of(b.id().toString()))
                        || !s.provenance().ruleId().equals("R4_ADJUSTMENT_PROJECTION")
                        || !s.provenance().ruleVersion().equals("V1")
                        || !s.provenance().businessTimeContext().endsWith("/"+b.fecha()))
                    throw new IllegalArgumentException("backing mismatch");
                String fingerprint=AdjustmentReadSnapshotContext.hash("F2E-R4-SOURCE-V1",s.sourceSystem().name(),
                        s.sourceAtomType().name(),s.sourceIdentity(),ReadSnapshotIdentifiers.decodificarUtf8(
                                ReadSnapshotIdentifiers.mapaCanonico(b.normalizedFields())));
                if(!s.sourceFingerprint().equals(fingerprint)) throw new IllegalArgumentException("source hash");
                if(previous!=null && (!previous.fecha().equals(b.fecha()) || compareUuid(previous.id(),b.id())>=0))
                    throw new IllegalArgumentException("order/date");
                previous=b;
            }
        } catch(RuntimeException e) {
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                    null,List.of(),e);
        }
    }
    public static int compareUuid(UUID a, UUID b) {
        int c=Long.compareUnsigned(a.getMostSignificantBits(),b.getMostSignificantBits());
        return c!=0?c:Long.compareUnsigned(a.getLeastSignificantBits(),b.getLeastSignificantBits());
    }
}
