# Plan de pruebas — Punto de venta

Casos de uso, escenarios y resultados. Primera ejecución: **2026-11-23** (versión 1.22). Re-ejecución tras las correcciones de la migración V1.23: **2026-11-24**.

- **Automáticas (A):** verificaciones en Java contra una base PostgreSQL de pruebas que se crea desde cero (baseline + migraciones V1.1–V1.23 + `seeds/seed_usuario.sql`).
- **Manuales (M):** dependen de clics, impresora, cámara del celular o Internet. Se hacen a mano siguiendo la tabla.

## 1. Cómo correr las pruebas

| Qué | Comando | Necesita |
|---|---|---|
| Unitarias (sin base) | `bash test/correr_pruebas.sh` | JDK |
| Funcionales con base + pantallas | `bash test/correr_pruebas_bd.sh` | JDK, Docker Desktop encendido |
| Solo una clase | `bash test/correr_pruebas_bd.sh puntoventa.db.VentasPruebaBD` | — |
| Reutilizar la base sin recrearla | `SIN_RECREAR=1 bash test/correr_pruebas_bd.sh` | — |

`test/preparar_bd_pruebas.sh` crea el contenedor `pdv-pruebas-pg` en el puerto **15432** (para no chocar con un PostgreSQL instalado en Windows en el 5432) y la base `pdv_pruebas`. **Nunca toca la base `punto_de_venta`.** Todo lo que generan las pruebas (log, Excel, `db.properties`) queda en `build/pruebas-bd/trabajo/`, que está en `.gitignore`.

| Clase | Módulos |
|---|---|
| `test/puntoventa/db/VentasPruebaBD` | Ventas, descuentos, listas de precios, ticket |
| `test/puntoventa/db/ApartadosDevolucionesPruebaBD` | Apartados, devoluciones |
| `test/puntoventa/db/ComprasCotizacionesPruebaBD` | Compras, borradores, proveedores, cotizaciones |
| `test/puntoventa/db/CatalogosPruebaBD` | Inventario, categorías, clientes, empleados y acceso, empresa, gastos, otras ganancias |
| `test/puntoventa/db/ReportesPruebaBD` | Reporte diario, corte, kardex, balance, estado de resultados, Excel |
| `test/puntoventa/web/WebInventarioPruebaBD` | Servidor de inventario para el celular |
| `test/puntoventa/ui/PantallasPruebaBD` | Carga de las 16 pantallas |
| `test/puntoventa/util/UtilidadesPrueba` | Mensajes de error, códigos de barras, ticket |

## 2. Actores

| Actor | Descripción |
|---|---|
| **Gerente** | Acceso total: empleados, compras, reportes, empresa, actualizaciones. |
| **Vendedor** | Ventas, apartados, devoluciones, cotizaciones, clientes. |
| **Cliente** | Persona a nombre de quien se vende o aparta (CURP). Existe el *Cliente Genérico* `XAXX111111HCCXXXX0`. |
| **Celular** | Navegador en la misma red que usa `https://<IP>:8765` para consultar y dar de alta productos. |
| **Sistema** | Procesos automáticos: revisión de apartados vencidos, kardex, actualizaciones. |

## 3. Datos de prueba

- Usuario `admin` / `admin` (gerente, CURP `GARC850315HDFRZNL5`), además de los empleados y 40 productos del baseline.
- Cada prueba crea sus propios productos (`T<MÓDULO><n>`), clientes y empleados con CURP y teléfono únicos, así que las pruebas no dependen entre sí.
- Regla de precio: **12 piezas o más = mayoreo**, menos = menudeo (triggers de `venta_temp` y `apartado_detalle`).

---

## 4. Casos de uso y escenarios

Estado: ✅ pasa · ❌ falla (defecto en la sección 6) · ✋ manual pendiente.

### CU-ACC — Iniciar sesión y control de acceso

