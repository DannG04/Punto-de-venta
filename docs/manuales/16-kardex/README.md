# Kardex

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

El kardex es la **historia de movimientos de un producto**: cada pieza que entra o sale queda anotada con su fecha, tipo, folio y el empleado que la movió. Sirve para:

- Saber **por qué** cambió la existencia de un producto.
- Revisar faltantes, mermas y ajustes.
- Comprobar que la existencia del sistema **cuadra** con sus movimientos.

El kardex se llena solo con cada venta, compra, apartado, devolución o ajuste. No se puede modificar ni borrar.

Esta sección está en **Administración** y solo la usa el **gerente**.

## Tipos de movimiento

| Tipo | Cuándo se registra |
|---|---|
| **Inventario inicial** | Al dar de alta un producto con existencia. |
| **Compra** / **Cancelacion compra** | Al guardar una factura de compra / al quitar un renglón de ella. |
| **Venta** | Al cobrar una venta. |
| **Devolucion** / **Cancelacion devolucion** | Al regresar producto a la existencia / al corregir una devolución. |
| **Apartado** / **Cancelacion apartado** | Al apartar mercancía / cuando el apartado se cancela o se anula. |
| **Ajuste entrada** / **Ajuste salida** | Al ajustar la existencia en Inventario (conteo físico, corrección de captura u otro motivo). |
| **Merma** | Producto dañado, caducado, robado o devuelto en mal estado. |

## Cómo entrar

En el menú superior haz clic en **Administración** y elige **Kardex**.

![Pantalla de Kardex](img/01_pantalla_kardex.png)

1. **Buscar:** escribe parte del nombre, el código o escanea el código de barras.
2. **Producto** encontrado. Si hay varios, elígelo en la lista.
3. **Incluir dados de baja:** busca también productos que ya no se venden.
4. **Existencia** actual del producto.
5. **Desde** y
6. **Hasta:** el periodo a consultar.
7. **Hoy**, **7 días** y **Este mes:** atajos para el periodo.
8. **Tipo:** muestra solo un tipo de movimiento.
9. **Filtrar:** aplica las fechas escritas a mano.
10. **Exportar Excel:** genera el kardex en Excel.
11. **Movimientos:** fecha, tipo, folio, entrada, salida, saldo, empleado y observación. Las entradas aparecen en verde y las salidas en rojo.
12. **Resumen** del periodo: saldo inicial, entradas, salidas, saldo final y existencia actual.
13. **Estado:** indica si el kardex **cuadra** con la existencia.

## Consultar un producto

1. Escribe el nombre o código en **Buscar** (o pulsa **Enter** después de escanear).
2. Elige el producto si aparecen varios.
3. Elige el periodo con **Hoy**, **7 días** o **Este mes**, o escribe las fechas y pulsa **Filtrar**.

![Movimientos de hoy](img/03_hoy.png)

Cada renglón muestra el **saldo** después del movimiento, así puedes seguir cómo cambió la existencia pieza por pieza.

## Filtrar por tipo de movimiento

En **Tipo** elige, por ejemplo, *Compra* para ver solo las entradas por compras, o *Merma* para revisar las pérdidas. El resumen sigue mostrando los totales de todo el periodo.

![Solo compras](img/02_filtro_tipo.png)

![Solo mermas](img/06_mermas.png)

## Ver el documento de un movimiento

Haz **doble clic** sobre un movimiento con folio para abrir el documento que lo originó (venta, compra, apartado o devolución), con sus datos y productos:

![Documento de una venta](img/04_documento.png)

## Productos dados de baja

Marca **Incluir dados de baja** antes de buscar. Así puedes consultar el historial completo de un producto que ya no se vende:

![Kardex de un producto dado de baja](img/05_dado_de_baja.png)

## ¿Cuadra o no cuadra?

Debajo de la tabla, el sistema indica:

- **Cuadra:** el saldo final del kardex coincide con la existencia actual. Todo está en orden.
- **No cuadra:** hay movimientos que no explican la existencia. Los renglones con problema se marcan en **amarillo**. Revisa con el encargado y, si hace falta, corrige con **Ajustar existencia** en Inventario.

Si consultas un periodo que termina antes de hoy, el saldo final es la existencia que había al cierre de ese día.

## Exportar a Excel

Pulsa **Exportar Excel**. Se genera un archivo con el resumen, los movimientos visibles y los totales, que se abre automáticamente:

![Kardex en Excel](img/07_excel_kardex.png)

El archivo se guarda en la carpeta **Reportes de ventas**, dentro de la carpeta del programa.

## Consejos

- Revisa el kardex de los productos con **mermas** o **ajustes** frecuentes: pueden indicar robos o problemas de almacenamiento.
- Antes de un conteo físico, consulta el kardex del producto para entender diferencias.
