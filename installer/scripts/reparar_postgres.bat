@echo off
REM Script para reparar el servicio de PostgreSQL
REM Ejecutar como Administrador

echo ============================================
echo   Script de Reparacion de PostgreSQL
echo ============================================
echo.

REM Verificar privilegios de administrador
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Este script requiere privilegios de administrador.
    echo Por favor, ejecute este archivo como Administrador.
    pause
    exit /b 1
)

REM Ruta de PostgreSQL
set PGPATH=C:\Program Files\PostgreSQL\17
set PGDATA=%PGPATH%\data
set PGBIN=%PGPATH%\bin

echo Verificando instalacion de PostgreSQL...
if not exist "%PGBIN%\postgres.exe" (
    echo ERROR: PostgreSQL no esta instalado en %PGPATH%
    pause
    exit /b 1
)

echo PostgreSQL encontrado en: %PGPATH%
echo.

REM Detener el servicio si existe
echo Deteniendo servicio postgresql-x64-17 (si existe)...
sc stop postgresql-x64-17 >nul 2>&1
timeout /t 3 /nobreak >nul

REM Eliminar el servicio si existe
echo Eliminando servicio antiguo (si existe)...
sc delete postgresql-x64-17 >nul 2>&1
timeout /t 2 /nobreak >nul

REM Registrar el servicio de PostgreSQL
echo.
echo Registrando servicio de PostgreSQL...
"%PGBIN%\pg_ctl.exe" register -N postgresql-x64-18 -D "%PGDATA%" -S auto

if %errorlevel% neq 0 (
    echo ERROR: No se pudo registrar el servicio.
    echo Intentando metodo alternativo...
    
    REM Metodo alternativo usando sc create
    sc create postgresql-x64-17 binPath= "\"%PGBIN%\pg_ctl.exe\" runservice -N \"postgresql-x64-17\" -D \"%PGDATA%\" -w" start= auto DisplayName= "PostgreSQL Database Server 17"
    
    if %errorlevel% neq 0 (
        echo ERROR: Fallo el registro del servicio.
        pause
        exit /b 1
    )
)

echo Servicio registrado exitosamente.
echo.

REM Iniciar el servicio
echo Iniciando servicio de PostgreSQL...
sc start postgresql-x64-17

if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudo iniciar el servicio automaticamente.
    echo Intente iniciarlo manualmente con:
    echo    net start postgresql-x64-17
    echo.
    echo O revise los logs en: %PGDATA%\log
    pause
    exit /b 1
)

REM Esperar a que PostgreSQL este listo
echo Esperando a que PostgreSQL inicie...
timeout /t 10 /nobreak >nul

REM Verificar estado
echo.
echo Verificando estado del servicio...
sc query postgresql-x64-17

echo.
echo ============================================
echo   PostgreSQL reparado exitosamente
echo ============================================
echo.
echo Puede verificar la conexion con:
echo Usuario: postgres
echo Puerto: 5432
echo.
pause
