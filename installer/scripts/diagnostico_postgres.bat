@echo off
setlocal enabledelayedexpansion

echo ============================================
echo DIAGNOSTICO POSTGRESQL - Sistema Punto de Venta
echo ============================================
echo.

REM 1. Buscar instalación de PostgreSQL
echo [1/6] Buscando instalacion de PostgreSQL...
set PGPATH=
set PGVERSION=

for %%v in (18 17 16 15 14 13 12) do (
    if exist "%ProgramFiles%\PostgreSQL\%%v\bin\psql.exe" (
        set PGPATH=%ProgramFiles%\PostgreSQL\%%v\bin
        set PGVERSION=%%v
        goto :found_postgres
    )
)

:found_postgres
if "%PGPATH%"=="" (
    echo [X] PostgreSQL NO encontrado
    echo.
    echo SOLUCION: Instale PostgreSQL desde:
    echo https://www.postgresql.org/download/windows/
    pause
    exit /b 1
) else (
    echo [OK] PostgreSQL %PGVERSION% encontrado en:
    echo     %PGPATH%
)
echo.

REM 2. Verificar servicio
echo [2/6] Verificando servicio PostgreSQL...
sc query postgresql-x64-%PGVERSION% >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Servicio postgresql-x64-%PGVERSION% no encontrado
    echo.
    echo Servicios PostgreSQL disponibles:
    sc query type= service state= all | findstr /C:"postgresql"
    echo.
) else (
    sc query postgresql-x64-%PGVERSION% | findstr /C:"STATE" | findstr /C:"RUNNING" >nul
    if !errorlevel! == 0 (
        echo [OK] Servicio esta CORRIENDO
    ) else (
        echo [!] Servicio esta DETENIDO
        echo.
        echo Intentando iniciar...
        net start postgresql-x64-%PGVERSION%
        timeout /t 3 /nobreak >nul
    )
)
echo.

REM 3. Verificar puerto
echo [3/6] Verificando que PostgreSQL escucha en puerto 5432...
netstat -an | findstr /C:":5432" | findstr /C:"LISTENING" >nul
if %errorlevel% == 0 (
    echo [OK] Puerto 5432 esta en uso (PostgreSQL escuchando)
) else (
    echo [X] Puerto 5432 NO esta en uso
    echo.
    echo Verificando configuracion...
    if exist "%ProgramFiles%\PostgreSQL\%PGVERSION%\data\postgresql.conf" (
        findstr /C:"port" "%ProgramFiles%\PostgreSQL\%PGVERSION%\data\postgresql.conf" | findstr /V /C:"#"
    )
)
echo.

REM 4. Probar conexión
echo [4/6] Probando conexion a PostgreSQL...
set /p PGUSER="Ingrese usuario PostgreSQL [postgres]: "
if "%PGUSER%"=="" set PGUSER=postgres

set /p PGPASSWORD="Ingrese password PostgreSQL: "
if "%PGPASSWORD%"=="" (
    echo [!] No ingreso password - intentando sin password
)

echo.
echo Conectando con:
echo   Usuario: %PGUSER%
echo   Host: localhost
echo   Puerto: 5432
echo.

"%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -c "SELECT version();" 2>nul
if %errorlevel% == 0 (
    echo.
    echo [OK] Conexion exitosa!
) else (
    echo.
    echo [X] Fallo la conexion
    echo.
    echo Intentando diagnosticar...
    echo.
    
    REM Probar sin password
    "%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -w -c "SELECT 1;" 2>nul
    if !errorlevel! == 0 (
        echo [!] La conexion funciona SIN password
        echo     Verifique que este usando la password correcta
    ) else (
        echo [!] La conexion falla incluso sin password
        echo.
        echo Verificando pg_hba.conf...
        if exist "%ProgramFiles%\PostgreSQL\%PGVERSION%\data\pg_hba.conf" (
            echo Contenido relevante de pg_hba.conf:
            findstr /V /C:"#" "%ProgramFiles%\PostgreSQL\%PGVERSION%\data\pg_hba.conf" | findstr /C:"local" /C:"host"
        )
    )
)
echo.

REM 5. Verificar base de datos
echo [5/6] Verificando base de datos 'punto_de_venta'...
"%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -c "SELECT 1 FROM pg_database WHERE datname='punto_de_venta';" -t 2>nul | findstr /C:"1" >nul
if %errorlevel% == 0 (
    echo [OK] Base de datos 'punto_de_venta' existe
    
    echo.
    echo Verificando tablas...
    "%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -d punto_de_venta -c "\dt" 2>nul
) else (
    echo [!] Base de datos 'punto_de_venta' NO existe
)
echo.

REM 6. Verificar archivos de backup
echo [6/6] Verificando archivos de backup...
set SCRIPT_DIR=%~dp0
if exist "%SCRIPT_DIR%..\base_datos\backup.tar" (
    echo [OK] backup.tar encontrado
    dir "%SCRIPT_DIR%..\base_datos\backup.tar" | findstr /C:"backup.tar"
) else (
    echo [X] backup.tar NO encontrado
)

if exist "%SCRIPT_DIR%..\base_datos\backup.dump" (
    echo [OK] backup.dump encontrado
    dir "%SCRIPT_DIR%..\base_datos\backup.dump" | findstr /C:"backup.dump"
) else (
    echo [X] backup.dump NO encontrado
)

if exist "%SCRIPT_DIR%..\base_datos\backup.sql" (
    echo [OK] backup.sql encontrado
    dir "%SCRIPT_DIR%..\base_datos\backup.sql" | findstr /C:"backup.sql"
) else (
    echo [X] backup.sql NO encontrado
)
echo.

echo ============================================
echo DIAGNOSTICO COMPLETADO
echo ============================================
echo.
echo Si necesita ayuda, comparta esta informacion.
echo.
pause