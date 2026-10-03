package com.feelingpilates.transicion.programacion.read;

import java.time.LocalDate;

public interface NominalProgrammingReadPort {
    NominalProgrammingReadSet readNominalOnDate(NominalReadSnapshotContext context, LocalDate fecha);
}
