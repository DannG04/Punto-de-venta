# Diseño — Registro de factura de compra completa desde el módulo Compras

- **Fecha:** 2026-06-07
- **Módulos afectados:** `ComprasP`, `ProveedoresP`, `InventarioP`, `VentasP`, `ConexionBD`, esquema PostgreSQL
- **Migración nueva:** `V1.14__compras_factura.sql`
- **Enfoque elegido:** Opción A — extender el flujo de dos pasos actual de Compras (menor riesgo sobre app en producción).

## 1. Problema y objetivo

Hoy Compras solo permite vincular productos **ya existentes** a una compra (código + cantidad + precio) y guarda un encabezado mínimo (proveedor + descripción + monto). No permite:

- Registrar el encabezado fiscal de la factura (folio/remisión, fecha de factura, origen, RFC del emisor).
- Dar de alta productos nuevos desde Compras.
- Capturar código de barras, marca de IVA y unidades de compra/venta.
- Convertir unidades (comprar por caja, vender por pieza).
- Calcular y cuadrar subtotal / IVA / total contra la factura física.

**Objetivo:** que desde el panel de Compras se registre una factura completa como la del proveedor de ejemplo (Nota de Remisión P-99843), creando o vinculando productos renglón por renglón, con conversión de unidades e IVA, y dejando el inventario y el kardex correctos.

## 2. Decisiones tomadas (resumen)

| Tema | Decisión |
|------|----------|
| Productos nuevos | Compras los **da de alta**; si el código/código de barras ya existe, **vincula** al producto existente. |
| Unidades | Inventario vive en **unidad de venta**. El producto guarda `unidad_compra`, `unidad_venta`, `factor_conversion`. Al comprar: `stock += cant × factor`, `costo_unitario = precio ÷ factor`. |
| IVA | Marca `lleva_iva` por producto. Se usa **solo** para calcular/cuadrar los totales de la factura (subtotal, IVA 16% sobre gravados, total). No afecta precio de venta ni costo para ganancias. |
| Encabezado | Por factura: folio/remisión, fecha de factura, origen, subtotal/IVA/total. RFC y domicilio fiscal son datos del **proveedor**. |
| Precio de venta | Producto nuevo: se **sugiere por margen %** sobre el costo unitario convertido, editable por renglón. |
| Códigos | **Dos códigos**: `id_producto` (interno) + `codigo_barras` (opcional). El POS resuelve el escaneo buscando en **ambos**, para no romper los productos viejos que usan el barcode como `id_producto`. |
| Flujo de escritura | **En vivo**: cada renglón confirma stock + kardex al instante (igual que hoy). No se difiere hasta Guardar. |

## 3. Cambios de base de datos (`V1.14__compras_factura.sql`)

Una sola migración, envuelta en `BEGIN; … COMMIT;`.

### 3.1 `proveedor`
```sql
ALTER TABLE proveedor ADD COLUMN IF NOT EXISTS rfc VARCHAR(13);
-- domicilio fiscal: se reutiliza la columna existente `direccion`.
```

### 3.2 `producto`
```sql
ALTER TABLE producto ADD COLUMN IF NOT EXISTS codigo_barras    VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS precio_compra     NUMERIC(10,2);   -- último costo por unidad de venta
ALTER TABLE producto ADD COLUMN IF NOT EXISTS lleva_iva         BOOLEAN DEFAULT false;
ALTER TABLE producto ADD COLUMN IF NOT EXISTS unidad_compra     VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS unidad_venta      VARCHAR(20);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS factor_conversion NUMERIC(10,3) DEFAULT 1;
ALTER TABLE producto ALTER COLUMN nombre TYPE VARCHAR(100);   -- los conceptos de factura no caben en 50
```
- `id_producto` sigue siendo el **código interno** (ej. `AGU001`), dominio `^[A-Za-z0-9_-]{3,50}$`.
- `codigo_barras` es opcional; índice para búsqueda de escaneo:
```sql
CREATE INDEX IF NOT EXISTS idx_producto_codigo_barras ON producto(codigo_barras);
```

### 3.3 `compras`
```sql
ALTER TABLE compras ADD COLUMN IF NOT EXISTS folio_proveedor VARCHAR(30);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS fecha_factura   DATE;
ALTER TABLE compras ADD COLUMN IF NOT EXISTS origen          VARCHAR(100);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS subtotal        NUMERIC(10,2);
ALTER TABLE compras ADD COLUMN IF NOT EXISTS iva             NUMERIC(10,2);
-- `monto` existente se conserva como gran total (= subtotal + iva), para no romper reportes.
```
Antiduplicado de facturas (índice único parcial; solo aplica cuando hay proveedor y folio):
```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_compra_folio_prov
  ON compras (id_proveedor, folio_proveedor)
  WHERE id_proveedor IS NOT NULL AND folio_proveedor IS NOT NULL;
```
> El índice es la red de seguridad; la UI avisa **antes** de llegar al error.

