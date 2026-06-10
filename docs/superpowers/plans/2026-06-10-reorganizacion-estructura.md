# Reorganización de la estructura del proyecto — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ordenar la raíz del repo (JARs a `lib/`, basura, reportes generados) y sacar `src/` del paquete por defecto hacia paquetes por capas (`puntoventa.ui|db|report|web|util`), sin romper la app.

**Architecture:** Proyecto NetBeans/Ant (Matisse). Los recursos se cargan por classpath absoluto (`/icons/...`), así que `icons/` e `img/` se quedan en la raíz del classpath y el cambio de paquetes no los afecta. Ningún `.form` referencia clases del proyecto, por lo que mover los paneles solo requiere declarar `package` y resolver imports cruzados.

**Tech Stack:** Java (Swing/Matisse), Ant, PostgreSQL JDBC, Apache POI, ZXing, FlatLaf/JTattoo, NanoHTTPD.

**Nota sobre verificación:** El repo no tiene suite de tests. La verificación de cada fase es **compilación + arranque manual**. El usuario compila (no se ejecuta `ant` desde el agente). En la Fase 2 **no se compila entre sub-tareas**: los estados intermedios no compilan a propósito (mover `ConexionBD` rompe 22 referencias hasta que se añaden los imports). Se compila **una sola vez al final de la Fase 2**.

---

## Fase 1 — Limpieza segura

Spec ref: Fase 1 (pasos 1–4). Va en su propio commit, separado de la Fase 2.

### Task 1.1: Mover los JARs a `lib/`

**Files:**
- Create: `lib/` (14 `.jar`)
- Modify: `nbproject/project.properties:38-51` (las líneas `file.reference.*.jar`)

- [ ] **Step 1: Crear `lib/` y mover los 14 JARs con git**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta"
mkdir -p lib
git mv commons-collections4-4.1.jar core-3.5.3.jar flatlaf-3.7.jar javase-3.5.3.jar \
  JTattoo-1.6.13.jar nanohttpd-2.3.1.jar nanohttpd-websocket-2.3.1.jar poi-3.16.jar \
  poi-ooxml-3.16.jar poi-ooxml-schemas-3.16.jar postgresql-42.7.4.jar svg-salamander-1.0.jar \
  Util.jar xmlbeans-2.6.0.jar lib/
```

Nota: si algún `.jar` está untracked, `git mv` fallará para ese archivo; muévelo con `mv <jar> lib/` y luego `git add lib/<jar>`.

- [ ] **Step 2: Actualizar las referencias en `project.properties`**

Editar `nbproject/project.properties` líneas 38–51: anteponer `lib/` al valor de cada referencia. El bloque `javac.classpath` (líneas 54–69) usa las variables `${file.reference.*}`, así que **no se toca**. Resultado:

```properties
file.reference.commons-collections4-4.1.jar=lib/commons-collections4-4.1.jar
file.reference.core-3.5.3.jar=lib/core-3.5.3.jar
file.reference.flatlaf-3.7.jar=lib/flatlaf-3.7.jar
file.reference.javase-3.5.3.jar=lib/javase-3.5.3.jar
file.reference.JTattoo-1.6.13.jar=lib/JTattoo-1.6.13.jar
file.reference.poi-3.16.jar=lib/poi-3.16.jar
file.reference.poi-ooxml-3.16.jar=lib/poi-ooxml-3.16.jar
file.reference.poi-ooxml-schemas-3.16.jar=lib/poi-ooxml-schemas-3.16.jar
file.reference.postgresql-42.7.4.jar=lib/postgresql-42.7.4.jar
file.reference.svg-salamander-1.0.jar=lib/svg-salamander-1.0.jar
file.reference.Util.jar=lib/Util.jar
file.reference.xmlbeans-2.6.0.jar=lib/xmlbeans-2.6.0.jar
file.reference.nanohttpd-2.3.1.jar=lib/nanohttpd-2.3.1.jar
file.reference.nanohttpd-websocket-2.3.1.jar=lib/nanohttpd-websocket-2.3.1.jar
```

- [ ] **Step 3 (CHECKPOINT — usuario): compilar**

El usuario corre `ant clean compile`.
Esperado: BUILD SUCCESSFUL (los JARs se resuelven desde `lib/`).

### Task 1.2: Borrar basura y ajustar `.gitignore`

**Files:**
- Delete: `nul`
- Modify: `.gitignore`

- [ ] **Step 1: Borrar el archivo `nul` (basura de Windows)**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta"
git rm --cached nul 2>/dev/null; rm -f nul
```

Nota: `Back1` y `backup_1.0.tar` ya están marcados como borrados en git; confirma su borrado con `git rm Back1 backup_1.0.tar` si siguen apareciendo en `git status`.

- [ ] **Step 2: Ignorar artefactos runtime untracked (`logo.png`, `impresion*.txt` de la raíz)**

