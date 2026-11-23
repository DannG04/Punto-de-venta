# Revisiones estáticas de los workflows de GitHub Actions (no se pueden ejecutar localmente).
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$script:fallos = 0; $script:ok = 0
function Check($cond, $desc) { if ($cond) { $script:ok++ } else { $script:fallos++; Write-Host "[FALLA] $desc" } }

$publicar = Get-Content (Join-Path $repo '.github\workflows\publicar.yml') -Raw
$pruebas  = Get-Content (Join-Path $repo '.github\workflows\pruebas.yml') -Raw

# El botón debe llamar a publicar_ci.ps1 (probado tal cual), no armar argumentos en línea.
Check ($publicar -match 'installer/update/publicar_ci\.ps1') "publicar.yml usa publicar_ci.ps1"
Check ($publicar -notmatch '@argumentos') "publicar.yml no hace splat de un arreglo de argumentos"

# El botón arma el instalador: prepara dependencias verificadas, asegura Inno Setup y
# deja PuntoVentaSetup.exe en el artefacto de la simulación.
Check ($publicar -match 'installer/preparar_dependencias\.ps1') "publicar.yml prepara JRE y PostgreSQL verificados"
Check ($publicar -match 'innosetup') "publicar.yml asegura Inno Setup"
Check ($publicar -match 'PuntoVentaSetup\.exe') "el artefacto de la simulacion incluye PuntoVentaSetup.exe"
Check ($publicar -match 'actions/cache@') "las descargas pesadas usan cache"

# origen-master: comparar también el repositorio de origen (un fork con rama 'dev' no cuenta)
# y no interpolar head_ref dentro del script (nombres de rama con $(...)).
$origen = ($pruebas -split 'origen-master:', 2)[1]
Check ($origen -match 'head\.repo\.full_name') "origen-master compara el repo de origen"
Check ($origen -notmatch 'run:[\s\S]*\$\{\{\s*github\.head_ref') "origen-master no interpola head_ref en el script"

Write-Host "probar_workflows: $($script:ok) OK, $($script:fallos) fallos"
exit [int]($script:fallos -gt 0)
