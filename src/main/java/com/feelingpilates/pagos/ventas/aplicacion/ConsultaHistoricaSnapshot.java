package com.feelingpilates.pagos.ventas.aplicacion;

import java.util.*;

public interface ConsultaHistoricaSnapshot {
    Optional<CompraHistoricaSnapshot> buscar(UUID clienteId, UUID ordenId);
    List<CompraHistoricaSnapshot> listar(UUID clienteId);
}
