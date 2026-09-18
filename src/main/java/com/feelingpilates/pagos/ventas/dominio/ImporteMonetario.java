package com.feelingpilates.pagos.ventas.dominio;

import java.util.Currency;
import java.util.Objects;

public record ImporteMonetario(long unidadesMinimas, String monedaIso) {
    public ImporteMonetario {
        Objects.requireNonNull(monedaIso, "monedaIso");
        if (unidadesMinimas < 0 || !monedaIso.matches("[A-Z]{3}"))
            throw new IllegalArgumentException("IMPORTE_INVALIDO");
        Currency moneda = Currency.getInstance(monedaIso);
        if (moneda.getDefaultFractionDigits() < 0 || moneda.getDefaultFractionDigits() > 9)
            throw new IllegalArgumentException("Unidad monetaria ISO no representable");
    }
    public int exponenteIso() { return Currency.getInstance(monedaIso).getDefaultFractionDigits(); }
}
