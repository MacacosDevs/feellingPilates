package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.time.*;
import java.lang.reflect.*;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.read.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveValidityEvidence.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
class EffectiveCompositionPropertyTest {
    @Test void boundedExhaustiveIndependentAlgebraOracle() {
        long seed=0xF2E5L;var random=new Random(seed);int cases=0;
        for(int n=0;n<=3;n++)for(int mask=0;mask<(1<<(2*n));mask++)for(int adds=0;adds<=2;adds++)for(int cause=0;cause<=8;cause++) {
            int[] kinds=new int[n];int cancellations=0;
            for(int k=0;k<n;k++){kinds[k]=(mask>>(2*k))&3;if(kinds[k]==1)cancellations++;}
            var i=withCause(input(n,kinds,adds),cause);var r=new EffectiveProgrammingComposer().compose(i);int preliminary=n-cancellations+adds;
            assertEquals(cancellations,r.suppressions().size());assertEquals(cause==0?preliminary:0,r.candidates().size());assertEquals(cause==0?0:preliminary,r.omissions().size());
            assertEquals(n+adds,r.candidates().size()+r.omissions().size()+r.suppressions().size());
            var union=new HashSet<ReferenciaOcurrencia>();
            r.candidates().forEach(c->assertTrue(union.add(c.reference())));r.omissions().forEach(o->assertTrue(union.add(o.reference())));r.suppressions().forEach(s->assertTrue(union.add(s.reference())));
            var expected=new HashSet<ReferenciaOcurrencia>();for(int k=0;k<n;k++)expected.add(nominal(k).reference());
            for(int k=0;k<adds;k++)expected.add(new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE,id(400+n+k),DATE));assertEquals(expected,union);
            for(var c:r.candidates()) {
                int index=(int)c.reference().id().getLeastSignificantBits();
                String origin=c.reference().tipo()==ReferenciaOcurrencia.Tipo.AJUSTE?"ADDITION_OCCURRENCE":kinds[index-100]==0?"RECURRENT_OCCURRENCE":"REPLACEMENT_OCCURRENCE";
                assertEquals(origin,c.candidateType().name());assertEquals(c.reference(),r.backingByReference().get(c.reference()).reference());
                assertEquals(hash("F2E-R5-CANDIDATE-V1",c.reference(),EffectiveCompositionBacking.Origin.valueOf(origin),c.salonId(),c.instructorId(),c.activityId(),c.start(),c.end(),RULE),c.candidateFingerprint());
            }
            int expectedCause=causeIndex(cause);if(cause!=0)r.omissions().forEach(o->assertEquals(EffectiveCompositionOmission.Cause.values()[expectedCause],o.cause()));
            var shuffled=new EffectiveProgrammingComposer().compose(permute(i,random));assertEquals(r.inputCommitment(),shuffled.inputCommitment());assertEquals(r.resultSnapshotIdentity(),shuffled.resultSnapshotIdentity());assertEquals(r.candidates(),shuffled.candidates());
            cases++;
        }
        assertEquals(2295,cases);System.out.println("R5_PROPERTY seed="+seed+" boundedCases="+cases);
    }
    private static int causeIndex(int cause) {return cause-1;}
    @Test void retainedFieldPerturbationsChangeInputButExecutionDoesNotChangeSemanticCandidateHash() {
        var i=input(1,new int[]{2},0);var r=new EffectiveProgrammingComposer().compose(i);
        var e=copy(i.envelope(),"invocationIdentity","SYNTHETIC_SECOND_ATTEMPT");var other=copy(i,"envelope",e);
        var t=new EffectiveProgrammingComposer().compose(other);assertEquals(r.candidates().getFirst().candidateFingerprint(),t.candidates().getFirst().candidateFingerprint());
        assertNotEquals(r.inputCommitment(),t.inputCommitment());assertNotEquals(r.resultSnapshotIdentity(),t.resultSnapshotIdentity());
        var nominalChanged=copy(nominal(0),"assignmentUpdated",nominal(0).assignmentUpdated().plusNanos(1000));
        var u=new EffectiveProgrammingComposer().compose(input(List.of(nominalChanged),List.of(adjustment(0,2))));
        assertEquals(r.candidates().getFirst().candidateFingerprint(),u.candidates().getFirst().candidateFingerprint());assertNotEquals(r.inputCommitment(),u.inputCommitment());
        assertNotEquals(new EffectiveProgrammingComposer().compose(input(1,new int[]{0},0)).candidates().getFirst().candidateFingerprint(),r.candidates().getFirst().candidateFingerprint());
    }
    @Test void everyRetainedRecordFieldIsInCanonicalPreimage() throws Exception {
        var i=input(3,new int[]{1,3,0},1);
        var salons=new LinkedHashMap<>(i.validityEvidence().salons());
        salons.put(id(2),copy(salons.get(id(2)),"value",new Salon(id(2),false)));
        i=copy(i,"validityEvidence",copy(i.validityEvidence(),"salons",salons));
        var r=new EffectiveProgrammingComposer().compose(i);
        assertEquals(2,r.candidates().size());assertEquals(1,r.omissions().size());assertEquals(1,r.suppressions().size());
        var schemas=new TreeMap<String,Object>();collect(i,schemas);collect(r.candidates(),schemas);collect(r.backingByReference(),schemas);collect(r.omissions(),schemas);collect(r.suppressions(),schemas);
        var c=r.candidates().getFirst();
        collect(new EffectiveCompositionCanonicalizer.CandidateContent(c.reference(),c.candidateType(),c.candidateFingerprint(),c.salonId(),c.instructorId(),c.activityId(),c.start(),c.end(),c.observableFields(),c.provenance()),schemas);
        // Explicit optional-state and typed relation alternatives, including exception support.
        var ex=new DateException(id(900),id(1),DATE,false,time(8),time(18),true);
        collect(new Relation<>(meta(i.envelope(),"DATE_EXCEPTIONS",id(1),List.of(ex.id().toString())),List.of(ex)),schemas);
        collect(copy(ex,"opening",null),schemas);collect(copy(ex,"cerrado",true),schemas);
        for(var presence:List.of(i.validityEvidence().salons().values().iterator().next(),i.validityEvidence().instructors().values().iterator().next(),i.validityEvidence().activities().values().iterator().next()))
            collect(new Presence<>(PresenceKind.ABSENT,presence.queriedId(),null,presence.metadata()),schemas);
        Set<String> expected=Set.of("EffectiveCompositionInput","EffectiveCompositionEnvelope","Participant","NominalProgrammingReadSet","NominalBackingSnapshot","ProgrammingCandidateSnapshot","CandidateContent","ReferenciaOcurrencia","EvidenceProvenance","AdjustmentReadSet","AdjustmentBackingSnapshot","GenericSourceSnapshot","EffectiveValidityEvidence","RelationMetadata","Presence","Relation","Hours","Salon","Instructor","Activity","DateException","WeeklyHours","RoleEdge","SpecializationEdge","OfferingEdge","EffectiveCompositionBacking","EffectiveCompositionOmission","EffectiveCompositionSuppression");
        assertEquals(expected,schemas.values().stream().map(x->x.getClass().getSimpleName()).collect(java.util.stream.Collectors.toSet()));
        int fields=0,enclosingInputs=0,constructorRejections=0;
        for(var entry:schemas.entrySet()) {
            Object original=entry.getValue();
            for(var field:original.getClass().getRecordComponents()) {
                Object changed=mutateRecord(original,field.getName());
                assertFalse(Objects.equals(field.getAccessor().invoke(original),field.getAccessor().invoke(changed)),entry.getKey()+"."+field.getName());
                sensitivity(original,changed,entry.getKey()+"."+field.getName());fields++;
                try {
                    var enclosing=(EffectiveCompositionInput)replaceRetained(i,original,changed);
                    if(enclosing!=i) {
                        sensitivity(i,enclosing,"full input/"+entry.getKey()+"."+field.getName());enclosingInputs++;
                        try {
                            var admitted=new EffectiveProgrammingComposer().compose(enclosing);
                            assertNotEquals(r.inputCommitment(),admitted.inputCommitment(),"admitted retained input perturbation");
                            assertNotEquals(r.resultSnapshotIdentity(),admitted.resultSnapshotIdentity(),"admitted result identity");
                        } catch(EffectiveCompositionFailure rejected) {assertSame(enclosing,rejected.evidence());}
                    }
                } catch(AssertionError rejected) {
                    assertTrue(rejected.getCause() instanceof NominalReadFailure||rejected.getCause() instanceof AdjustmentReadFailure,"only closed constructor rejection may stop reconstruction: "+entry.getKey()+"."+field.getName());constructorRejections++;
                }
            }
        }
        // Result is deliberately not a record. Exercise all13 published schema fields,
        // including five derived fields through their backing input. Reflection is test-only;
        // package issuance must still reject every forged inconsistent output.
        for(String field:List.of("input","candidates","backingByReference","omissions","suppressions","inputCommitment","resultContentFingerprint","resultSnapshotIdentity","date","businessZoneId","ruleVersion","envelope","evidenceMode")) {
            var changed=mutateResult(r,field);sensitivity(r,changed,"EffectiveProgrammingCompositionResult."+field);
            assertThrows(EffectiveCompositionFailure.class,()->EffectiveProgrammingCompositionResult.issue(changed.input(),changed.candidates(),changed.backingByReference(),changed.omissions(),changed.suppressions(),changed.inputCommitment(),changed.resultContentFingerprint(),changed.resultSnapshotIdentity()));fields++;
        }
        assertTrue(fields>250);assertTrue(enclosingInputs>150);System.out.println("R5_RETAINED_FIELDS productionPreimageControls="+fields+" schemaVariants="+schemas.size()+" resultFields=13 fullInputControls="+enclosingInputs+" closedConstructorRejections="+constructorRejections);
        // Admission controls accompany byte sensitivity: invalid stored evidence is never
        // repaired into success. A legal execution-only change is covered separately above.
        var original=input(1,new int[]{3},1);
        for(String field:List.of("date","businessZoneId","ruleVersion","nominalReadSet","adjustmentReadSet","validityEvidence")) {
            var changed=(EffectiveCompositionInput)mutateRecord(original,field);
            assertThrows(EffectiveCompositionFailure.class,()->new EffectiveProgrammingComposer().compose(changed),field);
        }
    }
    static void sensitivity(Object original,Object changed,String label) {
        byte[] before=oracle(original),after=oracle(changed);
        assertArrayEquals(before,EffectiveCompositionCanonicalizer.encode(original),label+" original bytes");
        assertArrayEquals(after,EffectiveCompositionCanonicalizer.encode(changed),label+" changed bytes");
        assertFalse(Arrays.equals(before,after),label+" enclosing record preimage");
        var session=new EffectiveCompositionCanonicalizer.Session();String domain="F2E-R5-RETAINED-FIELD-CONTROL-V1";
        assertEquals(hash(domain,original),session.hash(domain,original),label+" original commitment");
        assertEquals(hash(domain,changed),session.hash(domain,changed),label+" changed commitment");
        assertNotEquals(session.hash(domain,original),session.hash(domain,changed),label+" commitment sensitivity");
    }
    static void collect(Object value,Map<String,Object> schemas) throws ReflectiveOperationException {
        if(value==null)return;
        if(value instanceof Map<?,?> m){for(var e:m.entrySet()){collect(e.getKey(),schemas);collect(e.getValue(),schemas);}return;}
        if(value instanceof List<?> xs){for(var x:xs)collect(x,schemas);return;}
        if(!value.getClass().isRecord())return;
        String key=value.getClass().getSimpleName();
        if(value instanceof Presence<?> p)key+="/"+p.kind()+"/"+p.metadata().scope().split("/")[0];
        if(value instanceof Relation<?> r)key+="/"+r.metadata().scope().split("/")[0];
        if(value instanceof RelationMetadata m)key+="/"+m.scope().split("/")[0];
        if(value instanceof EffectiveCompositionBacking b)key+="/"+b.origin();
        if(value instanceof AdjustmentBackingSnapshot b)key+="/"+b.tipo();
        if(value instanceof DateException e)key+="/"+e.cerrado()+"/"+(e.opening()==null?"ABSENT":"PRESENT");
        schemas.putIfAbsent(key,value);
        for(var field:value.getClass().getRecordComponents())collect(field.getAccessor().invoke(value),schemas);
    }
    static Object replaceRetained(Object root,Object target,Object changed) throws ReflectiveOperationException {
        if(root==target)return changed;
        if(root==null)return null;
        if(root instanceof List<?> list) {
            var out=new ArrayList<Object>();boolean different=false;
            for(var x:list){var y=replaceRetained(x,target,changed);out.add(y);different|=x!=y;}return different?out:root;
        }
        if(root instanceof Map<?,?> map) {
            var out=new LinkedHashMap<Object,Object>();boolean different=false;
            for(var e:map.entrySet()){var k=replaceRetained(e.getKey(),target,changed);var v=replaceRetained(e.getValue(),target,changed);out.put(k,v);different|=k!=e.getKey()||v!=e.getValue();}return different?out:root;
        }
        if(root.getClass().isRecord()) {
            var changes=new HashMap<String,Object>();for(var c:root.getClass().getRecordComponents()) {
                var x=c.getAccessor().invoke(root);var y=replaceRetained(x,target,changed);if(x!=y)changes.put(c.getName(),y);
            }
            return changes.isEmpty()?root:rebuild(root,changes);
        }
        return root;
    }
    static Object mutateRecord(Object record,String field) throws ReflectiveOperationException {
        var changes=new HashMap<String,Object>();
        var component=Arrays.stream(record.getClass().getRecordComponents()).filter(c->c.getName().equals(field)).findFirst().orElseThrow();
        changes.put(field,perturb(component.getAccessor().invoke(record),component.getType(),field,record));
        if(record instanceof NominalBackingSnapshot n) {
            if(Set.of("reference","assignmentSeriesId").contains(field)) {
                UUID id=new UUID(n.assignmentSeriesId().getMostSignificantBits(),n.assignmentSeriesId().getLeastSignificantBits()+10000);
                changes.put("reference",new ReferenciaOcurrencia(n.reference().tipo(),id,n.fecha()));changes.put("assignmentSeriesId",id);
            }
            if(Set.of("fecha","dayOfWeek","blockDay").contains(field)) {
                var date=n.fecha().plusDays(1);changes.put("fecha",date);changes.put("reference",new ReferenciaOcurrencia(n.reference().tipo(),n.reference().id(),date));
                changes.put("dayOfWeek",(short)(date.getDayOfWeek().getValue()%7));changes.put("blockDay",(short)(date.getDayOfWeek().getValue()%7));
            }
        }
        if(record instanceof NominalProgrammingReadSet n) {
            if(field.equals("candidates")){var xs=new ArrayList<>(n.candidates());xs.set(0,copy(xs.getFirst(),"snapshotIdentity","PERTURBED_SNAPSHOT"));changes.put(field,xs);}
            else {var xs=new ArrayList<>(n.backing());xs.set(0,copy(xs.getFirst(),"assignmentUpdated",xs.getFirst().assignmentUpdated().plusNanos(1000)));changes.put(field,xs);}
        }
        if(record instanceof AdjustmentReadSet a) {
            if(field.equals("sources")){var xs=new ArrayList<>(a.sources());xs.set(0,copy(xs.getFirst(),"snapshotIdentity","PERTURBED_SNAPSHOT"));changes.put(field,xs);}
            else {var xs=new ArrayList<>(a.backing());xs.set(0,copy(xs.getFirst(),"actualizadoEn",xs.getFirst().actualizadoEn().plusNanos(1000)));changes.put(field,xs);changes.put("sources",xs.stream().map(b->source(b,envelope())).toList());}
        }
        if(record instanceof GenericSourceSnapshot&&field.equals("sourceSystem")) {
            changes.put("sourceSystem",DetectorVocabulary.SourceSystem.LEGACY);changes.put("sourceAtomType",DetectorVocabulary.SourceAtomType.LEGACY_RECURRENTE);
        }
        if(record instanceof GenericSourceSnapshot g&&field.equals("sourceAtomType"))changes.put(field,g.sourceAtomType()==DetectorVocabulary.SourceAtomType.NEW_ADICION?DetectorVocabulary.SourceAtomType.NEW_REEMPLAZO:DetectorVocabulary.SourceAtomType.NEW_ADICION);
        return rebuild(record,changes);
    }
    static Object rebuild(Object record,Map<String,Object> changes) throws ReflectiveOperationException {
        var components=record.getClass().getRecordComponents();var values=new Object[components.length];var types=new Class<?>[components.length];
        for(int k=0;k<components.length;k++){types[k]=components[k].getType();values[k]=changes.containsKey(components[k].getName())?changes.get(components[k].getName()):components[k].getAccessor().invoke(record);}
        try{return record.getClass().getDeclaredConstructor(types).newInstance(values);}catch(InvocationTargetException e){throw new AssertionError(record.getClass()+" "+changes.keySet(),e.getCause());}
    }
    static Object perturb(Object value,Class<?> type,String field,Object owner) throws ReflectiveOperationException {
        if(value==null) {
            if(type==LocalDate.class)return DATE.plusDays(2);if(type==LocalTime.class)return time(8);if(type==UUID.class)return id(9999);
            if(type==ProgrammingCandidateSnapshot.class)return candidate(nominal(0),envelope());if(type==NominalBackingSnapshot.class)return nominal(0);
            if(type==GenericSourceSnapshot.class)return source(adjustment(0,3),envelope());if(type==AdjustmentBackingSnapshot.class)return adjustment(0,3);
            if(owner instanceof Presence<?>)return new Salon(id(9999),false);
            throw new AssertionError("missing typed null mutation "+owner.getClass()+"."+field);
        }
        if(value instanceof String s)return s+"_PERTURBED";
        if(value instanceof UUID u)return new UUID(u.getMostSignificantBits(),u.getLeastSignificantBits()+10000);
        if(value instanceof LocalDate d)return d.plusDays(1);if(value instanceof LocalTime t)return t.plusNanos(1000);
        if(value instanceof OffsetDateTime t)return t.plusNanos(1000);if(value instanceof Boolean b)return !b;if(value instanceof Short n)return (short)(n+1);
        if(value instanceof Enum<?> e){var constants=e.getDeclaringClass().getEnumConstants();return constants[(e.ordinal()+1)%constants.length];}
        if(value instanceof List<?> xs) {
            var out=new ArrayList<Object>(xs);if(!out.isEmpty())out.removeFirst();
            else if(owner instanceof Relation<?>)out.add(new DateException(id(900),id(1),DATE,false,time(8),time(18),true));
            else out.add(id(9999).toString());return out;
        }
        if(value instanceof Map<?,?> m){var out=new LinkedHashMap<>(m);assertFalse(out.isEmpty(),"typed map sample");out.remove(out.keySet().iterator().next());return out;}
        if(value.getClass().isRecord()) {
            String first=value instanceof NominalBackingSnapshot?"assignmentUpdated":value instanceof NominalProgrammingReadSet?"candidates":value instanceof AdjustmentReadSet?"sources":value.getClass().getRecordComponents()[0].getName();
            return mutateRecord(value,first);
        }
        throw new AssertionError("unsupported typed mutation "+value.getClass());
    }
    static EffectiveProgrammingCompositionResult mutateResult(EffectiveProgrammingCompositionResult r,String field) throws ReflectiveOperationException {
        var in=r.input();Object candidates=r.candidates(),backing=r.backingByReference(),omissions=r.omissions(),suppressions=r.suppressions();
        String commitment=r.inputCommitment(),content=r.resultContentFingerprint(),snapshot=r.resultSnapshotIdentity();
        switch(field) {
            case "input" -> in=copy(in,"envelope",copy(in.envelope(),"invocationIdentity","PERTURBED"));
            case "date","businessZoneId","ruleVersion","envelope" -> in=(EffectiveCompositionInput)mutateRecord(in,field);
            case "evidenceMode" -> in=copy(in,"envelope",copy(in.envelope(),"mode",EffectiveCompositionEnvelope.Mode.R6_SUPPLIED_COHERENT_READ_FACTS));
            case "candidates" -> candidates=List.of();case "backingByReference" -> backing=Map.of();case "omissions" -> omissions=List.of();case "suppressions" -> suppressions=List.of();
            case "inputCommitment" -> commitment="PERTURBED";case "resultContentFingerprint" -> content="PERTURBED";case "resultSnapshotIdentity" -> snapshot="PERTURBED";
            default -> throw new AssertionError(field);
        }
        var ctor=EffectiveProgrammingCompositionResult.class.getDeclaredConstructors()[0];ctor.setAccessible(true);
        return (EffectiveProgrammingCompositionResult)ctor.newInstance(in,candidates,backing,omissions,suppressions,commitment,content,snapshot);
    }
}
