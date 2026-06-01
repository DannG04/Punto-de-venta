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
    REM /l = busqueda literal: sin el, findstr trata [.] como regex y todo matchea.
    findstr /l /c:"[!ver!]" "%SVFILE%" >nul
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
        if !errorlevel! neq 0 echo [AVISO] V!ver! se aplico pero NO se pudo registrar en schema_version (se reintentara en la proxima corrida).
        echo [OK]   V!ver! aplicada.
        echo [%date% %time%] Aplicada V!ver! >> "%LOG%"
    )
)

:swap_jar
REM ---------- 8. Reemplazar JAR y dependencias ----------
if exist "%PKGDIR%\Proy_Ventas.jar" (
    echo Reemplazando aplicacion...
    REM Respaldo con timestamp para no pisar un backup bueno en una segunda corrida.
    if exist "%INSTALL_DIR%\Proy_Ventas.jar" copy /y "%INSTALL_DIR%\Proy_Ventas.jar" "%INSTALL_DIR%\Proy_Ventas.jar.%STAMP%.bak" >nul
    copy /y "%PKGDIR%\Proy_Ventas.jar" "%INSTALL_DIR%\Proy_Ventas.jar" >nul
    echo [OK] JAR actualizado (respaldo: Proy_Ventas.jar.%STAMP%.bak).
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
