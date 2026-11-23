# Actualización automática desde el punto de venta — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Que el desarrollador publique una versión con un comando y que cada punto de venta la detecte, avise al gerente y se actualice con un clic.

**Architecture:** La versión vive en `src/version.properties`. `Actualizador` consulta `releases/latest` de GitHub, descarga y verifica el ZIP de parche existente y lanza `aplicar_actualizacion.bat --auto`, que respalda, migra, reemplaza archivos y reabre la app. `publicar_version.ps1` compila, empaqueta, calcula SHA-256 y crea el release con `gh`.

**Tech Stack:** Java 23 (Swing, `java.net.http.HttpClient`, `java.util.zip`, `java.security.MessageDigest`), Windows batch, PowerShell, GitHub CLI.

**Spec:** `docs/superpowers/specs/2026-11-22-auto-actualizacion-design.md`

## Global Constraints

- Repo de releases: `DannG04/Punto-de-venta`; endpoint `https://api.github.com/repos/DannG04/Punto-de-venta/releases/latest`.
- Encabezado HTTP `User-Agent: PuntoVenta-Updater`; timeout 10 s.
- Assets: `PuntoVenta-Update-<v>.zip` y `PuntoVenta-Update-<v>.zip.sha256` (solo hex en minúsculas). Tag `v<v>`.
- Revisión al iniciar sesión un gerente y cada 6 h; nunca en el hilo de Swing; sin red = silencio (solo `GestorErrores.registrar`, nunca `manejar`).
- Sin dependencias nuevas en la app (solo JDK).
- Aviso y sección solo para gerentes (`Interfaz.admActivo`). Sección = `visibilidad(3, 8)`.
- No actualizar si `venta_temp` tiene renglones.
- `aplicar_actualizacion.bat` sin `--auto` se comporta exactamente como hoy.
- Relanzar la app sin elevación con `explorer.exe "%INSTALL_DIR%\PuntoVenta.exe"`.
- Textos visibles en español; código y nombres en español (convención del proyecto).
- El desarrollador compila él mismo: no ejecutar `ant compile`/`ant run` salvo dentro de `publicar_version.ps1`.

**Cómo correr pruebas** (no hay JUnit en el proyecto; las pruebas son clases con `main` que terminan con código 1 si algo falla):

```bash
JDK="$(ls -d ~/.vscode/extensions/redhat.java-*/jre/*/bin | tail -1)"   # o $JAVA_HOME/bin
"$JDK/javac" --release 23 -encoding UTF-8 -nowarn -cp "lib/*" -d build/pruebas $(find src test -name "*.java") && cp src/version.properties build/pruebas/
"$JDK/java" -cp "build/pruebas;lib/*" <ClasePrueba>
```

## Review Focus

- Tags con y sin `v`, y `1.9` vs `1.10` vs `1.100`: la comparación debe ser numérica por partes (Task 1).
- Release sin el ZIP o sin el `.sha256`, JSON inesperado o HTTP 403 por límite de peticiones: `buscar()` devuelve `null`, nunca excepción (Task 2).
- Descarga interrumpida o hash distinto: no se lanza el `.bat` y se borra la carpeta temporal (Task 2).
- Carpeta de instalación con espacios y paréntesis (`C:\Program Files (x86)\...`): el `.bat` recibe la ruta intacta en modo `--auto` incluso tras autoelevarse (Task 3).
- Sin internet al iniciar sesión: el login no se retrasa ni aparece ningún diálogo (Task 4).

---

### Task 1: Versión de la app

**Files:**
- Create: `src/version.properties` (`app.version=1.22`)
- Create: `src/puntoventa/util/VersionApp.java`
- Test: `test/puntoventa/util/VersionAppPrueba.java`

**Interfaces:**
- Produces: `VersionApp.actual(): String` (lee `/version.properties` del classpath; si falta o falla → `"0"`); `VersionApp.esMasNueva(String remota, String local): boolean`; `VersionApp.normalizar(String v): String` (quita `v`/`V` inicial y espacios).

