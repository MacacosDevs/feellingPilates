package com.feelingpilates.ubicaciones.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "horario_operacion")
@Getter
@Setter
@NoArgsConstructor
public class HorarioOperacion extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    /** 0 = domingo ... 6 = sabado. */
    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;

    /** NULL = sin limite inferior. Transicionalmente, filas legadas quedan NULL. */
    @Column(name = "vigente_desde")
    private LocalDate vigenteDesde;

    /** NULL = sin limite superior. Transicionalmente, filas legadas quedan NULL. */
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;
}
