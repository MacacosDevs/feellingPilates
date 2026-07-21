package com.feelingpilates.ubicaciones.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "salon")
@Getter
@Setter
@NoArgsConstructor
public class Salon extends EntidadBase {

    @Column(nullable = false)
    private String nombre;

    @Column(name = "estado_id", nullable = false)
    private Short estadoId;

    @Column(name = "municipio_id", nullable = false)
    private Short municipioId;

    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;
}
