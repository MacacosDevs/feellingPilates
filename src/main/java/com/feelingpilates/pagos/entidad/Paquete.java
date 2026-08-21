package com.feelingpilates.pagos.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paquete")
@Getter
@Setter
@NoArgsConstructor
public class Paquete extends EntidadBase {

    // Legado: paquetes sembrados antes de que existiera el catalogo abierto de
    // actividades (ver PaqueteActividad). Los paquetes nuevos dejan esto en null
    // y describen su contenido con `actividades`.
    @Enumerated(EnumType.STRING)
    private CategoriaPaquete categoria;

    @OneToMany(mappedBy = "paquete", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaqueteActividad> actividades = new ArrayList<>();

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "precio_centavos", nullable = false)
    private int precioCentavos;

    @Column(name = "vigencia_dias", nullable = false)
    private int vigenciaDias;

    @Column(name = "unitario_texto")
    private String unitarioTexto;

    @Column(nullable = false)
    private boolean destacado;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private int orden;

    public enum CategoriaPaquete { pilates, bacu_fit, combo }
}
