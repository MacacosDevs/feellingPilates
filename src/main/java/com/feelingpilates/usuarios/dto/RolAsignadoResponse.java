package com.feelingpilates.usuarios.dto;

import java.util.List;
import java.util.UUID;

/** Un rol del usuario junto con las sedes en las que aplica (vacío = sin sede / rol global). */
public record RolAsignadoResponse(String rol, List<UUID> salonIds) {
}
