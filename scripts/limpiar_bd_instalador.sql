-- ============================================================
-- Limpieza de base de datos para instalador
-- Ejecutar en: punto_de_venta-V1
-- Resultado: BD limpia con un solo usuario admin (admin / admin123)
--            y configuración de empresa en blanco
-- ============================================================

BEGIN;

-- Un solo TRUNCATE con todas las tablas (incluyendo empleado) para respetar las FK
TRUNCATE TABLE
    cotizacion_temp, venta_temp,
    devolucion_ventas_detalle, forma_pago_venta, venta_detalle,
    cotizacion_detalle, apartado_detalle, compra_producto,
    kardex, otras_ganancias, producto_precio,
    cotizacion, devolucion_ventas, venta,
    apartado, compras, gastos,
    cliente, producto, categoria, proveedor,
    lista_precios, empleado;

-- Insertar único usuario admin por defecto
INSERT INTO empleado(id_empleado, nombre, puesto, telefono, usuario, contrasenia, estatus)
VALUES ('AEMA000101HDFBDM00', 'Administrador', 'gerente', '0000000000', 'admin', 'admin123', 'Activo');

-- Re-sembrar las listas de precios de fábrica (el TRUNCATE de arriba las borra).
-- Sin ellas la función "Lista de precios activa" en Ventas no funciona.
SELECT setval(pg_get_serial_sequence('lista_precios','id_lista'), 1, false);
INSERT INTO lista_precios (nombre, descripcion) VALUES
    ('Menudeo', 'Precio de menudeo estandar'),
    ('Mayoreo', 'Precio de mayoreo para compras al por mayor');

-- Empresa: resetear a valores en blanco
UPDATE empresa SET
    nombre           = 'Mi Empresa',
    razon_social     = NULL,
    rfc              = NULL,
    telefono         = NULL,
    correo           = NULL,
    direccion        = NULL,
    ciudad           = NULL,
    estado           = NULL,
    cp               = NULL,
    mensaje_ticket   = 'Muchas gracias por su compra.',
    logo_ruta        = NULL
WHERE id = 1;

-- Reiniciar secuencias
ALTER SEQUENCE cotizacion_seq RESTART WITH 1;

COMMIT;
