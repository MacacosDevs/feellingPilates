package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.time.*;
import com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
class EffectiveCompositionAdmissionTest {
    static void rejects(EffectiveCompositionInput i,EffectiveCompositionFailure.Category category) {
        var f=assertThrows(EffectiveCompositionFailure.class,()->new EffectiveProgrammingComposer().compose(i));assertEquals(category,f.category());assertSame(i,f.evidence());
    }
    @Test void allInputAndParticipantRequiredScalarsRejectMalformedValues() {
        assertThrows(EffectiveCompositionFailure.class,()->new EffectiveProgrammingComposer().compose(null));var i=input(1,new int[]{0},0);
        for(var f:EffectiveCompositionInput.class.getRecordComponents())assertThrows(RuntimeException.class,()->copy(i,f.getName(),null));
        rejects(copy(i,"ruleVersion","UNKNOWN"),EffectiveCompositionFailure.Category.COMPOSITION_INPUT_INVALID);
        rejects(copy(i,"businessZoneId","invalid/zone"),EffectiveCompositionFailure.Category.COMPOSITION_INPUT_INVALID);
        var p=i.envelope().participants().get("R3");
        for(var f:p.getClass().getRecordComponents()) {
            var broken=copy(p,f.getName(),null);var map=new HashMap<>(i.envelope().participants());map.put("R3",broken);
            rejects(copy(i,"envelope",copy(i.envelope(),"participants",map)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        }
        for(String f:List.of("invocationIdentity","authorityVersion","completionReceiptIdentity"))rejects(copy(i,"envelope",copy(i.envelope(),f,"")),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        rejects(copy(i,"envelope",copy(i.envelope(),"date",DATE.plusDays(1))),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        rejects(copy(i,"envelope",copy(i.envelope(),"businessZoneId","UTC")),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        var map=new HashMap<>(i.envelope().participants());map.remove("VALIDITY");rejects(copy(i,"envelope",copy(i.envelope(),"participants",map)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
    }
    @Test void originalHashesMapsProvenanceOrderAndPhysicalBackingCannotBeRepaired() {
        var i=input(2,new int[]{2,2},0);var list=new ArrayList<>(i.nominalReadSet().candidates());
        for(String field:List.of("candidateFingerprint","snapshotIdentity")) {
            list=new ArrayList<>(i.nominalReadSet().candidates());list.set(0,copy(list.getFirst(),field,"b".repeat(64)));
            rejects(copy(i,"nominalReadSet",new NominalProgrammingReadSet(list,i.nominalReadSet().backing())),EffectiveCompositionFailure.Category.READ_SET_INVARIANT_VIOLATION);
        }
        var c=i.nominalReadSet().candidates().getFirst();var fields=new HashMap<>(c.provenance().normalizedFields());fields.put("blockSeriesId",id(999).toString());
        list=new ArrayList<>(i.nominalReadSet().candidates());list.set(0,copy(c,"provenance",copy(c.provenance(),"normalizedFields",fields)));
        rejects(copy(i,"nominalReadSet",new NominalProgrammingReadSet(list,i.nominalReadSet().backing())),EffectiveCompositionFailure.Category.READ_SET_INVARIANT_VIOLATION);
        var reversedC=new ArrayList<>(i.nominalReadSet().candidates());var reversedB=new ArrayList<>(i.nominalReadSet().backing());Collections.reverse(reversedC);Collections.reverse(reversedB);
        rejects(copy(i,"nominalReadSet",new NominalProgrammingReadSet(reversedC,reversedB)),EffectiveCompositionFailure.Category.READ_SET_INVARIANT_VIOLATION);
        var sources=new ArrayList<>(i.adjustmentReadSet().sources());sources.set(0,copy(sources.getFirst(),"snapshotIdentity","b".repeat(64)));
        rejects(copy(i,"adjustmentReadSet",new AdjustmentReadSet(sources,i.adjustmentReadSet().backing())),EffectiveCompositionFailure.Category.READ_SET_INVARIANT_VIOLATION);
        var b=nominal(0);var n=nominal(1);
        rejects(input(List.of(b,copy(n,"assignmentId",b.assignmentId())),List.of()),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        var shared=copy(copy(n,"blockId",b.blockId()),"assignmentBlockId",b.blockId());
        rejects(input(List.of(b,shared),List.of()),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        assertThrows(RuntimeException.class,()->input(List.of(b,b),List.of()));
    }
    @Test void allAdjustmentFormsActiveDateAndIntervalsAreFailClosed() {
        var b=adjustment(0,2);
        for(var mutation:List.of(Map.entry("activo",false),Map.entry("fecha",DATE.plusDays(1)),Map.entry("horaFinResultado",time(9)),Map.entry("horaInicioResultado",time(10)),
                Map.entry("horaInicioResultado",time(9).plusNanos(1)),Map.entry("creadoEn",OffsetDateTime.MAX)))
            assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(input(List.of(nominal(0)),List.of(copy(b,mutation.getKey(),mutation.getValue())))));
        for(String field:List.of("salonResultadoId","instructorResultadoId","tipoActividadResultadoId","horaInicioResultado","horaFinResultado","asignacionSerieId","creadoEn","actualizadoEn"))
            assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(input(List.of(nominal(0)),List.of(copy(b,field,null)))));
        for(int kind:List.of(1,2,3)) {
            var a=adjustment(0,kind);var duplicate=copy(a,"id",id(401));
            rejects(input(List.of(nominal(0)),List.of(a,duplicate)),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        }
        rejects(input(List.of(nominal(0)),List.of(adjustment(0,1),copy(adjustment(0,2),"id",id(401)))),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        var add=copy(adjustment(0,4),"asignacionSerieId",id(100));assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(input(List.of(nominal(0)),List.of(add))));
        assertThrows(RuntimeException.class,()->input(List.of(nominal(0)),List.of(b,b)));
    }
    @Test void allClosedNominalApplicabilityPrecisionAndContainmentRulesRepeat() {
        var b=nominal(0);
        for(var mutation:List.of(Map.entry("assignmentActive",false),Map.entry("blockActive",false),Map.entry("assignmentFrom",DATE.plusDays(1)),
                Map.entry("blockUntil",DATE.minusDays(2)),Map.entry("assignmentBlockId",id(999)),Map.entry("assignmentStart",time(7)),
                Map.entry("assignmentEnd",time(19)),Map.entry("assignmentStart",time(9).plusNanos(1))))
            assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(input(List.of(copy(b,mutation.getKey(),mutation.getValue())),List.of())));
    }
    @Test void emptyFactsStillNeedCompleteEnvelopeAndDeclaredRealMetadataIsNotAuthenticity() {
        var i=input(0,new int[0],0);assertTrue(new EffectiveProgrammingComposer().compose(i).candidates().isEmpty());
        var m=new HashMap<>(i.envelope().participants());m.put("R3",copy(m.get("R3"),"completion",EffectiveCompositionEnvelope.Completion.SUCCESSFUL));
        rejects(copy(i,"envelope",copy(i.envelope(),"participants",m)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        rejects(copy(i,"envelope",copy(i.envelope(),"mode",EffectiveCompositionEnvelope.Mode.R6_SUPPLIED_COHERENT_READ_FACTS)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        for(String field:List.of("readScope","schemaFingerprint","executionIdentity","snapshotEvidenceId","statementCaptureCommitment","sourceName","ruleVersion")) {
            m=new HashMap<>(i.envelope().participants());m.put("R3",copy(m.get("R3"),field,"FORGED"));
            rejects(copy(i,"envelope",copy(i.envelope(),"participants",m)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        }
    }    @Test void declaredRealAdmissionChecksBindingsCompletionAndMixedModeWithoutPhysicalProof() {
        var synthetic=input(0,new int[0],0);var map=new LinkedHashMap<String,EffectiveCompositionEnvelope.Participant>();
        for(var entry:synthetic.envelope().participants().entrySet()) {
            var p=entry.getValue();String key=entry.getKey();
            for(var update:List.of(Map.entry("sourceName","DECLARED_"+key),Map.entry("physicalResourceIdentity","DECLARED_SHARED_RESOURCE"),
                    Map.entry("transactionBoundaryIdentity","DECLARED_SHARED_TRANSACTION"),Map.entry("statementCaptureIdentity",HASH),Map.entry("runIdentity","DECLARED_RUN"),
                    Map.entry("attemptIdentity","DECLARED_ATTEMPT"),Map.entry("ruleCatalogVersion","DECLARED_CATALOG"),Map.entry("databaseName","DECLARED_DB"),
                    Map.entry("schemaName","DECLARED_SCHEMA"),Map.entry("principal","DECLARED_PRINCIPAL"),Map.entry("readerInvocationIdentity","DECLARED_"+key+"_INVOCATION")))p=copy(p,update.getKey(),update.getValue());
            if(key.equals("VALIDITY"))p=copy(p,"projectionCatalogVersion","DECLARED_VALIDITY_V1");
            p=copy(p,"completion",EffectiveCompositionEnvelope.Completion.SUCCESSFUL);p=copy(p,"executionIdentity",execution(p,key));map.put(key,p);
        }
        var e=new EffectiveCompositionEnvelope(EffectiveCompositionEnvelope.Mode.R6_SUPPLIED_COHERENT_READ_FACTS,DATE,ZONE,"DECLARED_COMPOSITION","DECLARED_AUTHORITY","DECLARED_COMPLETION",map);
        var i=copy(synthetic,"envelope",e);var r=new EffectiveProgrammingComposer().compose(i);
        assertEquals(EffectiveCompositionEnvelope.Mode.R6_SUPPLIED_COHERENT_READ_FACTS,r.evidenceMode());
        // This proves comparisons of declared scalars only: no physical acquisition/authentication was performed.
        for(String field:List.of("physicalResourceIdentity","transactionBoundaryIdentity","snapshotIdentity","statementCaptureIdentity")) {
            var broken=new LinkedHashMap<>(map);broken.put("VALIDITY",copy(broken.get("VALIDITY"),field,"CONTRADICTORY"));
            rejects(copy(i,"envelope",copy(e,"participants",broken)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        }
        var mixed=new LinkedHashMap<>(map);mixed.put("R4",copy(mixed.get("R4"),"completion",EffectiveCompositionEnvelope.Completion.SYNTHETIC));
        rejects(copy(i,"envelope",copy(e,"participants",mixed)),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
        rejects(copy(i,"envelope",copy(e,"completionReceiptIdentity","NOT_APPLICABLE_SYNTHETIC")),EffectiveCompositionFailure.Category.INPUT_ENVELOPE_INVALID);
    }
    @Test void sharedPhysicalBlockIsAllowedOnlyWithCompleteIdenticalPayload() {
        var first=nominal(0);var second=nominal(1);
        second=copy(copy(copy(second,"blockId",first.blockId()),"assignmentBlockId",first.blockId()),"blockSeriesId",first.blockSeriesId());
        var i=input(List.of(first,second),List.of());var r=new EffectiveProgrammingComposer().compose(i);assertEquals(2,r.candidates().size());
        for(var b:r.backingByReference().values())assertEquals(first.blockId(),b.nominalBacking().blockId());
    }

}
