package com.feelingpilates.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompletarInvitacionRequestTest {

    @Test
    void toStringNoExponeTokenNiContrasena() {
        String token = "token-invitacion-super-secreto";
        String contrasena = "contrasena-super-secreta";

        String representacion = new CompletarInvitacionRequest(token, contrasena).toString();

        assertThat(representacion)
                .contains("token=[REDACTADO]")
                .contains("contrasena=[REDACTADA]")
                .doesNotContain(token)
                .doesNotContain(contrasena);
    }
}
