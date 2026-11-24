# Compras

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

Aquí se registran las **facturas o notas de los proveedores** cuando llega mercancía. Al guardar una compra, el sistema:

- Suma la mercancía a la **existencia** de cada producto (convirtiendo cajas, bultos o paquetes a piezas).
- Actualiza el **precio de compra** de cada producto.
- Da de alta los **productos nuevos** que vengan en la factura.
- Deja el movimiento en el **kardex** con el folio de la compra.

Esta sección está en **Administración** y solo la usa el **gerente**.

## Cómo entrar

En el menú superior haz clic en **Administración** y elige **Compras**.

![Pantalla de Compras](img/01_pantalla_compras.png)

1. **Ordenar:** por fecha o por monto.
2. **Lista de compras:** folio interno, folio del proveedor, proveedor, fecha, total y estado (*Terminada* o *En proceso*).
3. **Agregar:** registra una factura nueva.
4. **Actualizar:** retoma una factura *En proceso*.
5. **Descartar pendiente:** borra una factura *En proceso* que ya no se va a capturar.

## Registrar una factura

### 1. Datos de la factura

Pulsa **Agregar** y llena el encabezado:

![Encabezado de la factura](img/04_encabezado.png)

1. **Proveedor:** elígelo de la lista.
2. **+ Nuevo:** registra un proveedor que todavía no está en la lista.
3. **RFC** del proveedor (se muestra solo).
4. **Folio / Nº remisión:** el número que trae la factura o nota. Si ese folio ya se registró para el mismo proveedor, el sistema avisa para evitar capturarla dos veces.
5. **Fecha factura:** se elige en un calendario.
6. **Origen:** cómo llegó la mercancía (por ejemplo, *Entrega en tienda*).
7. **Descripción** breve de la compra.
8. **Continuar:** pasa a capturar los productos.

El botón de fecha abre un calendario:

![Calendario de la fecha de factura](img/03_fecha_factura.png)

Con **+ Nuevo** se abre el registro rápido de proveedor (nombre obligatorio; teléfono, email, dirección y RFC opcionales):

![Nuevo proveedor](img/02_nuevo_proveedor.png)

### 2. Productos de la factura

Se abre la ventana de captura. Para **cada renglón** de la factura:

![Captura de un producto nuevo](img/05_producto_nuevo.png)

1. **Código:** el código interno del producto.
2. **Cód. barras:** si lo prefieres, escribe o escanea el código de barras.
3. **Buscar:** busca el producto por cualquiera de los dos códigos.
4. **Estado:** *Existente* si el producto ya está en el inventario (se llenan solos sus datos) o **Nuevo** si no existe y hay que darlo de alta.
5. **Concepto:** nombre del producto (solo para productos nuevos).
6. **Cantidad** que llegó, **en la unidad de compra** (por ejemplo, 3 cajas).
7. **Unidad compra:** cómo lo vende el proveedor.
8. **Factor:** cuántas unidades de venta trae cada unidad de compra. Ejemplo: una caja de 12 botellas lleva factor **12**; al guardar entran 3 × 12 = 36 botellas.
9. **Unidad venta:** cómo lo vendes tú.
10. **P. adquirido (sin IVA):** precio de **una unidad de compra** antes de IVA, tal como viene en la factura.
11. **Lleva IVA:** márcalo si ese renglón causa IVA.
12. **Margen %:** ganancia deseada; con ella se calcula el precio de menudeo sugerido (solo productos nuevos).
13. **Precio menudeo** de venta por pieza.
14. **Precio mayoreo** (12 piezas o más); debe ser menor que el de menudeo.
15. **Categoría** del producto nuevo.
16. **Desc. máximo (%):** el descuento más alto permitido en Ventas.

Pulsa **Agregar** para pasar el renglón a la lista *Productos Comprados*. Para un producto **existente** basta con código, cantidad y precio adquirido.

### 3. Revisar totales y guardar

![Factura completa](img/06_factura_completa.png)

1. **Productos del inventario** como referencia.
2. **Agregar** el renglón capturado.
3. **Actualizar** el renglón seleccionado en *Productos Comprados*.
4. **Eliminar** el renglón seleccionado.
5. **Limpiar** los campos de captura.
6. **Productos Comprados:** los renglones de la factura.
7. **Subtotal, IVA 16 % y Total** calculados por el sistema.
8. **Total en factura:** escribe el total **que dice la factura** (es obligatorio).
9. **Cuadre:** indica si el total calculado coincide con el de la factura (*Cuadra* / *No cuadra*).
10. **Guardar factura:** registra la compra completa.
11. **Dejar pendiente:** guarda la captura para terminarla después.

Si el total no cuadra, revisa cantidades y precios. El sistema pregunta si quieres guardarla de todos modos.

Al guardar, la compra aparece como **Terminada** y la existencia de los productos aumenta.

## Dejar una factura pendiente y retomarla

Si no puedes terminar la captura (por ejemplo, falta revisar un precio con el proveedor), pulsa **Dejar pendiente**. La factura queda **En proceso** y **no afecta el inventario** hasta que la guardes.

![Factura dejada en proceso](img/10_dejada_pendiente.png)

Para continuarla, selecciónala en la lista y pulsa **Actualizar**. Se abre con los renglones capturados:

![Retomar una factura en proceso](img/09_retomar_pendiente.png)

Termínala y pulsa **Guardar factura**.

## Descartar una factura pendiente

Selecciona la factura **En proceso**, pulsa **Descartar pendiente** y confirma con **Sí**. Solo se pueden descartar facturas en proceso; las compras terminadas no se borran desde aquí.

![Descartar factura pendiente](img/11_descartar.png)

## Mensajes que pueden aparecer

| Mensaje | Qué hacer |
|---|---|
| *Debe llenar código, cantidad y precio de compra.* | Completa esos tres datos del renglón. |
| *El factor debe ser mayor a 0.* | Escribe cuántas piezas trae la unidad de compra. |
| *Cantidad × factor = … no es un número entero de unidades de venta.* | Revisa el factor o la cantidad. |
| *Capture el precio menudeo y el precio mayoreo del producto nuevo.* | Los productos nuevos necesitan precios de venta. |
| *Ese código de barras ya está en otro producto.* | Usa el código correcto o busca el producto existente. |
| *Agregue al menos un producto antes de guardar.* | La factura debe tener al menos un renglón. |
| *Escriba el Total en factura para poder guardar (es obligatorio).* | Copia el total de la factura en papel. |

Los botones **?** junto a algunos campos muestran una explicación rápida.

## Consejos

- Captura la factura **el mismo día** que llega la mercancía, para que la existencia sea la real.
- Escribe siempre el **total de la factura**: el cuadre detecta errores de captura antes de guardar.
- Revisa el **factor**: es lo que convierte cajas en piezas.
