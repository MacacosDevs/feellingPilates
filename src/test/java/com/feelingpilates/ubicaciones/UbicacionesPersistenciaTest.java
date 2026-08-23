package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.entidad.Salon;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UbicacionesPersistenciaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void jpaValidaYRegistraLasEntidadesDeUbicaciones() {
        assertThat(entityManagerFactory.getMetamodel().entity(Salon.class)).isNotNull();
        assertThat(entityManagerFactory.getMetamodel().entity(HorarioOperacion.class)).isNotNull();
    }

    @Test
    void v42AgregaPoliticasDeSalonConValoresTransicionales() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        var fila = jdbcTemplate.queryForMap("""
                select requiere_confirmacion_instructor, plazo_respuesta_confirmacion_horas,
                       anticipacion_maxima_reserva_horas, anticipacion_minima_reserva_horas,
                       margen_materializacion_dias
                from salon where id = ?
                """, salonId);

        assertThat(fila.get("requiere_confirmacion_instructor")).isEqualTo(false);
        assertThat(((Number) fila.get("anticipacion_minima_reserva_horas")).intValue()).isZero();
        assertThat(fila.get("plazo_respuesta_confirmacion_horas")).isNull();
        assertThat(fila.get("anticipacion_maxima_reserva_horas")).isNull();
        assertThat(fila.get("margen_materializacion_dias")).isNull();
    }

    @Test
    void v42RechazaConfirmacionRequeridaSinPlazoDeRespuesta() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update salon set requiere_confirmacion_instructor = true where id = ?", salonId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v43AgregaVigenciaAHorarioOperacionYPreservaFilasLegadas() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        UUID horarioId = insertarHorarioLegado(salonId, (short) 2);

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from horario_operacion where salon_id = ? and dia_semana = 2",
                Integer.class, salonId);
        assertThat(total).isEqualTo(1);

        var fila = jdbcTemplate.queryForMap(
                "select vigente_desde, vigente_hasta from horario_operacion where id = ?", horarioId);
        assertThat(fila.get("vigente_desde")).isNull();
        assertThat(fila.get("vigente_hasta")).isNull();
    }

    @Test
    void uniqueSalonDiaSemanaSigueVigenteEnF2A() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        insertarHorarioLegado(salonId, (short) 3);

        assertThatThrownBy(() -> insertarHorarioLegado(salonId, (short) 3))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void checkVigenciaRechazaHastaAnteriorADesde() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        assertThatThrownBy(() -> insertarHorarioConVigencia(
                salonId, (short) 4, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void checkVigenciaPermiteCombinacionesConLimitesAbiertos() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);

        insertarHorarioConVigencia(salonId, (short) 5, null, null);
        insertarHorarioConVigencia(salonId, (short) 6, null, LocalDate.of(2026, 1, 31));
        insertarHorarioConVigencia(salonId, (short) 0, LocalDate.of(2026, 1, 1), null);

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from horario_operacion where salon_id = ? and dia_semana in (5, 6, 0)",
                Integer.class, salonId);
        assertThat(total).isEqualTo(3);
    }

    private UUID insertarHorarioLegado(UUID salonId, short diaSemana) {
        return insertarHorarioConVigencia(salonId, diaSemana, null, null);
    }

    private UUID insertarHorarioConVigencia(UUID salonId, short diaSemana, LocalDate desde, LocalDate hasta) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, '08:00', '20:00', ?, ?)
                """,
                id, salonId, diaSemana, desde, hasta);
        return id;
    }
}
