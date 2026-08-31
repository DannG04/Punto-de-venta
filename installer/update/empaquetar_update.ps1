# empaquetar_update.ps1
# Arma el ZIP del parche: JAR + bootstrap + .bat + todas las migraciones.
# Uso:  powershell -ExecutionPolicy Bypass -File installer\update\empaquetar_update.ps1 -Version 1.14
param([Parameter(Mandatory=$true)][string]$Version)
$ErrorActionPreference = 'Stop'

$updDir = $PSScriptRoot                               # installer/update
$proj   = (Resolve-Path (Join-Path $updDir '..\..')).Path  # raiz del proyecto
$jar    = Join-Path $proj 'dist\Proy_Ventas.jar'
$exe    = Join-Path $proj 'dist\PuntoVenta.exe'

if (-not (Test-Path $jar)) {
    throw "No existe $jar. Compila con 'ant jar' antes de empaquetar."
}
# El lanzador PuntoVenta.exe (generado por launch4j) tiene incrustada la clase
# principal. Si el cliente queda con un .exe viejo y el jar movio la clase
# principal, no abre. Por eso el parche tambien lo lleva y el .bat lo reemplaza.
if (-not (Test-Path $exe)) {
    throw "No existe $exe. Genera el lanzador con launch4j (installer\launch4j-config.xml) antes de empaquetar."
}

$staging = Join-Path $env:TEMP "PuntoVenta-Update-$Version"
if (Test-Path $staging) { Remove-Item $staging -Recurse -Force }
New-Item -ItemType Directory -Path $staging | Out-Null
New-Item -ItemType Directory -Path (Join-Path $staging 'migrations') | Out-Null

Copy-Item $jar                                              $staging
Copy-Item $exe                                              $staging
Copy-Item (Join-Path $updDir 'aplicar_actualizacion.bat')  $staging
Copy-Item (Join-Path $updDir '_bootstrap_schema_version.sql') $staging

# Carpeta lib/ completa (dependencias). IMPRESCINDIBLE: si un build agrega un JAR
# nuevo (p. ej. svg-salamander, nanohttpd), su manifest lo exige pero el lib/ viejo
# del cliente no lo tiene -> NoClassDefFoundError al arrancar y la app no abre.
# aplicar_actualizacion.bat ya sabe copiar esta carpeta al cliente (xcopy /e).
$lib = Join-Path $proj 'dist\lib'
if (-not (Test-Path $lib)) {
    throw "No existe $lib. Compila con 'ant jar' antes de empaquetar."
}
New-Item -ItemType Directory -Path (Join-Path $staging 'lib') | Out-Null
Copy-Item (Join-Path $lib '*') (Join-Path $staging 'lib') -Recurse

# Todas las migraciones .sql (el .bat salta las ya aplicadas via schema_version)
Copy-Item (Join-Path $proj 'migrations\V*.sql') (Join-Path $staging 'migrations')

$outDir = Join-Path $proj 'installer\output'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$zip = Join-Path $outDir "PuntoVenta-Update-$Version.zip"
if (Test-Path $zip) { Remove-Item $zip -Force }
Compress-Archive -Path (Join-Path $staging '*') -DestinationPath $zip

Remove-Item $staging -Recurse -Force
Write-Host "[OK] Parche creado: $zip"