`logo.png` (lo escribe `EmpresaP.java:232` con `new File("logo.png")`) e `impresionVenta.txt` son datos de tiempo de ejecución, no fuente. **No se borran del disco**; se ignoran. Añadir al final de `.gitignore`:

```gitignore
# Artefactos de tiempo de ejecución (logo de empresa y plantillas generadas)
/logo.png
/impresionVenta.txt
```

Nota: `quagga.min.js` lo sirve `WebInventario` en runtime y `impresion.txt`, `impresionApartado.txt`, `impresionPagado.txt` son plantillas que lee `GenTicket` — **se conservan tal cual**.

### Task 1.3: Dejar de trackear reportes generados

**Files:**
- Untrack: `BalancesGenerales/`, `Estados de resultados/`, `Reportes de ventas/`

- [ ] **Step 1: Quitar del índice las carpetas ya ignoradas**

Están en `.gitignore` pero siguen versionadas. Quitarlas del índice sin borrarlas del disco:

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta"
git rm -r --cached BalancesGenerales "Estados de resultados" "Reportes de ventas"
```

- [ ] **Step 2: Verificar que quedaron ignoradas**

```bash
git status --short
```
Esperado: las carpetas ya no aparecen como tracked; `git check-ignore BalancesGenerales` las reporta ignoradas.

### Task 1.4: Commit de la Fase 1

- [ ] **Step 1: Commit**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta"
git add -A
git commit -m "chore: limpieza de raíz (jars a lib/, basura, reportes generados sin trackear)"
```

---

## Fase 2 — Paquetes por capas

Spec ref: Fase 2 (pasos 5–8). Va en su propio commit. **No compilar entre sub-tareas.**

Cada archivo `.java` lleva `package puntoventa.<capa>;` como **primera línea** (antes de los imports actuales). Los archivos con `.form` se mueven **junto con su `.form`**. Los imports cruzados por archivo se listan abajo (derivados del mapa real de referencias).

### Task 2.1: Crear carpetas y mover archivos con git

**Files:**
- Create: `src/puntoventa/{ui,db,report,web,util}/`

- [ ] **Step 1: Crear carpetas de paquete**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta/src"
mkdir -p puntoventa/ui puntoventa/db puntoventa/report puntoventa/web puntoventa/util
```

- [ ] **Step 2: Mover los archivos `db`, `web`, `util`, `report`**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta/src"
# db
git mv ConexionBD.java puntoventa/db/
# web
git mv WebInventario.java puntoventa/web/
# util
git mv SvgIcon.java Hora.java ConfigApp.java GestorErrores.java Cake.java Mise.java puntoventa/util/
# report
git mv Excel.java GenTicket.java GeneradorCodigoBarras.java TicketBuilder.java TicketData.java BalGeneral.java puntoventa/report/
```

- [ ] **Step 3: Mover los archivos `ui` (con sus `.form`)**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta/src"
# Paneles/diálogos con .form (mover .java y .form)
git mv AdministracionP.java AdministracionP.form puntoventa/ui/
git mv ApartadosP.java ApartadosP.form puntoventa/ui/
git mv CategoriasP.java CategoriasP.form puntoventa/ui/
git mv ClientesP.java ClientesP.form puntoventa/ui/
git mv ComprasP.java ComprasP.form puntoventa/ui/
git mv DevolucionesP.java DevolucionesP.form puntoventa/ui/
git mv EmpleadosP.java EmpleadosP.form puntoventa/ui/
git mv GananciasP.java GananciasP.form puntoventa/ui/
git mv GastosP.java GastosP.form puntoventa/ui/
git mv Interfaz.java Interfaz.form puntoventa/ui/
git mv InventarioP.java InventarioP.form puntoventa/ui/
git mv KardexP.java KardexP.form puntoventa/ui/
git mv ProveedoresP.java ProveedoresP.form puntoventa/ui/
git mv TicketDialog.java TicketDialog.form puntoventa/ui/
git mv VentasP.java VentasP.form puntoventa/ui/
# Paneles sin .form
git mv CotizacionesP.java EmpresaP.java CalendarioPanel.java puntoventa/ui/
```

- [ ] **Step 4: Verificar que `icons/` e `img/` NO se movieron**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta/src"
ls icons img
```
Esperado: ambas carpetas siguen en `src/` (raíz del classpath). No tocar.

### Task 2.2: Declarar `package` + imports en la capa `db`

**Files:**
- Modify: `src/puntoventa/db/ConexionBD.java`

- [ ] **Step 1: Insertar package + imports al inicio del archivo**

`ConexionBD` referencia `GestorErrores`, `Mise` (util) y `TicketData` (report). Insertar como **primeras líneas** del archivo (antes de los `import` existentes):

```java
package puntoventa.db;

import puntoventa.util.*;
import puntoventa.report.*;
```

### Task 2.3: Declarar `package` + imports en la capa `util`

**Files:**
- Modify: los 6 archivos en `src/puntoventa/util/`

- [ ] **Step 1: `ConfigApp.java`** — referencia `ConexionBD` (db). Primeras líneas:

