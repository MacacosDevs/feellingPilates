package com.feelingpilates.programacion.repositorio;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostgreSQL real contra {@code buscarActivosVigentesDesde}, la query que alimenta la validacion
 * inversa de bloques. Fija el predicado: "la vigencia del bloque intersecta {@code [desde, +inf)}",
 * ni mas ni menos.
 *
 * <p>La mitad importante es la <b>negativa</b>: un bloque cuya vigencia termino antes de {@code D}
 * NO debe aparecer. Si apareciera, un versionado efectivo en septiembre podria rechazarse por una
 * incompatibilidad de febrero que ese cambio ni causa ni modifica.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class BloqueProgramacionRepositoryVigenciaTest {

    private static final short LUNES = 1;
    private static final LocalDate D = LocalDate.of(2027, 9, 1);

    @Autowired
    private BloqueProgramacionRepository bloqueRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bloqueAbiertoQueEmpiezaAntesDeDSiIntersecta() {
        UUID salonId = salon();
        UUID id = insertar(salonId, LocalDate.of(2027, 1, 1), null, true);

        assertThat(ids(salonId)).containsExactly(id);
    }

    @Test
    void bloqueQueTerminaExactamenteEnDSiIntersecta() {
        UUID salonId = salon();
        UUID id = insertar(salonId, LocalDate.of(2027, 1, 1), D, true);

        assertThat(ids(salonId)).containsExactly(id);
    }

    /** Mutacion: revalidar el pasado anterior a D. */
    @Test
    void bloqueQueTerminaElDiaAntesDeDNoIntersecta() {
        UUID salonId = salon();
        insertar(salonId, LocalDate.of(2027, 1, 1), D.minusDays(1), true);

        assertThat(ids(salonId)).isEmpty();
    }

    @Test
    void bloqueQueEmpiezaDespuesDeDSiIntersecta() {
        UUID salonId = salon();
        UUID id = insertar(salonId, D.plusDays(30), null, true);

        assertThat(ids(salonId)).containsExactly(id);
    }

    @Test
    void bloqueInactivoNoSeDevuelve() {
        UUID salonId = salon();
        insertar(salonId, LocalDate.of(2027, 1, 1), null, false);

        assertThat(ids(salonId)).isEmpty();
    }

    @Test
    void bloqueDeOtroDiaNoSeDevuelve() {
        UUID salonId = salon();
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin,
                     vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, 2, '08:00', '09:00', ?, null, true)
                """, id, UUID.randomUUID(), salonId, LocalDate.of(2027, 1, 1));

        assertThat(ids(salonId)).isEmpty();
    }

    private List<UUID> ids(UUID salonId) {
        return bloqueRepository.buscarActivosVigentesDesde(salonId, LUNES, D).stream()
                .map(BloqueProgramacion::getId)
                .toList();
    }

    private UUID salon() {
        return jdbcTemplate.queryForObject("select id from salon order by nombre limit 1", UUID.class);
    }

    private UUID insertar(UUID salonId, LocalDate desde, LocalDate hasta, boolean activo) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into programacion_bloque
                    (id, serie_id, salon_id, dia_semana, hora_inicio, hora_fin,
                     vigente_desde, vigente_hasta, activo)
                values (?, ?, ?, ?, '08:00', '09:00', ?, ?, ?)
                """, id, UUID.randomUUID(), salonId, LUNES, desde, hasta, activo);
        return id;
    }
}