### 3.4 `compra_producto`
Conserva los números de la factura tal cual, más snapshot de cómo se compró:
```sql
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS unidad_compra     VARCHAR(20);
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS factor_conversion NUMERIC(10,3);
ALTER TABLE compra_producto ADD COLUMN IF NOT EXISTS lleva_iva         BOOLEAN;
```
- `cantidad` = unidad de compra (ej. 40 cajas).
- `precio_adquirido` = precio por unidad de compra (ej. $44.25).
- `precio_total` = importe del renglón (ej. $1,770.00).

### 3.5 Procedimiento `reg_compra_prod` (se reemplaza)
Nueva firma con factor; aplica conversión a inventario y fija último costo.

```
reg_compra_prod(idecom, idepro, prec_compra_unidad, cant_compra, factor, acum)
```
Comportamiento:
1. Inserta/actualiza el renglón en `compra_producto` con `cantidad = cant_compra`,
   `precio_adquirido = prec_compra_unidad`, `precio_total = prec_compra_unidad * cant_compra`,
   `factor_conversion = factor` y el snapshot de unidad/IVA.
2. `UPDATE producto SET cantidad = cantidad + (cant_compra * factor),
   precio_compra = prec_compra_unidad / factor WHERE id_producto = idepro;`
   - El `UPDATE OF cantidad` dispara `trg_kardex_producto` → registra el movimiento en kardex.
3. En **edición** (`acum=false`), calcula el delta convertido entre el valor anterior y el nuevo y ajusta el stock con ese delta (el trigger registra el neto).

`elim_compra_prod` revierte el stock (resta `cantidad * factor`) y, para no ensuciar el historial de un renglón quitado durante la captura, elimina su entrada de kardex reciente (mismo patrón de reversión que ya usa el trigger para el carrito de ventas).

## 4. Flujo de UI (Opción A — dos pasos)

El botón **Agregar** del panel principal abre el Paso 1; **Continuar** lleva al Paso 2.

### 4.1 Paso 1 — Encabezado (`comDialog` ampliado)
Campos: Proveedor (combo) + `+ Nuevo`; RFC (solo lectura, del proveedor); Folio/Nº remisión; Fecha factura; Origen; Notas (opcional). Botón **Continuar →**.
- Al elegir proveedor se muestran su RFC/dirección (solo lectura).
- `+ Nuevo` abre el alta de proveedor (con RFC) sin salir del flujo.
- Si el folio ya existe para ese proveedor, avisa antes de continuar.

```
┌─ Registrar compra — Encabezado ────────────────────────────┐
│ Proveedor:  [ Distribuidora Oaxaca        ▼ ]  [ + Nuevo ]  │
│ RFC:  EPI790314K22      (se llena solo al elegir proveedor) │
│ Folio / Nº remisión:  [ P-99843        ]                    │
│ Fecha factura:        [ 06/06/2026  📅 ]                    │
│ Origen:               [ Oaxaca de Juárez            ]       │
│ Notas (opcional):     [                              ]       │
│                              [ Continuar →  ]               │
└────────────────────────────────────────────────────────────┘
```

### 4.2 Paso 2 — Renglones + totales (`prodComDialog` reorganizado)

```
┌─ Registrar compra — P-99843 · Distribuidora Oaxaca ───────────────────────────┐
│ ┌ Inventario (clic para usar) ┐  ┌ Capturar renglón ───────────────────────┐ │
│ │ Buscar: [chic______]        │  │ Cód.int:[CHI015] C.barras:[750102141..] │ │
│ │ ─────────────────────────── │  │                       [🔍 Buscar] ●Nuevo │ │
│ │ AGU001  Agua Ciel 1 L       │  │ Concepto: [Chicles Canel's 60 Pzas    ] │ │
│ │ CHI015  Chicles Canel's...  │  │ Cant:[40] U.compra:[Caja▼] Factor:[60]  │ │
│ │ ...                         │  │ U.venta:[Pieza▼]                        │ │
│ │                             │  │ P.compra:[44.25] ☐IVA  Margen%:[30]     │ │
│ │                             │  │ P.venta (sug.): [0.96]  (editable)      │ │
│ │                             │  │  [➕ Agregar] [✎ Actualizar] [🗑 Quitar] │ │
│ └─────────────────────────────┘  └─────────────────────────────────────────┘ │
│ ┌ Renglones de la factura ──────────────────────────────────────────────────┐│
│ │ Cód   │ Concepto              │ Cant │ Unid │ P.Unit │ IVA │   Importe     ││
│ │ CHI015  Chicles Canel's...      40    Caja   44.25    No     1,770.00      ││
│ │ AGU001  Agua Ciel 1 L           12    Bot.   11.70    Sí       140.40      ││
│ └───────────────────────────────────────────────────────────────────────────┘│
│  Subtotal: $9,331.44   IVA 16%: $1,493.03   Total: $10,824.47                  │
│  Total en factura: [10,824.47]  ✓ Cuadra           [ 💾 Guardar factura ]      │
└───────────────────────────────────────────────────────────────────────────────┘
```