**Actor:** Gerente o vendedor. **Precondición:** empleado activo.
**Flujo principal:** elige su usuario en el combo → escribe la contraseña → el sistema muestra "Bienvenido, …" y habilita los módulos de su puesto.
**Alternos:** contraseña incorrecta; empleado dado de baja; un vendedor intenta entrar a un módulo de administración.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-EMP-02 | Login correcto / contraseña incorrecta | Entra / "Contraseña incorrecta" | A | ✅ |
| CU-EMP-03 | Empleado dado de baja intenta entrar | Rechazado y no aparece en el combo | A | ✅ |
| CU-EMP-05 | Login `admin/admin` del seed | Entra como gerente | A | ✅ |
| CU-ACC-M1 | Vendedor da clic en *Empresa* | "Solo el administrador puede acceder." | M | ✋ |
| CU-ACC-M2 | Cerrar sesión y cerrar ventana | "Hasta luego" / "¿Seguro que desea cerrar la ventana?" | M | ✋ |
| CU-UI-01 | Abrir las 16 pantallas con la base de pruebas | Ninguna muestra error ni lanza excepción | A | ✅ 16/16 |

### CU-VEN — Registrar una venta

**Actor:** Vendedor. **Precondición:** sesión iniciada, productos con existencia.
**Flujo principal:** captura código (o código de barras) y cantidad → el producto entra al carrito y **se descuenta del inventario** → opcional: descuento por renglón, descuento global o lista de precios → *Cobrar*: forma de pago y monto recibido → se registra la venta, se vacía el carrito, se imprime el ticket.
**Alternos:** stock insuficiente, código inexistente, quitar renglón, vaciar carrito, descuento mayor al permitido.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-VEN-01 | Agregar 3 piezas | Stock −3, precio menudeo | A | ✅ |
| CU-VEN-02 | Agregar el mismo producto hasta 12 | Acumula y cambia a mayoreo | A | ✅ |
| CU-VEN-03 | Cambiar la cantidad del renglón | Stock se ajusta a la diferencia | A | ✅ |
| CU-VEN-04 | Pedir más de lo que hay | "No hay suficiente cantidad de productos", nada cambia | A | ✅ |
| CU-VEN-05 | Código inexistente | Error, nada cambia | A | ✅ |
| CU-VEN-06 | Vender un producto **dado de baja** | "El producto … está dado de baja y no se puede vender" | A | ✅ (D-01 corregido) |
| CU-VEN-07/08 | Quitar renglón / vaciar carrito | El stock regresa completo | A | ✅ |
| CU-VEN-09 | Descuento por renglón con tope `max_descuento` | Aplica el %, no mueve stock | A | ✅ |
| CU-VEN-10 | Cobrar con tarjeta | Venta con total con descuento, carrito vacío, kardex con folio de venta | A | ✅ |
| CU-VEN-11 | Datos del ticket | Productos, total, forma de pago, cliente, empresa | A | ✅ |
| CU-VEN-12 | Buscar venta por folio | La encuentra con su detalle | A | ✅ |
| CU-VEN-13 | Cobrar con carrito vacío (directo a la base) | "No es posible registrar una venta sin productos", no crea venta | A | ✅ (D-09 corregido) |
| CU-VEN-14 | Elegir una lista de precios para el carrito | Se cobra el precio de la lista (con descuento, al cambiar cantidad y con la lista Mayoreo aunque sean < 12); la lista Menudeo vuelve al precio automático | A | ✅ (D-02 corregido) |
| CU-VEN-M1 | Cobrar con monto recibido menor al total | "Lo recibido no debe ser menor que el total" | M | ✋ |
| CU-VEN-M2 | Descuento mayor al máximo del producto | Aviso y se aplica el máximo | M | ✋ |
| CU-VEN-M3 | Imprimir ticket de venta | Sale con logo, datos de empresa, cambio | M | ✋ |
| CU-VEN-M4 | Escanear código de barras con lector | Agrega el producto correcto | M | ✋ |

### CU-APA — Apartar productos

