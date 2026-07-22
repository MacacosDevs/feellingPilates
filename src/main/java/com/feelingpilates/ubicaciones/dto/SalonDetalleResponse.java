package com.feelingpilates.ubicaciones.dto;

import java.util.List;
import java.util.UUID;

public record SalonDetalleResponse(
        UUID id,
        String nombre,
        Short estadoId,
        String estadoNombre,
        Short municipioId,
        String municipioNombre,
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
        boolean activo,
        List<TipoActividadResponse> tiposActividad,
        List<HorarioOperacionResponse> horarios,
        List<EspacioResponse> espacios) {
}
