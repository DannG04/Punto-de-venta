# Apartados

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

Un apartado es mercancía que un cliente separa dejando un **pago inicial** y que se lleva cuando termina de pagar. Mientras está apartada, esa mercancía sale de la existencia disponible para que no se venda a otra persona.

Cada apartado tiene un plazo de **30 días**. Puede terminar de tres formas:

| Estado | Qué significa |
|---|---|
| **Vigente** | El cliente todavía puede terminar de pagar. |
| **Entregado** | El cliente pagó el resto (*saldó*) y se llevó la mercancía. |
| **Cancelado** | El cliente pidió cancelarlo antes de tiempo; la mercancía regresa al inventario. |
| **Anulado** | Pasaron los 30 días sin saldarlo; la mercancía regresa al inventario y el pago queda a favor de la tienda. |

## Cómo entrar

En el menú superior haz clic en **Apartados**.

![Pantalla de Apartados](img/01_pantalla_apartados.png)

1. **Buscar:** por folio del apartado, CURP del cliente o del empleado.
2. **Ordenar:** por fecha, por cliente o por total.
3. **Lista de apartados:** folio, empleado, cliente, fecha de inicio, fecha límite, cantidad dada, faltante, total y estado.
4. **Registrar:** crea un apartado nuevo.
5. **Saldar:** el cliente paga el resto y se lleva la mercancía.
6. **Cancelar:** cancela un apartado vigente.

Cada vez que abres esta sección, el sistema revisa los apartados vencidos y los marca como **Anulados**.

## Registrar un apartado

### 1. Elegir al cliente

Pulsa **Registrar**. En la ventana:

![Elegir al cliente](img/02_elegir_cliente.png)

1. **CURP cliente:** se llena al hacer clic en un cliente de la lista (también puedes escribirlo).
2. **Lista de clientes.** Si el cliente no está registrado, primero dalo de alta en **Clientes**.
3. **Registrar:** pasa a elegir los productos.

### 2. Agregar los productos

![Productos del apartado](img/03_productos_apartado.png)

1. **Productos disponibles.** Haz clic en uno para llenar el código.
2. **Código** del producto.
3. **Cantidad** que se aparta.
4. **Agregar:** pasa el producto a la lista del apartado.
5. **Actualizar:** cambia la cantidad del producto seleccionado en la lista del apartado.
6. **Eliminar:** quita el producto seleccionado de la lista del apartado.
7. **Productos apartados,** con su precio. Igual que en Ventas, **12 piezas o más** de un producto llevan precio de mayoreo.
8. **Hecho:** pasa al pago inicial.

### 3. Pago inicial

![Pago inicial](img/04_pago_inicial.png)

1. **Total** del apartado.
2. **Cantidad mínima a pagar:** la **mitad** del total.
3. **Cantidad Pagada:** lo que deja el cliente hoy. Debe ser al menos la cantidad mínima y **menor que el total** (si quiere pagar todo, regístralo como una venta normal).
4. **Vigencia:** fecha límite para saldar (30 días).
5. **Registrar:** guarda el apartado.
6. **Volver:** regresa a la lista de productos.

Al registrar se muestra el **comprobante de apartado**. Imprímelo y entrégaselo al cliente: tiene el folio, la fecha límite y lo que resta por pagar.

![Comprobante de apartado](img/05_ticket_apartado.png)

## Saldar un apartado (entregar la mercancía)

Cuando el cliente regresa a pagar lo que falta:

1. Busca su apartado y selecciónalo (debe estar **Vigente**).
2. Pulsa **Saldar**.

![Saldar apartado](img/06_saldar.png)

1. **ID Apartado:** el folio seleccionado.
2. **Salda con:** la cantidad que falta por pagar.
3. **Recibido:** lo que te entrega el cliente.
4. **Cambio:** se calcula solo.
5. **Saldar:** registra el pago. El apartado pasa a **Entregado** y se genera la venta.

Entrega la mercancía junto con el comprobante de saldo:

![Comprobante de saldo](img/07_ticket_saldo.png)

## Cancelar un apartado

Si el cliente ya no quiere la mercancía:

1. Selecciona su apartado (debe estar **Vigente**).
2. Pulsa **Cancelar**.
3. El sistema calcula cuánto se **cobra** por los días que estuvo apartado y cuánto se **devuelve** al cliente. Confirma con **Sí**.

![Confirmar cancelación](img/08_cancelar.png)

La mercancía regresa al inventario y lo cobrado se registra como una ganancia adicional del negocio.

## Consultar los apartados

Usa **Ordenar › Más reciente** para ver primero los últimos apartados, o busca por folio o CURP. La columna **Estado** indica cómo terminó cada uno:

![Estados de los apartados](img/09_estados.png)

## Mensajes que pueden aparecer

| Mensaje | Qué hacer |
|---|---|
| *El pago debe ser mayor o igual a la cantidad mínima a pagar* | El pago inicial es de al menos la mitad del total. |
| *No puede registrar un apartado sin productos* | Agrega al menos un producto. |
| *Ese pedido ya no esta vigente* | Solo se saldan apartados vigentes. |
| *Lo recibido debe ser mayor o igual a la cantidad con la que salda* | El cliente debe cubrir todo lo que falta. |
| *Solo se pueden cancelar apartados vigentes* | Los entregados, cancelados o anulados ya no se cancelan. |

## Consejos

- Entrega siempre el comprobante: el cliente necesita el **folio** para saldar.
- Recuérdale al cliente la **fecha límite**: después de 30 días el apartado se anula y el pago queda a favor de la tienda.
