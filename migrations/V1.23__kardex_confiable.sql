-- V1.23__kardex_confiable.sql
-- Descripcion: el kardex registra cada movimiento una sola vez, con su tipo,
--              folio y empleado, y ya no se borra ni se edita.
-- Fecha: 2026-11-24
--
-- Problemas que corrige (reproducidos con la app el 2026-11-24):
--  * El carrito descontaba stock al agregar un producto y cada cambio dejaba un
--    renglón "Venta" sin folio. Al cambiar la cantidad, quitar un producto o
--    vaciar el carrito, el renglón previo solo se borraba si tenía < 2 segundos:
--    quedaban ventas fantasma (en la prueba: 25 piezas como "Venta" con 6 vendidas).
--  * Editar la existencia en Inventario se registraba como "Venta" (al bajar) o
--    no se registraba (al subir).
--  * El alta de un producto con existencia no dejaba saldo inicial.
--  * Los apartados salían como "Venta" sin folio.
--  * Quitar un renglón de una compra BORRABA su entrada del kardex y la reversa
--    quedaba como "Venta".
--
-- Nuevo modelo:
--  * El carrito (venta_temp) solo valida existencia; NO mueve stock. La salida se
--    aplica y se registra en realizar_venta(), con el folio, el empleado y la hora
--    del cobro.
--  * Cualquier cambio de existencia sin tipo explícito se registra como
--    'Ajuste entrada' / 'Ajuste salida' (nunca se descarta).
--  * El kardex es de solo inserción.
--
-- Idempotente: se puede ejecutar más de una vez.

BEGIN;

-- 0) Vaciar el carrito con la lógica ANTERIOR para devolver al inventario lo que
--    tuviera reservado (después de esta migración el carrito ya no reserva).
SELECT vaciar_venta_temp();

-- 1) Esquema de la tabla
ALTER TABLE kardex ALTER COLUMN tipo_movimiento TYPE VARCHAR(30);
ALTER TABLE kardex ADD COLUMN IF NOT EXISTS observacion VARCHAR(150);

ALTER TABLE kardex DROP CONSTRAINT IF EXISTS kardex_tipo_movimiento_check;
ALTER TABLE kardex ADD CONSTRAINT kardex_tipo_movimiento_check CHECK (tipo_movimiento IN (
    'Inventario inicial',
    'Venta',
    'Compra', 'Cancelacion compra',
    'Devolucion', 'Cancelacion devolucion',
    'Apartado', 'Cancelacion apartado',
    'Ajuste', 'Ajuste entrada', 'Ajuste salida',
    'Merma'
));

CREATE INDEX IF NOT EXISTS idx_kardex_producto_fecha ON kardex(id_producto, fecha);

