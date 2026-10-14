package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.comparison.v2.ComparisonTestFixturesV2.*;
class ComparisonDeterminismV2Test {
    @Test void permutationsAndRepeatedCallsHaveExactCommitments() {
        var sources=new ArrayList<>(legacy("RECURRENTE",0,1));sources.addAll(legacy("EXCEPCION",0,2));sources.addAll(legacy("CANCELACION",0,3));
        var original=comparison(input(1,new int[]{0},1),sources);var service=new ComparisonCompatibilityV2();var expected=service.compare(original);
        for(int seed=0;seed<100;seed++) {
            Collections.shuffle(sources,new Random(seed));var actual=service.compare(copy(original,"legacy",sources));
            assertEquals(expected.inputCommitment(),actual.inputCommitment());assertEquals(expected.contentCommitment(),actual.contentCommitment());
            assertEquals(expected.executionCommitment(),actual.executionCommitment());
        }
    }
    @Test void framingUnicodeUnsignedUuidAndUnknownTypesAreDeterministic() {
        assertNotEquals(ComparisonCanonicalizerV2.hash("D","ab","c"),ComparisonCanonicalizerV2.hash("D","a","bc"));
        assertNotEquals(ComparisonCanonicalizerV2.hash("D",(Object)null),ComparisonCanonicalizerV2.hash("D","ABSENT"));
        assertNotEquals(ComparisonCanonicalizerV2.hash("D",Optional.empty()),ComparisonCanonicalizerV2.hash("D",List.of()));
        assertEquals(ComparisonCanonicalizerV2.hash("D",Map.of("a","é","b","水")),ComparisonCanonicalizerV2.hash("D",new TreeMap<>(Map.of("b","水","a","é"))));
        assertTrue(ComparisonCanonicalizerV2.UUID_ORDER.compare(new UUID(0,0),new UUID(Long.MIN_VALUE,0))<0);
        assertThrows(ComparisonFailureV2.class,()->ComparisonCanonicalizerV2.hash("D","\ud800"));
        assertThrows(ComparisonFailureV2.class,()->ComparisonCanonicalizerV2.encode(new Object()));
    }
    @Test void independentLiteralFramingAndUnsignedTextMapVectors() {
        assertEquals("4da8969d1958704d794894b4aa7110ee840d162ba866ebcdf4d469a03bdc935d",ComparisonCanonicalizerV2.hash("D","ab","c"));
        assertEquals("5fddd5c4ae98585a6a2641155f85a0716a57efad7206be9fcd1f7f6057cb22c9",ComparisonCanonicalizerV2.hash("D",Map.of("b","y","aa","x")));
        assertThrows(ComparisonFailureV2.class,()->ComparisonCanonicalizerV2.hash("D","nul\0value"));
    }
    @Test void everyInputMetadataAndOutcomeMutationChangesCommitment() {
        var i=comparison(withCause(input(0,new int[]{},1),1));var service=new ComparisonCompatibilityV2();var r=service.compare(i);
        for(var changed:List.of(copy(i,"runIdentity","other run"),copy(i,"invocationIdentity","other invocation"),copy(i,"evaluatedAt",i.evaluatedAt().plusSeconds(1)))) {
            var other=service.compare(changed);
            assertNotEquals(r.inputCommitment(),other.inputCommitment());
            assertNotEquals(r.executionCommitment(),other.executionCommitment());
            assertEquals(r.contentCommitment(),other.contentCommitment());
        }
        var outcome=r.evaluations().getFirst().outcome();
        for(var component:outcome.getClass().getRecordComponents()) {
            Object value;
            try{value=component.getAccessor().invoke(outcome);}catch(Exception e){throw new AssertionError(e);}
            Object changed=value instanceof Integer n?n+1:value instanceof Boolean b?!b:value instanceof OptionalInt?OptionalInt.of(1):value instanceof Optional<?>?Optional.of("changed"):
                    value instanceof List<?>?List.of("changed"):value instanceof Enum<?> en?differentEnum(en):
                    com.feelingpilates.transicion.programacion.detector.F2DCompatibilityResult.conflict(List.of("changed"));
            var mutated=copy(outcome,component.getName(),changed);
            assertFalse(Arrays.equals(ComparisonCanonicalizerV2.encode(outcome),ComparisonCanonicalizerV2.encode(mutated)),component.getName());
        }
        var other=service.compare(comparison(withCause(input(0,new int[]{},1),2)));
        assertNotEquals(r.inputCommitment(),other.inputCommitment());assertNotEquals(r.contentCommitment(),other.contentCommitment());
    }
    @Test void unsignedReferenceMapAndFullLedgerOrderingPreservesClosedCore() {
        var low=new com.feelingpilates.programacion.dominio.ReferenciaOcurrencia(com.feelingpilates.programacion.dominio.ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,new UUID(0,1),DATE);
        var high=new com.feelingpilates.programacion.dominio.ReferenciaOcurrencia(low.tipo(),new UUID(Long.MIN_VALUE,0),DATE);
        var input=comparison(input(1,new int[]{0},0),legacy("CANCELACION",0,43));var target=input.nominalCandidates().getFirst();
        var generated=new LegacyCandidateGeneratorV2().generate(input.legacy().getFirst(),List.of(copy(target,"reference",high),copy(target,"reference",low)),DATE);
        assertEquals(List.of(low,high),generated.observations().stream().map(o->o.target().reference()).toList());
        String encoded=new String(ComparisonCanonicalizerV2.encode(Map.of(high,"high",low,"low")),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(encoded.indexOf(low.id().toString())<encoded.indexOf(high.id().toString()));
        assertTrue(high.compareTo(low)<0); // CLOSED comparator intentionally stays signed.
    }
    @Test void allSourceGroupsSeparateComparisonMetadataFromContent() {
        var input=comparison(input(3,new int[]{1,2,0},1),legacy("CANCELACION",0,44));var service=new ComparisonCompatibilityV2();
        var original=service.compare(input);
        for(var changed:List.of(copy(input,"runIdentity","other"),copy(input,"invocationIdentity","other"),copy(input,"evaluatedAt",input.evaluatedAt().plusSeconds(1)))) {
            var other=service.compare(changed);assertEquals(original.contentCommitment(),other.contentCommitment());
            assertNotEquals(original.executionCommitment(),other.executionCommitment());
            assertNotEquals(original.evaluations().getFirst().resultCommitment(),other.evaluations().getFirst().resultCommitment());
        }
    }
    private static Object differentEnum(Enum<?> value) {
        for(var other:value.getDeclaringClass().getEnumConstants())if(other!=value)return other;return null;
    }
    private static List<Object> values(Object record) {
        var values=new ArrayList<Object>();for(var c:record.getClass().getRecordComponents())try{values.add(c.getAccessor().invoke(record));}catch(Exception e){throw new AssertionError(e);}return values;
    }
}
