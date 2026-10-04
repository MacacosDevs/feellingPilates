package com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import java.time.*;
import java.util.*;
public final class AdjustmentProjectionMapper {
    public AdjustmentReadSet map(List<AdjustmentProjectionRow> rows, AdjustmentReadSnapshotContext context, LocalDate fecha) {
        if(context==null || fecha==null || !fecha.equals(context.fecha()))
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.INVALID_INPUT,fecha);
        if(rows==null) throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,fecha);
        for(var r:rows) validate(r,fecha);
        Set<UUID> ids=new HashSet<>(); Map<UUID,UUID> targets=new HashMap<>(); AdjustmentProjectionRow previous=null;
        for(var r:rows) {
            if(!ids.add(r.id())) throw failure(AdjustmentReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,fecha,r.id());
            if(r.asignacionSerieId()!=null) {
                UUID prior=targets.putIfAbsent(r.asignacionSerieId(),r.id());
                if(prior!=null) throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.DUPLICATE_ACTIVE_TARGET_ON_DATE,
                        fecha,List.of(prior,r.id()),null);
            }
            if(previous!=null && AdjustmentReadSet.compareUuid(previous.id(),r.id())>=0)
                throw failure(AdjustmentReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,fecha,r.id());
            previous=r;
        }
        List<GenericSourceSnapshot> sources=new ArrayList<>(); List<AdjustmentBackingSnapshot> backing=new ArrayList<>();
        for(var r:rows) {
            var b=new AdjustmentBackingSnapshot(r.id(),r.tipo(),r.fecha(),r.asignacionSerieId(),r.salonResultadoId(),
                    r.instructorResultadoId(),r.tipoActividadResultadoId(),r.horaInicioResultado(),r.horaFinResultado(),
                    r.activo(),r.creadoEn(),r.actualizadoEn());
            var fields=b.normalizedFields(); var atom=DetectorVocabulary.SourceAtomType.valueOf("NEW_"+r.tipo());
            String fingerprint=AdjustmentReadSnapshotContext.hash("F2E-R4-SOURCE-V1","NEW_DARK_LAUNCH",atom.name(),
                    r.id().toString(),ReadSnapshotIdentifiers.decodificarUtf8(ReadSnapshotIdentifiers.mapaCanonico(fields)));
            sources.add(new GenericSourceSnapshot(DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH,atom,r.id().toString(),
                    AdjustmentReadSnapshotContext.hash("F2E-R4-SNAPSHOT-V1",context.executionProvenanceId(),fingerprint),
                    fingerprint,fields,new EvidenceProvenance(context.sourceName(),context.schemaFingerprint(),
                            List.of(r.id().toString()),"R4_ADJUSTMENT_PROJECTION","V1",context.businessZone().getId()+"/"+fecha,fields)));
            backing.add(b);
        }
        return new AdjustmentReadSet(sources,backing);
    }
    public static void validate(AdjustmentProjectionRow r, LocalDate fecha) {
        try {
            Objects.requireNonNull(r); Objects.requireNonNull(r.id()); Objects.requireNonNull(r.tipo());
            if(!fecha.equals(r.fecha()) || !Boolean.TRUE.equals(r.activo())) throw new IllegalArgumentException("predicate");
            if(!Set.of("CANCELACION","REEMPLAZO","ADICION").contains(r.tipo())) throw new IllegalArgumentException("type");
            boolean cancel=r.tipo().equals("CANCELACION"); boolean add=r.tipo().equals("ADICION");
            if((r.asignacionSerieId()==null)!=add) throw new IllegalArgumentException("target shape");
            Object[] result={r.salonResultadoId(),r.instructorResultadoId(),r.tipoActividadResultadoId(),
                    r.horaInicioResultado(),r.horaFinResultado()};
            for(Object v:result) if((v==null)!=cancel) throw new IllegalArgumentException("result shape");
            if(!cancel) {
                if(!r.horaFinResultado().isAfter(r.horaInicioResultado())) throw new IllegalArgumentException("range");
                ReadSnapshotIdentifiers.hora(r.horaInicioResultado()); ReadSnapshotIdentifiers.hora(r.horaFinResultado());
            }
            for(var t:Arrays.asList(r.creadoEn(),r.actualizadoEn())) {
                Objects.requireNonNull(t);
                if(t.equals(OffsetDateTime.MIN)||t.equals(OffsetDateTime.MAX)||t.getYear() < -4712 || t.getYear()>294276)
                    throw new IllegalArgumentException("unrepresentable timestamp");
                ReadSnapshotIdentifiers.instante(t);
            }
        } catch(RuntimeException e) {
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,fecha,
                    r==null || r.id()==null?List.of():List.of(r.id()),e);
        }
    }
    private AdjustmentReadFailure failure(AdjustmentReadFailure.Category category, LocalDate fecha,UUID id) {
        return new AdjustmentReadFailure(category,fecha,List.of(id),null);
    }
}
