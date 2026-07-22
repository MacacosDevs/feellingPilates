-- V12: direccion estructurada, telefono de atencion y coordenadas (Google Maps) por salon.
-- "direccion" se mantiene como el texto completo formateado (lo entrega Google Places),
-- y las columnas nuevas guardan las partes estructuradas para edicion/busqueda.

ALTER TABLE salon
    ADD COLUMN telefono          VARCHAR(20),
    ADD COLUMN calle             VARCHAR(150),
    ADD COLUMN numero_exterior   VARCHAR(20),
    ADD COLUMN numero_interior   VARCHAR(20),
    ADD COLUMN colonia           VARCHAR(150),
    ADD COLUMN codigo_postal     VARCHAR(10),
    ADD COLUMN referencias       VARCHAR(255),
    ADD COLUMN latitud           DOUBLE PRECISION,
    ADD COLUMN longitud          DOUBLE PRECISION;
