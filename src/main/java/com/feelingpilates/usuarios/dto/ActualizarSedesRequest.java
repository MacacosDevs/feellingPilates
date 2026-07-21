package com.feelingpilates.usuarios.dto;

import java.util.List;
import java.util.UUID;

public record ActualizarSedesRequest(List<UUID> salonIds) {
}
