package com.feelingpilates.ubicaciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SalonRequest(
        @NotBlank String nombre,
        @NotNull Short estadoId,
        @NotNull Short municipioId,
        String telefono,
        String calle,
        String numeroExterior,
        String numeroInterior,
        String colonia,
        String codigoPostal,
        String referencias,
        String direccionCompleta,
        Double latitud,
        Double longitud,
        List<UUID> tipoActividadIds,
        @Valid List<HorarioOperacionRequest> horarios,
        @Valid List<RecursoItem> recursos) {
}