**Actor:** Vendedor, cliente. **Precondición:** cliente registrado.
**Flujo principal:** nuevo apartado a nombre del cliente → agrega productos (salen del inventario) → registra el primer pago (mínimo 50 % del total) → imprime ticket con fecha límite (30 días).
**Alternos:** saldar (entregar) → se convierte en venta; cancelar → se devuelve mercancía y se retiene una parte proporcional a los días transcurridos; vencido → el sistema lo anula al revisar.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-APA-01 | Crear apartado | Vigente, 30 días | A | ✅ |
| CU-APA-02/03 | Agregar y acumular productos | Stock −, total correcto | A | ✅ |
| CU-APA-04 | Sin existencia suficiente | Error, stock intacto | A | ✅ |
| CU-APA-04b | Apartar un producto dado de baja | "… está dado de baja y no se puede apartar" | A | ✅ (D-01) |
| CU-APA-05 | Abonar / intentar pagar el total completo | Faltante correcto / "regístrelo como una venta" | A | ✅ |
| CU-APA-06 | Quitar un producto | Regresa stock, recalcula total y faltante | A | ✅ |
| CU-APA-07 | Eliminar apartado con / sin productos | Rechaza / elimina | A | ✅ |
| CU-APA-08 | Ticket del apartado | Muestra piezas apartadas, abonado y faltante | A | ✅ |
| CU-APA-09 | Entregar (saldar) | Estado Entregado, genera venta, ya no se puede cancelar | A | ✅ |
| CU-APA-10 | Cancelar a mitad del plazo | Regresa stock, otra ganancia = 15/30 del abono | A | ✅ (kardex ✅ D-07, V1.24) |
| CU-APA-11 | Apartado vencido + revisión | Anulado, regresa stock, abono a otras ganancias | A | ✅ |
| CU-APA-12 | Buscar apartado | Lo encuentra | A | ✅ |
| CU-APA-M1 | Primer pago menor al 50 % | "El pago debe ser mayor o igual a la cantidad mínima a pagar" | M | ✋ |
| CU-APA-M2 | Saldar con recibido menor a lo que falta | "Lo recibido debe ser mayor o igual…" | M | ✋ |
| CU-APA-M3 | Cerrar la ventana sin registrar | Pregunta de confirmación | M | ✋ |

### CU-DEV — Devolver productos de una venta

**Actor:** Vendedor. **Precondición:** venta registrada.
**Flujo principal:** captura el folio de la venta → agrega productos y cantidad → elige motivo: *mal estado* (no regresa al inventario) u *otro motivo* (regresa) → finaliza; el monto se descuenta en los reportes.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-DEV-01 | Registrar devolución | Folio D… | A | ✅ |
| CU-DEV-02 | Otro motivo | Regresa stock, monto, kardex *Devolucion* | A | ✅ |
| CU-DEV-03 | Mal estado | No regresa stock, sí suma al monto | A | ✅ |
| CU-DEV-04 | Más piezas de las vendidas / producto ajeno | Rechazado | A | ✅ |
| CU-DEV-05 | Corregir cantidad devuelta | Reemplaza sin duplicar stock | A | ✅ |
| CU-DEV-06 | Segunda devolución de la misma venta | Rechazada | A | ✅ |
| CU-DEV-07 | Quitar un producto de la devolución | Stock y **monto** regresan | A | ✅ (D-03 corregido) |
| CU-DEV-08 | Eliminar devolución con / sin productos | Rechaza / elimina | A | ✅ |
| CU-DEV-09 | Folio de venta inexistente | Rechazado | A | ✅ |

### CU-COT — Cotizar y convertir a venta

**Flujo principal:** arma el carrito de cotización eligiendo menudeo o mayoreo (no toca inventario) → descuentos → guarda con vigencia y notas → más tarde *Convertir a venta* → se descuenta el inventario y la cotización queda *Convertida*.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-COT-01/02 | Armar, acumular, descuento, quitar | Totales correctos, stock sin cambio | A | ✅ |
| CU-COT-03 | Guardar | Folio COT-…, detalle copiado, carrito vacío | A | ✅ |
| CU-COT-04 | Convertir con una venta en curso | Se bloquea sin perder el carrito de Ventas | A | ✅ |
| CU-COT-05 | Convertir | Venta = total cotizado, stock −, estatus Convertida | A | ✅ |
| CU-COT-06 | Convertir cotizaciones de 5 pzas a mayoreo y de 12 pzas a menudeo | Se cobra exactamente lo cotizado | A | ✅ (D-04 corregido) |
| CU-COT-07 | Cancelar | Estatus Cancelada | A | ✅ |
| CU-COT-08 | Convertir sin existencia | Error, sin venta, sigue Pendiente | A | ✅ |
| CU-COT-M1 | Vigencia con formato inválido | "Formato de vigencia inválido. Use dd/MM/yyyy." | M | ✋ |
| CU-COT-M2 | Imprimir cotización | Ticket con productos y vigencia | M | ✋ |

### CU-COM — Registrar compras a proveedor

