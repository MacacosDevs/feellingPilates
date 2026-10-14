package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import org.junit.jupiter.api.Test;
import com.feelingpilates.transicion.programacion.detector.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.comparison.v2.ComparisonTestFixturesV2.*;
class ComparisonInvariantV2Test {
    @Test void entireUnitFailsOnInputModeAuthorityDateZoneAndLateSourceErrors() {
        var i=comparison(input(1,new int[]{0},0),legacy("RECURRENTE",0,1));var service=new ComparisonCompatibilityV2();
        assertThrows(ComparisonFailureV2.class,()->service.compare(null));
        for(var bad:List.of(copy(i,"mode",ComparisonInputV2.Mode.OWNER_AUTHENTICATED_SUPPLIED),copy(i,"date",DATE.plusDays(1)),
                copy(i,"businessZoneId","UTC"),copy(i,"completionReceiptIdentity","foreign")))
            assertThrows(ComparisonFailureV2.class,()->service.compare(bad));
        assertThrows(ComparisonFailureV2.class,()->copy(i,"ruleVersion","another"));
        assertThrows(ComparisonFailureV2.class,()->copy(i,"businessZoneId","invalid zone"));
        assertThrows(ComparisonFailureV2.class,()->copy(i,"legacy",List.of(i.legacy().getFirst(),i.legacy().getFirst())));
        var broken=copy(i.legacy().getFirst(),"observableFields",Map.of());
        assertThrows(ComparisonFailureV2.class,()->service.compare(copy(i,"legacy",List.of(broken))));
    }
    @Test void mixedSyntheticSourceLabelsCannotUpgradeDeclaredRealInputs() {
        var service=new ComparisonCompatibilityV2();var real=declaredRealInput(legacy("RECURRENTE",0,1,false));
        assertEquals(1,service.compare(real).evaluationCount()); // supplied declarations, not physical authentication
        assertThrows(ComparisonFailureV2.class,()->service.compare(declaredRealInput(legacy("RECURRENTE",0,1))));
        assertThrows(ComparisonFailureV2.class,()->service.compare(copy(real,"runIdentity","SYNTHETIC_RUN")));
        var provenance=new EvidenceProvenance("SYNTHETIC_RESERVA","sha256:"+HASH,List.of(id(9).toString()),"R1","V1",ZONE+"/"+DATE,Map.of());
        var reservation=new ReservationSourceSnapshot(id(9),DetectorVocabulary.ReservationState.CONFIRMADA,DATE,id(1),id(20),id(3),
                new ReservedSubinterval(time(9),time(10)),HASH,HASH,Map.of(),provenance);
        assertThrows(ComparisonFailureV2.class,()->service.compare(copy(real,"reservations",List.of(reservation))));
    }
    @Test void everyDeclaredSourceIdentitySlotRejectsSyntheticLabels() {
        var service=new ComparisonCompatibilityV2();var real=declaredRealInput(legacy("RECURRENTE",0,41,false));
        var source=real.legacy().getFirst();
        for(String key:List.of("runIdentity","attemptIdentity","fixtureIdentity","readerInvocationIdentity","logicalSnapshotId","ruleCatalogVersion","projectionCatalogVersion","scope","scopeCanonical","projectionContractId","projectionContractVersion","snapshotClaim","snapshotEvidenceId","executionProvenanceId","sourceIdentity","snapshotIdentity")) {
            var fields=new LinkedHashMap<>(source.provenance().normalizedFields());fields.put(key,"SYNTHETIC_MARKER");
            var changed=copy(source,"provenance",copy(source.provenance(),"normalizedFields",fields));
            var failure=assertThrows(ComparisonFailureV2.class,()->service.compare(copy(real,"legacy",List.of(changed))));
            assertEquals("R5_LINKAGE",failure.stage(),key);
            assertEquals(Optional.of(source.sourceIdentity()),failure.context().sourceIdentity(),key);
        }
    }
    @Test void noCallerChosenBatchListsCanReachIssuance() {
        assertEquals(1,ComparisonResultV2.class.getDeclaredConstructors().length);
        assertArrayEquals(new Class<?>[]{ComparisonInputV2.class},ComparisonResultV2.class.getDeclaredConstructors()[0].getParameterTypes());
        for(var method:ComparisonResultV2.class.getDeclaredMethods())if(method.getName().equals("issue"))
            assertArrayEquals(new Class<?>[]{ComparisonInputV2.class},method.getParameterTypes());
        var input=comparison(withCause(input(0,new int[]{},1),1));
        var actual=ComparisonResultV2.issue(input);
        assertEquals(ComparisonEvaluationV2.Variant.EXPLAINED_ADDITION_OMISSION_V2,actual.evaluations().getFirst().variant());
        assertTrue(actual.evaluations().getFirst().coreResult().isEmpty());
        assertEquals(input.adjustmentSources().getFirst(),actual.evaluations().getFirst().request().source());
    }
    @Test void failuresRetainOnlyAvailableSafeContextAndOriginalCause() {
        var input=comparison(input(1,new int[]{0},0),legacy("RECURRENTE",0,42));
        var broken=copy(input.legacy().getFirst(),"observableFields",Map.of());
        var failed=assertThrows(ComparisonFailureV2.class,()->new ComparisonCompatibilityV2().compare(copy(input,"legacy",List.of(broken))));
        assertEquals(Optional.of(input.runIdentity()),failed.context().runIdentity());
        assertEquals(Optional.of(input.invocationIdentity()),failed.context().invocationIdentity());
        assertEquals(Optional.of(DATE),failed.context().date());assertEquals(Optional.of(broken.sourceIdentity()),failed.context().sourceIdentity());
        var absent=assertThrows(ComparisonFailureV2.class,()->new ComparisonCompatibilityV2().compare(null));
        assertEquals(Optional.empty(),absent.context().runIdentity());assertEquals(Optional.empty(),absent.context().sourceIdentity());
        assertInstanceOf(NullPointerException.class,absent.getCause());
        var cause=new IllegalArgumentException("original");
        assertSame(cause,new ComparisonFailureV2("TEST","safe",cause).withContext(input,broken).getCause());
    }
    @Test void allResultsAndEvidenceStayImmutableAndPrivatelyIssued() {
        var i=comparison(withCause(input(0,new int[]{},1),1));var r=new ComparisonCompatibilityV2().compare(i);var e=r.evaluations().getFirst();
        assertSame(i,r.input());
        assertThrows(UnsupportedOperationException.class,()->r.evaluations().clear());assertThrows(UnsupportedOperationException.class,()->r.observations().clear());
        assertThrows(UnsupportedOperationException.class,()->e.outcome().blockedCapabilities().clear());
        for(var type:List.of(ComparisonEvaluationV2.class,ComparisonResultV2.class))
            assertTrue(Arrays.stream(type.getDeclaredConstructors()).allMatch(c->java.lang.reflect.Modifier.isPrivate(c.getModifiers())));
        var mutable=new ArrayList<>(legacy("CANCELACION",0,3));var copied=comparison(input(1,new int[]{0},0),mutable);mutable.clear();assertEquals(1,copied.legacy().size());
        var ledger=new ComparisonCompatibilityV2().compare(copied).observations().getFirst();
        assertThrows(UnsupportedOperationException.class,()->ledger.dimensions().clear());
        assertThrows(ComparisonFailureV2.class,()->copy(ledger,"dimensions",Map.of()));
    }
    @Test void everyOriginalSourceHasOneTypedEvaluationInFixedGroupOrder() {
        var sources=new ArrayList<GenericSourceSnapshot>();sources.addAll(legacy("CANCELACION",0,9));sources.addAll(legacy("EXCEPCION",0,8));sources.addAll(legacy("RECURRENTE",0,7));
        var i=comparison(input(3,new int[]{1,2,0},1),sources);var batch=new ComparisonCompatibilityV2().compare(i);
        assertEquals(6,batch.sourceCount());assertEquals(6,batch.evaluationCount());
        assertEquals(List.of("LEGACY_RECURRENTE","LEGACY_EXCEPCION","LEGACY_CANCELACION","NEW_CANCELACION","NEW_REEMPLAZO","NEW_ADICION"),
                batch.evaluations().stream().map(e->e.request().source().sourceAtomType().name()).toList());
        assertEquals(6,new HashSet<>(batch.evaluations().stream().map(ComparisonEvaluationV2::evaluationIdentity).toList()).size());
    }
    @Test void emptyCompleteInputsHaveExplicitZeroAccounting() {
        var batch=new ComparisonCompatibilityV2().compare(comparison(input(0,new int[]{},0)));
        assertEquals(0,batch.sourceCount());assertEquals(0,batch.evaluationCount());assertEquals(0,batch.universeCount());assertEquals(0,batch.excludedCount());
        assertTrue(batch.inputCommitment().matches("[0-9a-f]{64}"));assertTrue(batch.executionCommitment().matches("[0-9a-f]{64}"));
    }
    @Test void originalCoreResultCannotBePatchedOrMismatched() {
        var e=new ComparisonCompatibilityV2().compare(comparison(input(0,new int[]{},1))).evaluations().getFirst();
        var request=copy(e.request(),"evaluationIdentity","another");
        assertThrows(ComparisonFailureV2.class,()->ComparisonEvaluationV2.core(request,e.coreResult().orElseThrow(),"EFFECTIVE","proof"));
    }
}
