# Instalador en cada release — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Que el botón "Publicar versión" arme `PuntoVentaSetup.exe` con la misma versión del parche y lo adjunte al release.

**Architecture:** `installer/preparar_dependencias.ps1` descarga y verifica JRE y PostgreSQL fijados; `publicar_version.ps1 -Instalador` compila `setup.iss` con Inno Setup dentro del mismo `try` que fija la versión y agrega el exe al release; `publicar.yml` prepara dependencias (con caché) e Inno Setup.

**Tech Stack:** PowerShell 7, Inno Setup 6, GitHub Actions, GitHub CLI.

**Spec:** `docs/superpowers/specs/2026-11-23-instalador-en-release-design.md`

## Global Constraints

- JRE: `https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.2%2B7/OpenJDK23U-jre_x64_windows_hotspot_23.0.2_7.zip`, SHA-256 `8de3b72f164555ad4b847d45bad2e455d60d414b58f63a07e3c6e4b744a7e5a1`.
- PostgreSQL: `https://get.enterprisedb.com/postgresql/postgresql-18.1-2-windows-x64.exe`, SHA-256 `a2d5fbdab60a2be71fe1c075895513075aa34a1a541c49b575a3aae0c4be1c0c`, destino `installer/postgresql-18.1-2-windows-x64.exe`.
- Salida del instalador: `installer/output/PuntoVentaSetup.exe` (nombre fijo para el link `releases/latest/download/PuntoVentaSetup.exe`).
- No versionar `installer/README_INSTALADOR.md` ni `installer/verificar_preparativos.bat`.
- `dev` y `master` están protegidas: todo por rama + PR a `dev`, luego PR `dev`→`master`. Commits sin `Co-Authored-By`.
- `publicar_version.ps1` requiere PowerShell 7; las pruebas nunca llaman a la red (usan `PV_ULTIMA_PUBLICADA` y `ISCC` falsos).

## Review Focus

- Descarga con huella distinta (archivo alterado o corrupto): falla y no deja el archivo en `installer/` (Task 1).
- Dependencias ya presentes y correctas: no se vuelven a descargar (Task 1).
- Inno Setup falla o no genera el exe: el script falla antes de crear el release y restaura `setup.iss` (Task 2).
- `setup.iss` compilado con la versión nueva, no con 1.22 (Task 2: el ISCC falso registra `MyAppVersion`).
- El runner no trae Inno Setup: se instala con choco (Task 3, verificado en la corrida simulada).

---

### Task 1: `preparar_dependencias.ps1` y archivos del instalador en git

**Files:**
- Create: `installer/preparar_dependencias.ps1`
- Create: `test/actualizacion/probar_dependencias.ps1`
- Add (`git add -f`): `installer/base_datos/backup.tar`, `installer/scripts/{check_postgres,crear_backup,diagnostico_postgres,iniciar_postgres,install_postgres,reparar_postgres,restore_database}.bat`

**Interfaces:**
- `preparar_dependencias.ps1 [-Destino <dir>] [-UrlJre <u>] [-ShaJre <h>] [-UrlPg <u>] [-ShaPg <h>]` — por defecto `-Destino` = `installer/` y URLs/huellas de Global Constraints (los parámetros existen para las pruebas). Código 0 si ambos quedan presentes y verificados.

- [ ] **Step 1: Prueba** (`probar_dependencias.ps1`, servidor HTTP local con `HttpListener` o `python -m http.server` sirviendo un zip con `carpeta/bin/java.exe` falso y un exe falso):
  - URLs locales + huellas correctas → código 0; existen `<dest>/jre/bin/java.exe` y `<dest>/postgresql-18.1-2-windows-x64.exe`.
  - Segunda corrida → código 0 y el servidor no recibe nuevas peticiones (ya presentes y verificados).
  - Huella de PG incorrecta (otro destino vacío) → código ≠ 0, salida contiene `no coincide`, y el exe no queda en el destino.
