-- V15: clave de idempotencia por compra. La app movil genera una clave unica
-- por cada intento de compra (no por cada tap); si el usuario reintenta tras
-- un timeout de red o un doble tap, el backend reutiliza el PaymentIntent ya
-- creado en vez de generar uno duplicado.
ALTER TABLE compra ADD COLUMN idempotency_key VARCHAR(100) UNIQUE;
