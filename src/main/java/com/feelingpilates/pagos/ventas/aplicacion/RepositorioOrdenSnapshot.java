package com.feelingpilates.pagos.ventas.aplicacion;

import com.feelingpilates.pagos.ventas.dominio.*;
import java.util.*;

public interface RepositorioOrdenSnapshot {
    CongelarOrdenSnapshot.Resultado congelar(OrdenVenta propuesta, EvidenciaScope evidencia);
    Optional<OrdenVenta> buscar(UUID ordenId, UUID clienteId);
    record EvidenciaScope(List<FuenteHistoricaCompra.Fila> filas, List<UUID> membership,
            ImporteMonetario totalAutoritativo, InformeBackfillSnapshot informe) {
        public EvidenciaScope {
            filas=List.copyOf(filas); membership=List.copyOf(membership); Objects.requireNonNull(totalAutoritativo);
            Objects.requireNonNull(informe);
            if (membership.isEmpty() || new HashSet<>(membership).size()!=membership.size())
                throw new IllegalArgumentException("Membership autoritativo ausente/duplicado");
        }
    }
    final class ConflictoSnapshot extends IllegalStateException {
        public ConflictoSnapshot(String causa) { super(causa); }
    }
}
