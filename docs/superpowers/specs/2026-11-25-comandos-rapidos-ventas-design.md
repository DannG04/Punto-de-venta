# Comandos rápidos en Ventas — Diseño

Fecha: 2026-11-25

## Intención

Quien cobra debe poder hacer una venta completa **solo con el teclado**, como en los
POS comerciales: escanear o teclear un producto, Enter, el siguiente… y cobrar con una
tecla + Enter. Hoy el flujo obliga a usar el mouse en cada venta (botón de registrar,
botón "Venta Hecha", **clic** en una fila de la tabla de clientes, botón de cobrar).

Debe funcionar **con y sin lector de código de barras**. Un lector se comporta como un
teclado que escribe el código y pulsa Enter, así que si el flujo de teclado funciona,
el lector funciona sin código especial.

La mayoría de las ventas son a **Público general** (`id_cliente = 'XAXX111111HCCXXXX0'`,
creado en V1.22). Elegir un cliente con nombre es opcional y sirve solo para llevar
control; más adelante podría usarse para descuentos.

## Metas

- Agregar productos solo con el campo de código + Enter, con cantidad 1 por defecto.
- Cobrar sin mouse: `F12` (o Enter con el campo de código vacío) → diálogo de pago → Enter.
- Corregir el carrito con el teclado (moverse, quitar y cambiar cantidad).
- Que el aviso de poco stock deje de interrumpir el cobro.

## No metas

- Atajos en otros módulos (Apartados, Devoluciones, etc.) o navegar entre módulos con teclado.
- Atajos configurables por el usuario.
- Descuentos por cliente (solo se deja el lugar: el cliente elegido se puede cambiar en el cobro).
- Venta por peso o con cantidades decimales (la cantidad sigue siendo entera).

## Anti-metas (sería un fracaso aunque funcione)

- Que un Enter de más cobre una venta que no se quería cobrar.
- Que se rompa el flujo actual con mouse: los botones existentes siguen funcionando.
- Que los atajos de Ventas se disparen en otros módulos o en otros diálogos.

## Enfoques considerados

- **A. Atajos registrados en `VentasP` y sus diálogos (`InputMap`/`ActionMap`) + un
  `CobroDialog` nuevo + la lógica de captura en una clase pura. — ELEGIDO.**
  Está acotado a Ventas, casi no toca el código generado por NetBeans, la lógica se
  puede probar sin interfaz y el cobro queda en un diálogo limpio.
- B. Igual, pero reusando `ventaDialog` y `regVentaDialog` tal cual. Es menos código, pero
  `ventaDialog` mezcla "actualizar renglón" y "cobrar" en un mismo panel con `CardLayout`
  y es frágil de mantener desde el editor visual.
- C. Un `KeyEventDispatcher` global. Es útil si algún día hay atajos en toda la aplicación,
  pero para algo que solo vive en Ventas es difícil de acotar y propenso a que las teclas se
  disparen donde no deben.

## Diseño

### 1. Captura de productos (pantalla de Ventas)

El foco vive en el campo **Código** (`codigoProd`). Vuelve ahí al entrar al panel, después
de agregar un producto, al cerrar cualquier diálogo de Ventas y al terminar una venta.

**Sintaxis del campo de código** (la interpreta una clase pura, p. ej.
`puntoventa.util.CapturaVenta`, con pruebas):

| Lo que escriben + Enter | Resultado |
|---|---|
| `7501234567890` (código o código de barras existente) | Se agrega con cantidad 1. Si ya está en el carrito, se le suma 1 (`set_venta_temp` con `acum=true` ya hace esto). |
| `3*7501234567890` | Se agrega con cantidad 3 (o se le suman 3). |
| `3*` y luego escanean | Igual que el anterior: el lector escribe el código después del `3*` y manda Enter. |
| Algo con forma de código (solo dígitos) que **no existe** (p. ej. un código de barras no registrado) | **No** va a la búsqueda: aviso en rojo + sonido (sección 1b), el texto se queda en Código y se selecciona, así el siguiente escaneo lo reemplaza. |
| Texto que no es un código existente (`coca`) | Se pasa el texto a la búsqueda de productos (ver abajo). Si traía `N*`, la cantidad se conserva para cuando elijan el producto. |
| Vacío | Abre el cobro (sección 2), solo si el carrito tiene productos. |
| Cantidad inválida (`0*…`, `abc*…`) | Aviso en rojo + sonido (sección 1b) y el texto se queda en el campo para corregirlo. |

