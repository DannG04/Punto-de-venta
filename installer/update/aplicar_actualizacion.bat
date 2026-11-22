@echo off
setlocal EnableDelayedExpansion

REM ============================================================
REM  Parche de actualizacion - Sistema Punto de Venta
REM  Aplica migraciones de BD pendientes y reemplaza el JAR.
REM  Uso normal: doble clic (se auto-eleva a Administrador).
REM  Uso de prueba: aplicar_actualizacion.bat "C:\ruta\install_dir"
REM  Desde la app: aplicar_actualizacion.bat --auto "C:\ruta\install_dir"
REM    --auto: sin pausas, deja el resultado en ultima_actualizacion.txt
REM            y vuelve a abrir la app (sin privilegios de administrador).
REM
REM  Nota: dentro de bloques ( ... ) las rutas se usan como !VAR!. Con
REM  %VAR% una ruta con parentesis, p. ej. "Program Files (x86)", cierra el
REM  bloque y el script truena.
REM ============================================================

set "APPID={A1B2C3D4-E5F6-7890-ABCD-EF1234567890}_is1"
set "PKGDIR=%~dp0"
if "%PKGDIR:~-1%"=="\" set "PKGDIR=%PKGDIR:~0,-1%"

REM ---------- 0. Argumentos: [--elevated] [--auto] [ruta] en cualquier orden ----------
set "ELEVADO="
set "AUTO="
set "RUTA_ARG="
:leer_args
if "%~1"=="" goto :args_listos
if /i "%~1"=="--elevated" (set "ELEVADO=1") else if /i "%~1"=="--auto" (set "AUTO=1") else set "RUTA_ARG=%~1"
shift
goto :leer_args
:args_listos

REM ---------- 1. Auto-elevacion a Administrador ----------
REM Si ya venimos relanzados como administrador (marcador --elevated), saltamos
REM TODA la deteccion y la re-elevacion. Esto rompe cualquier bucle de
REM auto-elevacion aunque el equipo reporte mal el nivel de integridad del
REM token (relanzado como admin, UAC ya no pregunta y se generaria un bucle
REM de ventanas que parpadean y se cierran).
if defined ELEVADO goto :ya_es_admin

REM Detectamos la elevacion REAL por el nivel de integridad del token, que es
REM justo lo que controla UAC. No usamos "net session" (depende del servicio
REM LanmanServer) ni "cacls" (depende de ACLs del archivo y dio falsos
REM positivos en equipos endurecidos). Los SID de integridad NO se traducen
REM segun el idioma de Windows:
REM   S-1-16-12288 = Alta (proceso elevado)   S-1-16-16384 = Sistema
whoami /groups | findstr /c:"S-1-16-12288" /c:"S-1-16-16384" >nul 2>&1
if %errorlevel% equ 0 goto :ya_es_admin

echo Solicitando permisos de administrador...
REM Manual: "cmd /k" para que la ventana elevada NO se cierre (algunos
REM contextos de elevacion no retienen el "pause" y la consola desaparece
REM antes de poder leer el resultado). Auto: "cmd /c", la ventana se cierra
REM sola y el resultado queda en ultima_actualizacion.txt.
REM -ArgumentList como UNA sola cadena evita que PowerShell vuelva a re-citar
REM los argumentos. -PassThru: si el proceso elevado NO llega a lanzarse,
REM Start-Process no lanza excepcion pero $p queda nulo -> lo detectamos.
set "MODO_CMD=/k"
set "FLAGS=--elevated"
if defined AUTO set "MODO_CMD=/c"
if defined AUTO set "FLAGS=--elevated --auto"
set "PSARGS=%MODO_CMD% \"\"%~f0\" %FLAGS%\""
if defined RUTA_ARG set "PSARGS=%MODO_CMD% \"\"%~f0\" %FLAGS% \"!RUTA_ARG!\"\""
powershell -NoProfile -Command "try { $p = Start-Process -FilePath 'cmd.exe' -ArgumentList '!PSARGS!' -Verb RunAs -PassThru -ErrorAction Stop; if (-not $p) { exit 1 } } catch { exit 1 }"
if !errorlevel! equ 0 exit /b 0

