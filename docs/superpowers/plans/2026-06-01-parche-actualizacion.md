# Parche de actualización — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Crear un paquete de actualización (ZIP) que el usuario ejecuta para aplicar las migraciones de BD pendientes y reemplazar el JAR, sin destruir sus datos.

**Architecture:** Un `.bat` orquestador genérico localiza la instalación vía registro de Inno Setup, lee `db.properties`, respalda la BD con `pg_dump`, aplica solo las migraciones no registradas en una tabla `schema_version` (que se siembra con el historial existente la primera vez), y reemplaza el JAR. Un script de empaquetado arma el ZIP por release. El `setup.iss` se ajusta para que las instalaciones nuevas también nazcan con `schema_version`.

**Tech Stack:** Batch (cmd.exe), PowerShell (empaquetado), PostgreSQL 14–18 (`psql`/`pg_dump`), Inno Setup (Pascal), Ant (compilación del JAR ya existente).

---

## Estructura de archivos

| Archivo | Responsabilidad | Acción |
|---|---|---|
| `installer/update/_bootstrap_schema_version.sql` | Crea la tabla `schema_version` y siembra el historial ≤1.13 | Crear |
| `installer/update/aplicar_actualizacion.bat` | Orquestador que el usuario ejecuta | Crear |
| `installer/update/empaquetar_update.ps1` | Arma el ZIP del release | Crear |
| `installer/update/README.md` | Cómo armar y entregar un parche | Crear |
| `migrations/README.md` | Documentar `schema_version` + plantilla de migración auto-registrante | Modificar |
| `installer/setup.iss` | Sembrar `schema_version` en instalaciones nuevas + bump de versión | Modificar |

**Convención clave:** el bootstrap siembra TODAS las versiones históricas existentes (1.0–1.13). Por eso el empaquetador puede incluir


 *todas* las migraciones `V*.sql` sin riesgo: el `.bat` salta las ya registradas y solo corre las nuevas (1.14+).

---

### Task 1: Bootstrap de `schema_version`

**Files:**
- Create: `installer/update/_bootstrap_schema_version.sql`

- [ ] **Step 1: Crear el archivo SQL**

```sql
-- _bootstrap_schema_version.sql
-- Crea la tabla de control de versiones e inicializa el historial existente.
-- Idempotente: seguro de ejecutar varias veces.

BEGIN;

CREATE TABLE IF NOT EXISTS schema_version (
    version     VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(255),
    aplicada_en TIMESTAMP NOT NULL DEFAULT now()
);

-- Si la tabla está vacía, sembramos el historial que YA traía la BD del
-- instalador v1.0 (migraciones V1.0 a V1.13). Así el parche nunca re-aplica
-- lo viejo: solo correrá las versiones que no estén aquí.
INSERT INTO schema_version (version, descripcion)
SELECT v, 'Baseline previo al sistema de versionado'
FROM (VALUES
    ('1.0'),('1.1'),('1.2'),('1.3'),('1.4'),('1.5'),
    ('1.6'),('1.7'),('1.10'),('1.11'),('1.12'),('1.13')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM schema_version);

COMMIT;
```

- [ ] **Step 2: Verificar contra una BD de prueba**

Crea una BD que simule a un usuario actual (esquema 1.13 sin tabla de versión) y corre el bootstrap. Ajusta la ruta de `psql` a tu versión instalada.

```powershell
$env:PGPASSWORD = "root"
$bin = "$env:ProgramFiles\PostgreSQL\18\bin"
& "$bin\psql.exe"      -U postgres -h localhost -c "DROP DATABASE IF EXISTS pv_test;"
& "$bin\psql.exe"      -U postgres -h localhost -c "CREATE DATABASE pv_test;"
& "$bin\pg_restore.exe" -U postgres -h localhost -d pv_test "installer\base_datos\backup.tar"
& "$bin\psql.exe"      -U postgres -h localhost -d pv_test -v ON_ERROR_STOP=1 -f "installer\update\_bootstrap_schema_version.sql"
& "$bin\psql.exe"      -U postgres -h localhost -d pv_test -c "SELECT version FROM schema_version ORDER BY version;"
```

Expected: la última consulta lista las 12 versiones (1.0 … 1.13). Correrlo dos veces no agrega filas ni da error.

- [ ] **Step 3: Commit**

```bash
git add installer/update/_bootstrap_schema_version.sql
git commit -m "feat: bootstrap de schema_version para control de versiones de BD"
```

