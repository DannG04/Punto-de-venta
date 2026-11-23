# Instalador en cada release — diseño

Fecha: 2026-11-23 · Estado: aprobado en conversación (opción B)

## Intención

Poder dar a una tienda nueva un link fijo para descargar el instalador completo
(`PuntoVentaSetup.exe`, ~390 MB) y que ese instalador sea siempre de la misma
versión que el parche que reciben las tiendas existentes. Que el botón
"Publicar versión" de GitHub Actions lo arme y lo adjunte solo, sin pasos manuales.

## Metas

- Cada release lleva `PuntoVentaSetup.exe` además de `PuntoVenta-Update-<v>.zip` y su `.sha256`.
- El instalador se compila con la misma versión (`MyAppVersion` y el JAR) que el parche.
- Link fijo: `https://github.com/DannG04/Punto-de-venta/releases/latest/download/PuntoVentaSetup.exe`.
- Con **simular**, el botón también arma el instalador y lo deja como artefacto.

## No metas

- Firmar el instalador (SmartScreen seguirá avisando).
- Regenerar `backup.tar` a partir de las migraciones.
- Releases solo con instalador (romperían la actualización: la app espera el `.zip`).

## Anti-metas

- Descargar dependencias sin verificar su integridad.
- Versionar contraseñas reales (`installer/README_INSTALADOR.md` y
  `installer/verificar_preparativos.bat` mencionan `Daniel183.`: no se versionan).
- Que un fallo al armar el instalador deje publicado un release a medias.

## Contexto

- `installer/setup.iss` toma: `installer\jre\*`, `dist\*`, `installer\scripts\*.bat|*.sql`,
  `installer\update\_bootstrap_schema_version.sql`, `installer\base_datos\*`,
  `migrations\*.sql`, `keystore.jks`, `quagga.min.js`, `installer\postgresql-18.1-2-windows-x64.exe`.
- En git ya están: `setup.iss`, `instalar_bd.bat`, `limpiar_datos_demo.sql`, `_bootstrap_schema_version.sql`,
  `keystore.jks`, `quagga.min.js`, migraciones. **Faltan:** `installer/base_datos/backup.tar` (218 KB)
  y 7 `.bat` de `installer/scripts` (solo contienen la contraseña por defecto `root`).
- `installer/` está en `.gitignore` salvo `installer/update/`; los archivos de arriba se agregan con `git add -f`.
- `publicar_version.ps1` ya escribe la versión en `setup.iss` dentro de un `try/finally` que la restaura.

## Enfoques considerados

| | Enfoque | A favor | En contra |
|---|---|---|---|
| A | Subir el instalador desde la PC del desarrollador (`-Instalador` local) | Rápido | No funciona con el botón de GitHub |
| **B (elegido)** | El workflow descarga JRE y PostgreSQL fijados y verificados, compila con Inno Setup y adjunta | Todo con un clic, versión siempre consistente | ~400 MB de descarga y unos minutos más por publicación; depende de Adoptium/EDB |

## Diseño

### Dependencias fijadas (descarga + verificación SHA-256)

| Archivo | Origen | SHA-256 |
|---|---|---|
| JRE Temurin 23.0.2+7 (zip, 48 858 441 bytes) | `https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.2%2B7/OpenJDK23U-jre_x64_windows_hotspot_23.0.2_7.zip` | `8de3b72f164555ad4b847d45bad2e455d60d414b58f63a07e3c6e4b744a7e5a1` |
| PostgreSQL 18.1-2 (exe, 371 622 632 bytes) | `https://get.enterprisedb.com/postgresql/postgresql-18.1-2-windows-x64.exe` | `a2d5fbdab60a2be71fe1c075895513075aa34a1a541c49b575a3aae0c4be1c0c` |

- Script nuevo `installer/preparar_dependencias.ps1`: descarga (si falta o no coincide la huella)
  a `installer/`, verifica, y descomprime el JRE en `installer/jre` (contenido de la carpeta
  raíz del zip). Huella distinta → falla sin dejar archivos a medias. Sirve en CI y en la PC.
- En el workflow, caché de `actions/cache` con clave basada en ambas huellas.
- Inno Setup: si no existe `ISCC.exe` en el runner, `choco install innosetup -y`.

### `publicar_version.ps1 -Instalador`

- Nuevo switch `-Instalador`. Dentro del mismo `try` (la versión ya está escrita en
  `setup.iss`), después de empaquetar el ZIP: verifica que existan `installer/jre/bin/java.exe`,
  el exe de PostgreSQL y `installer/base_datos/backup.tar`; localiza `ISCC.exe` (variable
  `ISCC` si está definida, luego `Program Files (x86)\Inno Setup 6\ISCC.exe`, luego el PATH)
  y compila `installer/setup.iss`. Debe producir `installer/output/PuntoVentaSetup.exe`.
- Si falla algo del instalador, el script falla **antes** de crear el release (nada a medias).
- Al publicar, el exe se agrega como tercer asset de `gh release create`.

### Workflow `publicar.yml`

- El job `publicar` corre `preparar_dependencias.ps1` (con caché) y asegura Inno Setup antes de
  `publicar_ci.ps1`; `publicar_ci.ps1` pasa siempre `-Instalador`.
- Simulación: el artefacto incluye `PuntoVentaSetup.exe` además del ZIP y su huella.

### Documentación

- `installer/update/README.md`: link fijo del instalador y cómo preparar las dependencias en la PC.

## Lo que queda a criterio del constructor

- Detalles de la caché y de los timeouts de descarga.
- Mensajes de error exactos.

## Decisiones tomadas por el diseñador (revisables)

- No versionar `README_INSTALADOR.md` ni `verificar_preparativos.bat` (contraseña real).
- Mismo JRE que el instalador actual (23.0.2+7), no "la última 23".
- La variable `ISCC` permite pruebas con un compilador falso.
