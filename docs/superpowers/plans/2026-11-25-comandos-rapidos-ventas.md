# Comandos rápidos en Ventas — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Que una venta completa se pueda hacer solo con teclado (o lector de código de barras) en la pantalla de Ventas: capturar, corregir el carrito, cobrar e imprimir el ticket.

**Architecture:** La interpretación de lo capturado, el manejo de montos y la protección contra un Enter de más viven en clases puras de `puntoventa.util`, con pruebas sin base de datos. `VentasP` registra los atajos con `InputMap`/`ActionMap` fuera de `initComponents`. El cobro pasa a un `CobroDialog` nuevo y la selección de cliente a un `SeleccionClienteDialog` nuevo. Los errores al agregar se muestran en una línea roja con sonido: la capa de datos gana una variante que regresa el mensaje en lugar de abrir una ventana. El aviso de poco stock pasa al botón Inventario del menú y a un filtro en `InventarioP`.

**Tech Stack:** Java Swing (proyecto NetBeans/Ant), PostgreSQL vía JDBC. Las pruebas son clases `main` con `pruebas.Prueba.check(...)`.

**Spec:** `docs/superpowers/specs/2026-11-25-comandos-rapidos-ventas-design.md`

## Global Constraints

- No editar a mano `initComponents()` ni los `.form`. Los componentes y atajos nuevos se crean en el constructor o en métodos propios, como ya se hace con `dctProd`/`cmbListaPrecios`. Sí se puede cambiar el **cuerpo** de los métodos de evento `xxxActionPerformed`/`xxxKeyTyped`.
- Los textos de la interfaz, nombres de clases, métodos y variables van en español.
- Sin migraciones de base de datos.
- **No ejecutar `ant compile` ni `ant run`** (el usuario compila y prueba la app él mismo). Para compilar y probar se usa el script de pruebas, que compila `src/` + `test/` con `javac` en `build/pruebas-bd/`.
- Commits sin línea `Co-Authored-By`. Rama: `feat/comandos-rapidos-ventas`.
- Cliente genérico: `XAXX111111HCCXXXX0` / `"Público general"`.
- Formas de pago, exactas: `"Efectivo"`, `"Transferencia"`, `"Tarjeta"`, `"Otro"`.
- Poco stock = producto `estatus='Activo'` con `cantidad <= 12` (criterio actual).
- Protección contra Enter de más: **300 ms** desde que se abre el diálogo.
- Fuente de la interfaz: `"Noto Serif"`. Rojo de aviso: `new java.awt.Color(190, 30, 30)` (el que ya usa `InventarioP`).
- Atajos de pantalla: `Enter` agregar/cobrar con campo vacío, `F2` buscar, `F4` editar renglón, `F12` cobrar, `Supr` quitar, `+`/`-` cantidad, `↑/↓` moverse. En el cobro: `F1` Efectivo, `F2` Transferencia, `F3` Tarjeta, `F4` Otro, `F5` cliente, `Enter` cobrar, `Esc` volver. En el ticket (solo desde Ventas): `Enter` imprimir, `Esc` cerrar. No usar `F10`.

## Review Focus

1. **Lector escaneando el mismo producto muchas veces seguidas:** cada escaneo debe sumar 1, sin perder ninguno y sin borrar el descuento que ya tenía el renglón (Task 3: solo se aplica descuento si `dctProd > 0`).
2. **Códigos con ceros a la izquierda o con guion** (`0012`, `ABC-12`): el código no se convierte a número y se resuelve tal cual (Task 1, prueba `"0012"`; Task 3, `-` con texto escribe guion).
3. **Espacios y formatos raros en el campo** (`" 3 * 750 "`, `"3*"`, `"0*750"`, `"*750"`): se recortan o se rechazan con mensaje claro, nunca hay una excepción (Task 1).
4. **Montos en el cobro:** `"74.1"` contra un total de `74.10` debe alcanzar (tolerancia de centavos). `"100,50"`, `"$100"` o vacío se rechazan en la línea del diálogo, sin ventana (Task 1, `Cobro`).
5. **Enter sostenido o doble** al abrir el cobro o el ticket: no cobra ni imprime solo (Task 1, `GuardaEnter`; Tasks 6).

---

### Task 1: Lógica pura — captura, montos y protección de Enter

**Files:**
- Create: `src/puntoventa/util/CapturaVenta.java`
- Create: `src/puntoventa/util/Cobro.java`
- Create: `src/puntoventa/util/GuardaEnter.java`
- Test: `test/puntoventa/util/ComandosRapidosPrueba.java`

