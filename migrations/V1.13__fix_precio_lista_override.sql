BEGIN;

-- Re-sembrar las listas de fábrica si la tabla quedó vacía (p. ej. tras correr
-- scripts/limpiar_bd_instalador.sql, que hace TRUNCATE de lista_precios).
-- Sin estas filas la función "Lista de precios activa" en Ventas no funciona.
INSERT INTO lista_precios (nombre, descripcion)
SELECT 'Menudeo', 'Precio de menudeo estandar'
WHERE NOT EXISTS (SELECT 1 FROM lista_precios WHERE nombre = 'Menudeo');

INSERT INTO lista_precios (nombre, descripcion)
SELECT 'Mayoreo', 'Precio de mayoreo para compras al por mayor'
WHERE NOT EXISTS (SELECT 1 FROM lista_precios WHERE nombre = 'Mayoreo');

-- producto_precio pasa a ser una capa de override opcional sobre las columnas
-- producto.precio_menudeo / precio_mayoreo. Las filas con precio 0 (o negativo)
-- no son precios reales: las dejaron productos creados antes de este arreglo y
-- tapaban el precio base, haciendo que la lista de precios fallara en Ventas.
-- Se eliminan para que esas listas vuelvan a resolver al precio base del producto.
DELETE FROM producto_precio WHERE precio <= 0;

COMMIT;
