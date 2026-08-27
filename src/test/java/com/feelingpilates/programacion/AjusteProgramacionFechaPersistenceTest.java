package com.feelingpilates.programacion;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import com.feelingpilates.programacion.repositorio.AjusteProgramacionFechaRepository;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaPersistence;
import com.feelingpilates.programacion.servicio.AjusteProgramacionFechaService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AjusteProgramacionFechaPersistenceTest {

    @Autowired private AjusteProgramacionFechaPersistence persistence;
    @Autowired private AjusteProgramacionFechaRepository repository;
    @Autowired private AjusteProgramacionFechaService service;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbc;

    private UUID salon;
    private UUID instructor;
    private UUID actividad;

    @BeforeEach
    void ids() {
        salon = jdbc.queryForObject("select id from salon order by id limit 1", UUID.class);
        instructor = jdbc.queryForObject("select id from usuario order by id limit 1", UUID.class);
        actividad = jdbc.queryForObject("select id from tipo_actividad order by id limit 1", UUID.class);
    }

    @Test
    void createUsaUuidAsignadoYGeneraInsertConTimestamps() {
        UUID id = UUID.randomUUID();
        AjusteProgramacionFecha creada = persistence.crear(adicion(id));

        assertThat(creada.getId()).isEqualTo(id);
        assertThat(creada.getCreadoEn()).isNotNull();
        assertThat(creada.getActualizadoEn()).isNotNull();
        assertThat(jdbc.queryForObject(
                "select count(*) from programacion_ajuste_fecha where id = ?",
                Integer.class, id)).isOne();
    }

    @Test
    void updateManagedConservaIdFechaYCreadoEnSinCrearOtraFila() {
        UUID id = UUID.randomUUID();
        AjusteProgramacionFecha creada = persistence.crear(adicion(id));
        LocalDate fecha = creada.getFecha();
        OffsetDateTime creadoEn = creada.getCreadoEn();

        creada.actualizarResultado(
                salon, instructor, actividad, LocalTime.of(12, 0), LocalTime.of(13, 0));
        persistence.flushManaged();
        entityManager.clear();

        AjusteProgramacionFecha recargada = repository.findById(id).orElseThrow();
        assertThat(recargada.getId()).isEqualTo(id);
        assertThat(recargada.getFecha()).isEqualTo(fecha);
        assertThat(recargada.getCreadoEn()).isEqualTo(creadoEn);
        assertThat(recargada.getHoraInicioResultado()).isEqualTo(LocalTime.NOON);
        assertThat(jdbc.queryForObject(
                "select count(*) from programacion_ajuste_fecha where id = ?",
                Integer.class, id)).isOne();
    }

    @Test
    void retiroDejaFilaInactivaYUuidNoPuedeReutilizarse() {
        UUID id = UUID.randomUUID();
        persistence.crear(adicion(id));

        service.retirarAdicion(id);
        assertThat(repository.findById(id)).get().extracting(AjusteProgramacionFecha::isActivo)
                .isEqualTo(false);

        assertThatThrownBy(() -> service.guardarAdicion(id, fecha(), resultado()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("histórico");
        assertThat(jdbc.queryForObject(
                "select count(*) from programacion_ajuste_fecha where id = ?",
                Integer.class, id)).isOne();
    }

    @Test
    void noOpRealNoCambiaActualizadoEn() {
        UUID id = UUID.randomUUID();
        AjusteProgramacionFecha creada = persistence.crear(adicion(id));
        OffsetDateTime actualizado = creada.getActualizadoEn();

        AjusteProgramacionFecha respuesta = service.guardarAdicion(id, fecha(), resultado());
        entityManager.flush();

        assertThat(respuesta.getActualizadoEn()).isEqualTo(actualizado);
    }

    private AjusteProgramacionFecha adicion(UUID id) {
        return AjusteProgramacionFecha.nuevaAdicion(
                id, fecha(), salon, instructor, actividad,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    private AjusteProgramacionFechaService.Resultado resultado() {
        return new AjusteProgramacionFechaService.Resultado(
                salon, instructor, actividad, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    private LocalDate fecha() {
        return LocalDate.now().plusYears(2);
    }
}
