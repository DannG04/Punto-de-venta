# Devoluciones

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

Registra los productos que un cliente regresa después de haberlos pagado. El sistema calcula cuánto dinero se le devuelve y ajusta el inventario según el motivo:

| Motivo | Qué pasa con el producto |
|---|---|
| **Otro motivo** (el cliente se equivocó, no lo quiso, etc.) | Regresa a la existencia y se puede volver a vender. |
| **El producto estaba en mal estado** | No regresa a la venta: se registra como **merma** en el kardex. |

Cada venta admite **una sola devolución**. Si después el cliente regresa otro producto de la misma venta, se agrega a esa devolución con **Actualizar**.

## Cómo entrar

En el menú superior haz clic en **Devoluciones**.

![Pantalla de Devoluciones](img/01_pantalla_devoluciones.png)

1. **Ordenar:** por fecha o por monto.
2. **Lista de devoluciones:** folio de la devolución, venta, fecha y monto devuelto.
3. **Registrar:** inicia una devolución nueva.
4. **Actualizar:** agrega o quita productos de una devolución existente.

## Registrar una devolución

### 1. Buscar la venta

Pide al cliente su **ticket**: ahí viene el folio de la venta (empieza con **V**).

1. Pulsa **Registrar**.
2. Escribe el folio en **Buscar** y haz clic en la venta.

![Buscar la venta](img/02_buscar_venta.png)

3. Confirma que es la venta correcta con **Sí**.

![Confirmar la venta](img/03_confirmar_venta.png)

### 2. Elegir los productos que regresan

![Productos de la devolución](img/04_productos_devolucion.png)

1. **Productos de la venta** y la cantidad que se vendió. Haz clic en uno para llenar el código.
2. **Código** del producto que regresa.
3. **Cantidad** que regresa (no puede ser mayor que la vendida).
4. **Motivo:** *El producto estaba en mal estado* u *Otro motivo*.
5. **Agregar:** pasa el producto a la lista de devueltos.
6. **Eliminar:** quita de la lista el producto seleccionado.
7. **Productos devueltos,** con su motivo y el importe a devolver.
8. **Hecho:** termina la devolución.

Repite los pasos para cada producto. En el ejemplo, se regresa una leche por otro motivo y un pan en mal estado:

![Productos agregados a la devolución](img/05_productos_agregados.png)

### 3. Terminar

Pulsa **Hecho**. El sistema muestra la **cantidad a devolver** al cliente. Entrégale ese dinero y pulsa **Aceptar**.

![Total a devolver](img/06_total_devolucion.png)

La devolución aparece en la lista:

![Devolución registrada](img/07_devolucion_registrada.png)

El importe que se devuelve es el que el cliente **pagó realmente** por cada pieza, incluyendo los descuentos que tuvo.

## Agregar productos a una devolución existente

1. Selecciona la devolución en la lista.
2. Pulsa **Actualizar**.
3. Agrega o elimina productos como en el registro y pulsa **Hecho**.

## Mensajes que pueden aparecer

| Mensaje | Qué hacer |
|---|---|
| *Debe llenar todos los campos* | Escribe el código y la cantidad. |
| *Debe registrar al menos un producto para finalizar la devolución* | Agrega al menos un producto antes de pulsar **Hecho**. |
| *No es posible devolver una cantidad mayor a la que se tiene registrada* | La cantidad supera lo que se vendió. |
| *No es posible registrar otra devolución sobre la misma venta…* | Esa venta ya tiene devolución: usa **Actualizar**. |

Si cierras la ventana de productos sin agregar ninguno, el sistema pregunta si quieres descartar la devolución.

## Consejos

- Revisa el producto antes de elegir el motivo: si ya no se puede vender, márcalo como **mal estado** para que no cuente como existencia.
- Pide siempre el ticket para encontrar la venta sin errores.
