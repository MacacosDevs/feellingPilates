package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.time.*;
import java.nio.charset.StandardCharsets;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.r5.composition.EffectiveCompositionTestFixtures.*;
class EffectiveCompositionCanonicalizerTest {
    @Test void literalIndependentByteAndShaVectors() {
        vector(EffectiveCompositionCanonicalizer.sequence(),"0:","ba768b331fd86cec803be04e56ab2b3d4c0e98ef4ee4fcd4e72ad7cce61a1d1f");
        vector(EffectiveCompositionCanonicalizer.encode(null),"1:6:ABSENT","d417c77ad0e7441b27f084d6e514a461fca81e80e2a0a2690122d79b718d38e5");
        vector(EffectiveCompositionCanonicalizer.encode(""),"3:5:VALUE6:String0:","5fb4aa21d334bc234eeabe7d20afeb55a46d27dc13bd345482c44caecd06401b");
        vector(EffectiveCompositionCanonicalizer.encode("á|:雪"),"3:5:VALUE6:String7:á|:雪","712a16b07ae3fe380b445d6792e59c4f11bad3adc5fc227484e4273443efac06");
        vector(EffectiveCompositionCanonicalizer.encode(new EffectiveValidityEvidence.Salon(id(1),true)),"8:6:RECORD5:Salon2:V11:26:activo24:3:5:VALUE7:Boolean4:true2:id54:3:5:VALUE4:UUID36:00000000-0000-0000-0000-000000000001","186f398918b5e8a807d2750220aed667e06edf75a109dcf4c07dc241f5000f94");
        vector(EffectiveCompositionCanonicalizer.encode(Map.of("rooms",List.of(new EffectiveValidityEvidence.Salon(id(1),true)))), "4:3:MAP1:124:3:5:VALUE6:String5:rooms135:3:4:LIST1:1120:8:6:RECORD5:Salon2:V11:26:activo24:3:5:VALUE7:Boolean4:true2:id54:3:5:VALUE4:UUID36:00000000-0000-0000-0000-000000000001", "f3a27371d47f54b5f4a36a99b35894526a3e80dfb9a213791fd6c8b4ac8f7a78");
    }
    static void vector(byte[] actual,String literal,String digest) {assertArrayEquals(bytes(literal),actual);assertEquals(digest,sha(actual));}
    @Test void fullCompletedEmptyCompositionGoldenHashesAndNonemptyIndependentRecordOracle() {
        var empty=input(0,new int[0],0);var r=new EffectiveProgrammingComposer().compose(empty);
        assertEquals("3857970d2fdf1aecbf246f547148a3f1193b1638d63f9b56c96d4e3969ac856d",r.inputCommitment());
        assertEquals("b787f892da16c2f0148a495a2323f8995a38f6d42d5d8d813eed91b697fc0795",r.resultContentFingerprint());
        assertEquals("d418154ab23d42e98904e352de2928ce2663c81ae39da562959adcc6fd6939c1",r.resultSnapshotIdentity());
        assertEquals("c88a7156074c2535b0223ab0885a1ed221bf7018d8ee700ca116d1fe5a3859a1",sha(EffectiveCompositionCanonicalizer.encode(empty)));
        assertEquals("f2688009654406a04237b59ee44a8bd43e9b63651e62eef9dcbc65b9338eae95",sha(EffectiveCompositionCanonicalizer.encode(r)));
        for(var i:List.of(input(1,new int[]{0},0),input(2,new int[]{1,3},1),withCause(input(1,new int[]{2},1),7))) {
            var result=new EffectiveProgrammingComposer().compose(i);
            assertArrayEquals(oracle(i),EffectiveCompositionCanonicalizer.encode(i));
            assertArrayEquals(oracle(result),EffectiveCompositionCanonicalizer.encode(result));
            assertEquals(hash("F2E-R5-INPUT-V1",i),result.inputCommitment());
            assertEquals(contentOracle(result),result.resultContentFingerprint());
            assertEquals(hash("F2E-R5-SNAPSHOT-V1",result.inputCommitment(),result.resultContentFingerprint(),i.envelope()),result.resultSnapshotIdentity());
            for(var c:result.candidates()) {
                assertEquals(hash("F2E-R5-CANDIDATE-SNAPSHOT-V1",result.resultSnapshotIdentity(),c.reference(),c.candidateFingerprint()),c.snapshotIdentity());
                assertEquals(hash("F2E-R5-BACKING-V1",result.backingByReference().get(c.reference())),c.provenance().normalizedFields().get("backingCommitment"));
                assertEquals(hash("F2E-R5-SUPPORT-V1",i.validityEvidence()),c.provenance().normalizedFields().get("supportCommitment"));
            }
        }
    }
    static String contentOracle(EffectiveProgrammingCompositionResult r) {
        var list=new ArrayList<byte[]>();list.add(bytes("LIST"));list.add(bytes(""+r.candidates().size()));
        for(var c:r.candidates()) {
            var map=new TreeMap<String,Object>();
            try {for(var f:c.getClass().getRecordComponents())if(!f.getName().equals("snapshotIdentity"))map.put(f.getName(),f.getAccessor().invoke(c));}
            catch(ReflectiveOperationException e){throw new AssertionError(e);}
            list.add(oracleRecord("ProgrammingCandidateSnapshot",map));
        }
        return sha(frame(bytes("F2E-R5-CONTENT-V1"),oracle(r.date()),oracle(r.businessZoneId()),oracle(r.ruleVersion()),frame(list.toArray(byte[][]::new)),
                oracle(r.backingByReference()),oracle(r.omissions()),oracle(r.suppressions())));
    }
    @Test void strictScalarsAbsenceFramingMapOrderAndCollisionNegativeControls() {
        for(Object invalid:List.of("NUL\0x","\uD800",time(9).plusNanos(1),OffsetDateTime.parse("2026-10-04T00:00:00.000000001Z"),new Object()))
            assertThrows(EffectiveCompositionFailure.class,()->EffectiveCompositionCanonicalizer.encode(invalid));
        assertFalse(Arrays.equals(EffectiveCompositionCanonicalizer.encode(null),EffectiveCompositionCanonicalizer.encode("")));
        assertFalse(Arrays.equals(EffectiveCompositionCanonicalizer.encode("ABSENT"),EffectiveCompositionCanonicalizer.encode(null)));
        assertFalse(Arrays.equals(EffectiveCompositionCanonicalizer.sequence(bytes("a:b"),bytes("c")),EffectiveCompositionCanonicalizer.sequence(bytes("a"),bytes("b:c"))));
        var first=new LinkedHashMap<String,String>();first.put("z","1");first.put("á","2");var second=new LinkedHashMap<String,String>();second.put("á","2");second.put("z","1");
        assertArrayEquals(EffectiveCompositionCanonicalizer.encode(first),EffectiveCompositionCanonicalizer.encode(second));
        var session=new EffectiveCompositionCanonicalizer.Session();byte[] p=bytes("first");session.register("FORCED_TEST_DIGEST",p);p[0]=0;
        session.register("FORCED_TEST_DIGEST",bytes("first"));assertThrows(EffectiveCompositionFailure.class,()->session.register("FORCED_TEST_DIGEST",bytes("different")));
    }
    @Test void referenceOrderIsDateThenTypeThenUnsignedUuid() {
        var hi=new UUID(Long.MIN_VALUE,0);var lo=new UUID(Long.MAX_VALUE,-1);assertTrue(EffectiveCompositionCanonicalizer.UUID_ORDER.compare(lo,hi)<0);
        var refs=new ArrayList<>(List.of(new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE,id(0),DATE),new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,hi,DATE),
                new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION,lo,DATE),new ReferenciaOcurrencia(ReferenciaOcurrencia.Tipo.AJUSTE,hi,DATE.minusDays(1))));
        refs.sort(EffectiveCompositionCanonicalizer.REFERENCE_ORDER);assertEquals(DATE.minusDays(1),refs.getFirst().fecha());assertEquals(lo,refs.get(1).id());assertEquals(hi,refs.get(2).id());assertEquals(ReferenciaOcurrencia.Tipo.AJUSTE,refs.getLast().tipo());
    }    @Test void optionalNullSalonRoleKeysRowsAndProvenanceAreCanonicallyOrdered() {
        var i=input(1,new int[]{0},0);var role=i.validityEvidence().roles().get(id(20));var global=role.rows().getFirst();var scoped=copy(global,"nullableSalonId",id(1));
        String globalKey=id(20)+"/"+id(6)+"/ABSENT",scopedKey=id(20)+"/"+id(6)+"/"+id(1);
        var evidence=new EffectiveValidityEvidence.Relation<>(copy(role.metadata(),"recordKeys",List.of(scopedKey,globalKey)),List.of(scoped,global));
        assertEquals(global,evidence.rows().getFirst());assertEquals(globalKey,evidence.metadata().recordKeys().getFirst());
        var supplied=copy(i,"validityEvidence",copy(i.validityEvidence(),"roles",Map.of(id(20),evidence)));var r=new EffectiveProgrammingComposer().compose(supplied);
        var ids=r.candidates().getFirst().provenance().recordIds();assertTrue(ids.indexOf("ROLES/"+globalKey)<ids.indexOf("ROLES/"+scopedKey));
        assertArrayEquals(oracle(supplied),EffectiveCompositionCanonicalizer.encode(supplied));
        assertEquals(r.resultSnapshotIdentity(),new EffectiveProgrammingComposer().compose(permute(supplied,new Random(0xF2E5))).resultSnapshotIdentity());
    }

}
