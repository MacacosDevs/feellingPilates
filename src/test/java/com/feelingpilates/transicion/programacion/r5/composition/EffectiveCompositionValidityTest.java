package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveValidityEvidence.*;
class EffectiveCompositionValidityTest {
    @Test void eightFirstPredicatesAndAllCombinationsPreserveExactPriority() {
        var base=input(1,new int[]{3},0);
        for(int mask=1;mask<256;mask++) {
            var i=base;for(int cause=8;cause>=1;cause--)if((mask&(1<<(cause-1)))!=0)i=withCause(i,cause);
            var r=new EffectiveProgrammingComposer().compose(i);assertTrue(r.candidates().isEmpty());var o=r.omissions().getFirst();
            assertEquals(EffectiveCompositionOmission.Cause.values()[Integer.numberOfTrailingZeros(mask)],o.cause());
            assertEquals(id(2),o.backing().salonId());assertEquals(id(50),o.backing().instructorId());assertEquals(id(4),o.backing().activityId());
            assertEquals(nominal(0).reference(),o.reference());assertEquals(adjustment(0,3),o.backing().adjustmentBacking());assertSame(i.envelope(),o.envelope());
        }
    }
    @Test void absentIsProvenAndMissingAnyPreliminaryRelationFailsEvenBehindEarlyOmission() {
        var i=input(1,new int[]{0},0);var v=i.validityEvidence();
        var absent=new Presence<Salon>(PresenceKind.ABSENT,id(1),null,meta(i.envelope(),"SALON",id(1),List.of()));
        var r=new EffectiveProgrammingComposer().compose(copy(i,"validityEvidence",copy(v,"salons",Map.of(id(1),absent))));
        assertEquals(EffectiveCompositionOmission.Cause.SALON_INEXISTENTE_O_INACTIVO,r.omissions().getFirst().cause());
        var early=withCause(i,1);
        for(String map:List.of("salons","instructors","activities","hours","roles","specializations","offerings"))
            EffectiveCompositionAdmissionTest.rejects(copy(early,"validityEvidence",copy(early.validityEvidence(),map,Map.of())),EffectiveCompositionFailure.Category.INPUT_EVIDENCE_INCOMPLETE);
        var bad=copy(v.instructors().get(id(20)),"value",new Instructor(id(20),"UNKNOWN"));
        EffectiveCompositionAdmissionTest.rejects(copy(early,"validityEvidence",copy(early.validityEvidence(),"instructors",Map.of(id(20),bad))),EffectiveCompositionFailure.Category.COMPOSITION_INPUT_INVALID);
    }
    @Test void roleMustBeGlobalOrFinalSalonScoped() {
        var i=input(1,new int[]{3},0);var v=i.validityEvidence();
        for(UUID salon:Arrays.asList(null,id(2),id(1))) {
            var edge=new RoleEdge(id(50),id(6),"INSTRUCTOR",salon);var key=id(50)+"/"+id(6)+"/"+(salon==null?"ABSENT":salon);
            var roles=Map.of(id(50),new Relation<>(meta(i.envelope(),"ROLES",id(50),List.of(key)),List.of(edge)));
            var r=new EffectiveProgrammingComposer().compose(copy(i,"validityEvidence",copy(v,"roles",roles)));
            assertEquals(salon==null||salon.equals(id(2))?1:0,r.candidates().size());
            if(r.candidates().isEmpty())assertEquals(EffectiveCompositionOmission.Cause.ROL_INSTRUCTOR_AUSENTE,r.omissions().getFirst().cause());
        }
    }
    @Test void exceptionPrecedenceClosedOpenNoWeeklyAndHoursCardinality() {
        var i=input(1,new int[]{0},0);var original=i.validityEvidence().hours().get(id(1));
        for(boolean closed:List.of(false,true)) {
            var x=new DateException(id(900),id(1),DATE,closed,closed?null:time(9),closed?null:time(10),true);
            var h=new Hours(new Relation<>(meta(i.envelope(),"DATE_EXCEPTIONS",id(1),List.of(x.id().toString())),List.of(x)),
                    new Relation<>(meta(i.envelope(),"WEEKLY_HOURS",id(1),List.of()),List.of()));
            var r=new EffectiveProgrammingComposer().compose(withHours(i,h));assertEquals(closed?0:1,r.candidates().size());
            if(closed)assertEquals(EffectiveCompositionOmission.Cause.SALON_NO_OPERATIVO_EN_FECHA,r.omissions().getFirst().cause());
            var duplicate=copy(x,"id",id(901));var bad=copy(h,"exceptions",new Relation<>(meta(i.envelope(),"DATE_EXCEPTIONS",id(1),List.of(x.id().toString(),duplicate.id().toString())),List.of(x,duplicate)));
            EffectiveCompositionAdmissionTest.rejects(withHours(i,bad),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        }
        var w=original.weekly().rows().getFirst();var duplicate=copy(w,"id",id(1002));
        EffectiveCompositionAdmissionTest.rejects(withHours(withCause(i,1),copy(original,"weekly",new Relation<>(meta(i.envelope(),"WEEKLY_HOURS",id(1),List.of(w.id().toString(),duplicate.id().toString())),List.of(w,duplicate)))),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        for(var entry:List.of(Map.entry("day",(short)1),Map.entry("validFrom",DATE.plusDays(1)),Map.entry("validUntil",DATE.minusDays(1)),Map.entry("opening",time(19)),Map.entry("closing",time(8)),Map.entry("opening",time(8).plusNanos(1)))) {
            var bad=copy(original,"weekly",new Relation<>(original.weekly().metadata(),List.of(copy(w,entry.getKey(),entry.getValue()))));
            assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(withHours(i,bad)));
        }
        for(var until:Arrays.asList(null,DATE)) {
            var good=copy(original,"weekly",new Relation<>(original.weekly().metadata(),List.of(copy(w,"validUntil",until))));assertEquals(1,new EffectiveProgrammingComposer().compose(withHours(i,good)).candidates().size());
        }
    }
    private static EffectiveCompositionInput withHours(EffectiveCompositionInput i,Hours h) {return copy(i,"validityEvidence",copy(i.validityEvidence(),"hours",Map.of(id(1),h)));}
    @Test void duplicateUnknownMalformedAndUnboundIncludedRelationsRejectEvenIfUnused() {
        var i=withCause(input(1,new int[]{0},0),1);var v=i.validityEvidence();var role=v.roles().get(id(20));
        var duplicate=new Relation<>(role.metadata(),List.of(role.rows().getFirst(),role.rows().getFirst()));
        EffectiveCompositionAdmissionTest.rejects(copy(i,"validityEvidence",copy(v,"roles",Map.of(id(20),duplicate))),EffectiveCompositionFailure.Category.AMBIGUOUS_OR_CONTRADICTORY_INPUT);
        for(String f:List.of("sourceName","schemaFingerprint","ruleVersion","participantName","participantExecutionIdentity","scope")) {
            var bad=new Relation<>(copy(role.metadata(),f,"UNBOUND"),role.rows());
            EffectiveCompositionAdmissionTest.rejects(copy(i,"validityEvidence",copy(v,"roles",Map.of(id(20),bad))),EffectiveCompositionFailure.Category.INPUT_EVIDENCE_INCOMPLETE);
        }
        var bad=new Relation<>(copy(role.metadata(),"complete",false),role.rows());
        EffectiveCompositionAdmissionTest.rejects(copy(i,"validityEvidence",copy(v,"roles",Map.of(id(20),bad))),EffectiveCompositionFailure.Category.INPUT_EVIDENCE_INCOMPLETE);
        var weekly=v.hours().get(id(1)).weekly();var wrong=copy(weekly.rows().getFirst(),"salonId",id(99));
        assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(withHours(i,copy(v.hours().get(id(1)),"weekly",new Relation<>(weekly.metadata(),List.of(wrong))))));
    }
    @Test void exactTime6EndpointsContainmentAndNoOvernightRounding() {
        var n=nominal(0);var h=input(List.of(n),List.of()).validityEvidence().hours().get(id(1));var w=h.weekly().rows().getFirst();
        var exact=copy(copy(w,"opening",n.assignmentStart()),"closing",n.assignmentEnd());
        var i=input(List.of(n),List.of());assertEquals(1,new EffectiveProgrammingComposer().compose(withHours(i,copy(h,"weekly",new Relation<>(h.weekly().metadata(),List.of(exact))))).candidates().size());
        var tooLate=copy(exact,"opening",n.assignmentStart().plusNanos(1000));
        assertEquals(EffectiveCompositionOmission.Cause.AJUSTE_FUERA_DE_HORARIO_EFECTIVO,new EffectiveProgrammingComposer().compose(withHours(i,copy(h,"weekly",new Relation<>(h.weekly().metadata(),List.of(tooLate))))).omissions().getFirst().cause());
        assertThrows(RuntimeException.class,()->new EffectiveProgrammingComposer().compose(input(List.of(copy(n,"assignmentEnd",time(1))),List.of())));
    }
}
