# publicar_version.ps1
# Publica una version nueva para que los puntos de venta la detecten y se actualicen solos.
# Camino normal: el boton "Publicar version" de GitHub Actions (.github/workflows/publicar.yml),
# que corre este mismo script. Tambien se puede correr desde la PC del desarrollador.
#
#   1. Valida la version contra el ULTIMO RELEASE publicado y el estado del repositorio.
#   2. Escribe la version en src/version.properties e installer/setup.iss SOLO para compilar
#      (al terminar se restauran: master esta protegida y no recibe commits directos).
#   3. Compila el JAR (con -Jdk: JDK directo; si no: ant del PATH, ant de NetBeans o JDK).
#   4. Arma el ZIP del parche (empaquetar_update.ps1) y su huella SHA-256.
#   5. Crea tag y release en un paso: gh release create v<v> --target <commit>.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File installer\update\publicar_version.ps1 -Version 1.23 -Notas "- Corrige el corte de caja"
#   ... -NotasArchivo notas.md          (notas largas; tambien se usa notas/v<version>.md si existe)
#   ... -Jdk "C:\ruta\jdk\bin"          (compilar con ese JDK aunque haya ant)
#   ... -SinPublicar                    (todo menos el release; para probar, desde cualquier rama)
#
# Requisito para publicar: GitHub CLI con sesion (gh auth login) o GH_TOKEN (en Actions).
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
$REPO = 'DannG04/Punto-de-venta'

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

function Version-DelArchivo {
    $vp = Join-Path $proj 'src\version.properties'
    if (-not (Test-Path $vp)) { return '0' }
    $linea = Get-Content $vp | Where-Object { $_ -match '^\s*app\.version\s*=' } | Select-Object -First 1
    if ($linea) { return ($linea -split '=', 2)[1].Trim() }
    return '0'
}

# Versión vigente = último release publicado (lo que tienen las tiendas). Sin releases → archivo.
# PV_ULTIMA_PUBLICADA solo se usa en las pruebas ("ninguna" = no hay releases).
function Version-Vigente {
    $forzada = $env:PV_ULTIMA_PUBLICADA
    if ($forzada) {
        if ($forzada -eq 'ninguna') { return Version-DelArchivo }
        return $forzada.TrimStart('v', 'V')
    }
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
        if ($SinPublicar) { return Version-DelArchivo }
        Fallar "Falta GitHub CLI. Instálalo con: winget install GitHub.cli  y luego: gh auth login"
    }
    $tag = gh release view --repo $REPO --json tagName --jq .tagName 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $tag) { return Version-DelArchivo }   # aún no hay releases
    return ($tag.Trim().TrimStart('v', 'V'))
}

# ---------- 1. Validaciones ----------
$Version = $Version.Trim().TrimStart('v', 'V')
$actual = Version-Vigente
if (-not (EsMasNueva $Version $actual)) { Fallar "La versión debe ser mayor que $actual (recibí '$Version')." }

$archivoNotas = $NotasArchivo
if (-not $archivoNotas) {
    $porVersion = Join-Path $proj "notas\v$Version.md"
    if (Test-Path $porVersion) { $archivoNotas = $porVersion }
}
if ($archivoNotas -and -not (Test-Path $archivoNotas)) { Fallar "No existe el archivo de notas: $archivoNotas" }
if (-not $SinPublicar -and -not $Notas -and -not $archivoNotas) {
    Fallar "Indica qué cambió con -Notas ""..."", -NotasArchivo ruta.md o un archivo notas/v$Version.md (es lo que verá el dueño de la tienda)."
}