**Actor:** Gerente. **Flujo principal:** elige proveedor, folio y fecha → captura renglones (producto existente o nuevo, unidad de compra, factor, precio, IVA) → escribe el total de la factura → *Guardar factura*: en una sola transacción se crea la compra, los productos nuevos, se suma el inventario (cantidad × factor) y se registra el kardex.
**Alternos:** dejar la factura *En proceso* (borrador) y retomarla; descartar borrador; folio repetido.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-COM-01 | Factura con producto existente y nuevo con factor 12 | Stock y costo unitario correctos, kardex *Compra* | A | ✅ |
| CU-COM-02 | Folio repetido del mismo proveedor | Detectado; si se fuerza, no mueve stock | A | ✅ |
| CU-COM-03 | Un renglón falla | No queda nada (rollback) | A | ✅ |
| CU-COM-04 | Borrador: guardar, retomar, actualizar, terminar | No mueve stock hasta terminar; borrador se elimina | A | ✅ |
| CU-COM-05 | Descartar borrador | Eliminado | A | ✅ |
| CU-COM-06 | Quitar un renglón de una compra registrada | Stock regresa, kardex sin movimientos falsos | A | ✅ D-05 (V1.24) |
| CU-COM-07 | Proveedor: alta, edición, baja, búsqueda | Inactivo no sale en el combo | A | ✅ |
| CU-COM-M1 | Total calculado ≠ total en factura | Aviso de diferencia | M | ✋ |
| CU-COM-M2 | Código de barras de 7 dígitos / repetido | Mensajes de validación | M | ✋ |

### CU-INV — Administrar inventario

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-INV-01 | Alta con código, categoría y descuento máximo | Aparece en inventario y su categoría | A | ✅ |
| CU-INV-02 | Código repetido o con espacios | Rechazado | A | ✅ |
| CU-INV-03 | Código de barras: buscar y detectar repetido | Correcto | A | ✅ |
| CU-INV-04 | Editar precios/categoría | Guardado | A | ✅ |
| CU-INV-05 | Corregir existencias a mano desde *Actualizar* | Queda en kardex como ajuste | A | ✅ D-06 (V1.24 y V1.25) |
| CU-INV-06 | Baja con stock / con 0 / reactivar | Rechaza / baja lógica / reactiva | A | ✅ |
| CU-INV-07 | Archivar | Libera el código para reutilizarlo | A | ✅ |
| CU-INV-08 | Stock bajo (≤ 12) | Aviso y listado | A | ✅ |
| CU-INV-09 | Producto creado desde compra | Todos los campos guardados | A | ✅ |
| CU-INV-M1 | Generar e imprimir código de barras | PNG guardado / impresión | M | ✋ |

### CU-WEB — Inventario desde el celular

**Flujo principal:** el gerente inicia el servidor (*Servidor móvil*) → abre `https://<IP>:8765` en el celular → acepta el certificado → escanea o escribe el código → consulta, da de alta o edita.

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-WEB-01 | Página, lector de códigos, 404 | Responde por HTTPS | A | ✅ |
| CU-WEB-02 | Consultar un producto de la base configurada | Lo encuentra | A | ✅ (D-08 corregido) |
| CU-WEB-03 | Listas de precios y categorías | Responde | A | ✅ |
| CU-WEB-04 | Alta, edición, código repetido, campos vacíos | Correcto | A | ✅ |
| CU-WEB-M1 | Escanear con la cámara del celular | Encuentra el producto | M | ✋ |

### CU-CLI / CU-EMP / CU-CAT / CU-EMPR — Catálogos

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-CLI-01..04 | Alta, teléfono repetido, CURP inválida, teléfono corto, editar, baja/alta | Validaciones correctas | A | ✅ |
| CU-CLI-05 | Dar de baja al Cliente Genérico | Rechazado | A | ✅ |
| CU-EMP-01 | Alta de vendedor, usuario ocupado | Correcto | A | ✅ |
| CU-EMP-04 | Cambiar puesto a gerente | Permitido (V1.20) | A | ✅ |
| CU-EMP-M1 | Reactivar empleado por CURP | "Empleado registrado nuevamente" | M | ✋ |
| CU-CAT-01,02,04 | Alta, editar, buscar, inactivar (no sale en combos) | Correcto | A | ✅ |
| CU-CAT-03 | Dos categorías con el mismo nombre (sin distinguir mayúsculas) | "Ya existe un registro con esos datos." | A | ✅ (D-11 corregido) |
| CU-EMPR-01/02 | Guardar datos de empresa, RFC demasiado largo | Guarda / rechaza | A | ✅ |
| CU-EMPR-M1 | Cargar logo y verlo en el ticket | Logo impreso | M | ✋ |