-- 2) Trigger de movimientos: registra todo cambio de producto.cantidad.
--    Variables de sesión que puede fijar quien mueve el stock:
--      kardex.tipo, kardex.referencia, kardex.empleado, kardex.observacion
CREATE OR REPLACE FUNCTION fn_kardex_producto()
RETURNS TRIGGER AS $$
DECLARE
    v_tipo VARCHAR(30);
    v_ref  VARCHAR(15);
    v_emp  VARCHAR(18);
    v_obs  VARCHAR(150);
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF COALESCE(NEW.cantidad, 0) = 0 THEN
            RETURN NEW;
        END IF;
        v_tipo := 'Inventario inicial';
    ELSIF NEW.cantidad IS NOT DISTINCT FROM OLD.cantidad THEN
        RETURN NEW;
    ELSE
        v_tipo := NULLIF(current_setting('kardex.tipo', true), '');
        -- Sin tipo (o 'Ajuste' genérico): se registra como ajuste según la dirección.
        IF v_tipo IS NULL OR v_tipo = 'Ajuste' THEN
            v_tipo := CASE WHEN NEW.cantidad > OLD.cantidad THEN 'Ajuste entrada' ELSE 'Ajuste salida' END;
        END IF;
    END IF;

    v_ref := NULLIF(current_setting('kardex.referencia', true), '');
    v_obs := NULLIF(current_setting('kardex.observacion', true), '');
    v_emp := NULLIF(current_setting('kardex.empleado', true), '');
    IF v_emp IS NOT NULL AND NOT EXISTS (SELECT 1 FROM empleado WHERE id_empleado = v_emp) THEN
        v_emp := NULL;  -- nunca bloquear un movimiento por un empleado inválido
    END IF;

    INSERT INTO kardex (
        id_producto, tipo_movimiento, cantidad,
        existencia_anterior, existencia_posterior,
        referencia, id_empleado, observacion
    ) VALUES (
        NEW.id_producto, v_tipo,
        NEW.cantidad - CASE WHEN TG_OP = 'INSERT' THEN 0 ELSE OLD.cantidad END,
        CASE WHEN TG_OP = 'INSERT' THEN 0 ELSE OLD.cantidad END,
        NEW.cantidad,
        v_ref, v_emp, v_obs
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_kardex_producto ON producto;
CREATE TRIGGER trg_kardex_producto
AFTER UPDATE OF cantidad ON producto
FOR EACH ROW EXECUTE FUNCTION fn_kardex_producto();

DROP TRIGGER IF EXISTS trg_kardex_producto_alta ON producto;
CREATE TRIGGER trg_kardex_producto_alta
AFTER INSERT ON producto
FOR EACH ROW EXECUTE FUNCTION fn_kardex_producto();

-- 3) El kardex es de solo inserción. Única excepción automática: el cambio de
--    id_producto que llega en cascada al archivar un producto (V1.17).
--    Para mantenimiento manual: SET LOCAL kardex.mantenimiento = 'on';
CREATE OR REPLACE FUNCTION fn_kardex_inmutable()
RETURNS TRIGGER AS $$
BEGIN
    IF current_setting('kardex.mantenimiento', true) = 'on' THEN
        RETURN CASE WHEN TG_OP = 'DELETE' THEN OLD ELSE NEW END;
    END IF;
    IF TG_OP = 'UPDATE'
       AND (NEW.id_kardex, NEW.fecha, NEW.tipo_movimiento, NEW.cantidad,
            NEW.existencia_anterior, NEW.existencia_posterior,
            NEW.referencia, NEW.id_empleado, NEW.observacion)
           IS NOT DISTINCT FROM
           (OLD.id_kardex, OLD.fecha, OLD.tipo_movimiento, OLD.cantidad,
            OLD.existencia_anterior, OLD.existencia_posterior,
            OLD.referencia, OLD.id_empleado, OLD.observacion) THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'El kardex no se puede modificar ni borrar; registre un movimiento contrario';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_kardex_inmutable ON kardex;
CREATE TRIGGER trg_kardex_inmutable
BEFORE UPDATE OR DELETE ON kardex
FOR EACH ROW EXECUTE FUNCTION fn_kardex_inmutable();

-- 4) Helper (V1.22) ahora también fija el empleado si se le pasa.
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

-- 5) Carrito: solo valida existencia, ya no reserva stock.
CREATE OR REPLACE FUNCTION public.verif_exist_insert()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    neim   VARCHAR(50);
    praise NUMERIC(10,2);
    cant   INTEGER;
BEGIN
    SELECT cantidad INTO cant FROM producto WHERE id_producto = NEW.id_producto;
    IF cant >= NEW.cantidad_prod THEN
        SELECT nombre INTO neim FROM producto WHERE id_producto = NEW.id_producto;
        NEW.nombre_p := neim;
        IF NEW.cantidad_prod >= 12 THEN
            SELECT precio_mayoreo INTO praise FROM producto WHERE id_producto = NEW.id_producto;
            NEW.precio_dado := praise;
            NEW.tipo_venta  := 'Mayoreo';
        ELSE
            SELECT precio_menudeo INTO praise FROM producto WHERE id_producto = NEW.id_producto;
            NEW.precio_dado := praise;
            NEW.tipo_venta  := 'Menudeo';
        END IF;
        NEW.precio_total := NEW.precio_dado * NEW.cantidad_prod;
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.verif_exist_update()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    praise NUMERIC(10,2);
    cant   INTEGER;
