# publicar_version.ps1
# Publica una version nueva para que los puntos de venta la detecten y se actualicen solos.
#   1. Valida la version y que el repositorio no tenga cambios sin commit.
#   2. Escribe la version en src/version.properties e installer/setup.iss.
#   3. Compila el JAR (ant del PATH, ant de NetBeans, o javac/jar del JDK).
#   4. Arma el ZIP del parche (empaquetar_update.ps1) y su huella SHA-256.
#   5. Commit + tag + push y crea el release en GitHub con el ZIP y la huella.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File installer\update\publicar_version.ps1 -Version 1.23 -Notas "- Corrige el corte de caja"
#   ... -NotasArchivo notas.md          (notas largas desde un archivo)
#   ... -Jdk "C:\ruta\jdk\bin"          (si no hay ant ni JAVA_HOME)
#   ... -SinPublicar                    (hace todo menos commit/tag/push/release; para probar)
#
# Requisito para publicar: GitHub CLI instalado y con sesion (winget install GitHub.cli; gh auth login).
param(
    [Parameter(Mandatory = $true)][string]$Version,
    [string]$Notas = "",
    [string]$NotasArchivo = "",
    [string]$Jdk = "",
    [switch]$SinPublicar
)
$ErrorActionPreference = 'Stop'
$upd  = $PSScriptRoot
$proj = (Resolve-Path (Join-Path $upd '..\..')).Path

function Fallar([string]$mensaje) {
    Write-Host "[ERROR] $mensaje" -ForegroundColor Red
    exit 1
}

function Partes([string]$v) {
    $v = $v.Trim().TrimStart('v', 'V')
    if ($v -notmatch '^\d+(\.\d+)*$') { return $null }
    return @($v.Split('.') | ForEach-Object { [int]$_ })
}

function EsMasNueva([string]$nueva, [string]$actual) {
    $a = Partes $nueva; $b = Partes $actual
    if ($null -eq $a) { return $false }
    if ($null -eq $b) { $b = @(0) }
    for ($i = 0; $i -lt [Math]::Max($a.Count, $b.Count); $i++) {
        $x = if ($i -lt $a.Count) { $a[$i] } else { 0 }
        $y = if ($i -lt $b.Count) { $b[$i] } else { 0 }
        if ($x -ne $y) { return $x -gt $y }
    }
    return $false
}

# ---------- 1. Validaciones ----------
$Version = $Version.Trim().TrimStart('v', 'V')
$vpFile = Join-Path $proj 'src\version.properties'
$actual = '0'
if (Test-Path $vpFile) {
    $linea = Get-Content $vpFile | Where-Object { $_ -match '^\s*app\.version\s*=' } | Select-Object -First 1
    if ($linea) { $actual = ($linea -split '=', 2)[1].Trim() }
}
if (-not (EsMasNueva $Version $actual)) { Fallar "La versión debe ser mayor que $actual (recibí '$Version')." }

if (-not $SinPublicar -and -not $Notas -and -not $NotasArchivo) {
    Fallar "Indica qué cambió con -Notas ""..."" o -NotasArchivo ruta.md (es lo que verá el dueño de la tienda)."
}
if ($NotasArchivo -and -not (Test-Path $NotasArchivo)) { Fallar "No existe el archivo de notas: $NotasArchivo" }

