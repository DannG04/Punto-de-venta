BEGIN;

-- Corregir trigger verif_exist_update para que respete descuento_pct
-- al calcular precio_total. Antes solo calculaba precio_dado * cantidad_prod.
CREATE OR REPLACE FUNCTION public.verif_exist_update()
RETURNS trigger
LANGUAGE plpgsql
AS $function$
DECLARE
    praise NUMERIC(10,2);
    cant   INTEGER;
BEGIN
    SELECT cantidad INTO cant FROM producto WHERE id_producto = NEW.id_producto;
    cant := cant + OLD.cantidad_prod;

    IF cant >= NEW.cantidad_prod THEN
        PERFORM act_productos_inventario(TRUE, NEW.id_producto, OLD.cantidad_prod);

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

        PERFORM act_productos_inventario(FALSE, NEW.id_producto, NEW.cantidad_prod);
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'No hay suficiente cantidad de productos';
    END IF;
END;
$function$;

-- Simplificar actualizar_desc_temp: solo setea descuento_pct,
-- el trigger recalcula precio_total automaticamente
CREATE OR REPLACE FUNCTION actualizar_desc_temp(
    p_id_producto id_producto_dominio,
    p_descuento   NUMERIC
) RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE venta_temp
    SET descuento_pct = p_descuento
    WHERE id_producto = p_id_producto;
END;
$$;

COMMIT;
