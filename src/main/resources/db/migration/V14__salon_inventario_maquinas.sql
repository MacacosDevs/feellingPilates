-- V14: simplifica "espacios" (cuartos con nombre/cupo propios) a un inventario
-- directo de maquinas por salon, mas un flag de si el salon permite acomodar
-- clientes en pareja (usando las maquinas disponibles, ej. Reformers).

ALTER TABLE salon ADD COLUMN permite_pareja BOOLEAN NOT NULL DEFAULT false;

DROP TABLE espacio_maquina;
DROP TABLE espacio;

CREATE TABLE salon_maquina (
    salon_id        UUID NOT NULL REFERENCES salon (id),
    tipo_maquina_id UUID NOT NULL REFERENCES tipo_maquina (id),
    cantidad        SMALLINT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (salon_id, tipo_maquina_id)
);
