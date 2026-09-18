package com.feelingpilates.pagos.ventas.dominio;

import java.time.Instant;
import java.util.*;

public record OrdenVenta(UUID id, UUID clienteId, String scopeKey, ImporteMonetario total,
        List<Compra> compras, Instant congeladoEn, String payloadHash) {
    public OrdenVenta {
        Objects.requireNonNull(clienteId); Objects.requireNonNull(total); ContenidoSnapshotCanonico.instante(congeladoEn);
        if (!ContenidoSnapshotCanonico.ordenId(scopeKey).equals(id)) throw new IllegalArgumentException("Orden identidad inválida");
        compras = compras.stream().sorted(Comparator.comparingInt(Compra::numeroLinea)).toList();
        if (compras.isEmpty()) throw new IllegalArgumentException("Orden vacía");
        long suma=0; Set<UUID> ids=new HashSet<>();
        for (int i=0;i<compras.size();i++) {
            Compra c=compras.get(i);
            if (c.numeroLinea()!=i+1 || !ids.add(c.id()) || !c.ordenId().equals(id) || !c.clienteId().equals(clienteId)
                    || !c.precioVenta().monedaIso().equals(total.monedaIso()) || !c.congeladoEn().equals(congeladoEn))
                throw new IllegalArgumentException("Orden membership/cliente/moneda inválido");
            suma=Math.addExact(suma,c.precioVenta().unidadesMinimas());
            c.proveniencia().exigir(List.of("scopeKey","membership","numeroLineas","total.unidadesMinimas","total.monedaIso"));
        }
        if (suma!=total.unidadesMinimas()) throw new IllegalArgumentException("Total inválido");
        if (scopeKey.startsWith("LEGACY_COMPRA:") && (compras.size()!=1 || !scopeKey.endsWith(compras.getFirst().id().toString())))
            throw new IllegalArgumentException("Scope individual inconsistente");
        String h=ContenidoSnapshotCanonico.sha256(ContenidoSnapshotCanonico.pares(Map.of("id",id,"clienteId",clienteId,
                "scopeKey",scopeKey,"total",total,"compras",compras,"congeladoEn",congeladoEn)));
        if (payloadHash!=null && !payloadHash.equals(h)) throw new IllegalArgumentException("Orden hash incoherente");
        payloadHash=h;
    }
}
