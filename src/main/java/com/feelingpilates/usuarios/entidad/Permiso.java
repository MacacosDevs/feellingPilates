package com.feelingpilates.usuarios.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permiso")
@Getter
@Setter
@NoArgsConstructor
public class Permiso extends EntidadBase {

    @Column(nullable = false, unique = true)
    private String codigo;

    private String descripcion;

    @Column(nullable = false)
    private String categoria;
}