- [ ] **Step 1: Escribir la prueba**

```java
check(VersionApp.esMasNueva("1.23", "1.22"));
check(VersionApp.esMasNueva("v1.10", "1.9"));
check(VersionApp.esMasNueva("1.100", "1.22"));
check(VersionApp.esMasNueva("2.0", "1.99"));
check(VersionApp.esMasNueva("1.22.1", "1.22"));
check(!VersionApp.esMasNueva("1.22", "1.22"));
check(!VersionApp.esMasNueva("v1.22", "1.22"));
check(!VersionApp.esMasNueva("1.3", "1.22"));
check(!VersionApp.esMasNueva("basura", "1.22"));   // no numérica → false
check(!VersionApp.esMasNueva(null, "1.22"));
check(VersionApp.actual().equals("1.22"));          // con version.properties en el classpath
```

- [ ] **Step 2: Correr y ver que falla** — Expected: error de compilación `cannot find symbol VersionApp`.
- [ ] **Step 3: Implementar `VersionApp`** — partes separadas por `.`; parte faltante = 0; cualquier parte no numérica en la remota → `false`.
- [ ] **Step 4: Correr y ver que pasa** — `java ... puntoventa.util.VersionAppPrueba` → `OK` y código 0.
- [ ] **Step 5: Commit** — `feat(actualizacion): numero de version de la app`

---

### Task 2: Actualizador (consulta, descarga, verificación, lanzamiento)

**Files:**
- Create: `src/puntoventa/util/Actualizador.java`
- Test: `test/puntoventa/util/ActualizadorPrueba.java`, `test/recursos/release_ok.json`, `test/recursos/release_sin_sha.json`

