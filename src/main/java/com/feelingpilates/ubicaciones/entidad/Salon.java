package com.feelingpilates.ubicaciones.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

    private String telefono;

    private String calle;

    @Column(name = "numero_exterior")
    private String numeroExterior;

    @Column(name = "numero_interior")
    private String numeroInterior;

    private String colonia;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private String referencias;

    private Double latitud;

    private Double longitud;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "salon_tipo_actividad",
            joinColumns = @JoinColumn(name = "salon_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_actividad_id"))
    private Set<TipoActividad> tiposActividad = new HashSet<>();
}
