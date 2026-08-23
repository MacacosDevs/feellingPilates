package com.feelingpilates.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Fuente temporal central de la aplicacion.
 *
 * <p>F2B.2 centraliza el "hoy" de negocio en un {@link Clock} inyectable para que los servicios
 * que resuelven versiones de horario sean deterministas en tests. Deliberadamente se usa
 * {@link Clock#systemDefaultZone()}: el proyecto no declara ninguna zona horaria de negocio
 * (ni en {@code application.properties} ni en el codigo), asi que el comportamiento actual es
 * el de la zona por defecto de la JVM del servidor. Fijar aqui una zona concreta
 * ({@code America/Mexico_City}, {@code UTC}, ...) seria un cambio de semantica encubierto,
 * fuera del alcance de esta fase.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
