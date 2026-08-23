package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.ubicaciones.dominio.HorarioEfectivo;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Resuelve el horario con el que un salon realmente opera en una fecha concreta, componiendo
 * {@link SalonHorarioExcepcion} (excepcion puntual) sobre {@link HorarioOperacionResolver}
 * (plantilla semanal versionada). Prioridad:
 *
 * <ol>
 *   <li>excepcion activa CERRADO -&gt; CERRADO;</li>
 *   <li>excepcion activa con horario especial -&gt; ABIERTO con ese horario;</li>
 *   <li>sin excepcion, con version semanal vigente -&gt; ABIERTO con el horario base;</li>
 *   <li>sin excepcion ni version semanal vigente -&gt; NO_OPERATIVO.</li>
 * </ol>
 *
 * <p>Una excepcion abierta gana aunque el dia no tenga horario semanal: {@code SalonHorarioExcepcion}
 * se guarda sin consultar {@code HorarioOperacion}, asi que un horario especial en un dia sin
 * plantilla es un estado legitimo del modelo actual.
 *
 * <p>Recibe la fecha explicitamente y nunca llama a {@code LocalDate.now()}: eso mantiene los tests
 * deterministas y permite consultar fechas arbitrarias (pasadas o futuras). El "hoy" de negocio lo
 * decide quien llama, con el {@link java.time.Clock} central.
 *
 * <p>Pertenece a {@code ubicaciones} y no conoce turnos ni bloques: la dependencia va siempre
 * calendario/programacion -&gt; ubicaciones.
 */
@Service
@Transactional(readOnly = true)
public class HorarioEfectivoSalon {

    private final SalonHorarioExcepcionRepository excepcionRepository;
    private final HorarioOperacionResolver horarioOperacionResolver;

    public HorarioEfectivoSalon(
            SalonHorarioExcepcionRepository excepcionRepository,
            HorarioOperacionResolver horarioOperacionResolver) {
        this.excepcionRepository = excepcionRepository;
        this.horarioOperacionResolver = horarioOperacionResolver;
    }

    public HorarioEfectivo resolver(UUID salonId, LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("fecha no puede ser null");
        }

        Optional<SalonHorarioExcepcion> excepcion =
                excepcionRepository.findBySalonIdAndFechaAndActivoTrue(salonId, fecha);
        if (excepcion.isPresent()) {
            SalonHorarioExcepcion e = excepcion.get();
            if (e.isCerrado()) {
                return HorarioEfectivo.cerrado();
            }
            return HorarioEfectivo.abiertoPorExcepcion(e.getHoraApertura(), e.getHoraCierre());
        }

        return horarioOperacionResolver.resolver(salonId, fecha)
                .map(this::aHorarioAbierto)
                .orElseGet(HorarioEfectivo::noOperativo);
    }

    private HorarioEfectivo aHorarioAbierto(HorarioOperacion horario) {
        return HorarioEfectivo.abiertoPorHorarioSemanal(horario.getHoraApertura(), horario.getHoraCierre());
    }
}
