package com.feelingpilates.pagos.ventas.aplicacion;

import com.feelingpilates.pagos.ventas.dominio.*;
import java.time.Instant;
import java.util.*;

public record CompraHistoricaSnapshot(UUID ordenId, UUID clienteId, String scopeKey, ImporteMonetario total,
        Instant congeladoEn, String payloadHash, List<Compra> lineas) {
    public CompraHistoricaSnapshot { lineas=List.copyOf(lineas); }
    public static CompraHistoricaSnapshot de(OrdenVenta o) {
        return new CompraHistoricaSnapshot(o.id(),o.clienteId(),o.scopeKey(),o.total(),o.congeladoEn(),o.payloadHash(),o.compras());
    }
}
