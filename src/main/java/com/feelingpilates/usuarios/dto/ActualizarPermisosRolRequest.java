package com.feelingpilates.usuarios.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ActualizarPermisosRolRequest(@NotNull List<String> permisos) {
}
