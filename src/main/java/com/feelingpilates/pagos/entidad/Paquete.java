package com.feelingpilates.pagos.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paquete")
@Getter
@Setter
@NoArgsConstructor
public class Paquete extends EntidadBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaPaquete categoria;

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
