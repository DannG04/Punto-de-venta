# Actualización automática desde el punto de venta — diseño

Fecha: 2026-11-22 · Estado: aprobado en conversación, pendiente de revisión del documento

## Intención

Hoy cada corrección llega a las tiendas como un ZIP que el desarrollador lleva o
envía, y alguien ejecuta `aplicar_actualizacion.bat` a mano. Son pocas tiendas;
en cada una **el dueño usa la PC y es administrador de Windows**. Se quiere que
el desarrollador **publique una versión una sola vez** y que cada punto de venta
**se entere solo**, avise, y tenga una sección para **actualizar con un clic
cuando al dueño le convenga** (no a media venta).

## Metas

- Publicar una versión con un solo comando desde la PC del desarrollador.
- Que la app detecte versiones nuevas sin intervención y lo muestre de forma discreta.
- Que el dueño actualice desde la app con un clic, con respaldo previo y sin perder datos.
- Reutilizar el actualizador existente (`installer/update/aplicar_actualizacion.bat`).

## No metas (fuera de alcance)

- Actualizaciones obligatorias o silenciosas (sin que el dueño dé clic).
- Volver a una versión anterior desde la app (el respaldo de BD y del JAR ya existen para hacerlo a mano).
- Canales o versiones distintas por tienda.
- Firma de código del `.exe` / ZIP.

## Anti-metas (sería un fracaso aunque “funcione”)

- Que una actualización interrumpa una venta en curso o pierda el carrito.
- Que una descarga incompleta o un fallo de migración deje la app sin poder abrir.
- Que sin internet la app muestre errores o se vuelva lenta al iniciar.

## Restricciones y contexto

- App Java 23 Swing, instalada con Inno Setup en `C:\Program Files\PuntoVenta`
  (escribir ahí requiere elevación UAC; el dueño es admin).
- JRE propio en `{app}\jre`; lanzador Launch4j `PuntoVenta.exe`.
- Repositorio **público** en GitHub (`DannG04/Punto-de-venta`): los releases se
  descargan sin autenticación. Consecuencia aceptada: el ZIP es público.
- API de GitHub sin token: 60 peticiones/hora por IP (sobrado para revisar cada 6 h).
- El ZIP de parche ya existe (`empaquetar_update.ps1`): `Proy_Ventas.jar`,
  `PuntoVenta.exe`, `lib/`, `migrations/`, `_bootstrap_schema_version.sql`,
  `aplicar_actualizacion.bat`. El `.bat` ya: se autoeleva, respalda la BD
  (`pg_dump`), cierra la app, aplica migraciones pendientes vía `schema_version`,
  y reemplaza JAR/lib/exe con respaldo `.bak`. Si una migración falla, se detiene
  **sin** reemplazar el JAR.
- La app hoy **no conoce su propio número de versión**.
- El carrito de ventas vive en la tabla `venta_temp` (el stock se descuenta al agregar).

## Enfoques considerados

| | Enfoque | A favor | En contra |
|---|---|---|---|
| **A (elegido)** | **GitHub Releases + el `.bat` existente.** La app consulta `releases/latest`, descarga el ZIP y lanza el `.bat`. | Gratis, sin servidor, reutiliza el actualizador probado. | ZIP público; requiere GitHub CLI para publicar. |
| B | `version.json` + ZIP en Google Drive/Dropbox. | No depende de GitHub. | Enlaces directos inestables (confirmación en archivos grandes); publicación manual propensa a errores. |
| C | Actualizador 100 % Java (respaldo, migraciones, reemplazo). | Sin consola. | Duplica lógica que ya funciona; Java no puede reemplazar su JAR en ejecución, igual necesita proceso externo. |

Se eligió **A** en la conversación.

## Diseño

### 1. Versión de la app

