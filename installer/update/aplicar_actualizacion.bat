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
REM Si ya venimos relanzados como administrador (marcador --elevated), saltamos
REM TODA la deteccion y la re-elevacion. Esto rompe cualquier bucle de
REM auto-elevacion aunque el equipo reporte mal el nivel de integridad del
REM token (relanzado como admin, UAC ya no pregunta y se generaria un bucle
REM de ventanas que parpadean y se cierran).
if /i "%~1"=="--elevated" goto :ya_es_admin

REM Detectamos la elevacion REAL por el nivel de integridad del token, que es
REM justo lo que controla UAC. No usamos "net session" (depende del servicio
REM LanmanServer) ni "cacls" (depende de ACLs del archivo y dio falsos
REM positivos en equipos endurecidos). Los SID de integridad NO se traducen
REM segun el idioma de Windows:
REM   S-1-16-12288 = Alta (proceso elevado)   S-1-16-16384 = Sistema
whoami /groups | findstr /c:"S-1-16-12288" /c:"S-1-16-16384" >nul 2>&1
if %errorlevel% equ 0 goto :ya_es_admin

echo Solicitando permisos de administrador...
REM Relanzamos a traves de "cmd /k" para que la ventana elevada NO se cierre
REM (algunos contextos de elevacion no retienen el "pause" del script y la
REM consola desaparece antes de que se pueda leer el error o el resultado).
REM Pasamos el marcador --elevated y, si lo hubo, el argumento de ruta.
REM -ArgumentList como UNA sola cadena evita que PowerShell vuelva a re-citar
REM los argumentos. -PassThru: si el proceso elevado NO llega a lanzarse,
REM Start-Process no lanza excepcion pero $p queda nulo -> lo detectamos.
if "%~1"=="" (
    powershell -NoProfile -Command "try { $p = Start-Process -FilePath 'cmd.exe' -ArgumentList '/k \"%~f0\" --elevated' -Verb RunAs -PassThru -ErrorAction Stop; if (-not $p) { exit 1 } } catch { exit 1 }"
) else (
    powershell -NoProfile -Command "try { $p = Start-Process -FilePath 'cmd.exe' -ArgumentList '/k \"\"%~f0\" --elevated \"%~1\"\"' -Verb RunAs -PassThru -ErrorAction Stop; if (-not $p) { exit 1 } } catch { exit 1 }"
)
REM Si Start-Process fallo (UAC cancelado, cuenta sin privilegios, sesion
REM no-interactiva, etc.) avisamos en vez de cerrar la ventana en silencio.
if !errorlevel! neq 0 (
    echo.
    echo [ERROR] No se pudo obtener permisos de administrador.
    echo Es posible que hayas cancelado el aviso de Control de Cuentas de
    echo Usuario ^(UAC^), o que esta cuenta de Windows no tenga privilegios
    echo de Administrador.
    echo Vuelve a ejecutar el archivo y acepta el aviso de UAC cuando aparezca.
    pause
)
exit /b

:ya_es_admin
REM Ya corremos como administrador. Si entramos con el marcador, lo descartamos
REM para que el resto del script vea el argumento original (ruta) en %~1.
if /i "%~1"=="--elevated" shift
REM Rastro de diagnostico garantizado: prueba que el hijo elevado SI llego al
REM cuerpo del script (si este archivo no aparece, el problema es la elevacion).
echo [%date% %time%] Proceso elevado iniciado en "%PKGDIR%" >> "%PKGDIR%\actualizacion_debug.log"

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
REM Probamos varias raices: segun la "bitness" del proceso elevado,
REM %ProgramFiles% puede apuntar a "Program Files (x86)" y no encontrar una
REM instalacion de 64 bits. %ProgramW6432% SIEMPRE apunta al "Program Files"
REM de 64 bits. Las comillas protegen los parentesis de "(x86)".
set "PGBIN="
for %%r in ("%ProgramW6432%" "%ProgramFiles%" "%ProgramFiles(x86)%") do (
    for %%v in (18 17 16 15 14) do (
        if not defined PGBIN if exist "%%~r\PostgreSQL\%%v\bin\psql.exe" set "PGBIN=%%~r\PostgreSQL\%%v\bin"
    )
)
REM Ultimo recurso: psql.exe ya disponible en el PATH.
if not defined PGBIN for %%p in (psql.exe) do if not "%%~$PATH:p"=="" set "PGBIN=%%~dp$PATH:p"
if not defined PGBIN (
    echo [ERROR] No se encontro PostgreSQL ^(psql.exe^).
    echo Verifica que PostgreSQL este instalado.
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
        if !errorlevel! neq 0 echo [AVISO] V!ver! se aplico pero NO se pudo registrar en schema_version ^(se reintentara en la proxima corrida^).
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
    echo [OK] JAR actualizado ^(respaldo: Proy_Ventas.jar.%STAMP%.bak^).
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
