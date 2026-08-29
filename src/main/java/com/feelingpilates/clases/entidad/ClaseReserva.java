package com.feelingpilates.clases.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import com.feelingpilates.usuarios.entidad.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Lugar reservado por un cliente en una Clase (cupo compartido, no exclusivo). */
@Entity
@Table(name = "clase_reserva")
@Getter
@Setter
@NoArgsConstructor
public class ClaseReserva extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clase_id")
    private Clase clase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.CONFIRMADA;

    @Column(name = "asistio_en")
    private OffsetDateTime asistioEn;

    public enum Estado { CONFIRMADA, CANCELADA, ASISTIO }
}
