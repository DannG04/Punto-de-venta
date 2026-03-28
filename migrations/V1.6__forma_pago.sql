BEGIN;

-- Agregar columna forma_pago a la tabla venta para el caso simple de una sola forma de pago
ALTER TABLE venta ADD COLUMN IF NOT EXISTS forma_pago VARCHAR(20) DEFAULT 'Efectivo';

-- Nueva tabla para desglose de pagos (cuando una venta tiene múltiples formas de pago)
CREATE TABLE IF NOT EXISTS forma_pago_venta (
    id_venta    VARCHAR(15) REFERENCES venta(id_venta),
    forma_pago  VARCHAR(20) CHECK (forma_pago IN ('Efectivo','Transferencia','Tarjeta','Otro')),
    monto       NUMERIC(10,2),
    PRIMARY KEY (id_venta, forma_pago)
);

COMMIT;
