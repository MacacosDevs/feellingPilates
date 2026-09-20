-- V25: registra en que sede se hizo cada compra. Las ventas de caja siempre
-- quedan ligadas a una sede (la del recepcionista, o la que elige un admin);
-- las compras por Stripe desde la app quedan sin sede (null).
ALTER TABLE compra ADD COLUMN salon_id UUID REFERENCES salon (id);
