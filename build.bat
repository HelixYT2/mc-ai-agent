@echo off
echo ========================================
echo   Minecraft AI Agent - Build Script
echo ========================================
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Node.js is not installed!
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed!
    echo Please install Java JDK 21 from https://adoptium.net/
    pause
    exit /b 1
)

echo [1/4] Installing Node.js dependencies...
call npm install
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] npm install failed!
    pause
    exit /b 1
)
echo [✓] Dependencies installed
echo.

echo [2/4] Building Minecraft mod...
cd minecraft-mod
call gradlew build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Mod build failed!
    cd ..
    pause
    exit /b 1
)
cd ..
echo [✓] Mod built successfully
echo.

echo [3/4] Building Electron app...
call npm run build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Electron build failed!
    pause
    exit /b 1
)
echo [✓] App built successfully
echo.

echo [4/4] Creating distribution package...
if not exist "release" mkdir release
if exist "minecraft-mod\build\libs\*.jar" (
    copy "minecraft-mod\build\libs\*.jar" "release\" >nul
)
if exist "dist\*.exe" (
    copy "dist\*.exe" "release\" >nul
)
echo [✓] Package created
echo.

echo ========================================
echo   Build Complete!
echo ========================================
echo.
echo Your files are in the 'release' folder:
dir release
echo.
echo Next steps:
echo 1. Install the .jar file into Minecraft mods folder
echo 2. Run the .exe file to start the AI Agent
echo.
pause
