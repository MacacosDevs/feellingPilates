package com.feelingpilates.pagos.ventas.dominio;

import java.time.Instant;
import java.util.*;

public record Compra(UUID id, UUID ordenId, UUID clienteId, int numeroLinea, UUID productoFuenteId,
        String nombreProducto, TipoProducto tipoProducto, ImporteMonetario precioVenta,
        PoliticaComercialSnapshot politica, List<CompraComponenteSnapshot> componentes,
        ProvenienciaSnapshot proveniencia, Instant congeladoEn, String contratoHash) {
    public enum TipoProducto { CLASE_INDIVIDUAL, PAQUETE }
    public Compra {
        Objects.requireNonNull(id); Objects.requireNonNull(ordenId); Objects.requireNonNull(clienteId);
        PoliticaComercialSnapshot.texto(nombreProducto); Objects.requireNonNull(tipoProducto);
        Objects.requireNonNull(precioVenta); Objects.requireNonNull(politica); Objects.requireNonNull(proveniencia);
        ContenidoSnapshotCanonico.instante(congeladoEn);
        componentes = componentes.stream().sorted(Comparator.comparingInt(CompraComponenteSnapshot::numero)).toList();
        if (numeroLinea <= 0 || componentes.isEmpty() || precioVenta.exponenteIso() != politica.unidadMonetariaExponente())
            throw new IllegalArgumentException("Compra incompleta");
        Set<UUID> actividades = new HashSet<>();
        for (int i=0;i<componentes.size();i++) {
            var c = componentes.get(i);
            if (c.numero()!=i+1 || !c.compraId().equals(id) || !actividades.add(c.actividadId())
                    || !c.politicaVersion().equals(politica.version())) throw new IllegalArgumentException("Composición inválida");
        }
        Map<String,Object> m = datos(id,ordenId,clienteId,numeroLinea,productoFuenteId,nombreProducto,tipoProducto,
                precioVenta,politica,componentes,proveniencia,congeladoEn);
        Map<String,Object> comerciales = new HashMap<>(m); comerciales.remove("proveniencia"); comerciales.remove("congeladoEn");
        proveniencia.exigir(ContenidoSnapshotCanonico.paths(comerciales));
        if (productoFuenteId == null) proveniencia.exigir(List.of("productoIdentidadAlternativa"));
        String calculado = ContenidoSnapshotCanonico.sha256(ContenidoSnapshotCanonico.pares(m));
        if (contratoHash != null && !contratoHash.equals(calculado)) throw new IllegalArgumentException("Contrato hash incoherente");
        contratoHash = calculado;
    }
    private static Map<String,Object> datos(UUID id, UUID ordenId, UUID clienteId, int numeroLinea, UUID productoFuenteId,
            String nombreProducto, TipoProducto tipoProducto, ImporteMonetario precioVenta, PoliticaComercialSnapshot politica,
            List<CompraComponenteSnapshot> componentes, ProvenienciaSnapshot proveniencia, Instant congeladoEn) {
        Map<String,Object> m = new HashMap<>(); m.put("id",id); m.put("ordenId",ordenId); m.put("clienteId",clienteId);
        m.put("numeroLinea",numeroLinea); m.put("productoFuenteId",productoFuenteId); m.put("nombreProducto",nombreProducto);
        m.put("tipoProducto",tipoProducto); m.put("precioVenta",precioVenta); m.put("politica",politica);
        m.put("componentes",componentes); m.put("proveniencia",proveniencia); m.put("congeladoEn",congeladoEn); return m;
    }
}