- [ ] **Step 2: Correr y ver que falla** — el script no existe.
- [ ] **Step 3: Implementar** — `Invoke-WebRequest` a un archivo temporal, `Get-FileHash`, mover solo si coincide; JRE: `Expand-Archive` a temporal y mover el contenido de la única carpeta raíz a `<dest>/jre` (reemplazando).
- [ ] **Step 4: Correr y ver que pasa.**
- [ ] **Step 5: Commit** — `feat(instalador): descarga verificada de JRE y PostgreSQL; archivos del instalador en git`

---

### Task 2: `publicar_version.ps1 -Instalador`

**Files:**
- Modify: `installer/update/publicar_version.ps1`, `installer/update/publicar_ci.ps1` (pasa `Instalador = $true`)
- Modify: `test/actualizacion/probar_publicar.ps1`

**Interfaces:**
- Switch `-Instalador`. Variable `ISCC` (ruta a ISCC.exe) tiene prioridad al buscar Inno Setup.
- Salida: `installer/output/PuntoVentaSetup.exe`; tercer asset de `gh release create`.

- [ ] **Step 1: Prueba** — en el clon de prueba crear `installer/jre/bin/java.exe`, `installer/postgresql-18.1-2-windows-x64.exe` (falsos) y usar un `ISCC` falso (`.bat`) que copia `installer/setup.iss` a `installer/output/iss_usado.txt` y crea `installer/output/PuntoVentaSetup.exe`:
  - `-Version 9.4 -SinPublicar -Instalador` → código 0, existe `PuntoVentaSetup.exe`, `iss_usado.txt` contiene `#define MyAppVersion "9.4"`, y `setup.iss` vuelve a `1.22`.
  - Sin `installer/jre/bin/java.exe` → código ≠ 0, salida contiene `preparar_dependencias.ps1`.
  - `ISCC` falso que termina con código 1 → código ≠ 0 y `setup.iss` restaurado.
  - `publicar_ci.ps1` simulando con el ISCC falso → genera `PuntoVentaSetup.exe`.
- [ ] **Step 2: Correr y ver que falla.**
- [ ] **Step 3: Implementar.**
- [ ] **Step 4: Correr y ver que pasa** (`probar_publicar.ps1` completo).
- [ ] **Step 5: Commit** — `feat(instalador): publicar_version arma y adjunta PuntoVentaSetup.exe`

---

### Task 3: Workflow y documentación; verificación real

**Files:**
- Modify: `.github/workflows/publicar.yml`
- Modify: `installer/update/README.md`
- Modify: `test/actualizacion/probar_workflows.ps1` (publicar.yml llama a `preparar_dependencias.ps1` y el artefacto incluye `PuntoVentaSetup.exe`)

- [ ] **Step 1: Prueba estática** en `probar_workflows.ps1` → RED.
- [ ] **Step 2: Implementar** — en el job `publicar`: `actions/cache@v4` (path `installer/jre` y `installer/postgresql-18.1-2-windows-x64.exe`, clave con ambas huellas), paso `preparar_dependencias.ps1`, paso Inno Setup (`if (-not (Test-Path "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe")) { choco install innosetup -y --no-progress }`), artefacto con `installer/output/PuntoVentaSetup.exe`. README: link fijo y `pwsh -File installer\preparar_dependencias.ps1` para la PC.
- [ ] **Step 3:** `probar_workflows.ps1` → GREEN; commit `feat(instalador): el boton Publicar arma el instalador`.
- [ ] **Step 4: Verificación real** — rama → PR a `dev` (CI verde) → merge → `gh workflow run publicar.yml --ref dev -f version=9.9 -f notas=prueba -f simular=true` → run verde; descargar el artefacto y comprobar que `PuntoVentaSetup.exe` existe y pesa > 300 MB. Luego PR `dev`→`master` y merge. Sin release real.
