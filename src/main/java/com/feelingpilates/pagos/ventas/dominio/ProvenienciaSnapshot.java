package com.feelingpilates.pagos.ventas.dominio;

import java.time.Instant;
import java.util.*;

public record ProvenienciaSnapshot(Origen origen, String referencia, UUID actorId, Instant evidenciaEn,
        String fuenteRaw, String fuenteRawHash, String reglaVersion, List<CampoFuente> campos) {
    public enum Origen { CONTRATO_CONTEMPORANEO_VERIFICADO, ARCHIVO_VERSIONADO_VERIFICADO, EVIDENCIA_COMERCIAL_VERIFICADA }
    public record CampoFuente(String fieldPath, String referencia, String fuenteHash) {
        public CampoFuente { PoliticaComercialSnapshot.texto(fieldPath); PoliticaComercialSnapshot.texto(referencia);
            ContenidoSnapshotCanonico.validarHash(fuenteHash); }
    }
    public ProvenienciaSnapshot {
        Objects.requireNonNull(origen); Objects.requireNonNull(actorId);
        PoliticaComercialSnapshot.texto(referencia); PoliticaComercialSnapshot.texto(fuenteRaw);
        ContenidoSnapshotCanonico.instante(evidenciaEn);
        if (!"PN14_S2_1".equals(reglaVersion) || !ContenidoSnapshotCanonico.sha256(fuenteRaw).equals(fuenteRawHash))
            throw new IllegalArgumentException("Provenance raw/regla incompatible");
        campos = campos.stream().sorted(Comparator.comparing(CampoFuente::fieldPath, ContenidoSnapshotCanonico.ORDEN_UTF8)).toList();
        if (campos.isEmpty() || campos.stream().map(CampoFuente::fieldPath).distinct().count() != campos.size())
            throw new IllegalArgumentException("Provenance por campo ausente/duplicada");
    }
    public void exigir(Collection<String> paths) {
        Set<String> observados = new HashSet<>(); campos.forEach(c -> observados.add(c.fieldPath()));
        if (!observados.containsAll(paths)) {
            Set<String> faltan = new TreeSet<>(paths); faltan.removeAll(observados);
            throw new IllegalArgumentException("FUENTE_AUSENTE: " + faltan);
        }
    }
}
