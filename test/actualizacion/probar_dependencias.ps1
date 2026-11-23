# Prueba de installer/preparar_dependencias.ps1 con un servidor HTTP local y archivos falsos
# (no descarga nada de internet). Uso: pwsh -File test/actualizacion/probar_dependencias.ps1
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$prep = Join-Path $repo 'installer\preparar_dependencias.ps1'
$script:fallos = 0; $script:ok = 0
function Check($cond, $desc) { if ($cond) { $script:ok++ } else { $script:fallos++; Write-Host "[FALLA] $desc" } }

$raiz = Join-Path ([IO.Path]::GetTempPath()) 'pv dependencias prueba'
if (Test-Path $raiz) { Remove-Item $raiz -Recurse -Force }
$srv = Join-Path $raiz 'servidor'
New-Item -ItemType Directory -Force -Path (Join-Path $raiz 'armado\jdk-23.0.2+7-jre\bin'), $srv | Out-Null

# JRE falso: zip con una carpeta raíz (como el de Adoptium) y bin\java.exe
'java falso' | Set-Content (Join-Path $raiz 'armado\jdk-23.0.2+7-jre\bin\java.exe')
'release falso' | Set-Content (Join-Path $raiz 'armado\jdk-23.0.2+7-jre\release')
Compress-Archive -Path (Join-Path $raiz 'armado\jdk-23.0.2+7-jre') -DestinationPath (Join-Path $srv 'jre.zip')
'postgres falso' | Set-Content (Join-Path $srv 'pg.exe')
$shaJre = (Get-FileHash (Join-Path $srv 'jre.zip') -Algorithm SHA256).Hash.ToLower()
$shaPg  = (Get-FileHash (Join-Path $srv 'pg.exe') -Algorithm SHA256).Hash.ToLower()

$puerto = Get-Random -Minimum 20000 -Maximum 40000
$log = Join-Path $raiz 'servidor.log'
$python = (Get-Command python -ErrorAction SilentlyContinue).Source
if (-not $python) { $python = (Get-Command python3).Source }
$p = Start-Process $python -ArgumentList '-m', 'http.server', $puerto, '--bind', '127.0.0.1', '--directory', "`"$srv`"" -PassThru -WindowStyle Hidden -RedirectStandardError $log
Start-Sleep 2
$base = "http://127.0.0.1:$puerto"
function Peticiones { if (Test-Path $log) { @(Select-String -Path $log -Pattern 'GET /').Count } else { 0 } }

function Preparar($dest, $shaPgUsar) {
    $out = & pwsh -NoProfile -File $prep -Destino $dest -UrlJre "$base/jre.zip" -ShaJre $shaJre -UrlPg "$base/pg.exe" -ShaPg $shaPgUsar 2>&1 | Out-String
    return @{ Codigo = $LASTEXITCODE; Salida = $out }
}

try {
    # 1. descarga, verifica y deja todo en su lugar
    $dest = Join-Path $raiz 'destino ok'
    $r = Preparar $dest $shaPg
    Check ($r.Codigo -eq 0) "primera corrida codigo 0: $($r.Salida)"
    Check (Test-Path (Join-Path $dest 'jre\bin\java.exe')) "JRE descomprimido en jre\bin\java.exe (sin la carpeta raiz del zip)"
    Check (Test-Path (Join-Path $dest 'postgresql-18.1-2-windows-x64.exe')) "instalador de PostgreSQL en su lugar"

    # 2. segunda corrida: nada que descargar
    $antes = Peticiones
    $r = Preparar $dest $shaPg
    Check ($r.Codigo -eq 0) "segunda corrida codigo 0"
    Check ((Peticiones) -eq $antes) "segunda corrida no descarga de nuevo ($antes -> $(Peticiones))"

    # 2b. JRE descomprimido alterado (p. ej. caché manipulada): se reconstruye desde el zip
    #     verificado, sin volver a descargar
    'java ALTERADO' | Set-Content (Join-Path $dest 'jre\bin\java.exe')
    'extra' | Set-Content (Join-Path $dest 'jre\bin\intruso.dll')
    $antes = Peticiones
    $r = Preparar $dest $shaPg
    Check ($r.Codigo -eq 0) "JRE alterado: codigo 0"
    Check ((Get-Content (Join-Path $dest 'jre\bin\java.exe') -Raw).Trim() -eq 'java falso') "JRE alterado: java.exe reconstruido desde el zip verificado"
    Check (-not (Test-Path (Join-Path $dest 'jre\bin\intruso.dll'))) "JRE alterado: archivos ajenos eliminados"
    Check ((Peticiones) -eq $antes) "JRE alterado: no hizo falta descargar (zip en cache verificado)"

    # 2c. zip del JRE en caché alterado: se descarta y se vuelve a descargar
    'zip corrupto' | Set-Content (Join-Path $dest 'jre.zip')
    $antes = Peticiones
    $r = Preparar $dest $shaPg
    Check ($r.Codigo -eq 0 -and (Peticiones) -gt $antes) "zip en cache alterado: se descarga de nuevo"

    # 3. huella incorrecta: falla y no deja el archivo
    $dest2 = Join-Path $raiz 'destino malo'
    $r = Preparar $dest2 ('0' * 64)
    Check ($r.Codigo -ne 0) "huella de PostgreSQL incorrecta -> codigo distinto de 0"
    Check ($r.Salida -match 'no coincide') "mensaje de huella: $($r.Salida)"
    Check (-not (Test-Path (Join-Path $dest2 'postgresql-18.1-2-windows-x64.exe'))) "no queda el exe con huella incorrecta"

    # 4. un PostgreSQL ya presente pero distinto (corrupto) se vuelve a descargar
    'corrupto' | Set-Content (Join-Path $dest 'postgresql-18.1-2-windows-x64.exe')
    $r = Preparar $dest $shaPg
    Check ($r.Codigo -eq 0 -and ((Get-FileHash (Join-Path $dest 'postgresql-18.1-2-windows-x64.exe') -Algorithm SHA256).Hash.ToLower() -eq $shaPg)) "exe corrupto reemplazado por el correcto"
} finally {
    Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
}

Write-Host "probar_dependencias: $($script:ok) OK, $($script:fallos) fallos"
exit [int]($script:fallos -gt 0)
