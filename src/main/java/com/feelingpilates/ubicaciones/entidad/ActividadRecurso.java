package com.feelingpilates.ubicaciones.entidad;

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
import java.util.UUID;

@Entity
@Table(name = "actividad_recurso")
@Getter
@Setter
@NoArgsConstructor
public class ActividadRecurso {

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tipoActividadId")
    @JoinColumn(name = "tipo_actividad_id")
    private TipoActividad tipoActividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tipoRecursoId")
    @JoinColumn(name = "tipo_recurso_id")
    private TipoRecurso tipoRecurso;

    /** Unidades totales de este recurso que requiere una reserva de esta actividad. */
    @Column(nullable = false)
    private Short cantidad;

    public ActividadRecurso(TipoActividad tipoActividad, TipoRecurso tipoRecurso, Short cantidad) {
        this.tipoActividad = tipoActividad;
        this.tipoRecurso = tipoRecurso;
        this.cantidad = cantidad;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private UUID tipoActividadId;
        private UUID tipoRecursoId;
    }
}
