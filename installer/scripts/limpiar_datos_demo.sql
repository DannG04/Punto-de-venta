-- Se ejecuta SOLO justo después de restaurar backup.tar en una base NUEVA
-- (instalar_bd.bat). Quita los datos de prueba que trae el respaldo para que
-- el cliente empiece en limpio. Nunca se corre sobre una base existente.
-- Conserva: usuario admin, datos de empresa, listas de precios y catálogos.
BEGIN;
-- El kardex es de solo inserción (V1.23); permitir el borrado solo en esta limpieza.
SET LOCAL kardex.mantenimiento = 'on';
DELETE FROM venta_temp;
DELETE FROM cotizacion_temp;
DELETE FROM kardex;
DELETE FROM producto_precio;
DELETE FROM producto;
COMMIT;
