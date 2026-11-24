# Cotizaciones

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

Una cotización es un **presupuesto**: le dices al cliente cuánto le costaría una lista de productos, sin vender nada todavía. La cotización no descuenta existencia. Si el cliente acepta, la conviertes en venta con un clic y entonces sí se cobra y baja el inventario.

Es útil para pedidos grandes (fiestas, eventos, tiendas de la colonia) y para clientes que quieren comparar precios.

## Cómo entrar

En el menú superior haz clic en **Cotizaciones**.

![Pantalla de Cotizaciones](img/01_pantalla_cotizaciones.png)

1. **Carrito actual:** los productos de la cotización que estás armando.
2. **Código del producto.**
3. **Lupa:** busca el producto por nombre.
4. **Cantidad.**
5. **Tipo de venta:** *Menudeo* o *Mayoreo*; define qué precio se cotiza.
6. **Desc. producto (%):** descuento para ese producto.
7. **Agregar:** pasa el producto al carrito.
8. **Quitar:** quita del carrito el renglón seleccionado.
9. **Total** de la cotización.
10. **Seleccionar:** elige al cliente (por omisión, *Público general*).
11. **Vigencia:** fecha hasta la que respetas los precios (formato dd/MM/aaaa).
12. **Notas:** cualquier comentario para el cliente.
13. **Guardar Cotización.**
14. **Limpiar carrito:** empieza de nuevo.

En la parte de abajo está la lista de cotizaciones guardadas:

![Lista de cotizaciones](img/02_lista_cotizaciones.png)

1. **Buscar cotizaciones.**
2. **Lista:** folio, fecha, cliente, total, estatus y vigencia.
3. **Convertir a Venta.**
4. **Cancelar Cot.**
5. **Imprimir:** imprime la cotización en la impresora de tickets para entregársela al cliente.
6. **Ver detalle:** muestra los productos de la cotización.

## Hacer una cotización

### 1. Agregar productos

Escribe el código del producto o pulsa la **lupa** para buscarlo por nombre y haz clic sobre él:

![Buscar producto](img/03_buscar_producto.png)

Después escribe la **cantidad**, elige el **tipo de venta** (Mayoreo si son 12 piezas o más) y, si aplica, el **descuento**. Pulsa **Agregar**. Repite con cada producto.

### 2. Elegir al cliente

Pulsa **Seleccionar** y haz clic en el cliente (puedes buscarlo por nombre). Si no está registrado, usa **Público general**.

![Elegir al cliente](img/04_elegir_cliente.png)

### 3. Vigencia y notas

Escribe hasta qué fecha es válida la cotización y, si quieres, una nota.

![Cotización lista para guardar](img/05_cotizacion_armada.png)

### 4. Guardar

Pulsa **Guardar Cotización**. El sistema le asigna un folio (por ejemplo **COT-0000000046**). Dáselo al cliente para cuando regrese.

![Cotización guardada](img/06_cotizacion_guardada.png)

La cotización queda en la lista con estatus **Pendiente**.

## Consultar una cotización

Selecciona la cotización en la lista y pulsa **Ver detalle**:

![Detalle de la cotización](img/07_detalle.png)

## Convertir una cotización en venta

Cuando el cliente acepta:

1. Selecciona la cotización (debe estar **Pendiente**).
2. Pulsa **Convertir a Venta**.
3. Elige la **forma de pago** y pulsa **Aceptar**.

![Forma de pago](img/08_forma_pago.png)

La venta se registra con los mismos precios y descuentos de la cotización, se descuenta la existencia y la cotización pasa a **Convertida**:

![Cotización convertida](img/09_convertida.png)

> **Nota:** para convertir, el carrito de **Ventas** debe estar vacío y debe haber existencia suficiente de todos los productos. Si falta algo, el sistema lo avisa y la cotización sigue Pendiente.

## Cancelar una cotización

Si el cliente ya no la quiere, selecciónala, pulsa **Cancelar Cot.** y confirma con **Sí**. Pasa a estatus **Cancelada**.

![Cancelar cotización](img/10_cancelar.png)

## Estatus de una cotización

| Estatus | Significado |
|---|---|
| **Pendiente** | Guardada, esperando respuesta del cliente. |
| **Convertida** | Se convirtió en venta. |
| **Cancelada** | El cliente no la aceptó. |

## Consejos

- Pon siempre una **vigencia**: los precios pueden cambiar.
- Revisa la existencia antes de prometer una fecha de entrega; la cotización no aparta mercancía (para eso están los **Apartados**).
