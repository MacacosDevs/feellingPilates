package com.feelingpilates.calendario.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.usuarios.entidad.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bloque de horario en un salon: define un rango de horas (recurrente por
 * {@code diaSemana}, o EXCEPCION/CANCELACION puntual por {@code fecha},
 * sobrescribiendo lo que diga el turno recurrente ese dia) y puede tener
 * varios instructores y varias actividades asignadas.
 */
@Entity
@Table(name = "turno_instructor")
@Getter
@Setter
@NoArgsConstructor
public class TurnoInstructor extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    /** 0 = domingo ... 6 = sabado. Solo aplica si tipo = RECURRENTE. */
    @Column(name = "dia_semana")
    private Short diaSemana;

    /** Solo aplica si tipo = EXCEPCION o CANCELACION. */
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean activo = true;

    /** Instructores asignados a este bloque (uno o mas). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "turno_instructor_usuario",
            joinColumns = @JoinColumn(name = "turno_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> instructores = new HashSet<>();

    /** Que actividad especifica da cada instructor de este bloque (puede quedar vacio: sin definir). */
    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TurnoInstructorAsignacion> asignaciones = new ArrayList<>();

    public enum Tipo { RECURRENTE, EXCEPCION, CANCELACION }
}