BEGIN
    SELECT cantidad INTO cant FROM producto WHERE id_producto = NEW.id_producto;

    IF cant >= NEW.cantidad_prod THEN
        IF NEW.cantidad_prod >= 12 THEN
            SELECT precio_mayoreo INTO praise FROM producto WHERE id_producto = NEW.id_producto;
            NEW.precio_dado  := praise;
            NEW.tipo_venta   := 'Mayoreo';
        ELSE
            SELECT precio_menudeo INTO praise FROM producto WHERE id_producto = NEW.id_producto;
            NEW.precio_dado  := praise;
            NEW.tipo_venta   := 'Menudeo';
        END IF;

        -- Aplicar descuento al calcular precio_total
        NEW.precio_total := ROUND(NEW.precio_dado * NEW.cantidad_prod * (1.0 - NEW.descuento_pct / 100.0), 2);
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.eliminar_prod(idepro id_producto_dominio)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
BEGIN
    DELETE FROM venta_temp WHERE id_producto = idepro;
END;
$function$;

-- 6) Cobro: aquí se descuenta el stock y se registra la salida, una sola vez.
CREATE OR REPLACE FUNCTION public.realizar_venta(idemp curp_dominio, idecli curp_dominio)
 RETURNS character varying
 LANGUAGE plpgsql
AS $function$
DECLARE
    idevent VARCHAR(15);
    r       RECORD;
    disp    INTEGER;
    suma    NUMERIC(10,2);
BEGIN
    INSERT INTO venta(id_empleado, total_venta, id_cliente) VALUES (idemp, -1, idecli);
    SELECT id_venta INTO idevent FROM venta WHERE total_venta = -1;

    PERFORM set_config('kardex.empleado', idemp::text, true);

    FOR r IN SELECT id_producto, cantidad_prod, precio_dado, precio_total, tipo_venta
               FROM venta_temp ORDER BY id_producto LOOP
        SELECT cantidad INTO disp FROM producto WHERE id_producto = r.id_producto FOR UPDATE;
        IF disp IS NULL OR disp < r.cantidad_prod THEN
            RAISE EXCEPTION 'No hay suficiente existencia de % (quedan %, se piden %)',
                r.id_producto, COALESCE(disp, 0), r.cantidad_prod;
        END IF;

        INSERT INTO venta_detalle(id_venta, id_producto) VALUES (idevent, r.id_producto);
        UPDATE venta_detalle
           SET cantidad_producto = r.cantidad_prod, precio_dado = r.precio_dado,
               precio_total = r.precio_total, tipo_venta = r.tipo_venta
         WHERE id_venta = idevent AND id_producto = r.id_producto;

        PERFORM mover_inventario_con_kardex(FALSE, r.id_producto, r.cantidad_prod, 'Venta', idevent);
    END LOOP;

    SELECT SUM(precio_total) INTO suma FROM venta_detalle WHERE id_venta = idevent;
    UPDATE venta SET total_venta = suma WHERE id_venta = idevent;
    TRUNCATE TABLE venta_temp;
    RETURN idevent;
END;
$function$;

CREATE OR REPLACE FUNCTION public.realizar_venta(idemp curp_dominio)
 RETURNS character varying
 LANGUAGE plpgsql
AS $function$
DECLARE
    idevent VARCHAR(15);
    r       RECORD;
    disp    INTEGER;
    suma    NUMERIC(10,2);
