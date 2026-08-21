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
@Table(name = "salon_recurso")
@Getter
@Setter
@NoArgsConstructor
public class SalonRecurso {

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("salonId")
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tipoRecursoId")
    @JoinColumn(name = "tipo_recurso_id")
    private TipoRecurso tipoRecurso;

    @Column(nullable = false)
    private Short cantidad;

    public SalonRecurso(Salon salon, TipoRecurso tipoRecurso, Short cantidad) {
        this.salon = salon;
        this.tipoRecurso = tipoRecurso;
        this.cantidad = cantidad;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private UUID salonId;
        private UUID tipoRecursoId;
    }
}