**Interfaces:**
- Consumes: `VersionApp.normalizar`.
- Produces:
  - `record Actualizador.Release(String version, String notas, java.time.LocalDate publicada, String urlZip, String urlSha)`
  - `static Release Actualizador.parsear(String json)` — `null` si falta `tag_name`, el asset `.zip` o el `.zip.sha256`. `version` normalizada; `notas` = `body` (con `\n`, `\"`, `\\`, `\uXXXX` decodificados); `publicada` = fecha de `published_at`.
  - `static Release Actualizador.buscar()` — GET al endpoint; cualquier fallo (red, timeout, HTTP ≠ 200, parseo) → `null` y `GestorErrores.registrar`.
  - `static String Actualizador.sha256(java.nio.file.Path archivo)` — hex minúsculas.
  - `static java.nio.file.Path Actualizador.descargar(Release r, java.util.function.DoubleConsumer progreso)` — baja ZIP y `.sha256` a `%TEMP%\PuntoVenta-Update-<v>\`, compara (ignora espacios/nombre de archivo tras el hash), descomprime ahí y devuelve esa carpeta. Hash distinto o error → borra la carpeta y lanza `java.io.IOException` con mensaje en español.
  - `static void Actualizador.lanzarYSalir(java.nio.file.Path carpeta, java.nio.file.Path instalacion)` — `ProcessBuilder("cmd", "/c", "<carpeta>\\aplicar_actualizacion.bat", "--auto", "<instalacion>")` y `System.exit(0)`.
  - `static boolean Actualizador.hayVentaEnCurso()` — `true` si `venta_temp` tiene renglones (método nuevo `ConexionBD.ventaEnCurso()` → `VentaDAO`: `SELECT EXISTS(SELECT 1 FROM venta_temp)`).

- [ ] **Step 1: Escribir las pruebas** (JSON de ejemplo copiado de un release real de GitHub, recortado: `tag_name`, `body`, `published_at`, `assets[].name`, `assets[].browser_download_url`)

```java
Release r = Actualizador.parsear(leer("test/recursos/release_ok.json"));
check(r.version().equals("1.23"));
check(r.notas().contains("Corrige") && r.notas().contains("\n"));
check(r.publicada().equals(LocalDate.of(2026, 11, 25)));
check(r.urlZip().endsWith("PuntoVenta-Update-1.23.zip"));
check(r.urlSha().endsWith("PuntoVenta-Update-1.23.zip.sha256"));
check(Actualizador.parsear(leer("test/recursos/release_sin_sha.json")) == null);
check(Actualizador.parsear("{\"message\":\"API rate limit exceeded\"}") == null);
check(Actualizador.parsear("no es json") == null);
// sha256 de un archivo con contenido "abc"
check(Actualizador.sha256(tmp("abc")).equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));
```

Prueba de descarga sin red real: levantar `com.sun.net.httpserver.HttpServer` en `localhost` sirviendo un ZIP con `aplicar_actualizacion.bat` y su `.sha256`; `descargar` debe devolver una carpeta que contiene el `.bat`. Con un `.sha256` incorrecto debe lanzar `IOException` y la carpeta no debe existir.

- [ ] **Step 2: Correr y ver que falla** — `cannot find symbol Actualizador`.
- [ ] **Step 3: Implementar `Actualizador`** — parseo con regex sobre campos concretos (sin librería JSON); para los assets, buscar cada `"browser_download_url":"..."` y elegir por sufijo `.zip` / `.zip.sha256`. `HttpClient` con `followRedirects(NORMAL)` (los assets redirigen a otro host).
- [ ] **Step 4: Correr y ver que pasa** — `ActualizadorPrueba` → `OK`.
- [ ] **Step 5: Commit** — `feat(actualizacion): consulta y descarga verificada de releases`

---

### Task 3: Modo `--auto` en `aplicar_actualizacion.bat`

**Files:**
- Modify: `installer/update/aplicar_actualizacion.bat`
- Test: `test/actualizacion/probar_bat_auto.ps1`

**Interfaces:**
- Consumes: argumentos `--auto "<instalacion>"` (de Task 2).
- Produces: al terminar escribe `%INSTALL_DIR%\ultima_actualizacion.txt` con una línea `OK <version>` o `ERROR <paso>`; `<version>` sale de `version.properties` dentro del ZIP (Task 5 lo incluye en la raíz del ZIP).

- [ ] **Step 1: Escribir la prueba** — script que arma una carpeta de instalación simulada (`db.properties` hacia un Postgres de prueba en Docker, `Proy_Ventas.jar` falso, `PuntoVenta.exe` falso) dentro de una ruta con espacios y paréntesis, y un paquete con una migración trivial. Ejecuta `aplicar_actualizacion.bat --elevated --auto "<instalacion>"` (el `--elevated` evita UAC en la prueba) y verifica: código de salida 0, sin esperar tecla (timeout de 120 s), `ultima_actualizacion.txt` = `OK 1.23`, JAR reemplazado y `.bak` creado. Segundo caso: migración que falla → `ERROR migracion V<x>`, JAR **no** reemplazado.
- [ ] **Step 2: Correr y ver que falla** — se queda esperando en `pause` (timeout).
- [ ] **Step 3: Implementar** — detectar `--auto` en cualquier posición junto a `--elevated`; propagarlo en la relanzada elevada usando `cmd /c` en lugar de `cmd /k`; reemplazar cada `pause` por una etiqueta `:salir` que en modo auto escribe `ultima_actualizacion.txt`, ejecuta `explorer.exe "%INSTALL_DIR%\PuntoVenta.exe"` y sale; antes de `taskkill`, esperar hasta 10 s a que no exista `PuntoVenta.exe`/`javaw.exe` con ruta en `%INSTALL_DIR%`.
- [ ] **Step 4: Correr y ver que pasa** — ambos casos verdes; correr también sin `--auto` y comprobar que sigue pidiendo tecla al final (comportamiento intacto).
- [ ] **Step 5: Commit** — `feat(actualizacion): modo --auto en aplicar_actualizacion.bat`

---

### Task 4: Sección “Actualizaciones” y aviso en la app

**Files:**
- Create: `src/puntoventa/ui/ActualizacionesP.java`
- Modify: `src/puntoventa/ui/Interfaz.java` (menú Administración, `panelesAdm` caso 8, `iniciarSesion`, constructor, `horaPanel`)
- Modify: `src/puntoventa/db/VentaDAO.java`, `src/puntoventa/db/ConexionBD.java` (`ventaEnCurso()`)
- Test: `test/puntoventa/ui/ActualizacionesPrueba.java`

**Interfaces:**
- Consumes: `VersionApp.actual/esMasNueva`, `Actualizador.buscar/descargar/lanzarYSalir/hayVentaEnCurso`, `Actualizador.Release`.
- Produces: `ActualizacionesP.mostrar(Release r)` (null = sin versión nueva o sin consulta); `Interfaz.avisarActualizacion(Release r)`.

Comportamiento (textos exactos):
- Encabezado: `Versión instalada: <actual>` / `Versión disponible: <v> (publicada dd/MM)` / área no editable con las novedades.
- Sin novedad: `Tienes la versión más reciente` y botón **Actualizar ahora** deshabilitado. Sin red: `No se pudo consultar. Revisa la conexión a internet.`
- **Buscar ahora**: consulta en `SwingWorker`, botón deshabilitado mientras tanto.
- **Actualizar ahora**: si `hayVentaEnCurso()` → `Hay una venta en curso; termínela o vacíe el carrito.`; si no, confirmación `La app se cerrará y se actualizará. Tardará unos minutos.` → diálogo modal con `JProgressBar` y **Cancelar** → `lanzarYSalir(carpeta, Paths.get(System.getProperty("user.dir")))`. Error → mensaje con el texto de la `IOException`.
- Aviso: ítem de menú `Actualizaciones` → `Actualizaciones (<v> disponible)`, menú `Administración` → `Administración •`; etiqueta clicable en `horaPanel`: `Hay una actualización disponible (<v>)` que abre la sección. Solo si `admActivo`.
- Revisión: al final de `iniciarSesion()` si `admActivo`, y `javax.swing.Timer` de 6 h (21 600 000 ms) creado una sola vez.
- Al construir `Interfaz`: si existe `ultima_actualizacion.txt` en `user.dir` → `OK <v>`: `Actualizado a la versión <v>`; `ERROR ...`: `La actualización no se completó; la versión anterior sigue funcionando. Detalle en actualizacion.log`. Luego borrarlo.

- [ ] **Step 1: Escribir la prueba** — con `PruebaUI` como modelo (ver memoria del entorno de pruebas): instanciar `ActualizacionesP`, llamar `mostrar(null)` y comprobar el texto `Tienes la versión más reciente` y el botón deshabilitado; `mostrar(new Release("9.0", "• Nuevo", LocalDate.of(2026,11,25), "u", "s"))` → etiqueta contiene `9.0` y `25/11`, botón habilitado; crear `ultima_actualizacion.txt` con `OK 1.23` en un `user.dir` temporal, construir `Interfaz` y verificar que se mostró `Actualizado a la versión 1.23` (capturando diálogos como en `PruebaUI`) y que el archivo ya no existe.
- [ ] **Step 2: Correr y ver que falla** — `cannot find symbol ActualizacionesP`.
- [ ] **Step 3: Implementar** — panel con `BorderLayout` siguiendo el estilo visual de `EmpresaP` (fuente Noto Serif, botones con `SvgIcon`); reutilizar el ícono `/icons/servidor_movil.svg` o agregar `actualizar.svg`.
- [ ] **Step 4: Correr y ver que pasa**; además correr `PruebaUI` y confirmar 0 errores y que el panel nuevo no se corta a 1024×768.
- [ ] **Step 5: Commit** — `feat(actualizacion): seccion Actualizaciones y aviso para gerentes`

---

### Task 5: Publicar una versión

**Files:**
- Create: `installer/update/publicar_version.ps1`
- Create: `installer/update/PuntoVenta.exe` (copia del lanzador actual, `git add -f` si hace falta)
- Modify: `installer/update/empaquetar_update.ps1` (incluir `src/version.properties` en la raíz del ZIP; tomar `PuntoVenta.exe` de `installer/update/` si falta en `dist/`)
- Modify: `installer/update/README.md` (sección “Publicar una versión”)

**Interfaces:**
- Parámetros: `-Version <x.y>` (obligatorio), `-Notas <texto>` o `-NotasArchivo <ruta>`, `-Jdk <ruta bin>` (opcional), `-SinPublicar` (hace todo menos commit/tag/push/release; para probar).
- Produce en `installer/output/`: `PuntoVenta-Update-<v>.zip` y `PuntoVenta-Update-<v>.zip.sha256`.

Orden: validar `-Version` > `VersionApp` actual (leer `src/version.properties`) y árbol git limpio → escribir `app.version` y `#define MyAppVersion` en `installer/setup.iss` → compilar (buscar `ant` en PATH; luego `ant.bat` en `C:\Program Files\NetBeans*\netbeans\extide\ant\bin` y `C:\Program Files\NetBeans*\extide\ant\bin`; si no hay, `javac`/`jar` del JDK con manifiesto `Main-Class: puntoventa.ui.Interfaz` + `Class-Path` de `lib/*.jar`, recursos no-Java de `src/`, `lib` → `dist/lib`) → `empaquetar_update.ps1 -Version` → SHA-256 (`Get-FileHash`, minúsculas) → si no `-SinPublicar`: verificar `gh auth status`, commit `chore(release): v<v>`, `git tag v<v>`, push rama y tag, `gh release create v<v> <zip> <sha> --title "Versión <v>" --notes(-file)`.

