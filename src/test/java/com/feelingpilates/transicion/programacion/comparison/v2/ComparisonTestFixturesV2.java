package com.feelingpilates.transicion.programacion.comparison.v2;
import com.feelingpilates.transicion.programacion.r5.composition.*;

import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.lang.reflect.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.detector.EvidenceProvenance;
import com.feelingpilates.transicion.programacion.detector.DetectorVocabulary;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveValidityEvidence.*;

/** Independent literal scalar/formula fixtures and algebra oracle; never acquires facts. */
final class ComparisonTestFixturesV2 {
    static final LocalDate DATE=LocalDate.of(2026,10,4);
    static final String ZONE="America/Mexico_City", RULE="F2E-R5-PURE-COMPOSITION-V1", HASH="a".repeat(64);
    static UUID id(int n) {return new UUID(0,n);}
    static LocalTime time(int hour) {return LocalTime.of(hour,0);}
    static String time6(LocalTime t) {return String.format(Locale.ROOT,"%02d:%02d:%02d.%06d",t.getHour(),t.getMinute(),t.getSecond(),t.getNano()/1000);}
    static String instant6(OffsetDateTime t) {var u=t.withOffsetSameInstant(ZoneOffset.UTC);return u.toLocalDate()+"T"+time6(u.toLocalTime())+"Z";}
    static byte[] frame(byte[]... parts) {
        var out=new java.io.ByteArrayOutputStream();out.writeBytes((parts.length+":").getBytes(StandardCharsets.US_ASCII));
        for(var p:parts) {out.writeBytes((p.length+":").getBytes(StandardCharsets.US_ASCII));out.writeBytes(p);}return out.toByteArray();
    }
    static byte[] bytes(String s) {return s.getBytes(StandardCharsets.UTF_8);}
    static String sha(byte[] b) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));}
        catch(Exception e) {throw new AssertionError(e);}
    }
    static String closed(String... values) {return sha(frame(Arrays.stream(values).map(ComparisonTestFixturesV2::bytes).toArray(byte[][]::new)));}
    static String closedMap(Map<String,String> fields) {
        var parts=new ArrayList<byte[]>();parts.add(bytes("F2E-R1-NORMALIZED-FIELDS-V2"));parts.add(bytes(Integer.toString(fields.size())));new TreeMap<>(fields).forEach((k,v)->parts.add(frame(bytes("F2E-R1-NORMALIZED-FIELD-V2"),bytes(k),bytes(v))));
        return new String(frame(parts.toArray(byte[][]::new)),StandardCharsets.UTF_8);
    }
    @SuppressWarnings("unchecked") static <T> T copy(T record,String field,Object value) {
        try {
            var fields=record.getClass().getRecordComponents();var values=new Object[fields.length];var types=new Class<?>[fields.length];
            boolean found=false;
            for(int k=0;k<fields.length;k++) {types[k]=fields[k].getType();values[k]=fields[k].getAccessor().invoke(record);
                if(fields[k].getName().equals(field)){values[k]=value;found=true;}}
            if(!found)throw new AssertionError(field);
            return (T)record.getClass().getDeclaredConstructor(types).newInstance(values);
        } catch(InvocationTargetException e) {if(e.getCause() instanceof RuntimeException r)throw r;throw new AssertionError(e.getCause());}
        catch(ReflectiveOperationException e) {throw new AssertionError(e);}
    }
    static EffectiveCompositionEnvelope envelope() {
        var map=new LinkedHashMap<String,EffectiveCompositionEnvelope.Participant>();
        for(String key:List.of("R3","R4","VALIDITY")) {
            String scope=key.equals("R3")?"R3_NOMINAL_ON_DATE_V1/"+DATE+"/0":key.equals("R4")?"R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/"+DATE:"R5_VALIDITY_ON_DATE_V1/"+DATE;
            var p=new EffectiveCompositionEnvelope.Participant("SYNTHETIC_"+key,"sha256:"+HASH,"V1",scope,"PENDING",HASH,
                    "NOT_APPLICABLE_SYNTHETIC","NOT_APPLICABLE_SYNTHETIC","NOT_APPLICABLE_SYNTHETIC",EffectiveCompositionEnvelope.Completion.SYNTHETIC,
                    "SYNTHETIC_RUN","SYNTHETIC_ATTEMPT","SYNTHETIC_CATALOG","SYNTHETIC_DB","SYNTHETIC_SCHEMA","SYNTHETIC_PRINCIPAL",
                    key.equals("R3")?"R3_NOMINAL_V1":key.equals("R4")?"R4_ADJUSTMENT_V1":"SYNTHETIC_VALIDITY_V1",
                    "SYNTHETIC_INVOCATION",key+"_INTERNAL_RR_TEST",HASH,HASH);
            p=copy(p,"executionIdentity",execution(p,key));map.put(key,p);
        }
        return new EffectiveCompositionEnvelope(EffectiveCompositionEnvelope.Mode.SYNTHETIC_DESIGN_FIXTURE,DATE,ZONE,
                "SYNTHETIC_COMPOSITION","SYNTHETIC_AUTHORITY_V1","NOT_APPLICABLE_SYNTHETIC",map);
    }
    static String execution(EffectiveCompositionEnvelope.Participant p,String key) {
        if(key.equals("R3"))return closed("F2E-R3-EXECUTION-V1",p.runIdentity(),p.attemptIdentity(),p.sourceName(),p.schemaFingerprint(),p.ruleCatalogVersion(),ZONE,
                p.readScope(),p.readerInvocationIdentity(),p.databaseName(),p.schemaName(),p.principal(),p.physicalResourceIdentity(),p.projectionCatalogVersion(),p.snapshotClaim(),p.snapshotEvidenceId(),p.statementCaptureCommitment());
        if(key.equals("R4"))return closed("F2E-R4-EXECUTION-V1",p.runIdentity(),p.attemptIdentity(),p.readScope(),ZONE,p.ruleCatalogVersion(),p.sourceName(),p.schemaFingerprint(),
                p.databaseName(),p.schemaName(),p.principal(),p.physicalResourceIdentity(),p.projectionCatalogVersion(),p.readerInvocationIdentity(),p.snapshotClaim(),p.snapshotEvidenceId(),p.statementCaptureCommitment());
        return closed("SYNTHETIC_VALIDITY",p.sourceName(),p.schemaFingerprint(),p.readScope());
    }
    static NominalBackingSnapshot nominal(int index) {
        var r=new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,id(100+index),DATE);var stamp=OffsetDateTime.parse("2026-10-01T00:00:00Z");
        return new NominalBackingSnapshot(r,DATE,(short)0,r.id(),id(200+index),id(300+index),id(300+index),id(500+index),id(1),id(20+index),id(3),
                time(9+index),time(10+index),time(8),time(18),DATE.minusDays(1),null,true,DATE.minusDays(1),null,true,stamp,stamp,stamp,stamp,(short)0);
    }
    static Map<String,String> nominalFields(NominalBackingSnapshot b,EffectiveCompositionEnvelope e) {
        var m=new LinkedHashMap<String,String>();
        String[] names={"assignmentSeriesId","assignmentId","assignmentBlockId","blockId","blockSeriesId","salonId","instructorId","activityId",
                "assignmentStart","assignmentEnd","blockStart","blockEnd","assignmentFrom","assignmentUntil","assignmentActive","blockFrom","blockUntil","blockActive",
                "assignmentCreated","assignmentUpdated","blockCreated","blockUpdated","blockDay","fecha","dayOfWeek"};
        Object[] values={b.assignmentSeriesId(),b.assignmentId(),b.assignmentBlockId(),b.blockId(),b.blockSeriesId(),b.salonId(),b.instructorId(),b.activityId(),
                b.assignmentStart(),b.assignmentEnd(),b.blockStart(),b.blockEnd(),b.assignmentFrom(),b.assignmentUntil(),b.assignmentActive(),b.blockFrom(),b.blockUntil(),b.blockActive(),
                b.assignmentCreated(),b.assignmentUpdated(),b.blockCreated(),b.blockUpdated(),b.blockDay(),b.fecha(),b.dayOfWeek()};
        for(int k=0;k<names.length;k++){Object v=values[k];m.put(names[k],v==null?"NULL":v instanceof LocalTime t?time6(t):v instanceof OffsetDateTime t?instant6(t):v.toString());}
        var p=e.participants().get("R3");m.put("snapshotEvidenceId",p.snapshotEvidenceId());m.put("executionProvenanceId",p.executionIdentity());m.put("statementCaptureCommitment",p.statementCaptureCommitment());return m;
    }
    static Map<String,String> obs(UUID salon,UUID instructor,UUID activity,LocalTime start,LocalTime end) {
        return Map.of("salonId",salon.toString(),"instructorId",instructor.toString(),"activityId",activity.toString(),"start",time6(start),"end",time6(end),"fecha",DATE.toString());
    }
    static ProgrammingCandidateSnapshot candidate(NominalBackingSnapshot b,EffectiveCompositionEnvelope e) {
        var fields=nominalFields(b,e);var o=obs(b.salonId(),b.instructorId(),b.activityId(),b.assignmentStart(),b.assignmentEnd());var p=e.participants().get("R3");
        var provenance=new EvidenceProvenance(p.sourceName(),p.schemaFingerprint(),List.of(b.assignmentId().toString(),b.blockId().toString()),"R3_NOMINAL_PROJECTION","V1",ZONE+"/"+DATE,fields);
        return new ProgrammingCandidateSnapshot(b.reference(),DetectorVocabulary.CandidateType.NOMINAL_OCCURRENCE,
                closed("F2E-R3-SNAPSHOT-V1",p.executionIdentity(),closed("F2E-R3-PROJECTION-V1",closedMap(fields))),
                closed("F2E-R3-CANDIDATE-V1",b.reference().toString(),closedMap(o)),b.salonId(),b.instructorId(),b.activityId(),b.assignmentStart(),b.assignmentEnd(),o,provenance);
    }
    static AdjustmentBackingSnapshot adjustment(int index,int kind) {
        // kind 1 cancellation, 2 equal replacement, 3 moved replacement, 4 addition.
        boolean cancel=kind==1,addition=kind==4,moved=kind==3;var n=nominal(index);var stamp=OffsetDateTime.parse("2026-10-01T00:00:00Z");
        return new AdjustmentBackingSnapshot(id(400+index),addition?"ADICION":cancel?"CANCELACION":"REEMPLAZO",DATE,addition?null:n.assignmentSeriesId(),
                cancel?null:moved?id(2):id(1),cancel?null:addition?id(70+index):moved?id(50+index):n.instructorId(),cancel?null:moved?id(4):id(3),
                cancel?null:moved?time(12+index):n.assignmentStart(),cancel?null:moved?time(13+index):n.assignmentEnd(),true,stamp,stamp);
    }
    static GenericSourceSnapshot source(AdjustmentBackingSnapshot b,EffectiveCompositionEnvelope e) {
        var p=e.participants().get("R4");var fields=b.normalizedFields();var atom=DetectorVocabulary.SourceAtomType.valueOf("NEW_"+b.tipo());
        String fp=closed("F2E-R4-SOURCE-V1","NEW_DARK_LAUNCH",atom.name(),b.id().toString(),closedMap(fields));
        return new GenericSourceSnapshot(DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH,atom,b.id().toString(),closed("F2E-R4-SNAPSHOT-V1",p.executionIdentity(),fp),fp,fields,
                new EvidenceProvenance(p.sourceName(),p.schemaFingerprint(),List.of(b.id().toString()),"R4_ADJUSTMENT_PROJECTION","V1",ZONE+"/"+DATE,fields));
    }
    static EffectiveCompositionInput input(List<NominalBackingSnapshot> nominal,List<AdjustmentBackingSnapshot> adjustments) {
        return input(nominal,adjustments,envelope(),null);
    }
    static EffectiveCompositionInput input(List<NominalBackingSnapshot> nominal,List<AdjustmentBackingSnapshot> adjustments,EffectiveCompositionEnvelope e,EffectiveValidityEvidence validity) {
        var n=List.copyOf(nominal);var a=List.copyOf(adjustments);
        var readN=new NominalProgrammingReadSet(n.stream().map(b->candidate(b,e)).toList(),n);
        var readA=new AdjustmentReadSet(a.stream().map(b->source(b,e)).toList(),a);
        if(validity==null)validity=validity(n,a,e);
        return new EffectiveCompositionInput(DATE,ZONE,RULE,e,readN,readA,validity);
    }
    static EffectiveCompositionInput input(int n,int[] kinds,int additions) {
        var nom=new ArrayList<NominalBackingSnapshot>();var a=new ArrayList<AdjustmentBackingSnapshot>();
        for(int k=0;k<n;k++){nom.add(nominal(k));if(kinds[k]!=0)a.add(adjustment(k,kinds[k]));}
        for(int k=0;k<additions;k++)a.add(adjustment(n+k,4));return input(nom,a);
    }
    static RelationMetadata meta(EffectiveCompositionEnvelope e,String relation,UUID id,List<String> keys) {
        var p=e.participants().get("VALIDITY");return new RelationMetadata(p.sourceName(),p.schemaFingerprint(),p.ruleVersion(),"VALIDITY",p.executionIdentity(),relation+"/"+id+"/"+DATE,true,keys);
    }
    record Dimensions(UUID salon,UUID instructor,UUID activity) { }
    static EffectiveValidityEvidence validity(List<NominalBackingSnapshot> n,List<AdjustmentBackingSnapshot> a,EffectiveCompositionEnvelope e) {
        var dimensions=new ArrayList<Dimensions>();
        for(var b:n){var target=a.stream().filter(x->Objects.equals(x.asignacionSerieId(),b.assignmentSeriesId())).findFirst().orElse(null);
            if(target==null)dimensions.add(new Dimensions(b.salonId(),b.instructorId(),b.activityId()));
            else if(!target.tipo().equals("CANCELACION"))dimensions.add(new Dimensions(target.salonResultadoId(),target.instructorResultadoId(),target.tipoActividadResultadoId()));}
        a.stream().filter(b->b.tipo().equals("ADICION")).forEach(b->dimensions.add(new Dimensions(b.salonResultadoId(),b.instructorResultadoId(),b.tipoActividadResultadoId())));
        var salons=new LinkedHashMap<UUID,Presence<Salon>>();var instructors=new LinkedHashMap<UUID,Presence<Instructor>>();var activities=new LinkedHashMap<UUID,Presence<Activity>>();
        var hours=new LinkedHashMap<UUID,Hours>();var roles=new LinkedHashMap<UUID,Relation<RoleEdge>>();
        var special=new LinkedHashMap<UUID,Relation<SpecializationEdge>>();var offers=new LinkedHashMap<UUID,Relation<OfferingEdge>>();
        for(var d:dimensions) {
            salons.put(d.salon(),new Presence<>(PresenceKind.PRESENT,d.salon(),new Salon(d.salon(),true),meta(e,"SALON",d.salon(),List.of(d.salon().toString()))));
            instructors.put(d.instructor(),new Presence<>(PresenceKind.PRESENT,d.instructor(),new Instructor(d.instructor(),"activo"),meta(e,"INSTRUCTOR",d.instructor(),List.of(d.instructor().toString()))));
            activities.put(d.activity(),new Presence<>(PresenceKind.PRESENT,d.activity(),new Activity(d.activity(),true),meta(e,"ACTIVITY",d.activity(),List.of(d.activity().toString()))));
            var w=new WeeklyHours(id(1000+(int)d.salon().getLeastSignificantBits()),d.salon(),(short)0,time(8),time(18),DATE,null);
            hours.put(d.salon(),new Hours(new Relation<>(meta(e,"DATE_EXCEPTIONS",d.salon(),List.of()),List.of()),new Relation<>(meta(e,"WEEKLY_HOURS",d.salon(),List.of(w.id().toString())),List.of(w))));
            var role=new RoleEdge(d.instructor(),id(6),"INSTRUCTOR",null);String rk=d.instructor()+"/"+id(6)+"/ABSENT";
            roles.put(d.instructor(),new Relation<>(meta(e,"ROLES",d.instructor(),List.of(rk)),List.of(role)));
            var sr=new ArrayList<>(special.containsKey(d.instructor())?special.get(d.instructor()).rows():List.<SpecializationEdge>of());
            var se=new SpecializationEdge(d.instructor(),d.activity());if(!sr.contains(se))sr.add(se);
            special.put(d.instructor(),new Relation<>(meta(e,"SPECIALIZATIONS",d.instructor(),sr.stream().map(x->x.usuarioId()+"/"+x.activityId()).toList()),sr));
            var or=new ArrayList<>(offers.containsKey(d.salon())?offers.get(d.salon()).rows():List.<OfferingEdge>of());
            var oe=new OfferingEdge(d.salon(),d.activity());if(!or.contains(oe))or.add(oe);
            offers.put(d.salon(),new Relation<>(meta(e,"OFFERINGS",d.salon(),or.stream().map(x->x.salonId()+"/"+x.activityId()).toList()),or));
        }
        return new EffectiveValidityEvidence(salons,instructors,activities,hours,roles,special,offers);
    }
    static EffectiveCompositionInput withCause(EffectiveCompositionInput i,int cause) {
        if(cause==0)return i;var v=i.validityEvidence();
        var salons=new LinkedHashMap<>(v.salons());var instructors=new LinkedHashMap<>(v.instructors());var activities=new LinkedHashMap<>(v.activities());
        var hours=new LinkedHashMap<>(v.hours());var roles=new LinkedHashMap<>(v.roles());var specs=new LinkedHashMap<>(v.specializations());var offers=new LinkedHashMap<>(v.offerings());
        if(cause==1)salons.replaceAll((id,p)->copy(p,"value",new Salon(id,false)));
        if(cause==2)hours.replaceAll((id,h)->copy(h,"weekly",new Relation<>(meta(i.envelope(),"WEEKLY_HOURS",id,List.of()),List.of())));
        if(cause==3)hours.replaceAll((id,h)->{var w=copy(h.weekly().rows().getFirst(),"opening",time(17));return copy(h,"weekly",new Relation<>(h.weekly().metadata(),List.of(w)));});
        if(cause==4)instructors.replaceAll((id,p)->copy(p,"value",new Instructor(id,"suspendido")));
        if(cause==5)roles.replaceAll((id,r)->new Relation<>(meta(i.envelope(),"ROLES",id,List.of()),List.of()));
        if(cause==6)activities.replaceAll((id,p)->copy(p,"value",new Activity(id,false)));
        if(cause==7)specs.replaceAll((id,r)->new Relation<>(meta(i.envelope(),"SPECIALIZATIONS",id,List.of()),List.of()));
        if(cause==8)offers.replaceAll((id,r)->new Relation<>(meta(i.envelope(),"OFFERINGS",id,List.of()),List.of()));
        return copy(i,"validityEvidence",new EffectiveValidityEvidence(salons,instructors,activities,hours,roles,specs,offers));
    }

    static ComparisonInputV2 comparison(EffectiveCompositionInput input) { return comparison(input,List.of()); }
    static ComparisonInputV2 comparison(EffectiveCompositionInput input,List<GenericSourceSnapshot> legacy) {
        var result=new EffectiveProgrammingComposer().compose(input);
        return new ComparisonInputV2("SYNTHETIC_RUN","SYNTHETIC_COMPARISON",DATE,ZONE,Instant.parse("2026-10-04T12:00:00Z"),ComparisonInputV2.RULE,
                ComparisonInputV2.Mode.SYNTHETIC_DESIGN_FIXTURE,result.envelope().completionReceiptIdentity(),List.of(),legacy,input.nominalReadSet(),input.adjustmentReadSet(),result);
    }
    static List<GenericSourceSnapshot> legacy(String type,int shape,int serial) { return legacy(type,shape,serial,true); }
    static List<GenericSourceSnapshot> legacy(String type,int shape,int serial,boolean synthetic) {
        var scope=new com.feelingpilates.transicion.programacion.read.LegacyTurnScope(Set.of(id(1)),DATE);
        var context=new com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext(synthetic?"SYNTHETIC_RUN":"run",synthetic?"SYNTHETIC_ATTEMPT":"attempt",synthetic?"SYNTHETIC_LEGACY":"REAL_LEGACY","sha256:"+HASH,
                com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext.ProjectionCatalogVersion.R2_LEGACY_TURN_V1,synthetic?"SYNTHETIC_RULE":"rules",ZoneId.of(ZONE),scope.canonical(),
                com.feelingpilates.transicion.programacion.read.LegacyTurnReadContext.SnapshotClaim.R2_INTERNAL_RR_TEST,HASH);
        var turn=id(800+serial);var stamp=OffsetDateTime.parse("2026-10-01T00:00:00Z");
        var member=new com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnMemberRow(turn,type,true,id(1),type.equals("RECURRENTE")?(short)0:null,
                type.equals("RECURRENTE")?null:DATE,time(8),time(18),stamp,stamp,shape==3?null:shape==4?id(99):id(20));
        List<com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyAssignmentRow> assignments=shape==2||shape==3?List.of():List.of(
                new com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyAssignmentRow(turn,id(20),id(3),shape==1?null:shape==6?time(7):time(9),
                        shape==1||shape==5?null:shape==6?time(19):time(10)));
        return new com.feelingpilates.transicion.programacion.adapter.jpa.mapper.LegacyTurnProjectionMapper(
                com.feelingpilates.transicion.programacion.adapter.jpa.projection.LegacyTurnProjectionCatalog.R2_LEGACY_TURN_V1).mapear(List.of(member),assignments,context,scope).sources();
    }

    /** Real-mode declarations only; these fixtures do NOT authenticate physical acquisition. */
    static EffectiveCompositionEnvelope declaredRealEnvelope() {
        var participants=new LinkedHashMap<String,EffectiveCompositionEnvelope.Participant>();
        for(var entry:envelope().participants().entrySet()) {
            var p=entry.getValue();String key=entry.getKey();
            p=new EffectiveCompositionEnvelope.Participant("REAL_"+key,p.schemaFingerprint(),p.ruleVersion(),p.readScope(),"PENDING",HASH,
                    "resource","boundary",HASH,EffectiveCompositionEnvelope.Completion.SUCCESSFUL,"run","attempt","rules","database","public","reader",
                    key.equals("VALIDITY")?"VALIDITY_V1":p.projectionCatalogVersion(),"invocation",p.snapshotClaim(),HASH,HASH);
            p=copy(p,"executionIdentity",execution(p,key));participants.put(key,p);
        }
        return new EffectiveCompositionEnvelope(EffectiveCompositionEnvelope.Mode.R6_SUPPLIED_COHERENT_READ_FACTS,DATE,ZONE,"composition","authority","receipt",participants);
    }
    static ComparisonInputV2 declaredRealInput(List<GenericSourceSnapshot> sources) {
        var e=declaredRealEnvelope();var i=input(List.of(nominal(0)),List.of(),e,null);var result=new EffectiveProgrammingComposer().compose(i);
        return new ComparisonInputV2("run","invocation",DATE,ZONE,Instant.parse("2026-10-04T12:00:00Z"),ComparisonInputV2.RULE,
                ComparisonInputV2.Mode.OWNER_AUTHENTICATED_SUPPLIED,"receipt",List.of(),sources,i.nominalReadSet(),i.adjustmentReadSet(),result);
    }

}
