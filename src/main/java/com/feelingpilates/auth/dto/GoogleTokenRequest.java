package com.feelingpilates.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Token de ID emitido por Google (flujo OAuth2 del cliente). */
public record GoogleTokenRequest(@NotBlank String idToken) {
}
