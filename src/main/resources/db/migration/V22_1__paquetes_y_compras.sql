-- V14: catalogo de paquetes (Pilates / Bacu Fit / combos) y compras pagadas con Stripe.
-- Los montos se guardan en centavos (unidad minima de MXN) porque asi los espera
-- la API de Stripe al crear un PaymentIntent.

CREATE TABLE paquete (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria       VARCHAR(20) NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(255),
    precio_centavos INTEGER NOT NULL,
    vigencia_dias   INTEGER NOT NULL,
    unitario_texto  VARCHAR(50),
    destacado       BOOLEAN NOT NULL DEFAULT false,
    activo          BOOLEAN NOT NULL DEFAULT true,
    orden           INTEGER NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE compra (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID NOT NULL REFERENCES usuario (id),
    paquete_id                  UUID NOT NULL REFERENCES paquete (id),
    monto_centavos              INTEGER NOT NULL,
    moneda                      VARCHAR(3) NOT NULL DEFAULT 'mxn',
    estado                      VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    stripe_payment_intent_id    VARCHAR(100) UNIQUE,
    fecha_expiracion            TIMESTAMPTZ,
    creado_en                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_compra_usuario ON compra (usuario_id);

-- Semilla: mismo catalogo que hoy vive hardcodeado en PackagesScreen.tsx
-- (feellingPilatesApp/src/screens/PackagesScreen.tsx), para que deje de ser mock.
INSERT INTO paquete (categoria, nombre, descripcion, precio_centavos, vigencia_dias, unitario_texto, destacado, orden) VALUES
    ('pilates', '4 clases',  '4 clases grupales de Reformer o Mat, a tu elección.', 90000,  30, '$225 c/u', false, 1),
    ('pilates', '8 clases',  '8 clases grupales de Reformer o Mat, a tu elección.', 160000, 45, '$200 c/u', true,  2),
    ('pilates', '12 clases', '12 clases grupales de Reformer o Mat, a tu elección.', 216000, 60, '$180 c/u', false, 3),
    ('bacu_fit', '4 rentas Bacu Fit',  '4 sesiones individuales en la máquina Bacu Fit.', 100000, 30, '$250 c/u', false, 1),
    ('bacu_fit', '8 rentas Bacu Fit',  '8 sesiones individuales en la máquina Bacu Fit.', 184000, 45, '$230 c/u', true,  2),
    ('bacu_fit', '12 rentas Bacu Fit', '12 sesiones individuales en la máquina Bacu Fit.', 252000, 60, '$210 c/u', false, 3),
    ('combo', 'Combo 4 + 4', '4 clases de Pilates + 4 rentas de Bacu Fit.', 175000, 30, 'Ahorras $150', false, 1),
    ('combo', 'Combo 8 + 8', '8 clases de Pilates + 8 rentas de Bacu Fit.', 320000, 45, 'Ahorras $240', true,  2);
