package com.feelingpilates.ubicaciones;

import com.feelingpilates.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PostgreSQL/Testcontainers real (no mocks) contra la invariante fisica de F2B.3a:
 * ex_horario_operacion_vigencia (V45) reemplaza a UNIQUE(salon_id, dia_semana) como
 * garantia de no-duplicidad, permitiendo N versiones por salon+dia siempre que sus
 * vigencias no se intersecten. No se implementa ningun writer aqui: los escenarios de
 * "cerrar version legada + insertar nueva" se hacen a mano via JdbcTemplate, solo para
 * probar la capacidad fisica del esquema.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class HorarioOperacionVersionadoPersistenciaTest {

    private static final String EXCLUDE_CONSTRAINT = "ex_horario_operacion_vigencia";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void versionesContiguasSonAmbasInsertadas() {
        UUID salonId = salonA();
        short dia = 1;

        insertarHorario(salonId, dia, null, LocalDate.of(2026, 8, 31));
        insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null);

        assertThat(contarFilas(salonId, dia)).isEqualTo(2);
    }

    @Test
    void solapeDeUnDiaEnFronteraCompartidaEsRechazado() {
        UUID salonId = salonA();
        short dia = 2;

        insertarHorario(salonId, dia, null, LocalDate.of(2026, 9, 1));

        assertThatThrownBy(() -> insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(EXCLUDE_CONSTRAINT);
    }

    @Test
    void solapeInteriorEsRechazado() {
        UUID salonId = salonA();
        short dia = 3;

        insertarHorario(salonId, dia, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));

        assertThatThrownBy(() -> insertarHorario(
                salonId, dia, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(EXCLUDE_CONSTRAINT);
    }

    @Test
    void legacyUniversalBloqueaNuevaVersionReal() {
        UUID salonId = salonA();
        short dia = 4;

        insertarHorario(salonId, dia, null, null);

        assertThatThrownBy(() -> insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(EXCLUDE_CONSTRAINT);
    }

    @Test
    void cerrarLegacyManualmentePermiteInsertarNuevaVersion() {
        UUID salonId = salonA();
        short dia = 5;

        UUID legacyId = insertarHorario(salonId, dia, null, null);
        jdbcTemplate.update(
                "update horario_operacion set vigente_hasta = ? where id = ?",
                LocalDate.of(2026, 8, 31), legacyId);
        insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null);

        assertThat(contarFilas(salonId, dia)).isEqualTo(2);
    }

    @Test
    void updateQueCreaSolapeEsRechazado() {
        UUID salonId = salonA();
        short dia = 6;

        UUID versionA = insertarHorario(salonId, dia, null, LocalDate.of(2026, 8, 31));
        insertarHorario(salonId, dia, LocalDate.of(2026, 9, 1), null);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update horario_operacion set vigente_hasta = ? where id = ?",
                LocalDate.of(2026, 9, 1), versionA))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(EXCLUDE_CONSTRAINT);
    }

    @Test
    void distintoSalonPermiteMismasVigencias() {
        UUID salonA = salonA();
        UUID salonB = salonB();
        short dia = 0;

        insertarHorario(salonA, dia, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        insertarHorario(salonB, dia, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(contarFilas(salonA, dia)).isEqualTo(1);
        assertThat(contarFilas(salonB, dia)).isEqualTo(1);
    }

    @Test
    void distintoDiaPermiteMismasVigencias() {
        UUID salonId = salonA();

        insertarHorario(salonId, (short) 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        insertarHorario(salonId, (short) 2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(contarFilas(salonId, (short) 1)).isEqualTo(1);
        assertThat(contarFilas(salonId, (short) 2)).isEqualTo(1);
    }

    @Test
    void gapEntreVersionesEsPermitidoPorLaBaseDeDatos() {
        UUID salonId = salonA();
        short dia = 3;

        insertarHorario(salonId, dia, null, LocalDate.of(2026, 8, 31));
        insertarHorario(salonId, dia, LocalDate.of(2026, 9, 15), null);

        assertThat(contarFilas(salonId, dia)).isEqualTo(2);
    }

    @Test
    void checkHoraCierreMenorOIgualAAperturaSigueRechazado() {
        UUID salonId = salonA();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into horario_operacion (id, salon_id, dia_semana, hora_apertura, hora_cierre)
                values (?, ?, 1, '20:00', '08:00')
                """, UUID.randomUUID(), salonId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void checkDiaSemanaFueraDeRangoSigueRechazado() {
        UUID salonId = salonA();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into horario_operacion (id, salon_id, dia_semana, hora_apertura, hora_cierre)
                values (?, ?, 7, '08:00', '20:00')
                """, UUID.randomUUID(), salonId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID salonA() {
        return salones().get(0);
    }

    private UUID salonB() {
        return salones().get(1);
    }

    private List<UUID> salones() {
        List<UUID> ids = jdbcTemplate.queryForList("select id from salon order by nombre", UUID.class);
        assertThat(ids).hasSizeGreaterThanOrEqualTo(2);
        return ids;
    }

    private UUID insertarHorario(UUID salonId, short diaSemana, LocalDate desde, LocalDate hasta) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, '08:00', '20:00', ?, ?)
                """,
                id, salonId, diaSemana, desde, hasta);
        return id;
    }

    private int contarFilas(UUID salonId, short diaSemana) {
        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from horario_operacion where salon_id = ? and dia_semana = ?",
                Integer.class, salonId, diaSemana);
        return total;
    }
}
