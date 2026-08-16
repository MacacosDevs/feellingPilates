package com.feelingpilates.ubicaciones.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_actividad")
@Getter
@Setter
@NoArgsConstructor
public class TipoActividad extends EntidadBase {

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    /** Duracion de una clase de este tipo; define el tamano del bloque al reservar. */
    @Column(name = "duracion_minutos", nullable = false)
    private Short duracionMinutos = 60;
}
