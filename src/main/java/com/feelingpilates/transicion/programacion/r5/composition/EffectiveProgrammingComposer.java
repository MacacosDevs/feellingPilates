package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.detector.EvidenceProvenance;
import com.feelingpilates.transicion.programacion.detector.DetectorVocabulary;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionFailure.Category.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionBacking.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveValidityEvidence.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionCanonicalizer.*;

/** Stateless, pure composition of already-supplied CLOSED facts. */
public final class EffectiveProgrammingComposer {
    public static final String RULE = "F2E-R5-PURE-COMPOSITION-V1";

    public EffectiveProgrammingCompositionResult compose(EffectiveCompositionInput input) {
        require(input!=null,COMPOSITION_INPUT_INVALID,input,"input required");
        admit(input);
        var session=new Session();
        String commitment=session.hash("F2E-R5-INPUT-V1",input);
        String schema=schema(input,session);
        var outcomes=preliminary(input,schema);
        validateEvidence(input,outcomes.preliminary());
        var backing=new LinkedHashMap<ReferenciaOcurrencia,EffectiveCompositionBacking>();
        var candidates=new ArrayList<ProgrammingCandidateSnapshot>();
        var omissions=new ArrayList<EffectiveCompositionOmission>();
        for(var b:outcomes.preliminary()) {
            var cause=firstCause(input,b);
            if(cause!=null) omissions.add(new EffectiveCompositionOmission(b.reference(),b,cause,b.support(),RULE,schema,input.envelope()));
            else {
                backing.put(b.reference(),b);
                candidates.add(candidate(input,b,commitment,schema,"PENDING_HASH",session));
            }
        }
        conflicts(input,candidates);
        String content=content(input,candidates,backing,omissions,outcomes.suppressions(),session);
        String snapshot=session.hash("F2E-R5-SNAPSHOT-V1",commitment,content,input.envelope());
        candidates.replaceAll(c->withSnapshot(c,session.hash("F2E-R5-CANDIDATE-SNAPSHOT-V1",snapshot,c.reference(),c.candidateFingerprint())));
        return EffectiveProgrammingCompositionResult.issue(input,candidates,backing,omissions,outcomes.suppressions(),commitment,content,snapshot);
    }

