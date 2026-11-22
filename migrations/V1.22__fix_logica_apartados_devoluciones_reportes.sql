BEGIN;

-- Correcciones de lógica encontradas en la auditoría del 2026-11-22.
-- Todas son CREATE OR REPLACE (idempotentes) y no cambian el esquema.

-- Helper: aplica un movimiento de inventario dejando rastro en el kardex.
-- Sin kardex.tipo, fn_kardex_producto trata cualquier aumento como
-- "reversión interna" y NO lo registra, así que las devoluciones de stock por
-- cancelar/anular apartados o quitar productos de un apartado se perdían.
CREATE OR REPLACE FUNCTION public.mover_inventario_con_kardex(
    incrementar boolean, ideprod id_producto_dominio, cant integer, tipo text, ref text)
    RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    PERFORM set_config('kardex.tipo', tipo, true);
    PERFORM set_config('kardex.referencia', COALESCE(ref, ''), true);
    PERFORM act_productos_inventario(incrementar, ideprod, cant);
    PERFORM set_config('kardex.tipo', '', true);
    PERFORM set_config('kardex.referencia', '', true);
END;
$$;

-- 1) Quitar un producto de un apartado DESCONTABA el stock otra vez en lugar
--    de regresarlo, y no recalculaba el total del apartado.
CREATE OR REPLACE FUNCTION public.elim_apdet(ideap character varying, idepro id_producto_dominio)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    cant INTEGER;
    tot  NUMERIC(10,2);
BEGIN
    SELECT cantidad INTO cant FROM apartado_detalle WHERE id_apartado = ideap AND id_producto = idepro;
    IF cant IS NULL THEN RETURN; END IF;
    DELETE FROM apartado_detalle WHERE id_apartado = ideap AND id_producto = idepro;
    PERFORM mover_inventario_con_kardex(TRUE, idepro, cant, 'Ajuste', ideap);
    SELECT COALESCE(SUM(precio_total), 0) INTO tot FROM apartado_detalle WHERE id_apartado = ideap;
    UPDATE apartado
       SET cantidad_total = tot,
           cantidad_faltante = GREATEST(tot - cantidad_dada, 0)
     WHERE id_apartado = ideap;
END;
$$;

-- Regresar productos de un apartado (cancelación / anulación) con kardex.
CREATE OR REPLACE FUNCTION public.regresar_productos_inv(ideap character varying)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
    FOR r IN SELECT id_producto, cantidad FROM apartado_detalle WHERE id_apartado = ideap LOOP
        PERFORM mover_inventario_con_kardex(TRUE, r.id_producto, r.cantidad, 'Ajuste', ideap);
    END LOOP;
END;
$$;

-- 2) Un apartado 'Anulado' (vencido: ya regresó productos y se quedó el
--    anticipo) se podía cancelar o entregar: regresaba el stock DOS veces y
--    registraba otra ganancia duplicada, o generaba una venta sin stock.
CREATE OR REPLACE FUNCTION public.cancelar_ap(ideap character varying)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    est "estadoValido";
    mon NUMERIC(10,2);
BEGIN
    SELECT estado INTO est FROM apartado WHERE id_apartado = ideap FOR UPDATE;
    IF est IS NULL THEN
        RAISE EXCEPTION 'El apartado no existe';
    ELSIF est <> 'Vigente' THEN
        RAISE EXCEPTION 'Solo se pueden cancelar apartados vigentes (este está %)', est;
    END IF;
    UPDATE apartado SET estado = 'Cancelado' WHERE id_apartado = ideap;
    SELECT cancelar_ap_cliente(ideap) INTO mon;
    INSERT INTO otras_ganancias(descripcion, monto) VALUES ('Cancelación del apartado: ' || ideap, mon);
    PERFORM regresar_productos_inv(ideap);
END;
$$;

-- 3) Al entregar: solo apartados vigentes; la venta se registraba a nombre del
--    "público general" en vez del cliente del apartado, y el apartado quedaba
--    con cantidad_faltante > 0 (inflaba las cuentas por cobrar del balance).
CREATE OR REPLACE FUNCTION public.entregar_ap(ideap character varying)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    est  "estadoValido";
    idem "curp_dominio";
BEGIN
    SELECT estado, id_empleado INTO est, idem FROM apartado WHERE id_apartado = ideap FOR UPDATE;
    IF est IS NULL THEN
        RAISE EXCEPTION 'El apartado no existe';
    ELSIF est <> 'Vigente' THEN
        RAISE EXCEPTION 'Solo se pueden entregar apartados vigentes (este está %)', est;
    END IF;
    UPDATE apartado
       SET estado = 'Entregado', cantidad_dada = cantidad_total, cantidad_faltante = 0
     WHERE id_apartado = ideap;
    PERFORM reg_apartado_venta(ideap, idem);
END;
$$;