$cambios = git -C $proj status --porcelain
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo leer el estado de git en $proj." }
if ($cambios) { Fallar "El repositorio tiene cambios sin commit. Haz commit o descártalos antes de publicar:`n$($cambios -join "`n")" }

if (-not $SinPublicar) {
    $rama = (git -C $proj rev-parse --abbrev-ref HEAD).Trim()
    # En Actions el checkout de un dispatch queda en la rama; localmente debe ser master.
    if ($rama -ne 'master') { Fallar "Solo se publica desde master (estás en '$rama')." }
    git -C $proj fetch --quiet origin master
    if ($LASTEXITCODE -ne 0) { Fallar "No se pudo consultar origin/master." }
    $head = (git -C $proj rev-parse HEAD).Trim()
    if ($head -ne (git -C $proj rev-parse origin/master).Trim()) { Fallar "Tu master no es igual a origin/master. Haz git pull antes de publicar." }
    gh release view "v$Version" --repo $REPO *> $null
    if ($LASTEXITCODE -eq 0) { Fallar "Ya existe el release v$Version." }
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
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) { return (Join-Path $env:JAVA_HOME 'bin') }
    $c = Get-Command javac -ErrorAction SilentlyContinue
    if ($c) { return (Split-Path $c.Source) }
    return $null
}

function Compilar-ConJdk([string]$bin) {
    $javac = Join-Path $bin 'javac.exe'; $jar = Join-Path $bin 'jar.exe'
    if (-not (Test-Path $javac) -or -not (Test-Path $jar)) { throw "No hay javac/jar en $bin" }
    $build = Join-Path $proj 'build\publicar'
    if (Test-Path $build) { Remove-Item $build -Recurse -Force }
    $clases = Join-Path $build 'classes'
    New-Item -ItemType Directory -Force -Path $clases | Out-Null
    $src = Join-Path $proj 'src'
    $fuentes = Join-Path $build 'fuentes.txt'
    Get-ChildItem $src -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' } | Set-Content $fuentes -Encoding ascii
    & $javac --release 23 -encoding UTF-8 -nowarn -cp (Join-Path $proj 'lib\*') -d $clases "@$fuentes"
    if ($LASTEXITCODE -ne 0) { throw "La compilación falló." }
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
    if ($LASTEXITCODE -ne 0) { throw "No se pudo crear el JAR." }
    Copy-Item (Join-Path $proj 'lib\*.jar') (Join-Path $dist 'lib') -Force
}

# ---------- 2-4. Versión temporal, compilación y empaquetado (siempre se restaura) ----------
Write-Host "Preparando versión $Version (vigente: $actual)..."
$vpFile = Join-Path $proj 'src\version.properties'
$iss = Join-Path $proj 'installer\setup.iss'
$zip = Join-Path $proj "installer\output\PuntoVenta-Update-$Version.zip"
$shaFile = "$zip.sha256"
$error1 = $null
try {
    Set-Content -Path $vpFile -Value "app.version=$Version" -Encoding ascii
    if (Test-Path $iss) {
        $t = Get-Content $iss -Raw -Encoding UTF8
        $t = [regex]::Replace($t, '#define MyAppVersion "[^"]*"', "#define MyAppVersion ""$Version""")
        [IO.File]::WriteAllText($iss, $t, (New-Object Text.UTF8Encoding($false)))
    }

    if ($Jdk) {
        Write-Host "Compilando con el JDK indicado: $Jdk"
        Compilar-ConJdk $Jdk
    } else {
        $ant = Buscar-Ant
        if ($ant) {
            Write-Host "Compilando con ant: $ant"
            Push-Location $proj
            try { & $ant -q jar } finally { Pop-Location }
            if ($LASTEXITCODE -ne 0) { throw "ant jar falló." }
        } else {
            $bin = Buscar-Jdk
            if (-not $bin) { throw "No encontré ant (ni el de NetBeans) ni un JDK. Instala NetBeans, define JAVA_HOME o usa -Jdk 'C:\ruta\jdk\bin'." }
            Write-Host "Compilando con el JDK: $bin"
            Compilar-ConJdk $bin
        }
    }
    $exeDist = Join-Path $proj 'dist\PuntoVenta.exe'
    if (-not (Test-Path $exeDist)) { Copy-Item (Join-Path $upd 'PuntoVenta.exe') $exeDist }

    & (Join-Path $upd 'empaquetar_update.ps1') -Version $Version
    if (-not (Test-Path $zip)) { throw "No se generó $zip" }
    $hash = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLower()
    [IO.File]::WriteAllText($shaFile, "$hash  $(Split-Path $zip -Leaf)`n", (New-Object Text.ASCIIEncoding))
    Write-Host "[OK] $zip"
    Write-Host "[OK] SHA-256 $hash"
} catch {
    $error1 = $_.Exception.Message
} finally {
    # Nunca dejar la versión cambiada en el repo (master no recibe commits directos)
    git -C $proj checkout --quiet -- src/version.properties installer/setup.iss 2>$null
}
if ($error1) { Fallar $error1 }

if ($SinPublicar) {
    Write-Host "-SinPublicar: no se creó tag ni release."
    exit 0
}

# ---------- 5. Publicar (tag + release en un paso) ----------
$notasArgs = if ($archivoNotas) { @('--notes-file', $archivoNotas) } else { @('--notes', $Notas) }
gh release create "v$Version" $zip $shaFile --repo $REPO --target $head --title "Versión $Version" @notasArgs
if ($LASTEXITCODE -ne 0) { Fallar "No se pudo crear el release v$Version en GitHub." }
Write-Host "[OK] Versión $Version publicada. Los puntos de venta la verán en su próxima revisión (al iniciar sesión o cada 6 horas)."
