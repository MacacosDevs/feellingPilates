package com.feelingpilates.notificaciones;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class EmailServiceConsolaTest {

    private final EmailServiceConsola emailService = new EmailServiceConsola();

    @Test
    void noRegistraTokenNiUrlDeInvitacion(CapturedOutput output) {
        String token = "token-invitacion-super-secreto";
        String enlace = "https://app.example.com/invitacion/" + token;

        emailService.enviarInvitacionCliente("aldair@example.com", "Aldair", enlace);

        assertThat(output.getOut())
                .contains("[EMAIL SIMULADO] Invitación generada")
                .doesNotContain(token)
                .doesNotContain(enlace)
                .doesNotContain("aldair@example.com");
    }
}
