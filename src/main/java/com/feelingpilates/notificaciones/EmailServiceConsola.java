package com.feelingpilates.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación de desarrollo: no hay proveedor de correo configurado todavía,
 * así que el envío se simula dejando el enlace en el log del servidor.
 * Reemplazar por una implementación real (SMTP/SES/SendGrid) cuando se defina el proveedor.
 */
@Service
public class EmailServiceConsola implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceConsola.class);

    @Override
    public void enviarInvitacionCliente(String correoDestino, String nombre, String enlaceInvitacion) {
        log.info("[EMAIL SIMULADO] Invitación para {} <{}>: {}", nombre, correoDestino, enlaceInvitacion);
    }
}
