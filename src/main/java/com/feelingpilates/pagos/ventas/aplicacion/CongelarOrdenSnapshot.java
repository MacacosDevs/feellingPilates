package com.feelingpilates.pagos.ventas.aplicacion;

import com.feelingpilates.pagos.ventas.dominio.*;
import java.util.*;

/** Comando interno sin wiring productivo. El instante viene capturado por la autoridad backend. */
public final class CongelarOrdenSnapshot {
    private final RepositorioOrdenSnapshot repositorio;
    public CongelarOrdenSnapshot(RepositorioOrdenSnapshot repositorio) { this.repositorio=Objects.requireNonNull(repositorio); }
    public record Entrada(OrdenVenta orden, RepositorioOrdenSnapshot.EvidenciaScope evidencia) {
        public Entrada { Objects.requireNonNull(orden); Objects.requireNonNull(evidencia); }
    }
    public record Resultado(OrdenVenta orden, boolean replay, InformeBackfillSnapshot informe) {
        public Resultado { Objects.requireNonNull(orden); }
        public Resultado(OrdenVenta orden,boolean replay) { this(orden,replay,null); }
    }
    public Resultado ejecutar(Entrada entrada) {
        OrdenVenta o=entrada.orden(); var e=entrada.evidencia();
        if (!new HashSet<>(e.membership()).equals(new HashSet<>(o.compras().stream().map(Compra::id).toList()))
                || !e.totalAutoritativo().equals(o.total()) || e.filas().size()!=o.compras().size()
                || !e.informe().scopeKey().equals(o.scopeKey()))
            throw new IllegalArgumentException("GRUPO_INCOMPLETO");
        return repositorio.congelar(o,e);
    }
}