**Interfaces:**
- Produces:
  - `CapturaVenta.interpretar(String entrada) -> CapturaVenta` (nunca `null`, nunca lanza excepciones).
  - `CapturaVenta.Tipo { VACIO, PRODUCTO, INVALIDO }`. Campos `public final`: `Tipo tipo`, `int cantidad` (1 si no hubo prefijo), `String texto` (código o texto a buscar, recortado; `""` si no aplica), `String mensaje` (solo con `INVALIDO`). Método `boolean soloDigitos()` (true si `texto` no está vacío y son solo dígitos).
  - `CapturaVenta.CANTIDAD_MAXIMA = 9999`, `LONGITUD_MAXIMA = 40`.
  - `Cobro.parsearMonto(String) -> Double` (`null` si no es un número ≥ 0 con a lo más un punto y dos decimales; no acepta `,` ni `$`).
  - `Cobro.alcanza(double total, double recibido) -> boolean` (`recibido + 0.005 >= total`).
  - `Cobro.cambio(double total, double recibido) -> double` (redondeado a 2 decimales, nunca negativo).
  - `Cobro.formato(double) -> String` (`"%.2f"` con `Locale.US`, p. ej. `"74.10"`).
  - `GuardaEnter(long esperaMs)`, `void abrir(long ahoraMs)`, `boolean permite(long ahoraMs)` (false si no se ha llamado `abrir` o si `ahora - apertura < esperaMs`).

- [ ] **Step 1: Escribir la prueba que falla** `test/puntoventa/util/ComandosRapidosPrueba.java` (paquete `puntoventa.util`, `import static pruebas.Prueba.*;`, `main` que termina con `terminar("ComandosRapidosPrueba")`). Casos (`check(condición, "CU-CR-xx descripción")`):

```java
CapturaVenta c;
c = CapturaVenta.interpretar("");            check(c.tipo == CapturaVenta.Tipo.VACIO, "CU-CR-01 vacío");
c = CapturaVenta.interpretar("   ");         check(c.tipo == CapturaVenta.Tipo.VACIO, "CU-CR-01 solo espacios");
c = CapturaVenta.interpretar(null);          check(c.tipo == CapturaVenta.Tipo.VACIO, "CU-CR-01 null");
c = CapturaVenta.interpretar("7501234567890");
check(c.tipo == CapturaVenta.Tipo.PRODUCTO && c.cantidad == 1 && c.texto.equals("7501234567890") && c.soloDigitos(), "CU-CR-02 código cantidad 1");
c = CapturaVenta.interpretar("0012");        check(c.texto.equals("0012") && c.soloDigitos(), "CU-CR-02 conserva ceros");
c = CapturaVenta.interpretar("3*7501234567890");
check(c.tipo == CapturaVenta.Tipo.PRODUCTO && c.cantidad == 3 && c.texto.equals("7501234567890"), "CU-CR-03 N*código");
c = CapturaVenta.interpretar(" 3 * 750 ");   check(c.cantidad == 3 && c.texto.equals("750"), "CU-CR-03 tolera espacios");
c = CapturaVenta.interpretar("ABC-12");      check(c.tipo == CapturaVenta.Tipo.PRODUCTO && !c.soloDigitos(), "CU-CR-04 código con guion no es soloDígitos");
c = CapturaVenta.interpretar("coca cola");   check(c.tipo == CapturaVenta.Tipo.PRODUCTO && c.texto.equals("coca cola") && !c.soloDigitos(), "CU-CR-04 texto");
c = CapturaVenta.interpretar("2*coca");      check(c.cantidad == 2 && c.texto.equals("coca"), "CU-CR-04 N*texto");
for (String malo : new String[]{"3*", "0*750", "*750", "abc*750", "-2*750", "10000*750", "2*3*750"}) {
    c = CapturaVenta.interpretar(malo);
    check(c.tipo == CapturaVenta.Tipo.INVALIDO && c.mensaje != null && !c.mensaje.isEmpty(), "CU-CR-05 inválido: " + malo);
}
check(CapturaVenta.interpretar("9999*1").cantidad == 9999, "CU-CR-05 máximo permitido");

check(Cobro.parsearMonto("100") == 100.0 && Cobro.parsearMonto("74.1") == 74.1, "CU-CR-06 montos válidos");
for (String m : new String[]{"", "100,50", "$100", "1.2.3", "-5", "abc", "1.234"}) check(Cobro.parsearMonto(m) == null, "CU-CR-06 monto inválido: " + m);
check(Cobro.alcanza(74.10, 74.1) && !Cobro.alcanza(74.10, 74.0), "CU-CR-07 alcanza con tolerancia");
check(Cobro.cambio(74.0, 100.0) == 26.0 && Cobro.cambio(74.0, 50.0) == 0.0, "CU-CR-07 cambio");
check(Cobro.formato(74.1).equals("74.10"), "CU-CR-07 formato");

GuardaEnter g = new GuardaEnter(300);
check(!g.permite(1000), "CU-CR-08 sin abrir no permite");
g.abrir(1000);
check(!g.permite(1000) && !g.permite(1299) && g.permite(1300) && g.permite(5000), "CU-CR-08 bloquea los primeros 300 ms");
```

