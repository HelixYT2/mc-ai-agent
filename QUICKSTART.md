# Quick Start Guide

Get up and running with Minecraft AI Agent in minutes!

## Prerequisites Checklist

Before you begin, make sure you have:

- [ ] **Minecraft Java Edition 1.21.5** installed
- [ ] **Fabric Loader** installed for 1.21.5 ([Download](https://fabricmc.net/use/installer/))
- [ ] **Fabric API** mod installed ([Download](https://modrinth.com/mod/fabric-api))
- [ ] **LM Studio** installed and running ([Download](https://lmstudio.ai/))
- [ ] **hermes-3-llama-3.1-8b** model downloaded in LM Studio
- [ ] **32GB RAM** (recommended for AI model)

## Step-by-Step Installation

### 1. Setup LM Studio (5 minutes)

1. Open LM Studio
2. Go to "Discover" tab
3. Search for "hermes-3-llama-3.1-8b"
4. Click "Download"
5. Once downloaded, click "Chat" tab
6. Select the hermes model
7. Click the server icon (🌐) at the top
8. Click "Start Server" - it should show "Running on http://localhost:1234"

### 2. Install the Minecraft Mod (2 minutes)

#### Option A: Pre-built Mod (Easier)
1. Download `mc-ai-agent-mod-1.0.0.jar` from releases
2. Copy to your mods folder:
   - **Prism**: `instances/<your-instance>/.minecraft/mods/`
   - **Default**: `%appdata%\.minecraft\mods/`
3. Launch Minecraft 1.21.5 with Fabric

#### Option B: Build from Source
1. Navigate to `minecraft-mod` folder
2. Run `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)
3. Find JAR in `build/libs/`
4. Copy to mods folder as above

### 3. Run the Desktop App (1 minute)

#### Option A: Pre-built Executable
1. Download `Minecraft-AI-Agent-Setup.exe`
2. Run the installer
3. Launch "Minecraft AI Agent" from Start Menu

#### Option B: Run from Source
```bash
npm install
npm start
```

### 4. First Use (2 minutes)

1. **Start LM Studio** server (if not already running)
2. **Launch Minecraft** 1.21.5 with Fabric and the mod
3. **Open the AI Agent app**
4. Click **"🔄 Refresh"** to detect Minecraft
5. **Click on your Minecraft instance** to connect
6. Wait for "Connected" status (green badge)
7. **Type a command** like: "mine 10 stone"
8. Click **"🚀 Execute"**
9. Watch your character act autonomously!

## First Commands to Try

Start with simple commands to test the system:

### Basic Movement
```
go to coordinates 100, 64, 200
```

### Simple Mining
```
mine 10 stone
mine 5 oak wood
```

### Crafting (when you have materials)
```
craft 1 wooden pickaxe
craft 64 stick
```

### Complex Task
```
get 10 diamonds
```
This will:
- Find a cave or dig down to Y=11
- Search for diamond ore
- Mine the diamonds
- Return to surface

## Troubleshooting

### "No instances detected"
- ✅ Make sure Minecraft is actually running
- ✅ Verify you're running Java Edition (not Bedrock)
- ✅ Check that the mod is in the mods folder
- ✅ Look for mod in Minecraft mod list (press ESC → Mods)

### "Failed to connect to LM Studio"
- ✅ Open LM Studio and start the server
- ✅ Check it's on http://localhost:1234
- ✅ Try opening http://localhost:1234 in browser
- ✅ Make sure firewall isn't blocking it

### "Mod not responding"
- ✅ Check Minecraft logs for errors
- ✅ Verify Fabric API is installed
- ✅ Try restarting both Minecraft and the app
- ✅ Make sure WebSocket port 9876 is not blocked

### "AI does nothing"
- ✅ Check that you clicked "Execute" not just typed
- ✅ Look at the Activity Log for errors
- ✅ Make sure you're connected (green badge)
- ✅ Try a simpler command first

### Performance Issues
If the app or game is slow:
- Close other applications
- Lower Minecraft graphics settings
- In app settings, set AI Mode to "High-Level"
- Increase Temperature to make AI responses faster

## Tips for Best Results

1. **Start Simple**: Begin with basic commands before complex tasks
2. **Be Specific**: "mine 10 iron ore" works better than "get iron"
3. **Check Inventory**: Make sure you have tools/materials needed
4. **Monitor Logs**: Watch the Activity Log to understand what's happening
5. **Use Stop Button**: Don't hesitate to stop if something goes wrong

## What Can the AI Do?

✅ **Pathfinding**: Navigate to any location
✅ **Mining**: Find and mine specific blocks
✅ **Crafting**: Craft items (with materials)
✅ **Smelting**: Smelt ores in furnaces
✅ **Inventory**: Manage items
✅ **Building**: Place blocks (basic)

❌ **Cannot** (yet):
- Combat with mobs
- Complex redstone
- Enchanting
- Brewing potions

## Getting Help

- **Documentation**: See [README.md](README.md)
- **Configuration**: See [CONFIGURATION.md](CONFIGURATION.md)
- **Building**: See [BUILD.md](BUILD.md)
- **Issues**: Open a GitHub issue
- **Discord**: (Add link if you create one)

## Next Steps

Once you're comfortable with basic commands:
1. Try progressively more complex tasks
2. Experiment with different AI modes in settings
3. Adjust temperature for creativity vs. consistency
4. Share your cool automations!

---

**Enjoy automating Minecraft! 🎮🤖**
