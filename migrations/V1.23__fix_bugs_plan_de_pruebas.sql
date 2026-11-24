BEGIN;

-- Correcciones de los defectos encontrados por el plan de pruebas del 2026-11-23
-- (docs/pruebas/PLAN_DE_PRUEBAS.md, sección 6). No incluye los de kardex (D-05/06/07).

-- D-02 / D-04) Precio fijo en el carrito.
-- Los triggers de venta_temp recalculaban SIEMPRE el precio por cantidad
-- (>= 12 mayoreo, si no menudeo). Eso pisaba el precio de una lista de precios
-- elegida en Ventas y el precio cotizado al convertir una cotización.
-- Con precio_fijo = true el renglón conserva precio_dado y tipo_venta.
ALTER TABLE venta_temp ADD COLUMN IF NOT EXISTS precio_fijo boolean NOT NULL DEFAULT false;

-- D-01) Además: no se puede agregar al carrito un producto dado de baja.
CREATE OR REPLACE FUNCTION public.verif_exist_insert()
    RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    neim  VARCHAR(50);
    cant  INTEGER;
    est   VARCHAR(10);
BEGIN
    SELECT cantidad, nombre, estatus INTO cant, neim, est FROM producto WHERE id_producto = NEW.id_producto;
    IF est IS DISTINCT FROM 'Activo' THEN
        RAISE EXCEPTION 'El producto % está dado de baja y no se puede vender', NEW.id_producto;
    END IF;
    IF cant < NEW.cantidad_prod THEN
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
    NEW.nombre_p := neim;
    IF NOT NEW.precio_fijo OR NEW.precio_dado IS NULL OR NEW.tipo_venta IS NULL THEN
        IF NEW.cantidad_prod >= 12 THEN
            SELECT precio_mayoreo INTO NEW.precio_dado FROM producto WHERE id_producto = NEW.id_producto;
            NEW.tipo_venta := 'Mayoreo';
        ELSE
            SELECT precio_menudeo INTO NEW.precio_dado FROM producto WHERE id_producto = NEW.id_producto;
            NEW.tipo_venta := 'Menudeo';
        END IF;
    END IF;
    NEW.precio_total := ROUND(NEW.precio_dado * NEW.cantidad_prod * (1.0 - COALESCE(NEW.descuento_pct, 0) / 100.0), 2);
    PERFORM act_productos_inventario(FALSE, NEW.id_producto, NEW.cantidad_prod);
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.verif_exist_update()
    RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    cant INTEGER;
BEGIN
    SELECT cantidad INTO cant FROM producto WHERE id_producto = NEW.id_producto;
    cant := cant + OLD.cantidad_prod;
    IF cant < NEW.cantidad_prod THEN
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
    PERFORM act_productos_inventario(TRUE, NEW.id_producto, OLD.cantidad_prod);
    IF NOT NEW.precio_fijo OR NEW.precio_dado IS NULL THEN
        IF NEW.cantidad_prod >= 12 THEN
            SELECT precio_mayoreo INTO NEW.precio_dado FROM producto WHERE id_producto = NEW.id_producto;
            NEW.tipo_venta := 'Mayoreo';
        ELSE
            SELECT precio_menudeo INTO NEW.precio_dado FROM producto WHERE id_producto = NEW.id_producto;
            NEW.tipo_venta := 'Menudeo';
        END IF;
    END IF;
    NEW.precio_total := ROUND(NEW.precio_dado * NEW.cantidad_prod * (1.0 - NEW.descuento_pct / 100.0), 2);
    PERFORM act_productos_inventario(FALSE, NEW.id_producto, NEW.cantidad_prod);
    RETURN NEW;
END;
$$;

-- D-01) Tampoco se puede apartar un producto dado de baja (resto igual a la versión anterior).
CREATE OR REPLACE FUNCTION public.reg_apdet(ideapar character varying, idepro id_producto_dominio, cant integer, acum boolean)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    num   INTEGER;
    ka    INTEGER;
    cantt INTEGER;
    est   VARCHAR(10);
BEGIN
    SELECT cantidad, estatus INTO ka, est FROM producto WHERE id_producto = idepro;
    IF est IS DISTINCT FROM 'Activo' THEN
        RAISE EXCEPTION 'El producto % está dado de baja y no se puede apartar', idepro;
    END IF;
    IF ka >= cant THEN
        SELECT COUNT(id_producto) INTO num FROM apartado_detalle WHERE id_apartado = ideapar AND id_producto = idepro;
        IF num = 0 THEN
            INSERT INTO apartado_detalle(id_apartado, id_producto, cantidad) VALUES (ideapar, idepro, cant);
        ELSE
            SELECT cantidad INTO cantt FROM apartado_detalle WHERE id_apartado = ideapar AND id_producto = idepro;
            PERFORM act_productos_inventario(TRUE, idepro, cantt);
            IF acum = TRUE THEN cant := cantt + cant; END IF;
            UPDATE apartado_detalle SET cantidad = cant WHERE id_apartado = ideapar AND id_producto = idepro;
        END IF;
        SELECT SUM(precio_total) INTO num FROM apartado_detalle WHERE id_apartado = ideapar;
        UPDATE apartado SET cantidad_total = num WHERE id_apartado = ideapar;
        PERFORM act_productos_inventario(FALSE, idepro, cant);
    ELSE
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
END;
$$;