```java
package puntoventa.util;

import puntoventa.db.*;
```

- [ ] **Step 2: `SvgIcon.java`, `Hora.java`, `GestorErrores.java`, `Cake.java`, `Mise.java`** — sin referencias cruzadas a otras capas. Primera línea de cada uno:

```java
package puntoventa.util;
```

### Task 2.4: Declarar `package` + imports en la capa `report`

**Files:**
- Modify: los 6 archivos en `src/puntoventa/report/`

- [ ] **Step 1: `Excel.java`** — referencia `ConexionBD` (db), `GestorErrores` (util). Primeras líneas:

```java
package puntoventa.report;

import puntoventa.db.*;
import puntoventa.util.*;
```

- [ ] **Step 2: `GenTicket.java`** — referencia `ConexionBD` (db), `GestorErrores`, `Hora`, `Mise` (util). Primeras líneas:

```java
package puntoventa.report;

import puntoventa.db.*;
import puntoventa.util.*;
```

- [ ] **Step 3: `BalGeneral.java`** — referencia `ConexionBD` (db) y `Excel` (misma capa). Primeras líneas:

```java
package puntoventa.report;

import puntoventa.db.*;
```

- [ ] **Step 4: `GeneradorCodigoBarras.java`, `TicketBuilder.java`, `TicketData.java`** — sin referencias cruzadas a otras capas. Primera línea de cada uno:

```java
package puntoventa.report;
```

### Task 2.5: Declarar `package` + imports en la capa `web`

**Files:**
- Modify: `src/puntoventa/web/WebInventario.java`

- [ ] **Step 1: Insertar package + import** — referencia `ConexionBD` (db). Primeras líneas:

```java
package puntoventa.web;

import puntoventa.db.*;
```

### Task 2.6: Declarar `package` + imports en la capa `ui`

**Files:**
- Modify: los 18 `.java` en `src/puntoventa/ui/`

- [ ] **Step 1: Insertar el mismo encabezado en los 18 archivos `ui`**

Para CADA uno de: `Interfaz`, `VentasP`, `InventarioP`, `ComprasP`, `ClientesP`, `ApartadosP`, `DevolucionesP`, `EmpleadosP`, `GastosP`, `GananciasP`, `AdministracionP`, `CategoriasP`, `KardexP`, `ProveedoresP`, `CotizacionesP`, `EmpresaP`, `TicketDialog`, `CalendarioPanel` — insertar como **primeras líneas** (antes de los imports existentes):

```java
package puntoventa.ui;

import puntoventa.db.*;
import puntoventa.report.*;
import puntoventa.util.*;
import puntoventa.web.*;
```

Las cuatro wildcard cubren cualquier clase no-UI usada. No hay choque de nombres con `java.util.*` (las clases de `puntoventa.util` son `SvgIcon`, `Hora`, `ConfigApp`, `GestorErrores`, `Cake`, `Mise`). Los paneles se referencian entre sí dentro de `puntoventa.ui`, por lo que no necesitan import adicional.

### Task 2.7: Actualizar la clase principal

**Files:**
- Modify: `nbproject/project.properties` (línea `main.class`)

- [ ] **Step 1: Cambiar `main.class`**

```properties
main.class=puntoventa.ui.Interfaz
```

### Task 2.8: CHECKPOINT — compilar y arrancar (usuario)

- [ ] **Step 1: Compilar limpio**

El usuario corre `ant clean compile`.
Esperado: BUILD SUCCESSFUL, cero errores de import. Si aparece "cannot find symbol", anotar la clase/archivo y añadir el import puntual que falte (caso no previsto por el mapa de referencias).

- [ ] **Step 2: Arrancar y verificar visualmente**

El usuario corre `ant run`.
Esperado: la app arranca, el login se ve con sus iconos, y al navegar los paneles muestran sus iconos (confirma que la carga por classpath `/icons/` sigue OK).

### Task 2.9: Commit de la Fase 2

- [ ] **Step 1: Commit**

```bash
cd "C:/Users/gonza/OneDrive/Escritorio/Punto-de-venta"
git add -A
git commit -m "refactor: organizar src/ en paquetes por capas (ui, db, report, web, util)"
```

---

## Verificación final (contra el spec)

- [ ] JARs en `lib/` y `project.properties` apuntando ahí (Fase 1.1).
- [ ] `nul`/`Back1`/`backup_1.0.tar` fuera; `logo.png` e `impresionVenta.txt` ignorados pero en disco (Fase 1.2).
- [ ] Reportes generados sin trackear (Fase 1.3).
- [ ] Todas las clases con `package puntoventa.<capa>;`; `icons/` e `img/` intactas en `src/` (Fase 2).
- [ ] `main.class=puntoventa.ui.Interfaz` (Fase 2.7).
- [ ] `ant clean compile` y `ant run` OK (Fase 2.8).
- [ ] Dos commits separados (uno por fase) para revertir de forma independiente.
