package com.feelingpilates.calendario.entidad;

import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import com.feelingpilates.usuarios.entidad.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

/** Que actividad especifica imparte un instructor dentro de un turno (bloque de horario). */
@Entity
@Table(name = "turno_instructor_asignacion")
@Getter
@Setter
@NoArgsConstructor
public class TurnoInstructorAsignacion {

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("turnoId")
    @JoinColumn(name = "turno_id")
    private TurnoInstructor turno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tipoActividadId")
    @JoinColumn(name = "tipo_actividad_id")
    private TipoActividad tipoActividad;

    /** Lapso que cubre este instructor dentro del turno; null en ambas = cubre el bloque completo. */
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    public TurnoInstructorAsignacion(TurnoInstructor turno, Usuario usuario, TipoActividad tipoActividad) {
        this.turno = turno;
        this.usuario = usuario;
        this.tipoActividad = tipoActividad;
    }

    public TurnoInstructorAsignacion(
            TurnoInstructor turno, Usuario usuario, TipoActividad tipoActividad, LocalTime horaInicio, LocalTime horaFin) {
        this.turno = turno;
        this.usuario = usuario;
        this.tipoActividad = tipoActividad;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private UUID turnoId;
        private UUID usuarioId;
        private UUID tipoActividadId;
    }
}
