# Prueba de installer/update/aplicar_actualizacion.bat (modo --auto y manual).
# Requiere un PostgreSQL accesible en 127.0.0.1:5432 (postgres/root), p. ej. Docker:
#   docker run -d --name pdv-bat -e POSTGRES_PASSWORD=root -p 5432:5432 postgres:17
# y psql/pg_dump de Windows en -PgBin (o en el PATH).
# Uso: pwsh -File test/actualizacion/probar_bat_auto.ps1 -PgBin C:\ruta\pgsql\bin
param([string]$PgBin = "")
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
if ($PgBin) { $env:PATH = "$PgBin;$env:PATH" }
$env:PGPASSWORD = 'root'
$env:PV_NO_RELANZAR = '1'   # en pruebas no se reabre la app (no hay PuntoVenta.exe real)
$script:fallos = 0; $script:ok = 0
function Check($cond, $desc) { if ($cond) { $script:ok++ } else { $script:fallos++; Write-Host "[FALLA] $desc" } }

$raiz = Join-Path ([IO.Path]::GetTempPath()) 'PV prueba bat (x86)'
if (Test-Path $raiz) { Remove-Item $raiz -Recurse -Force }
$inst = Join-Path $raiz 'Punto Venta (x86)'
New-Item -ItemType Directory -Force -Path $inst | Out-Null

psql -h 127.0.0.1 -U postgres -d postgres -q -c "DROP DATABASE IF EXISTS pv_bat" -c "CREATE DATABASE pv_bat" | Out-Null
"db.host=127.0.0.1`r`ndb.port=5432`r`ndb.name=pv_bat`r`ndb.user=postgres`r`ndb.password=root" | Set-Content -Encoding ascii (Join-Path $inst 'db.properties')

function Nuevo-Paquete($nombre, $migracion, $sql) {
    $pkg = Join-Path $raiz $nombre
    New-Item -ItemType Directory -Force -Path (Join-Path $pkg 'migrations') | Out-Null
    Copy-Item (Join-Path $repo 'installer\update\aplicar_actualizacion.bat') $pkg
    Copy-Item (Join-Path $repo 'installer\update\_bootstrap_schema_version.sql') $pkg
    'nuevo' | Set-Content -NoNewline (Join-Path $pkg 'Proy_Ventas.jar')
    "app.version=1.23" | Set-Content -Encoding ascii (Join-Path $pkg 'version.properties')
    $sql | Set-Content -Encoding ascii (Join-Path $pkg "migrations\$migracion")
    return $pkg
}

