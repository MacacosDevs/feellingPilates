-- V23: paquetes abiertos a cualquier actividad del catalogo (ya no solo pilates/
-- bacu_fit/combo, para poder mezclar reformer u otras actividades futuras sin
-- tocar codigo) y venta presencial en caja (efectivo/transferencia), sin Stripe.

ALTER TABLE paquete ALTER COLUMN categoria DROP NOT NULL;

CREATE TABLE paquete_actividad (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paquete_id        UUID NOT NULL REFERENCES paquete (id) ON DELETE CASCADE,
    tipo_actividad_id UUID NOT NULL REFERENCES tipo_actividad (id),
    cantidad_clases   INTEGER NOT NULL,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (paquete_id, tipo_actividad_id)
);

ALTER TABLE compra
    ADD COLUMN metodo_pago       VARCHAR(20) NOT NULL DEFAULT 'stripe',
    ADD COLUMN registrada_por_id UUID REFERENCES usuario (id);

INSERT INTO permiso (codigo, descripcion, categoria) VALUES
    ('pagos.paquetes.gestionar', 'Crear, editar y deshabilitar paquetes de clases', 'PAGOS');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.nombre = 'ADMIN' AND p.codigo = 'pagos.paquetes.gestionar';
