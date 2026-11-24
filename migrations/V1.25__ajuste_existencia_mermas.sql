-- V1.25__ajuste_existencia_mermas.sql
-- Descripcion: ajustes de existencia con motivo obligatorio y mermas en el kardex.
-- Fecha: 2026-11-24
-- Depende de: V1.24__kardex_confiable.sql
--
--  * ajustar_existencia(): la existencia ya no se edita directamente. Se captura la
--    cantidad contada y un motivo; la diferencia se registra en el kardex como
--    'Ajuste entrada' / 'Ajuste salida', o como 'Merma' si el motivo es merma
--    (dañado o caducado) o robo/extravío.
--  * Devoluciones "en mal estado": antes no dejaban rastro. Ahora registran la
--    entrada de la devolución y la merma (existencia sin cambio, pero visible).
--
-- Idempotente: se puede ejecutar más de una vez.

BEGIN;

-- Helper con observación (la versión de 5 argumentos de V1.22/V1.23 se conserva).
CREATE OR REPLACE FUNCTION public.mover_inventario_con_kardex(
    incrementar boolean, ideprod id_producto_dominio, cant integer, tipo text, ref text, obs text)
    RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    PERFORM set_config('kardex.observacion', COALESCE(obs, ''), true);
    PERFORM mover_inventario_con_kardex(incrementar, ideprod, cant, tipo, ref);
    PERFORM set_config('kardex.observacion', '', true);
END;
$$;

-- Ajuste de existencia por conteo físico u otro motivo. Devuelve la diferencia aplicada.
-- El empleado lo toma de la sesión (kardex.empleado, lo fija la app al conectarse).
CREATE OR REPLACE FUNCTION public.ajustar_existencia(
    ideprod id_producto_dominio, contada integer, motivo text, nota text)
    RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
    actual  INTEGER;
    delta   INTEGER;
    es_merma BOOLEAN;
    v_nota  TEXT := NULLIF(btrim(COALESCE(nota, '')), '');
    obs     TEXT;
BEGIN
    IF motivo IS NULL OR motivo NOT IN ('Conteo físico', 'Merma (dañado o caducado)',
                                        'Robo o extravío', 'Corrección de captura', 'Otro') THEN
        RAISE EXCEPTION 'Motivo de ajuste no válido: %', COALESCE(motivo, '(vacío)');
    END IF;
    IF motivo = 'Otro' AND v_nota IS NULL THEN
        RAISE EXCEPTION 'Con el motivo "Otro" es obligatorio escribir una nota';
    END IF;
    IF contada IS NULL OR contada < 0 THEN
        RAISE EXCEPTION 'La existencia contada no puede ser negativa';
    END IF;

    SELECT cantidad INTO actual FROM producto WHERE id_producto = ideprod FOR UPDATE;
    IF actual IS NULL THEN
        RAISE EXCEPTION 'El producto no existe';
    END IF;

    delta := contada - actual;
    IF delta = 0 THEN
        RETURN 0;
    END IF;

    es_merma := motivo IN ('Merma (dañado o caducado)', 'Robo o extravío');
    IF es_merma AND delta > 0 THEN
        RAISE EXCEPTION 'Una merma solo puede reducir la existencia (sistema %, contado %)', actual, contada;
    END IF;

    obs := left(motivo || ': sistema ' || actual || ', contado ' || contada
                || COALESCE('. ' || v_nota, ''), 150);
    PERFORM mover_inventario_con_kardex(delta > 0, ideprod, abs(delta),
                                        CASE WHEN es_merma THEN 'Merma' ELSE 'Ajuste' END,
                                        NULL, obs);
    RETURN delta;
END;
$$;

-- Devoluciones: "mal estado" registra Devolucion (+) y Merma (-) con el folio.
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
        -- deshacer lo registrado antes de aplicar lo nuevo
        PERFORM revertir_devolucion_prod(idedev, idepro, rason, cant_ant);
        UPDATE devolucion_ventas_detalle SET cantidad = canti, total = tot
         WHERE id_producto = idepro AND id_devolucion = idedev;
    END IF;

    PERFORM mover_inventario_con_kardex(TRUE, idepro, canti, 'Devolucion', idedev);
    IF mot THEN
        UPDATE devolucion_ventas_detalle SET motivo = 'El producto estaba en mal estado'
         WHERE id_producto = idepro AND id_devolucion = idedev;
        PERFORM mover_inventario_con_kardex(FALSE, idepro, canti, 'Merma', idedev, 'Devuelto en mal estado');
    ELSE
        UPDATE devolucion_ventas_detalle SET motivo = 'Otro motivo'
         WHERE id_producto = idepro AND id_devolucion = idedev;
    END IF;

    UPDATE devolucion_ventas
       SET monto = (SELECT COALESCE(SUM(total), 0) FROM devolucion_ventas_detalle WHERE id_devolucion = idedev)
     WHERE id_devolucion = idedev;
END;
$function$;

-- Movimientos contrarios a los de un renglón de devolución (al editarlo o eliminarlo).
CREATE OR REPLACE FUNCTION public.revertir_devolucion_prod(
    idedev character varying, idepro id_producto_dominio, motivo_ant character varying, cant integer)
    RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    IF cant IS NULL OR cant = 0 THEN
        RETURN;
    END IF;
    IF motivo_ant = 'Otro motivo' THEN
        PERFORM mover_inventario_con_kardex(FALSE, idepro, cant, 'Cancelacion devolucion', idedev);
    ELSIF motivo_ant = 'El producto estaba en mal estado' THEN
        -- Antes de V1.24 este caso no dejaba movimientos; desde V1.24 hay Devolucion + Merma.
        IF EXISTS (SELECT 1 FROM kardex WHERE id_producto = idepro AND referencia = idedev
                                          AND tipo_movimiento = 'Merma') THEN
            PERFORM mover_inventario_con_kardex(TRUE, idepro, cant, 'Ajuste entrada', idedev,
                                                'Se anula la merma de la devolución');
            PERFORM mover_inventario_con_kardex(FALSE, idepro, cant, 'Cancelacion devolucion', idedev);
        END IF;
    END IF;
END;
$$;

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
    IF NOT FOUND THEN
        RETURN;
    END IF;
    PERFORM revertir_devolucion_prod(idedev, idepro, ras, cant);
    DELETE FROM devolucion_ventas_detalle WHERE id_devolucion = idedev AND id_producto = idepro;
    UPDATE devolucion_ventas
       SET monto = (SELECT COALESCE(SUM(total), 0) FROM devolucion_ventas_detalle WHERE id_devolucion = idedev)
     WHERE id_devolucion = idedev;
END;
$function$;

DO $$
BEGIN
    IF to_regclass('public.schema_version') IS NOT NULL THEN
        INSERT INTO schema_version (version, descripcion)
        VALUES ('1.25', 'ajuste de existencia con motivo y mermas')
        ON CONFLICT (version) DO NOTHING;
    END IF;
END;
$$;

COMMIT;
