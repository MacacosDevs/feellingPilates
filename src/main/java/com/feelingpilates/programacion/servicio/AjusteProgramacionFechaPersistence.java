package com.feelingpilates.programacion.servicio;

import com.feelingpilates.programacion.entidad.AjusteProgramacionFecha;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/** Ciclo JPA explícito para UUID asignado: create=persist; update/retiro=managed+flush. */
@Component
public class AjusteProgramacionFechaPersistence {

    private final EntityManager entityManager;

    public AjusteProgramacionFechaPersistence(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AjusteProgramacionFecha crear(AjusteProgramacionFecha ajuste) {
        entityManager.persist(ajuste);
        entityManager.flush();
        return ajuste;
    }

    /**
     * Fuerza que una relectura posterior a los locks observe la fila confirmada en la base y no
     * la instancia conservada por el first-level cache de la transacción actual.
     */
    public void refrescar(AjusteProgramacionFecha ajuste) {
        entityManager.refresh(ajuste);
    }

    public void flushManaged() {
        entityManager.flush();
    }
}
