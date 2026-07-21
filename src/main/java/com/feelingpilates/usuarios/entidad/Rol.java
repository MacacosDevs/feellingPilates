package com.feelingpilates.usuarios.entidad;

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
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol extends EntidadBase {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN = "ADMIN";
    public static final String PERSONAL = "PERSONAL";
    public static final String INSTRUCTOR = "INSTRUCTOR";
    public static final String CLIENTE = "CLIENTE";

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id"))
    private Set<Permiso> permisos = new HashSet<>();
}