*Regla de clasificación (decisión de diseño):* si el texto, sin el prefijo `N*`, es solo dígitos,
se trata como código. Si se resuelve, se agrega; si no, es un error de "código no encontrado".
Cualquier otra cosa se intenta resolver primero como código (los id de producto pueden llevar
letras o guion) y, si no existe, va a la búsqueda.

- Resolver el código usa el `resolverCodigo` actual (id o código de barras).
- **La validación del campo cambia**: hoy `codigoProdKeyTyped` solo acepta mayúsculas,
  dígitos y guion. Debe aceptar también `*`, minúsculas y espacios (para buscar por nombre).
  Se mantiene un límite de longitud razonable (hoy es 20; subirlo a ~40 para poder buscar texto).
- El campo **Cantidad** (`cantidadProd`) y el botón de registrar siguen existiendo para el
  flujo con mouse, pero ya no son obligatorios: si `cantidadProd` está vacío, se usa 1.
- El **descuento por producto** (`dctProd`) se sigue aplicando igual que hoy al agregar, con
  el mismo tope de `obtenerMaxDescuento`, y se regresa a 0 después.
- La **lista de precios activa** se aplica igual que hoy (`aplicarPrecioLista`).

**Búsqueda sin mouse** (usa la tabla de búsqueda que ya está en el panel: `Busc` + `tablaBusc`):

- `F2`, o un texto que no es código, pasa el foco a `Busc` con ese texto y filtra (la búsqueda
  actual ya es por nombre o código sin distinguir mayúsculas).
- En `Busc`, **Enter**: si el filtro deja **un solo** producto, lo agrega directo (con la cantidad
  pendiente o 1) y regresa a Código. Si hay varios, entra a `tablaBusc` con el primero
  seleccionado. Si no hay ninguno, muestra el aviso en rojo + sonido.
- Desde `Busc`, `↓` entra a `tablaBusc`. En la tabla, `↑/↓` mueven la selección,
  **Enter agrega** el producto seleccionado (con la cantidad pendiente o 1) y `Esc` regresa
  a Código sin agregar nada.
- La validación de `Busc` hoy rechaza algunos caracteres. Que acepte lo mismo que el
  campo de código, sin `*`.

**Editar el carrito sin mouse** (con el foco en Código y el campo **vacío**):

| Tecla | Acción |
|---|---|
| `↑` / `↓` | Mueve la selección en `tablaVentas`. Si no hay selección, `↑` selecciona el último renglón. |
| `+` / `-` | Suma o resta 1 al renglón seleccionado. Si llega a 0, lo quita. |
| `Supr` | Quita el renglón seleccionado, sin confirmar. |
| `F4` | Abre el diálogo de actualizar renglón que ya existe (cantidad y descuento) para el renglón seleccionado. Enter guarda y Esc cancela. |

Con texto en el campo, estas teclas se comportan como en un campo de texto normal. Así no
chocan con códigos que llevan guion.

### 1b. Errores al agregar: aviso en rojo + sonido (sin ventanas)

Con lector, una ventana `JOptionPane` es peligrosa: el Enter del siguiente escaneo la cierra
y ese producto se pierde sin que nadie lo note. Por eso, **en Ventas, al agregar productos
no se abre ninguna ventana de error**:

- Debajo del campo Código hay una **línea de aviso en rojo** (un `JLabel`) y suena un **sonido
  de error** (`Toolkit.getDefaultToolkit().beep()` como mínimo; se puede usar un sonido propio
  si se ve conveniente).
- El texto que causó el error se queda en Código, **seleccionado**: el siguiente escaneo o lo
  que escriban lo reemplaza.
- El aviso se borra con el siguiente producto agregado correctamente.
- Esto aplica a todos los errores al agregar desde el teclado, desde la búsqueda o con
  `+`/`-`/F4:
  - código no encontrado y cantidad inválida
  - sin existencia suficiente o producto dado de baja (hoy salen del trigger de
    `set_venta_temp` vía `GestorErrores.manejar`)
  - "el producto no tiene precio en la lista…" (`aplicarPrecioLista`)
  - descuento mayor al máximo (`obtenerMaxDescuento`): se aplica el tope y se avisa en la línea
    roja, sin ventana
