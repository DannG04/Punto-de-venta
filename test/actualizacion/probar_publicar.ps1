# Prueba de installer/update/publicar_version.ps1 en un clon temporal con -SinPublicar
# (no hace commit/tag/push ni crea releases). Uso:
#   pwsh -File test/actualizacion/probar_publicar.ps1 -Jdk C:\ruta\jdk\bin
param([string]$Jdk = "")
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$script:fallos = 0; $script:ok = 0
function Check($cond, $desc) { if ($cond) { $script:ok++ } else { $script:fallos++; Write-Host "[FALLA] $desc" } }

$clon = Join-Path ([IO.Path]::GetTempPath()) 'pv publicar prueba'
if (Test-Path $clon) { Remove-Item $clon -Recurse -Force }
git clone --quiet $repo $clon
# Usar los scripts de la copia de trabajo (pueden no estar en commit todavía)
foreach ($f in 'publicar_version.ps1', 'empaquetar_update.ps1', 'PuntoVenta.exe') {
    $src = Join-Path $repo "installer\update\$f"
    if (Test-Path $src) { Copy-Item $src (Join-Path $clon "installer\update\$f") -Force }
}
Push-Location $clon
# --allow-empty: siempre hay un commit "prueba" como referencia (aunque no haya diferencias que copiar)
git add -A; git -c user.name=prueba -c user.email=prueba@local commit --quiet --allow-empty -m "prueba" 2>$null | Out-Null
$pub = Join-Path $clon 'installer\update\publicar_version.ps1'
$jdkArg = @(); if ($Jdk) { $jdkArg = @('-Jdk', $Jdk) }
# Sin red ni releases reales: "ninguna" = no hay releases, se usa src/version.properties (1.22)
$env:PV_ULTIMA_PUBLICADA = 'ninguna'

function Publicar([string[]]$a) {
    $out = & pwsh -NoProfile -File $pub @a @jdkArg 2>&1 | Out-String
    return @{ Codigo = $LASTEXITCODE; Salida = $out }
}

# 1. versión menor o igual: rechazada
$r = Publicar @('-Version', '1.0', '-Notas', 'x', '-SinPublicar')
Check ($r.Codigo -ne 0) "1.0 rechazada (codigo $($r.Codigo))"
Check ($r.Salida -match 'La versión debe ser mayor que 1\.22') "mensaje de version menor: $($r.Salida)"

# 2. árbol con cambios: rechazado
'x' | Set-Content (Join-Path $clon 'cambio_suelto.txt')
$r = Publicar @('-Version', '9.9', '-Notas', 'x', '-SinPublicar')
Check ($r.Codigo -ne 0) "arbol sucio rechazado"
Check ($r.Salida -match 'cambios sin commit') "mensaje de arbol sucio: $($r.Salida)"
Remove-Item (Join-Path $clon 'cambio_suelto.txt')

