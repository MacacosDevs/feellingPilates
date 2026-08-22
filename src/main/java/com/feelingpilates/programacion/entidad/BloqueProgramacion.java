package com.feelingpilates.programacion.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "programacion_bloque")
@Getter
@Setter
@NoArgsConstructor
public class BloqueProgramacion extends EntidadBase {

    /** Identidad logica compartida por todas las versiones de la regla. */
    @Column(name = "serie_id", nullable = false)
    private UUID serieId;

    @Column(name = "salon_id", nullable = false)
    private UUID salonId;

    /** 0 = domingo ... 6 = sabado. */
    @Column(name = "dia_semana", nullable = false)
    private short diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @Column(nullable = false)
    private boolean activo = true;
}