### CU-GAS / CU-GAN — Gastos y otras ganancias

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-GAS-01..03 | Alta, editar, eliminar gasto | Correcto | A | ✅ |
| CU-GAN-01/02 | Alta, editar, eliminar otra ganancia | Correcto | A | ✅ |

### CU-REP — Reportes y cierre de día

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| CU-REP-01 | Reporte diario tras venta 100, devolución 20, compra 50, gasto 30, ganancia 10 | Cada total cambia exactamente eso; neto +10 | A | ✅ |
| CU-REP-02 | Corte del día | La venta aparece con forma de pago y empleado | A | ✅ |
| CU-REP-03 | Kardex del producto con filtro de fechas | Venta, devolución y compra; cuadra con el stock | A | ✅ |
| CU-REP-04 | Balance general: caja | +10 con los mismos movimientos | A | ✅ |
| CU-REP-05 | Estado de resultados (año) | Ventas +100 | A | ✅ |
| CU-REP-06 | Valor de inventario en reporte diario vs balance | Iguales | A | ✅ (D-10 corregido) |
| CU-REP-07 | Excel de reporte diario, balance, estado de resultados, kardex | Se generan los 4 archivos | A | ✅ |
| CU-REP-M1 | Iniciar día / terminar día | "Día iniciado" / "Día terminado…" y se genera el reporte | M | ✋ |
| CU-REP-M2 | Estado de resultados con inventario inicial no numérico | "Debe ingresar un numero" | M | ✋ |

### CU-ACT — Actualizaciones

| ID | Escenario | Resultado esperado | Tipo | Estado |
|---|---|---|---|---|
| (existentes) | `ActualizacionesPrueba`, `ActualizadorPrueba`, `VersionAppPrueba` | Ver `test/correr_pruebas.sh` | A | ✅ |
| CU-ACT-M1 | Actualizar con una venta en curso | "Hay una venta en curso; termínela o vacíe el carrito." | M | ✋ |
| CU-ACT-M2 | Actualizar con UAC real en una PC instalada | La app se cierra, actualiza y reabre | M | ✋ |

### CU-UTL — Utilidades

| ID | Escenario | Tipo | Estado |
|---|---|---|---|
| CU-UTL-01 | Mensajes de error amigables por código de PostgreSQL | A | ✅ |
| CU-UTL-02 | EAN-13 con dígito verificador, CODE-128, QR, código automático | A | ✅ |
| CU-UTL-03 | Ticket: encabezado, escape de HTML, apartado con fecha límite | A | ✅ |

---

## 5. Resultados de la ejecución

| Clase | 2026-11-23 (antes) | 2026-11-24 (con V1.23) |
|---|---|---|
| VentasPruebaBD | 41 OK / 3 fallos | 50 OK / 0 |
| ApartadosDevolucionesPruebaBD | 48 / 2 | 50 / 1 (kardex) |
| ComprasCotizacionesPruebaBD | 49 / 3 | 51 / 2 (kardex) |
| CatalogosPruebaBD | 54 / 3 | 55 / 2 (kardex) |
| ReportesPruebaBD | 20 / 1 | 21 / 0 |
| WebInventarioPruebaBD | 6 / 2 | 11 / 0 |
| PantallasPruebaBD | 16 / 0 | 16 / 0 |
| UtilidadesPrueba | 15 / 0 | 15 / 0 |
| **Total** | **249 / 14** | **269 / 5** |

Los 5 fallos que quedaban (defectos de kardex D-05, D-06 y D-07) se corrigieron en V1.24 (kardex confiable) y V1.25 (ajuste de existencia con motivo); la suite completa pasa.

## 6. Defectos encontrados

