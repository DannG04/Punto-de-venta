-- V1.25__precio_lista_carrito.sql
-- Descripcion: el carrito respeta el precio de la "Lista de precios activa".
-- Fecha: 2026-11-24
-- Depende de: V1.23__kardex_confiable.sql (última versión de verif_exist_update)
--
-- Ventas aplica el precio de lista con un UPDATE de venta_temp, y el descuento con
-- otro. verif_exist_update recalculaba SIEMPRE precio_dado (menudeo/mayoreo) en cada
-- UPDATE, así que el precio de lista se perdía y se cobraba el precio base.
-- Ahora el precio base solo se recalcula cuando cambia la cantidad; en cualquier otro
-- UPDATE se conserva precio_dado y solo se recalcula el total con el descuento.
--
-- Idempotente.

BEGIN;

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
        -- Precio base solo si cambió la cantidad; si no, se conserva el precio dado
        -- (por ejemplo, el de una lista de precios).
        IF NEW.cantidad_prod IS DISTINCT FROM OLD.cantidad_prod THEN
            IF NEW.cantidad_prod >= 12 THEN
                SELECT precio_mayoreo INTO praise FROM producto WHERE id_producto = NEW.id_producto;
                NEW.precio_dado  := praise;
                NEW.tipo_venta   := 'Mayoreo';
            ELSE
                SELECT precio_menudeo INTO praise FROM producto WHERE id_producto = NEW.id_producto;
                NEW.precio_dado  := praise;
                NEW.tipo_venta   := 'Menudeo';
            END IF;
        END IF;

        -- Aplicar descuento al calcular precio_total
        NEW.precio_total := ROUND(NEW.precio_dado * NEW.cantidad_prod * (1.0 - NEW.descuento_pct / 100.0), 2);
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
END;
$function$;

DO $$
BEGIN
    IF to_regclass('public.schema_version') IS NOT NULL THEN
        INSERT INTO schema_version (version, descripcion)
        VALUES ('1.25', 'precio de lista en el carrito')
        ON CONFLICT (version) DO NOTHING;
    END IF;
END;
$$;

COMMIT;
