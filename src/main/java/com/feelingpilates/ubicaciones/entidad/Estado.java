package com.feelingpilates.ubicaciones.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estado")
@Getter
@Setter
@NoArgsConstructor
public class Estado {

    @Id
    private Short id;

    @Column(nullable = false)
    private String nombre;
}
