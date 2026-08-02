package com.feelingpilates.pagos.dto;

import java.util.UUID;

public record ReembolsoResponse(
        UUID compraId,
        String estado,
        int montoReembolsadoCentavos
) {
}
