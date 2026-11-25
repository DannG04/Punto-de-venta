# Inventario

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

Inventario es el catálogo de todo lo que vende la tienda. Aquí puedes:

- Consultar qué productos hay, cuántas piezas quedan y a qué precio se venden.
- Dar de alta productos nuevos.
- Cambiar nombre, precios y datos de un producto.
- **Ajustar la existencia** cuando el conteo físico no coincide con el sistema, o cuando algo se daña o se pierde.
- Dar de baja productos que ya no se venden y reactivarlos si vuelven.

Cualquier empleado puede **consultar** el inventario. Los botones para agregar, modificar, ajustar y dar de baja solo los ve el **gerente**.

## Cómo entrar

En el menú superior haz clic en **Inventario**.

![Pantalla de Inventario](img/01_pantalla_inventario.png)

1. **Buscar:** escribe parte del nombre o del código del producto.
2. **Categoría:** muestra solo los productos de una categoría.
3. **Ordenar:** acomoda la lista por nombre o por existencia.
4. **Tabla de productos:** código, nombre, cantidad en existencia, precio de mayoreo, precio de menudeo y categoría.
5. **Agregar:** da de alta un producto nuevo.
6. **Eliminar:** da de baja el producto seleccionado.
7. **Actualizar:** modifica el producto seleccionado.
8. **Dados de baja:** consulta y reactiva productos dados de baja.
9. **Ajustar existencia:** corrige la cantidad en existencia del producto seleccionado.

## Productos por agotarse

Cuando hay productos con **12 piezas o menos**, el botón del menú se pone en rojo y dice cuántos son, por ejemplo **Inventario (⚠ 3)**. El número se actualiza al cambiar de módulo y después de cada venta.

Para verlos, entra a Inventario y marca la casilla **Solo poco stock**, junto al filtro de categoría. Al desmarcarla, buscar o elegir una categoría, la tabla vuelve a mostrar todo.

## Buscar un producto

Escribe en **Buscar** parte del nombre o del código; la tabla se filtra mientras escribes. No importa si escribes con mayúsculas o minúsculas.

![Búsqueda de productos](img/02_buscar.png)

Para volver a ver todos los productos, borra el texto del buscador.

## Filtrar por categoría y ordenar

- En **Categoría** elige una categoría para ver solo sus productos. Elige **Todas** para quitar el filtro.

![Filtro por categoría](img/03_filtro_categoria.png)

- En **Ordenar** puedes elegir **A-Z (Nombre)**, **Z-A (Nombre)**, **Mayor existencia** o **Menor existencia**. *Menor existencia* es útil para ver rápidamente qué hay que resurtir.

![Orden por menor existencia](img/04_orden_menor_existencia.png)

## Agregar un producto nuevo

1. Pulsa **Agregar**. Se abre la ventana *Agregar Producto*.
2. Llena los datos principales:

![Agregar producto: datos principales](img/05_agregar_producto.png)

1. **Nombre** del producto, como quieres que aparezca en el ticket.
2. **Precio Menudeo:** precio normal por pieza.
3. **Precio Mayoreo:** precio cuando el cliente lleva **12 piezas o más** del mismo producto. Debe ser menor que el de menudeo.
4. **Cantidad:** piezas que tienes hoy en la tienda. Queda registrada como *Inventario inicial* en el kardex.
5. **Código del Proveedor:** el código con el que identificarás el producto (letras, números y guiones).
6. **Generar código propio:** márcalo si el producto no tiene código; el sistema crea uno automáticamente.
7. **Categoría** a la que pertenece.
8. **Desc. máximo (%):** el descuento más alto que se puede dar en Ventas a este producto (100 = sin límite, 0 = sin descuento).

3. Baja con la barra de desplazamiento para llenar el resto:

![Agregar producto: datos de compra y listas de precios](img/06_agregar_producto_2.png)

1. **Código de barras:** el que trae el empaque, para cobrarlo con el lector.
2. **Lleva IVA:** márcalo si el producto causa IVA (se usa al registrar compras).
3. **Unidad de compra:** cómo te lo vende el proveedor (caja, bulto, paquete…).
4. **Unidad de venta:** cómo lo vendes tú (pieza, botella, bolsa…).
5. **Factor de conversión:** cuántas unidades de venta trae una unidad de compra. Por ejemplo, una caja con 20 bolsas lleva factor **20**.
6. **Precios por lista:** precio especial para cada lista de precios. Déjalo en **0** para usar el precio normal del producto.
7. **Guardar:** da de alta el producto.

