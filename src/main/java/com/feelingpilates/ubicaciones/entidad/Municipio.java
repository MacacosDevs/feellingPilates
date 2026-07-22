package com.feelingpilates.ubicaciones.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "municipio")
@Getter
@Setter
@NoArgsConstructor
public class Municipio {

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("estadoId")
    @JoinColumn(name = "estado_id")
    private Estado estado;

    @Column(nullable = false)
    private String nombre;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @lombok.AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private Short estadoId;
        private Short id;
    }
}