BEGIN
    INSERT INTO venta(id_empleado, total_venta) VALUES (idemp, -1);
    SELECT id_venta INTO idevent FROM venta WHERE total_venta = -1;

    PERFORM set_config('kardex.empleado', idemp::text, true);

    FOR r IN SELECT id_producto, cantidad_prod, precio_dado, precio_total, tipo_venta
               FROM venta_temp ORDER BY id_producto LOOP
        SELECT cantidad INTO disp FROM producto WHERE id_producto = r.id_producto FOR UPDATE;
        IF disp IS NULL OR disp < r.cantidad_prod THEN
            RAISE EXCEPTION 'No hay suficiente existencia de % (quedan %, se piden %)',
                r.id_producto, COALESCE(disp, 0), r.cantidad_prod;
        END IF;

        INSERT INTO venta_detalle(id_venta, id_producto) VALUES (idevent, r.id_producto);
        UPDATE venta_detalle
           SET cantidad_producto = r.cantidad_prod, precio_dado = r.precio_dado,
               precio_total = r.precio_total, tipo_venta = r.tipo_venta
         WHERE id_venta = idevent AND id_producto = r.id_producto;

        PERFORM mover_inventario_con_kardex(FALSE, r.id_producto, r.cantidad_prod, 'Venta', idevent);
    END LOOP;

    SELECT SUM(precio_total) INTO suma FROM venta_detalle WHERE id_venta = idevent;
    UPDATE venta SET total_venta = suma WHERE id_venta = idevent;
    TRUNCATE TABLE venta_temp;
    RETURN idevent;
END;
$function$;

-- 7) Apartados: solo se mueve la diferencia y con su propio tipo.
CREATE OR REPLACE FUNCTION public.reg_apdet(ideapar character varying, idepro id_producto_dominio, cant integer, acum boolean)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
    cantt INTEGER;
    nueva INTEGER;
    delta INTEGER;
    ka    INTEGER;
    tot   NUMERIC(10,2);
BEGIN
    SELECT cantidad INTO cantt FROM apartado_detalle WHERE id_apartado = ideapar AND id_producto = idepro;
    nueva := CASE WHEN cantt IS NULL THEN cant
                  WHEN acum THEN cantt + cant
                  ELSE cant END;
    delta := nueva - COALESCE(cantt, 0);

    SELECT cantidad INTO ka FROM producto WHERE id_producto = idepro FOR UPDATE;
    IF ka IS NULL OR ka < delta THEN
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;

    IF cantt IS NULL THEN
        INSERT INTO apartado_detalle(id_apartado, id_producto, cantidad) VALUES (ideapar, idepro, nueva);
    ELSE
        UPDATE apartado_detalle SET cantidad = nueva WHERE id_apartado = ideapar AND id_producto = idepro;
    END IF;

    SELECT COALESCE(SUM(precio_total), 0) INTO tot FROM apartado_detalle WHERE id_apartado = ideapar;
    UPDATE apartado SET cantidad_total = tot WHERE id_apartado = ideapar;

    IF delta > 0 THEN
        PERFORM mover_inventario_con_kardex(FALSE, idepro, delta, 'Apartado', ideapar);
    ELSIF delta < 0 THEN
        PERFORM mover_inventario_con_kardex(TRUE, idepro, -delta, 'Cancelacion apartado', ideapar);
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.elim_apdet(ideap character varying, idepro id_producto_dominio)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    cant INTEGER;
    tot  NUMERIC(10,2);
BEGIN
    SELECT cantidad INTO cant FROM apartado_detalle WHERE id_apartado = ideap AND id_producto = idepro;
    IF cant IS NULL THEN RETURN; END IF;
    DELETE FROM apartado_detalle WHERE id_apartado = ideap AND id_producto = idepro;
    PERFORM mover_inventario_con_kardex(TRUE, idepro, cant, 'Cancelacion apartado', ideap);
    SELECT COALESCE(SUM(precio_total), 0) INTO tot FROM apartado_detalle WHERE id_apartado = ideap;
    UPDATE apartado
       SET cantidad_total = tot,
           cantidad_faltante = GREATEST(tot - cantidad_dada, 0)
     WHERE id_apartado = ideap;
END;
$$;

CREATE OR REPLACE FUNCTION public.regresar_productos_inv(ideap character varying)
    RETURNS void LANGUAGE plpgsql AS $$
DECLARE r RECORD;
BEGIN
    FOR r IN SELECT id_producto, cantidad FROM apartado_detalle WHERE id_apartado = ideap LOOP
        PERFORM mover_inventario_con_kardex(TRUE, r.id_producto, r.cantidad, 'Cancelacion apartado', ideap);
    END LOOP;
END;
$$;

