Write-Host "1) Build mod (Gradle)"
Push-Location mod
if (Test-Path "./gradlew") { .\gradlew build } else { gradle build }
Pop-Location

Write-Host "2) Build backend (PyInstaller)"
Push-Location backend
pip install -r requirements.txt
pip install pyinstaller
pyinstaller --onefile main.py --name backend
Pop-Location

Write-Host "3) Package Electron"
Push-Location electron
npm ci
npm run dist
Pop-Location

Write-Host "Build script finished. Check mod/build/libs, backend/dist/backend.exe, electron/dist."
