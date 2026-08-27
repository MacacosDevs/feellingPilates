package com.feelingpilates.programacion.dominio;

public class ProgramacionInvarianteException extends RuntimeException {

    private final String codigo;
    private final ReferenciaOcurrencia referencia;

    public ProgramacionInvarianteException(
            String codigo, String detalle, ReferenciaOcurrencia referencia) {
        super(codigo + ": " + detalle);
        this.codigo = codigo;
        this.referencia = referencia;
    }

    public String getCodigo() {
        return codigo;
    }

    public ReferenciaOcurrencia getReferencia() {
        return referencia;
    }
}