- [ ] **Step 2: Correr y ver que falla (no compila)**

Run: `SIN_RECREAR=1 bash test/correr_pruebas_bd.sh puntoventa.util.ComandosRapidosPrueba`
Expected: `COMPILACION FALLIDA` (no existen las clases).

- [ ] **Step 3: Implementar `CapturaVenta`, `Cobro` y `GuardaEnter`**

Regla de `interpretar`: recortar. Si queda vacío → `VACIO`. Si contiene `*`: separar en el **primer** `*`. La parte izquierda recortada debe ser un entero de 1 a `CANTIDAD_MAXIMA` (solo dígitos) y la derecha recortada no puede estar vacía ni contener otro `*`; si no se cumple → `INVALIDO`. Mensajes: `"Cantidad no válida: use un número de 1 a 9999 antes del *"` y `"Falta el código después de N*"`. Si no hay `*` → `PRODUCTO` con cantidad 1. Las tres clases son `final`, sin Swing ni base de datos.

- [ ] **Step 4: Correr y ver que pasa**

Run: `SIN_RECREAR=1 bash test/correr_pruebas_bd.sh puntoventa.util.ComandosRapidosPrueba`
Expected: el resumen de `terminar` sin fallas y código de salida 0.

- [ ] **Step 5: Commit**

```bash
git add src/puntoventa/util/CapturaVenta.java src/puntoventa/util/Cobro.java src/puntoventa/util/GuardaEnter.java test/puntoventa/util/ComandosRapidosPrueba.java
git commit -m "feat(ventas): lógica de captura rápida, montos y protección de Enter"
```

---

### Task 2: Capa de datos — errores sin ventana y conteo de poco stock

**Files:**
- Modify: `src/puntoventa/db/VentaDAO.java` (junto a `insertarVentaTemp`, ~línea 77)
- Modify: `src/puntoventa/db/ProductoDAO.java` (junto a `obtenerProductosPocos`, ~línea 354)
- Modify: `src/puntoventa/db/ConexionBD.java` (delegados junto a las líneas 68 y 73)
- Test: `test/puntoventa/db/ComandosRapidosPruebaBD.java`

**Interfaces:**
- Produces:
  - `VentaDAO.insertarVentaTempSinDialogo(String[] campos, boolean acum) -> String`: `null` si todo salió bien; si no, el mensaje de `GestorErrores.mensajeAmigable(e)` después de `GestorErrores.registrar(e)`. Nunca abre ventanas.
  - `VentaDAO.actualizarDescuentoTempSinDialogo(String idProducto, double descuento) -> String` con el mismo contrato.
  - `ProductoDAO.contarProductosPocos() -> int` (`SELECT count(*) FROM producto WHERE estatus='Activo' AND cantidad<=12`; 0 si hay error, registrándolo).
  - Delegados de mismo nombre en `ConexionBD`.
  - `insertarVentaTemp` y `actualizarDescuentoTemp` existentes **no cambian** su comportamiento (otros módulos los usan). Para no duplicar código, se implementan delegando en la variante nueva y mostrando el mensaje con `Mise.JOption` si no es `null`.

- [ ] **Step 1: Escribir la prueba que falla** `test/puntoventa/db/ComandosRapidosPruebaBD.java` (paquete `puntoventa.db`, `import static pruebas.Prueba.*; import static pruebas.BD.*;`). Se corre headless, así que cualquier ventana lanzaría `HeadlessException` y tumbaría la prueba: eso valida "sin diálogo".

