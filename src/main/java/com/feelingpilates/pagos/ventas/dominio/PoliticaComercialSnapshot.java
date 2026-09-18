package com.feelingpilates.pagos.ventas.dominio;

import java.util.Objects;

/** Términos explícitos del sobre histórico; este tipo no ejecuta derechos ni reembolsos. */
public record PoliticaComercialSnapshot(String esquema, String id, String version, String zonaNegocio,
        Vigencia vigencia, ReservaCancelacion reservaCancelacion, Recuperacion recuperacion,
        Reembolso reembolso, int unidadMonetariaExponente, Terminos terminos) {
    public enum Unidad { DIAS, MESES }
    public enum ExtensionAlcance { MISMA_ACTIVIDAD, TODAS_LAS_ACTIVIDADES_ACTIVAS }
    public enum ReembolsoAlcance { PRODUCTO_ENTERO }
    public enum Ancla { ACREDITACION_COMPLETADA }
    public enum Resolucion { ADMIN }
    public record Vigencia(Unidad unidad, int cantidad, ExtensionAlcance extensionAlcance) {
        public Vigencia { Objects.requireNonNull(unidad); Objects.requireNonNull(extensionAlcance);
            if (cantidad <= 0) throw new IllegalArgumentException("vigencia.cantidad"); }
    }
    public record ReservaCancelacion(int limitePostVencimientoDias, long anticipacionSegundos, int cuotaMensual) {
        public ReservaCancelacion { if (limitePostVencimientoDias < 0 || anticipacionSegundos < 0 || cuotaMensual < 0)
            throw new IllegalArgumentException("reservaCancelacion"); }
    }
    public record Recuperacion(Unidad unidad, int cantidad, String politicaId, String politicaVersion) {
        public Recuperacion { Objects.requireNonNull(unidad); texto(politicaId); texto(politicaVersion);
            if (cantidad <= 0) throw new IllegalArgumentException("recuperacion.cantidad"); }
    }
    public record Reembolso(ReembolsoAlcance alcance, boolean ventanaAdicional, String politicaId, String politicaVersion) {
        public Reembolso { Objects.requireNonNull(alcance); texto(politicaId); texto(politicaVersion);
            if (ventanaAdicional) throw new IllegalArgumentException("reembolso.ventanaAdicional"); }
    }
    public record Terminos(boolean saldoCeroExtiende, boolean acortaVigencia, boolean reviveExpirado,
            Ancla ancla, boolean limitesExclusivos, boolean igualdadCutoffValida,
            boolean cancelacionLiberaCapacidad, boolean tardiaConsume, boolean cuotaAgotadaConsume,
            boolean noAsistidaConsume, boolean estudioRestauraSinCuota, boolean asistenciaPendienteTimer,
            Resolucion asistenciaResolucion, boolean reintegroEsDinero, boolean recuperacionMismaActividad,
            boolean recuperacionDerechoNuevo, boolean refundExigeIntegridad, boolean refundExigeKBCERcero,
            boolean refundPermiteContencion, boolean refundPermiteExpiracion, String claveCuota) {
        public Terminos {
            if (saldoCeroExtiende || acortaVigencia || reviveExpirado || ancla != Ancla.ACREDITACION_COMPLETADA
                    || !limitesExclusivos || !igualdadCutoffValida || !cancelacionLiberaCapacidad
                    || !tardiaConsume || !cuotaAgotadaConsume || !noAsistidaConsume || !estudioRestauraSinCuota
                    || asistenciaPendienteTimer || asistenciaResolucion != Resolucion.ADMIN || reintegroEsDinero
                    || !recuperacionMismaActividad || !recuperacionDerechoNuevo || !refundExigeIntegridad
                    || !refundExigeKBCERcero || refundPermiteContencion || refundPermiteExpiracion
                    || !"clienteId+YearMonth(inicioSesion,zonaNegocio)+policyVersion".equals(claveCuota))
                throw new IllegalArgumentException("Términos incompatibles PN14_S2_1");
        }
    }
    public PoliticaComercialSnapshot {
        texto(id); texto(version);
        if (!"PN14_S2_1".equals(esquema) || !"America/Mexico_City".equals(zonaNegocio)
                || unidadMonetariaExponente < 0 || unidadMonetariaExponente > 9)
            throw new IllegalArgumentException("Política incompleta/incompatible");
        Objects.requireNonNull(vigencia); Objects.requireNonNull(reservaCancelacion);
        Objects.requireNonNull(recuperacion); Objects.requireNonNull(reembolso); Objects.requireNonNull(terminos);
    }
    public static void texto(String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Campo comercial ausente");
        ContenidoSnapshotCanonico.utf8(valor);
    }
}