    private static void require(boolean ok,EffectiveCompositionFailure.Category category,EffectiveCompositionInput i,String rule) {
        if(!ok) throw failure(category,i,rule,null);
    }
    private static EffectiveCompositionFailure failure(EffectiveCompositionFailure.Category category,EffectiveCompositionInput i,String rule,Throwable cause) {
        var refs=new ArrayList<ReferenciaOcurrencia>();var ids=new ArrayList<UUID>();
        if(i!=null) {
            i.nominalReadSet().backing().forEach(b->{refs.add(b.reference());ids.add(b.assignmentId());ids.add(b.blockId());});
            i.adjustmentReadSet().backing().forEach(b->{if(b.id()!=null)ids.add(b.id());
                if(b.asignacionSerieId()!=null)refs.add(new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,b.asignacionSerieId(),i.date()));
                else if(b.id()!=null)refs.add(new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE,b.id(),i.date()));});
        }
        return new EffectiveCompositionFailure(category,i==null?null:i.date(),refs,ids,rule,i,cause);
    }
    static void admit(EffectiveCompositionInput i) {
        try { ZoneId.of(i.businessZoneId()); }
        catch(DateTimeException e) { throw failure(COMPOSITION_INPUT_INVALID,i,"explicit valid business zone",e); }
        require(RULE.equals(i.ruleVersion()),COMPOSITION_INPUT_INVALID,i,"fixed composition rule");
        envelope(i);
        // Force strict scalar/schema validation over all retained fields, including otherwise unused support.
        try { encode(i); }
        catch(EffectiveCompositionFailure e) { throw failure(CANONICALIZATION_FAILURE,i,"full input canonicalization",e); }
        nominal(i); adjustments(i);
    }
    private static void envelope(EffectiveCompositionInput i) {
        var e=i.envelope();
        require(e.mode()!=null && i.date().equals(e.date()) && i.businessZoneId().equals(e.businessZoneId())
                && e.participants().keySet().equals(Set.of("R3","R4","VALIDITY")),INPUT_ENVELOPE_INVALID,i,"envelope mode/date/zone/participants");
        textRequired(i,e.invocationIdentity(),e.authorityVersion(),e.completionReceiptIdentity());
        boolean synthetic=e.mode()==EffectiveCompositionEnvelope.Mode.SYNTHETIC_DESIGN_FIXTURE;
        require(synthetic?e.completionReceiptIdentity().equals(EffectiveCompositionEnvelope.SYNTHETIC):
                !syntheticToken(e.completionReceiptIdentity()),INPUT_ENVELOPE_INVALID,i,"completion receipt mode");
        for(var entry:e.participants().entrySet()) {
            var p=entry.getValue();
            textRequired(i,p.sourceName(),p.schemaFingerprint(),p.ruleVersion(),p.readScope(),p.executionIdentity(),p.snapshotIdentity(),
                    p.physicalResourceIdentity(),p.transactionBoundaryIdentity(),p.statementCaptureIdentity(),p.runIdentity(),p.attemptIdentity(),
                    p.ruleCatalogVersion(),p.databaseName(),p.schemaName(),p.principal(),p.projectionCatalogVersion(),p.readerInvocationIdentity(),
                    p.snapshotClaim(),p.snapshotEvidenceId(),p.statementCaptureCommitment());
            require(p.ruleVersion().equals("V1") && p.schemaFingerprint().matches("sha256:[0-9a-f]{64}") && p.snapshotEvidenceId().matches("[0-9a-f]{64}")
                    && p.statementCaptureCommitment().matches("[0-9a-f]{64}"),INPUT_ENVELOPE_INVALID,i,"participant descriptor hashes");
            if(synthetic) require(p.completion()==EffectiveCompositionEnvelope.Completion.SYNTHETIC
                    && p.physicalResourceIdentity().equals(EffectiveCompositionEnvelope.SYNTHETIC)
                    && p.transactionBoundaryIdentity().equals(EffectiveCompositionEnvelope.SYNTHETIC)
                    && p.statementCaptureIdentity().equals(EffectiveCompositionEnvelope.SYNTHETIC),INPUT_ENVELOPE_INVALID,i,"explicit synthetic participant");
            else {
                require(p.completion()==EffectiveCompositionEnvelope.Completion.SUCCESSFUL,INPUT_ENVELOPE_INVALID,i,"successful participant completion");
                for(String s:List.of(p.sourceName(),p.readScope(),p.executionIdentity(),p.snapshotIdentity(),p.physicalResourceIdentity(),
                        p.transactionBoundaryIdentity(),p.statementCaptureIdentity(),p.runIdentity(),p.attemptIdentity(),p.ruleCatalogVersion(),
                        p.databaseName(),p.schemaName(),p.principal(),p.readerInvocationIdentity(),e.invocationIdentity(),e.authorityVersion()))
                    require(!syntheticToken(s),INPUT_ENVELOPE_INVALID,i,"real mode forbids synthetic tokens");
                var r=e.participants().get("R3");
                require(p.physicalResourceIdentity().equals(r.physicalResourceIdentity())
                        && p.transactionBoundaryIdentity().equals(r.transactionBoundaryIdentity())
                        && p.snapshotIdentity().equals(r.snapshotIdentity()),INPUT_ENVELOPE_INVALID,i,"declared shared physical boundary/snapshot");
                require(p.statementCaptureIdentity().equals(p.statementCaptureCommitment()),INPUT_ENVELOPE_INVALID,i,"declared statement capture binding");
            }
            String key=entry.getKey();
            String scope=key.equals("R3")?"R3_NOMINAL_ON_DATE_V1/"+i.date()+"/"+(i.date().getDayOfWeek().getValue()%7):
                    key.equals("R4")?"R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/"+i.date():"R5_VALIDITY_ON_DATE_V1/"+i.date();
            require(p.readScope().equals(scope),INPUT_ENVELOPE_INVALID,i,"complete global read scope "+key);
            if(!key.equals("VALIDITY")) {
                require(p.ruleVersion().equals("V1") && p.projectionCatalogVersion().equals(key.equals("R3")?"R3_NOMINAL_V1":"R4_ADJUSTMENT_V1")
                        && p.snapshotClaim().equals(key+"_INTERNAL_RR_TEST"),INPUT_ENVELOPE_INVALID,i,"preserved closed descriptor "+key);
                require(execution(i,key).equals(p.executionIdentity()),INPUT_ENVELOPE_INVALID,i,"original execution formula "+key);
            }
        }
    }
    private static boolean syntheticToken(String s) { String u=s.toUpperCase(Locale.ROOT);return u.contains("SYNTHETIC")||u.contains("FIXTURE"); }
    private static void textRequired(EffectiveCompositionInput i,String... values) {
        for(var s:values) require(s!=null&&!s.isBlank(),INPUT_ENVELOPE_INVALID,i,"required participant text");
    }
    static String execution(EffectiveCompositionInput i,String key) {
        var p=i.envelope().participants().get(key);
        if(key.equals("R3")) return closedHash("F2E-R3-EXECUTION-V1",p.runIdentity(),p.attemptIdentity(),p.sourceName(),p.schemaFingerprint(),
                p.ruleCatalogVersion(),i.businessZoneId(),p.readScope(),p.readerInvocationIdentity(),p.databaseName(),p.schemaName(),p.principal(),
                p.physicalResourceIdentity(),p.projectionCatalogVersion(),p.snapshotClaim(),p.snapshotEvidenceId(),p.statementCaptureCommitment());
        return closedHash("F2E-R4-EXECUTION-V1",p.runIdentity(),p.attemptIdentity(),p.readScope(),i.businessZoneId(),p.ruleCatalogVersion(),p.sourceName(),
                p.schemaFingerprint(),p.databaseName(),p.schemaName(),p.principal(),p.physicalResourceIdentity(),p.projectionCatalogVersion(),
                p.readerInvocationIdentity(),p.snapshotClaim(),p.snapshotEvidenceId(),p.statementCaptureCommitment());
    }
    private static boolean applicable(LocalDate from,LocalDate until,LocalDate date) {
        return from!=null&&!from.isAfter(date)&&(until==null||(!until.isBefore(from)&&!until.isBefore(date)));
    }
    private static void interval(EffectiveCompositionInput i,LocalTime start,LocalTime end) {
        require(start!=null&&end!=null&&end.isAfter(start),COMPOSITION_INPUT_INVALID,i,"positive local time6 interval");
        try { ReadSnapshotIdentifiers.hora(start);ReadSnapshotIdentifiers.hora(end); }
        catch(IllegalArgumentException e) { throw failure(COMPOSITION_INPUT_INVALID,i,"time6 precision",e); }
    }
    private static void timestamp(EffectiveCompositionInput i,OffsetDateTime t,boolean adjustment) {
        require(t!=null,COMPOSITION_INPUT_INVALID,i,"required technical timestamp");
        require(!adjustment||(t.getYear()>=-4712&&t.getYear()<=294276&&!t.equals(OffsetDateTime.MIN)&&!t.equals(OffsetDateTime.MAX)),
                COMPOSITION_INPUT_INVALID,i,"closed R4 timestamp range");
        try { ReadSnapshotIdentifiers.instante(t); } catch(IllegalArgumentException e) {throw failure(COMPOSITION_INPUT_INVALID,i,"timestamp6 precision",e);}
    }
    static Map<String,String> observables(UUID salon,UUID instructor,UUID activity,LocalTime start,LocalTime end,LocalDate date) {
        return Map.of("salonId",salon.toString(),"instructorId",instructor.toString(),"activityId",activity.toString(),
                "start",ReadSnapshotIdentifiers.hora(start),"end",ReadSnapshotIdentifiers.hora(end),"fecha",date.toString());
    }
    static Map<String,String> nominalFields(EffectiveCompositionInput i,NominalBackingSnapshot b) {
        var p=i.envelope().participants().get("R3");var m=new LinkedHashMap<String,String>();
        m.put("assignmentSeriesId",b.assignmentSeriesId()==null?"NULL":b.assignmentSeriesId().toString());
        m.put("assignmentId",b.assignmentId()==null?"NULL":b.assignmentId().toString());
        m.put("assignmentBlockId",b.assignmentBlockId()==null?"NULL":b.assignmentBlockId().toString());
        m.put("blockId",b.blockId()==null?"NULL":b.blockId().toString());
        m.put("blockSeriesId",b.blockSeriesId()==null?"NULL":b.blockSeriesId().toString());
        m.put("salonId",b.salonId()==null?"NULL":b.salonId().toString());
        m.put("instructorId",b.instructorId()==null?"NULL":b.instructorId().toString());
        m.put("activityId",b.activityId()==null?"NULL":b.activityId().toString());
        m.put("assignmentStart",ReadSnapshotIdentifiers.hora(b.assignmentStart()));
        m.put("assignmentEnd",ReadSnapshotIdentifiers.hora(b.assignmentEnd()));
        m.put("blockStart",ReadSnapshotIdentifiers.hora(b.blockStart()));
        m.put("blockEnd",ReadSnapshotIdentifiers.hora(b.blockEnd()));
        m.put("assignmentFrom",b.assignmentFrom()==null?"NULL":b.assignmentFrom().toString());
        m.put("assignmentUntil",b.assignmentUntil()==null?"NULL":b.assignmentUntil().toString());
        m.put("assignmentActive",b.assignmentActive()==null?"NULL":b.assignmentActive().toString());
        m.put("blockFrom",b.blockFrom()==null?"NULL":b.blockFrom().toString());
        m.put("blockUntil",b.blockUntil()==null?"NULL":b.blockUntil().toString());
        m.put("blockActive",b.blockActive()==null?"NULL":b.blockActive().toString());
        m.put("assignmentCreated",ReadSnapshotIdentifiers.instante(b.assignmentCreated()));
        m.put("assignmentUpdated",ReadSnapshotIdentifiers.instante(b.assignmentUpdated()));
        m.put("blockCreated",ReadSnapshotIdentifiers.instante(b.blockCreated()));
        m.put("blockUpdated",ReadSnapshotIdentifiers.instante(b.blockUpdated()));
        m.put("blockDay",b.blockDay()==null?"NULL":b.blockDay().toString());
        m.put("fecha",b.fecha()==null?"NULL":b.fecha().toString());
        m.put("dayOfWeek",b.dayOfWeek()==null?"NULL":b.dayOfWeek().toString());
        m.put("snapshotEvidenceId",p.snapshotEvidenceId());m.put("executionProvenanceId",p.executionIdentity());
        m.put("statementCaptureCommitment",p.statementCaptureCommitment());return Map.copyOf(m);
    }
    private static void provenance(EffectiveCompositionInput i,EvidenceProvenance v,String key,String rule,List<String> ids,Map<String,String> fields) {
        var p=i.envelope().participants().get(key);
        require(v.sourceName().equals(p.sourceName())&&v.schemaFingerprint().equals(p.schemaFingerprint())
                &&v.ruleId().equals(rule)&&v.ruleVersion().equals(p.ruleVersion())
                &&v.businessTimeContext().equals(i.businessZoneId()+"/"+i.date())&&v.recordIds().equals(ids)
                &&v.normalizedFields().equals(fields),READ_SET_INVARIANT_VIOLATION,i,"original "+key+" provenance and exact fields");
    }
    private static void nominal(EffectiveCompositionInput i) {
        var n=i.nominalReadSet();
        require(n.candidates().size()==n.backing().size(),READ_SET_INVARIANT_VIOLATION,i,"nominal bijection");
        var refs=new HashSet<ReferenciaOcurrencia>();var assignments=new HashSet<UUID>();
        var blocks=new HashMap<UUID,List<Object>>(); NominalBackingSnapshot previous=null;
        for(int k=0;k<n.backing().size();k++) {
            var b=n.backing().get(k);var c=n.candidates().get(k);
            require(refs.add(b.reference())&&assignments.add(b.assignmentId()),AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"unique nominal identity/physical assignment");
            require(i.date().equals(b.fecha())&&b.reference().tipo()==ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION
                    &&b.reference().id().equals(b.assignmentSeriesId())&&b.reference().fecha().equals(i.date())
                    &&b.dayOfWeek()==(short)(i.date().getDayOfWeek().getValue()%7)&&b.dayOfWeek().equals(b.blockDay())
                    &&b.assignmentBlockId().equals(b.blockId())&&Boolean.TRUE.equals(b.assignmentActive())&&Boolean.TRUE.equals(b.blockActive())
                    &&applicable(b.assignmentFrom(),b.assignmentUntil(),i.date())&&applicable(b.blockFrom(),b.blockUntil(),i.date()),
                    COMPOSITION_INPUT_INVALID,i,"closed nominal applicability/shape");
            interval(i,b.assignmentStart(),b.assignmentEnd());interval(i,b.blockStart(),b.blockEnd());
            require(!b.assignmentStart().isBefore(b.blockStart())&&!b.assignmentEnd().isAfter(b.blockEnd()),COMPOSITION_INPUT_INVALID,i,"assignment contained in block");
            for(var t:List.of(b.assignmentCreated(),b.assignmentUpdated(),b.blockCreated(),b.blockUpdated()))timestamp(i,t,false);
            var payload=Arrays.<Object>asList(b.blockSeriesId(),b.salonId(),b.blockStart(),b.blockEnd(),b.blockFrom(),b.blockUntil(),
                    b.blockActive(),b.blockCreated(),b.blockUpdated(),b.blockDay());
            var old=blocks.putIfAbsent(b.blockId(),payload);
            require(old==null||old.equals(payload),AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"shared block physical consistency");
            require(previous==null||UUID_ORDER.compare(previous.assignmentSeriesId(),b.assignmentSeriesId())<0,
                    READ_SET_INVARIANT_VIOLATION,i,"original closed nominal list order");previous=b;
            var obs=observables(b.salonId(),b.instructorId(),b.activityId(),b.assignmentStart(),b.assignmentEnd(),i.date());
            require(c.reference().equals(b.reference())&&c.candidateType()==DetectorVocabulary.CandidateType.NOMINAL_OCCURRENCE
                    &&c.salonId().equals(b.salonId())&&c.instructorId().equals(b.instructorId())&&c.activityId().equals(b.activityId())
                    &&c.start().equals(b.assignmentStart())&&c.end().equals(b.assignmentEnd())&&c.observableFields().equals(obs),
                    READ_SET_INVARIANT_VIOLATION,i,"nominal candidate/backing/observable equality");
            var fields=nominalFields(i,b);
            provenance(i,c.provenance(),"R3","R3_NOMINAL_PROJECTION",List.of(b.assignmentId().toString(),b.blockId().toString()),fields);
            String projection=closedHash("F2E-R3-PROJECTION-V1",ReadSnapshotIdentifiers.decodificarUtf8(ReadSnapshotIdentifiers.mapaCanonico(fields)));
            require(c.candidateFingerprint().equals(closedHash("F2E-R3-CANDIDATE-V1",b.reference().toString(),
                    ReadSnapshotIdentifiers.decodificarUtf8(ReadSnapshotIdentifiers.mapaCanonico(obs))))
                    &&c.snapshotIdentity().equals(closedHash("F2E-R3-SNAPSHOT-V1",i.envelope().participants().get("R3").executionIdentity(),projection)),
                    READ_SET_INVARIANT_VIOLATION,i,"stored closed nominal fingerprints");
        }
    }
    private static void adjustments(EffectiveCompositionInput i) {
        var a=i.adjustmentReadSet();var ids=new HashSet<UUID>();var targets=new HashSet<UUID>();UUID previous=null;
        require(a.sources().size()==a.backing().size(),READ_SET_INVARIANT_VIOLATION,i,"adjustment bijection");
        for(int k=0;k<a.backing().size();k++) {
            var b=a.backing().get(k);var s=a.sources().get(k);
            require(b.id()!=null&&b.tipo()!=null&&i.date().equals(b.fecha())&&Boolean.TRUE.equals(b.activo())
                    &&Set.of("CANCELACION","REEMPLAZO","ADICION").contains(b.tipo()),COMPOSITION_INPUT_INVALID,i,"active exact-date known adjustment form");
            require(ids.add(b.id())&&(b.asignacionSerieId()==null||targets.add(b.asignacionSerieId())),
                    AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"unique adjustment ID and active series target");
            require(previous==null||UUID_ORDER.compare(previous,b.id())<0,READ_SET_INVARIANT_VIOLATION,i,"original closed adjustment order");previous=b.id();
            boolean cancel=b.tipo().equals("CANCELACION"),add=b.tipo().equals("ADICION");
            require((b.asignacionSerieId()==null)==add,COMPOSITION_INPUT_INVALID,i,"adjustment target shape");
            for(var v:Arrays.asList(b.salonResultadoId(),b.instructorResultadoId(),b.tipoActividadResultadoId(),b.horaInicioResultado(),b.horaFinResultado()))
                require((v==null)==cancel,COMPOSITION_INPUT_INVALID,i,"all five adjustment result fields");
            if(!cancel)interval(i,b.horaInicioResultado(),b.horaFinResultado());
            timestamp(i,b.creadoEn(),true);timestamp(i,b.actualizadoEn(),true);
            var fields=b.normalizedFields();
            require(s.sourceSystem()==DetectorVocabulary.SourceSystem.NEW_DARK_LAUNCH&&s.sourceAtomType().name().equals("NEW_"+b.tipo())
                    &&s.sourceIdentity().equals(b.id().toString())&&s.observableFields().equals(fields),READ_SET_INVARIANT_VIOLATION,i,"adjustment source/backing equality");
            provenance(i,s.provenance(),"R4","R4_ADJUSTMENT_PROJECTION",List.of(b.id().toString()),fields);
            String fp=closedHash("F2E-R4-SOURCE-V1","NEW_DARK_LAUNCH",s.sourceAtomType().name(),b.id().toString(),
                    ReadSnapshotIdentifiers.decodificarUtf8(ReadSnapshotIdentifiers.mapaCanonico(fields)));
            require(s.sourceFingerprint().equals(fp)&&s.snapshotIdentity().equals(closedHash("F2E-R4-SNAPSHOT-V1",i.envelope().participants().get("R4").executionIdentity(),fp)),
                    READ_SET_INVARIANT_VIOLATION,i,"stored closed adjustment hashes");
        }
    }
    private record Outcomes(List<EffectiveCompositionBacking> preliminary,List<EffectiveCompositionSuppression> suppressions) { }
    private static Outcomes preliminary(EffectiveCompositionInput i,String schema) {
        var a=i.adjustmentReadSet();var targets=new HashMap<UUID,Integer>();
        for(int k=0;k<a.backing().size();k++)if(a.backing().get(k).asignacionSerieId()!=null)targets.put(a.backing().get(k).asignacionSerieId(),k);
        var n=i.nominalReadSet();var present=new HashSet<UUID>();n.backing().forEach(b->present.add(b.assignmentSeriesId()));
        require(present.containsAll(targets.keySet()),TARGET_NOMINAL_MISSING,i,"each cancellation/replacement has exactly one nominal target");
        var p=new ArrayList<EffectiveCompositionBacking>();var s=new ArrayList<EffectiveCompositionSuppression>();
        for(int k=0;k<n.backing().size();k++) {
            var nb=n.backing().get(k);var nc=n.candidates().get(k);Integer target=targets.get(nb.assignmentSeriesId());
            var ab=target==null?null:a.backing().get(target);var as=target==null?null:a.sources().get(target);
            if(ab!=null&&ab.tipo().equals("CANCELACION"))s.add(new EffectiveCompositionSuppression(nb.reference(),nc,nb,as,ab,RULE,schema,i.envelope()));
            else p.add(backing(i,nb.reference(),nc,nb,as,ab));
        }
        for(int k=0;k<a.backing().size();k++) {
            var b=a.backing().get(k);
            if(b.tipo().equals("ADICION"))p.add(backing(i,new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE,b.id(),i.date()),null,null,a.sources().get(k),b));
        }
        p.sort(Comparator.comparing(EffectiveCompositionBacking::reference,REFERENCE_ORDER));
        s.sort(Comparator.comparing(EffectiveCompositionSuppression::reference,REFERENCE_ORDER));
        return new Outcomes(List.copyOf(p),List.copyOf(s));
    }
    private static EffectiveCompositionBacking backing(EffectiveCompositionInput i,ReferenciaOcurrencia r,ProgrammingCandidateSnapshot n,
            NominalBackingSnapshot nb,GenericSourceSnapshot a,AdjustmentBackingSnapshot ab) {
        var origin=ab==null?Origin.RECURRENT_OCCURRENCE:nb==null?Origin.ADDITION_OCCURRENCE:Origin.REPLACEMENT_OCCURRENCE;
        return new EffectiveCompositionBacking(r,origin,ab==null?nb.salonId():ab.salonResultadoId(),ab==null?nb.instructorId():ab.instructorResultadoId(),
                ab==null?nb.activityId():ab.tipoActividadResultadoId(),ab==null?nb.assignmentStart():ab.horaInicioResultado(),ab==null?nb.assignmentEnd():ab.horaFinResultado(),
                nb==null?NominalAxis.NOT_APPLICABLE:NominalAxis.PRESENT,n,nb,ab==null?AdjustmentAxis.NOT_APPLICABLE:AdjustmentAxis.PRESENT,a,ab,i.validityEvidence());
    }
    static String scope(String relation,UUID id,LocalDate date) { return relation+"/"+id+"/"+date; }
    static String roleKey(RoleEdge r) { return r.usuarioId()+"/"+r.rolId()+"/"+(r.nullableSalonId()==null?"ABSENT":r.nullableSalonId()); }
    static String specializationKey(SpecializationEdge r) { return r.usuarioId()+"/"+r.activityId(); }
    static String offeringKey(OfferingEdge r) { return r.salonId()+"/"+r.activityId(); }
    private static void metadata(EffectiveCompositionInput i,RelationMetadata m,String relation,UUID id,List<String> keys) {
        require(m!=null,INPUT_EVIDENCE_INCOMPLETE,i,"metadata required "+relation);
        var p=i.envelope().participants().get("VALIDITY");
        require(m.complete()&&"VALIDITY".equals(m.participantName())&&p.executionIdentity().equals(m.participantExecutionIdentity())
                &&p.sourceName().equals(m.sourceName())&&p.schemaFingerprint().equals(m.schemaFingerprint())
                &&p.ruleVersion().equals(m.ruleVersion())&&scope(relation,id,i.date()).equals(m.scope()),
                INPUT_EVIDENCE_INCOMPLETE,i,"complete bound relation "+relation);
        require(new HashSet<>(keys).size()==keys.size()&&new HashSet<>(m.recordKeys()).size()==m.recordKeys().size(),
                AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"duplicate physical/composite support key "+relation);
        require(new HashSet<>(keys).equals(new HashSet<>(m.recordKeys())),INPUT_EVIDENCE_INCOMPLETE,i,"physical key coverage "+relation);
    }
    private static void presence(EffectiveCompositionInput i,UUID id,Presence<?> p,String relation) {
        require(p!=null&&id!=null&&id.equals(p.queriedId())&&p.kind()!=null,INPUT_EVIDENCE_INCOMPLETE,i,"typed queried presence "+relation);
        require((p.kind()==PresenceKind.PRESENT)==(p.value()!=null),COMPOSITION_INPUT_INVALID,i,"presence shape "+relation);
        UUID actual=null;
        if(p.value() instanceof Salon v) {require(relation.equals("SALON"),COMPOSITION_INPUT_INVALID,i,"typed salon relation");actual=v.id();require(v.activo()!=null,COMPOSITION_INPUT_INVALID,i,"salon active scalar");}
        else if(p.value() instanceof Instructor v) {require(relation.equals("INSTRUCTOR"),COMPOSITION_INPUT_INVALID,i,"typed instructor relation");actual=v.id();require(v.estatus()!=null&&Set.of("activo","suspendido","eliminado").contains(v.estatus()),COMPOSITION_INPUT_INVALID,i,"known instructor status");}
        else if(p.value() instanceof Activity v) {require(relation.equals("ACTIVITY"),COMPOSITION_INPUT_INVALID,i,"typed activity relation");actual=v.id();require(v.activo()!=null,COMPOSITION_INPUT_INVALID,i,"activity active scalar");}
        else require(p.value()==null,COMPOSITION_INPUT_INVALID,i,"typed master value");
        require(actual==null?p.kind()==PresenceKind.ABSENT:actual.equals(id),COMPOSITION_INPUT_INVALID,i,"queried/entity identity "+relation);
        metadata(i,p.metadata(),relation,id,actual==null?List.of():List.of(actual.toString()));
    }
    private static void validateEvidence(EffectiveCompositionInput i,List<EffectiveCompositionBacking> preliminary) {
        var v=i.validityEvidence();
        for(Object value:v.salons().values())require(value instanceof Presence<?>,COMPOSITION_INPUT_INVALID,i,"typed salon presence");
        for(Object value:v.instructors().values())require(value instanceof Presence<?>,COMPOSITION_INPUT_INVALID,i,"typed instructor presence");
        for(Object value:v.activities().values())require(value instanceof Presence<?>,COMPOSITION_INPUT_INVALID,i,"typed activity presence");
        for(Object value:v.hours().values())require(value instanceof Hours,COMPOSITION_INPUT_INVALID,i,"typed hours relation");
        for(Object value:v.roles().values())require(value instanceof Relation<?>,COMPOSITION_INPUT_INVALID,i,"typed role relation");
        for(Object value:v.specializations().values())require(value instanceof Relation<?>,COMPOSITION_INPUT_INVALID,i,"typed specialization relation");
        for(Object value:v.offerings().values())require(value instanceof Relation<?>,COMPOSITION_INPUT_INVALID,i,"typed offering relation");
        for(var h:v.hours().values()) {
            if(h.exceptions()!=null)for(Object row:h.exceptions().rows())require(row instanceof DateException,COMPOSITION_INPUT_INVALID,i,"typed date exception row");
            if(h.weekly()!=null)for(Object row:h.weekly().rows())require(row instanceof WeeklyHours,COMPOSITION_INPUT_INVALID,i,"typed weekly row");
        }
        for(var r:v.roles().values())for(Object row:r.rows())require(row instanceof RoleEdge,COMPOSITION_INPUT_INVALID,i,"typed role row");
        for(var r:v.specializations().values())for(Object row:r.rows())require(row instanceof SpecializationEdge,COMPOSITION_INPUT_INVALID,i,"typed specialization row");
        for(var r:v.offerings().values())for(Object row:r.rows())require(row instanceof OfferingEdge,COMPOSITION_INPUT_INVALID,i,"typed offering row");
        for(var b:preliminary)require(v.salons().containsKey(b.salonId())&&v.hours().containsKey(b.salonId())&&v.offerings().containsKey(b.salonId())
                &&v.instructors().containsKey(b.instructorId())&&v.roles().containsKey(b.instructorId())&&v.specializations().containsKey(b.instructorId())
                &&v.activities().containsKey(b.activityId()),INPUT_EVIDENCE_INCOMPLETE,i,"complete preliminary final-dimension coverage");
        v.salons().forEach((id,p)->presence(i,id,p,"SALON"));
        v.instructors().forEach((id,p)->presence(i,id,p,"INSTRUCTOR"));
        v.activities().forEach((id,p)->presence(i,id,p,"ACTIVITY"));
        var exceptionIds=new HashSet<UUID>();var weeklyIds=new HashSet<UUID>();
        v.hours().forEach((id,h)->{
            require(h!=null&&h.exceptions()!=null&&h.weekly()!=null,INPUT_EVIDENCE_INCOMPLETE,i,"hours complete relations");
            var exceptions=h.exceptions().rows();var weekly=h.weekly().rows();
            var keys=new ArrayList<String>();
            for(var r:exceptions) {
                require(r.id()!=null&&id.equals(r.salonId())&&i.date().equals(r.date())&&Boolean.TRUE.equals(r.activo())&&r.cerrado()!=null,
                        COMPOSITION_INPUT_INVALID,i,"active exact-date exception shape");
                require(r.cerrado()?(r.opening()==null&&r.closing()==null):(r.opening()!=null&&r.closing()!=null),COMPOSITION_INPUT_INVALID,i,"exception closed/open form");
                require(exceptionIds.add(r.id()),AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"globally unique physical date exception");
                if(!r.cerrado())interval(i,r.opening(),r.closing());keys.add(r.id().toString());
            }
            metadata(i,h.exceptions().metadata(),"DATE_EXCEPTIONS",id,keys);keys.clear();
            for(var r:weekly) {
                require(r.id()!=null&&id.equals(r.salonId())&&r.day()!=null&&r.day()==(short)(i.date().getDayOfWeek().getValue()%7)
                        &&applicable(r.validFrom(),r.validUntil(),i.date()),COMPOSITION_INPUT_INVALID,i,"applicable weekly hours shape");
                require(weeklyIds.add(r.id()),AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"globally unique physical weekly hours");
                interval(i,r.opening(),r.closing());keys.add(r.id().toString());
            }
            metadata(i,h.weekly().metadata(),"WEEKLY_HOURS",id,keys);
            require(exceptions.size()<=1&&weekly.size()<=1,AMBIGUOUS_OR_CONTRADICTORY_INPUT,i,"unique applicable exception/weekly version");
        });
        v.roles().forEach((id,r)->{
            var keys=new ArrayList<String>();
            for(var row:r.rows()) {
                require(id.equals(row.usuarioId())&&row.rolId()!=null&&row.roleName()!=null&&!row.roleName().isBlank(),COMPOSITION_INPUT_INVALID,i,"role scalar shape and queried instructor");
                keys.add(roleKey(row));
            }
            metadata(i,r.metadata(),"ROLES",id,keys);
        });
        v.specializations().forEach((id,r)->{
            var keys=new ArrayList<String>();
            for(var row:r.rows()) {
                require(id.equals(row.usuarioId())&&row.activityId()!=null,COMPOSITION_INPUT_INVALID,i,"specialization edge shape");keys.add(specializationKey(row));
            }
            metadata(i,r.metadata(),"SPECIALIZATIONS",id,keys);
        });
        v.offerings().forEach((id,r)->{
            var keys=new ArrayList<String>();
            for(var row:r.rows()) {
                require(id.equals(row.salonId())&&row.activityId()!=null,COMPOSITION_INPUT_INVALID,i,"offering edge shape");keys.add(offeringKey(row));
            }
            metadata(i,r.metadata(),"OFFERINGS",id,keys);
        });
    }
    private static EffectiveCompositionOmission.Cause firstCause(EffectiveCompositionInput i,EffectiveCompositionBacking b) {
        var v=i.validityEvidence();var salon=v.salons().get(b.salonId());
        if(salon.kind()==PresenceKind.ABSENT||!salon.value().activo())return EffectiveCompositionOmission.Cause.SALON_INEXISTENTE_O_INACTIVO;
        var h=v.hours().get(b.salonId());LocalTime open=null,close=null;
        if(!h.exceptions().rows().isEmpty()) {
            var e=h.exceptions().rows().getFirst();if(!e.cerrado()) {open=e.opening();close=e.closing();}
        } else if(!h.weekly().rows().isEmpty()) {var w=h.weekly().rows().getFirst();open=w.opening();close=w.closing();}
        if(open==null)return EffectiveCompositionOmission.Cause.SALON_NO_OPERATIVO_EN_FECHA;
        if(b.start().isBefore(open)||b.end().isAfter(close))return EffectiveCompositionOmission.Cause.AJUSTE_FUERA_DE_HORARIO_EFECTIVO;
        var instructor=v.instructors().get(b.instructorId());
        if(instructor.kind()==PresenceKind.ABSENT||!instructor.value().estatus().equals("activo"))return EffectiveCompositionOmission.Cause.INSTRUCTOR_INEXISTENTE_O_INACTIVO;
        if(v.roles().get(b.instructorId()).rows().stream().noneMatch(r->r.roleName().equals("INSTRUCTOR")&&(r.nullableSalonId()==null||r.nullableSalonId().equals(b.salonId()))))return EffectiveCompositionOmission.Cause.ROL_INSTRUCTOR_AUSENTE;
        var activity=v.activities().get(b.activityId());
        if(activity.kind()==PresenceKind.ABSENT||!activity.value().activo())return EffectiveCompositionOmission.Cause.ACTIVIDAD_INEXISTENTE_O_INACTIVA;
        if(v.specializations().get(b.instructorId()).rows().stream().noneMatch(r->r.activityId().equals(b.activityId())))return EffectiveCompositionOmission.Cause.ESPECIALIDAD_AUSENTE;
        if(v.offerings().get(b.salonId()).rows().stream().noneMatch(r->r.activityId().equals(b.activityId())))return EffectiveCompositionOmission.Cause.ACTIVIDAD_NO_OFRECIDA_POR_SALON;
        return null;
    }
    private static void conflicts(EffectiveCompositionInput i,List<ProgrammingCandidateSnapshot> candidates) {
        for(int a=0;a<candidates.size();a++)for(int b=a+1;b<candidates.size();b++) {
            var x=candidates.get(a);var y=candidates.get(b);
            require(!x.reference().equals(y.reference()),READ_SET_INVARIANT_VIOLATION,i,"unique preliminary reference");
            require(!(x.salonId().equals(y.salonId())&&x.instructorId().equals(y.instructorId())&&x.activityId().equals(y.activityId())
                    &&x.start().equals(y.start())&&x.end().equals(y.end())),EFFECTIVE_SET_CONFLICT,i,"duplicate operative key");
            require(!(x.instructorId().equals(y.instructorId())&&x.start().isBefore(y.end())&&y.start().isBefore(x.end())),EFFECTIVE_SET_CONFLICT,i,"positive instructor overlap");
        }
    }
    private static String schema(EffectiveCompositionInput i,Session session) {
        var schemas=new LinkedHashMap<String,String>();i.envelope().participants().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->schemas.put(e.getKey(),e.getValue().schemaFingerprint()));
        return session.hash("F2E-R5-SCHEMA-BUNDLE-V1",schemas);
    }
    private static ProgrammingCandidateSnapshot candidate(EffectiveCompositionInput i,EffectiveCompositionBacking b,String inputCommitment,String schema,String snapshot,Session session) {
        String fingerprint=session.hash("F2E-R5-CANDIDATE-V1",b.reference(),b.origin(),b.salonId(),b.instructorId(),b.activityId(),b.start(),b.end(),RULE);
        var obs=observables(b.salonId(),b.instructorId(),b.activityId(),b.start(),b.end(),i.date());var fields=new LinkedHashMap<>(obs);
        fields.put("referenceType",b.reference().tipo().name());fields.put("referenceId",b.reference().id().toString());fields.put("origin",b.origin().name());
        fields.put("rule",RULE);fields.put("date",i.date().toString());fields.put("zone",i.businessZoneId());fields.put("inputCommitment",inputCommitment);
        fields.put("backingCommitment",session.hash("F2E-R5-BACKING-V1",b));fields.put("supportCommitment",session.hash("F2E-R5-SUPPORT-V1",b.support()));
        var ids=new ArrayList<String>();
        if(b.nominalBacking()!=null) {ids.add("NOMINAL_ASSIGNMENT/"+b.nominalBacking().assignmentId());ids.add("NOMINAL_BLOCK/"+b.nominalBacking().blockId());}
        if(b.adjustmentBacking()!=null)ids.add("ADJUSTMENT/"+b.adjustmentBacking().id());
        addSupportIds(b.support(),ids);ids.sort(EffectiveProgrammingComposer::recordIdOrder);
        var provenance=new EvidenceProvenance("R5_PURE_EFFECTIVE_GRAPH",schema,ids,"F2E_R5_PURE_COMPOSITION","V1",i.businessZoneId()+"/"+i.date(),fields);
        return new ProgrammingCandidateSnapshot(b.reference(),DetectorVocabulary.CandidateType.valueOf(b.origin().name()),snapshot,fingerprint,b.salonId(),b.instructorId(),b.activityId(),b.start(),b.end(),obs,provenance);
    }
    private static int recordIdOrder(String a,String b) {
        int left=a.indexOf('/'),right=b.indexOf('/');String ap=a.substring(0,left),bp=b.substring(0,right);
        int c=Arrays.compareUnsigned(text(ap),text(bp));
        return c!=0?c:EffectiveValidityEvidence.compareRecordKey(ap+"/",a.substring(left+1),b.substring(right+1));
    }
    private static void addSupportIds(EffectiveValidityEvidence v,List<String> ids) {
        v.salons().values().forEach(p->p.metadata().recordKeys().forEach(k->ids.add("SALON/"+k)));
        v.instructors().values().forEach(p->p.metadata().recordKeys().forEach(k->ids.add("INSTRUCTOR/"+k)));
        v.activities().values().forEach(p->p.metadata().recordKeys().forEach(k->ids.add("ACTIVITY/"+k)));
        v.hours().values().forEach(h->{h.exceptions().metadata().recordKeys().forEach(k->ids.add("DATE_EXCEPTIONS/"+k));h.weekly().metadata().recordKeys().forEach(k->ids.add("WEEKLY_HOURS/"+k));});
        v.roles().values().forEach(r->r.metadata().recordKeys().forEach(k->ids.add("ROLES/"+k)));
        v.specializations().values().forEach(r->r.metadata().recordKeys().forEach(k->ids.add("SPECIALIZATIONS/"+k)));
        v.offerings().values().forEach(r->r.metadata().recordKeys().forEach(k->ids.add("OFFERINGS/"+k)));
    }
    private static ProgrammingCandidateSnapshot withSnapshot(ProgrammingCandidateSnapshot c,String snapshot) {
        return new ProgrammingCandidateSnapshot(c.reference(),c.candidateType(),snapshot,c.candidateFingerprint(),c.salonId(),c.instructorId(),c.activityId(),c.start(),c.end(),c.observableFields(),c.provenance());
    }
    private static String content(EffectiveCompositionInput i,List<ProgrammingCandidateSnapshot> candidates,Map<ReferenciaOcurrencia,EffectiveCompositionBacking> backing,
            List<EffectiveCompositionOmission> omissions,List<EffectiveCompositionSuppression> suppressions,Session s) {
        var values=candidates.stream().map(c->candidateContent(c)).toList();
        return s.hash("F2E-R5-CONTENT-V1",i.date(),i.businessZoneId(),RULE,values,backing,omissions,suppressions);
    }
    private static CandidateContent candidateContent(ProgrammingCandidateSnapshot c) {
        return new CandidateContent(c.reference(),c.candidateType(),c.candidateFingerprint(),c.salonId(),c.instructorId(),
                c.activityId(),c.start(),c.end(),c.observableFields(),c.provenance());
    }
    /** Re-derives the complete outcome partition and hashes for issuance; never invokes compose recursively. */
    static void verifyResult(EffectiveProgrammingCompositionResult r) {
        var i=r.input();admit(i);var s=new Session();String commitment=s.hash("F2E-R5-INPUT-V1",i),schema=schema(i,s);
        var expected=preliminary(i,schema);validateEvidence(i,expected.preliminary());
        var backing=new LinkedHashMap<ReferenciaOcurrencia,EffectiveCompositionBacking>();var omissions=new ArrayList<EffectiveCompositionOmission>();
        var candidates=new ArrayList<ProgrammingCandidateSnapshot>();
        for(var b:expected.preliminary()) {
            var cause=firstCause(i,b);
            if(cause!=null)omissions.add(new EffectiveCompositionOmission(b.reference(),b,cause,b.support(),RULE,schema,i.envelope()));
            else {backing.put(b.reference(),b);candidates.add(candidate(i,b,commitment,schema,"PENDING_HASH",s));}
        }
        conflicts(i,candidates);
        String content=content(i,candidates,backing,omissions,expected.suppressions(),s);
        String snapshot=s.hash("F2E-R5-SNAPSHOT-V1",commitment,content,i.envelope());
        candidates.replaceAll(c->withSnapshot(c,s.hash("F2E-R5-CANDIDATE-SNAPSHOT-V1",snapshot,c.reference(),c.candidateFingerprint())));
        require(r.candidates().equals(candidates)&&r.backingByReference().equals(backing)
                &&new ArrayList<>(r.backingByReference().keySet()).equals(new ArrayList<>(backing.keySet()))
                &&r.omissions().equals(omissions)&&r.suppressions().equals(expected.suppressions())
                &&r.inputCommitment().equals(commitment)&&r.resultContentFingerprint().equals(content)&&r.resultSnapshotIdentity().equals(snapshot),
                READ_SET_INVARIANT_VIOLATION,i,"exact E/O/S partition, backing, candidate/provenance and commitment issuance contract");
        require(candidates.size()+omissions.size()+expected.suppressions().size()==i.nominalReadSet().backing().size()
                +i.adjustmentReadSet().backing().stream().filter(b->b.tipo().equals("ADICION")).count(),READ_SET_INVARIANT_VIOLATION,i,"exhaustive N union D accounting");
    }
}