```java
VentaDAO v = new VentaDAO(); v.limpiarVentaTemp();
String p = producto("CR", 5, 10.0, 8.0);
check(v.insertarVentaTempSinDialogo(new String[]{p, "2"}, true) == null, "CU-CR-10 alta sin error regresa null");
check(entero("SELECT cantidad_prod FROM venta_temp WHERE id_producto=?", p) == 2, "CU-CR-10 entró al carrito");
String err = v.insertarVentaTempSinDialogo(new String[]{p, "9"}, true);
check(err != null && err.contains("No hay suficiente cantidad"), "CU-CR-11 sin existencia regresa mensaje: " + err);
check(entero("SELECT cantidad_prod FROM venta_temp WHERE id_producto=?", p) == 2, "CU-CR-11 carrito intacto");
err = v.insertarVentaTempSinDialogo(new String[]{"NOEXISTE999", "1"}, true);
check(err != null && err.contains("no existe"), "CU-CR-12 inexistente regresa mensaje: " + err);
check(v.actualizarDescuentoTempSinDialogo(p, 0) == null, "CU-CR-13 descuento sin error");
v.limpiarVentaTemp();
int esperado = entero("SELECT count(*)::int FROM producto WHERE estatus='Activo' AND cantidad<=12");
check(new ProductoDAO().contarProductosPocos() == esperado, "CU-CR-14 conteo de poco stock = " + esperado);
terminar("ComandosRapidosPruebaBD");
```

- [ ] **Step 2: Correr y ver que falla**

Run: `bash test/correr_pruebas_bd.sh puntoventa.db.ComandosRapidosPruebaBD`
Expected: `COMPILACION FALLIDA`.

- [ ] **Step 3: Implementar los métodos y delegados** descritos en Interfaces.

- [ ] **Step 4: Correr esta prueba y la suite de ventas**

Run: `bash test/correr_pruebas_bd.sh puntoventa.db.ComandosRapidosPruebaBD puntoventa.db.VentasPruebaBD`
Expected: ambas sin fallas (VentasPruebaBD confirma que `insertarVentaTemp` sigue igual).

- [ ] **Step 5: Commit**

```bash
git add src/puntoventa/db/VentaDAO.java src/puntoventa/db/ProductoDAO.java src/puntoventa/db/ConexionBD.java test/puntoventa/db/ComandosRapidosPruebaBD.java
git commit -m "feat(ventas): agregar al carrito sin ventana de error y conteo de poco stock"
```

---

### Task 3: Ventas — captura por teclado, línea de aviso y barra de ayuda

**Files:**
- Modify: `src/puntoventa/ui/VentasP.java`

**Interfaces:**
- Consumes: `CapturaVenta`, `ConexionBD.insertarVentaTempSinDialogo`, `ConexionBD.actualizarDescuentoTempSinDialogo`, `ConexionBD.resolverCodigo`.
- Produces (los usan las Tasks 4–6):
  - `void mostrarAviso(String mensaje)`: pone el texto en la línea roja y suena `Toolkit.getDefaultToolkit().beep()`. `void limpiarAviso()`.
  - `boolean agregarProducto(String idProd, int cantidad)`: inserta con `acum=true`. Aplica descuento **solo si** `dctProd > 0` (con el tope de `obtenerMaxDescuento`, avisando el tope por la línea roja). Aplica la lista de precios, refresca la tabla, deja seleccionado el renglón de ese producto, limpia el aviso si todo salió bien y regresa `true`. Si falla, muestra el aviso y regresa `false`.
  - `void seleccionarRenglon(String idProd)`: selecciona en `tablaVentas` la fila con ese id, si existe.
  - `public void enfocarCodigo()`: `codigoProd.requestFocusInWindow()` vía `SwingUtilities.invokeLater`.
  - Campo `int cantidadPendiente` (default 1) y `void procesarCaptura()`: la acción de Enter en `codigoProd`.

Decisiones:
- **Línea de aviso y barra de ayuda:** las filas del `GridBagLayout` de `jPanel2` las fija el `.form` (0 total, 1 descuento, 2 código, 3 cantidad, 4 registrar…), así que no cabe una fila "justo debajo" sin moverlas. Van en un `JPanel` nuevo en `BorderLayout.SOUTH` de `VentasP`: arriba, la línea de aviso (`JLabel`, Noto Serif negrita 20, rojo de aviso, texto `" "` cuando está vacía para no cambiar la altura); abajo, la ayuda: `Enter Agregar · F2 Buscar · F4 Cantidad · Supr Quitar · +/- Cantidad · F12 COBRAR` (Noto Serif 16).
- `aplicarPrecioLista` deja de abrir `JOptionPane`: regresa `String` (aviso o `null`) y `agregarProducto` lo muestra en la línea roja. Antes de cambiarle la firma, revisar dentro de `VentasP` quién más lo llama.

