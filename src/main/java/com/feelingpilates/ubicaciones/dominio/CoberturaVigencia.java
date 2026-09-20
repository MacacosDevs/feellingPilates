package com.feelingpilates.ubicaciones.dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Politica compartida de cobertura temporal: dado un {@link RangoVigencia} objetivo y una
 * coleccion de rangos disponibles, responde si los disponibles cubren el objetivo COMPLETO,
 * sin huecos.
 *
 * <p>Es dominio neutral: no conoce horarios, turnos ni bloques. La usan tanto la validacion de
 * turnos recurrentes como la de bloques de programacion, para no duplicar el algoritmo.
 *
 * <p>El barrido es de una sola pasada sobre los rangos ordenados por {@code desde}
 * (O(n log n) por el orden, O(n) si ya viniera ordenado): nunca itera fecha por fecha.
 *
 * <p>El infinito es estado logico, no una fecha centinela: {@code desde == null} es -infinito y
 * {@code hasta == null} es +infinito. La aritmetica de fronteras usa {@link ChronoUnit#DAYS}
 * en vez de {@code plusDays}/{@code minusDays} sobre valores que podrian ser
 * {@link LocalDate#MIN}/{@link LocalDate#MAX}, para no desbordar.
 */
public final class CoberturaVigencia {

    private static final Comparator<RangoVigencia> POR_DESDE_NULLS_FIRST =
            Comparator.comparing(RangoVigencia::desde, Comparator.nullsFirst(Comparator.naturalOrder()));

    private CoberturaVigencia() {
    }

    /**
     * Resultado del barrido. {@code completa} es la respuesta minima; {@code gapDesde}/
     * {@code gapHasta} describen el primer hueco encontrado (con la misma semantica de infinito
     * que {@link RangoVigencia}) solo para enriquecer mensajes de error y tests.
     */
    public record Resultado(boolean completa, LocalDate gapDesde, LocalDate gapHasta) {

        public boolean tieneGap() {
            return !completa;
        }

        static Resultado sinGap() {
            return new Resultado(true, null, null);
        }

        static Resultado conGap(LocalDate desde, LocalDate hasta) {
            return new Resultado(false, desde, hasta);
        }
    }

    public static boolean cubreCompletamente(RangoVigencia objetivo, Collection<RangoVigencia> disponibles) {
        return evaluar(objetivo, disponibles).completa();
    }

    public static Resultado evaluar(RangoVigencia objetivo, Collection<RangoVigencia> disponibles) {
        if (objetivo == null) {
            throw new IllegalArgumentException("objetivo no puede ser null");
        }
        if (disponibles == null) {
            throw new IllegalArgumentException("disponibles no puede ser null");
        }

        // Los rangos que no tocan el objetivo no aportan cobertura y solo estorbarian al barrido.
        List<RangoVigencia> ordenados = disponibles.stream()
                .filter(objetivo::intersecta)
                .sorted(POR_DESDE_NULLS_FIRST)
                .toList();

        // `cubiertoHasta` = ultimo dia cubierto de forma continua desde el inicio del objetivo.
        //   nadaCubierto == true                        -> todavia no se cubre nada
        //   nadaCubierto == false && cubiertoHasta null -> cubierto hasta +infinito
        LocalDate cubiertoHasta = null;
        boolean nadaCubierto = true;

        for (RangoVigencia disponible : ordenados) {
            if (!nadaCubierto && cubiertoHasta == null) {
                break;
            }

            if (nadaCubierto) {
                if (!alcanzaElInicio(objetivo, disponible)) {
                    return Resultado.conGap(objetivo.desde(), diaAnterior(disponible.desde()));
                }
            } else if (!continuaDespuesDe(cubiertoHasta, disponible)) {
                return Resultado.conGap(diaSiguiente(cubiertoHasta), diaAnterior(disponible.desde()));
            }

            if (disponible.hasta() == null) {
                cubiertoHasta = null;
                nadaCubierto = false;
                break;
            }
            if (nadaCubierto || disponible.hasta().isAfter(cubiertoHasta)) {
                cubiertoHasta = disponible.hasta();
            }
            nadaCubierto = false;
        }

        if (nadaCubierto) {
            return Resultado.conGap(objetivo.desde(), objetivo.hasta());
        }
        if (cubiertoHasta == null) {
            return Resultado.sinGap();
        }
        if (objetivo.hasta() == null) {
            return Resultado.conGap(diaSiguiente(cubiertoHasta), null);
        }
        if (cubiertoHasta.isBefore(objetivo.hasta())) {
            return Resultado.conGap(diaSiguiente(cubiertoHasta), objetivo.hasta());
        }
        return Resultado.sinGap();
    }

    /** Un rango sirve para arrancar si empieza en -infinito o no despues del inicio del objetivo. */
    private static boolean alcanzaElInicio(RangoVigencia objetivo, RangoVigencia disponible) {
        if (disponible.desde() == null) {
            return true;
        }
        if (objetivo.desde() == null) {
            return false;
        }
        return !disponible.desde().isAfter(objetivo.desde());
    }

    /**
     * Un rango continua la cobertura si empieza a mas tardar el dia siguiente al ultimo cubierto
     * (contiguo) o antes (solapado).
     */
    private static boolean continuaDespuesDe(LocalDate cubiertoHasta, RangoVigencia disponible) {
        if (disponible.desde() == null) {
            return true;
        }
        return ChronoUnit.DAYS.between(disponible.desde(), cubiertoHasta) >= -1;
    }

    private static LocalDate diaAnterior(LocalDate fecha) {
        if (fecha == null || LocalDate.MIN.equals(fecha)) {
            return fecha;
        }
        return fecha.minusDays(1);
    }

    private static LocalDate diaSiguiente(LocalDate fecha) {
        if (fecha == null || LocalDate.MAX.equals(fecha)) {
            return fecha;
        }
        return fecha.plusDays(1);
    }
}