---

### Task 2: Plantilla de migración + documentación

**Files:**
- Modify: `migrations/README.md`

- [ ] **Step 1: Agregar sección de `schema_version` y plantilla al README**

Añade al final de `migrations/README.md` (antes de "## Reglas del equipo"):

```markdown
---

## Control de versiones en producción (schema_version)

Las instalaciones de usuarios llevan una tabla `schema_version` que registra qué
migraciones se han aplicado. La crea y siembra `installer/update/_bootstrap_schema_version.sql`.

**Toda migración nueva (V1.14 en adelante) DEBE:**

1. Ser idempotente: usar `IF NOT EXISTS` (DDL) y `WHERE NOT EXISTS` / `ON CONFLICT`
   (DML) para poder re-ejecutarse sin romper nada.
2. Auto-registrarse al final, dentro de su propia transacción.

### Plantilla

​```sql
-- V1.14__descripcion_corta.sql
-- Descripcion: ...
-- Autor: ...
-- Fecha: YYYY-MM-DD

BEGIN;

-- ... cambios idempotentes ...
ALTER TABLE producto ADD COLUMN IF NOT EXISTS ejemplo TEXT;

-- Registrar la versión (el parche también la registra, pero esto deja
-- constancia cuando la corres a mano con psql -f en desarrollo).
INSERT INTO schema_version (version, descripcion)
VALUES ('1.14', 'descripcion corta')
ON CONFLICT (version) DO NOTHING;

COMMIT;
​```

> **Ordenamiento:** el parche aplica las migraciones por nombre de archivo ascendente.
> Numera las migraciones de un mismo release de forma consecutiva (1.14, 1.15, 1.16).
```

- [ ] **Step 2: Actualizar la tabla de historial del README**

En la tabla "## Historial de versiones" de `migrations/README.md`, agrega las filas que falten para reflejar las migraciones ya existentes (1.1 a 1.13), tomando la descripción del encabezado de cada archivo `.sql`. Ejemplo de fila:

```markdown
| 1.13 | `V1.13__fix_precio_lista_override.sql` | Fix override de precios de lista | 2026-05-28 |
```

- [ ] **Step 3: Commit**

```bash
git add migrations/README.md
git commit -m "docs: convencion de schema_version y plantilla de migracion"
```

---

### Task 3: Orquestador `aplicar_actualizacion.bat`

**Files:**
- Create: `installer/update/aplicar_actualizacion.bat`

- [ ] **Step 1: Crear el `.bat` completo**

