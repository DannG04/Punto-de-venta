# Ventas

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

Es la caja del punto de venta. Aquí agregas los productos que lleva el cliente, aplicas descuentos si corresponde, cobras y entregas el ticket. Al cobrar, la existencia de cada producto baja automáticamente y la venta queda registrada a tu nombre.

## Cómo entrar

En el menú superior haz clic en **Ventas**.

![Pantalla de Ventas](img/01_pantalla_ventas.png)

1. **Carrito:** los productos de la venta en curso, con su cantidad, tipo de venta (menudeo o mayoreo), precio, total y descuento.
2. **Total** a cobrar.
3. **Desc. producto (%):** descuento para el producto que vas a agregar.
4. **Código:** código del producto o código de barras.
5. **Cantidad** de piezas.
6. **Registrar:** agrega el producto al carrito.
7. **Eliminar:** quita del carrito el renglón seleccionado.
8. **Actualizar:** cambia la cantidad o el descuento del renglón seleccionado.
9. **Desc. global (%):** descuento para todos los productos del carrito.
10. **Lista de precios activa:** con qué lista de precios se cobra.
11. **Venta Hecha:** pasa al cobro.
12. **Buscar:** busca productos por nombre o código.
13. **Resultados de la búsqueda,** con precios y existencia.

## Agregar productos al carrito

Hay dos formas:

**Con el buscador.** Escribe parte del nombre en **Buscar** y haz clic sobre el producto en la lista. Se llenan solos el **Código** y la **Cantidad** (1). Cambia la cantidad si hace falta y pulsa **Registrar**.

![Elegir un producto con el buscador](img/03_buscar_producto.png)

**Con el código o el lector.** Escribe el código del producto en **Código** (o pásalo por el lector de código de barras), escribe la **Cantidad** y pulsa **Registrar**.

Cada producto aparece como un renglón en el carrito y el **Total** se actualiza:

![Carrito con productos](img/04_carrito.png)

Si vuelves a registrar un producto que ya está en el carrito, la cantidad se **suma** al renglón existente.

### Precio de mayoreo automático

Cuando el cliente lleva **12 piezas o más** del mismo producto, el carrito aplica solo el **precio de mayoreo** y marca el renglón como *Mayoreo*:

![Venta de mayoreo](img/05_mayoreo.png)

### Mensajes al agregar

| Mensaje | Qué hacer |
|---|---|
| *Debe llenar los dos campos de texto* | Escribe el código y la cantidad. |
| *No existe un producto con ese código o código de barras.* | Revisa el código o búscalo por nombre. |
| *No hay suficiente cantidad de productos* | No hay existencia suficiente; agrega menos piezas. |

## Descuentos

### Descuento a un producto

Antes de pulsar **Registrar**, escribe el porcentaje en **Desc. producto (%)**. El renglón muestra el descuento en la columna **Dct%** y su total ya rebajado:

![Producto con descuento](img/07_descuento.png)

Cada producto tiene un **descuento máximo** (se define en Inventario). Si escribes uno mayor, el sistema avisa y aplica el máximo permitido:

![Aviso de descuento máximo](img/06_descuento_maximo.png)

### Descuento a toda la venta

Escribe el porcentaje en **Desc. global (%)**: se aplica a todos los productos del carrito, respetando el máximo de cada uno.

## Cambiar la cantidad o quitar un producto

**Cambiar la cantidad o el descuento:**

1. Selecciona el renglón en el carrito.
2. Pulsa **Actualizar**.
3. Escribe la nueva cantidad o el descuento y pulsa **Hecho**.

![Actualizar un renglón](img/08_actualizar_cantidad.png)

**Quitar un producto:** selecciona el renglón y pulsa **Eliminar**.

![Quitar un producto](img/09_quitar_producto.png)

## Lista de precios

En **Lista de precios activa** eliges con qué precios cobrar:

- **Menudeo:** la lista normal. Cobra el precio de menudeo y aplica el de mayoreo desde 12 piezas.
- **Mayoreo:** cobra el precio de mayoreo sin importar la cantidad.
- **Listas especiales** (por ejemplo *Tienditas*): cobran el precio especial que se capturó para cada producto en Inventario.

Elige la lista **antes** de agregar los productos. En el ejemplo, con la lista *Tienditas* el refresco de naranja se cobra a su precio especial de $14.50:

![Venta con la lista Tienditas](img/09b_lista_precios.png)

Si un producto no tiene precio en la lista elegida, el sistema avisa y usa el precio de menudeo. Al terminar, regresa la lista a **Menudeo**.

## Cobrar

1. Pulsa **Venta Hecha**.
2. Elige al **cliente**. Para una venta normal haz clic en **Público general**. Puedes buscar a un cliente escribiendo su CURP; si es nuevo, pulsa **Registrar nuevo cliente**.

![Elegir al cliente](img/10_elegir_cliente.png)

3. Se abre la ventana de cobro:

![Cobro](img/11_cobro.png)

1. **Total** a pagar.
2. **Recibido:** cuánto te entregó el cliente.
3. **Cambio:** se calcula solo.
4. **Forma de Pago:** Efectivo, Transferencia, Tarjeta u Otro.
5. **Hecho:** registra la venta.
6. **Cancelar:** regresa al carrito sin cobrar.

4. Aparece el **ticket**. Pulsa **Imprimir** para entregarlo al cliente o **Cerrar**.

![Ticket de venta](img/12_ticket.png)

El carrito queda vacío y listo para el siguiente cliente.

Si intentas cobrar sin productos, aparece el aviso *"No es posible registrar una venta sin productos"*:

![Venta sin productos](img/02_sin_productos.png)

Si lo recibido es menor que el total, el sistema lo indica y no registra la venta.

## Aviso de productos por agotarse

Después de cobrar, si hay productos con **12 piezas o menos**, aparece la lista **"Quedan pocos productos en el inventario"**. Pulsa **Aceptar** y avisa al encargado de compras.

![Productos por agotarse](img/13_pocos_productos.png)

## Consejos

- Usa el lector de código de barras: es más rápido y evita errores.
- Revisa el carrito antes de pulsar **Venta Hecha**; en la ventana de cobro ya no se pueden cambiar productos.
- Si el cliente regresa un producto después de pagar, se registra en **Devoluciones**, no en Ventas.