function Correr($pkg, [string[]]$extra) {
    $a = @('/c', '.\aplicar_actualizacion.bat', '--elevated') + $extra + @($inst)
    $p = Start-Process cmd.exe -ArgumentList ($a | ForEach-Object { if ($_ -match ' ') { "`"$_`"" } else { $_ } }) `
        -WorkingDirectory $pkg -NoNewWindow -PassThru -RedirectStandardOutput "$pkg\salida.txt" -RedirectStandardInput "$repo\test\actualizacion\vacio.txt"
    if (-not $p.WaitForExit(180000)) { $p.Kill(); return @{ Codigo = -1; Salida = 'TIMEOUT' } }
    return @{ Codigo = $p.ExitCode; Salida = (Get-Content "$pkg\salida.txt" -Raw) }
}

$ultima = Join-Path $inst 'ultima_actualizacion.txt'

# --- Caso 1: --auto con migracion correcta ---
'viejo' | Set-Content -NoNewline (Join-Path $inst 'Proy_Ventas.jar')
$pkg1 = Nuevo-Paquete 'paquete ok' 'V9.1__prueba.sql' "BEGIN; CREATE TABLE IF NOT EXISTS prueba_bat(id int); COMMIT;"
$r = Correr $pkg1 @('--auto')
Check ($r.Codigo -eq 0) "auto OK: codigo 0 (fue $($r.Codigo))"
Check ($r.Salida -notmatch 'Presione una tecla|Press any key') "auto OK: no espera tecla"
Check ((Test-Path $ultima) -and ((Get-Content $ultima -Raw).Trim() -eq 'OK 1.23')) "auto OK: ultima_actualizacion.txt = OK 1.23 (fue '$(if (Test-Path $ultima) { (Get-Content $ultima -Raw).Trim() })')"
Check ((Get-Content (Join-Path $inst 'Proy_Ventas.jar') -Raw) -eq 'nuevo') "auto OK: JAR reemplazado"
Check (@(Get-ChildItem $inst -Filter 'Proy_Ventas.jar.*.bak').Count -ge 1) "auto OK: respaldo .bak del JAR"
if ($script:fallos) { Write-Host $r.Salida }

# --- Caso 2: --auto con migracion que falla ---
Remove-Item $ultima -ErrorAction SilentlyContinue
'viejo' | Set-Content -NoNewline (Join-Path $inst 'Proy_Ventas.jar')
$pkg2 = Nuevo-Paquete 'paquete malo' 'V9.2__falla.sql' "BEGIN; SELECT 1/0; COMMIT;"
$r = Correr $pkg2 @('--auto')
Check ($r.Codigo -ne 0) "auto ERROR: codigo distinto de 0"
Check ($r.Salida -notmatch 'Presione una tecla|Press any key') "auto ERROR: no espera tecla"
Check ((Test-Path $ultima) -and ((Get-Content $ultima -Raw) -match '^ERROR .*V9\.2')) "auto ERROR: ultima_actualizacion.txt = ERROR ... V9.2"
Check ((Get-Content (Join-Path $inst 'Proy_Ventas.jar') -Raw) -eq 'viejo') "auto ERROR: JAR NO reemplazado"

# --- Caso 2b: falla la copia de lib/: el JAR nuevo NO debe quedar puesto (sin sus librerias la app no abre) ---
Remove-Item $ultima -ErrorAction SilentlyContinue
'viejo' | Set-Content -NoNewline (Join-Path $inst 'Proy_Ventas.jar')
$libInst = Join-Path $inst 'lib'
if (Test-Path $libInst) { Remove-Item $libInst -Recurse -Force }
'no soy carpeta' | Set-Content $libInst          # xcopy hacia lib\ falla
$pkgLib = Nuevo-Paquete 'paquete lib' 'V9.4__prueba.sql' "BEGIN; SELECT 1; COMMIT;"
New-Item -ItemType Directory -Force -Path (Join-Path $pkgLib 'lib') | Out-Null
'jar' | Set-Content (Join-Path $pkgLib 'lib\nueva.jar')
$r = Correr $pkgLib @('--auto')
Check ($r.Codigo -ne 0) "auto lib: codigo distinto de 0"
Check ((Test-Path $ultima) -and ((Get-Content $ultima -Raw) -match '^ERROR')) "auto lib: ultima_actualizacion.txt = ERROR"
Check ((Get-Content (Join-Path $inst 'Proy_Ventas.jar') -Raw) -eq 'viejo') "auto lib: JAR NO reemplazado si fallan las librerias"
Remove-Item $libInst -Force

# --- Caso 3: manual (sin --auto) conserva la pausa final y no escribe ultima_actualizacion ---
Remove-Item $ultima -ErrorAction SilentlyContinue
$pkg3 = Nuevo-Paquete 'paquete manual' 'V9.3__prueba.sql' "BEGIN; SELECT 1; COMMIT;"
$r = Correr $pkg3 @()
Check ($r.Codigo -eq 0) "manual: codigo 0"
Check ($r.Salida -match 'Presione una tecla|Press any key') "manual: conserva la pausa"
Check (-not (Test-Path $ultima)) "manual: no escribe ultima_actualizacion.txt"

Write-Host "probar_bat_auto: $($script:ok) OK, $($script:fallos) fallos"
exit [int]($script:fallos -gt 0)
