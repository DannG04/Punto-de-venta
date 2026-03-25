BEGIN;

CREATE OR REPLACE FUNCTION public.reporte_diario(p_fecha DATE)
RETURNS TABLE(
    total_ventas numeric,
    total_devoluciones numeric,
    total_compras numeric,
    total_gastos numeric,
    total_otras_ganancias numeric
)
LANGUAGE plpgsql AS $$
DECLARE
    inv_fin NUMERIC(10,2);
BEGIN
    SELECT COALESCE(SUM(m), 0) INTO inv_fin FROM (
        SELECT p.id_producto, p.cantidad * precio_adquirido AS m
        FROM producto AS p, compras NATURAL JOIN compra_producto AS cp
        WHERE p.id_producto = cp.id_producto
        AND fecha_compra = p_fecha
    ) AS sub;

    RETURN QUERY
    SELECT
        COALESCE((SELECT SUM(total_venta) FROM venta WHERE fecha_venta = p_fecha), 0),
        COALESCE((SELECT SUM(monto) FROM devolucion_ventas WHERE fecha_devolucion = p_fecha), 0),
        COALESCE((SELECT SUM(monto) FROM compras WHERE fecha_compra = p_fecha), 0),
        COALESCE((SELECT SUM(monto) FROM gastos WHERE fecha_gasto = p_fecha), 0),
        COALESCE((SELECT SUM(monto) FROM otras_ganancias WHERE fecha_otras_ganancias = p_fecha), 0);
END;
$$;

COMMIT;