- [ ] **Step 1: Cambiar el cuerpo de `codigoProdKeyTyped`**: aceptar cualquier carácter imprimible (no de control) mientras el texto mida menos de `CapturaVenta.LONGITUD_MAXIMA`. Seguir consumiendo el resto.

- [ ] **Step 2: Implementar `procesarCaptura()` y registrarla** en `codigoProd.getInputMap(JComponent.WHEN_FOCUSED)` para `ENTER`, reemplazando la acción de commit de `JFormattedTextField`. Flujo:
  - `VACIO` → `abrirCobro()`. En esta task es un método vacío; se llena en la Task 6.
  - `INVALIDO` → `mostrarAviso(mensaje)`, se deja el texto y se selecciona todo.
  - `PRODUCTO` → `resolverCodigo(texto)`; si es `null`, se reintenta con `texto.toUpperCase()`.
    - Si encontró el producto: `agregarProducto(id, cantidad)`. Si salió bien, se limpia el campo; si no, se selecciona todo el texto.
    - Si no lo encontró y `soloDigitos()`: `mostrarAviso("No existe un producto con el código " + texto)` y se selecciona el texto.
    - Si no lo encontró y es texto: `irABusqueda(texto, cantidad)`, de la Task 4. En esta task basta un método que por ahora solo muestre un aviso.

- [ ] **Step 3: El botón Registrar (`regActionPerformed`) usa el mismo camino**: el texto de `codigoProd` pasa por `CapturaVenta`. Si `cantidadProd` tiene un número, ese gana sobre la cantidad del prefijo; si está vacío, se usa la de la captura (1 por defecto). Se quitan los `JOptionPane` del método en favor de `mostrarAviso`.

- [ ] **Step 4: Agregar el panel SUR** (aviso + ayuda) en el constructor, y `enfocarCodigo()` al final del constructor.

- [ ] **Step 5: Compilar**

Run: `SIN_RECREAR=1 bash test/correr_pruebas_bd.sh puntoventa.util.ComandosRapidosPrueba`
Expected: compila y la prueba pasa.

- [ ] **Step 6: Commit**

```bash
git add src/puntoventa/ui/VentasP.java
git commit -m "feat(ventas): capturar productos con Enter, N*código y aviso en rojo con sonido"
```

---

### Task 4: Ventas — búsqueda sin mouse

**Files:**
- Modify: `src/puntoventa/ui/VentasP.java`

**Interfaces:**
- Consumes: `agregarProducto`, `mostrarAviso`, `enfocarCodigo`, `cantidadPendiente` (Task 3); `mostrarBusqueda(String)` existente.
- Produces: `void irABusqueda(String texto, int cantidad)`: guarda `cantidadPendiente = cantidad`, pone `texto` en `Busc`, llama `mostrarBusqueda(texto)` y enfoca `Busc`.

- [ ] **Step 1: `F2`** registrado en `VentasP.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)` → `irABusqueda(codigoProd.getText().trim(), 1)`. Si el campo trae `N*texto`, se pasa por `CapturaVenta` para separar la cantidad.

- [ ] **Step 2: Teclas en `Busc`** (`WHEN_FOCUSED`):
  - `ENTER`: si `modeloBusc` tiene 1 fila, la agrega. Si tiene más, selecciona la fila 0 y enfoca `tablaBusc`. Si no tiene ninguna, `mostrarAviso("No se encontraron productos con \"" + texto + "\"")`.
  - `DOWN`: igual que Enter con varias filas.
  - `ESCAPE`: `volverACodigo()`, que limpia `Busc`, llama `mostrarBusqueda("")`, regresa `cantidadPendiente = 1` y llama `enfocarCodigo()`.

  Agregar una fila es `agregarProducto(idDeLaFila, cantidadPendiente)`. Si sale bien, `volverACodigo()` y se limpia `codigoProd`.

- [ ] **Step 3: Teclas en `tablaBusc`**: `ENTER` en `getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)`, que es donde `JTable` trae su acción de "siguiente fila" y hay que reemplazarla, agrega la fila seleccionada. `ESCAPE` hace `volverACodigo()`.

- [ ] **Step 4: Cambiar el cuerpo de `BuscKeyTyped`**: aceptar letras (incluidas acentuadas y ñ, con `Character.isLetter`), dígitos, espacio y `-`. Máximo `CapturaVenta.LONGITUD_MAXIMA` caracteres y sin espacios al inicio, como hoy.

- [ ] **Step 5: Reemplazar el método provisional `irABusqueda` de la Task 3** por el real.

