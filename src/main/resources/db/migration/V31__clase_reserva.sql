-- V31: reserva de un cliente en una clase, con cupo compartido (a diferencia
-- de "reserva", que es una cita exclusiva 1-a-1 cliente-instructor). La
-- asistencia es un cambio de estado (CONFIRMADA -> ASISTIO) con timestamp,
-- no una tabla aparte: una reserva tiene a lo mas un check-in.
CREATE TABLE clase_reserva (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clase_id       UUID NOT NULL REFERENCES clase (id),
    cliente_id     UUID NOT NULL REFERENCES usuario (id),
    estado         VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA' CHECK (estado IN ('CONFIRMADA', 'CANCELADA', 'ASISTIO')),
    asistio_en     TIMESTAMPTZ,
    creado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (clase_id, cliente_id)
);

CREATE INDEX idx_clase_reserva_cliente ON clase_reserva (cliente_id);
CREATE INDEX idx_clase_reserva_clase_confirmada ON clase_reserva (clase_id) WHERE estado = 'CONFIRMADA';
