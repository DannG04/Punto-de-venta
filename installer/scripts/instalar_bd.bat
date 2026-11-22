@echo off
setlocal EnableDelayedExpansion
REM ============================================================
REM  Preparacion de la base de datos durante la instalacion.
REM  Lo invoca setup.iss (ssPostInstall) con PGUSER y PGPASSWORD
REM  en el entorno. Uso: instalar_bd.bat "<carpeta de instalacion>"
REM
REM  1. Crea punto_de_venta y restaura base_datos\backup.tar SOLO si
REM     la base no existe (una reinstalacion no duplica ni pisa datos).
REM  2. Inicializa schema_version.
REM  3. Aplica las migraciones de migrations\ que falten. Sin este paso
REM     una instalacion nueva queda con el esquema del backup (V1.12) y
REM     la app falla en compras, proveedores, inventario, etc.
REM  Codigo de salida 0 = OK; cualquier otro = fallo (ver instalacion_bd.log).
REM ============================================================

set "APPDIR=%~1"
if "%APPDIR%"=="" for %%I in ("%~dp0..") do set "APPDIR=%%~fI"
if "%APPDIR:~-1%"=="\" set "APPDIR=%APPDIR:~0,-1%"
set "LOG=%APPDIR%\instalacion_bd.log"
if not defined PGUSER set "PGUSER=postgres"
if not defined PGPASSWORD set "PGPASSWORD=root"
set "PGHOST=localhost"
set "PGPORT=5432"
set "DB=punto_de_venta"

echo [%date% %time%] Inicio instalacion BD en "%APPDIR%" > "%LOG%"

REM ---------- Localizar PostgreSQL ----------
set "PGBIN="
for %%r in ("%ProgramW6432%" "%ProgramFiles%" "%ProgramFiles(x86)%") do (
    for %%v in (18 17 16 15 14) do (
        if not defined PGBIN if exist "%%~r\PostgreSQL\%%v\bin\psql.exe" set "PGBIN=%%~r\PostgreSQL\%%v\bin"
    )
)
if not defined PGBIN for %%p in (psql.exe) do if not "%%~$PATH:p"=="" set "PGBIN=%%~dp$PATH:p"
if not defined PGBIN (
    echo [ERROR] No se encontro psql.exe >> "%LOG%"
    exit /b 2
)
set "PATH=%PGBIN%;%PATH%"
echo [OK] PostgreSQL: %PGBIN% >> "%LOG%"

REM ---------- Esperar al servidor (hasta ~60 s) ----------
set /a intentos=0
:esperar
psql.exe -w -d postgres -tAc "SELECT 1" >nul 2>>"%LOG%"
if !errorlevel! equ 0 goto :servidor_listo
set /a intentos+=1
if !intentos! geq 20 (
    echo [ERROR] PostgreSQL no responde o las credenciales son incorrectas >> "%LOG%"
    exit /b 3
)
timeout /t 3 /nobreak >nul
goto :esperar
:servidor_listo

REM ---------- Crear y restaurar solo si la BD no existe ----------
set "EXISTE="
for /f %%x in ('psql.exe -w -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='%DB%'"') do set "EXISTE=%%x"
if "%EXISTE%"=="1" (
    echo [INFO] La base %DB% ya existe: se conservan sus datos >> "%LOG%"
) else (
    psql.exe -w -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE %DB%;" >> "%LOG%" 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] No se pudo crear la base >> "%LOG%"
        exit /b 4
    )
    pg_restore.exe -w -d %DB% --no-owner "%APPDIR%\base_datos\backup.tar" >> "%LOG%" 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] Fallo pg_restore de backup.tar >> "%LOG%"
        exit /b 5
    )
    echo [OK] Base creada y restaurada >> "%LOG%"
    REM Solo en base nueva: quitar datos de prueba del respaldo
    psql.exe -w -d %DB% -v ON_ERROR_STOP=1 -f "%APPDIR%\scripts\limpiar_datos_demo.sql" >> "%LOG%" 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] Fallo la limpieza de datos de prueba >> "%LOG%"
        exit /b 8
    )
    echo [OK] Datos de prueba eliminados >> "%LOG%"
)

REM ---------- Control de versiones ----------
psql.exe -w -d %DB% -v ON_ERROR_STOP=1 -f "%APPDIR%\scripts\_bootstrap_schema_version.sql" >> "%LOG%" 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Fallo el bootstrap de schema_version >> "%LOG%"
    exit /b 6
)

set "SVFILE=%TEMP%\pv_schema_versions_inst.txt"
psql.exe -w -d %DB% -tAc "SELECT '['||version||']' FROM schema_version" > "%SVFILE%" 2>>"%LOG%"

REM ---------- Migraciones pendientes ----------
if not exist "%APPDIR%\migrations" goto :fin
for /f "delims=" %%F in ('dir /b /on "%APPDIR%\migrations\V*.sql" 2^>nul') do (
    set "fname=%%~nF"
    set "ver=!fname:~1!"
    for /f "tokens=1 delims=_" %%A in ("!ver!") do set "ver=%%A"
    findstr /l /c:"[!ver!]" "%SVFILE%" >nul
    if !errorlevel! neq 0 (
        psql.exe -w -d %DB% -v ON_ERROR_STOP=1 -f "%APPDIR%\migrations\%%F" >> "%LOG%" 2>&1
        if !errorlevel! neq 0 (
            echo [ERROR] Fallo la migracion V!ver! >> "%LOG%"
            exit /b 7
        )
        psql.exe -w -d %DB% -c "INSERT INTO schema_version(version,descripcion) VALUES('!ver!','aplicada por instalador') ON CONFLICT (version) DO NOTHING" >nul 2>>"%LOG%"
        echo [OK] V!ver! aplicada >> "%LOG%"
    )
)

:fin
del "%SVFILE%" >nul 2>&1
echo [%date% %time%] Instalacion BD OK >> "%LOG%"
exit /b 0
