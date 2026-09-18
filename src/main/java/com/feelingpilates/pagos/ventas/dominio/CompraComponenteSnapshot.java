package com.feelingpilates.pagos.ventas.dominio;

import java.util.*;

public record CompraComponenteSnapshot(UUID id, UUID compraId, int numero, UUID actividadId,
        String nombreActividad, int cantidad, String politicaVersion) {
    public CompraComponenteSnapshot {
        Objects.requireNonNull(id); Objects.requireNonNull(compraId); Objects.requireNonNull(actividadId);
        PoliticaComercialSnapshot.texto(nombreActividad); PoliticaComercialSnapshot.texto(politicaVersion);
        if (numero <= 0 || cantidad <= 0 || !id.equals(ContenidoSnapshotCanonico.componenteId(compraId, numero)))
            throw new IllegalArgumentException("Componente identidad/cantidad inválida");
    }
}