- Recurso `src/version.properties` con `app.version=1.22` (va dentro del JAR).
- Clase `puntoventa.util.VersionApp`: `actual()` lee el recurso (si falta → `"0"`)
  y `esMasNueva(String remota, String local)` compara por partes numéricas
  (`1.3` < `1.22` < `1.100`; ignora una `v` inicial).
- `setup.iss` `MyAppVersion` debe coincidir; el script de publicar actualiza ambos.

### 2. Publicar (desarrollador)

Nuevo `installer/update/publicar_version.ps1 -Version 1.23 -Notas "..."` (o `-NotasArchivo`):

1. Valida que la versión sea mayor que la actual y que el árbol git esté limpio.
2. Escribe la versión en `src/version.properties` y en `setup.iss`; hace commit `chore(release): v1.23`.
3. Compila el JAR. Usa `ant jar` si `ant` está en el PATH; si no, compila directo
   con el JDK (`javac --release 23` + `jar`, manifiesto con `Main-Class:
   puntoventa.ui.Interfaz` y `Class-Path` de `lib/*.jar`, recursos no-Java de `src/`,
   y copia `lib/` a `dist/lib`). El JDK sale de `JAVA_HOME` o del parámetro `-Jdk`.
   *(Decisión del diseñador: en la PC de desarrollo actual no hay `ant` ni NetBeans.)*
   El lanzador `PuntoVenta.exe` no se regenera: se versiona una copia en
   `installer/update/PuntoVenta.exe` (64 KB, configuración Launch4j estable) y el
   script la copia a `dist/` si falta.
4. Llama a `empaquetar_update.ps1 -Version 1.23` → `installer/output/PuntoVenta-Update-1.23.zip`.
5. Calcula SHA-256 → `PuntoVenta-Update-1.23.zip.sha256` (solo el hash en hexadecimal).
6. `git tag v1.23`, push de commit y tag, y `gh release create v1.23 <zip> <sha256> --title "Versión 1.23" --notes ...`.

Requisito único: instalar GitHub CLI (`winget install GitHub.cli`) y `gh auth login`.

### 3. Detección (app)

- Clase `puntoventa.util.Actualizador` (sin UI):
  - `buscar()` → `GET https://api.github.com/repos/DannG04/Punto-de-venta/releases/latest`
    con `User-Agent: PuntoVenta-Updater`, timeout 10 s. Toma `tag_name`, `body`
    (novedades), `published_at`, y las URLs de los assets `*.zip` y `*.zip.sha256`.
    Devuelve `null` si no hay red, error HTTP, o faltan los assets.
  - Usa `java.net.http.HttpClient` (JDK); el JSON se lee con un parser mínimo
    propio o expresiones regulares sobre los campos necesarios (no agregar
    dependencias nuevas).
- Cuándo: en segundo plano (SwingWorker) **al iniciar sesión un gerente** y cada
  **6 horas** mientras la sesión siga abierta (`javax.swing.Timer`). Nunca bloquea la UI.
- Si `esMasNueva(remota, actual)`: se marca el aviso. Sin red / error: no se
  muestra nada (solo se registra en `logs/app.log` con `GestorErrores.registrar`, sin diálogo).

### 4. Aviso

- El menú **Administración** muestra `Administración •` y el ítem
  **Actualizaciones (1.23 disponible)**.
- La primera vez que se detecta una versión dada en la sesión: una notificación
  discreta no modal (p. ej. etiqueta en la barra inferior de la hora:
  “Hay una actualización disponible (1.23)”), clicable para abrir la sección.
- Solo gerentes (`Interfaz.admActivo`) ven el aviso y la sección.

### 5. Sección “Actualizaciones”

Nuevo panel `ActualizacionesP` (convención de paneles `P`), en
**Administración → Actualizaciones**, abierto con `visibilidad(3, 8)`.

```
┌ Actualizaciones ─────────────────────────────┐
│ Versión instalada:  1.22                     │
│ Versión disponible: 1.23  (publicada 25/11)  │
│ Novedades:                                   │
│  • Corrige el corte de caja...               │
│ [Buscar ahora]          [Actualizar ahora]   │
└──────────────────────────────────────────────┘
```

