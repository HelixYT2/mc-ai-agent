```text
AIHandlerMod (Fabric 1.21.5) - Quick README

Purpose:
- Fabric client-side mod that exposes a local WebSocket API to let a desktop app control the client.
- Optional Baritone integration (if you place a compatible Baritone jar into mod/libs).
- Streams screenshots (base64 PNG) to the desktop app.

Quick dev setup (local):
1. Open the mod folder as a Gradle project (IntelliJ recommended).
2. Add Baritone jar to mod/libs if you have a compatible build for 1.21.5 (optional).
3. Build the mod: gradlew build
4. Copy the resulting jar from build/libs to %appdata%\.minecraft\mods
5. Create config %appdata%\.minecraft\config\aiagent_config.json with:
   { "auth_token": "<token-from-backend-config.json>" }
   (or set defaultAuth in AIHandlerMod.java for initial testing).
6. Start Minecraft with Fabric loader.

Notes:
- Screenshot code may need mapping-specific tweaks.
- Baritone integration in code is defensive; if Baritone isn't present, the mod won't crash.
```
