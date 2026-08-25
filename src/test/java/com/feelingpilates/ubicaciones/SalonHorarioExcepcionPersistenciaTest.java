package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.ubicaciones.dto.GuardarExcepcionSalonRequest;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.entidad.SalonHorarioExcepcion;
import com.feelingpilates.ubicaciones.repositorio.SalonHorarioExcepcionRepository;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.ubicaciones.servicio.SalonHorarioExcepcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static com.feelingpilates.ubicaciones.servicio.ConflictoExcepcionHorarioTranslator.traduciendoConflictoDeExcepcion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PostgreSQL/Testcontainers real contra el esquema V18: CHECK, indice unico parcial, traduccion de
 * {@code 23505} y confirmacion de que la excepcion nunca escribe programacion recurrente.
 *
 * <p>{@code @Transactional} de clase: cada test hace rollback automatico; el flush explicito
 * ({@code saveAndFlush}) hace que las violaciones se disparen de forma sincrona sin necesitar un
 * commit real.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SalonHorarioExcepcionPersistenciaTest {

    private static final short LUNES = 1;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SalonRepository salonRepository;
    @Autowired
    private SalonHorarioExcepcionRepository excepcionRepository;
    @Autowired
    private SalonHorarioExcepcionService service;
    @Autowired
    private Clock reloj;

    private UUID salonId;
    private UUID actorId;
    private LocalDate lunesFuturo;

    @BeforeEach
    void preparar() {
        salonId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into salon (id, nombre, estado_id, municipio_id, direccion)
                values (?, ?, 22, 14, 'Calle de prueba')
                """, salonId, "Salón excepción " + salonId);
        actorId = jdbcTemplate.queryForObject("select id from usuario limit 1", UUID.class);

        lunesFuturo = LocalDate.now(reloj).plusDays(30);
        while (lunesFuturo.getDayOfWeek().getValue() != 1) {
            lunesFuturo = lunesFuturo.plusDays(1);
        }
    }

    // ---------- P1: CHECK real ----------

    @Test
    void checkRechazaCerradoConHoras() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into salon_horario_excepcion (id, salon_id, fecha, cerrado, hora_apertura, hora_cierre)
                values (?, ?, ?, true, '08:00', '20:00')
                """, UUID.randomUUID(), salonId, lunesFuturo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void checkRechazaAbiertoSinHoras() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into salon_horario_excepcion (id, salon_id, fecha, cerrado)
                values (?, ?, ?, false)
                """, UUID.randomUUID(), salonId, lunesFuturo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void checkRechazaCierreIgualAApertura() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into salon_horario_excepcion (id, salon_id, fecha, cerrado, hora_apertura, hora_cierre)
                values (?, ?, ?, false, '10:00', '10:00')
                """, UUID.randomUUID(), salonId, lunesFuturo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------- P2: indice parcial, dos activas ----------

    @Test
    void dosFilasActivasMismoSalonYFechaEsRechazado() {
        insertarExcepcion(UUID.randomUUID(), lunesFuturo, true, true);

        assertThatThrownBy(() -> insertarExcepcion(UUID.randomUUID(), lunesFuturo, true, true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("idx_salon_horario_excepcion_unica");
    }

    // ---------- P3: N inactivas + 1 activa, permitido ----------

    @Test
    void variasFilasInactivasMasUnaActivaEsPermitido() {
        insertarExcepcion(UUID.randomUUID(), lunesFuturo, true, false);
        insertarExcepcion(UUID.randomUUID(), lunesFuturo, true, false);
        insertarExcepcion(UUID.randomUUID(), lunesFuturo, true, false);
        insertarExcepcion(UUID.randomUUID(), lunesFuturo, true, true);

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from salon_horario_excepcion where salon_id = ? and fecha = ?",
                Integer.class, salonId, lunesFuturo);
        assertThat(total).isEqualTo(4);
    }

    // ---------- P4: cancelar y recrear, contra BD real ----------

    @Test
    void cancelarYRecrearLaMismaFechaInsertaFilaNuevaSinViolarElIndice() {
        var creada = service.guardar(actorId, salonId, new GuardarExcepcionSalonRequest(lunesFuturo, true, null, null));
        service.eliminar(actorId, salonId, creada.id());
        var recreada = service.guardar(actorId, salonId,
                new GuardarExcepcionSalonRequest(lunesFuturo, false, LocalTime.of(10, 0), LocalTime.of(16, 0)));

        assertThat(recreada.id()).isNotEqualTo(creada.id());

        SalonHorarioExcepcion vieja = excepcionRepository.findById(creada.id()).orElseThrow();
        assertThat(vieja.isActivo()).isFalse();
        SalonHorarioExcepcion nueva = excepcionRepository.findById(recreada.id()).orElseThrow();
        assertThat(nueva.isActivo()).isTrue();

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from salon_horario_excepcion where salon_id = ? and fecha = ?",
                Integer.class, salonId, lunesFuturo);
        assertThat(total).isEqualTo(2);
    }

    // ---------- P5: traduccion especifica, se assertea el codigo ----------

    @Test
    void violacionRealDelIndiceSeTraduceConCodigoEstable() {
        Salon salon = salonRepository.findById(salonId).orElseThrow();
        SalonHorarioExcepcion primera = nuevaExcepcionCerrada(salon, lunesFuturo);
        excepcionRepository.saveAndFlush(primera);

        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() -> {
            SalonHorarioExcepcion segunda = nuevaExcepcionCerrada(salon, lunesFuturo);
            return excepcionRepository.saveAndFlush(segunda);
        }))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ConflictException) e).getMessage())
                        .startsWith("CONFLICTO_EXCEPCION_HORARIO"));
    }

    // ---------- P6: otra violacion de integridad NO se traduce ----------

    @Test
    void otraViolacionDeIntegridadNoSeTraduceComoConflictoDeExcepcion() {
        assertThatThrownBy(() -> traduciendoConflictoDeExcepcion(() ->
                jdbcTemplate.update("""
                        insert into salon_horario_excepcion (id, salon_id, fecha, cerrado)
                        values (?, ?, ?, true)
                        """, UUID.randomUUID(), UUID.randomUUID(), lunesFuturo)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(ConflictException.class);
    }

    // ---------- P7: cerrar un dia no toca programacion recurrente ----------

    @Test
    void cerrarUnDiaConProgramacionRecurrenteLaDejaIdentica() {
        UUID bloqueId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin, vigente_desde, activo)
                values (?, ?, ?, ?, '08:00', '12:00', ?, true)
                """, bloqueId, UUID.randomUUID(), salonId, LUNES, lunesFuturo.minusDays(60));
        jdbcTemplate.update("""
                insert into turno_instructor (id, salon_id, tipo, dia_semana, hora_inicio, hora_fin, activo)
                values (?, ?, 'RECURRENTE', ?, '08:00', '12:00', true)
                """, turnoId, salonId, LUNES);

        Map<String, Object> bloqueAntes = jdbcTemplate.queryForMap(
                "select activo, hora_inicio, hora_fin, vigente_desde, vigente_hasta "
                        + "from programacion_bloque where id = ?", bloqueId);
        Map<String, Object> turnoAntes = jdbcTemplate.queryForMap(
                "select activo, hora_inicio, hora_fin from turno_instructor where id = ?", turnoId);

        service.guardar(actorId, salonId, new GuardarExcepcionSalonRequest(lunesFuturo, true, null, null));

        assertThat(jdbcTemplate.queryForMap(
                "select activo, hora_inicio, hora_fin, vigente_desde, vigente_hasta "
                        + "from programacion_bloque where id = ?", bloqueId))
                .isEqualTo(bloqueAntes);
        assertThat(jdbcTemplate.queryForMap(
                "select activo, hora_inicio, hora_fin from turno_instructor where id = ?", turnoId))
                .isEqualTo(turnoAntes);
    }

    // ---------- helpers ----------

    private void insertarExcepcion(UUID id, LocalDate fecha, boolean cerrado, boolean activo) {
        jdbcTemplate.update("""
                insert into salon_horario_excepcion (id, salon_id, fecha, cerrado, activo)
                values (?, ?, ?, ?, ?)
                """, id, salonId, fecha, cerrado, activo);
    }

    private SalonHorarioExcepcion nuevaExcepcionCerrada(Salon salon, LocalDate fecha) {
        SalonHorarioExcepcion e = new SalonHorarioExcepcion();
        e.setSalon(salon);
        e.setFecha(fecha);
        e.setCerrado(true);
        e.setActivo(true);
        return e;
    }
}