REM Start-Process fallo (UAC cancelado, cuenta sin privilegios, sesion
REM no-interactiva, etc.): avisamos en vez de cerrar la ventana en silencio.
echo.
echo [ERROR] No se pudo obtener permisos de administrador.
echo Es posible que hayas cancelado el aviso de Control de Cuentas de
echo Usuario ^(UAC^), o que esta cuenta de Windows no tenga privilegios
echo de Administrador.
echo Vuelve a ejecutar el archivo y acepta el aviso de UAC cuando aparezca.
if not defined AUTO (
    pause
    exit /b 1
)
REM En modo auto la app ya se cerro: dejar el motivo y volver a abrirla.
set "INSTALL_DIR=!RUTA_ARG!"
set "PASO=permisos de administrador no concedidos"
goto :resultado_error

:ya_es_admin
REM Rastro de diagnostico garantizado: prueba que el hijo elevado SI llego al
REM cuerpo del script (si este archivo no aparece, el problema es la elevacion).
echo [%date% %time%] Proceso elevado iniciado en "%PKGDIR%" >> "%PKGDIR%\actualizacion_debug.log"

echo ============================================
echo   Actualizacion Punto de Venta
echo ============================================

REM Version que trae este paquete (para el resultado en modo auto)
set "VERSION_PKG=?"
if exist "%PKGDIR%\version.properties" for /f "usebackq tokens=1,* delims==" %%K in ("%PKGDIR%\version.properties") do if /i "%%K"=="app.version" set "VERSION_PKG=%%L"

REM ---------- 2. Localizar instalacion ----------
set "INSTALL_DIR="
REM 2a. Override por argumento (pruebas / instalaciones sin registro / modo auto)
if defined RUTA_ARG if exist "!RUTA_ARG!\db.properties" set "INSTALL_DIR=!RUTA_ARG!"
REM 2b. Registro de Inno Setup (vista nativa y WOW6432Node)
if not defined INSTALL_DIR for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\%APPID%" /v InstallLocation 2^>nul ^| findstr /i "InstallLocation"') do set "INSTALL_DIR=%%B"
if not defined INSTALL_DIR for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\%APPID%" /v InstallLocation 2^>nul ^| findstr /i "InstallLocation"') do set "INSTALL_DIR=%%B"
REM 2c. Fallback al directorio por defecto
if not defined INSTALL_DIR set "INSTALL_DIR=%ProgramFiles%\PuntoVenta"
if "!INSTALL_DIR:~-1!"=="\" set "INSTALL_DIR=!INSTALL_DIR:~0,-1!"

if exist "!INSTALL_DIR!\db.properties" goto :instalacion_ok
echo [ERROR] No se encontro la instalacion en: !INSTALL_DIR!
echo Verifica que el programa este instalado.
REM En modo auto se intenta igual dejar resultado en la ruta recibida.
if defined RUTA_ARG set "INSTALL_DIR=!RUTA_ARG!"
set "PASO=instalacion no encontrada"
goto :fallo
:instalacion_ok
echo [OK] Instalacion: !INSTALL_DIR!
set "LOG=!INSTALL_DIR!\actualizacion.log"

REM ---------- 3. Leer db.properties ----------
set "DBHOST=localhost"
set "DBPORT=5432"
set "DBNAME=punto_de_venta"
set "DBUSER=postgres"
set "DBPASS=root"
for /f "usebackq eol=# tokens=1,* delims==" %%K in ("!INSTALL_DIR!\db.properties") do (
    if /i "%%K"=="db.host"     set "DBHOST=%%L"
    if /i "%%K"=="db.port"     set "DBPORT=%%L"
    if /i "%%K"=="db.name"     set "DBNAME=%%L"
    if /i "%%K"=="db.user"     set "DBUSER=%%L"
    if /i "%%K"=="db.password" set "DBPASS=%%L"
)
echo [OK] BD: !DBNAME! en !DBHOST!:!DBPORT! (usuario !DBUSER!)

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
if defined PGBIN goto :pg_ok
echo [ERROR] No se encontro PostgreSQL ^(psql.exe^).
echo Verifica que PostgreSQL este instalado.
set "PASO=PostgreSQL no encontrado"
goto :fallo
:pg_ok
echo [OK] PostgreSQL: !PGBIN!
REM Poner los binarios en PATH para evitar problemas de comillas con espacios
set "PATH=!PGBIN!;!PATH!"
set "PGPASSWORD=!DBPASS!"

