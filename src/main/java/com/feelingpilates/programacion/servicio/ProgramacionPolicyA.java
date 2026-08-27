package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.programacion.dominio.OcurrenciaNominal;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Policy A inversa: un writer recurrente nunca deja un target activo con cardinalidad 0 o >1. */
@Component
public class ProgramacionPolicyA {

    private final AjusteProgramacionFechaRepository ajusteRepository;
    private final ProgramacionNominal nominal;
    private final Clock reloj;

    public ProgramacionPolicyA(
            AjusteProgramacionFechaRepository ajusteRepository,
            ProgramacionNominal nominal,
            Clock reloj) {
        this.ajusteRepository = ajusteRepository;
        this.nominal = nominal;
        this.reloj = reloj;
    }

    public void validarNuevaAsignacion(
            BloqueProgramacionService.CrearAsignacion comando,
            BloqueProgramacion bloque) {
        validarNuevaAsignacion(comando, bloque, ajusteRepository.buscarTargetsActivosDesde(
                comando.serieId(), reloj.instant().atZone(reloj.getZone()).toLocalDate()));
    }

    void validarNuevaAsignacion(
            BloqueProgramacionService.CrearAsignacion comando,
            BloqueProgramacion bloque,
            List<AjusteProgramacionFecha> ajustesReleidos) {
        List<AjusteProgramacionFecha> afectados = ajustesReleidos.stream()
                .filter(a -> a.getTipo() != AjusteProgramacionFecha.Tipo.ADICION)
                .filter(a -> comando.serieId().equals(a.getAsignacionSerieId()))
                .filter(a -> !a.getFecha().isBefore(
                        reloj.instant().atZone(reloj.getZone()).toLocalDate()))
                .toList();
        for (AjusteProgramacionFecha ajuste : afectados) {
            LocalDate fecha = ajuste.getFecha();
            long cardinalidad = nominal.porSerieYFecha(comando.serieId(), fecha).size();
            if (aplica(comando, bloque, fecha)) {
                cardinalidad++;
            }
            if (cardinalidad != 1) {
                throw new ValidacionException(ProgramacionErrores.mensaje(
                        ProgramacionErrores.CONFLICTO_AJUSTE_PROGRAMACION,
                        "La creación recurrente dejaría un ajuste activo con cardinalidad "
                                + cardinalidad));
            }
        }
    }

    private boolean aplica(
            BloqueProgramacionService.CrearAsignacion comando,
            BloqueProgramacion bloque,
            LocalDate fecha) {
        short dia = (short) (fecha.getDayOfWeek().getValue() % 7);
        return bloque.getDiaSemana() == dia
                && contiene(fecha, bloque.getVigenteDesde(), bloque.getVigenteHasta())
                && contiene(fecha, comando.vigenteDesde(), comando.vigenteHasta());
    }

    private boolean contiene(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && (hasta == null || !fecha.isAfter(hasta));
    }
}
