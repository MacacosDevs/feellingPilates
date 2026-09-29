package com.feelingpilates.transicion.programacion.read;

public interface LegacyTurnReadPort {

    LegacyTurnReadSet readForDate(LegacyTurnReadContext context, LegacyTurnScope scope);
}