| ID | Sev. | Módulo | Defecto | Causa | Sugerencia / corrección |
|---|---|---|---|---|---|
| **D-08** | Alta | Celular | El servidor móvil ignora `db.properties` y se conecta siempre a `localhost:5432/punto_de_venta` con `postgres/root`. En una instalación con otra contraseña (el instalador la pide) **no funciona**; si hay otra base en ese puerto, **lee y escribe en ella**. | Credenciales fijas en `WebInventario.java:30-33`. | ✅ Corregido: `WebInventario` abre la conexión con `BaseDAO.nuevaConexion()`. |
| **D-02** | Alta | Ventas | Elegir una lista de precios (incluida *Mayoreo* con menos de 12 piezas o una lista nueva) no cambia lo cobrado. | `actualizarPrecioListaTemp` hace `UPDATE venta_temp` y el trigger `verif_exist_update` vuelve a poner menudeo/mayoreo según la cantidad. | ✅ Corregido (V1.23): columna `venta_temp.precio_fijo`; los triggers respetan el precio fijo. La lista *Menudeo* es el modo automático. |
| **D-04** | Alta | Cotizaciones | Al convertir, se cobra el precio según la cantidad (≥ 12 mayoreo), no el que se cotizó. Cotización 5 pzas a mayoreo = $40 → venta $50 (y al revés, 12 a menudeo se cobra más barato). | El `INSERT` a `venta_temp` pasa por `verif_exist_insert`, que recalcula precio y tipo. | ✅ Corregido (V1.23): la conversión copia los renglones con `precio_fijo = true`. |
| **D-01** | Media | Ventas | Se puede vender un producto **dado de baja** tecleando su código. | `resolverCodigo` y `set_venta_temp` no filtran `estatus='Activo'`. | ✅ Corregido (V1.23): `verif_exist_insert` y `reg_apdet` rechazan productos dados de baja. |
| **D-03** | Media | Devoluciones | Al quitar un producto de una devolución el stock regresa pero el **monto no baja**; el reporte diario y la caja descuentan de más. | `elim_devolucion_prod` no recalcula `devolucion_ventas.monto`. | ✅ Corregido (V1.23): `elim_devolucion_prod` recalcula el monto. |
| **D-10** | Media | Reportes | El valor de inventario del reporte diario (≈ $2,839) no coincide con el del balance (≈ $320,336). | `reporte_diario` usa `SUM(cantidad*precio_compra)` (ignora productos sin costo e incluye inactivos); el balance usa `valor_inventario_actual()`. | ✅ Corregido (V1.23): `reporte_diario` usa `valor_inventario_actual()`. |
| **D-05** | Media | Compras / Kardex | Quitar un renglón de una compra registrada deja en el kardex una **"Venta" que no existió** y rompe la cadena de existencias. | `elim_compra_prod` borra el renglón *Compra* del kardex y además baja el stock sin fijar `kardex.tipo`, así que el trigger lo anota como venta. | Registrar un *Ajuste* con referencia en lugar de borrar. |
| **D-06** | Media | Inventario / Kardex | Corregir existencias a mano (pantalla *Actualizar* o celular): si se bajan, el kardex dice *Venta*; si se suben, **no queda rastro**. | `fn_kardex_producto` sin `kardex.tipo`: baja → "Venta"; sube → borra el renglón previo. | Fijar `kardex.tipo='Ajuste'` al editar existencias. |
| **D-07** | Baja | Apartados / Kardex | Apartar productos se anota en el kardex como *Venta* sin referencia (al cancelar sí se anota *Ajuste* con folio). | `reg_apdet` no fija `kardex.tipo`/`referencia`. | Usar `mover_inventario_con_kardex(FALSE, …, 'Apartado', folio)`. |
| **D-09** | Baja | Ventas | `realizar_venta` con el carrito vacío crea una venta con `total_venta` NULL. La pantalla lo impide, pero cualquier otro camino (p. ej. doble clic en *Cobrar*) la dejaría. | No valida que haya renglones. | ✅ Corregido (V1.23): `realizar_venta` rechaza el carrito vacío. |
| **D-11** | Baja | Categorías | Se pueden crear dos categorías con el mismo nombre. | Sin `UNIQUE` en `categoria.nombre` ni validación en pantalla. | ✅ Corregido (V1.23): índice único `lower(trim(nombre))`; los duplicados existentes se renombran a "Nombre (id)". |

> Nota sobre D-08: durante esta ejecución, la prueba de alta desde el celular escribió un producto (`TWEBN77314`) en la base `punto_de_venta` del PostgreSQL de Windows. Se borró de inmediato (no tenía kardex ni precios) y la prueba ahora **no envía altas** si detecta que el servidor no está conectado a la base de pruebas.

Además, V1.23 hace que `eliminar_prod` no truene si se intenta quitar del carrito un producto que no está en él (antes fallaba con un error de kardex).

**Corregidos después:** D-05, D-06 y D-07 (kardex) en V1.24 y V1.25. Desde V1.24 el carrito ya no aparta existencia: se descuenta al cobrar, una sola vez y con su folio en el kardex.
