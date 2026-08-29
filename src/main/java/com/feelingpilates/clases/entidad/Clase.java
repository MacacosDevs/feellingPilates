package com.feelingpilates.clases.entidad;

import com.feelingpilates.calendario.entidad.TurnoInstructor;
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
 * Ocurrencia concreta de una clase (fecha+hora+salon+instructor+actividad+cupo),
 * materializada bajo demanda a partir de los turnos de instructor vigentes ese
 * dia. Una vez creada es estable: editar el turno recurrente de origen despues
 * no reordena clases que ya tienen reservas.
 */
@Entity
@Table(name = "clase")
@Getter
@Setter
@NoArgsConstructor
public class Clase extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_origen_id")
    private TurnoInstructor turnoOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id")
    private Usuario instructor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_actividad_id")
    private TipoActividad tipoActividad;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private short capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PROGRAMADA;

    public enum Estado { PROGRAMADA, CANCELADA }
}
