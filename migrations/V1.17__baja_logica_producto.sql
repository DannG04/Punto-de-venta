-- V1.13: Baja lógica de productos (soft delete + reversibilidad + liberación de código)
BEGIN;

-- 1. Columnas de baja lógica en producto
ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS estatus VARCHAR(10) NOT NULL DEFAULT 'Activo'
        CHECK (estatus IN ('Activo','Inactivo'));
ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS fecha_baja TIMESTAMP NULL;

-- 2. Secuencia para códigos de archivo únicos
CREATE SEQUENCE IF NOT EXISTS seq_baja_producto;

-- 3. FKs con ON UPDATE CASCADE para que el renombrado de id_producto se propague.
ALTER TABLE kardex DROP CONSTRAINT kardex_id_producto_fkey;
ALTER TABLE kardex ADD CONSTRAINT kardex_id_producto_fkey
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON UPDATE CASCADE;

ALTER TABLE producto_precio DROP CONSTRAINT producto_precio_id_producto_fkey;
ALTER TABLE producto_precio ADD CONSTRAINT producto_precio_id_producto_fkey
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
    ON UPDATE CASCADE ON DELETE CASCADE;

-- 4. Dar de baja (soft delete): exige stock 0
CREATE OR REPLACE FUNCTION baja_producto(ideprod id_producto_dominio)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE cant INTEGER;
BEGIN
    SELECT cantidad INTO cant FROM producto WHERE id_producto = ideprod;
    IF cant IS NULL THEN
        RAISE EXCEPTION 'El producto no existe';
    END IF;
    IF cant <> 0 THEN
        RAISE EXCEPTION 'No es posible dar de baja un producto con stock mayor a 0';
    END IF;
    UPDATE producto SET estatus = 'Inactivo', fecha_baja = now() WHERE id_producto = ideprod;
END;
$$;

-- 5. Reactivar: vuelve a Activo conservando su id e historial
CREATE OR REPLACE FUNCTION reactivar_producto(ideprod id_producto_dominio)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    UPDATE producto SET estatus = 'Activo', fecha_baja = NULL WHERE id_producto = ideprod;
END;
$$;

-- 6. Archivar: libera el código de barras renombrando el id (CASCADE mueve el historial)
CREATE OR REPLACE FUNCTION archivar_producto(ideprod id_producto_dominio)
RETURNS id_producto_dominio LANGUAGE plpgsql AS $$
DECLARE nuevo_id id_producto_dominio;
BEGIN
    nuevo_id := left(ideprod::text, 41) || '_B' || lpad(nextval('seq_baja_producto')::text, 6, '0');
    UPDATE producto
        SET id_producto = nuevo_id, estatus = 'Inactivo', fecha_baja = now()
        WHERE id_producto = ideprod;
    RETURN nuevo_id;
END;
$$;

-- 7. delete_producto se conserva por compatibilidad pero delega en baja_producto
CREATE OR REPLACE FUNCTION delete_producto(ideprod id_producto_dominio)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    PERFORM baja_producto(ideprod);
END;
$$;

COMMIT;