- **Cambio necesario en la capa de datos:** `insertarVentaTemp` (y lo que lo rodea) hoy regresa
  `void` y muestra el error él mismo. Para Ventas debe poder **regresar el mensaje de error**
  (o lanzarlo) en lugar de mostrarlo, para que `VentasP` lo ponga en la línea roja. El mensaje que se
  muestra es el texto de negocio que ya limpia `GestorErrores` (sin el texto técnico de
  PostgreSQL). Otros módulos que usan estos métodos deben seguir funcionando igual, ya sea
  con una variante nueva del método o con un parámetro.
- Si `aplicarPrecioLista` falla después de insertar, el producto queda en el carrito con el
  precio normal (como hoy) y solo se avisa.

**Barra de ayuda:** una línea fija en la parte de abajo de Ventas con los atajos:
`Enter Agregar · F2 Buscar · F4 Cantidad · Supr Quitar · +/- Cantidad · F12 COBRAR`.

### 2. Cobro (`CobroDialog`, nuevo)

Es un diálogo modal nuevo que reemplaza para el cobro el flujo
`ventaReali → regVentaDialog → panelCamb de ventaDialog`. El botón **Venta Hecha** también
abre este diálogo, así que con mouse o con teclado el cobro es el mismo.

Se abre con `F12`, con Enter en el campo de código vacío o con el botón. Si el carrito está
vacío, no se abre (aviso corto).

```
 ┌──────────────── Cobrar ────────────────┐
 │  Total:               $74.00           │
 │  Recibido:       [ 74.00        ]      │  ← foco aquí, texto seleccionado
 │  CAMBIO:               $0.00           │  ← grande
 │  [F1 Efectivo] F2 Transf  F3 Tarjeta  F4 Otro │
 │  Cliente: Público general   (F5 cambiar)      │
 │       Enter = Cobrar      Esc = Volver        │
 └────────────────────────────────────────┘
```

- Al abrir: el cliente es **Público general**, la forma de pago es **Efectivo** y "Recibido"
  trae el **total ya escrito y seleccionado**. Así, escribir un monto lo reemplaza y Enter
  cobra con pago justo.
- El cambio se recalcula al teclear (igual que `recCTKeyReleased`).
- `F1`–`F4` eligen Efectivo / Transferencia / Tarjeta / Otro, los mismos valores que
  `cmbFormaPago`. Si no es Efectivo, "Recibido" se fija al total y se deshabilita, y el cambio queda en 0.
- **Enter = cobrar.** Las validaciones son las de hoy: lo recibido no puede ser menor al
  total. Si falla, el mensaje aparece en el propio diálogo (no con `JOptionPane`), para no
  romper el ritmo de quien cobra.
- `F5` abre la **selección de cliente** (ver abajo). Al volver, la etiqueta muestra al cliente elegido.
- `Esc` cierra sin cobrar. El carrito queda intacto.
- Al cobrar se ejecuta exactamente la lógica actual de `hechoCambActionPerformed`: se guardan
  los descuentos del carrito, `registrarVentaConFormaPago`, `actualizarDescuentoVenta`,
  `limpiarVentaTemp`, se limpia la tabla y el total, se pone `dctGlobal` en 0 y se arma
  el `TicketData`. Esa lógica se **mueve a un método** de `VentasP` (o del diálogo) para que la
  llamen tanto el botón viejo como el nuevo flujo. Si `registrarVentaConFormaPago` falla, el
  carrito se conserva (como hoy).

**Selección de cliente (F5):** un diálogo pequeño con un campo de búsqueda y una tabla.
Usa `buscarClientesActivos(filtro)`, que ya busca por CURP **o nombre** sin distinguir
mayúsculas. Al teclear se filtra, `↓` entra a la tabla, `↑/↓` mueven, Enter elige y Esc
vuelve sin cambiar. El botón para registrar un cliente nuevo (`ClientesP.Registro`) sigue
disponible.

**Ticket:** después de cobrar se abre el `TicketDialog` actual con teclas:
**Enter = Imprimir**, **Esc = Cerrar**. Al cerrarse, el foco regresa a Código.
(Se respeta lo que ya hace al cerrar: si hay impresora y no se imprimió, imprime.)
`TicketDialog` también se abre desde `ApartadosP`, y ahí **no** debe cambiar. Las teclas solo se
activan cuando lo abre Ventas (p. ej. `VentasP` las registra en el `JRootPane` del diálogo antes
de mostrarlo, o con un parámetro del constructor). La misma protección de ~300 ms aplica al
ticket, para que el Enter de cobrar no imprima por accidente.

