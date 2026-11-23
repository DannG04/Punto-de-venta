# preparar_dependencias.ps1
# Descarga y verifica (SHA-256) lo pesado que necesita installer\setup.iss y que no
# se guarda en git: el JRE que se instala con la app y el instalador de PostgreSQL.
# Lo usa el botón "Publicar versión" (GitHub Actions) y sirve igual en la PC:
#   pwsh -File installer\preparar_dependencias.ps1
# Si un archivo ya está y su huella coincide, no se vuelve a descargar.
# Una huella distinta = archivo alterado o corrupto: se descarta y el script falla.
#Requires -Version 7
param(
    [string]$Destino = $PSScriptRoot,
    # JRE Temurin 23.0.2+7 (el mismo que trae el instalador desde 2026-05)
    [string]$UrlJre = 'https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.2%2B7/OpenJDK23U-jre_x64_windows_hotspot_23.0.2_7.zip',
    [string]$ShaJre = '8de3b72f164555ad4b847d45bad2e455d60d414b58f63a07e3c6e4b744a7e5a1',
    # PostgreSQL 18.1-2 (EnterpriseDB)
    [string]$UrlPg = 'https://get.enterprisedb.com/postgresql/postgresql-18.1-2-windows-x64.exe',
    [string]$ShaPg = 'a2d5fbdab60a2be71fe1c075895513075aa34a1a541c49b575a3aae0c4be1c0c'
)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'   # la barra de progreso hace muy lento Invoke-WebRequest

function Fallar([string]$m) { Write-Host "[ERROR] $m" -ForegroundColor Red; exit 1 }
function Huella([string]$ruta) { (Get-FileHash $ruta -Algorithm SHA256).Hash.ToLower() }

# Descarga a un temporal y solo lo entrega si la huella coincide.
function Descargar-Verificado([string]$url, [string]$sha, [string]$nombre) {
    $tmp = Join-Path ([IO.Path]::GetTempPath()) ("pv-dep-" + [guid]::NewGuid() + "-" + $nombre)
    try {
        Write-Host "Descargando $nombre ..."
        Invoke-WebRequest -Uri $url -OutFile $tmp -UseBasicParsing
        $h = Huella $tmp
        if ($h -ne $sha.ToLower()) {
            throw "La huella de $nombre no coincide (esperada $sha, obtenida $h). Archivo alterado o corrupto: no se usa."
        }
        return $tmp
    } catch {
        Remove-Item $tmp -Force -ErrorAction SilentlyContinue
        throw
    }
}

try {
    New-Item -ItemType Directory -Force -Path $Destino | Out-Null

    # ---------- PostgreSQL ----------
    $pgDestino = Join-Path $Destino 'postgresql-18.1-2-windows-x64.exe'
    if ((Test-Path $pgDestino) -and ((Huella $pgDestino) -eq $ShaPg.ToLower())) {
        Write-Host "[OK] PostgreSQL ya presente y verificado."
    } else {
        $tmp = Descargar-Verificado $UrlPg $ShaPg 'postgresql-18.1-2-windows-x64.exe'
        Move-Item $tmp $pgDestino -Force
        Write-Host "[OK] PostgreSQL descargado y verificado."
    }

    # ---------- JRE ----------
    # Marca con la huella del zip de origen: si coincide y java.exe existe, ya está.
    $jreDir = Join-Path $Destino 'jre'
    $marca = Join-Path $jreDir '.origen.sha256'
    $listo = (Test-Path (Join-Path $jreDir 'bin\java.exe')) -and (Test-Path $marca) -and ((Get-Content $marca -Raw).Trim() -eq $ShaJre.ToLower())
    if ($listo) {
        Write-Host "[OK] JRE ya presente y verificado."
    } else {
        $zip = Descargar-Verificado $UrlJre $ShaJre 'jre.zip'
        $tmpDir = Join-Path ([IO.Path]::GetTempPath()) ("pv-jre-" + [guid]::NewGuid())
        try {
            Expand-Archive -Path $zip -DestinationPath $tmpDir
            # El zip de Adoptium trae una sola carpeta raíz (jdk-23.0.2+7-jre)
            $raices = @(Get-ChildItem $tmpDir -Directory)
            $origen = if ($raices.Count -eq 1 -and -not (Test-Path (Join-Path $tmpDir 'bin'))) { $raices[0].FullName } else { $tmpDir }
            if (-not (Test-Path (Join-Path $origen 'bin\java.exe'))) { throw "El zip del JRE no contiene bin\java.exe." }
            if (Test-Path $jreDir) { Remove-Item $jreDir -Recurse -Force }
            Move-Item $origen $jreDir
            Set-Content -Path $marca -Value $ShaJre.ToLower() -Encoding ascii -NoNewline
            Write-Host "[OK] JRE descargado, verificado y descomprimido en $jreDir"
        } finally {
            Remove-Item $zip -Force -ErrorAction SilentlyContinue
            Remove-Item $tmpDir -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
} catch {
    Fallar $_.Exception.Message
}
exit 0
