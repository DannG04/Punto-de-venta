@echo off
REM Script para instalar PostgreSQL de forma silenciosa
REM Este script instala PostgreSQL 17 con configuración predeterminada

echo ============================================
echo Instalando PostgreSQL...
echo ============================================

REM Verificar si PostgreSQL ya está instalado
sc query postgresql-x64-18 >nul 2>&1
if %errorlevel% == 0 (
    echo PostgreSQL ya está instalado.
    goto :restore_db
)

REM Instalar PostgreSQL de forma silenciosa
REM Nota: Debes descargar el instalador de PostgreSQL y colocarlo en la carpeta installer
echo Instalando PostgreSQL desde el instalador...
"%~dp0..\postgresql-18.1-2-windows-x64.exe" --mode unattended --unattendedmodeui none --superpassword "root" --servicename "postgresql-x64-18" --serviceaccount "NT AUTHORITY\NetworkService" --serverport 5432 --datadir "%ProgramFiles%\PostgreSQL\18\data" --locale "Spanish, Spain"

if %errorlevel% neq 0 (
    echo Error al instalar PostgreSQL.
    exit /b 1
)

echo PostgreSQL instalado correctamente.

REM Esperar a que el servicio esté activo
timeout /t 10 /nobreak >nul

:restore_db
echo ============================================
echo Restaurando base de datos...
echo ============================================

REM Configurar variable de entorno para pg_restore
set PGPASSWORD=root

REM Crear base de datos
"%ProgramFiles%\PostgreSQL\18\bin\psql.exe" -U postgres -c "CREATE DATABASE punto_de_venta;"

REM Restaurar el backup (tar tiene prioridad)
if exist "%~dp0..\base_datos\backup.tar" (
    "%ProgramFiles%\PostgreSQL\18\bin\pg_restore.exe" -U postgres -d punto_de_venta "%~dp0..\base_datos\backup.tar"
) else (
    "%ProgramFiles%\PostgreSQL\18\bin\pg_restore.exe" -U postgres -d punto_de_venta "%~dp0..\base_datos\backup.dump"
)

if %errorlevel% neq 0 (
    echo Intentando con psql para archivos .sql...
    "%ProgramFiles%\PostgreSQL\18\bin\psql.exe" -U postgres -d punto_de_venta -f "%~dp0..\base_datos\backup.sql"
)

echo ============================================
echo Instalación completada
echo ============================================
exit /b 0
