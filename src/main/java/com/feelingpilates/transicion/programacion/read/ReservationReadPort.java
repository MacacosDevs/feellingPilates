package com.feelingpilates.transicion.programacion.read;

import com.feelingpilates.transicion.programacion.detector.ReservationSourceSnapshot;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ReservationReadPort {

    List<ReservationSourceSnapshot> readByReservationIds(
            ReadSnapshotContext context,
            Set<UUID> reservationIds);

    List<ReservationSourceSnapshot> readByScope(
            ReadSnapshotContext context,
            ReservationScope scope);
}
