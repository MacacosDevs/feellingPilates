package com.feelingpilates.pagos.ventas;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;

class VentasSnapshotArquitecturaTest {
    static final String BASE="com/feelingpilates/pagos/ventas/";
    @Test void T17_importsYBytecodeDominioJavaAppInwardInfraSinWiringProductivo() throws Exception {
        try(Stream<Path> paths=Files.walk(Path.of("target/classes",BASE))) {
            var classes=paths.filter(p -> p.toString().endsWith(".class")).toList();assertThat(classes).hasSizeGreaterThan(18);
            for(Path p:classes) {
                String nombre=Path.of("target/classes").relativize(p).toString();Set<String> refs=referencias(p);
                for(String ref:refs) {
                    if(ref.startsWith("["))continue;
                    if(nombre.startsWith(BASE+"dominio/")) assertThat(ref).as(nombre+" -> "+ref).matches("java/.*|"+BASE+"dominio/.*");
                    if(nombre.startsWith(BASE+"aplicacion/")) assertThat(ref).as(nombre+" -> "+ref).matches("java/.*|"+BASE+"(dominio|aplicacion)/.*");
                    if(nombre.startsWith(BASE+"infraestructura/")) assertThat(ref).as(nombre+" -> "+ref).matches("java/.*|javax/sql/.*|org/springframework/(jdbc|transaction)/.*|"+BASE+".*");
                }
                String bytecode=new String(Files.readAllBytes(p),java.nio.charset.StandardCharsets.ISO_8859_1);
                assertThat(bytecode).doesNotContain("jakarta/persistence/Entity","org/springframework/stereotype/Component",
                    "org/springframework/stereotype/Service","org/springframework/context/annotation/Bean",
                    "org/springframework/context/annotation/Configuration","Scheduled","Controller");
            }
        }
        try(Stream<Path> sources=Files.walk(Path.of("src/main/java",BASE))) {
            var main=sources.filter(p -> p.toString().endsWith(".java")).toList();assertThat(main).hasSize(18);
            for(Path p:main) {String s=Files.readString(p);assertThat(s).doesNotContain("com.feelingpilates.pagos.entidad","com.stripe","@Component","@Service","@Bean","@Configuration","@Scheduled","@RestController","@Entity");}
        }
    }
    @Test void T17_ningunConsumerProductivoNuevoNiJPACompraCompetidora() throws Exception {
        try(Stream<Path> paths=Files.walk(Path.of("target/classes/com/feelingpilates"))) {
            for(Path p:paths.filter(x -> x.toString().endsWith(".class") && !x.toString().contains("/pagos/ventas/")).toList())
                assertThat(referencias(p)).as(p.toString()).noneMatch(s -> s.startsWith(BASE));
        }
        try(Stream<Path> paths=Files.walk(Path.of("src/main/java"))) {
            var mappings=paths.filter(p -> p.toString().endsWith(".java")).filter(p -> {
                try{return Files.readString(p).contains("@Table(name = \"compra\")");}catch(IOException ex){throw new UncheckedIOException(ex);}
            }).toList();assertThat(mappings).containsExactly(Path.of("src/main/java/com/feelingpilates/pagos/entidad/Compra.java"));
        }
    }
    static Set<String> referencias(Path p) throws IOException {
        try(DataInputStream in=new DataInputStream(Files.newInputStream(p))) {
            assertThat(in.readInt()).isEqualTo(0xCAFEBABE);in.readUnsignedShort();in.readUnsignedShort();
            int n=in.readUnsignedShort();String[] utf=new String[n];int[] indices=new int[n];
            for(int i=1;i<n;i++)switch(in.readUnsignedByte()) {
                case 1 -> utf[i]=in.readUTF();case 7 -> indices[i]=in.readUnsignedShort();
                case 3,4,9,10,11,12,17,18 -> in.skipNBytes(4);case 5,6 -> {in.skipNBytes(8);i++;}
                case 8,16,19,20 -> in.skipNBytes(2);case 15 -> in.skipNBytes(3);default -> throw new IOException("CP tag desconocido");
            }
            Set<String> refs=new HashSet<>();for(int idx:indices)if(idx!=0)refs.add(utf[idx]);
            // Descriptores y firmas también revelan dependencias sin una entrada CONSTANT_Class.
            var pattern=java.util.regex.Pattern.compile("L([a-zA-Z_$][a-zA-Z0-9_$/]*)(?:[;<])");
            for(String s:utf)if(s!=null){var m=pattern.matcher(s);while(m.find())if(m.group(1).contains("/"))refs.add(m.group(1));}
            return refs;
        }
    }
}
