#!/bin/bash

echo "========================================"
echo "  Minecraft AI Agent - Build Script"
echo "========================================"
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "[ERROR] Node.js is not installed!"
    echo "Please install Node.js from https://nodejs.org/"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed!"
    echo "Please install Java JDK 21 from https://adoptium.net/"
    exit 1
fi

echo "[1/4] Installing Node.js dependencies..."
npm install
if [ $? -ne 0 ]; then
    echo "[ERROR] npm install failed!"
    exit 1
fi
echo "[✓] Dependencies installed"
echo ""

echo "[2/4] Building Minecraft mod..."
cd minecraft-mod
./gradlew build
if [ $? -ne 0 ]; then
    echo "[ERROR] Mod build failed!"
    cd ..
    exit 1
fi
cd ..
echo "[✓] Mod built successfully"
echo ""

echo "[3/4] Building Electron app..."
npm run build
if [ $? -ne 0 ]; then
    echo "[ERROR] Electron build failed!"
    exit 1
fi
echo "[✓] App built successfully"
echo ""

echo "[4/4] Creating distribution package..."
mkdir -p release
if [ -f minecraft-mod/build/libs/*.jar ]; then
    cp minecraft-mod/build/libs/*.jar release/ 2>/dev/null
fi
if [ -f dist/*.exe ] || [ -f dist/*.dmg ] || [ -f dist/*.AppImage ]; then
    cp dist/*.{exe,dmg,AppImage} release/ 2>/dev/null
fi
echo "[✓] Package created"
echo ""

echo "========================================"
echo "  Build Complete!"
echo "========================================"
echo ""
echo "Your files are in the 'release' folder:"
ls -lh release/
echo ""
echo "Next steps:"
echo "1. Install the .jar file into Minecraft mods folder"
echo "2. Run the executable to start the AI Agent"
echo ""