- Sin versión nueva: “Tienes la versión más reciente”, botón Actualizar deshabilitado.
- Sin red: “No se pudo consultar. Revisa la conexión a internet.”

### 6. Aplicar la actualización

Al pulsar **Actualizar ahora**:

1. Si `venta_temp` tiene renglones → aviso “Hay una venta en curso; termínela o
   vacíe el carrito” y no continúa.
2. Confirmación: “La app se cerrará y se actualizará. Tardará unos minutos.”
3. Descarga el ZIP a `%TEMP%\PuntoVenta-Update-<v>\` con barra de progreso
   (diálogo modal con Cancelar).
4. Descarga el `.sha256` y compara; si no coincide → borra y muestra error, no continúa.
5. Descomprime (`java.util.zip`) en esa carpeta.
6. Lanza el `.bat` y cierra la app (`System.exit(0)`):
   `cmd /c "<tmp>\aplicar_actualizacion.bat" --auto "<carpeta de instalación>"`
   — la carpeta de instalación es `user.dir` (WorkingDir del lanzador). El `.bat`
   se autoeleva (UAC) como ya hace.

### 7. Cambios a `aplicar_actualizacion.bat`

- Nuevo modo `--auto` (se conserva a través de la autoelevación junto a `--elevated` y la ruta):
  - Sin `pause` en ningún punto (ni en éxito ni en error).
  - Elevación con `cmd /c` en vez de `cmd /k` (la ventana se cierra sola).
  - Espera breve (hasta ~10 s) a que termine el proceso de la app antes de reemplazar
    archivos; el `taskkill` existente queda como respaldo.
  - Al terminar **con éxito o con error**, vuelve a abrir la app **sin privilegios
    de administrador**: `explorer.exe "%INSTALL_DIR%\PuntoVenta.exe"` (un proceso
    lanzado desde la consola elevada heredaría la elevación).
  - Escribe el resultado en `%INSTALL_DIR%\actualizacion.log` (ya lo hace) y
    además `ultima_actualizacion.txt` con `OK <version>` o `ERROR <paso>`.
- Sin `--auto`, el comportamiento actual no cambia (uso manual con USB sigue igual).

### 8. Después de reabrir

- Al iniciar, si existe `ultima_actualizacion.txt`: con `OK` → mensaje
  “Actualizado a la versión X”; con `ERROR` → “La actualización no se completó;
  la versión anterior sigue funcionando. Detalle en actualizacion.log”. Luego se borra el archivo.

## Arranque: tiendas ya instaladas

Las tiendas con la versión actual **no tienen el actualizador**. Necesitan **una
última actualización manual** (ZIP + `aplicar_actualizacion.bat`, o reinstalar
con el nuevo `PuntoVentaSetup.exe`) a la primera versión que lo incluya; a partir
de ahí se actualizan solas. El instalador nuevo ya trae la versión con actualizador.

## Lo que queda a criterio del constructor

- Forma exacta del indicador en el menú y de la notificación (mientras sea no modal y discreta).
- Parser JSON mínimo vs. regex, siempre que no agregue dependencias.
- Manejo de reintentos de descarga (uno es suficiente).
- Pruebas: `VersionApp.esMasNueva` y el parseo de la respuesta de GitHub con JSON
  de ejemplo son lógica pura y deben tener prueba; el `.bat --auto` se prueba con
  la carpeta de instalación simulada (ver memoria de entorno de pruebas).

## Decisiones tomadas por el diseñador (revisables)

- Revisar al iniciar sesión de gerente + cada 6 h.
- Aviso solo para gerentes; sección dentro de Administración.
- Verificación SHA-256 publicada junto al ZIP.
- Bloquear la actualización si hay venta en curso.
- Relanzar la app sin elevación vía `explorer.exe`.
- La versión vive en `src/version.properties` y la mantiene el script de publicar.
