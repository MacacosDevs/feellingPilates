-- V24: los paquetes sembrados en V22_1 eran solo mock estatico (categoria fija
-- pilates/bacu_fit/combo). Ahora que el catalogo de paquetes se gestiona desde
-- Caja > Paquetes (abierto a cualquier actividad), se quitan para arrancar
-- vacio: el negocio los ira dando de alta el mismo.
DELETE FROM paquete WHERE categoria IS NOT NULL;
