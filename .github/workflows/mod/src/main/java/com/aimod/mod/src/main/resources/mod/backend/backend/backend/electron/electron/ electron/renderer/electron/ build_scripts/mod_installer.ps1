Param (
  [Parameter(Mandatory=$false)][string]$ModJarPath = "..\mod\build\libs\AIHandlerMod-1.0.0.jar",
  [Parameter(Mandatory=$false)][string]$MinecraftFolder = "$env:APPDATA\.minecraft",
  [Parameter(Mandatory=$false)][string]$AuthToken = ""
)

if (-not (Test-Path $ModJarPath)) {
  Write-Error "Mod jar not found at $ModJarPath. Build the mod first."
  exit 1
}

$modsFolder = Join-Path $MinecraftFolder "mods"
if (-not (Test-Path $modsFolder)) {
  New-Item -ItemType Directory -Path $modsFolder | Out-Null
}
Write-Host "Will install mod to $modsFolder"
$yn = Read-Host "Proceed? (y/n)"
if ($yn -ne "y") { Write-Host "Cancelled."; exit 0 }

Copy-Item $ModJarPath -Destination $modsFolder -Force
Write-Host "Copied $ModJarPath -> $modsFolder"

if ($AuthToken -ne "") {
  $configFolder = Join-Path $MinecraftFolder "config"
  if (-not (Test-Path $configFolder)) { New-Item -ItemType Directory -Path $configFolder | Out-Null }
  $modConfigPath = Join-Path $configFolder "aiagent_config.json"
  $obj = @{ auth_token = $AuthToken } | ConvertTo-Json
  Set-Content -Path $modConfigPath -Value $obj -Encoding UTF8
  Write-Host "Wrote auth token to $modConfigPath"
} else {
  Write-Host "No auth token provided; remember to edit mod config and put backend/config.json auth token in it."
}
Write-Host "Done. Start Minecraft with your Fabric profile and the mod should load."