- [ ] **Step 6: Compilar** (mismo comando que en la Task 3, Step 5). Expected: compila y pasa.

- [ ] **Step 7: Commit**

```bash
git add src/puntoventa/ui/VentasP.java
git commit -m "feat(ventas): búsqueda de productos con teclado (F2, flechas, Enter, Esc)"
```

---

### Task 5: Ventas — editar el carrito sin mouse

**Files:**
- Modify: `src/puntoventa/ui/VentasP.java`

**Interfaces:**
- Consumes: `seleccionarRenglon`, `mostrarAviso`, `agregarProducto`, `enfocarCodigo` (Task 3); `ConexionBD.insertarVentaTempSinDialogo`, `ConexionBD.actualizarDescuentoTempSinDialogo`, `eliminarVentaTemp` (Task 2 / existente).
- Produces: `void cambiarCantidadSeleccionada(int delta)`, `void quitarSeleccionado()`.

- [ ] **Step 1: Teclas en `codigoProd` (`WHEN_FOCUSED`) que actúan solo con el campo vacío.** Con texto, cada tecla hace lo normal de un campo de texto: para `+`/`-` hay que insertar el carácter a mano, porque el binding se lo come. Si no hay renglón seleccionado, no hacen nada.
  - `UP`/`DOWN` mueven la selección de `tablaVentas` y la hacen visible (`scrollRectToVisible`). Sin selección, `UP` selecciona el último renglón y `DOWN` el primero.
  - `DELETE` → `quitarSeleccionado()`.
  - `typed +`/`typed -`, más las del teclado numérico (`ADD`/`SUBTRACT`) → `cambiarCantidadSeleccionada(±1)`.

- [ ] **Step 2: `cambiarCantidadSeleccionada(delta)`**:
  - La nueva cantidad es la actual + `delta`. Si llega a 0 o menos, se quita el renglón.
  - Si no, `insertarVentaTempSinDialogo({id, nueva}, false)`. Luego se vuelve a aplicar el descuento que ya tenía el renglón (columna 6) con `actualizarDescuentoTempSinDialogo` y la lista de precios.
  - Al final se refresca la tabla y se hace `seleccionarRenglon(id)`. Cualquier error va por `mostrarAviso`.

- [ ] **Step 3: `F4`** en el `InputMap` de pantalla → `actBActionPerformed(null)`, que abre el diálogo actual con el renglón seleccionado. Sin selección, `mostrarAviso("Seleccione un renglón con ↑/↓")` en lugar del `JOptionPane` actual. Dentro de `ventaDialog`, en su `JRootPane`: `ENTER` → `hechoAct.doClick()` y `ESCAPE` → ocultarlo y `enfocarCodigo()`. El foco entra en `cantP` con el texto seleccionado.

- [ ] **Step 4: Cambiar el cuerpo de `hechoActActionPerformed`, `elimActionPerformed` y `actBActionPerformed`**: usar las variantes `SinDialogo` y `mostrarAviso` en lugar de `JOptionPane`, incluido el aviso del tope de descuento. Al terminar, `enfocarCodigo()`.

- [ ] **Step 5: Compilar** (mismo comando). Expected: compila y pasa.

- [ ] **Step 6: Commit**

```bash
git add src/puntoventa/ui/VentasP.java
git commit -m "feat(ventas): editar el carrito con flechas, +/-, Supr y F4"
```

---

### Task 6: Cobro con teclado, selección de cliente y ticket

**Files:**
- Create: `src/puntoventa/ui/CobroDialog.java`
- Create: `src/puntoventa/ui/SeleccionClienteDialog.java`
- Modify: `src/puntoventa/ui/TicketDialog.java`
- Modify: `src/puntoventa/ui/VentasP.java`

**Interfaces:**
- Consumes: `Cobro`, `GuardaEnter` (Task 1); `enfocarCodigo`, `mostrarAviso` (Task 3); `ConexionBD.buscarClientesActivos(String)` existente.
- Produces:
  - `CobroDialog(java.awt.Window owner, double total)`: modal. `CobroDialog.Resultado mostrar()` regresa `null` si se canceló. `Resultado` tiene los campos `public final`: `String formaPago`, `double recibido`, `double cambio`, `String idCliente`, `String nombreCliente`.
  - `SeleccionClienteDialog.elegir(java.awt.Window owner, ConexionBD conect) -> String[]` (`{id, nombre}` o `null` si se canceló).
  - `TicketDialog.activarTeclasRapidas()`: registra `ENTER` → imprimir y `ESCAPE` → cerrar en su `JRootPane`, protegidos con un `GuardaEnter(300)` que se abre en `windowOpened`. Solo lo llama Ventas; Apartados no cambia.
  - `VentasP.abrirCobro()` y `VentasP.setAlCambiarStock(Runnable)`.

