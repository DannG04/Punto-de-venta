@echo off
REM Script para crear backup de la base de datos PostgreSQL
REM Ejecuta este script antes de crear el instalador

echo ============================================
echo Creando backup de la base de datos...
echo ============================================

REM Solicitar credenciales al usuario
set /p PGUSER="Ingresa el usuario de PostgreSQL [postgres]: " || set PGUSER=postgres
if "%PGUSER%"=="" set PGUSER=postgres

set /p PGPASSWORD="Ingresa la contrasena de PostgreSQL: "
if "%PGPASSWORD%"=="" (
    echo Error: Debes ingresar una contrasena.
    pause
    exit /b 1
)

set /p DBNAME="Ingresa el nombre de la base de datos [punto_de_venta]: " || set DBNAME=punto_de_venta
if "%DBNAME%"=="" set DBNAME=punto_de_venta

set /p PGHOST="Ingresa el host [localhost]: " || set PGHOST=localhost
if "%PGHOST%"=="" set PGHOST=localhost

set /p PGPORT="Ingresa el puerto [5432]: " || set PGPORT=5432
if "%PGPORT%"=="" set PGPORT=5432

echo.
echo Conectando a PostgreSQL...
echo Host: %PGHOST%:%PGPORT%
echo Usuario: %PGUSER%
echo Base de datos: %DBNAME%
echo.

set FECHA=%date:~-4%%date:~3,2%%date:~0,2%

REM Crear backup en formato tar (recomendado para backups completos)
echo Creando backup en formato tar...
"%ProgramFiles%\PostgreSQL\17\bin\pg_dump.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -F t -b -v -f "%~dp0..\base_datos\backup.tar" %DBNAME%

if %errorlevel% neq 0 (
    echo Error al crear el backup en formato tar.
    echo Intentando crear backup en formato custom...
    "%ProgramFiles%\PostgreSQL\17\bin\pg_dump.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -F c -b -v -f "%~dp0..\base_datos\backup.dump" %DBNAME%
    if %errorlevel% neq 0 (
        echo Error al crear el backup en formato custom.
        echo Intentando crear backup en formato SQL plano...
        "%ProgramFiles%\PostgreSQL\17\bin\pg_dump.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -f "%~dp0..\base_datos\backup.sql" %DBNAME%
        if %errorlevel% neq 0 (
            echo Error al crear el backup.
            echo Verifica que las credenciales sean correctas.
            pause
            exit /b 1
        )
    )
)

echo ============================================
echo Backup creado exitosamente en:
echo %~dp0..\base_datos\
echo ============================================
pause
