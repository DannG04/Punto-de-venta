-- Migración: borrador de compra (factura "En proceso")
-- Permite dejar una factura a medio capturar SIN afectar inventario ni kardex.
-- Estado de una compra:
--   * fila en compra_borrador            -> "En proceso"
--   * fila en compras                    -> "Terminada"
-- Al terminar el borrador se crea la compra real y se elimina el borrador (ver guardarFacturaCompleta).
-- Fecha: 2026-06-12
BEGIN;

-- 1. Encabezado del borrador
CREATE TABLE IF NOT EXISTS compra_borrador (
    id_borrador        SERIAL PRIMARY KEY,
    id_empleado        VARCHAR(18),
    id_proveedor       INTEGER REFERENCES proveedor(id_proveedor),
    folio_proveedor    VARCHAR(30),
    fecha_factura      DATE,
    origen             VARCHAR(100),
    descripcion        VARCHAR(100),
    total_factura      NUMERIC(10,2),
    fecha_creacion     TIMESTAMP DEFAULT now(),
    fecha_modificacion TIMESTAMP DEFAULT now()
);

-- 2. Renglones del borrador: snapshot completo de cada línea, incluyendo la
--    definición de un producto NUEVO que todavía no existe en el catálogo.
CREATE TABLE IF NOT EXISTS compra_borrador_producto (
    id_borrador     INTEGER NOT NULL REFERENCES compra_borrador(id_borrador) ON DELETE CASCADE,
    linea           INTEGER NOT NULL,
    es_nuevo        BOOLEAN NOT NULL DEFAULT false,
    id_producto     VARCHAR(50) NOT NULL,
    nombre          VARCHAR(100),
    codigo_barras   VARCHAR(20),
    unidad_compra   VARCHAR(20),
    unidad_venta    VARCHAR(20),
    factor          NUMERIC(10,3),
    lleva_iva       BOOLEAN DEFAULT false,
    precio_compra   NUMERIC(10,2),
    cantidad        INTEGER,
    precio_menudeo  NUMERIC(10,2),
    precio_mayoreo  NUMERIC(10,2),
    max_descuento   NUMERIC(5,2),
    id_categoria    INTEGER,
    PRIMARY KEY (id_borrador, linea)
);

COMMIT;
