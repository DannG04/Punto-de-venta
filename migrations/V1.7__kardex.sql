BEGIN;

-- Tabla kardex: un registro por cada vez que entra o sale una unidad
CREATE TABLE IF NOT EXISTS kardex (
    id_kardex            SERIAL PRIMARY KEY,
    id_producto          VARCHAR(50) REFERENCES producto(id_producto),
    fecha                TIMESTAMP DEFAULT NOW(),
    tipo_movimiento      VARCHAR(20) CHECK (tipo_movimiento IN ('Venta','Compra','Devolucion','Ajuste')),
    cantidad             INTEGER NOT NULL,          -- negativo en ventas, positivo en compras/devoluciones
    existencia_anterior  INTEGER NOT NULL,
    existencia_posterior INTEGER NOT NULL,
    referencia           VARCHAR(15),               -- id_venta / id_compra / id_devolucion
    id_empleado          CHAR(18) REFERENCES empleado(id_empleado)
);

CREATE OR REPLACE FUNCTION fn_kardex_producto()
RETURNS TRIGGER AS $$
DECLARE
    v_tipo VARCHAR(20);
    v_ref  VARCHAR(15);
    v_emp  CHAR(18);
BEGIN
    IF NEW.cantidad IS DISTINCT FROM OLD.cantidad THEN

        v_tipo := NULLIF(current_setting('kardex.tipo', true), '');
        v_ref  := NULLIF(current_setting('kardex.referencia', true), '');
        v_emp  := NULLIF(current_setting('kardex.empleado', true), '')::CHAR(18);

        -- Aumento sin tipo explícito = reversión interna del SP (p.ej. cambio de
        -- cantidad en carrito). Elimina el kardex del decrement previo y no registra.
        IF NEW.cantidad > OLD.cantidad AND v_tipo IS NULL THEN
            DELETE FROM kardex
            WHERE id_producto = NEW.id_producto
              AND existencia_posterior = OLD.cantidad
              AND fecha >= (NOW() - INTERVAL '2 seconds');
            RETURN NEW;
        END IF;

        -- Si no viene tipo en la sesión, infiere por dirección del cambio
        IF v_tipo IS NULL THEN
            v_tipo := CASE WHEN NEW.cantidad < OLD.cantidad THEN 'Venta' ELSE 'Compra' END;
        END IF;

        INSERT INTO kardex (
            id_producto, tipo_movimiento, cantidad,
            existencia_anterior, existencia_posterior,
            referencia, id_empleado
        ) VALUES (
            NEW.id_producto, v_tipo, NEW.cantidad - OLD.cantidad,
            OLD.cantidad, NEW.cantidad,
            v_ref, v_emp
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger en tabla producto: se dispara después de cada UPDATE en la columna cantidad
DROP TRIGGER IF EXISTS trg_kardex_producto ON producto;
CREATE TRIGGER trg_kardex_producto
AFTER UPDATE OF cantidad ON producto
FOR EACH ROW EXECUTE FUNCTION fn_kardex_producto();

COMMIT;
