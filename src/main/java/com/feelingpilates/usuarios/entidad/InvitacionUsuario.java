package com.feelingpilates.usuarios.entidad;

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

import java.time.OffsetDateTime;

@Entity
@Table(name = "invitacion_usuario")
@Getter
@Setter
@NoArgsConstructor
public class InvitacionUsuario extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "usado_en")
    private OffsetDateTime usadoEn;

    public InvitacionUsuario(Usuario usuario, String token, OffsetDateTime expiraEn) {
        this.usuario = usuario;
        this.token = token;
        this.expiraEn = expiraEn;
    }

    public boolean estaVigente() {
        return usadoEn == null && expiraEn.isAfter(OffsetDateTime.now());
    }
}
