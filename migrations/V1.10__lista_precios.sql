BEGIN;

-- Tabla de listas de precios
CREATE TABLE IF NOT EXISTS lista_precios (
    id_lista SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(150),
    estatus VARCHAR(10) DEFAULT 'Activo'
);

-- Insertar listas por defecto solo si la tabla estaba vacía
INSERT INTO lista_precios (nombre, descripcion)
SELECT 'Menudeo', 'Precio de menudeo estándar'
WHERE NOT EXISTS (SELECT 1 FROM lista_precios WHERE nombre = 'Menudeo');

INSERT INTO lista_precios (nombre, descripcion)
SELECT 'Mayoreo', 'Precio de mayoreo para compras al por mayor'
WHERE NOT EXISTS (SELECT 1 FROM lista_precios WHERE nombre = 'Mayoreo');

-- Tabla de precios por producto y lista
CREATE TABLE IF NOT EXISTS producto_precio (
    id_producto VARCHAR(50) REFERENCES producto(id_producto) ON DELETE CASCADE,
    id_lista    INTEGER     REFERENCES lista_precios(id_lista) ON DELETE CASCADE,
    precio      NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (id_producto, id_lista)
);

-- Migrar precio_menudeo existente a la lista "Menudeo" (id_lista = 1)
INSERT INTO producto_precio (id_producto, id_lista, precio)
SELECT p.id_producto, lp.id_lista, p.precio_menudeo
FROM producto p
CROSS JOIN lista_precios lp
WHERE lp.nombre = 'Menudeo'
ON CONFLICT (id_producto, id_lista) DO NOTHING;

-- Migrar precio_mayoreo existente a la lista "Mayoreo" (id_lista = 2)
INSERT INTO producto_precio (id_producto, id_lista, precio)
SELECT p.id_producto, lp.id_lista, p.precio_mayoreo
FROM producto p
CROSS JOIN lista_precios lp
WHERE lp.nombre = 'Mayoreo'
ON CONFLICT (id_producto, id_lista) DO NOTHING;

COMMIT;
