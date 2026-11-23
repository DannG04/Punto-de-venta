@echo off
REM Script simple para iniciar PostgreSQL
REM Ejecutar como Administrador

echo Iniciando PostgreSQL...

REM Verificar privilegios de administrador
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Este script requiere privilegios de administrador.
    echo Por favor, ejecute este archivo como Administrador.
    pause
    exit /b 1
)

REM Intentar iniciar el servicio
net start postgresql-x64-18

if %errorlevel% neq 0 (
    echo.
    echo No se pudo iniciar el servicio postgresql-x64-18.
    echo Intentando buscar otros servicios de PostgreSQL...
    echo.
    
    REM Buscar servicios de PostgreSQL
    sc query type= service state= all | findstr /i "postgresql"
    
    echo.
    echo Si el servicio no existe, ejecute: reparar_postgres.bat
)

echo.
pause
