package com.feelingpilates.programacion.servicio;

import com.feelingpilates.exception.ConflictException;

/** Conflicto interno con evidencia estructurada del backstop PostgreSQL exacto. */
public class ConflictoAjusteProgramacionException extends ConflictException {

    private final String sqlState;
    private final String constraint;

    public ConflictoAjusteProgramacionException(
            String mensaje, String sqlState, String constraint) {
        super(mensaje);
        this.sqlState = sqlState;
        this.constraint = constraint;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getConstraint() {
        return constraint;
    }
}