-- 8) Compras: quitar un renglón registra la cancelación en lugar de borrar el kardex.
CREATE OR REPLACE FUNCTION public.elim_compra_prod(idecom character varying, idepro id_producto_dominio)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
    cant_ant   INTEGER;
    factor_ant NUMERIC;
BEGIN
    SELECT cantidad, COALESCE(factor_conversion,1)
      INTO cant_ant, factor_ant
      FROM compra_producto
     WHERE id_compra = idecom AND id_producto = idepro;
    IF FOUND THEN
        DELETE FROM compra_producto
         WHERE id_compra = idecom AND id_producto = idepro;
        PERFORM set_config('kardex.tipo', 'Cancelacion compra', true);
        PERFORM set_config('kardex.referencia', idecom, true);
        UPDATE producto
           SET cantidad = cantidad - (cant_ant * factor_ant)
         WHERE id_producto = idepro;
        PERFORM set_config('kardex.tipo', '', true);
        PERFORM set_config('kardex.referencia', '', true);
    END IF;
END;
$function$;

-- 9) Devoluciones: deshacer un reingreso se registra como cancelación.
CREATE OR REPLACE FUNCTION public.elim_devolucion_prod(idedev character varying, idepro id_producto_dominio)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
    cant INTEGER;
    ras  VARCHAR(50);
BEGIN
    SELECT cantidad, motivo INTO cant, ras
      FROM devolucion_ventas_detalle WHERE id_devolucion = idedev AND id_producto = idepro;
    IF ras = 'Otro motivo' THEN
        PERFORM mover_inventario_con_kardex(FALSE, idepro, cant, 'Cancelacion devolucion', idedev);
    END IF;
    DELETE FROM devolucion_ventas_detalle WHERE id_devolucion = idedev AND id_producto = idepro;
END;
$function$;

CREATE OR REPLACE FUNCTION public.reg_devolucion_prod(idedev character varying, ideve character varying, idepro id_producto_dominio, mot boolean, canti integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
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
            PERFORM mover_inventario_con_kardex(FALSE, idepro, cant_ant, 'Cancelacion devolucion', idedev);
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
        PERFORM mover_inventario_con_kardex(TRUE, idepro, canti, 'Devolucion', idedev);
    END IF;

    UPDATE devolucion_ventas
       SET monto = (SELECT COALESCE(SUM(total), 0) FROM devolucion_ventas_detalle WHERE id_devolucion = idedev)
     WHERE id_devolucion = idedev;
END;
$function$;

-- 10) Conciliación: a partir de aquí el kardex de cada producto debe terminar en
--     su existencia real. Donde no cuadra (historial previo incompleto) se deja
--     un renglón que lo explica.
INSERT INTO kardex (id_producto, tipo_movimiento, cantidad,
                    existencia_anterior, existencia_posterior, observacion)
SELECT p.id_producto,
       CASE WHEN u.existencia_posterior IS NULL THEN 'Inventario inicial'
            WHEN p.cantidad > u.existencia_posterior THEN 'Ajuste entrada'
            ELSE 'Ajuste salida' END,
       p.cantidad - COALESCE(u.existencia_posterior, 0),
       COALESCE(u.existencia_posterior, 0),
       p.cantidad,
       CASE WHEN u.existencia_posterior IS NULL
            THEN 'Saldo inicial registrado al actualizar el kardex (V1.23)'
            ELSE 'Conciliación con la existencia real al actualizar el kardex (V1.23)' END
  FROM producto p
  LEFT JOIN LATERAL (
        SELECT k.existencia_posterior
          FROM kardex k
         WHERE k.id_producto = p.id_producto
         ORDER BY k.id_kardex DESC
         LIMIT 1) u ON TRUE
 WHERE (u.existencia_posterior IS NULL AND p.cantidad <> 0)
    OR (u.existencia_posterior IS NOT NULL AND u.existencia_posterior <> p.cantidad);

DO $$
BEGIN
    IF to_regclass('public.schema_version') IS NOT NULL THEN
        INSERT INTO schema_version (version, descripcion)
        VALUES ('1.23', 'kardex confiable')
        ON CONFLICT (version) DO NOTHING;
    END IF;
END;
$$;

COMMIT;