CREATE OR REPLACE FUNCTION public.reg_apartado_venta(idapart character varying, idemp curp_dominio)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    idevent VARCHAR(15);
    idecli  curp_dominio;
    suma    NUMERIC(10,2);
BEGIN
    SELECT id_cliente INTO idecli FROM apartado WHERE id_apartado = idapart;
    INSERT INTO venta(id_empleado, total_venta, id_cliente)
    VALUES (idemp, -1, COALESCE(idecli, 'XAXX111111HCCXXXX0'))
    RETURNING id_venta INTO idevent;
    INSERT INTO venta_detalle(id_venta, id_producto, cantidad_producto, precio_dado, precio_total, tipo_venta)
    SELECT idevent, id_producto, cantidad, precio_unitario, precio_total, tipo
      FROM apartado_detalle WHERE id_apartado = idapart;
    SELECT COALESCE(SUM(precio_total), 0) INTO suma FROM venta_detalle WHERE id_venta = idevent;
    UPDATE venta SET total_venta = suma WHERE id_venta = idevent;
END;
$$;

-- 4) Devoluciones:
--    a) al corregir la cantidad de un renglón con 'Otro motivo' se restaba la
--       cantidad NUEVA en vez de la anterior (1 -> 3 dejaba el stock en +1).
--    b) el reembolso usaba precio_dado (precio de lista) e ignoraba el
--       descuento: se devolvía más dinero del cobrado. Ahora usa el precio
--       unitario efectivamente pagado (precio_total / cantidad).
CREATE OR REPLACE FUNCTION public.reg_devolucion_prod(idedev character varying, ideve character varying,
        idepro id_producto_dominio, mot boolean, canti integer)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    cant_vendida INTEGER;
    pagado       NUMERIC(10,2);
    unit         NUMERIC(12,4);
    tot          NUMERIC(10,2);
    rason        VARCHAR(50);
    cant_ant     INTEGER;
BEGIN
    SELECT cantidad_producto, precio_total INTO cant_vendida, pagado
      FROM venta_detalle WHERE id_producto = idepro AND id_venta = ideve;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'El producto no esta dentro de la id de venta';
    END IF;
    IF canti > cant_vendida THEN
        RAISE EXCEPTION 'No es posible devolver una cantidad mayor a la que se tiene registrada';
    END IF;
    unit := pagado / NULLIF(cant_vendida, 0);
    tot  := ROUND(COALESCE(unit, 0) * canti, 2);

    SELECT motivo, cantidad INTO rason, cant_ant
      FROM devolucion_ventas_detalle WHERE id_producto = idepro AND id_devolucion = idedev;
    IF NOT FOUND THEN
        INSERT INTO devolucion_ventas_detalle(id_devolucion, id_producto, motivo, cantidad, total)
        VALUES (idedev, idepro, NULL, canti, tot);
    ELSE
        -- deshacer el reingreso anterior antes de aplicar el nuevo
        IF rason = 'Otro motivo' THEN
            PERFORM act_productos_inventario(FALSE, idepro, cant_ant);
        END IF;
        UPDATE devolucion_ventas_detalle SET cantidad = canti, total = tot
         WHERE id_producto = idepro AND id_devolucion = idedev;
    END IF;

    IF mot THEN
        UPDATE devolucion_ventas_detalle SET motivo = 'El producto estaba en mal estado'
         WHERE id_producto = idepro AND id_devolucion = idedev;
    ELSE
        UPDATE devolucion_ventas_detalle SET motivo = 'Otro motivo'
         WHERE id_producto = idepro AND id_devolucion = idedev;
        PERFORM act_productos_inventario(TRUE, idepro, canti);
    END IF;

    UPDATE devolucion_ventas
       SET monto = (SELECT COALESCE(SUM(total), 0) FROM devolucion_ventas_detalle WHERE id_devolucion = idedev)
     WHERE id_devolucion = idedev;
END;
$$;

-- 5) Reportes / balance general:
--    a) total_caja devolvía NULL si en el periodo faltaba cualquier concepto
--       (sin gastos, sin devoluciones...) y restaba TODAS las compras de la
--       historia en vez de las del periodo.
CREATE OR REPLACE FUNCTION public.total_caja(inicio date, fin date)
    RETURNS numeric LANGUAGE plpgsql AS $$
BEGIN
    RETURN
          COALESCE((SELECT SUM(total_venta) FROM venta WHERE fecha_venta BETWEEN inicio AND fin), 0)
        + COALESCE((SELECT SUM(cantidad_dada) FROM apartado WHERE estado = 'Vigente'), 0)
        + COALESCE((SELECT SUM(monto) FROM otras_ganancias WHERE fecha_otras_ganancias BETWEEN inicio AND fin), 0)
        - COALESCE((SELECT SUM(cp.precio_total) FROM compra_producto cp JOIN compras c ON c.id_compra = cp.id_compra
                     WHERE c.fecha_compra BETWEEN inicio AND fin), 0)
        - COALESCE((SELECT SUM(monto) FROM devolucion_ventas WHERE fecha_devolucion BETWEEN inicio AND fin), 0)
        - COALESCE((SELECT SUM(monto) FROM gastos WHERE fecha_gasto BETWEEN inicio AND fin), 0);
