# Minecraft AI Agent Configuration

This file documents the configuration options available in the app.

## Desktop App Settings

### LM Studio Configuration

```javascript
{
  "lmStudioUrl": "http://localhost:1234",  // LM Studio API endpoint
  "model": "hermes-3-llama-3.1-8b",        // AI model to use
  "temperature": 0.7,                       // Creativity (0.0 - 1.0)
  "maxTokens": 2000                        // Max response length
}
```

### AI Mode

Choose how the AI plans and executes tasks:

- **Hybrid** (Recommended): AI provides high-level goals with some specific actions
  - Best for: General use, complex tasks
  - Example: "Get diamonds" → AI says "mine to Y=11, find diamond ore, mine 5 blocks"

- **High-Level**: AI provides only goals, mod figures out details
  - Best for: When you want the mod to handle implementation
  - Example: "Get diamonds" → AI says "obtain 5 diamonds"

- **Low-Level**: AI plans every single action
  - Best for: Precise control, debugging
  - Example: "Get diamonds" → AI says "move forward 10 blocks, turn left, dig down..."

### WebSocket Configuration

```javascript
{
  "backendPort": 9876,           // Port for mod communication
  "reconnectInterval": 5000,     // Auto-reconnect delay (ms)
  "heartbeatInterval": 30000     // Keep-alive ping interval (ms)
}
```

### Instance Detection

```javascript
{
  "scanInterval": 10000,         // How often to scan for instances (ms)
  "processName": "javaw.exe",    // Process to look for (Windows)
  "autoConnect": false           // Auto-connect to first instance found
}
```

## Minecraft Mod Configuration

### State Update Frequency

The mod sends state updates to the backend. Configure frequency:

```java
// In StateManager.java
stateManager.setUpdateInterval(20);  // Update every 20 ticks (1 second)
```

Options:
- `20` = 1 second (default, recommended)
- `10` = 0.5 seconds (more responsive, more network traffic)
- `40` = 2 seconds (less network traffic)

### Action Timeout

How long to wait for an action to complete:

```java
// In ActionExecutor.java
private static final int ACTION_TIMEOUT = 60000;  // 60 seconds
```

### Baritone Settings

When Baritone is available, you can configure:

```java
// Example Baritone settings (when integrated)
BaritoneAPI.getSettings().allowBreak.value = true;
BaritoneAPI.getSettings().allowPlace.value = true;
BaritoneAPI.getSettings().allowSprint.value = true;
BaritoneAPI.getSettings().allowParkour.value = false;  // Safer
```

## System Prompts

Customize how the AI understands its capabilities.

### Default System Prompt

Located in `src/backend/server.js`, function `buildSystemPrompt()`:

```javascript
const systemPrompt = `You are an AI assistant that controls a Minecraft player.

Available capabilities:
- Movement and pathfinding (using Baritone)
- Mining blocks
- Placing blocks
- Crafting items
- Smelting items
- Managing inventory
- Interacting with entities and blocks

Current world state:
- Position: ${state.position}
- Health: ${state.health}
- Inventory: ${state.inventory}

...
`;
```

### Custom Prompts for Specific Tasks

You can add specialized prompts for different task types:

```javascript
const prompts = {
  mining: "You are a mining specialist. Focus on efficiency...",
  building: "You are a builder. Consider aesthetics...",
  survival: "You are in survival mode. Prioritize safety...",
};
```

## Advanced Configuration

### Enable Debug Logging

In the desktop app:
```javascript
// Set in src/main/main.js
const DEBUG = true;
```

In the mod:
```java
// Add to MCAIAgentMod.java
public static final boolean DEBUG = true;
```

### Custom Action Handlers

Add new action types in `ActionExecutor.java`:

```java
case "custom_action":
    return executeCustomAction(client, action);
```

### Video Capture Settings

Configure window capture (when implemented):
```javascript
{
  "captureEnabled": true,
  "captureRate": 30,           // FPS
  "captureQuality": "medium",  // low, medium, high
  "captureWidth": 1280,
  "captureHeight": 720
}
```

## Performance Tuning

### For Low-End Systems

```javascript
{
  "aiMode": "high-level",       // Less processing
  "temperature": 0.3,           // More deterministic
  "stateUpdateInterval": 40,    // Less frequent updates
  "captureEnabled": false       // Disable video
}
```

### For High-End Systems

```javascript
{
  "aiMode": "hybrid",
  "temperature": 0.7,
  "stateUpdateInterval": 10,
  "captureEnabled": true,
  "captureQuality": "high"
}
```

## Security Settings

### API Key Protection (Future Feature)

```javascript
{
  "requireApiKey": true,
  "apiKey": "your-secret-key-here"
}
```

### Whitelist Actions (Safety)

```javascript
{
  "allowedActions": [
    "goto", "mine", "craft", "smelt", "interact"
  ],
  "blockedActions": [
    "give", "gamemode"  // Prevent cheating commands
  ]
}
```

## Environment Variables

You can also use environment variables:

```bash
# Windows (Command Prompt)
set LM_STUDIO_URL=http://localhost:1234
set BACKEND_PORT=9876

# Windows (PowerShell)
$env:LM_STUDIO_URL="http://localhost:1234"
$env:BACKEND_PORT="9876"

# Linux/Mac
export LM_STUDIO_URL=http://localhost:1234
export BACKEND_PORT=9876
```

## Resetting Configuration

To reset to defaults:
1. Close the app
2. Delete settings file (location TBD - will be in app data folder)
3. Restart the app

---

For more information, see the main [README.md](README.md).
