package com.feelingpilates.usuarios.entidad;

import com.feelingpilates.comun.entidad.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "perfil_instructor")
@Getter
@Setter
@NoArgsConstructor
public class PerfilInstructor extends EntidadBase {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(name = "sobre_su_clase")
    private String sobreSuClase;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "info_horarios", columnDefinition = "jsonb")
    private Map<String, Object> infoHorarios;

    @Column(name = "calificacion_promedio")
    private BigDecimal calificacionPromedio;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "tiktok_url")
    private String tiktokUrl;

    @Column(name = "whatsapp_url")
    private String whatsappUrl;
}