- [ ] **Step 1: Prueba** — en un clon temporal del repo, `publicar_version.ps1 -Version 9.9 -Notas "prueba" -SinPublicar`: existen el ZIP y el `.sha256`; el hash coincide con `Get-FileHash`; el ZIP contiene `Proy_Ventas.jar`, `PuntoVenta.exe`, `lib/`, `migrations/`, `aplicar_actualizacion.bat`, `version.properties` con `app.version=9.9`; `setup.iss` dice `1.22`→`9.9`. Con `-Version 1.0` debe fallar con “La versión debe ser mayor que 1.22”.
- [ ] **Step 2: Correr y ver que falla** — el script no existe.
- [ ] **Step 3: Implementar** el script y los cambios a `empaquetar_update.ps1`.
- [ ] **Step 4: Correr y ver que pasa**; correr `ActualizadorPrueba` contra el ZIP generado servido por el `HttpServer` local (hash y descompresión con un paquete real).
- [ ] **Step 5: Commit** — `feat(actualizacion): script para publicar versiones en GitHub Releases`

---

### Task 6: Prueba de punta a punta

- [ ] **Step 1:** Con `-SinPublicar` generar la versión 9.9; servir `release_ok.json` apuntando a los assets locales mediante el `HttpServer` de prueba y una propiedad de sistema `-Dpdv.releases.url=` (agregar en `Actualizador.buscar()`: si la propiedad existe se usa en vez del endpoint de GitHub; solo para pruebas).
- [ ] **Step 2:** Instalación simulada (como en la auditoría: carpeta con `jre`, `dist`, `db.properties` → Postgres en Docker). Abrir la app, iniciar sesión como admin, confirmar aviso en menú y barra, abrir **Actualizaciones**, **Actualizar ahora**, aceptar UAC.
- [ ] **Step 3:** Verificar: la app se cerró, el `.bat` corrió sin pedir tecla, `Proy_Ventas.jar.<fecha>.bak` existe, la app se reabrió y mostró `Actualizado a la versión 9.9`, `VersionApp.actual()` = `9.9` en la sección.
- [ ] **Step 4:** Repetir con un carrito abierto → debe bloquear. Repetir sin red → la sección dice `No se pudo consultar...` y el login no tarda.
- [ ] **Step 5: Commit** de cualquier ajuste — `test(actualizacion): prueba de punta a punta`
