-- FUNCTION: public.reporte_diario(date)

-- DROP FUNCTION IF EXISTS public.reporte_diario(date);

CREATE OR REPLACE FUNCTION public.reporte_diario(
	p_fecha date)
    RETURNS TABLE(total_ventas numeric, ventas_netas numeric, total_devoluciones numeric, num_transacciones bigint, ticket_promedio numeric, total_compras numeric, total_gastos numeric, total_otras_ganancias numeric, resultado_neto numeric, valor_inventario numeric) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE
    fecha DATE := p_fecha;
    v_total_ventas          NUMERIC(10,2);
    v_total_devoluciones    NUMERIC(10,2);
    v_total_compras         NUMERIC(10,2);
    v_total_gastos          NUMERIC(10,2);
    v_total_otras_ganancias NUMERIC(10,2);
    v_num_transacciones     BIGINT;
BEGIN
    SELECT COALESCE(SUM(total_venta), 0), COUNT(*)
        INTO v_total_ventas, v_num_transacciones
        FROM venta WHERE fecha_venta = fecha;

    SELECT COALESCE(SUM(monto), 0)
        INTO v_total_devoluciones
        FROM devolucion_ventas WHERE fecha_devolucion = fecha;

    SELECT COALESCE(SUM(monto), 0)
        INTO v_total_compras
        FROM compras WHERE fecha_compra = fecha;

    SELECT COALESCE(SUM(monto), 0)
        INTO v_total_gastos
        FROM gastos WHERE fecha_gasto = fecha;

    SELECT COALESCE(SUM(monto), 0)
        INTO v_total_otras_ganancias
        FROM otras_ganancias WHERE fecha_otras_ganancias = fecha;

    RETURN QUERY
    SELECT
        v_total_ventas,
        v_total_ventas - v_total_devoluciones,
        v_total_devoluciones,
        v_num_transacciones,
        CASE WHEN v_num_transacciones > 0
             THEN ROUND(v_total_ventas / v_num_transacciones, 2)
             ELSE 0 END,
        v_total_compras,
        v_total_gastos,
        v_total_otras_ganancias,
        -- resultado neto del día
        (v_total_ventas - v_total_devoluciones + v_total_otras_ganancias)
            - (v_total_compras + v_total_gastos),
        -- valor del inventario actual a precio de compra
        (SELECT COALESCE(SUM(p.cantidad * p.precio_compra), 0) FROM producto p);
END;
$BODY$;

ALTER FUNCTION public.reporte_diario(date)
    OWNER TO postgres;

-- ACTUALIZACIÓN DE LA FUNCION PARA INACTIVAR EMPLEADOS

CREATE OR REPLACE FUNCTION public.inactivar_empleado(
	idemp curp_dominio,
	inact boolean)
    RETURNS void
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
	cant INTEGER;
	usur VARCHAR(20);
BEGIN
	SELECT COUNT(id_empleado) INTO cant FROM empleado WHERE id_empleado=idemp;
    IF cant=0 THEN
        RAISE EXCEPTION 'No se encontró al empleado';
    ELSE
		SELECT usuario INTO usur FROM empleado WHERE id_empleado=idemp;
        IF inact=TRUE THEN
			UPDATE empleado SET estatus='Inactivo' WHERE id_empleado=idemp;
		ELSE
			UPDATE empleado SET estatus='Activo' WHERE id_empleado=idemp;
		END IF;
    END IF;
END;
$BODY$;

-- ACTUALIZACIÓN DE LA FUNCION PARA ELIMINAR DEVOLUCIONES
CREATE OR REPLACE FUNCTION public.elim_devolucion(
	idedev character varying)
    RETURNS void
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
	mon INTEGER;
BEGIN
	SELECT COUNT(*) INTO mon FROM devolucion_ventas_detalle WHERE id_devolucion=idedev;
	IF mon=0 THEN
		DELETE FROM devolucion_ventas WHERE id_devolucion=idedev;
	ELSE
		RAISE EXCEPTION 'No es posible eliminar una devolucion con registros'; 
	END IF;
END;
$BODY$;
