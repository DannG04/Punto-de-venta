# Reportes financieros

Manual de usuario · Punto de Venta

> **Antes de empezar:** las imágenes de este manual muestran una tienda de ejemplo, *Abarrotes La Esperanza*, con datos ficticios.

## ¿Para qué sirve?

El punto de venta genera tres reportes en **Excel** para conocer cómo va el negocio:

| Reporte | Qué muestra |
|---|---|
| **Estado de resultados** | Si el negocio gana o pierde: ventas, costo de lo vendido, gastos, otros ingresos y la utilidad del ejercicio. |
| **Balance general** | Lo que tiene el negocio (caja, inventario, lo que deben los clientes), lo que debe (anticipos de apartados) y su capital. |
| **Reporte diario** | El resumen de un día: número de ventas, ticket promedio, valor del inventario y resultado del día. |

Cada reporte se guarda como archivo de Excel y se abre automáticamente al generarlo.

## Cómo entrar

En el menú superior haz clic en **Reportes**. Las opciones son **Bal. General**, **Est. Resultados** y **Rep. Diario**.

![Menú Reportes](img/01_menu_reportes.png)

## El inventario inicial

El estado de resultados y el balance piden el **inventario inicial**: el valor, **en pesos**, de la mercancía que tenía la tienda al empezar el periodo y que no se registró como compra en el sistema.

- Si toda la mercancía entró al sistema mediante **Compras** (como en el ejemplo), escribe **0**.
- Si la tienda ya tenía mercancía antes de usar el sistema, escribe su valor a precio de costo.

Usa el **mismo valor** en los dos reportes para que sus cifras coincidan.

## Estado de resultados

1. Haz clic en **Reportes › Est. Resultados**.
2. Escribe el **inventario inicial** y pulsa **Aceptar**.

![Inventario inicial](img/02_inventario_inicial.png)

Si escribes algo que no es un número, el sistema lo indica:

![Debe ingresar un número](img/03_debe_ser_numero.png)

Se genera y se abre el reporte:

![Estado de resultados](img/05_estado_resultados.png)

Cómo leerlo, de arriba hacia abajo:

- **Ventas netas** = ventas − devoluciones − descuentos.
- **Costo de lo vendido** = inventario inicial + compras − inventario final (el valor de la mercancía que queda hoy).
- **Utilidad bruta** = ventas netas − costo de lo vendido.
- **Utilidad de operación** = utilidad bruta − gastos (los registrados en **Reportes › Gastos**).
- **Utilidad antes de impuestos** = utilidad de operación + otros ingresos (los de **Reportes › Ganancias**).
- **Utilidad del ejercicio** (en amarillo) = utilidad antes de impuestos − impuestos.

El archivo se guarda en la carpeta **Estados de resultados**, dentro de la carpeta del programa.

## Balance general

1. Haz clic en **Reportes › Bal. General**.
2. Escribe el **inventario inicial** (el mismo que usaste en el estado de resultados) y pulsa **Aceptar**.

![Inventario inicial para el balance](img/02b_inventario_balance.png)

Se genera y se abre el reporte:

![Balance general](img/06_balance_general.png)

- **Activo:** *Caja* (el dinero del negocio), *Inventario* (valor de la mercancía) y *Clientes* (lo que deben los clientes por sus apartados).
- **Pasivo:** *Anticipo de clientes* (el dinero que dejaron los clientes en apartados vigentes y que todavía no es venta).
- **Capital:** la utilidad del ejercicio, calculada igual que en el estado de resultados.

El archivo se guarda en la carpeta **BalancesGenerales**, dentro de la carpeta del programa.

Para cerrar la ventana sin generar el reporte, ciérrala con la **X**.

## Reporte diario

1. Haz clic en **Reportes › Rep. Diario**.
2. Elige el **día** en el calendario y pulsa **Generar Reporte**.

![Elegir el día](img/04_calendario.png)

Se genera y se abre el reporte de ese día:

![Reporte diario](img/07_reporte_diario.png)

- **Resumen operativo:** número de ventas (transacciones), ticket promedio y valor del inventario.
- **Estado de resultados del día:** ventas, devoluciones, compras, gastos y otras ganancias de ese día, y el **resultado neto del día**. Un día con compras grandes puede salir en negativo aunque se haya vendido bien: la mercancía comprada se venderá en los días siguientes.

El archivo se guarda en tu carpeta **Documentos › Reportes de ventas**.

## Consejos

- Revisa el **estado de resultados** al cierre de cada mes para saber si el negocio está ganando.
- Registra todos los gastos y otras ganancias antes de generar los reportes; si falta alguno, la utilidad no será real.
- Guarda o imprime los reportes de cada mes: cada nuevo reporte del mismo día reemplaza al anterior.
