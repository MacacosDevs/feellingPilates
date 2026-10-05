package com.feelingpilates.transicion.programacion.r5.composition;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import static org.junit.jupiter.api.Assertions.*;
class EffectiveCompositionArchitectureTest {
    static final String PACKAGE="com.feelingpilates.transicion.programacion.r5.composition";
    static final List<String> MAIN=List.of("EffectiveCompositionBacking","EffectiveCompositionCanonicalizer","EffectiveCompositionEnvelope","EffectiveCompositionFailure",
            "EffectiveCompositionInput","EffectiveCompositionOmission","EffectiveCompositionSuppression","EffectiveProgrammingComposer","EffectiveProgrammingCompositionResult","EffectiveValidityEvidence");
    static final List<String> TEST=List.of("EffectiveCompositionAdmissionTest","EffectiveCompositionArchitectureTest","EffectiveCompositionCanonicalizerTest","EffectiveCompositionImmutabilityTest",
            "EffectiveCompositionInvariantTest","EffectiveCompositionPropertyTest","EffectiveCompositionRuntimeIsolationTest","EffectiveCompositionTestFixtures","EffectiveCompositionValidityTest","EffectiveProgrammingComposerTest");
    static Set<String> paths() {
        var p=new HashSet<String>();MAIN.forEach(n->p.add("src/main/java/"+PACKAGE.replace('.','/')+"/"+n+".java"));TEST.forEach(n->p.add("src/test/java/"+PACKAGE.replace('.','/')+"/"+n+".java"));return p;
    }
    static Set<String> symbols(String code) {
        code=code.replaceAll("(?s)/\\*.*?\\*/"," ").replaceAll("(?m)//.*$"," ");var out=new HashSet<String>();var matcher=Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*").matcher(code);
        while(matcher.find())out.add(matcher.group());return out;
    }
    static void pure(String code,String name) {
        assertTrue(code.contains("package "+PACKAGE+";"),"exact package");
        assertTrue(Pattern.compile("public\\s+(?:final\\s+class|record)\\s+"+name+"\\b").matcher(code).find(),"plain final/record "+name);
        for(String token:List.of("Component","Service","Repository","Configuration","Bean","Transactional","EntityManager","DataSource","JdbcTemplate","DriverManager",
                "NominalJpaReader","AdjustmentJpaReader","NominalProgrammingReadPort","AdjustmentReadPort","NominalReadSnapshotContext","AdjustmentReadSnapshotContext",
                "Thread","Clock","Random","System","Runtime","ProcessBuilder","ClassLoader","Logger","Proxy"))assertFalse(symbols(code).contains(token),token);
        for(String edge:List.of("org.springframework","jakarta.persistence","javax.persistence","org.hibernate","java.sql","javax.sql","java.net","java.nio.file","java.lang.reflect",
                "java.util.concurrent","java.util.logging",".adapter.",".servicio.",".repositorio.",".entidad.",".controlador.",".read.*","Class.forName","getDeclared","UUID.randomUUID",".now(","::now"))assertFalse(code.contains(edge),edge);
        assertFalse(Pattern.compile("\\bSystem\\s*\\.").matcher(code).find(),"System effect");
        var imports=Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.*]+)\\s*;").matcher(code);
        var allowed=Set.of("com.feelingpilates.programacion.dominio.ReferenciaOcurrencia",
                "com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot","com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot",
                "com.feelingpilates.transicion.programacion.detector.EvidenceProvenance","com.feelingpilates.transicion.programacion.detector.DetectorVocabulary",
                "com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot","com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet",
                "com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers","com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot",
                "com.feelingpilates.transicion.programacion.r4.read.AdjustmentReadSet");
        while(imports.find()){String edge=imports.group(1);assertTrue(edge.startsWith("java.time.")||edge.startsWith("java.util.")||edge.startsWith("java.nio.charset.")||edge.startsWith("java.security.")||edge.startsWith(PACKAGE+".")||allowed.contains(edge),edge);}
    }
    static void caller(String code) {assertTrue(Collections.disjoint(symbols(code),new HashSet<>(MAIN)),"outside caller or bean into R5");assertFalse(code.contains(PACKAGE),"dynamic/FQ caller edge");}
    @Test void exactTenMainTenTestAndSealedScopeHashWithMissingExtraControls() throws Exception {
        var actual=new HashSet<String>();for(String root:List.of("src/main/java/","src/test/java/"))try(var files=Files.walk(Path.of(root+PACKAGE.replace('.','/')))) {
            files.filter(p->p.toString().endsWith(".java")).forEach(p->actual.add(p.toString()));}
        assertEquals(paths(),actual);assertEquals(20,actual.size());
        String canonical=String.join("\n",actual.stream().sorted().toList())+"\n";
        assertEquals("330488d90768982ed3c18c7d49a2531767eda2409b2de178f7c5a2d68586d01b",EffectiveCompositionTestFixtures.sha(EffectiveCompositionTestFixtures.bytes(canonical)));
        var missing=new HashSet<>(actual);missing.remove(missing.iterator().next());assertThrows(AssertionError.class,()->assertEquals(paths(),missing));
        var extra=new HashSet<>(actual);extra.add("src/main/java/"+PACKAGE.replace('.','/')+"/Extra.java");assertThrows(AssertionError.class,()->assertEquals(paths(),extra));
    }
    @Test void allProductionEdgesAndNoCallersAreInspected() throws Exception {
        try(var files=Files.walk(Path.of("src/main/java"))) {
            for(var p:files.filter(f->f.toString().endsWith(".java")).toList()) {
                String code=Files.readString(p);
                if(paths().contains(p.toString()))pure(code,p.getFileName().toString().replace(".java",""));else caller(code);
            }
        }
    }
    @Test void missingPartialStereotypeDependencyImportReflectionEffectsAndCallerControls() {
        String name="EffectiveProgrammingComposer";String good="package "+PACKAGE+"; public final class "+name+" { }";pure(good,name);
        assertThrows(AssertionError.class,()->pure("package "+PACKAGE+";",name));
        assertThrows(AssertionError.class,()->pure(good.replace("public final","public"),name));
        for(String bad:List.of("@Component","NominalJpaReader reader;","AdjustmentReadPort input;","javax.sql.DataSource source;","java.lang.reflect.Method method;","Clock clock;","Thread worker;","System.out.println();","Class.forName();","java.net.URI uri;","import com.feelingpilates.calendario.servicio.ReservaService;"))
            assertThrows(AssertionError.class,()->pure(good+bad,name));
        for(String main:MAIN)assertThrows(AssertionError.class,()->caller("class Caller { "+main+" dependency; }"));
        assertThrows(AssertionError.class,()->caller("Class.forName(\""+PACKAGE+".EffectiveProgrammingComposer\");"));
    }
}