```bat
@echo off
setlocal EnableDelayedExpansion

REM ============================================================
REM  Parche de actualizacion - Sistema Punto de Venta
REM  Aplica migraciones de BD pendientes y reemplaza el JAR.
REM  Uso normal: doble clic (se auto-eleva a Administrador).
REM  Uso de prueba: aplicar_actualizacion.bat "C:\ruta\install_dir"
REM ============================================================

set "APPID={A1B2C3D4-E5F6-7890-ABCD-EF1234567890}_is1"
set "PKGDIR=%~dp0"
if "%PKGDIR:~-1%"=="\" set "PKGDIR=%PKGDIR:~0,-1%"

REM ---------- 1. Auto-elevacion a Administrador ----------
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo Solicitando permisos de administrador...
    if "%~1"=="" (
        powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    ) else (
        powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -ArgumentList '%~1' -Verb RunAs"
    )
    exit /b
)

echo ============================================
echo   Actualizacion Punto de Venta
echo ============================================

REM ---------- 2. Localizar instalacion ----------
set "INSTALL_DIR="
REM 2a. Override por argumento (pruebas / instalaciones sin registro)
if not "%~1"=="" if exist "%~1\db.properties" set "INSTALL_DIR=%~1"
REM 2b. Registro de Inno Setup (vista nativa y WOW6432Node)
if not defined INSTALL_DIR for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\%APPID%" /v InstallLocation 2^>nul ^| findstr /i "InstallLocation"') do set "INSTALL_DIR=%%B"
if not defined INSTALL_DIR for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\%APPID%" /v InstallLocation 2^>nul ^| findstr /i "InstallLocation"') do set "INSTALL_DIR=%%B"
REM 2c. Fallback al directorio por defecto
if not defined INSTALL_DIR set "INSTALL_DIR=%ProgramFiles%\PuntoVenta"
if "%INSTALL_DIR:~-1%"=="\" set "INSTALL_DIR=%INSTALL_DIR:~0,-1%"

if not exist "%INSTALL_DIR%\db.properties" (
    echo [ERROR] No se encontro la instalacion en: %INSTALL_DIR%
    echo Verifica que el programa este instalado.
    pause & exit /b 1
)
echo [OK] Instalacion: %INSTALL_DIR%
set "LOG=%INSTALL_DIR%\actualizacion.log"

REM ---------- 3. Leer db.properties ----------
set "DBHOST=localhost"
set "DBPORT=5432"
set "DBNAME=punto_de_venta"
set "DBUSER=postgres"
set "DBPASS=root"
for /f "usebackq eol=# tokens=1,* delims==" %%K in ("%INSTALL_DIR%\db.properties") do (
    if /i "%%K"=="db.host"     set "DBHOST=%%L"
    if /i "%%K"=="db.port"     set "DBPORT=%%L"
    if /i "%%K"=="db.name"     set "DBNAME=%%L"
    if /i "%%K"=="db.user"     set "DBUSER=%%L"
    if /i "%%K"=="db.password" set "DBPASS=%%L"
)
echo [OK] BD: %DBNAME% en %DBHOST%:%DBPORT% (usuario %DBUSER%)

REM ---------- 4. Encontrar PostgreSQL ----------
set "PGBIN="
for %%v in (18 17 16 15 14) do (
    if not defined PGBIN if exist "%ProgramFiles%\PostgreSQL\%%v\bin\psql.exe" set "PGBIN=%ProgramFiles%\PostgreSQL\%%v\bin"
)
if not defined PGBIN (
    echo [ERROR] No se encontro PostgreSQL (psql.exe).
    pause & exit /b 1
)
echo [OK] PostgreSQL: %PGBIN%
REM Poner los binarios en PATH para evitar problemas de comillas con espacios
set "PATH=%PGBIN%;%PATH%"
set "PGPASSWORD=%DBPASS%"

REM ---------- 5. Respaldo de seguridad ----------
for /f %%t in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "STAMP=%%t"
if not exist "%INSTALL_DIR%\base_datos" mkdir "%INSTALL_DIR%\base_datos"
set "BKP=%INSTALL_DIR%\base_datos\backup_pre_%STAMP%.tar"
echo Creando respaldo de seguridad...
pg_dump.exe -h %DBHOST% -p %DBPORT% -U %DBUSER% -F t -f "%BKP%" %DBNAME%
if %errorlevel% neq 0 (
    echo [ERROR] No se pudo crear el respaldo. Se aborta sin tocar nada.
    echo [%date% %time%] ERROR backup >> "%LOG%"
    pause & exit /b 1
)
echo [OK] Respaldo: %BKP%

REM ---------- 6. Cerrar la app ----------
taskkill /IM PuntoVenta.exe /T /F >nul 2>&1

REM ---------- 7. Migraciones ----------
echo Inicializando control de versiones...
psql.exe -h %DBHOST% -p %DBPORT% -U %DBUSER% -d %DBNAME% -v ON_ERROR_STOP=1 -f "%PKGDIR%\_bootstrap_schema_version.sql"
if %errorlevel% neq 0 (
    echo [ERROR] Fallo el bootstrap de schema_version.
    echo [%date% %time%] ERROR bootstrap >> "%LOG%"
    pause & exit /b 1
)

REM Volcar versiones ya aplicadas a un archivo temporal, con corchetes como
REM centinela para que findstr no confunda 1.1 con 1.14.
set "SVFILE=%TEMP%\pv_schema_versions.txt"
psql.exe -h %DBHOST% -p %DBPORT% -U %DBUSER% -d %DBNAME% -tAc "SELECT '['||version||']' FROM schema_version" > "%SVFILE%" 2>nul

if not exist "%PKGDIR%\migrations" goto :swap_jar
for /f "delims=" %%F in ('dir /b /on "%PKGDIR%\migrations\V*.sql" 2^>nul') do (
    set "fname=%%~nF"
    set "ver=!fname:~1!"
    for /f "tokens=1 delims=_" %%A in ("!ver!") do set "ver=%%A"
    findstr /c:"[!ver!]" "%SVFILE%" >nul
    if !errorlevel!==0 (
        echo [SKIP] V!ver! ya estaba aplicada.
    ) else (
        echo [..]   Aplicando V!ver! ...
        psql.exe -h %DBHOST% -p %DBPORT% -U %DBUSER% -d %DBNAME% -v ON_ERROR_STOP=1 -f "%PKGDIR%\migrations\%%F"
        if !errorlevel! neq 0 (
            echo [ERROR] Fallo la migracion V!ver!. Esa transaccion se revirtio.
            echo Tu respaldo esta en: %BKP%
            echo [%date% %time%] ERROR migracion V!ver! >> "%LOG%"
            pause & exit /b 1
        )
        REM Registrar la version (garantizado, aunque el .sql no se auto-registre)
        psql.exe -h %DBHOST% -p %DBPORT% -U %DBUSER% -d %DBNAME% -c "INSERT INTO schema_version(version,descripcion) VALUES('!ver!','aplicada por parche') ON CONFLICT (version) DO NOTHING" >nul
        echo [OK]   V!ver! aplicada.
        echo [%date% %time%] Aplicada V!ver! >> "%LOG%"
    )
)

:swap_jar
REM ---------- 8. Reemplazar JAR y dependencias ----------
if exist "%PKGDIR%\Proy_Ventas.jar" (
    echo Reemplazando aplicacion...
    if exist "%INSTALL_DIR%\Proy_Ventas.jar" copy /y "%INSTALL_DIR%\Proy_Ventas.jar" "%INSTALL_DIR%\Proy_Ventas.jar.bak" >nul
    copy /y "%PKGDIR%\Proy_Ventas.jar" "%INSTALL_DIR%\Proy_Ventas.jar" >nul
    echo [OK] JAR actualizado (respaldo: Proy_Ventas.jar.bak).
)
if exist "%PKGDIR%\lib" (
    xcopy /y /e "%PKGDIR%\lib\*" "%INSTALL_DIR%\lib\" >nul
    echo [OK] Dependencias actualizadas.
)

echo.
echo ============================================
echo   Actualizacion completada con exito
echo ============================================
echo [%date% %time%] Actualizacion OK >> "%LOG%"
pause
endlocal
exit /b 0
```

