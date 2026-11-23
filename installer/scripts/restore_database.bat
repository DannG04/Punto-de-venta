@echo off
REM Script para restaurar la base de datos durante la instalacion
REM Este script se ejecuta automaticamente por el instalador

echo ============================================
echo Restaurando base de datos...
echo ============================================

REM Usar credenciales desde variables de entorno (establecidas por el instalador)
if not defined PGUSER set PGUSER=postgres
if not defined PGPASSWORD set PGPASSWORD=root

REM Buscar PostgreSQL en versiones comunes
set PGPATH=
if exist "%ProgramFiles%\PostgreSQL\18\bin\psql.exe" set PGPATH=%ProgramFiles%\PostgreSQL\18\bin
if "%PGPATH%"=="" if exist "%ProgramFiles%\PostgreSQL\17\bin\psql.exe" set PGPATH=%ProgramFiles%\PostgreSQL\17\bin
if "%PGPATH%"=="" if exist "%ProgramFiles%\PostgreSQL\16\bin\psql.exe" set PGPATH=%ProgramFiles%\PostgreSQL\16\bin
if "%PGPATH%"=="" if exist "%ProgramFiles%\PostgreSQL\15\bin\psql.exe" set PGPATH=%ProgramFiles%\PostgreSQL\15\bin
if "%PGPATH%"=="" if exist "%ProgramFiles%\PostgreSQL\14\bin\psql.exe" set PGPATH=%ProgramFiles%\PostgreSQL\14\bin

REM Verificar si PostgreSQL esta instalado
if "%PGPATH%"=="" (
    echo [ERROR] PostgreSQL no encontrado en ubicaciones comunes.
    echo Buscando en: %ProgramFiles%\PostgreSQL\
    pause
    exit /b 1
)

echo [OK] Usando PostgreSQL en: %PGPATH%
echo [OK] Usuario: %PGUSER%
echo.

REM Configurar variable de entorno para autenticacion
set PGPASSWORD=%PGPASSWORD%

REM Esperar a que PostgreSQL este listo
echo Esperando a que PostgreSQL este listo...
timeout /t 5 /nobreak >nul

REM Verificar si la base de datos ya existe
echo Verificando si la base de datos ya existe...
"%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -c "SELECT 1 FROM pg_database WHERE datname='punto_de_venta';" -t 2>nul | findstr /C:"1" >nul 2>&1
if %errorlevel% == 0 (
    echo [INFO] La base de datos 'punto_de_venta' ya existe.
    echo Saltando creacion...
    call :crear_config
    exit /b 0
)

REM Crear la base de datos
echo.
echo Creando base de datos 'punto_de_venta'...
"%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -c "CREATE DATABASE punto_de_venta;"
if %errorlevel% neq 0 (
    echo [ERROR] No se pudo crear la base de datos.
    echo Posibles causas:
    echo   - Credenciales incorrectas
    echo   - PostgreSQL no esta ejecutandose
    echo   - No tiene permisos suficientes
    pause
    exit /b 1
)

echo [OK] Base de datos creada exitosamente.
echo.

REM Determinar ruta de instalacion (un nivel arriba del script)
for %%I in ("%~dp0..") do set "INSTALL_DIR=%%~fI"
set "BACKUP_DIR=%INSTALL_DIR%\base_datos"

echo [DEBUG] Ruta de instalacion: %INSTALL_DIR%
echo [DEBUG] Buscando backup en: %BACKUP_DIR%

REM Restaurar desde backup.tar (formato tar)
if exist "%BACKUP_DIR%\backup.tar" (
    echo Restaurando desde backup.tar...
    "%PGPATH%\pg_restore.exe" -U %PGUSER% -h localhost -p 5432 -d punto_de_venta -v "%BACKUP_DIR%\backup.tar" 2>nul
    
    REM Verificar si la restauracion fue exitosa revisando si hay tablas
    "%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -d punto_de_venta -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" -t 2>nul | findstr /R "[1-9]" >nul
    if %errorlevel% == 0 (
        echo [OK] Base de datos restaurada correctamente desde backup.tar
        call :crear_config
        exit /b 0
    ) else (
        echo [WARN] Fallo restauracion desde backup.tar, intentando siguiente formato...
    )
) else (
    echo [WARN] No se encontro %BACKUP_DIR%\backup.tar
)

REM Restaurar desde backup.dump (formato custom)
if exist "%BACKUP_DIR%\backup.dump" (
    echo Restaurando desde backup.dump...
    "%PGPATH%\pg_restore.exe" -U %PGUSER% -h localhost -p 5432 -d punto_de_venta -v "%BACKUP_DIR%\backup.dump"
    if %errorlevel% == 0 (
        echo [OK] Base de datos restaurada correctamente desde backup.dump
        call :crear_config
        exit /b 0
    ) else (
        echo [WARN] Fallo restauracion desde backup.dump, intentando siguiente formato...
    )
) else (
    echo [WARN] No se encontro %BACKUP_DIR%\backup.dump
)

REM Si falla, intentar con backup.sql (formato plano)
if exist "%BACKUP_DIR%\backup.sql" (
    echo Restaurando desde backup.sql...
    "%PGPATH%\psql.exe" -U %PGUSER% -h localhost -p 5432 -d punto_de_venta -f "%BACKUP_DIR%\backup.sql"
    if %errorlevel% == 0 (
        echo [OK] Base de datos restaurada correctamente desde backup.sql
        call :crear_config
        exit /b 0
    ) else (
        echo [ERROR] Fallo restauracion desde backup.sql
    )
) else (
    echo [WARN] No se encontro %BACKUP_DIR%\backup.sql
)

echo.
echo [ERROR] No se pudo restaurar la base de datos desde ninguno de los formatos disponibles.
echo Archivos buscados en: %BACKUP_DIR%
if exist "%BACKUP_DIR%\backup.tar" (
    echo   - backup.tar (encontrado)
) else (
    echo   - backup.tar (NO encontrado)
)
if exist "%BACKUP_DIR%\backup.dump" (
    echo   - backup.dump (encontrado)
) else (
    echo   - backup.dump (NO encontrado)
)
if exist "%BACKUP_DIR%\backup.sql" (
    echo   - backup.sql (encontrado)
) else (
    echo   - backup.sql (NO encontrado)
)
echo.
pause
exit /b 1

:crear_config
REM Crear archivo de configuracion db.properties
echo Creando archivo de configuracion...
(
    echo # Configuracion de base de datos PostgreSQL
    echo db.host=localhost
    echo db.port=5432
    echo db.name=punto_de_venta
    echo db.user=%PGUSER%
    echo db.password=%PGPASSWORD%
) > "%INSTALL_DIR%\db.properties"
if exist "%INSTALL_DIR%\db.properties" (
    echo [OK] Archivo db.properties creado correctamente
) else (
    echo [WARN] No se pudo crear db.properties en la ubicacion esperada
)
exit /b 0
