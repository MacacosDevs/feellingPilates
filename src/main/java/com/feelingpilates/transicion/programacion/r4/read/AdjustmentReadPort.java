package com.feelingpilates.transicion.programacion.r4.read;
import java.time.LocalDate;
public interface AdjustmentReadPort {
    AdjustmentReadSet readActiveAdjustmentsOnDate(AdjustmentReadSnapshotContext context, LocalDate fecha);
}