END;
$$;

--    b) cuentas por cobrar sumaban el faltante de apartados cancelados,
--       anulados y entregados.
CREATE OR REPLACE FUNCTION public.total_clientes()
    RETURNS numeric LANGUAGE sql AS $$
    SELECT COALESCE(SUM(cantidad_faltante), 0) FROM apartado WHERE estado = 'Vigente' AND cantidad_faltante > 0;
$$;

--    c) el valor del inventario usaba la cantidad COMPRADA en una compra
--       cualquiera (no la existencia actual), el precio de VENTA para lo que
--       nunca se compró, e incluía productos dados de baja.
CREATE OR REPLACE FUNCTION public.valor_inventario_actual()
    RETURNS numeric LANGUAGE sql AS $$
    SELECT COALESCE(SUM(p.cantidad * COALESCE(
               p.precio_compra,
               (SELECT cp.precio_adquirido / NULLIF(COALESCE(cp.factor_conversion, 1), 0)
                  FROM compra_producto cp JOIN compras c ON c.id_compra = cp.id_compra
                 WHERE cp.id_producto = p.id_producto
                 ORDER BY c.fecha_compra DESC, c.id_compra DESC LIMIT 1),
               p.precio_menudeo, 0)), 0)
      FROM producto p
     WHERE p.estatus = 'Activo' AND p.cantidad > 0;
$$;

CREATE OR REPLACE FUNCTION public.total_inventario()
    RETURNS numeric LANGUAGE sql AS $$ SELECT valor_inventario_actual(); $$;

--    d) estado de resultados: el inventario final contaba la existencia de un
--       producto una vez POR CADA compra del año (3 compras = triple valor).
CREATE OR REPLACE FUNCTION public.estado_resultados(inv_ini numeric)
    RETURNS TABLE(total_ventas numeric, total_devoluciones numeric, inventario_inicial numeric,
                  total_compras numeric, total_gastos numeric, inventario_final numeric,
                  total_otras_ganancias numeric)
    LANGUAGE plpgsql AS $$
DECLARE
    fin DATE := CURRENT_DATE;
    ini DATE := date_trunc('year', CURRENT_DATE)::date;
BEGIN
    RETURN QUERY
    SELECT
        COALESCE((SELECT SUM(v.total_venta) FROM venta v WHERE v.fecha_venta BETWEEN ini AND fin), 0),
        COALESCE((SELECT SUM(d.monto) FROM devolucion_ventas d WHERE d.fecha_devolucion BETWEEN ini AND fin), 0),
        COALESCE(inv_ini, 0),
        COALESCE((SELECT SUM(c.monto) FROM compras c WHERE c.fecha_compra BETWEEN ini AND fin), 0),
        COALESCE((SELECT SUM(g.monto) FROM gastos g WHERE g.fecha_gasto BETWEEN ini AND fin), 0),
        valor_inventario_actual(),
        COALESCE((SELECT SUM(o.monto) FROM otras_ganancias o WHERE o.fecha_otras_ganancias BETWEEN ini AND fin), 0);
END;
$$;

-- 6) Aviso de "pocos productos" contaba productos dados de baja (siempre en 0)
--    y salía después de CADA venta aunque la lista quedara vacía.
CREATE OR REPLACE FUNCTION public.hay_pocos_prod()
    RETURNS boolean LANGUAGE sql AS $$
    SELECT EXISTS (SELECT 1 FROM producto WHERE estatus = 'Activo' AND cantidad <= 12);
$$;

-- 7) Vaciar el carrito sin perder inventario. El stock se descuenta al agregar
--    al carrito; un DELETE directo (al abrir Ventas con un carrito que quedó de
--    una sesión anterior, o al convertir una cotización) lo perdía para siempre.
CREATE OR REPLACE FUNCTION public.vaciar_venta_temp()
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
    FOR r IN SELECT id_producto FROM venta_temp LOOP
        PERFORM eliminar_prod(r.id_producto);
    END LOOP;
END;
$$;

-- 8) Cliente genérico "Público general". La app lo usa como valor por defecto
--    (venta.id_cliente, cotizaciones, conversión) pero el backup del instalador
--    trae la tabla cliente VACÍA: no se podía cerrar ninguna venta (la lista de
--    clientes del cobro estaba vacía) y guardar una cotización a público general
--    fallaba por llave foránea.
INSERT INTO cliente(id_cliente, nombre, telefono)
SELECT 'XAXX111111HCCXXXX0', 'Público general', '0000000000'
WHERE NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = 'XAXX111111HCCXXXX0')
  AND NOT EXISTS (SELECT 1 FROM cliente WHERE telefono = '0000000000');

COMMIT;
