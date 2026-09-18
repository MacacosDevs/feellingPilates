package com.feelingpilates.pagos.ventas.dominio;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.nio.*;
import java.nio.charset.*;
import java.security.*;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

/** Encoder recursivo: claves UTF8, tipo explícito y longitud en bytes, incluyendo LF final. */
public final class ContenidoSnapshotCanonico {
    private ContenidoSnapshotCanonico() { }
    public static final Comparator<String> ORDEN_UTF8 = (a,b) -> Arrays.compareUnsigned(utf8(a), utf8(b));
    public static byte[] utf8(String s) {
        Objects.requireNonNull(s);
        if(s.indexOf(0)>=0) throw new IllegalArgumentException("NUL no representable en texto PostgreSQL");
        try { ByteBuffer b = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(s));
            byte[] r = new byte[b.remaining()]; b.get(r); return r;
        } catch (CharacterCodingException e) { throw new IllegalArgumentException("Unicode no representable", e); }
    }
    public static String instante(Instant s) {
        Objects.requireNonNull(s);
        if (s.getNano() % 1000 != 0 || s.isBefore(Instant.parse("0001-01-01T00:00:00Z"))
                || s.isAfter(Instant.parse("9999-12-31T23:59:59.999999Z")))
            throw new IllegalArgumentException("Instant UTC6 no representable");
        return new DateTimeFormatterBuilder().appendInstant(6).toFormatter().format(s);
    }
    public static String sha256(String s) { return HexFormat.of().formatHex(digest(utf8(s))); }
    public static byte[] digest(byte[] bytes) {
        try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static void validarHash(String s) {
        if (s == null || !s.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Hash inválido");
    }
    public static UUID ordenId(String scope) { validarScope(scope); return UUID.nameUUIDFromBytes(utf8("PN14_S2_1:ORDEN:" + scope)); }
    public static UUID componenteId(UUID compra, int n) { return UUID.nameUUIDFromBytes(utf8("PN14_S2_1:COMPONENTE:" + compra + ":" + n)); }
    public static void validarScope(String s) {
        if (s == null || !s.matches("LEGACY_(GRUPO|COMPRA):[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
            throw new IllegalArgumentException("Scope inválido");
        UUID.fromString(s.substring(s.indexOf(':')+1));
    }
    public static long advisory(String scope) { return ByteBuffer.wrap(digest(utf8(scope))).getLong(); }
    public static String pares(Map<String,?> m) {
        StringBuilder b = new StringBuilder();
        m.keySet().stream().sorted(ORDEN_UTF8).forEach(k -> {
            String v = tipado(m.get(k)); b.append(utf8(k).length).append(':').append(k)
                .append(utf8(v).length).append(':').append(v).append('\n');
        }); return b.toString();
    }
    public static String tipado(Object o) {
        if (o == null) return "NULL:~NULL";
        if (o instanceof String s) return "STRING:" + s;
        if (o instanceof UUID) return "UUID:" + o;
        if (o instanceof Long) return "LONG:" + o;
        if (o instanceof Integer || o instanceof Short) return "INT:" + o;
        if (o instanceof Boolean) return "BOOL:" + o;
        if (o instanceof Instant s) return "INSTANT:" + instante(s);
        if (o instanceof Enum<?> e) return "ENUM:" + e.name();
        if (o instanceof List<?> l) {
            Map<String,Object> m = new HashMap<>(); for (int i=0;i<l.size();i++) m.put(Integer.toString(i+1),l.get(i));
            return "LIST:" + pares(m);
        }
        if (o instanceof Map<?,?> m) {
            Map<String,Object> r = new HashMap<>(); m.forEach((k,v) -> r.put((String)k,v)); return "OBJECT:" + pares(r);
        }
        if (o.getClass().isRecord()) return "OBJECT:" + pares(campos(o, Set.of()));
        throw new IllegalArgumentException("Tipo no canónico: " + o.getClass());
    }
    public static Map<String,Object> campos(Object record, Set<String> excluir) {
        Map<String,Object> m = new HashMap<>();
        try { for (RecordComponent c: record.getClass().getRecordComponents())
            if (!excluir.contains(c.getName())) m.put(c.getName(), c.getAccessor().invoke(record));
        } catch (ReflectiveOperationException e) { throw new IllegalArgumentException(e); }
        return m;
    }
    public static String politica(PoliticaComercialSnapshot p) { return pares(campos(p, Set.of())); }
    public static String contrato(Compra c) { return pares(campos(c, Set.of("contratoHash"))); }
    public static String orden(OrdenVenta o) { return pares(campos(o, Set.of("payloadHash"))); }
    public static Set<String> paths(Map<String,?> valores) {
        Set<String> r = new TreeSet<>(ORDEN_UTF8); valores.forEach((k,v) -> paths(k,v,r)); return Set.copyOf(r);
    }
    private static void paths(String p, Object v, Set<String> r) {
        if (v != null && v.getClass().isRecord()) campos(v, Set.of()).forEach((k,x) -> paths(p+"."+k,x,r));
        else if (v instanceof List<?> l) { for (int i=0;i<l.size();i++) paths(p+"["+(i+1)+"]",l.get(i),r); }
        else r.add(p);
    }
    private record Valor(String tipo, Object contenido) { }
    /** Decoder estricto para proyecciones JDBC; no acepta representación alternativa del mismo valor. */
    public static <T> T leerRecord(String canonico, Class<T> clase) {
        Object objeto=convertir(new Valor("OBJECT",leerPares(canonico)),clase);
        return clase.cast(objeto);
    }
    private static Map<String,Valor> leerPares(String canon) {
        byte[] bytes=utf8(canon); int[] pos={0}; Map<String,Valor> m=new LinkedHashMap<>(); String anterior=null;
        while(pos[0]<bytes.length) {
            String k=segmento(bytes,pos),v=segmento(bytes,pos);
            if(pos[0]>=bytes.length || bytes[pos[0]++]!='\n' || anterior!=null && ORDEN_UTF8.compare(anterior,k)>=0)
                throw new IllegalArgumentException("Canon orden/LF/duplicado");
            anterior=k; int dos=v.indexOf(':'); if(dos<0) throw new IllegalArgumentException("Tipo ausente");
            String tipo=v.substring(0,dos),contenido=v.substring(dos+1);
            Object x=switch(tipo) {
                case "NULL" -> { if(!contenido.equals("~NULL")) throw new IllegalArgumentException("NULL"); yield null; }
                case "STRING","ENUM" -> contenido;
                case "UUID" -> { UUID u=UUID.fromString(contenido); if(!u.toString().equals(contenido)) throw new IllegalArgumentException("UUID"); yield u; }
                case "INT" -> { int n=Integer.parseInt(contenido); if(!Integer.toString(n).equals(contenido)) throw new IllegalArgumentException("INT"); yield n; }
                case "LONG" -> { long n=Long.parseLong(contenido); if(!Long.toString(n).equals(contenido)) throw new IllegalArgumentException("LONG"); yield n; }
                case "BOOL" -> { if(!Set.of("true","false").contains(contenido)) throw new IllegalArgumentException("BOOL"); yield Boolean.valueOf(contenido); }
                case "INSTANT" -> { Instant s=Instant.parse(contenido); if(!instante(s).equals(contenido)) throw new IllegalArgumentException("INSTANT"); yield s; }
                case "OBJECT","LIST" -> leerPares(contenido);
                default -> throw new IllegalArgumentException("Tipo no permitido");
            }; m.put(k,new Valor(tipo,x));
        } return m;
    }
    private static String segmento(byte[] b,int[] p) {
        int desde=p[0]; while(p[0]<b.length && b[p[0]]>='0' && b[p[0]]<='9') p[0]++;
        if(p[0]==desde || p[0]>=b.length || b[p[0]++]!=':' || p[0]-desde>2 && b[desde]=='0')
            throw new IllegalArgumentException("Longitud canónica");
        int n=Integer.parseInt(new String(b,desde,p[0]-desde-1,StandardCharsets.US_ASCII));
        if(n<0 || n>b.length-p[0]) throw new IllegalArgumentException("Longitud canónica");
        String s=new String(b,p[0],n,StandardCharsets.UTF_8); if(!Arrays.equals(utf8(s),Arrays.copyOfRange(b,p[0],p[0]+n)))
            throw new IllegalArgumentException("UTF8"); p[0]+=n; return s;
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private static Object convertir(Valor v,Type type) {
        if(v.tipo().equals("NULL")) { if(type instanceof Class<?> c && c.isPrimitive()) throw new IllegalArgumentException("NULL primitivo"); return null; }
        if(type instanceof ParameterizedType p && p.getRawType()==List.class) {
            if(!v.tipo().equals("LIST")) throw new IllegalArgumentException("LIST esperado");
            Map<String,Valor> m=(Map<String,Valor>)v.contenido(); List<Object> l=new ArrayList<>();
            for(int i=1;i<=m.size();i++) { Valor x=m.get(Integer.toString(i)); if(x==null) throw new IllegalArgumentException("LIST gaps"); l.add(convertir(x,p.getActualTypeArguments()[0])); }
            return List.copyOf(l);
        }
        Class<?> c=(Class<?>)type;
        String esperado=c==String.class?"STRING":c==UUID.class?"UUID":c==Instant.class?"INSTANT":c==long.class||c==Long.class?"LONG":
                c==int.class||c==Integer.class?"INT":c==boolean.class||c==Boolean.class?"BOOL":c.isEnum()?"ENUM":c.isRecord()?"OBJECT":"";
        if(!v.tipo().equals(esperado)) throw new IllegalArgumentException("Tipo/campo incompatible");
        if(c.isEnum()) return Enum.valueOf((Class)c,(String)v.contenido());
        if(!c.isRecord()) return v.contenido();
        Map<String,Valor> m=(Map<String,Valor>)v.contenido(); RecordComponent[] campos=c.getRecordComponents();
        if(m.size()!=campos.length) throw new IllegalArgumentException("Campos canónicos incompletos/extra");
        Object[] args=new Object[campos.length]; Class<?>[] tipos=new Class<?>[campos.length];
        for(int i=0;i<campos.length;i++) { Valor x=m.get(campos[i].getName()); if(x==null) throw new IllegalArgumentException("Campo ausente");
            args[i]=convertir(x,campos[i].getGenericType()); tipos[i]=campos[i].getType(); }
        try { return c.getConstructor(tipos).newInstance(args); }
        catch(ReflectiveOperationException e) { throw new IllegalArgumentException("Record canónico inválido",e); }
    }
    public static OrdenVenta leerOrden(String canon, String hash) {
        // El contenido contractual excluye su propio hash. Añadirlo sólo para construir el record.
        String k="payloadHash";
        Map<String,Valor> m=leerPares(canon); m.put(k,new Valor("STRING",hash));
        OrdenVenta o=(OrdenVenta)convertir(new Valor("OBJECT",m),OrdenVenta.class);
        if(!orden(o).equals(canon)) throw new IllegalArgumentException("Orden canon no normalizado"); return o;
    }
}