- [ ] **Step 1: `CobroDialog`**, con el diseño del spec, sección 2:
  - Etiquetas grandes de Total y CAMBIO, y el campo Recibido con el total escrito (`Cobro.formato`) y seleccionado.
  - Una fila de cuatro botones de forma de pago; el seleccionado se marca en negrita o con color.
  - Una etiqueta `Cliente: Público general (F5 cambiar)` y una línea de error roja dentro del diálogo.
  - Teclas en el `JRootPane` con `WHEN_IN_FOCUSED_WINDOW`:
    - `F1`–`F4` cambian la forma de pago. Si no es `"Efectivo"`, Recibido queda igual al total y deshabilitado, y el cambio en 0.
    - `F5` llama a `SeleccionClienteDialog.elegir`.
    - `ENTER` cobra, pero solo si `guarda.permite(System.currentTimeMillis())`. Valida con `Cobro.parsearMonto` y `Cobro.alcanza`; si algo falla, el error va a la línea roja del diálogo.
    - `ESCAPE` → `null`.
  - El cambio se recalcula con un `DocumentListener` sobre Recibido.
  - `guarda.abrir(...)` se llama en `windowOpened`.

- [ ] **Step 2: `SeleccionClienteDialog`**: campo de búsqueda más tabla (id, nombre) llenada con `buscarClientesActivos(filtro)` mientras se escribe.
  - En el campo: `DOWN`/`ENTER` entran a la tabla con la fila 0 seleccionada.
  - En la tabla: `ENTER` (en `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT`) elige.
  - `ESCAPE` en cualquier lado cancela.
  - Un botón "Registrar nuevo cliente" llama `ClientesP.Registro.setVisible(true)` y al volver recarga la tabla.

- [ ] **Step 3: `TicketDialog.activarTeclasRapidas()`**, según la sección Interfaces.

- [ ] **Step 4: En `VentasP`**:
  - Extraer de `hechoCambActionPerformed` un método `private boolean registrarVenta(CobroDialog.Resultado r)`. Hace lo mismo que hoy (guardar los descuentos de cada renglón, `registrarVentaConFormaPago(Interfaz.idVendedor, r.idCliente, r.formaPago)`, `actualizarDescuentoVenta`, `limpiarVentaTemp`, limpiar la tabla y el total, `dctGlobal` en `"0"`), arma `TicketData` con `r.recibido`/`r.cambio` y muestra el `TicketDialog` con `activarTeclasRapidas()`.
  - **Se quitan** `mostrarProdPocos()` y la apertura de `prodPocosDialog`. En su lugar se ejecuta el `Runnable` de `setAlCambiarStock`, si existe.
  - Si `registrarVentaConFormaPago` falla, regresa `false` y el carrito se queda.
  - `abrirCobro()`: con el carrito vacío, `mostrarAviso("No hay productos para cobrar")`. Si no, crea el `CobroDialog` con `conect.sumaVentaTemp()`, `mostrar()` y, si no es `null`, `registrarVenta`. Al final, `enfocarCodigo()`.
  - `F12` en el `InputMap` de pantalla → `abrirCobro()`. `ventaRealiActionPerformed` → `abrirCobro()`.
  - El cuerpo de `hechoCambActionPerformed` y de `tablaCliMouseClicked` queda como un comentario `// Reemplazado por CobroDialog (comandos rápidos)`, porque `regVentaDialog`/`panelCamb` ya no se muestran. Se borra el método `mostrarProdPocos`. El `.form` no se toca.

- [ ] **Step 5: Compilar y correr las pruebas de ventas**

Run: `bash test/correr_pruebas_bd.sh puntoventa.util.ComandosRapidosPrueba puntoventa.db.VentasPruebaBD puntoventa.ui.PantallasPruebaBD`
Expected: todo sin fallas (PantallasPruebaBD construye las pantallas).

- [ ] **Step 6: Commit**

```bash
git add src/puntoventa/ui/CobroDialog.java src/puntoventa/ui/SeleccionClienteDialog.java src/puntoventa/ui/TicketDialog.java src/puntoventa/ui/VentasP.java
git commit -m "feat(ventas): cobrar con F12/Enter, formas de pago y cliente con teclado, ticket con Enter/Esc"
```

---

### Task 7: Aviso de poco stock en el menú y filtro en Inventario

