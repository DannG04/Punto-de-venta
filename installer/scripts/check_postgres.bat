@echo off
REM Script para verificar si PostgreSQL está instalado y funcionando

echo Verificando instalación de PostgreSQL...

sc query postgresql-x64-18 >nul 2>&1
if %errorlevel% == 0 (
    echo PostgreSQL está instalado y el servicio está activo.
    
    REM Verificar si la base de datos existe
    set PGPASSWORD=root
    "%ProgramFiles%\PostgreSQL\18\bin\psql.exe" -U postgres -lqt | findstr punto_de_venta >nul 2>&1
    if %errorlevel% == 0 (
        echo La base de datos 'punto_de_venta' existe.
        exit /b 0
    ) else (
        echo La base de datos 'punto_de_venta' NO existe.
        exit /b 1
    )
) else (
    echo PostgreSQL NO está instalado.
    exit /b 1
)
