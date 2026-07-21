package com.feelingpilates.notificaciones;

public interface EmailService {

    void enviarInvitacionCliente(String correoDestino, String nombre, String enlaceInvitacion);
}
