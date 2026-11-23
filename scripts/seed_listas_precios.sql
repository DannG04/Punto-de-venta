BEGIN;
SELECT setval(pg_get_serial_sequence('lista_precios','id_lista'), 1, false);
INSERT INTO lista_precios (nombre, descripcion)
SELECT 'Menudeo', 'Precio de menudeo estandar'
WHERE NOT EXISTS (SELECT 1 FROM lista_precios WHERE nombre='Menudeo');
INSERT INTO lista_precios (nombre, descripcion)
SELECT 'Mayoreo', 'Precio de mayoreo para compras al por mayor'
WHERE NOT EXISTS (SELECT 1 FROM lista_precios WHERE nombre='Mayoreo');
COMMIT;
