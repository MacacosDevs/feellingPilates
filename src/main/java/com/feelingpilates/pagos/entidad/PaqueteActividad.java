package com.feelingpilates.pagos.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cuantas clases de una actividad incluye un paquete; un paquete puede mezclar varias. */
@Entity
@Table(name = "paquete_actividad")
@Getter
@Setter
@NoArgsConstructor
public class PaqueteActividad extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paquete_id")
    private Paquete paquete;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_actividad_id")
    private TipoActividad tipoActividad;

    @Column(name = "cantidad_clases", nullable = false)
    private int cantidadClases;

    public PaqueteActividad(Paquete paquete, TipoActividad tipoActividad, int cantidadClases) {
        this.paquete = paquete;
        this.tipoActividad = tipoActividad;
        this.cantidadClases = cantidadClases;
    }
}
