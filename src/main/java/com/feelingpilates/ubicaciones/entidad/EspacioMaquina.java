package com.feelingpilates.ubicaciones.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "espacio_maquina")
@Getter
@Setter
@NoArgsConstructor
public class EspacioMaquina {

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("espacioId")
    @JoinColumn(name = "espacio_id")
    private Espacio espacio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tipoMaquinaId")
    @JoinColumn(name = "tipo_maquina_id")
    private TipoMaquina tipoMaquina;

    @Column(nullable = false)
    private Short cantidad;

    public EspacioMaquina(Espacio espacio, TipoMaquina tipoMaquina, Short cantidad) {
        this.espacio = espacio;
        this.tipoMaquina = tipoMaquina;
        this.cantidad = cantidad;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private UUID espacioId;
        private UUID tipoMaquinaId;
    }
}
