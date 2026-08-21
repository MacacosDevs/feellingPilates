package com.feelingpilates.pagos.dto;

import java.util.List;
import java.util.UUID;

public record VentaCarritoResponse(
        UUID grupoCompraId,
        List<VentaResponse> items,
        int totalCentavos
) {
}
