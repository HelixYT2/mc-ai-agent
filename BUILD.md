# Build Instructions

This guide will help you build the Minecraft AI Agent from source.

## Prerequisites

- **Node.js** (v18 or later) - [Download](https://nodejs.org/)
- **npm** (comes with Node.js)
- **Java JDK 21** - [Download](https://adoptium.net/)
- **Git** - [Download](https://git-scm.com/)

## Building the Desktop App

### 1. Clone the Repository

```bash
git clone https://github.com/HelixYT2/mc-ai-agent.git
cd mc-ai-agent
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Run in Development Mode

```bash
npm run dev
```

This will start the Electron app in development mode with DevTools open.

### 4. Build for Windows

```bash
npm run build
```

The built `.exe` file will be in the `dist` folder.

### 5. Build for All Platforms (Optional)

```bash
npm run build-all
```

This builds for Windows, macOS, and Linux.

## Building the Minecraft Mod

### 1. Navigate to Mod Directory

```bash
cd minecraft-mod
```

### 2. Build with Gradle

#### Windows
```bash
gradlew build
```

#### macOS/Linux
```bash
./gradlew build
```

### 3. Locate the Built Mod

The compiled mod JAR file will be in:
```
minecraft-mod/build/libs/mc-ai-agent-mod-1.0.0.jar
```

### 4. Install the Mod

Copy the JAR file to your Minecraft mods folder:
- **Default Launcher**: `%appdata%\.minecraft\mods\`
- **Prism Launcher**: `<instance_folder>\.minecraft\mods\`

## Development Setup

### Hot Reload for Desktop App

When running `npm run dev`, the Electron app will reload when you make changes to files.

### Debugging the Mod

1. Set up a development environment in your IDE (IntelliJ IDEA recommended)
2. Use the `runClient` Gradle task to launch Minecraft with the mod

```bash
cd minecraft-mod
./gradlew runClient
```

This opens a development Minecraft instance with your mod loaded.

## Troubleshooting

### Electron Build Issues

**Issue**: `electron-builder` fails to build
- **Solution**: Make sure you have all dependencies installed: `npm install`
- Try clearing cache: `npm cache clean --force`

### Gradle Build Issues

**Issue**: Java version mismatch
- **Solution**: Ensure you have JDK 21 installed
- Set `JAVA_HOME` environment variable to JDK 21 path

**Issue**: Fabric mappings not found
- **Solution**: Make sure you're connected to the internet (Gradle downloads mappings)
- Try running `./gradlew clean` then `./gradlew build`

### Mod Not Loading

**Issue**: Mod doesn't appear in Minecraft
- **Solution**: 
  - Check you have Fabric Loader installed for 1.21.5
  - Ensure Fabric API is installed
  - Check Minecraft logs for errors

## Project Structure

```
mc-ai-agent/
├── src/
│   ├── main/              # Electron main process
│   │   └── main.js
│   ├── renderer/          # UI (HTML, CSS, JS)
│   │   ├── index.html
│   │   ├── styles.css
│   │   └── renderer.js
│   └── backend/           # Node.js backend
│       ├── server.js
│       └── minecraft-detector.js
├── minecraft-mod/         # Fabric mod
│   ├── src/main/java/
│   ├── build.gradle
│   └── gradle.properties
├── assets/               # Icons and images
├── package.json          # npm configuration
└── README.md            # User documentation
```

## Next Steps

After building:
1. Install LM Studio and download the hermes-3-llama-3.1-8b model
2. Install the mod in Minecraft
3. Run the desktop app
4. Start LM Studio server
5. Launch Minecraft and connect!

## Need Help?

- Check the main [README.md](README.md) for usage instructions
- Open an issue on GitHub
- Check the troubleshooting section

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request
