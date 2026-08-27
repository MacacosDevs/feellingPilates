package com.feelingpilates.programacion.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Ajuste puntual interno. Su UUID es asignado y nunca se reutiliza tras el retiro. */
@Entity
@Table(name = "programacion_ajuste_fecha")
@Getter
@NoArgsConstructor
public class AjusteProgramacionFecha {

    public enum Tipo { CANCELACION, REEMPLAZO, ADICION }

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Tipo tipo;

    @Column(nullable = false, updatable = false)
    private LocalDate fecha;

    @Column(name = "asignacion_serie_id")
    private UUID asignacionSerieId;

    @Column(name = "salon_resultado_id")
    private UUID salonResultadoId;

    @Column(name = "instructor_resultado_id")
    private UUID instructorResultadoId;

    @Column(name = "tipo_actividad_resultado_id")
    private UUID tipoActividadResultadoId;

    @Column(name = "hora_inicio_resultado")
    private LocalTime horaInicioResultado;

    @Column(name = "hora_fin_resultado")
    private LocalTime horaFinResultado;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    public static AjusteProgramacionFecha nuevaAdicion(
            UUID id, LocalDate fecha, UUID salonId, UUID instructorId, UUID actividadId,
            LocalTime horaInicio, LocalTime horaFin) {
        AjusteProgramacionFecha ajuste = new AjusteProgramacionFecha();
        ajuste.id = id;
        ajuste.tipo = Tipo.ADICION;
        ajuste.fecha = fecha;
        ajuste.aplicarResultado(salonId, instructorId, actividadId, horaInicio, horaFin);
        return ajuste;
    }

    public static AjusteProgramacionFecha nuevoTarget(
            UUID id, Tipo tipo, UUID serieId, LocalDate fecha, UUID salonId, UUID instructorId,
            UUID actividadId, LocalTime horaInicio, LocalTime horaFin) {
        AjusteProgramacionFecha ajuste = new AjusteProgramacionFecha();
        ajuste.id = id;
        ajuste.tipo = tipo;
        ajuste.fecha = fecha;
        ajuste.asignacionSerieId = serieId;
        if (tipo == Tipo.REEMPLAZO) {
            ajuste.aplicarResultado(salonId, instructorId, actividadId, horaInicio, horaFin);
        }
        return ajuste;
    }

    public void actualizarResultado(
            UUID salonId, UUID instructorId, UUID actividadId,
            LocalTime horaInicio, LocalTime horaFin) {
        aplicarResultado(salonId, instructorId, actividadId, horaInicio, horaFin);
    }

    public void actualizarTipoYResultado(
            Tipo nuevoTipo, UUID salonId, UUID instructorId, UUID actividadId,
            LocalTime horaInicio, LocalTime horaFin) {
        this.tipo = nuevoTipo;
        if (nuevoTipo == Tipo.CANCELACION) {
            aplicarResultado(null, null, null, null, null);
        } else {
            aplicarResultado(salonId, instructorId, actividadId, horaInicio, horaFin);
        }
    }

    public void retirar() {
        this.activo = false;
    }

    private void aplicarResultado(
            UUID salonId, UUID instructorId, UUID actividadId,
            LocalTime horaInicio, LocalTime horaFin) {
        this.salonResultadoId = salonId;
        this.instructorResultadoId = instructorId;
        this.tipoActividadResultadoId = actividadId;
        this.horaInicioResultado = horaInicio;
        this.horaFinResultado = horaFin;
    }
}
