package com.feelingpilates.usuarios.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import com.feelingpilates.ubicaciones.entidad.TipoActividad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends EntidadBase {

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "contrasena_hash")
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor_auth", nullable = false)
    private ProveedorAuth proveedorAuth = ProveedorAuth.local;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "foto_datos")
    private byte[] fotoDatos;

    @Column(name = "foto_tipo")
    private String fotoTipo;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatusUsuario estatus = EstatusUsuario.activo;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> roles = new HashSet<>();

    /** Actividades que este usuario, como instructor, esta capacitado para impartir. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "instructor_actividad",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_actividad_id"))
    private Set<TipoActividad> especialidades = new HashSet<>();

    public enum ProveedorAuth { local, google }

    public enum EstatusUsuario { activo, suspendido, eliminado }
}