- [ ] **Step 2: Verificar la lectura de config y la localización (sin tocar BD real)**

Crea un directorio de prueba que simule una instalación y ejecuta el `.bat` con el override de argumento. Primero solo valida que detecte rutas y credenciales: comenta temporalmente desde la sección 5 en adelante NO es necesario — en su lugar usa la BD `pv_test` de la Task 1.

```powershell
$test = "$env:TEMP\pv_install_test"
New-Item -ItemType Directory -Force -Path $test | Out-Null
@"
db.host=localhost
db.port=5432
db.name=pv_test
db.user=postgres
db.password=root
"@ | Set-Content -Encoding ascii "$test\db.properties"
# JAR dummy para probar el swap
"jar-viejo" | Set-Content -Encoding ascii "$test\Proy_Ventas.jar"
```

- [ ] **Step 3: Probar el flujo completo contra `pv_test`**

Copia el bootstrap junto al `.bat` (simulando el paquete) y crea una migración de prueba:

```powershell
$pkg = "$env:TEMP\pv_pkg"
New-Item -ItemType Directory -Force -Path "$pkg\migrations" | Out-Null
Copy-Item "installer\update\aplicar_actualizacion.bat" $pkg
Copy-Item "installer\update\_bootstrap_schema_version.sql" $pkg
Copy-Item "dist\Proy_Ventas.jar" $pkg
@"
BEGIN;
ALTER TABLE producto ADD COLUMN IF NOT EXISTS _patch_test TEXT;
INSERT INTO schema_version (version, descripcion)
VALUES ('1.14','migracion de prueba') ON CONFLICT (version) DO NOTHING;
COMMIT;
"@ | Set-Content -Encoding ascii "$pkg\migrations\V1.14__prueba.sql"
# Ejecutar (se elevara a admin; aceptar el UAC)
& "$pkg\aplicar_actualizacion.bat" "$env:TEMP\pv_install_test"
```

Expected:
- Se crea `…\pv_install_test\base_datos\backup_pre_<stamp>.tar`.
- Sale `[OK] V1.14 aplicada.`
- `psql -d pv_test -c "\d producto"` muestra la columna `_patch_test`.
- `…\pv_install_test\Proy_Ventas.jar` ahora contiene el JAR real (no "jar-viejo") y existe `Proy_Ventas.jar.bak`.

