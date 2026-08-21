package com.feelingpilates.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompletarInvitacionRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 72) String contrasena) {

    @Override
    public String toString() {
        return "CompletarInvitacionRequest[token=[REDACTADO], contrasena=[REDACTADA]]";
    }
}