REM ---------- 5. Respaldo de seguridad ----------
for /f %%t in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "STAMP=%%t"
if not exist "!INSTALL_DIR!\base_datos" mkdir "!INSTALL_DIR!\base_datos"
set "BKP=!INSTALL_DIR!\base_datos\backup_pre_!STAMP!.tar"
echo Creando respaldo de seguridad...
pg_dump.exe -h !DBHOST! -p !DBPORT! -U !DBUSER! -F t -f "!BKP!" !DBNAME!
if !errorlevel! equ 0 goto :respaldo_ok
echo [ERROR] No se pudo crear el respaldo. Se aborta sin tocar nada.
set "PASO=respaldo de la base de datos"
goto :fallo
:respaldo_ok
echo [OK] Respaldo: !BKP!

REM ---------- 6. Cerrar la app ----------
REM La app corre como javaw.exe del JRE incluido (PuntoVenta.exe solo la
REM lanza y termina). Si sigue abierta, el JAR queda bloqueado y no se puede
REM reemplazar. En modo auto la app se esta cerrando sola: se espera un poco.
set "FILTRO_APP=Get-CimInstance Win32_Process | Where-Object { $_.ExecutablePath -and $_.ExecutablePath.StartsWith($env:INSTALL_DIR + '\', 'OrdinalIgnoreCase') }"
if defined AUTO (
    for /l %%i in (1,1,10) do (
        powershell -NoProfile -Command "if (@(!FILTRO_APP!).Count) { exit 1 }" >nul 2>&1
        if !errorlevel! equ 0 goto :app_cerrada
        timeout /t 1 /nobreak >nul
    )
)
:app_cerrada
taskkill /IM PuntoVenta.exe /T /F >nul 2>&1
powershell -NoProfile -Command "!FILTRO_APP! | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }" >nul 2>&1

REM ---------- 7. Migraciones ----------
echo Inicializando control de versiones...
psql.exe -h !DBHOST! -p !DBPORT! -U !DBUSER! -d !DBNAME! -v ON_ERROR_STOP=1 -f "!PKGDIR!\_bootstrap_schema_version.sql"
if !errorlevel! equ 0 goto :bootstrap_ok
echo [ERROR] Fallo el bootstrap de schema_version.
set "PASO=bootstrap de schema_version"
goto :fallo
:bootstrap_ok

REM Volcar versiones ya aplicadas a un archivo temporal, con corchetes como
REM centinela para que findstr no confunda 1.1 con 1.14.
set "SVFILE=%TEMP%\pv_schema_versions.txt"
psql.exe -h !DBHOST! -p !DBPORT! -U !DBUSER! -d !DBNAME! -tAc "SELECT '['||version||']' FROM schema_version" > "!SVFILE!" 2>nul

if not exist "!PKGDIR!\migrations" goto :swap_jar
for /f "delims=" %%F in ('dir /b /on "!PKGDIR!\migrations\V*.sql" 2^>nul') do (
    set "fname=%%~nF"
    set "ver=!fname:~1!"
    for /f "tokens=1 delims=_" %%A in ("!ver!") do set "ver=%%A"
    REM /l = busqueda literal: sin el, findstr trata [.] como regex y todo matchea.
    findstr /l /c:"[!ver!]" "!SVFILE!" >nul
    if !errorlevel!==0 (
        echo [SKIP] V!ver! ya estaba aplicada.
    ) else (
        echo [..]   Aplicando V!ver! ...
        psql.exe -h !DBHOST! -p !DBPORT! -U !DBUSER! -d !DBNAME! -v ON_ERROR_STOP=1 -f "!PKGDIR!\migrations\%%F"
        if !errorlevel! neq 0 (
            echo [ERROR] Fallo la migracion V!ver!. Esa transaccion se revirtio.
            echo Tu respaldo esta en: !BKP!
            set "PASO=migracion V!ver!"
            goto :fallo
        )
        REM Registrar la version (garantizado, aunque el .sql no se auto-registre)
        psql.exe -h !DBHOST! -p !DBPORT! -U !DBUSER! -d !DBNAME! -c "INSERT INTO schema_version(version,descripcion) VALUES('!ver!','aplicada por parche') ON CONFLICT (version) DO NOTHING" >nul
        if !errorlevel! neq 0 echo [AVISO] V!ver! se aplico pero NO se pudo registrar en schema_version ^(se reintentara en la proxima corrida^).
        echo [OK]   V!ver! aplicada.
        echo [%date% %time%] Aplicada V!ver! >> "!LOG!"
    )
)

