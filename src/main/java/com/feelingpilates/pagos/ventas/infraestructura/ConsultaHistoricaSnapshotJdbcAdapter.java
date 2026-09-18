package com.feelingpilates.pagos.ventas.infraestructura;

import com.feelingpilates.pagos.ventas.aplicacion.*;
import com.feelingpilates.pagos.ventas.dominio.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;

public final class ConsultaHistoricaSnapshotJdbcAdapter implements ConsultaHistoricaSnapshot {
    private final JdbcTemplate jdbc;
    public ConsultaHistoricaSnapshotJdbcAdapter(JdbcTemplate jdbc) { this.jdbc=Objects.requireNonNull(jdbc); }
    private static final String SQL="""
        select distinct o.id,o.cliente_id,o.congelado_en,o.payload_canonico,o.payload_hash
        from public.orden_venta o
        join public.compra c on c.orden_venta_id=o.id and c.cliente_snapshot_id=o.cliente_id
        join public.compra_componente_snapshot s on s.compra_id=c.id and s.cliente_id=c.cliente_snapshot_id
        where o.estado_fundacion='CONGELADA' and o.cliente_id=?
        """;
    @Override public Optional<CompraHistoricaSnapshot> buscar(UUID cliente,UUID orden) {
        return consultar(cliente,orden).stream().findFirst();
    }
    @Override public List<CompraHistoricaSnapshot> listar(UUID cliente) { return consultar(cliente,null); }
    private List<CompraHistoricaSnapshot> consultar(UUID cliente,UUID orden) {
        Objects.requireNonNull(cliente);
        String sql=SQL+(orden==null?"":" and o.id=?")+" order by o.congelado_en desc,o.id asc";
        Object[] args=orden==null?new Object[]{cliente}:new Object[]{cliente,orden};
        return List.copyOf(jdbc.query(sql,(r,n) -> CompraHistoricaSnapshot.de(ContenidoSnapshotCanonico.leerOrden(
                r.getString("payload_canonico"),r.getString("payload_hash"))),args));
    }
}
