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

## Cobrar con el teclado (lo más rápido)

Toda la venta se puede hacer sin mouse, con el teclado o con el lector de código de barras. Al entrar a Ventas el cursor ya está en **Código**, y vuelve ahí después de cada producto.

1. **Escanea o escribe el código y pulsa Enter.** El producto entra con cantidad 1. Si ya estaba en el carrito, se le suma 1.
2. **¿Lleva varios?** Escribe la cantidad y un asterisco antes del código: `3*7501234567890` y Enter. Con lector: escribe `3*` y escanea.
3. **¿No sabes el código?** Escribe parte del nombre (por ejemplo `coca`) y Enter. Se abre la búsqueda; si hay un solo resultado se agrega solo. Si hay varios, elige con ↑/↓ y pulsa Enter.
4. **Para cobrar** pulsa **F12**, o **Enter con el campo de código vacío**. Si te pagan justo, pulsa Enter otra vez y listo.

| Tecla | En la pantalla de Ventas |
|---|---|
| **Enter** | Agrega el código escrito. Con el campo vacío, abre el cobro. |
| `N*código` | Agrega N piezas (de 1 a 9999). |
| **F2** | Ir a la búsqueda de productos. En la búsqueda: ↓ entra a la lista, Enter agrega, Esc regresa. |
| **↑ / ↓** | Moverse por los renglones del carrito (con el campo de código vacío). |
| **+ / -** | Sumar o restar una pieza al renglón seleccionado. En 0 se quita. |
| **Supr** | Quitar el renglón seleccionado. |
| **F4** | Cambiar la cantidad o el descuento del renglón seleccionado. Enter guarda, Esc cancela. |
| **F12** | Cobrar. |

| Tecla | En la ventana de cobro |
|---|---|
| **Enter** | Cobrar. |
| **F1 / F2 / F3 / F4** | Forma de pago: Efectivo / Transferencia / Tarjeta / Otro. |
| **F5** | Elegir otro cliente (por CURP o nombre). |
| **Esc** | Regresar al carrito sin cobrar. |

En el ticket: **Enter** imprime y **Esc** cierra.

### La línea roja

Si algo sale mal al agregar (un código que no existe, falta de existencia, una cantidad mal escrita, un producto sin precio en la lista), **no se abre ninguna ventana**: suena un aviso y el mensaje aparece en **rojo abajo de la pantalla**. Lo que escribiste se queda en el campo, seleccionado, así que puedes corregirlo o escanear el siguiente producto encima.

La línea roja se borra sola cuando agregas el siguiente producto.

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

Aparecen en la línea roja de abajo:

| Mensaje | Qué hacer |
|---|---|
| *Escriba o escanee el código del producto* | El campo **Código** está vacío. |
| *No existe un producto con el código …* | Revisa el código o búscalo por nombre. |
| *Cantidad no válida …* | Usa un número de 1 a 9999 (en **Cantidad** o antes del `*`). |
| *No hay suficiente cantidad de productos* | No hay existencia suficiente; agrega menos piezas. |

La **Cantidad** ya no es obligatoria: si la dejas vacía se agrega 1 pieza.

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

1. Pulsa **Venta Hecha**, **F12** o Enter con el campo de código vacío.
2. Se abre la ventana de cobro. La venta va a **Público general**; para ponerla a nombre de un cliente pulsa **F5** y búscalo por CURP o nombre (si es nuevo, pulsa **Registrar nuevo cliente**).
3. **Recibido** ya trae el total escrito:
   - **Pago justo:** pulsa Enter.
   - **Pagan con billete:** escribe con cuánto pagan (por ejemplo `500`). El **CAMBIO** aparece en grande; pulsa Enter.
   - **Otra forma de pago:** pulsa F2 (Transferencia), F3 (Tarjeta) o F4 (Otro). F1 regresa a Efectivo.

   Si lo recibido es menor que el total, el aviso aparece en rojo dentro de la ventana y no se registra la venta. **Esc** regresa al carrito sin perder nada.
4. Aparece el **ticket**. Pulsa **Imprimir** (o Enter) para entregarlo al cliente, o **Cerrar** (o Esc).

![Ticket de venta](img/12_ticket.png)

El carrito queda vacío y el cursor regresa a **Código**, listo para el siguiente cliente.

Para que un Enter de más no cobre ni imprima por accidente, la ventana de cobro y el ticket no aceptan Enter hasta que sueltas la tecla con la que los abriste.

Si intentas cobrar sin productos, aparece el aviso *"No hay productos para cobrar"* en la línea roja.

## Productos por agotarse

El aviso de productos con **12 piezas o menos** ya no interrumpe el cobro. Ahora aparece en el menú, en el botón **Inventario (⚠ N)**. Ver el manual de Inventario.

## Consejos

- Usa el lector de código de barras: es más rápido y evita errores.
- Revisa el carrito antes de pulsar **Venta Hecha**; en la ventana de cobro ya no se pueden cambiar productos.
- Si el cliente regresa un producto después de pagar, se registra en **Devoluciones**, no en Ventas.
