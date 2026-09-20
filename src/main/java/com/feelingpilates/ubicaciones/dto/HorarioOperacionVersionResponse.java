package com.feelingpilates.ubicaciones.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Una version del horario semanal de un dia: representa por igual el resultado de versionar, de
 * cerrar y cada elemento del historial. Sin {@code id} ni {@code salonId} (F2B.3b.2a, §7.3 y §25
 * del diseño): no existe operacion HTTP direccionada por version, y el salon ya esta en la URL.
 * {@code vigenteDesde}/{@code vigenteHasta} nulos se preservan tal cual: {@code null} es
 * -infinito/+infinito, no un sentinel.
 */
public record HorarioOperacionVersionResponse(
        short diaSemana,
        LocalTime horaApertura,
        LocalTime horaCierre,
        LocalDate vigenteDesde,
        LocalDate vigenteHasta) {
}
