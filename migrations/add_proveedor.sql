-- Migración: Catálogo de Proveedores
-- Fecha: 2026-03-21

-- 1. Crear tabla proveedor
CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(15),
    email VARCHAR(100),
    direccion VARCHAR(255),
    estatus VARCHAR(10) DEFAULT 'Activo' CHECK (estatus IN ('Activo', 'Inactivo'))
);

-- 2. Agregar columna id_proveedor a compras (nullable por retrocompatibilidad)
ALTER TABLE compras ADD COLUMN IF NOT EXISTS id_proveedor INTEGER REFERENCES proveedor(id_proveedor);
