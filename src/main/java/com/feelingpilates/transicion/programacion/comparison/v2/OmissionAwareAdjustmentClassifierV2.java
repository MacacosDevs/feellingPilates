package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.*;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveProgrammingCompositionResult;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionInput;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionEnvelope;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionBacking;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionOmission;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionSuppression;
import com.feelingpilates.transicion.programacion.r5.composition.EffectiveValidityEvidence;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.*;

/** Validates issued R5 linkage; never composes, authenticates a resource or changes old core results. */
public final class OmissionAwareAdjustmentClassifierV2 {
    public record OmissionProof(EffectiveCompositionOmission omission, String commitment) { }
    private static void require(boolean ok, String rule) { ComparisonFailureV2.require(ok,"R5_LINKAGE",rule); }
    static void validate(ComparisonInputV2 i) {
        EffectiveProgrammingCompositionResult r=i.effectiveResult(); EffectiveCompositionInput original=r.input();
        require(i.date().equals(r.date()) && i.businessZoneId().equals(r.businessZoneId()) &&
                "F2E-R5-PURE-COMPOSITION-V1".equals(r.ruleVersion()),"R5 date/zone/rule binding");
        require(i.nominalReadSet().equals(original.nominalReadSet()) && i.adjustmentReadSet().equals(original.adjustmentReadSet()),"exact original R3/R4 supplied facts");
        EffectiveCompositionEnvelope envelope=r.envelope();
        require(i.completionReceiptIdentity().equals(envelope.completionReceiptIdentity()),"supplied completion receipt binding");
        require((i.mode()==ComparisonInputV2.Mode.SYNTHETIC_DESIGN_FIXTURE)==
                (envelope.mode()==EffectiveCompositionEnvelope.Mode.SYNTHETIC_DESIGN_FIXTURE),"no synthetic/real mode upgrade");
        if(i.mode()==ComparisonInputV2.Mode.OWNER_AUTHENTICATED_SUPPLIED) {
            for(String value:List.of(i.runIdentity(),i.invocationIdentity(),i.completionReceiptIdentity()))realLabel(value);
            var sources=new ArrayList<SourceSnapshot>();sources.addAll(i.reservations());sources.addAll(i.legacy());
            for(var source:sources) {
                realLabel(source.provenance().sourceName());realLabel(source.snapshotIdentity());
                for(String key:List.of("runIdentity","attemptIdentity","fixtureIdentity","readerInvocationIdentity","logicalSnapshotId")) {
                    var value=source.provenance().normalizedFields().get(key);if(value!=null)realLabel(value);
                }
            }
        }
        for (var p:i.reservations()) require(p.date().equals(i.date()) && p.historicalProgrammingTarget().isEmpty(),"current exact-date reservation only");
        for (var s:i.legacy()) require(i.businessZoneId().equals(s.provenance().normalizedFields().get("businessZone")),"legacy supplied zone");
        var nominal=new LinkedHashMap<ReferenciaOcurrencia,NominalBackingSnapshot>();
        for (var b:i.nominalReadSet().backing()) { require(i.date().equals(b.fecha()),"nominal date");require(nominal.put(b.reference(),b)==null,"unique nominal reference"); }
        var adjustment=new LinkedHashMap<UUID,AdjustmentBackingSnapshot>();
        for (var b:i.adjustmentReadSet().backing()) {require(i.date().equals(b.fecha()),"adjustment date"); require(adjustment.put(b.id(),b)==null,"unique adjustment");}
        var expected=new HashSet<>(nominal.keySet());
        for (var b:adjustment.values()) {
            var ref=reference(b);
            if (b.tipo().equals("ADICION")) require(expected.add(ref),"unique addition reference");
            else require(nominal.containsKey(ref),"nominal adjustment target");
        }
        var partition=new HashMap<ReferenciaOcurrencia,Integer>();
        var candidateRefs=new HashSet<ReferenciaOcurrencia>();var guard=new F2DAuthorityGuard();
        for (var c:r.candidates()) {
            require(candidateRefs.add(c.reference()) && expected.contains(c.reference()) && c.reference().fecha().equals(i.date()),"unique accounted candidate");
            require(!guard.evaluate(c).blocking(),"F2D candidate reference/type");
            var b=r.backingByReference().get(c.reference());require(b!=null,"candidate backing");
            backing(i,b,nominal,adjustment);
            require(c.salonId().equals(b.salonId()) && c.instructorId().equals(b.instructorId()) && c.activityId().equals(b.activityId()) &&
                    c.start().equals(b.start()) && c.end().equals(b.end()),"candidate dimensions/backing");
            partition.merge(c.reference(),1,Integer::sum);
        }
        require(r.backingByReference().keySet().equals(candidateRefs),"candidate/backing bijection");
        var omissionRefs=new HashSet<ReferenciaOcurrencia>();
        for (EffectiveCompositionOmission o:r.omissions()) {
            require(o!=null && o.cause()!=null && omissionRefs.add(o.reference()) && expected.contains(o.reference()),"unique accounted omission");
            require(o.reference().equals(o.backing().reference()) && o.envelope().equals(envelope) &&
                    o.ruleVersion().equals(r.ruleVersion()) && o.schemaFingerprint().matches("[0-9a-f]{64}"),"omission origin/rule/schema/envelope");
            require(o.support().equals(original.validityEvidence()) && o.support().equals(o.backing().support()),"full original omission support");
            backing(i,o.backing(),nominal,adjustment);partition.merge(o.reference(),1,Integer::sum);
        }
        var suppressed=new HashSet<ReferenciaOcurrencia>();
        for (EffectiveCompositionSuppression s:r.suppressions()) {
            require(s!=null && suppressed.add(s.reference()) && expected.contains(s.reference()),"unique accounted suppression");
            var b=adjustment.get(s.adjustmentBacking().id());
            require(b!=null && b.equals(s.adjustmentBacking()) && b.tipo().equals("CANCELACION") && reference(b).equals(s.reference()) &&
                    s.nominalBacking().equals(nominal.get(s.reference())) && s.nominalCandidate().reference().equals(s.reference()) &&
                    s.adjustmentSource().equals(source(i,b.id())) && s.envelope().equals(envelope) && s.ruleVersion().equals(r.ruleVersion()),"exact cancellation suppression backing");
            partition.merge(s.reference(),1,Integer::sum);
        }
        require(partition.keySet().equals(expected) && partition.values().stream().allMatch(n->n==1),"exhaustive disjoint R5 outcome partition");
        for (var b:adjustment.values()) {
            var ref=reference(b);
            require(b.tipo().equals("CANCELACION")?suppressed.contains(ref):!suppressed.contains(ref),"cancellation versus required outcome");
            if (!b.tipo().equals("CANCELACION")) {
                var backing=r.backingByReference().get(ref);
                if (backing==null) backing=r.omissions().stream().filter(o->o.reference().equals(ref)).findFirst().orElseThrow().backing();
                require(b.equals(backing.adjustmentBacking()) && source(i,b.id()).equals(backing.adjustmentSource()),"exact originating adjustment for outcome");
            }
        }
    }
    private static void realLabel(String value) {
        String upper=value.toUpperCase(Locale.ROOT);require(!upper.contains("SYNTHETIC")&&!upper.contains("FIXTURE"),"no synthetic/mixed identity in real supplied mode");
    }
    private static void backing(ComparisonInputV2 i,EffectiveCompositionBacking b,
            Map<ReferenciaOcurrencia,NominalBackingSnapshot> nominal,Map<UUID,AdjustmentBackingSnapshot> adjustment) {
        EffectiveValidityEvidence support=i.effectiveResult().input().validityEvidence();require(b.support().equals(support),"original backing support");
        if (b.nominalAxis()==EffectiveCompositionBacking.NominalAxis.PRESENT) {
            require(b.nominalBacking()!=null && b.nominalBacking().equals(nominal.get(b.reference())) && b.nominalCandidate()!=null &&
                    i.nominalCandidates().contains(b.nominalCandidate()) && b.nominalCandidate().reference().equals(b.reference()),"original nominal backing/candidate");
        } else require(b.nominalAxis()==EffectiveCompositionBacking.NominalAxis.NOT_APPLICABLE && b.nominalBacking()==null && b.nominalCandidate()==null,"no synthetic nominal");
        if (b.adjustmentAxis()==EffectiveCompositionBacking.AdjustmentAxis.PRESENT) {
            var a=b.adjustmentBacking();require(a!=null && a.equals(adjustment.get(a.id())) && b.adjustmentSource().equals(source(i,a.id())) &&
                    !a.tipo().equals("CANCELACION") && reference(a).equals(b.reference()),"original adjustment backing/source");
            require(b.salonId().equals(a.salonResultadoId()) && b.instructorId().equals(a.instructorResultadoId()) && b.activityId().equals(a.tipoActividadResultadoId()) &&
                    b.start().equals(a.horaInicioResultado()) && b.end().equals(a.horaFinResultado()),"adjustment final dimensions");
            require(a.tipo().equals("ADICION")?b.origin()==EffectiveCompositionBacking.Origin.ADDITION_OCCURRENCE && b.nominalAxis()==EffectiveCompositionBacking.NominalAxis.NOT_APPLICABLE
                    :b.origin()==EffectiveCompositionBacking.Origin.REPLACEMENT_OCCURRENCE && b.nominalAxis()==EffectiveCompositionBacking.NominalAxis.PRESENT,"adjustment origin");
        } else {
            require(b.adjustmentAxis()==EffectiveCompositionBacking.AdjustmentAxis.NOT_APPLICABLE && b.adjustmentBacking()==null && b.adjustmentSource()==null &&
                    b.origin()==EffectiveCompositionBacking.Origin.RECURRENT_OCCURRENCE && b.nominalBacking()!=null,"recurrent original origin");
            var n=b.nominalBacking();require(b.salonId().equals(n.salonId()) && b.instructorId().equals(n.instructorId()) && b.activityId().equals(n.activityId()) &&
                    b.start().equals(n.assignmentStart()) && b.end().equals(n.assignmentEnd()),"nominal final dimensions");
        }
    }
    static GenericSourceSnapshot source(ComparisonInputV2 i,UUID id) {
        return i.adjustmentSources().stream().filter(s->s.sourceIdentity().equals(id.toString())).findFirst().orElseThrow();
    }
    private static ReferenciaOcurrencia reference(AdjustmentBackingSnapshot b) {
        return new ReferenciaOcurrencia(b.tipo().equals("ADICION")?ReferenciaOcurrencia.Tipo.AJUSTE:ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,
                b.tipo().equals("ADICION")?b.id():b.asignacionSerieId(),b.fecha());
    }
    static Optional<EffectiveCompositionOmission> omissionForSource(ComparisonInputV2 i, GenericSourceSnapshot source) {
        validate(i);require(i.adjustmentSources().contains(source),"original adjustment source");
        var a=i.adjustmentReadSet().backing().stream().filter(b->b.id().toString().equals(source.sourceIdentity())).findFirst().orElseThrow();
        return i.effectiveResult().omissions().stream().filter(o->o.reference().equals(reference(a)) && a.equals(o.backing().adjustmentBacking()) && source.equals(o.backing().adjustmentSource())).findFirst();
    }
    static OmissionProof additionOmission(ComparisonInputV2 i,GenericSourceSnapshot source) {
        validate(i); require(source.sourceAtomType()==SourceAtomType.NEW_ADICION && i.adjustmentSources().contains(source),"original addition source");
        var a=i.adjustmentReadSet().backing().stream().filter(b->b.id().toString().equals(source.sourceIdentity())).findFirst().orElseThrow();var ref=reference(a);
        var matches=i.effectiveResult().omissions().stream().filter(o->o.reference().equals(ref) && a.equals(o.backing().adjustmentBacking()) && source.equals(o.backing().adjustmentSource())).toList();
        require(matches.size()==1 && i.effectiveCandidates().stream().noneMatch(c->c.reference().equals(ref)),"exact explained addition omission, e=0");
        var o=matches.getFirst();
        String proof=ComparisonCanonicalizerV2.hash("F2E-EXPLAINED-ADDITION-OMISSION-V2",i.compositionBinding(),source,ref,o.cause(),o.ruleVersion(),o.schemaFingerprint(),
                a.normalizedFields(),o.backing().origin(),o.backing().salonId(),o.backing().instructorId(),o.backing().activityId(),o.backing().start(),o.backing().end(),
                o.backing().nominalAxis(),o.backing().adjustmentAxis(),i.completionReceiptIdentity());
        return new OmissionProof(o,proof);
    }
    ComparisonEvaluationV2 classify(ComparisonInputV2 i,GenericSourceSnapshot s) {
        var a=i.adjustmentReadSet().backing().stream().filter(b->b.id().toString().equals(s.sourceIdentity())).findFirst().orElseThrow();var ref=reference(a);
        int n=(int)i.nominalCandidates().stream().filter(c->c.reference().equals(ref)).count();
        var effective=i.effectiveCandidates().stream().filter(c->c.reference().equals(ref)).toList();int e=effective.size();
        if(s.sourceAtomType()==SourceAtomType.NEW_ADICION && e==0) return ComparisonEvaluationV2.omittedAddition(i,s);
        var candidates=effective.stream().map(c->new DetectorCandidate(c.reference(),c.candidateType(),c.snapshotIdentity(),c.candidateFingerprint(),
                List.of(new CandidateEvidenceItem(EvidenceDimension.DATE,EvidenceKind.EXACT_IDENTITY,i.date().toString(),c.reference().fecha().toString(),true)),
                EvidenceKind.EXACT_IDENTITY,c.observableFields(),Set.of(EvidenceDimension.DATE),Set.of(),EligibilityStatus.ELIGIBLE,List.of(),
                ComparisonCanonicalizerV2.hash("F2E-ADJUSTMENT-CANDIDATE-V2",s,c,i.compositionBinding()),c.provenance())).toList();
        var context=s.sourceAtomType()==SourceAtomType.NEW_CANCELACION?ClassificationContext.newCancellation(n,e)
                :s.sourceAtomType()==SourceAtomType.NEW_REEMPLAZO?ClassificationContext.newReplacement(n,e):ClassificationContext.newAddition(e);
        String proof=ComparisonCanonicalizerV2.hash("F2E-ADJUSTMENT-OUTCOME-V2",s,i.compositionBinding(),n,e,ref);
        var request=ComparisonCompatibilityV2.request(i,s,new CandidateGenerationResult(candidates,F2DCompatibilityResult.compatible()),context,
                s.sourceAtomType()==SourceAtomType.NEW_CANCELACION?RelationCardinality.ONE_TO_ZERO:RelationCardinality.ONE_TO_ONE,proof);
        return ComparisonEvaluationV2.adjustmentCore(i,s,request,new DetectorClassifier().classify(request),
                s.sourceAtomType()==SourceAtomType.NEW_ADICION?"EFFECTIVE":"NOMINAL_AND_EFFECTIVE",proof);
    }
}