El campo **Precio de compra** no se llena aquí: se actualiza solo cada vez que registras una compra del producto.

### Mensajes que pueden aparecer

| Mensaje | Qué hacer |
|---|---|
| *Todos los campos deben ser completados* | Llena nombre, precios y cantidad. |
| *Debe ingresar un código del proveedor o marcar generar código propio* | Escribe un código o marca la casilla. |
| *Ya existe un producto activo con ese código* | Usa otro código: ese ya está en uso. |
| *El precio mayoreo debe ser menor que el precio menudeo* | Corrige los precios. |

Si el código pertenece a un producto **dado de baja**, el sistema pregunta qué hacer: **Sí** lo reactiva (es el mismo producto y conserva su historial) y **No** crea un producto nuevo con ese código (el anterior se archiva).

## Imprimir el código de barras de un producto

Haz clic sobre el **código** de un producto en la tabla. Se abre una ventana con su código de barras, que puedes **Guardar** como imagen o **Imprimir** para pegarlo en el anaquel o en el producto.

![Código de barras del producto](img/07_codigo_barras.png)

## Modificar un producto

1. Selecciona el producto en la tabla.
2. Pulsa **Actualizar**.
3. Cambia los datos que necesites y pulsa **Guardar**.

![Actualizar producto](img/08_actualizar_producto.png)

1. El **código** no se puede cambiar.
2. La **cantidad** aparece bloqueada: para cambiar la existencia usa **Ajustar existencia** (siguiente apartado). Así cada corrección queda registrada con su motivo.
3. Los **precios** y demás datos sí se pueden modificar.

## Ajustar la existencia

Úsalo cuando la cantidad del sistema no coincide con lo que hay en la tienda: después de un conteo, cuando algo se dañó o caducó, o cuando falta mercancía.

1. Selecciona el producto en la tabla.
2. Pulsa **Ajustar existencia**.

![Ventana de ajuste](img/09_ajuste_vacio.png)

3. Llena la ventana:

![Ajuste por merma](img/10_ajuste_merma.png)

1. **Existencia contada:** escribe **cuántas piezas contaste**, no la diferencia. Arriba ves la existencia que tiene el sistema y, debajo, la **diferencia** que se va a registrar (en verde si es entrada, en rojo si es salida).
2. **Motivo** (obligatorio):
   - **Conteo físico:** la diferencia salió al contar.
   - **Merma (dañado o caducado):** producto que ya no se puede vender.
   - **Robo o extravío:** mercancía faltante.
   - **Corrección de captura:** se registró mal una cantidad.
   - **Otro:** cualquier otro caso; en este es **obligatorio** escribir una nota.
3. **Nota:** explica brevemente qué pasó.
4. **Registrar ajuste:** guarda el ajuste.

Aparece la confirmación con la diferencia registrada:

![Ajuste registrado](img/12_ajuste_registrado.png)

El botón **Registrar ajuste** se desactiva y se muestra el motivo cuando el ajuste no es válido. Por ejemplo, una merma o un robo nunca pueden **aumentar** la existencia:

![Ajuste no permitido](img/11_ajuste_no_permitido.png)

Todos los ajustes quedan en el **kardex** del producto con el motivo, la nota y el empleado que lo hizo.

## Dar de baja un producto

Cuando un producto ya no se va a vender:

1. Selecciona el producto.
2. Pulsa **Eliminar**.
3. Confirma con **Sí**.

![Confirmación para dar de baja](img/13_dar_de_baja.png)

- El producto sale de la lista, pero **su historial se conserva** (ventas, compras y kardex).
- Solo se puede dar de baja con **existencia 0**. Si todavía quedan piezas, primero véndelas o ajusta la existencia a 0 con el motivo que corresponda.

## Reactivar un producto dado de baja

1. Pulsa **Dados de baja**.
2. Elige el producto en la lista y pulsa **Aceptar**.

![Productos dados de baja](img/14_dados_de_baja.png)

El producto vuelve a aparecer en el inventario con su historial.

## Consejos

- Revisa con frecuencia el orden **Menor existencia** para saber qué resurtir.
- Nunca corrijas la existencia "a ojo": usa **Ajustar existencia** con el motivo correcto. Así el kardex siempre cuadra.
- Las mercancías que llegan del proveedor se registran en **Administración › Compras**, no con un ajuste: así queda la factura y el costo.
