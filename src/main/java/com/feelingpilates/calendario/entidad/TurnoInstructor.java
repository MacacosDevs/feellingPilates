package com.feelingpilates.calendario.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.usuarios.entidad.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Turno de un instructor en un salon. RECURRENTE se repite cada semana en
 * {@code diaSemana}; EXCEPCION/CANCELACION aplican solo a una {@code fecha}
 * puntual, sobrescribiendo lo que diga el turno recurrente ese dia.
 */
@Entity
@Table(name = "turno_instructor")
@Getter
@Setter
@NoArgsConstructor
public class TurnoInstructor extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    /** 0 = domingo ... 6 = sabado. Solo aplica si tipo = RECURRENTE. */
    @Column(name = "dia_semana")
    private Short diaSemana;

    /** Solo aplica si tipo = EXCEPCION o CANCELACION. */
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean activo = true;

    /** Clase que se imparte en este turno; opcional (el instructor puede dejarlo sin definir). */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tipo_actividad_id")
    private TipoActividad tipoActividad;

    public enum Tipo { RECURRENTE, EXCEPCION, CANCELACION }
}
