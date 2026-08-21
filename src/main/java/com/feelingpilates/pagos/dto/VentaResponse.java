package com.feelingpilates.pagos.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VentaResponse(
        UUID id,
        String clienteNombre,
        String paqueteNombre,
        int montoCentavos,
        String metodoPago,
        String estado,
        OffsetDateTime creadoEn,
        OffsetDateTime fechaExpiracion,
        String registradaPorNombre,
        String salonNombre,
        UUID grupoCompraId,
        Integer numeroItem,
        String motivoEstado
) {
}
