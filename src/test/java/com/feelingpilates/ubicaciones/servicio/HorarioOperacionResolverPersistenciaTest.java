package com.feelingpilates.ubicaciones.servicio;

import com.feelingpilates.TestcontainersConfiguration;
import com.feelingpilates.ubicaciones.entidad.HorarioOperacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostgreSQL/Testcontainers real contra HorarioOperacionResolver con dos versiones
 * contiguas reales (F2B.3a, ex_horario_operacion_vigencia). El caso ">1 fila vigente"
 * ya no se prueba aqui porque el EXCLUDE lo vuelve fisicamente imposible; ese caso
 * defensivo sigue cubierto con mocks en HorarioOperacionResolverTest.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class HorarioOperacionResolverPersistenciaTest {

    @Autowired
    private HorarioOperacionResolver resolver;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void resuelveCadaFechaASuVersionContiguaSinAmbiguedad() {
        UUID salonId = jdbcTemplate.queryForObject("select id from salon limit 1", UUID.class);
        // 2026-08-24 y 2026-08-31 son ambos lunes (dia_semana = 1).
        LocalDate lunesAnterior = LocalDate.of(2026, 8, 24);
        LocalDate lunesPosterior = LocalDate.of(2026, 8, 31);

        insertarHorario(salonId, (short) 1, LocalTime.of(7, 0), LocalTime.of(15, 0), null, lunesAnterior);
        insertarHorario(salonId, (short) 1, LocalTime.of(9, 0), LocalTime.of(21, 0), lunesPosterior, null);

        HorarioOperacion anterior = resolver.resolver(salonId, lunesAnterior).orElseThrow();
        assertThat(anterior.getHoraApertura()).isEqualTo(LocalTime.of(7, 0));

        HorarioOperacion posterior = resolver.resolver(salonId, lunesPosterior).orElseThrow();
        assertThat(posterior.getHoraApertura()).isEqualTo(LocalTime.of(9, 0));
    }

    private void insertarHorario(UUID salonId, short diaSemana, LocalTime apertura, LocalTime cierre,
            LocalDate desde, LocalDate hasta) {
        jdbcTemplate.update("""
                insert into horario_operacion
                    (id, salon_id, dia_semana, hora_apertura, hora_cierre, vigente_desde, vigente_hasta)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), salonId, diaSemana, apertura, cierre, desde, hasta);
    }
}