**Protección contra Enter de más (anti-meta 1):** el cobro no se confirma con un Enter que
venga de la tecla mantenida o repetida al abrir el diálogo. El diálogo ignora Enter durante
los primeros ~300 ms después de abrirse. Así, para cobrar con solo Enter hacen falta
dos pulsaciones deliberadas (abrir y confirmar), y una ráfaga o una tecla pegada no
cobra sola. *(Decisión de diseño; el valor exacto se puede ajustar.)*

### 3. Aviso de poco stock

- Se **quita** de Ventas: después de cobrar ya no se abre `prodPocosDialog`. El diálogo y
  `mostrarProdPocos` se pueden eliminar de `VentasP`.
- **Nuevo lugar** (el usuario pidió moverlo a otro lado; la ubicación es decisión de diseño):
  - El botón **Inventario** del menú de `Interfaz` muestra el conteo, p. ej.
    `Inventario (⚠ 3)`, cuando hay productos con poco stock. Se recalcula **al iniciar sesión,
    cada vez que se cambia de módulo** (en el cambio de panel de `Interfaz`) y **después de
    cada venta**. Así cubre cualquier módulo que mueva existencia (Apartados, Devoluciones,
    Compras, inventario web) sin que cada uno tenga que avisar.
  - En `InventarioP`, un filtro o casilla **"Solo poco stock"** muestra únicamente esos
    productos (usa `obtenerProductosPocos`).
- El criterio de "poco stock" no cambia: activos con `cantidad <= 12`, el de hoy.
  El conteo necesita una consulta de conteo (o contar lo que regresa `obtenerProductosPocos`).

### 4. Cómo se conectan las teclas (enfoque A)

- Los atajos de la pantalla se registran en el constructor de `VentasP`, **fuera de
  `initComponents`**, con `getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)` / `getActionMap()`.
  Así solo se disparan cuando el foco está dentro del panel de Ventas.
- Las teclas que dependen del campo (`↑/↓/+/-/Supr/Enter` en Código) se registran en el
  `InputMap(WHEN_FOCUSED)` de `codigoProd`, y verifican si el campo está vacío.
- Cada diálogo (`CobroDialog`, selección de cliente, `TicketDialog`) registra sus propias
  teclas en su `JRootPane`. Las teclas `F1`–`F5` del cobro no chocan con las de la pantalla
  porque el diálogo es modal.
- Se evita `F10`: en algunos look-and-feel activa la barra de menú.
- La interpretación del texto del campo de código (`N*código`, cantidad inválida, vacío, texto)
  vive en la clase pura `CapturaVenta`, sin Swing, con pruebas propias.

## Restricciones

- Proyecto NetBeans/Ant. `initComponents` y los `.form` no se editan a mano. Los
  componentes nuevos se crean en código, como ya se hace con `dctProd`, `dctGlobal` y
  `cmbListaPrecios` en el constructor.
- Los textos de la interfaz y los nombres de código van en español.
- No se necesitan migraciones de base de datos (la acumulación y la búsqueda ya existen).
- El usuario compila y prueba él mismo la aplicación.

## Lo que queda a criterio de quien implemente

- Nombre exacto de las clases nuevas (`CapturaVenta`, `CobroDialog`, `SeleccionClienteDialog`)
  y si la lógica de registrar la venta vive en `VentasP` o en el diálogo.
- Diseño visual del `CobroDialog`, con fuentes y colores coherentes con el resto
  (Noto Serif y la paleta actual) y el cambio en grande.
- Forma de la barra de ayuda.
- Cómo se avisa a `Interfaz` para refrescar el conteo después de una venta (llamada
  directa, listener, etc.).
- Forma exacta de que la capa de datos regrese el error (valor de retorno, excepción o
  variante del método).
- Qué hacer con `regVentaDialog` y el `panelCamb` de `ventaDialog`, que quedan sin uso:
  eliminarlos o dejarlos ocultos.
- Pruebas: las de `CapturaVenta` siguiendo las convenciones de `test/`. El flujo de teclado
  se verifica a mano.
