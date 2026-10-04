package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.entidad.BloqueProgramacion;
import com.feelingpilates.programacion.repositorio.BloqueProgramacionRepository;
import com.feelingpilates.ubicaciones.dominio.CambioHorarioOperacion;
import com.feelingpilates.ubicaciones.dominio.ConflictoProgramacion;
import com.feelingpilates.ubicaciones.dominio.ValidadorImpactoCambioHorarioOperacion;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter de {@code programacion} para la validacion inversa: dice que bloques activos quedarian
 * incompatibles si se aplicara un {@link CambioHorarioOperacion}.
 *
 * <p>Depende <b>solo del repositorio</b> y no de {@link BloqueProgramacionService}, que a su vez
 * depende de {@code ubicaciones}: usarlo cerraria un ciclo de beans.
 *
 * <p>Solo se considera la porcion afectada de cada bloque, {@code vigenciaBloque ∩ [D, +infinito)}:
 * un bloque que empieza antes de D y sigue despues si entra, pero solo se le exige encajar en el
 * horario <b>resultante</b>, que rige desde D. Nunca se re-evalua el tramo anterior a D: una
 * inconsistencia previa no puede rechazar un cambio que no la causo ni la modifica.
 *
 * <p>No hace falta analisis de cobertura ni de gaps: tras un versionado permitido, el tramo
 * {@code [D, +infinito)} queda cubierto exactamente por la version nueva {@code D/NULL}, porque el
 * append solo se admite cuando no existen versiones posteriores. Basta la contencion horaria.
 */
@Component
public class ImpactoBloquesEnHorario implements ValidadorImpactoCambioHorarioOperacion {

    private final BloqueProgramacionRepository bloqueRepository;

    public ImpactoBloquesEnHorario(BloqueProgramacionRepository bloqueRepository) {
        this.bloqueRepository = bloqueRepository;
    }

    /**
     * Con el cambio ABIERTO, conflicto es el bloque cuyas horas no caben en la nueva
     * apertura/cierre. Con el cambio CERRADO, cualquier bloque activo que siga aplicando desde D
     * es conflicto.
     *
     * <p>Politica A: no se recorta el bloque, no se desactiva y no se marca invalido; se rechaza el
     * cambio de horario. Consecuencia aceptada y fail-closed: tambien en alta y reapertura, donde
     * el cambio solo añade cobertura donde no habia ninguna, un bloque activo que no quepa
     * rechaza la operacion. La salida del operador es ajustar o desactivar el bloque primero.
     */
    @Override
    public List<ConflictoProgramacion> evaluar(CambioHorarioOperacion cambio) {
        List<BloqueProgramacion> bloques = bloqueRepository.buscarActivosVigentesDesde(
                cambio.salonId(), cambio.diaSemana(), cambio.efectivoDesde());

        return bloques.stream()
                .filter(bloque -> !cambio.admite(bloque.getHoraInicio(), bloque.getHoraFin()))
                .map(bloque -> ConflictoProgramacion.bloqueProgramacion(
                        bloque.getId(), bloque.getHoraInicio() + "-" + bloque.getHoraFin()))
                .toList();
    }
}
