package com.feelingpilates.pagos.ventas.aplicacion;

import com.feelingpilates.pagos.ventas.dominio.*;
import java.time.Instant;
import java.util.*;

public interface FuenteHistoricaCompra {
    Grupo obtener(String scopeKey);
    /** El verificador externo aporta explícitamente el sobre; raw financiero nunca constituye uno. */
    record Fuente(String raw, String rawHash, String referencia, UUID actorId, Instant evidenciaEn,
            ProvenienciaSnapshot.Origen origen, String reglaVersion, boolean verificada,
            OrdenVenta contrato, List<UUID> membership, ImporteMonetario total,
            List<String> faltantes, List<InformeBackfillSnapshot.Causa> causas) {
        public Fuente {
            Objects.requireNonNull(raw); ContenidoSnapshotCanonico.utf8(raw);
            if (!ContenidoSnapshotCanonico.sha256(raw).equals(rawHash)) throw new IllegalArgumentException("Fuente raw hash");
            ContenidoSnapshotCanonico.instante(evidenciaEn);
            membership=List.copyOf(membership); faltantes=List.copyOf(faltantes); causas=List.copyOf(causas);
            if (verificada) {
                PoliticaComercialSnapshot.texto(referencia); Objects.requireNonNull(actorId); Objects.requireNonNull(origen);
                if (!"PN14_S2_1".equals(reglaVersion)) throw new IllegalArgumentException("Regla fuente");
            }
        }
    }
    record Fila(UUID id, UUID clienteId, UUID grupoId, Integer numeroItem, long montoUnidadesMinimas,
            String monedaRaw, String raw, String rawHash, String observacionHash) {
        public Fila {
            Objects.requireNonNull(id); Objects.requireNonNull(clienteId); Objects.requireNonNull(monedaRaw);
            if (!ContenidoSnapshotCanonico.sha256(raw).equals(rawHash)
                    || !hashObservacion(id,clienteId,grupoId,numeroItem,montoUnidadesMinimas,monedaRaw).equals(observacionHash))
                throw new IllegalArgumentException("Fila histórica incoherente");
        }
        public static String hashObservacion(UUID id,UUID cliente,UUID grupo,Integer numero,long monto,String moneda) {
            Map<String,Object> m=new HashMap<>(); m.put("id",id); m.put("clienteId",cliente); m.put("grupoId",grupo);
            m.put("numeroItem",numero); m.put("monto",monto); m.put("monedaRaw",moneda);
            return ContenidoSnapshotCanonico.sha256(ContenidoSnapshotCanonico.pares(m));
        }
    }
    record Grupo(String scopeKey, List<Fila> filas, List<Fuente> fuentes, Instant evidenciaEn) {
        public Grupo { ContenidoSnapshotCanonico.validarScope(scopeKey); filas=List.copyOf(filas); fuentes=List.copyOf(fuentes);
            ContenidoSnapshotCanonico.instante(evidenciaEn); }
    }
}