- [ ] **Step 4: Probar idempotencia (re-ejecución)**

Vuelve a correr la misma línea `& "$pkg\aplicar_actualizacion.bat" "$env:TEMP\pv_install_test"`.

Expected: ahora sale `[SKIP] V1.14 ya estaba aplicada.`, no hay error, y se crea un segundo backup con stamp distinto.

- [ ] **Step 5: Commit**

```bash
git add installer/update/aplicar_actualizacion.bat
git commit -m "feat: orquestador de parche (migraciones + swap de JAR)"
```

---

### Task 4: Script de empaquetado del release

**Files:**
- Create: `installer/update/empaquetar_update.ps1`

- [ ] **Step 1: Crear el script PowerShell**

```powershell
# empaquetar_update.ps1
# Arma el ZIP del parche: JAR + bootstrap + .bat + todas las migraciones.
# Uso:  powershell -ExecutionPolicy Bypass -File installer\update\empaquetar_update.ps1 -Version 1.14
param([Parameter(Mandatory=$true)][string]$Version)
$ErrorActionPreference = 'Stop'

$updDir = $PSScriptRoot                               # installer/update
$proj   = (Resolve-Path (Join-Path $updDir '..\..')).Path  # raiz del proyecto
$jar    = Join-Path $proj 'dist\Proy_Ventas.jar'

if (-not (Test-Path $jar)) {
    throw "No existe $jar. Compila con 'ant jar' antes de empaquetar."
}

$staging = Join-Path $env:TEMP "PuntoVenta-Update-$Version"
if (Test-Path $staging) { Remove-Item $staging -Recurse -Force }
New-Item -ItemType Directory -Path $staging | Out-Null
New-Item -ItemType Directory -Path (Join-Path $staging 'migrations') | Out-Null

Copy-Item $jar                                              $staging
Copy-Item (Join-Path $updDir 'aplicar_actualizacion.bat')  $staging
Copy-Item (Join-Path $updDir '_bootstrap_schema_version.sql') $staging

# Todas las migraciones .sql (el .bat salta las ya aplicadas via schema_version)
Copy-Item (Join-Path $proj 'migrations\V*.sql') (Join-Path $staging 'migrations')

$outDir = Join-Path $proj 'installer\output'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$zip = Join-Path $outDir "PuntoVenta-Update-$Version.zip"
if (Test-Path $zip) { Remove-Item $zip -Force }
Compress-Archive -Path (Join-Path $staging '*') -DestinationPath $zip

Remove-Item $staging -Recurse -Force
Write-Host "[OK] Parche creado: $zip"
```

- [ ] **Step 2: Verificar el empaquetado**

```powershell
ant jar
powershell -ExecutionPolicy Bypass -File installer\update\empaquetar_update.ps1 -Version 1.14
# Inspeccionar el contenido del ZIP
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::OpenRead("installer\output\PuntoVenta-Update-1.14.zip").Entries | Select-Object FullName
```

Expected: el ZIP contiene `aplicar_actualizacion.bat`, `_bootstrap_schema_version.sql`, `Proy_Ventas.jar` y `migrations/V*.sql` (todas las del repo).

> Nota de memoria del proyecto: el usuario prefiere compilar él mismo. El paso `ant jar` lo corre él; si ejecutas el plan como agente, pídele que compile y solo después empaqueta.

- [ ] **Step 3: Commit**

```bash
git add installer/update/empaquetar_update.ps1
git commit -m "feat: script de empaquetado del parche de actualizacion"
```

---

### Task 5: `setup.iss` — schema_version en instalaciones nuevas + versión

**Files:**
- Modify: `installer/setup.iss`

- [ ] **Step 1: Empaquetar el bootstrap dentro del instalador**

En la sección `[Files]` de `installer/setup.iss`, después de la línea que copia `installer\scripts\*.bat`, agrega:

```pascal
Source: "{#ProjectRoot}\installer\update\_bootstrap_schema_version.sql"; DestDir: "{app}\scripts"; Flags: ignoreversion
```

- [ ] **Step 2: Ejecutar el bootstrap tras restaurar la BD**

En la sección `[Run]`, justo después del paso "4. Restaurar Backup" y antes del paso "5. Ejecutar App", agrega:

