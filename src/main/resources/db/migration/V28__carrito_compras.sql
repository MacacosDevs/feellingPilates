-- V28: soporte de carrito de compras (varios paquetes en un mismo PaymentIntent).
-- Antes cada Compra era la unica fila con su stripe_payment_intent_id /
-- idempotency_key (constraints UNIQUE). Un carrito ahora genera una Compra
-- por paquete elegido y todas comparten el mismo PaymentIntent y la misma
-- clave de idempotencia del intento de checkout, asi que esas columnas dejan
-- de ser unicas (se preservan como indices normales, ya usados para las
-- busquedas del webhook y del reintento por idempotencia).
DO $$
DECLARE
    nombre_constraint text;
BEGIN
    SELECT conname INTO nombre_constraint
    FROM pg_constraint
    WHERE conrelid = 'compra'::regclass AND contype = 'u'
      AND conkey = (SELECT array_agg(attnum) FROM pg_attribute
                    WHERE attrelid = 'compra'::regclass AND attname = 'stripe_payment_intent_id');
    IF nombre_constraint IS NOT NULL THEN
        EXECUTE format('ALTER TABLE compra DROP CONSTRAINT %I', nombre_constraint);
    END IF;

    SELECT conname INTO nombre_constraint
    FROM pg_constraint
    WHERE conrelid = 'compra'::regclass AND contype = 'u'
      AND conkey = (SELECT array_agg(attnum) FROM pg_attribute
                    WHERE attrelid = 'compra'::regclass AND attname = 'idempotency_key');
    IF nombre_constraint IS NOT NULL THEN
        EXECUTE format('ALTER TABLE compra DROP CONSTRAINT %I', nombre_constraint);
    END IF;
END $$;

CREATE INDEX idx_compra_stripe_payment_intent_id ON compra (stripe_payment_intent_id);
CREATE INDEX idx_compra_idempotency_key ON compra (idempotency_key);
