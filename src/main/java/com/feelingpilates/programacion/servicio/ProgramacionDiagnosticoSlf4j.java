package com.feelingpilates.programacion.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProgramacionDiagnosticoSlf4j implements ProgramacionDiagnostico {

    private static final Logger LOG = LoggerFactory.getLogger(ProgramacionDiagnosticoSlf4j.class);

    @Override
    public void registrarOmision(Omision omision) {
        LOG.warn("programacion_omitida causa={} referencia={} fecha={} salon={} instructor={} actividad={}",
                omision.causa(), omision.referencia(), omision.fecha(), omision.salonId(),
                omision.instructorId(), omision.actividadId());
    }
}