```pascal
; 4b. Inicializar control de versiones de esquema (schema_version)
Filename: "{cmd}"; Parameters: "/c set PGPASSWORD={code:GetPostgreSQLPassword}&& ""{code:GetPsqlPath}"" -U {code:GetPostgreSQLUser} -h localhost -p 5432 -d punto_de_venta -v ON_ERROR_STOP=1 -f ""{app}\scripts\_bootstrap_schema_version.sql"""; StatusMsg: "Inicializando control de versiones..."; Flags: waituntilterminated runhidden
```

- [ ] **Step 3: Subir la versión del instalador**

En el bloque de `#define`, cambia:

```pascal
#define MyAppVersion "1.0"
```

por la versión del release que estés sacando, p. ej.:

```pascal
#define MyAppVersion "1.14"
```

- [ ] **Step 4: Verificación manual**

Recompila el instalador con Inno Setup (`setup.iss` → Compile) y, en una VM o equipo limpio sin PostgreSQL, instala. Verifica:

```powershell
$env:PGPASSWORD = "root"
& "$env:ProgramFiles\PostgreSQL\18\bin\psql.exe" -U postgres -d punto_de_venta -c "SELECT version FROM schema_version ORDER BY version;"
```

Expected: lista 1.0 … 1.13 (y 1.14+ si incluiste esas migraciones en el repo al compilar). Sin errores durante la instalación.

- [ ] **Step 5: Commit**

```bash
git add installer/setup.iss
git commit -m "feat: setup.iss inicializa schema_version y sube version"
```

---

### Task 6: README del flujo de parcheo

**Files:**
- Create: `installer/update/README.md`

- [ ] **Step 1: Documentar el procedimiento de release**

```markdown
# Parches de actualización — Punto de Venta

Procedimiento para actualizar usuarios que YA tienen la app + PostgreSQL + la BD.
No reinstala PostgreSQL ni restaura la BD: solo aplica migraciones nuevas y cambia el JAR.

## Cómo sacar un parche

1. Escribe tus migraciones nuevas en `migrations/` siguiendo la plantilla de
   `migrations/README.md` (idempotentes + auto-registro en `schema_version`).
   Numéralas consecutivas: `V1.14__...`, `V1.15__...`.
2. Compila el JAR:  `ant jar`
3. Empaqueta:  `powershell -ExecutionPolicy Bypass -File installer\update\empaquetar_update.ps1 -Version 1.14`
   → genera `installer\output\PuntoVenta-Update-1.14.zip`.
4. Envía el ZIP al usuario.

## Qué hace el usuario

1. Descomprime el ZIP.
2. Clic derecho en `aplicar_actualizacion.bat` → **Ejecutar como administrador**
   (o doble clic; pedirá permisos solo).
3. El parche respalda su BD, aplica las migraciones pendientes y reemplaza el JAR.
   Al terminar puede abrir la app normalmente.

## Seguridad y reversa

- Antes de tocar nada se crea `…\PuntoVenta\base_datos\backup_pre_<fecha>.tar`.
- El JAR anterior queda en `…\PuntoVenta\Proy_Ventas.jar.bak`.
- Si una migración falla, su transacción se revierte y el parche se detiene sin
  cambiar el JAR. Para restaurar la BD a su estado previo:
  ​```
  pg_restore -U postgres -d punto_de_venta --clean "ruta\backup_pre_<fecha>.tar"
  ​```
- El log queda en `…\PuntoVenta\actualizacion.log`.

## Limitaciones conocidas

- Si la contraseña de la BD contiene el carácter `!`, edita `db.properties` o
  ejecuta el SQL a mano (el parsing batch no lo soporta).
```

- [ ] **Step 2: Commit**

```bash
git add installer/update/README.md
git commit -m "docs: guia del flujo de parcheo de actualizacion"
```

---

## Notas de verificación end-to-end

El orden de pruebas recomendado simula a un usuario real en `pv_test`:

1. Task 1 deja `pv_test` con esquema 1.13 + `schema_version` sembrada (1.0–1.13).
2. Task 3 (Steps 3–4) corre el parche completo contra `pv_test` y prueba idempotencia.
3. Task 5 prueba el camino de instalación nueva (VM limpia).

Limpieza al terminar:

```powershell
$env:PGPASSWORD = "root"
& "$env:ProgramFiles\PostgreSQL\18\bin\psql.exe" -U postgres -c "DROP DATABASE IF EXISTS pv_test;"
Remove-Item "$env:TEMP\pv_install_test","$env:TEMP\pv_pkg" -Recurse -Force -ErrorAction SilentlyContinue
```