**Files:**
- Modify: `src/puntoventa/ui/Interfaz.java` (`visibilidad(int,int)` ~línea 914; el `case 2` y la creación de `ventPanel` ~línea 1140)
- Modify: `src/puntoventa/ui/InventarioP.java`

**Interfaces:**
- Consumes: `ConexionBD.contarProductosPocos()` (Task 2), `VentasP.setAlCambiarStock`, `VentasP.enfocarCodigo` (Tasks 3 y 6), `ConexionBD.obtenerProductosPocos()` existente.
- Produces: `Interfaz.actualizarAvisoStock()`.

- [ ] **Step 1: `actualizarAvisoStock()`**: con N > 0, `inventButton` muestra `"Inventario (⚠ N)"` con el texto en el rojo de aviso; con 0, `"Inventario"` con el color original, que se guarda al iniciar. Si `⚠` sale como un cuadro en la fuente del menú, se usa `"Inventario (N)"` en rojo.
  - Se llama al inicio de `visibilidad(...)`, que se ejecuta al iniciar sesión y en cada cambio de módulo.
  - Al crear `ventPanel` se llama `ventPanel.setAlCambiarStock(this::actualizarAvisoStock)`.
  - En el `case 2`, después de `setVisible(true)`, se llama `ventPanel.enfocarCodigo()`.

- [ ] **Step 2: Agregar a `InventarioP` una casilla `JCheckBox` "Solo poco stock"** (Noto Serif negrita 16), en el mismo contenedor que `filtroCategoria` (`filtroCategoria.getParent()`, justo después).
  - Marcada: `mostrarInventario(conect.obtenerProductosPocos())`. `llenarTabla` ya tolera la falta de `categoria_nombre`.
  - Desmarcada: vuelve a la vista que corresponde al filtro de categoría actual.
  - Si se elige una categoría, la casilla se desmarca.

- [ ] **Step 3: Compilar y correr las pantallas**

Run: `bash test/correr_pruebas_bd.sh puntoventa.ui.PantallasPruebaBD`
Expected: sin fallas.

- [ ] **Step 4: Commit**

```bash
git add src/puntoventa/ui/Interfaz.java src/puntoventa/ui/InventarioP.java
git commit -m "feat(inventario): aviso de poco stock en el menú y filtro en Inventario"
```

---

### Task 8: Manual de usuario y lista de prueba manual

**Files:**
- Modify: `docs/manuales/03-ventas/README.md`
- Modify: `docs/manuales/02-inventario/README.md`
- Modify: `docs/pruebas/PLAN_DE_PRUEBAS.md` (agregar `ComandosRapidosPrueba` y `ComandosRapidosPruebaBD` a la tabla de clases de prueba)

- [ ] **Step 1: En el manual de Ventas**, una sección "Cobrar con el teclado" con la tabla de atajos de Global Constraints, el uso de `N*código` y lo que significa la línea roja. Se ajusta la parte de "Elegir al cliente": ahora es Público general por defecto y `F5` para cambiar.
- [ ] **Step 2: En el manual de Inventario**, un párrafo sobre el aviso del menú y la casilla "Solo poco stock".
- [ ] **Step 3: Agregar a `PLAN_DE_PRUEBAS.md`** las dos clases, más esta lista manual (la interfaz la prueba el usuario):
  - Escanear un mismo producto 5 veces rápido: queda con cantidad 5.
  - `3*` y luego escanear: suma 3.
  - Escanear un código no registrado: aviso rojo, bip, y el siguiente escaneo lo reemplaza.
  - Escribir `coca` y Enter: búsqueda. Con varios resultados, ↓ y Enter agrega.
  - ↑/↓, `+`/`-`, `Supr` y `F4` sobre el carrito.
  - `F12`, Enter: cobra con pago justo a Público general.
  - `F12`, `500`, Enter: muestra el cambio. `F3`: Recibido deshabilitado.
  - `F5`: elegir cliente por nombre con el teclado.
  - En el ticket: Enter imprime y Esc cierra; el foco regresa a Código.
  - Mantener Enter presionado al cobrar: no cobra solo.
  - El ticket desde Apartados se comporta igual que antes.
  - El menú muestra `Inventario (⚠ N)` y la casilla filtra.
- [ ] **Step 4: Commit**

```bash
git add docs/manuales/03-ventas/README.md docs/manuales/02-inventario/README.md docs/pruebas/PLAN_DE_PRUEBAS.md
git commit -m "docs(ventas): atajos de teclado en el manual y casos de prueba"
```
