package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.dominio.OcurrenciaEfectiva;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.dominio.ProgramacionInvarianteException;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Composición pura NOMINALES -> TARGETS -> RESULTADOS. */
@Component
public class AplicadorAjustesProgramacion {

    public List<OcurrenciaEfectiva> aplicar(
            List<OcurrenciaNominal> nominales,
            List<AjusteProgramacionFecha> ajustes) {
        Map<UUID, OcurrenciaNominal> porSerie = indexarNominales(nominales);
        Map<UUID, AjusteProgramacionFecha> targetPorSerie = new LinkedHashMap<>();
        List<AjusteProgramacionFecha> adiciones = new ArrayList<>();

        for (AjusteProgramacionFecha ajuste : ajustes) {
            if (!ajuste.isActivo()) {
                continue;
            }
            if (ajuste.getTipo() == AjusteProgramacionFecha.Tipo.ADICION) {
                adiciones.add(ajuste);
                continue;
            }
            OcurrenciaNominal nominal = porSerie.get(ajuste.getAsignacionSerieId());
            ReferenciaOcurrencia referencia = referenciaSerie(
                    ajuste.getAsignacionSerieId(), ajuste.getFecha());
            if (nominal == null) {
                throw new ProgramacionInvarianteException(
                        ProgramacionErrores.ASIGNACION_OBJETIVO_NO_EXISTE,
                        "El ajuste activo no tiene exactamente una nominal",
                        referencia);
            }
            if (targetPorSerie.putIfAbsent(ajuste.getAsignacionSerieId(), ajuste) != null) {
                throw new ProgramacionInvarianteException(
                        ProgramacionErrores.CONFLICTO_AJUSTE_PROGRAMACION,
                        "Existe más de un ajuste target activo para la serie y fecha",
                        referencia);
            }
        }

        List<OcurrenciaEfectiva> resultado = new ArrayList<>();
        for (OcurrenciaNominal nominal : nominales) {
            AjusteProgramacionFecha ajuste = targetPorSerie.get(nominal.serieId());
            if (ajuste == null) {
                resultado.add(desdeNominal(nominal));
            } else if (ajuste.getTipo() == AjusteProgramacionFecha.Tipo.REEMPLAZO) {
                resultado.add(desdeResultado(ajuste, OcurrenciaEfectiva.Origen.REEMPLAZO,
                        referenciaSerie(nominal.serieId(), nominal.fecha())));
            }
        }
        adiciones.forEach(a -> resultado.add(desdeResultado(
                a,
                OcurrenciaEfectiva.Origen.ADICION,
                new ReferenciaOcurrencia(
                        ReferenciaOcurrencia.Tipo.AJUSTE, a.getId(), a.getFecha()))));
        return List.copyOf(resultado);
    }

    private Map<UUID, OcurrenciaNominal> indexarNominales(List<OcurrenciaNominal> nominales) {
        Map<UUID, OcurrenciaNominal> porSerie = new LinkedHashMap<>();
        for (OcurrenciaNominal nominal : nominales) {
            if (porSerie.putIfAbsent(nominal.serieId(), nominal) != null) {
                throw new ProgramacionInvarianteException(
                        ProgramacionErrores.CONFLICTO_LOCK_SET_DESACTUALIZADO,
                        "Más de una versión nominal aplica a la misma serie y fecha",
                        referenciaSerie(nominal.serieId(), nominal.fecha()));
            }
        }
        return porSerie;
    }

    private OcurrenciaEfectiva desdeNominal(OcurrenciaNominal nominal) {
        return new OcurrenciaEfectiva(
                nominal.fecha(), nominal.salonId(), nominal.instructorId(),
                nominal.tipoActividadId(), nominal.horaInicio(), nominal.horaFin(),
                OcurrenciaEfectiva.Origen.RECURRENTE,
                referenciaSerie(nominal.serieId(), nominal.fecha()));
    }

    private OcurrenciaEfectiva desdeResultado(
            AjusteProgramacionFecha ajuste,
            OcurrenciaEfectiva.Origen origen,
            ReferenciaOcurrencia referencia) {
        return new OcurrenciaEfectiva(
                ajuste.getFecha(),
                ajuste.getSalonResultadoId(),
                ajuste.getInstructorResultadoId(),
                ajuste.getTipoActividadResultadoId(),
                ajuste.getHoraInicioResultado(),
                ajuste.getHoraFinResultado(),
                origen,
                referencia);
    }

    private ReferenciaOcurrencia referenciaSerie(UUID serieId, java.time.LocalDate fecha) {
        return new ReferenciaOcurrencia(
                ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION, serieId, fecha);
    }
}
