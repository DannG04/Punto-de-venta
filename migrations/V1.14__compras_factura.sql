-- Migración: Registro de factura de compra completa
-- Fecha: 2026-06-07
BEGIN;

-- 1. proveedor: RFC del emisor (domicilio fiscal usa la columna `direccion` existente)
ALTER TABLE proveedor ADD COLUMN IF NOT EXISTS rfc VARCHAR(13);

-- 2. producto: código de barras, costo, IVA, unidades y factor de conversión
ALTER TABLE producto ADD COLUMN IF NOT EXISTS codigo_barras     VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS precio_compra     NUMERIC(10,2);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS lleva_iva         BOOLEAN DEFAULT false;
ALTER TABLE producto ADD COLUMN IF NOT EXISTS unidad_compra     VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS unidad_venta      VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS factor_conversion NUMERIC(10,3) DEFAULT 1;
ALTER TABLE producto ALTER COLUMN nombre TYPE VARCHAR(100);
CREATE INDEX IF NOT EXISTS idx_producto_codigo_barras ON producto(codigo_barras);

-- 3. compras: encabezado fiscal de la factura
ALTER TABLE compras ADD COLUMN IF NOT EXISTS folio_proveedor VARCHAR(30);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS fecha_factura   DATE;
ALTER TABLE compras ADD COLUMN IF NOT EXISTS origen          VARCHAR(100);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS subtotal        NUMERIC(10,2);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS iva             NUMERIC(10,2);
-- `monto` existente = gran total (subtotal + iva)

CREATE UNIQUE INDEX IF NOT EXISTS uq_compra_folio_prov
  ON compras (id_proveedor, folio_proveedor)
  WHERE id_proveedor IS NOT NULL AND folio_proveedor IS NOT NULL;

-- 4. compra_producto: snapshot de cómo se compró el renglón
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS unidad_compra     VARCHAR(20);
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS factor_conversion NUMERIC(10,3);
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS lleva_iva         BOOLEAN;

-- 5. reg_compra_prod con factor de conversión.
--    cant/prec vienen en UNIDAD DE COMPRA; el inventario se sube en UNIDAD DE VENTA (cant*factor).
CREATE OR REPLACE FUNCTION reg_compra_prod(
    idecom  VARCHAR,
    idepro  id_producto_dominio,
    prec    NUMERIC,
    cant    INTEGER,
    factor  NUMERIC,
    acum    BOOLEAN
) RETURNS void AS $$
DECLARE
    cant_ant   INTEGER;
    factor_ant NUMERIC;
    fac        NUMERIC := COALESCE(NULLIF(factor,0), 1);
BEGIN
    SELECT cantidad, COALESCE(factor_conversion,1)
      INTO cant_ant, factor_ant
      FROM compra_producto
     WHERE id_compra = idecom AND id_producto = idepro;

    IF NOT FOUND THEN
        INSERT INTO compra_producto(
            id_compra, id_producto, cantidad, precio_adquirido, precio_total,
            unidad_compra, factor_conversion, lleva_iva)
        VALUES (idecom, idepro, cant, prec, prec*cant,
            (SELECT unidad_compra FROM producto WHERE id_producto=idepro),
            fac,
            (SELECT COALESCE(lleva_iva,false) FROM producto WHERE id_producto=idepro));
        UPDATE producto
           SET cantidad = cantidad + (cant * fac),
               precio_compra = prec / fac
         WHERE id_producto = idepro;
    ELSIF acum THEN
        UPDATE compra_producto
           SET cantidad = cant_ant + cant,
               precio_adquirido = prec,
               precio_total = prec*(cant_ant + cant),
               factor_conversion = fac
         WHERE id_compra = idecom AND id_producto = idepro;
        UPDATE producto
           SET cantidad = cantidad + (cant * fac),
               precio_compra = prec / fac
         WHERE id_producto = idepro;
    ELSE
        UPDATE compra_producto
           SET cantidad = cant,
               precio_adquirido = prec,
               precio_total = prec*cant,
               factor_conversion = fac
         WHERE id_compra = idecom AND id_producto = idepro;
        UPDATE producto
           SET cantidad = cantidad - (cant_ant * factor_ant) + (cant * fac),
               precio_compra = prec / fac
         WHERE id_producto = idepro;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 6. elim_compra_prod: revierte stock convertido y limpia el kardex reciente del renglón
CREATE OR REPLACE FUNCTION elim_compra_prod(
    idecom VARCHAR,
    idepro id_producto_dominio
) RETURNS void AS $$
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
        UPDATE producto
           SET cantidad = cantidad - (cant_ant * factor_ant)
         WHERE id_producto = idepro;
        DELETE FROM kardex
         WHERE id_producto = idepro
           AND referencia = idecom
           AND tipo_movimiento = 'Compra';
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMIT;
