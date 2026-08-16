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

/**
 * Excepcion puntual al horario semanal de un salon: cerrado por festivo, o
 * horario especial solo para esa fecha exacta. Sobrescribe a HorarioOperacion
 * unicamente ese dia.
 */
@Entity
@Table(name = "salon_horario_excepcion")
@Getter
@Setter
@NoArgsConstructor
public class SalonHorarioExcepcion extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private boolean cerrado;

    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    @Column(nullable = false)
    private boolean activo = true;
}
