package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.exception.ConflictException;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import com.feelingpilates.ubicaciones.repositorio.HorarioOperacionRepository;
import com.feelingpilates.ubicaciones.servicio.CerrarHorarioOperacion;
import com.feelingpilates.ubicaciones.servicio.HorarioOperacionResolver;
import com.feelingpilates.ubicaciones.servicio.VersionarHorarioOperacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.feelingpilates.ubicaciones.servicio.ConflictoVigenciaHorarioTranslator.traduciendoConflictoDeVigencia;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PostgreSQL/Testcontainers real contra el esquema V46 (EXCLUDE {@code
 * ex_horario_operacion_vigencia}, sin UNIQUE(salon, dia)): resultado FISICO de los writers.
 *
 * <p>Deliberadamente <b>sin</b> {@code @Transactional} de clase: varios de estos escenarios exigen
 * commits y rollbacks reales, no el rollback automatico del test. La limpieza es explicita.
 *
 * <p>Como el contexto de Spring se levanta completo, este test es tambien la evidencia de que el
 * port de {@code ubicaciones} y sus adapters de {@code calendario}/{@code programacion} no forman
 * un ciclo de beans.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class HorarioOperacionWritersPersistenciaTest {

    private static final short LUNES = 1;
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);
    private static final LocalTime VEINTE = LocalTime.of(20, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private VersionarHorarioOperacion versionar;
    @Autowired
    private CerrarHorarioOperacion cerrar;
    @Autowired
    private HorarioOperacionResolver resolver;
    @Autowired
    private HorarioOperacionRepository horarioRepository;
    @Autowired
    private Clock reloj;

    private UUID salonId;
    /** Un lunes futuro: el resolver deriva el dia de la semana de la fecha, asi que debe cuadrar. */
    private LocalDate d;

    @BeforeEach
    void preparar() {
        salonId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into salon (id, nombre, estado_id, municipio_id, direccion)
                values (?, ?, 22, 14, 'Calle de prueba')
                """, salonId, "Salón writers " + salonId);
        d = proximoLunes(LocalDate.now(reloj).plusDays(30));
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("delete from horario_operacion where salon_id = ?", salonId);
        jdbcTemplate.update("delete from salon where id = ?", salonId);
    }

    // ---------- Versionar sobre la fila legada ----------

    @Test
    void versionarSobreLegacyCierraLaViejaEInsertaLaNueva() {
        insertarHorario(null, null, OCHO, VEINTE);

        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, d, NUEVE, VEINTE));

        List<Map<String, Object>> filas = filas();
        assertThat(filas).hasSize(2);
        assertThat(filas.get(0)).containsEntry("vigente_desde", null)
                .containsEntry("vigente_hasta", java.sql.Date.valueOf(d.minusDays(1)));
        assertThat(filas.get(1)).containsEntry("vigente_desde", java.sql.Date.valueOf(d))
                .containsEntry("vigente_hasta", null);
    }

    /**
     * El resolver distingue las dos versiones por fecha: {@code D-1} devuelve la vieja y {@code D}
     * la nueva. Como el resolver deriva el dia de la semana de la fecha, la comprobacion "en D-1
     * rige la vieja" se hace con {@code findVigente} (mismo dia de la semana) y ademas con el
     * resolver sobre el lunes anterior, que tambien cae dentro de la vigencia cerrada.
     */
    @Test
    void elResolverDistingueLaVersionViejaDeLaNueva() {
        insertarHorario(null, null, OCHO, VEINTE);

        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, d, NUEVE, VEINTE));

        assertThat(horarioRepository.findVigente(salonId, LUNES, d.minusDays(1)))
                .singleElement()
                .extracting(HorarioOperacion::getHoraApertura)
                .isEqualTo(OCHO);
        assertThat(resolver.resolver(salonId, d.minusDays(7)))
                .get().extracting(HorarioOperacion::getHoraApertura).isEqualTo(OCHO);
        assertThat(resolver.resolver(salonId, d))
                .get().extracting(HorarioOperacion::getHoraApertura).isEqualTo(NUEVE);
    }

    /**
     * Alta sin historia: no hay nada que cerrar, solo el INSERT. Y es la unica version, asi que
     * cualquier fecha posterior la resuelve.
     */
    @Test
    void altaSinHistoriaInsertaUnaSolaFilaAbierta() {
        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, d, OCHO, VEINTE));

        assertThat(filas()).hasSize(1);
        assertThat(resolver.resolver(salonId, d.plusDays(70))).isPresent();
    }

    /**
     * Politica A cubre TurnoInstructor RECURRENTE y no EXCEPCION. Un turno EXCEPCION cuyo rango no
     * cabe en el horario nuevo NO bloquea el versionado: su invariante ya no la mantiene el sistema
     * hoy (una {@code SalonHorarioExcepcion} puede cerrar el dia sin mirar los turnos), asi que
     * meterlo aqui seria ampliar la politica, no cerrar un hueco. Si alguien lo hiciera, este test
     * lo detecta.
     */
    @Test
    void unTurnoExcepcionIncompatibleNoBloqueaElVersionado() {
        insertarHorario(null, null, OCHO, VEINTE);
        UUID turnoId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into turno_instructor (id, salon_id, tipo, fecha, hora_inicio, hora_fin, activo)
                values (?, ?, 'EXCEPCION', ?, ?, ?, true)
                """, turnoId, salonId, d.plusDays(7), OCHO, NUEVE);

        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, d, NUEVE, VEINTE));

        assertThat(filas()).hasSize(2);
        jdbcTemplate.update("delete from turno_instructor where id = ?", turnoId);
    }

    // ---------- Cerrar ----------

    @Test
    void cerrarDejaLaVersionEnDMenosUnoYElDiaSinHorarioDesdeD() {
        LocalDate d0 = d.minusDays(28);
        insertarHorario(d0, null, OCHO, VEINTE);

        cerrar.ejecutar(new CerrarHorarioOperacion.CerrarHorario(salonId, LUNES, d));

        assertThat(filas()).hasSize(1);
        assertThat(filas().get(0))
                .containsEntry("vigente_desde", java.sql.Date.valueOf(d0))
                .containsEntry("vigente_hasta", java.sql.Date.valueOf(d.minusDays(1)));
        assertThat(horarioRepository.findVigente(salonId, LUNES, d.minusDays(1))).hasSize(1);
        assertThat(resolver.resolver(salonId, d)).isEmpty();
    }

    // ---------- Reapertura ----------

    @Test
    void reaperturaTrasHistoriaCerradaPreservaElGapYNoTocaElPasado() {
        LocalDate finHistoria = d.minusDays(14);
        UUID historiaId = insertarHorario(null, finHistoria, OCHO, VEINTE);

        versionar.ejecutar(new VersionarHorarioOperacion.VersionarHorario(
                salonId, LUNES, d, NUEVE, VEINTE));

        List<Map<String, Object>> filas = filas();
        assertThat(filas).hasSize(2);
        // La historia queda exactamente como estaba.
        assertThat(filas.get(0)).containsEntry("id", historiaId)
                .containsEntry("vigente_hasta", java.sql.Date.valueOf(finHistoria));
        assertThat(filas.get(1)).containsEntry("vigente_desde", java.sql.Date.valueOf(d));
        // El gap intermedio sigue siendo un gap: ningun horario lo cubre.
        assertThat(resolver.resolver(salonId, d.minusDays(7))).isEmpty();
    }

    // ---------- Rollback tras el primer flush ----------

    /**
     * El primer {@code flush} NO es un commit. Fixture: una version abierta que el writer cerraria
     * (UPDATE + primer flush OK) y una version futura ya committeada que hace chocar el INSERT
     * posterior contra el EXCLUDE.
     *
     * <p>Para que el escenario llegue al INSERT sin debilitar el writer, la version futura se
     * inserta <b>despues</b> de que el writer clasifico: se ejercita directamente la secuencia de
     * persistencia, que es la pieza cuya atomicidad se afirma.
     *
     * <p>Aserción desde una transaccion NUEVA: la fila vieja conserva su {@code vigenteHasta}
     * original. No basta con mirar el estado managed dentro de la transaccion fallida.
     */
    @Test
    void rollbackTrasElPrimerFlushDejaLaVersionViejaConSuVigenteHastaOriginal() {
        LocalDate finVieja = d.plusDays(70);
        LocalDate inicioFutura = d.plusDays(71);
        UUID viejaId = insertarHorario(null, finVieja, OCHO, VEINTE);
        insertarHorario(inicioFutura, null, OCHO, VEINTE);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status ->
                traduciendoConflictoDeVigencia(() -> {
                    HorarioOperacion vieja = horarioRepository.findById(viejaId).orElseThrow();
                    // Paso 1 + primer flush: pasa sin problema.
                    vieja.setVigenteHasta(d.minusDays(1));
                    horarioRepository.saveAndFlush(vieja);
                    assertThat(horarioRepository.findById(viejaId).orElseThrow().getVigenteHasta())
                            .isEqualTo(d.minusDays(1));
                    // Paso 2: choca con la futura -> 23P01 -> traducido -> rollback de TODO.
                    HorarioOperacion nueva = new HorarioOperacion();
                    nueva.setSalon(horarioRepository.findById(viejaId).orElseThrow().getSalon());
                    nueva.setDiaSemana(LUNES);
                    nueva.setHoraApertura(NUEVE);
                    nueva.setHoraCierre(VEINTE);
                    nueva.setVigenteDesde(d);
                    nueva.setVigenteHasta(null);
                    return horarioRepository.saveAndFlush(nueva);
                })))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CONFLICTO_VIGENCIA_HORARIO");

        Map<String, Object> vieja = jdbcTemplate.queryForMap(
                "select vigente_hasta from horario_operacion where id = ?", viejaId);
        assertThat(vieja).containsEntry("vigente_hasta", java.sql.Date.valueOf(finVieja));
        assertThat(filas()).hasSize(2);
    }

    // ---------- 23P01 real ----------

    /**
     * Backstop: una insercion solapada que no pasa por la clasificacion del writer produce un
     * {@code 23P01} real de PostgreSQL, con la cadena real Spring -> Hibernate -> driver, y se
     * traduce a codigo de dominio estable.
     */
    @Test
    void insercionSolapadaRealSeTraduceAConflictoDeVigencia() {
        insertarHorario(d, null, OCHO, VEINTE);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status ->
                traduciendoConflictoDeVigencia(() -> {
                    HorarioOperacion solapada = new HorarioOperacion();
                    solapada.setSalon(horarioRepository.findAll().stream()
                            .filter(h -> h.getSalon().getId().equals(salonId))
                            .findFirst().orElseThrow().getSalon());
                    solapada.setDiaSemana(LUNES);
                    solapada.setHoraApertura(NUEVE);
                    solapada.setHoraCierre(VEINTE);
                    solapada.setVigenteDesde(d.plusDays(7));
                    solapada.setVigenteHasta(null);
                    return horarioRepository.saveAndFlush(solapada);
                })))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CONFLICTO_VIGENCIA_HORARIO");

        assertThat(filas()).hasSize(1);
    }

    // ---------- helpers ----------

    private LocalDate proximoLunes(LocalDate desde) {
        LocalDate fecha = desde;
        while (fecha.getDayOfWeek() != DayOfWeek.MONDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    private UUID insertarHorario(
            LocalDate desde, LocalDate hasta, LocalTime apertura, LocalTime cierre) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, ?, ?, ?, ?)
                """, id, salonId, LUNES, apertura, cierre, desde, hasta);
        return id;
    }

    private List<Map<String, Object>> filas() {
        return jdbcTemplate.queryForList("""
                select id, vigente_desde, vigente_hasta, hora_apertura, hora_cierre
                from horario_operacion
                where salon_id = ? and dia_semana = ?
                order by vigente_desde asc nulls first
                """, salonId, LUNES);
    }
}
