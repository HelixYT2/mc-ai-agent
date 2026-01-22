# Minecraft AI Agent

An AI-powered Minecraft automation system using Electron, LM Studio, and a custom Fabric mod.

## 🎮 Features

- **AI-Powered Automation**: Control your Minecraft character with natural language commands
- **Baritone Integration**: Advanced pathfinding and mining capabilities
- **Custom Automation**: Crafting, smelting, and complex task execution
- **Real-time Monitoring**: Live view of AI actions with video feed
- **Multi-Instance Support**: Detect and connect to multiple Minecraft instances
- **LM Studio Integration**: Uses hermes-3-llama-3.1-8b model locally

## 📋 Prerequisites

1. **Minecraft Java Edition 1.21.5** with Fabric Loader installed
2. **LM Studio** running locally on port 1234
3. **Windows OS** (for the .exe)
4. **32GB RAM recommended** for running AI model + Minecraft

## 🚀 Quick Start

### Step 1: Install LM Studio
1. Download LM Studio from https://lmstudio.ai/
2. Install and launch LM Studio
3. Download the `hermes-3-llama-3.1-8b` model
4. Start the local server on port 1234 (default)

### Step 2: Install the Mod
1. Make sure you have Fabric Loader installed for Minecraft 1.21.5
2. Copy `mc-ai-agent-mod.jar` from the `minecraft-mod/build/libs/` folder
3. Place it in your Minecraft `mods` folder:
   - For Prism Launcher: `.minecraft/mods/` in your instance folder
   - For default launcher: `%appdata%\.minecraft\mods\`
4. Make sure Fabric API is also installed

### Step 3: Run the Desktop App
1. Extract the downloaded zip file
2. Run `Minecraft AI Agent.exe`
3. The app will start and show the main interface

### Step 4: Connect and Use
1. Launch Minecraft 1.21.5 with Fabric and the mod installed
2. In the desktop app, click "🔄 Refresh" to detect your Minecraft instance
3. Click on your Minecraft instance to connect
4. Enter a prompt (e.g., "get full diamonds", "build a house", "mine 64 iron ore")
5. Click "🚀 Execute" and watch the AI work!

## 💡 Usage Examples

### Basic Commands
- `get full diamonds` - Mine diamonds and craft diamond gear
- `mine 64 iron ore` - Find and mine 64 iron ore
- `build a house` - Construct a basic house
- `farm wheat` - Plant and harvest wheat

### Complex Tasks
- `get full netherite armor` - Complete journey to get netherite
- `build automatic farm` - Create a redstone farm
- `enchant diamond pickaxe` - Get experience and enchant tools

## ⚙️ Settings

### AI Mode
- **Hybrid (Recommended)**: Mix of high-level goals and low-level actions
- **High-Level Commands**: AI sends goals, mod determines steps
- **Low-Level Actions**: AI plans every detail

### Temperature
- Lower (0.1-0.3): More deterministic, safer
- Medium (0.4-0.7): Balanced creativity
- Higher (0.8-1.0): More creative, experimental

## 🛠️ Development Setup

### Building from Source

#### Desktop App
```bash
# Install dependencies
npm install

# Run in development mode
npm run dev

# Build for Windows
npm run build
```

#### Minecraft Mod
```bash
cd minecraft-mod

# Build the mod
./gradlew build

# The built jar will be in build/libs/
```

## 📁 Project Structure

```
mc-ai-agent/
├── src/
│   ├── main/           # Electron main process
│   ├── renderer/       # UI (HTML, CSS, JS)
│   └── backend/        # Node.js backend server
├── minecraft-mod/      # Fabric mod source
│   └── src/main/java/
├── assets/            # Icons and resources
└── package.json       # Node.js configuration
```

## 🔧 Configuration

### LM Studio URL
By default, the app connects to `http://localhost:1234`. Change this in Settings if your LM Studio is on a different port.

### WebSocket Port
The backend server runs on port 9876. Make sure this port is not blocked by firewall.

## ⚠️ Important Notes

- **Single Player Only**: This tool is designed for singleplayer worlds
- **Resource Intensive**: Running the AI model requires significant RAM/GPU
- **Experimental**: This is an experimental project, bugs may occur
- **Backup Worlds**: Always backup your worlds before using automation

## 🐛 Troubleshooting

### Mod Not Connecting
1. Check that the mod is in the mods folder
2. Verify Fabric API is installed
3. Check Minecraft logs for errors
4. Ensure the desktop app is running

### LM Studio Not Responding
1. Verify LM Studio is running
2. Check that the server is started (port 1234)
3. Test the URL in settings
4. Make sure the model is loaded

### Can't Detect Minecraft Instance
1. Make sure Minecraft is actually running
2. Try clicking Refresh again
3. Check that you're running Java Edition (not Bedrock)
4. Verify the mod is loaded (check F3 menu in-game)

## 📝 License

MIT License - See LICENSE file for details

## 🙏 Credits

- **Baritone**: https://github.com/cabaletta/baritone
- **Fabric**: https://fabricmc.net/
- **LM Studio**: https://lmstudio.ai/
- **Electron**: https://www.electronjs.org/

## 🤝 Contributing

This is a personal project, but contributions are welcome! Please open issues or pull requests on GitHub.

## ⚡ Future Plans

- [ ] Multi-platform support (Mac, Linux)
- [ ] More automation modules
- [ ] Visual programming interface
- [ ] Replay system
- [ ] Multi-agent coordination
- [ ] Better video capture and streaming
