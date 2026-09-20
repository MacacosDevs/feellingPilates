package com.feelingpilates.programacion.dominio;

import java.time.LocalDate;
import java.util.UUID;

public record ReferenciaOcurrencia(Tipo tipo, UUID id, LocalDate fecha)
        implements Comparable<ReferenciaOcurrencia> {

    public enum Tipo { SERIE_ASIGNACION, AJUSTE }

    @Override
    public int compareTo(ReferenciaOcurrencia otra) {
        int porTipo = tipo.compareTo(otra.tipo);
        if (porTipo != 0) {
            return porTipo;
        }
        int porId = id.compareTo(otra.id);
        return porId != 0 ? porId : fecha.compareTo(otra.fecha);
    }
}
