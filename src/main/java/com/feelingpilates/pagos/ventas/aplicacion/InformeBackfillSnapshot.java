package com.feelingpilates.pagos.ventas.aplicacion;

import com.feelingpilates.pagos.ventas.dominio.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

public record InformeBackfillSnapshot(UUID id, String scopeKey, String fuentePayloadHash, Estado estado,
        String fuenteRaw, String fuenteRawHash, String fuentesCanonicas, List<String> faltantes,
        List<Causa> causas, List<UUID> compraIds, int conteoObservado, BigInteger totalObservadoUnidadesMinimas,
        String monedaObservada, Instant evidenciaEn, String reglaVersion) {
    public enum Estado { CONGELADA, REPLAY, REQUIERE_REVISION }
    public enum Causa { FUENTE_AUSENTE, FUENTE_CONTRADICTORIA, GRUPO_INCOMPLETO, CLIENTE_INCONSISTENTE,
        MONEDA_INCONSISTENTE, NUMERACION_INVALIDA, IMPORTE_INVALIDO, PAYLOAD_CONTRADICTORIO }
    public interface Puerto { InformeBackfillSnapshot registrar(InformeBackfillSnapshot informe); }
    public InformeBackfillSnapshot {
        Objects.requireNonNull(id); ContenidoSnapshotCanonico.validarScope(scopeKey); Objects.requireNonNull(estado);
        ContenidoSnapshotCanonico.validarHash(fuentePayloadHash);
        Objects.requireNonNull(fuenteRaw); Objects.requireNonNull(fuentesCanonicas);
        if (!ContenidoSnapshotCanonico.sha256(fuenteRaw).equals(fuenteRawHash) || conteoObservado<0
                || !"PN14_S2_1".equals(reglaVersion)) throw new IllegalArgumentException("Informe incoherente");
        faltantes=faltantes.stream().distinct().sorted(ContenidoSnapshotCanonico.ORDEN_UTF8).toList();
        causas=causas.stream().distinct().sorted().toList(); compraIds=compraIds.stream().distinct().sorted().toList();
        ContenidoSnapshotCanonico.instante(evidenciaEn);
    }
    public static UUID identidad(String scope,String hash) {
        return UUID.nameUUIDFromBytes(ContenidoSnapshotCanonico.utf8("PN14_S2_1:INFORME:"+scope+":"+hash));
    }
}
