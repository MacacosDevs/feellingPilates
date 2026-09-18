package com.feelingpilates.pagos.ventas.infraestructura;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.util.*;

/** Sin bean productivo: el caller entrega sobres ya verificados y un instante explícito. */
public final class FuenteHistoricaCompraJdbcAdapter implements FuenteHistoricaCompra {
    private final JdbcTemplate jdbc;
    private final Map<String,List<Fuente>> sobres;
    private final Instant evidenciaEn;
    public FuenteHistoricaCompraJdbcAdapter(JdbcTemplate jdbc, Map<String,List<Fuente>> sobres, Instant evidenciaEn) {
        this.jdbc=Objects.requireNonNull(jdbc); Map<String,List<Fuente>> copia=new HashMap<>();
        sobres.forEach((k,v) -> copia.put(k,List.copyOf(v))); this.sobres=Map.copyOf(copia);
        ContenidoSnapshotCanonico.instante(evidenciaEn); this.evidenciaEn=evidenciaEn;
    }
    @Override public Grupo obtener(String scope) { return new Grupo(scope,filas(jdbc,scope,false),sobres.getOrDefault(scope,List.of()),evidenciaEn); }
    static List<Fila> filas(JdbcTemplate jdbc,String scope,boolean lock) {
        ContenidoSnapshotCanonico.validarScope(scope); boolean grupo=scope.startsWith("LEGACY_GRUPO:");
        String where=grupo?"c.grupo_compra_id=?":"c.id=? and c.grupo_compra_id is null";
        return jdbc.query("select c.*,row_to_json(c)::text as fuente_raw from public.compra c where "+where+
                " order by c.id"+(lock?" for update of c":""),(r,n) -> {
            UUID id=r.getObject("id",UUID.class),cliente=r.getObject("usuario_id",UUID.class),g=r.getObject("grupo_compra_id",UUID.class);
            Integer numero=r.getObject("numero_item",Integer.class); long monto=r.getLong("monto_centavos"); String moneda=r.getString("moneda");
            String raw=r.getString("fuente_raw"); return new Fila(id,cliente,g,numero,monto,moneda,raw,ContenidoSnapshotCanonico.sha256(raw),
                    Fila.hashObservacion(id,cliente,g,numero,monto,moneda));
        },UUID.fromString(scope.substring(scope.indexOf(':')+1)));
    }
}