$cambios = git -C $proj status --porcelain
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo leer el estado de git en $proj." }
if ($cambios) { Fallar "El repositorio tiene cambios sin commit. Haz commit o descártalos antes de publicar:`n$($cambios -join "`n")" }

if (-not $SinPublicar) {
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { Fallar "Falta GitHub CLI. Instálalo con: winget install GitHub.cli  y luego: gh auth login" }
    gh auth status *> $null
    if ($LASTEXITCODE -ne 0) { Fallar "GitHub CLI no tiene sesión. Ejecuta: gh auth login" }
}

# ---------- 2. Escribir la versión ----------
Write-Host "Publicando versión $Version (actual: $actual)..."
Set-Content -Path $vpFile -Value "app.version=$Version" -Encoding ascii
$iss = Join-Path $proj 'installer\setup.iss'
if (Test-Path $iss) {
    $t = Get-Content $iss -Raw -Encoding UTF8
    $t = [regex]::Replace($t, '#define MyAppVersion "[^"]*"', "#define MyAppVersion ""$Version""")
    [IO.File]::WriteAllText($iss, $t, (New-Object Text.UTF8Encoding($false)))
}

# ---------- 3. Compilar ----------
function Buscar-Ant {
    $c = Get-Command ant -ErrorAction SilentlyContinue
    if ($c) { return $c.Source }
    $candidatos = @()
    foreach ($raiz in @($env:ProgramFiles, ${env:ProgramFiles(x86)}, "$env:LOCALAPPDATA\Programs") | Where-Object { $_ }) {
        $candidatos += Get-ChildItem -Path $raiz -Directory -Filter '*NetBeans*' -ErrorAction SilentlyContinue | ForEach-Object {
            Join-Path $_.FullName 'netbeans\extide\ant\bin\ant.bat'
            Join-Path $_.FullName 'extide\ant\bin\ant.bat'
        }
    }
    return ($candidatos | Where-Object { Test-Path $_ } | Select-Object -First 1)
}

function Buscar-Jdk {
    if ($Jdk) { return $Jdk }
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) { return (Join-Path $env:JAVA_HOME 'bin') }
    $c = Get-Command javac -ErrorAction SilentlyContinue
    if ($c) { return (Split-Path $c.Source) }
    return $null
}

function Compilar-ConJdk([string]$bin) {
    $javac = Join-Path $bin 'javac.exe'; $jar = Join-Path $bin 'jar.exe'
    if (-not (Test-Path $javac) -or -not (Test-Path $jar)) { Fallar "No hay javac/jar en $bin" }
    $build = Join-Path $proj 'build\publicar'
    if (Test-Path $build) { Remove-Item $build -Recurse -Force }
    $clases = Join-Path $build 'classes'
    New-Item -ItemType Directory -Force -Path $clases | Out-Null
    $src = Join-Path $proj 'src'
    $fuentes = Join-Path $build 'fuentes.txt'
    Get-ChildItem $src -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' } | Set-Content $fuentes -Encoding ascii
    & $javac --release 23 -encoding UTF-8 -nowarn -cp (Join-Path $proj 'lib\*') -d $clases "@$fuentes"
    if ($LASTEXITCODE -ne 0) { Fallar "La compilación falló." }
    # recursos (iconos, imágenes, version.properties...)
    Get-ChildItem $src -Recurse -File | Where-Object { $_.Extension -notin '.java', '.form' } | ForEach-Object {
        $rel = $_.FullName.Substring($src.Length + 1)
        $dst = Join-Path $clases $rel
        New-Item -ItemType Directory -Force -Path (Split-Path $dst) | Out-Null
        Copy-Item $_.FullName $dst
    }
    # manifiesto (renglones de máximo 72 bytes)
    $cp = 'Class-Path: ' + ((Get-ChildItem (Join-Path $proj 'lib') -Filter *.jar | Sort-Object Name | ForEach-Object { "lib/$($_.Name)" }) -join ' ')
    $lineas = @('Main-Class: puntoventa.ui.Interfaz', $cp.Substring(0, [Math]::Min(70, $cp.Length)))
    for ($i = 70; $i -lt $cp.Length; $i += 69) { $lineas += ' ' + $cp.Substring($i, [Math]::Min(69, $cp.Length - $i)) }
    $manifiesto = Join-Path $build 'MANIFEST.MF'
    [IO.File]::WriteAllText($manifiesto, (($lineas -join "`r`n") + "`r`n"), (New-Object Text.ASCIIEncoding))
    $dist = Join-Path $proj 'dist'
    New-Item -ItemType Directory -Force -Path (Join-Path $dist 'lib') | Out-Null
    $jarOut = Join-Path $dist 'Proy_Ventas.jar'
    if (Test-Path $jarOut) { Remove-Item $jarOut -Force }
    & $jar --create --file $jarOut --manifest $manifiesto -C $clases .
    if ($LASTEXITCODE -ne 0) { Fallar "No se pudo crear el JAR." }
    Copy-Item (Join-Path $proj 'lib\*.jar') (Join-Path $dist 'lib') -Force
}

$ant = Buscar-Ant
if ($ant) {
    Write-Host "Compilando con ant: $ant"
    Push-Location $proj
    try { & $ant -q jar } finally { Pop-Location }
    if ($LASTEXITCODE -ne 0) { Fallar "ant jar falló." }
} else {
    $bin = Buscar-Jdk
    if (-not $bin) { Fallar "No encontré ant (ni el de NetBeans) ni un JDK. Instala NetBeans, define JAVA_HOME o usa -Jdk 'C:\ruta\jdk\bin'." }
    Write-Host "Compilando con el JDK: $bin"
    Compilar-ConJdk $bin
}
$exeDist = Join-Path $proj 'dist\PuntoVenta.exe'
if (-not (Test-Path $exeDist)) { Copy-Item (Join-Path $upd 'PuntoVenta.exe') $exeDist }

# ---------- 4. Empaquetar y huella ----------
& (Join-Path $upd 'empaquetar_update.ps1') -Version $Version
if (-not $?) { Fallar "Falló el empaquetado." }
$zip = Join-Path $proj "installer\output\PuntoVenta-Update-$Version.zip"
if (-not (Test-Path $zip)) { Fallar "No se generó $zip" }
$hash = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLower()
$shaFile = "$zip.sha256"
[IO.File]::WriteAllText($shaFile, "$hash  $(Split-Path $zip -Leaf)`n", (New-Object Text.ASCIIEncoding))
Write-Host "[OK] $zip"
Write-Host "[OK] SHA-256 $hash"

if ($SinPublicar) {
    Write-Host "-SinPublicar: no se hizo commit, tag, push ni release."
    Write-Host "Para deshacer el cambio de versión: git checkout -- src/version.properties installer/setup.iss"
    exit 0
}

# ---------- 5. Publicar ----------
git -C $proj add src/version.properties installer/setup.iss
git -C $proj commit -m "chore(release): v$Version"
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo hacer el commit de la versión." }
git -C $proj tag "v$Version"
git -C $proj push origin HEAD
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo hacer push de la rama." }
git -C $proj push origin "v$Version"
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo hacer push del tag v$Version." }
$notasArgs = if ($NotasArchivo) { @('--notes-file', $NotasArchivo) } else { @('--notes', $Notas) }
gh release create "v$Version" $zip $shaFile --repo DannG04/Punto-de-venta --title "Versión $Version" @notasArgs
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo crear el release en GitHub (el tag v$Version ya está subido; puedes crearlo a mano)." }
Write-Host "[OK] Versión $Version publicada. Los puntos de venta la verán en su próxima revisión (al iniciar sesión o cada 6 horas)."
