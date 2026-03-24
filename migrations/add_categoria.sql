-- Migración: Categorías de Productos
-- Fecha: 2026-03-08

-- 1. Crear tabla categoria
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estatus VARCHAR(10) DEFAULT 'Activo' CHECK (estatus IN ('Activo', 'Inactivo'))
);

-- 2. Agregar columna id_categoria a la tabla producto (nullable para no romper productos existentes)
ALTER TABLE producto ADD COLUMN IF NOT EXISTS id_categoria INTEGER REFERENCES categoria(id_categoria);

