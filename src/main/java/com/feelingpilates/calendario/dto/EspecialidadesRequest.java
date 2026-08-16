package com.feelingpilates.calendario.dto;

import java.util.List;
import java.util.UUID;

public record EspecialidadesRequest(List<UUID> tipoActividadIds) {
}
