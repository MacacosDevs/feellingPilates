package com.feelingpilates.usuarios.servicio;

import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.usuarios.entidad.Rol;

import java.util.List;
import java.util.UUID;

/**
 * PERSONAL e INSTRUCTOR pueden estar en una o varias sedes: al menos una es obligatoria.
 * ADMIN y SUPER_ADMIN son roles globales, no se asignan a una sede.
 * CLIENTE es libre: puede no elegir ninguna (cualquier sede) o elegir varias.
 */
final class SedeRolValidador {

    private SedeRolValidador() {
    }

    static void validar(String nombreRol, List<UUID> salonIds) {
        boolean requiereSede = nombreRol.equals(Rol.PERSONAL) || nombreRol.equals(Rol.INSTRUCTOR);
        boolean esGlobal = nombreRol.equals(Rol.ADMIN) || nombreRol.equals(Rol.SUPER_ADMIN);

        if (requiereSede && salonIds.isEmpty()) {
            throw new ValidacionException("Selecciona al menos una sede para este rol");
        }
        if (esGlobal && !salonIds.isEmpty()) {
            throw new ValidacionException("El rol " + nombreRol + " es general y no se asigna a una sede");
        }
    }
}