**Acomodo de botones:** acciones de renglón juntas bajo el formulario (Agregar=verde, Actualizar=amarillo, Quitar=rojo, como hoy). **Guardar factura** (verde, grande) abajo a la derecha, separado para no confundir "agregar renglón" con "cerrar factura". Franja de totales siempre visible sobre Guardar.

**Captura por renglón:**
- Escribes/escaneas código interno o de barras → `🔍 Buscar`:
  - **Existe:** autollena concepto, unidades, factor e IVA (solo lectura); solo capturas `Cant` y, si cambió, `P.compra`. Indicador "Existente". Actualiza stock y último costo; no cambia el precio de venta.
  - **No existe:** indicador `● Nuevo`; se habilitan concepto, unidades, factor, IVA, margen. `P.venta` sugerido = `(P.compra ÷ factor) × (1 + margen%)`, editable.
  - El campo `P.venta` fija `precio_menudeo`. `precio_mayoreo` se guarda **igual a ese valor por defecto** y se ajusta después en Inventario (evita pedir dos precios al capturar la factura).

## 5. Lógica de registro de un renglón (en vivo)

1. **Resolver producto** por código interno o de barras.
   - Existente → usa su `id_producto`.
   - Nuevo → inserta en `producto` con `cantidad = 0`, unidades, factor, IVA, `codigo_barras`, y precios de venta (`precio_menudeo` = P.venta sugerida; `precio_mayoreo` = mismo valor por defecto).
2. **Guardar renglón** en `compra_producto` con los números de la factura + snapshot.
3. **Convertir a inventario:** `producto.cantidad += cant × factor`; `producto.precio_compra = precio_adquirido ÷ factor`.
4. **Kardex — un movimiento por producto por renglón:** el `UPDATE` de `cantidad` dispara `trg_kardex_producto` →
   `tipo='Compra'`, `cantidad=+convertido`, `referencia=id_compra`, `id_empleado` de la compra.
   - El producto nuevo se crea en 0 y luego se sube **a propósito**: el trigger es `AFTER UPDATE OF cantidad`, así registra la entrada inicial.
   - Las variables de sesión `kardex.tipo/referencia/empleado` se siguen fijando como hoy en `insertarProdCompra`.
5. **Atomicidad:** crear producto + registrar renglón + subir stock van en **una sola transacción** por renglón. Falla → revierte todo (sin productos huérfanos ni renglones a medias).

**Al `💾 Guardar factura`:** guarda en `compras` `subtotal = Σ importes`, `iva = Σ(importes gravados) × 0.16`, `monto = subtotal + iva`.

## 6. Validaciones y manejo de errores

- **RFC:** patrón SAT (12 moral / 13 física). Avisa pero no bloquea.
- **Folio duplicado** por proveedor: aviso "esta factura ya fue registrada", permite continuar.
- **Código interno** (`id_producto`): debe cumplir `^[A-Za-z0-9_-]{3,50}$`. Si marcas Nuevo pero ya existe, ofrece usar el existente.
- **Código de barras:** solo dígitos, 8–14; opcional. Duplicado → aviso.
- **Renglón:** `cantidad>0`, `precio_compra>0`, `factor>0` (default 1), `unidades` y `concepto` obligatorios si es nuevo, `margen≥0`.
- **Existentes:** unidades/factor/IVA solo lectura; solo `Cant` y `P.compra` editables; no se altera el precio de venta.
- **Cuadre:** check ✓/✗ vs. total tecleado (tolerancia ±$0.50). No cuadra → avisa pero deja guardar.
- Mensajes con `Mise.JOption`, consistente con el resto de la app.

## 7. Alcance acompañante

- **`InventarioP`:** el alta/edición de producto gana los campos nuevos (código de barras, IVA, unidad compra/venta, factor, precio compra) para verlos/corregirlos fuera de Compras.
- **`VentasP` / `ConexionBD`:** helper `resolverCodigo(codigo)` que devuelve el `id_producto` buscando por `id_producto` **o** `codigo_barras`; se llama antes de `insertarVentaTemp` para que el escaneo de barcode encuentre productos cuyo id es un código interno. Los productos viejos (barcode como id) siguen funcionando sin backfill.

## 8. Fuera de alcance

- IVA en el precio de venta al público (el POS no maneja IVA en ventas; sería un cambio mayor aparte).
- Costo con IVA incluido para ganancias (se acordó usar costo sin IVA).
- Modo "diferir todas las escrituras hasta Guardar" (se mantiene el modelo en vivo actual).
- Manejo de cajas abiertas/cerradas (stock en doble unidad).

## 9. Datos de prueba

La factura de ejemplo (Nota de Remisión P-99843, 20 renglones, Subtotal $9,331.44 / IVA $1,493.03 / Total $10,824.47) sirve como caso de validación end-to-end, incluyendo el renglón caja→pieza (CHI015: 40 cajas × 60 pzas = 2,400 piezas, costo $0.7375/pza).