# 3. publicación local correcta
$r = Publicar @('-Version', '9.9', '-Notas', 'Notas de prueba', '-SinPublicar')
Check ($r.Codigo -eq 0) "9.9 -SinPublicar codigo 0: $($r.Salida)"
$zip = Join-Path $clon 'installer\output\PuntoVenta-Update-9.9.zip'
$sha = "$zip.sha256"
Check (Test-Path $zip) "existe el ZIP"
Check (Test-Path $sha) "existe el .sha256"
if ((Test-Path $zip) -and (Test-Path $sha)) {
    $esperado = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLower()
    Check ((Get-Content $sha -Raw).Trim().Split(' ')[0] -ceq $esperado) "hash del .sha256 coincide (minusculas)"
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $z = [IO.Compression.ZipFile]::OpenRead($zip)
    $nombres = $z.Entries | ForEach-Object { $_.FullName.Replace('\', '/') }
    foreach ($n in 'Proy_Ventas.jar', 'PuntoVenta.exe', 'aplicar_actualizacion.bat', '_bootstrap_schema_version.sql', 'version.properties') {
        Check ($nombres -contains $n) "ZIP contiene $n"
    }
    Check (@($nombres | Where-Object { $_ -like 'lib/*.jar' }).Count -ge 10) "ZIP contiene lib/*.jar"
    Check (@($nombres | Where-Object { $_ -like 'migrations/V*.sql' }).Count -ge 5) "ZIP contiene migraciones"
    $vp = $z.Entries | Where-Object { $_.FullName -eq 'version.properties' }
    if ($vp) { $t = (New-Object IO.StreamReader($vp.Open())).ReadToEnd(); Check ($t -match 'app.version=9\.9') "version.properties del ZIP = 9.9" }
    $z.Dispose()
    # el JAR también lleva la versión (es la que lee VersionApp)
    $tmpJar = Join-Path ([IO.Path]::GetTempPath()) 'pv-jar-prueba'
    if (Test-Path $tmpJar) { Remove-Item $tmpJar -Recurse -Force }
    $z = [IO.Compression.ZipFile]::OpenRead($zip); [IO.Compression.ZipFileExtensions]::ExtractToFile(($z.Entries | Where-Object FullName -eq 'Proy_Ventas.jar'), "$tmpJar.jar", $true); $z.Dispose()
    $j = [IO.Compression.ZipFile]::OpenRead("$tmpJar.jar")
    $vj = $j.Entries | Where-Object { $_.FullName -eq 'version.properties' }
    Check ($null -ne $vj) "JAR contiene version.properties"
    if ($vj) { $t = (New-Object IO.StreamReader($vj.Open())).ReadToEnd(); Check ($t -match 'app.version=9\.9') "version.properties del JAR = 9.9" }
    Check ($null -ne ($j.Entries | Where-Object { $_.FullName -eq 'puntoventa/ui/Interfaz.class' })) "JAR contiene clases"
    Check ($null -ne ($j.Entries | Where-Object { $_.FullName -like 'icons/*.svg' } | Select-Object -First 1)) "JAR contiene iconos"
    $man = (New-Object IO.StreamReader(($j.Entries | Where-Object FullName -eq 'META-INF/MANIFEST.MF').Open())).ReadToEnd()
    Check ($man -match 'Main-Class: puntoventa\.ui\.Interfaz') "manifiesto con Main-Class"
    $j.Dispose()
}
# la versión solo se escribe para compilar: al terminar los archivos quedan como estaban (master está protegida)
Check ((Get-Content (Join-Path $clon 'src\version.properties') -Raw) -match 'app.version=1\.22') "src/version.properties restaurado a 1.22"
Check ((Get-Content (Join-Path $clon 'installer\setup.iss') -Raw) -match '#define MyAppVersion "1\.22"') "setup.iss restaurado a 1.22"
Check (-not (git -C $clon tag --list 'v9.9')) "-SinPublicar no crea tag"

# 4. versión vigente tomada del último release
$env:PV_ULTIMA_PUBLICADA = '9.9'
$r = Publicar @('-Version', '9.5', '-Notas', 'x', '-SinPublicar')
Check ($r.Codigo -ne 0) "9.5 rechazada cuando el ultimo release es 9.9"
Check ($r.Salida -match 'La versión debe ser mayor que 9\.9') "mensaje usa el ultimo release: $($r.Salida)"
$env:PV_ULTIMA_PUBLICADA = 'ninguna'

# 5. con -Jdk no se usa ant aunque exista (en el runner hay ant y el build.xml es de NetBeans)
$antFalso = Join-Path ([IO.Path]::GetTempPath()) 'pv ant falso'
New-Item -ItemType Directory -Force -Path $antFalso | Out-Null
"@echo off`r`necho ANT FALSO`r`nexit /b 1" | Set-Content -Encoding ascii (Join-Path $antFalso 'ant.bat')
$pathAntes = $env:PATH; $env:PATH = "$antFalso;$env:PATH"
$r = Publicar @('-Version', '9.8', '-Notas', 'x', '-SinPublicar')
$env:PATH = $pathAntes
Check ($r.Codigo -eq 0 -and $r.Salida -notmatch 'ANT FALSO') "con -Jdk compila con el JDK aunque haya ant: $($r.Salida)"

# 6. si la compilación falla, los archivos de versión quedan intactos
'class Roto {' | Set-Content (Join-Path $clon 'src\Roto.java')
git -C $clon add -A; git -C $clon -c user.name=prueba -c user.email=prueba@local commit --quiet -m "roto" | Out-Null
$r = Publicar @('-Version', '9.7', '-Notas', 'x', '-SinPublicar')
Check ($r.Codigo -ne 0) "compilacion rota -> codigo distinto de 0"
Check ((Get-Content (Join-Path $clon 'src\version.properties') -Raw) -match 'app.version=1\.22') "compilacion rota: version.properties intacto"
Check (-not (git -C $clon status --porcelain)) "compilacion rota: arbol sin cambios"
git -C $clon reset --quiet --hard HEAD~1

# 7. el script nunca hace commits
Check ((git -C $clon log -1 --format=%s) -eq 'prueba') "no se crearon commits"

Pop-Location
Write-Host "probar_publicar: $($script:ok) OK, $($script:fallos) fallos"
exit [int]($script:fallos -gt 0)
