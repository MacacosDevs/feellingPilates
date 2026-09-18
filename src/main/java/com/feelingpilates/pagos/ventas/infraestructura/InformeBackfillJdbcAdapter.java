package com.feelingpilates.pagos.ventas.infraestructura;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public final class InformeBackfillJdbcAdapter implements InformeBackfillSnapshot.Puerto {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaccion;
    public InformeBackfillJdbcAdapter(JdbcTemplate jdbc,PlatformTransactionManager manager) {
        this.jdbc=Objects.requireNonNull(jdbc); transaccion=new TransactionTemplate(manager);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); transaccion.setTimeout(15);
    }
    @Override public InformeBackfillSnapshot registrar(InformeBackfillSnapshot i) {
        return transaccion.execute(s -> insertarEnTransaccion(jdbc,i));
    }
    static InformeBackfillSnapshot insertarEnTransaccion(JdbcTemplate jdbc,InformeBackfillSnapshot i) {
        jdbc.update("""
            insert into public.informe_backfill_snapshot (id,scope_key,fuente_payload_hash,estado,fuente_raw,
            fuente_raw_hash,fuentes_canonicas,faltantes_canonicos,causas_canonicas,compra_ids_canonicos,
            conteo_observado,total_observado_unidades_minimas,moneda_observada,evidencia_en,regla_version)
            values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) on conflict (scope_key,fuente_payload_hash) do nothing
            """,i.id(),i.scopeKey(),i.fuentePayloadHash(),i.estado().name(),i.fuenteRaw(),i.fuenteRawHash(),i.fuentesCanonicas(),
            ContenidoSnapshotCanonico.pares(Map.of("valores",i.faltantes())),ContenidoSnapshotCanonico.pares(Map.of("valores",i.causas())),
            ContenidoSnapshotCanonico.pares(Map.of("valores",i.compraIds())),i.conteoObservado(),
            i.totalObservadoUnidadesMinimas()==null?null:new java.math.BigDecimal(i.totalObservadoUnidadesMinimas()),
            i.monedaObservada(),Timestamp.from(i.evidenciaEn()),i.reglaVersion());
        var observado=jdbc.queryForObject("select * from public.informe_backfill_snapshot where scope_key=? and fuente_payload_hash=?",(r,n) -> {
            if(!r.getString("fuente_raw").equals(i.fuenteRaw()) || !r.getString("fuentes_canonicas").equals(i.fuentesCanonicas()))
                throw new RepositorioOrdenSnapshot.ConflictoSnapshot("Informe hash contradictorio");
            return leer(r);
        },i.scopeKey(),i.fuentePayloadHash());
        return observado;
    }
    static InformeBackfillSnapshot congelado(JdbcTemplate jdbc,String scope) {
        return jdbc.queryForObject("select * from public.informe_backfill_snapshot where scope_key=? and estado='CONGELADA'",(r,n) -> leer(r),scope);
    }
    private static InformeBackfillSnapshot leer(java.sql.ResultSet r) throws java.sql.SQLException {
            return new InformeBackfillSnapshot(r.getObject("id",UUID.class),r.getString("scope_key"),r.getString("fuente_payload_hash"),
                InformeBackfillSnapshot.Estado.valueOf(r.getString("estado")),r.getString("fuente_raw"),r.getString("fuente_raw_hash"),
                r.getString("fuentes_canonicas"),ContenidoSnapshotCanonico.leerRecord(r.getString("faltantes_canonicos"),Faltantes.class).valores(),
                ContenidoSnapshotCanonico.leerRecord(r.getString("causas_canonicas"),Causas.class).valores(),
                ContenidoSnapshotCanonico.leerRecord(r.getString("compra_ids_canonicos"),Ids.class).valores(),r.getInt("conteo_observado"),
                r.getBigDecimal("total_observado_unidades_minimas")==null?null:r.getBigDecimal("total_observado_unidades_minimas").toBigIntegerExact(),
                r.getString("moneda_observada"),r.getTimestamp("evidencia_en").toInstant(),r.getString("regla_version"));
    }
    public record Faltantes(List<String> valores) { }
    public record Causas(List<InformeBackfillSnapshot.Causa> valores) { }
    public record Ids(List<UUID> valores) { }
}
