# publicar_ci.ps1 — lo llama .github/workflows/publicar.yml (botón "Publicar versión").
# Lee las entradas del workflow desde variables de entorno (VERSION, NOTAS, SIMULAR,
# JAVA_HOME) y llama a publicar_version.ps1. Vive en un archivo propio para poder
# probarlo exactamente como lo ejecuta Actions (test/actualizacion/probar_publicar.ps1).
#
# Ojo: el splat debe ser una TABLA (@{...}). Con un arreglo de cadenas PowerShell
# los toma como argumentos posicionales y "-Version" llega como valor, no como nombre.
$ErrorActionPreference = 'Stop'
$parametros = @{ Version = $env:VERSION; Jdk = "$env:JAVA_HOME\bin" }
if ($env:NOTAS) { $parametros.Notas = $env:NOTAS }
if ($env:SIMULAR -eq 'true') { $parametros.SinPublicar = $true }
$parametros.Instalador = $true   # el botón siempre arma y adjunta PuntoVentaSetup.exe
& (Join-Path $PSScriptRoot 'publicar_version.ps1') @parametros
exit $LASTEXITCODE
