package com.feelingpilates.transicion.programacion.comparison.v2;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ComparisonArchitectureV2Test {
    static final String PACKAGE="com.feelingpilates.transicion.programacion.comparison.v2";
    static final List<String> MAIN=List.of("ComparisonInputV2","ComparisonCandidateEvidenceV2","ComparisonEvaluationV2","ComparisonResultV2","ComparisonCompatibilityV2","LegacyCandidateGeneratorV2","OmissionAwareAdjustmentClassifierV2","ComparisonCanonicalizerV2","ComparisonFailureV2");
    static final List<String> TEST=List.of("ComparisonTestFixturesV2","LegacyCandidateGeneratorV2Test","OmissionAwareAdjustmentClassifierV2Test","ComparisonInvariantV2Test","ComparisonDeterminismV2Test","ComparisonArchitectureV2Test","ComparisonRuntimeIsolationV2Test");
    static Set<String> paths() {
        var out=new HashSet<String>();MAIN.forEach(n->out.add("src/main/java/"+PACKAGE.replace('.','/')+"/"+n+".java"));
        TEST.forEach(n->out.add("src/test/java/"+PACKAGE.replace('.','/')+"/"+n+".java"));return out;
    }
    static Set<String> symbols(String code) {
        var out=new HashSet<String>();var matcher=Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*").matcher(code.replaceAll("(?s)/\\*.*?\\*/"," ").replaceAll("(?m)//.*$"," "));
        while(matcher.find())out.add(matcher.group());return out;
    }
    static void caller(String code) {
        assertTrue(Collections.disjoint(symbols(code),new HashSet<>(MAIN)),"outside caller into comparison");assertFalse(code.contains(PACKAGE),"dynamic/FQ comparison caller");
    }
    static void pure(String code,String name) {
        String withoutComments=code.replaceAll("(?s)/\\*.*?\\*/"," ").replaceAll("(?m)//.*$"," ");
        String body=withoutComments.replaceAll("\\b(?:package|import)\\s+(?:static\\s+)?[\\w.*]+\\s*;"," ");
        assertFalse(java.util.regex.Pattern.compile("\\bcom\\s*\\.\\s*feelingpilates\\b").matcher(body).find(),"no fully qualified project routes");
        String effects=withoutComments.replace("java.io.ByteArrayOutputStream", "MemoryBuffer");
        assertFalse(effects.contains("java.io"),"only explicit memory byte buffer I/O type");
        assertFalse(effects.contains("java.nio.channels"),"no channels");
        for(String effect:List.of("FileInputStream","FileOutputStream","FileReader","FileWriter","RandomAccessFile","ObjectInputStream","ObjectOutputStream","FileChannel","SocketChannel","PrintStream","PrintWriter"))
            assertFalse(java.util.regex.Pattern.compile("\\b"+effect+"\\b").matcher(effects).find(),"no I/O sink "+effect);
        assertTrue(Pattern.compile("(?m)^\\s*package\\s+com\\.feelingpilates\\.transicion\\.programacion\\.comparison\\.v2\\s*;").matcher(code.replaceAll("(?s)/\\*.*?\\*/"," ").replaceAll("(?m)//.*$"," ")).find());assertTrue(Pattern.compile("public\\s+(?:final\\s+class|record)\\s+"+name+"\\b").matcher(code).find());
        for(String forbidden:List.of("org.springframework","jakarta.persistence","javax.persistence","org.hibernate","java.sql","javax.sql","java.net","java.nio.file","java.lang.reflect","java.util.concurrent",
                ".adapter.",".repositorio.",".servicio.",".controlador.",".entidad.",".read.*","Class.forName","getDeclared","UUID.randomUUID",".now(","::now"))assertFalse(code.contains(forbidden),forbidden);
        for(String token:List.of("Component","Service","Bean","Configuration","Scheduled","EventListener","Transactional","Repository","EntityManager","DataSource","DriverManager","JdbcTemplate",
                "Thread","Clock","Random","System","Runtime","ProcessBuilder","ClassLoader","Proxy","ReadPort","NominalJpaReader","AdjustmentJpaReader","EffectiveProgrammingComposer"))
            assertFalse(symbols(code).contains(token),token);
        var permitted=new HashSet<String>();
        if(name.equals("ComparisonInputV2"))permitted.addAll(Set.of("NominalProgrammingReadSet","AdjustmentReadSet","EffectiveProgrammingCompositionResult"));
        if(name.equals("ComparisonCompatibilityV2"))permitted.add("EffectiveProgrammingCompositionResult");
        if(name.equals("ComparisonEvaluationV2"))permitted.add("EffectiveCompositionOmission");
        if(name.equals("OmissionAwareAdjustmentClassifierV2"))permitted.addAll(Set.of("NominalBackingSnapshot","AdjustmentBackingSnapshot","EffectiveProgrammingCompositionResult","EffectiveCompositionInput","EffectiveCompositionEnvelope",
                "EffectiveCompositionBacking","EffectiveCompositionOmission","EffectiveCompositionSuppression","EffectiveValidityEvidence"));
        var imports=Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.*]+)\\s*;").matcher(code);
        while(imports.find()) {
            String edge=imports.group(1),simple=edge.substring(edge.lastIndexOf('.')+1);
            assertTrue(edge.startsWith("java.")||edge.startsWith(PACKAGE+".")||edge.equals("com.feelingpilates.programacion.dominio.ReferenciaOcurrencia")||
                    edge.startsWith("com.feelingpilates.transicion.programacion.detector.")||permitted.contains(simple)&&edge.equals(
                        simple.startsWith("Nominal")?"com.feelingpilates.transicion.programacion.read."+simple:
                        simple.startsWith("Adjustment")?"com.feelingpilates.transicion.programacion.r4.read."+simple:
                        "com.feelingpilates.transicion.programacion.r5.composition."+simple),"unapproved import "+edge);
        }
    }
    @Test void exactNineMainSevenTestInventoryAndNoProductionCaller() throws Exception {
        var actual=new HashSet<String>();for(String root:List.of("src/main/java/","src/test/java/"))try(var files=Files.walk(Path.of(root+PACKAGE.replace('.','/')))) {
            files.filter(p->p.toString().endsWith(".java")).forEach(p->actual.add(p.toString()));}
        assertEquals(paths(),actual);assertEquals(16,actual.size());
        assertEquals("cb95373a6ffefaf882833bae8b5abbcd375ebf8abc3343a3917c1dc08b439461",ComparisonTestFixturesV2.sha(ComparisonTestFixturesV2.bytes(String.join("\n",actual.stream().sorted().toList())+"\n")));
        try(var files=Files.walk(Path.of("src/main/java"))) {
            for(var p:files.filter(x->x.toString().endsWith(".java")).toList()) {
                String code=Files.readString(p);if(paths().contains(p.toString()))pure(code,p.getFileName().toString().replace(".java",""));else caller(code);
            }
        }
    }
    @Test void purityInventoryAndCallerNegativeControlsAreReal() {
        String good="package "+PACKAGE+"; public final class ComparisonCompatibilityV2 {}";pure(good,"ComparisonCompatibilityV2");
        for(String bad:List.of("@Component","import java.sql.Connection;","Class.forName();","new Thread();","import org.springframework.context.Bean;","EffectiveProgrammingComposer composer;","import productivo.Other;","import java.io.FileInputStream;","new java.io.FileInputStream(\"x\");","com.feelingpilates.transicion.programacion.read.NominalProgrammingReadSet input;"))
            assertThrows(AssertionError.class,()->pure(good+"\n"+bad,"ComparisonCompatibilityV2"));
        assertThrows(AssertionError.class,()->pure("package productivo; /* package "+PACKAGE+"; */ public final class ComparisonCompatibilityV2 {}","ComparisonCompatibilityV2"));
        for(String name:MAIN)assertThrows(AssertionError.class,()->caller("class Caller { "+name+" dependency; }"));
        assertThrows(AssertionError.class,()->caller("Class.forName(\""+PACKAGE+".ComparisonCompatibilityV2\");"));
        var missing=new HashSet<>(paths());missing.remove(missing.iterator().next());assertThrows(AssertionError.class,()->assertEquals(paths(),missing));
        var extra=new HashSet<>(paths());extra.add("extra");assertThrows(AssertionError.class,()->assertEquals(paths(),extra));
    }
}