:swap_jar
REM ---------- 8. Reemplazar JAR y dependencias ----------
if not exist "!PKGDIR!\Proy_Ventas.jar" goto :swap_lib
echo Reemplazando aplicacion...
REM Respaldo con timestamp para no pisar un backup bueno en una segunda corrida.
if exist "!INSTALL_DIR!\Proy_Ventas.jar" copy /y "!INSTALL_DIR!\Proy_Ventas.jar" "!INSTALL_DIR!\Proy_Ventas.jar.!STAMP!.bak" >nul
copy /y "!PKGDIR!\Proy_Ventas.jar" "!INSTALL_DIR!\Proy_Ventas.jar" >nul
if !errorlevel! equ 0 goto :jar_ok
echo [ERROR] No se pudo reemplazar Proy_Ventas.jar ^(la app sigue abierta?^).
set "PASO=reemplazo de Proy_Ventas.jar"
goto :fallo
:jar_ok
echo [OK] JAR actualizado ^(respaldo: Proy_Ventas.jar.!STAMP!.bak^).
:swap_lib
if not exist "!PKGDIR!\lib" goto :swap_exe
xcopy /y /e /i "!PKGDIR!\lib\*" "!INSTALL_DIR!\lib\" >nul
if !errorlevel! equ 0 goto :lib_ok
set "PASO=copia de dependencias (lib)"
goto :fallo
:lib_ok
echo [OK] Dependencias actualizadas.
:swap_exe
REM Lanzador PuntoVenta.exe: si el jar cambia la clase principal, un .exe viejo
REM apunta a una clase que ya no existe y la app no abre. Por eso lo reemplazamos.
if not exist "!PKGDIR!\PuntoVenta.exe" goto :fin_ok
if exist "!INSTALL_DIR!\PuntoVenta.exe" copy /y "!INSTALL_DIR!\PuntoVenta.exe" "!INSTALL_DIR!\PuntoVenta.exe.!STAMP!.bak" >nul
copy /y "!PKGDIR!\PuntoVenta.exe" "!INSTALL_DIR!\PuntoVenta.exe" >nul
if !errorlevel! equ 0 goto :exe_ok
set "PASO=reemplazo de PuntoVenta.exe"
goto :fallo
:exe_ok
echo [OK] Lanzador actualizado ^(respaldo: PuntoVenta.exe.!STAMP!.bak^).

:fin_ok
echo.
echo ============================================
echo   Actualizacion completada con exito
echo ============================================
echo [%date% %time%] Actualizacion OK >> "!LOG!"
if not defined AUTO (
    pause
    exit /b 0
)
> "!INSTALL_DIR!\ultima_actualizacion.txt" echo OK !VERSION_PKG!
call :relanzar
exit /b 0

REM ---------- Errores ----------
:fallo
if defined LOG echo [%date% %time%] ERROR !PASO! >> "!LOG!"
if not defined AUTO (
    pause
    exit /b 1
)
:resultado_error
if defined INSTALL_DIR if exist "!INSTALL_DIR!\" > "!INSTALL_DIR!\ultima_actualizacion.txt" echo ERROR !PASO!
call :relanzar
exit /b 1

REM Vuelve a abrir la app SIN privilegios de administrador: un proceso lanzado
REM desde esta consola elevada heredaria la elevacion; explorer.exe lo abre
REM con el usuario normal. PV_NO_RELANZAR solo se usa en las pruebas.
:relanzar
if defined PV_NO_RELANZAR exit /b 0
if exist "!INSTALL_DIR!\PuntoVenta.exe" explorer.exe "!INSTALL_DIR!\PuntoVenta.exe"
exit /b 0
