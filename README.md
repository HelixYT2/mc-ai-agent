# Minecraft AI Agent

An AI-powered Minecraft automation system that uses natural language to control your character. Built with Electron, Node.js, and a custom Fabric mod for Minecraft 1.21.5.

## 🎮 Features

- **🤖 AI-Powered Control**: Use natural language to control your Minecraft character
- **🧠 Smart Planning**: AI breaks down complex tasks into actionable steps
- **🗺️ Advanced Pathfinding**: Integrated with Baritone for intelligent navigation
- **⚒️ Full Automation**: Mining, crafting, smelting, and more
- **📹 Live Monitoring**: Real-time activity logs and status updates
- **🎛️ Flexible Modes**: Choose between high-level goals or low-level control
- **💻 Multi-Instance**: Detect and connect to multiple Minecraft instances
- **🔌 Plug & Play**: Easy setup with pre-built executables

## 📸 Screenshots

*(UI Preview)*
- Modern gradient interface
- Real-time instance detection
- Activity logging
- Settings panel with AI mode selection

## 🚀 Quick Start

### Prerequisites
- ✅ Minecraft Java Edition 1.21.5 with Fabric Loader
- ✅ LM Studio with hermes-3-llama-3.1-8b model
- ✅ 32GB RAM recommended
- ✅ Windows OS (for .exe)

### Installation

1. **Install LM Studio** and download the hermes-3-llama-3.1-8b model
2. **Copy the mod** (`mc-ai-agent-mod-1.0.0.jar`) to your Minecraft mods folder
3. **Run the desktop app** (`Minecraft AI Agent.exe`)
4. **Launch Minecraft** with the mod installed
5. **Click "Refresh"** in the app to detect your instance
6. **Connect** to your Minecraft instance
7. **Enter a command** and click "Execute"!

See [QUICKSTART.md](QUICKSTART.md) for detailed instructions.

## 💡 Example Commands

```
mine 64 iron ore
get full diamonds
craft diamond pickaxe
build a house
farm wheat
smelt 32 iron ore
go to coordinates 100 64 200
```

## 🏗️ Architecture

```
Electron UI (React-like) ──IPC──> Node.js Backend ──WebSocket──> Minecraft Mod
                                         │
                                         └──HTTP──> LM Studio (AI)
```

### Components

1. **Electron Desktop App** (`src/`)
   - Beautiful gradient UI
   - Instance detection
   - Settings management
   - Activity logging

2. **Node.js Backend** (`src/backend/`)
   - WebSocket server
   - LM Studio integration
   - Task orchestration
   - Action planning

3. **Minecraft Mod** (`minecraft-mod/`)
   - Fabric mod for 1.21.5
   - Action executor
   - Baritone integration
   - State reporting

## 🛠️ Building from Source

### Desktop App
```bash
npm install
npm run build
```

### Minecraft Mod
```bash
cd minecraft-mod
./gradlew build
```

### Complete Build
```bash
# Windows
build.bat

# Mac/Linux
./build.sh
```

See [BUILD.md](BUILD.md) for detailed instructions.

See [BUILD.md](BUILD.md) for detailed instructions.

## ⚙️ Configuration

Customize the system to your needs. See [CONFIGURATION.md](CONFIGURATION.md) for details.

### AI Modes
- **Hybrid**: Best balance of control and automation
- **High-Level**: Let the mod figure out the details
- **Low-Level**: AI plans every action precisely

### Settings
- LM Studio URL (default: `http://localhost:1234`)
- Temperature (0.0 - 1.0)
- WebSocket port (default: 9876)
- State update frequency

## 📚 Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Get started in 5 minutes
- **[BUILD.md](BUILD.md)** - Build from source
- **[CONFIGURATION.md](CONFIGURATION.md)** - Configuration options
- **[PROTOCOL.md](PROTOCOL.md)** - Communication protocol details

## 🔒 Security & Safety

- ⚠️ **Singleplayer Only**: Designed for local worlds
- 🔐 WebSocket runs on localhost only
- 🛡️ No network exposure by default
- ⚡ Can be stopped at any time

## 🎯 Roadmap

### Current Features ✅
- Natural language commands
- Pathfinding with Baritone
- Mining automation
- Crafting system
- Smelting system
- Multi-instance support

### Planned Features 🚀
- [ ] Combat automation
- [ ] Redstone circuits
- [ ] Enchanting
- [ ] Brewing
- [ ] Building templates
- [ ] Multi-agent coordination
- [ ] Video capture/streaming
- [ ] Mac and Linux support

## 🐛 Troubleshooting

### Common Issues

**Mod not connecting?**
- Check Minecraft logs
- Verify Fabric API installed
- Ensure port 9876 not blocked

**LM Studio not responding?**
- Start LM Studio server
- Check http://localhost:1234
- Verify model is loaded

**Can't detect instance?**
- Make sure Minecraft is running
- Click Refresh
- Check you're using Java Edition

See [QUICKSTART.md](QUICKSTART.md) for more troubleshooting tips.

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Test your changes thoroughly
4. Submit a pull request

## 📝 License

MIT License - See [LICENSE](LICENSE) file for details.

## 🙏 Credits & Acknowledgments

### Built With
- **[Electron](https://www.electronjs.org/)** - Desktop app framework
- **[Node.js](https://nodejs.org/)** - Backend runtime
- **[Fabric](https://fabricmc.net/)** - Minecraft modding framework
- **[LM Studio](https://lmstudio.ai/)** - Local AI model hosting
- **[Baritone](https://github.com/cabaletta/baritone)** - Pathfinding library

### AI Model
- **hermes-3-llama-3.1-8b** by Nous Research

### Special Thanks
- Fabric community for modding support
- Baritone developers for pathfinding
- LM Studio team for local AI hosting

## 💬 Community & Support

- **GitHub Issues**: Report bugs and request features
- **Discussions**: Share your automations and ask questions
- **Discord**: (Coming soon)

## ⚖️ Legal & Ethics

- This tool is for **personal use in singleplayer**
- Do not use on servers where automation is prohibited
- Respect server rules and terms of service
- Always backup your worlds before using automation

## 🎓 Learn More

### For Users
- [Quick Start Guide](QUICKSTART.md)
- [Configuration Guide](CONFIGURATION.md)

### For Developers
- [Build Instructions](BUILD.md)
- [Protocol Documentation](PROTOCOL.md)
- [Source Code Structure](BUILD.md#project-structure)

## 📊 Project Status

**Version**: 1.0.0  
**Status**: Beta - Ready for testing  
**Minecraft**: 1.21.5 (Java Edition)  
**Platform**: Windows (Mac/Linux coming soon)

---

**Made with ❤️ for the Minecraft community**

*Star ⭐ this repo if you find it useful!*
