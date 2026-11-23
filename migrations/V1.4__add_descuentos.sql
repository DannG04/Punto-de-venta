BEGIN;

-- Agregar max_descuento a producto (limite maximo de descuento permitido)
ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS max_descuento NUMERIC(5,2) NOT NULL DEFAULT 100;

-- Agregar descuento_pct a venta_temp
ALTER TABLE venta_temp
    ADD COLUMN IF NOT EXISTS descuento_pct NUMERIC(5,2) NOT NULL DEFAULT 0;

-- Agregar descuento_pct a venta_detalle
ALTER TABLE venta_detalle
    ADD COLUMN IF NOT EXISTS descuento_pct NUMERIC(5,2) NOT NULL DEFAULT 0;

-- Funcion para actualizar el descuento de un producto en venta_temp
-- y recalcular precio_total con el descuento aplicado
CREATE OR REPLACE FUNCTION actualizar_desc_temp(
    p_id_producto id_producto_dominio,
    p_descuento   NUMERIC
) RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE venta_temp
    SET descuento_pct = p_descuento,
        precio_total  = ROUND(precio_dado * cantidad_prod * (1.0 - p_descuento / 100.0), 2)
    WHERE id_producto = p_id_producto;
END;
$$;

COMMIT;