-- Quitar del carrito un producto que no está en él intentaba sumar NULL al stock
-- y fallaba con un error de kardex. Ahora no hace nada.
CREATE OR REPLACE FUNCTION public.eliminar_prod(idepro id_producto_dominio)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE cant INTEGER;
BEGIN
    SELECT cantidad_prod INTO cant FROM venta_temp WHERE id_producto = idepro;
    IF cant IS NULL THEN RETURN; END IF;
    PERFORM act_productos_inventario(TRUE, idepro, cant);
    DELETE FROM venta_temp WHERE id_producto = idepro;
END;
$$;

-- D-09) Cobrar con el carrito vacío dejaba una venta con total_venta NULL.
CREATE OR REPLACE FUNCTION public.realizar_venta(idemp curp_dominio, idecli curp_dominio)
    RETURNS character varying LANGUAGE plpgsql AS $$
DECLARE
    idevent VARCHAR(15);
    suma    NUMERIC(10,2);
BEGIN
    IF NOT EXISTS (SELECT 1 FROM venta_temp) THEN
        RAISE EXCEPTION 'No es posible registrar una venta sin productos';
    END IF;
    INSERT INTO venta(id_empleado, total_venta, id_cliente) VALUES (idemp, -1, idecli)
        RETURNING id_venta INTO idevent;
    INSERT INTO venta_detalle(id_venta, id_producto, cantidad_producto, precio_dado, precio_total, tipo_venta)
        SELECT idevent, id_producto, cantidad_prod, precio_dado, precio_total, tipo_venta FROM venta_temp;
    SELECT SUM(precio_total) INTO suma FROM venta_detalle WHERE id_venta = idevent;
    UPDATE venta SET total_venta = suma WHERE id_venta = idevent;
    TRUNCATE TABLE venta_temp;
    RETURN idevent;
END;
$$;

CREATE OR REPLACE FUNCTION public.realizar_venta(idemp curp_dominio)
    RETURNS character varying LANGUAGE sql AS $$
    SELECT realizar_venta(idemp, NULL::curp_dominio);
$$;

-- D-03) Quitar un producto de una devolución no recalculaba el monto.
CREATE OR REPLACE FUNCTION public.elim_devolucion_prod(idedev character varying, idepro id_producto_dominio)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    cant INTEGER;
    ras  VARCHAR(50);
BEGIN
    SELECT cantidad, motivo INTO cant, ras FROM devolucion_ventas_detalle
     WHERE id_devolucion = idedev AND id_producto = idepro;
    IF ras = 'Otro motivo' THEN
        PERFORM act_productos_inventario(FALSE, idepro, cant);
    END IF;
    DELETE FROM devolucion_ventas_detalle WHERE id_devolucion = idedev AND id_producto = idepro;
    UPDATE devolucion_ventas
       SET monto = (SELECT COALESCE(SUM(total), 0) FROM devolucion_ventas_detalle WHERE id_devolucion = idedev)
     WHERE id_devolucion = idedev;
END;
$$;

-- D-10) El valor de inventario del reporte diario no coincidía con el del balance:
--       ignoraba productos sin precio_compra y sumaba los dados de baja.
CREATE OR REPLACE FUNCTION public.reporte_diario(p_fecha date)
    RETURNS TABLE(total_ventas numeric, ventas_netas numeric, total_devoluciones numeric, num_transacciones bigint,
                  ticket_promedio numeric, total_compras numeric, total_gastos numeric, total_otras_ganancias numeric,
                  resultado_neto numeric, valor_inventario numeric)
    LANGUAGE plpgsql AS $$
DECLARE
    fecha DATE := p_fecha;
    v_total_ventas          NUMERIC(10,2);
    v_total_devoluciones    NUMERIC(10,2);
    v_total_compras         NUMERIC(10,2);
    v_total_gastos          NUMERIC(10,2);
    v_total_otras_ganancias NUMERIC(10,2);
    v_num_transacciones     BIGINT;
BEGIN
    SELECT COALESCE(SUM(total_venta), 0), COUNT(*) INTO v_total_ventas, v_num_transacciones
      FROM venta WHERE fecha_venta = fecha;
    SELECT COALESCE(SUM(monto), 0) INTO v_total_devoluciones FROM devolucion_ventas WHERE fecha_devolucion = fecha;
    SELECT COALESCE(SUM(monto), 0) INTO v_total_compras FROM compras WHERE fecha_compra = fecha;
    SELECT COALESCE(SUM(monto), 0) INTO v_total_gastos FROM gastos WHERE fecha_gasto = fecha;
    SELECT COALESCE(SUM(monto), 0) INTO v_total_otras_ganancias FROM otras_ganancias WHERE fecha_otras_ganancias = fecha;
    RETURN QUERY
    SELECT
        v_total_ventas,
        v_total_ventas - v_total_devoluciones,
        v_total_devoluciones,
        v_num_transacciones,
        CASE WHEN v_num_transacciones > 0 THEN ROUND(v_total_ventas / v_num_transacciones, 2) ELSE 0 END,
        v_total_compras,
        v_total_gastos,
        v_total_otras_ganancias,
        (v_total_ventas - v_total_devoluciones + v_total_otras_ganancias) - (v_total_compras + v_total_gastos),
        valor_inventario_actual();
END;
$$;

-- D-11) Categorías con el mismo nombre. Antes de crear el índice único se
--       renombran los duplicados que ya existan (se conserva la de menor id).
UPDATE categoria c
   SET nombre = left(c.nombre, 90) || ' (' || c.id_categoria || ')'
 WHERE EXISTS (SELECT 1 FROM categoria o
                WHERE lower(trim(o.nombre)) = lower(trim(c.nombre))
                  AND o.id_categoria < c.id_categoria);
CREATE UNIQUE INDEX IF NOT EXISTS uq_categoria_nombre ON categoria (lower(trim(nombre)));

COMMIT;
