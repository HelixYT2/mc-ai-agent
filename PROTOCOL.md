# Communication Protocol

This document describes the communication protocol between components of the Minecraft AI Agent system.

## Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│  Electron UI    │◄───────►│  Node.js Backend │◄───────►│ Minecraft Mod   │
│  (Renderer)     │   IPC   │  (WebSocket      │   WS    │ (Fabric/Java)   │
│                 │         │   Server)        │         │                 │
└─────────────────┘         └──────────────────┘         └─────────────────┘
                                     │
                                     │ HTTP
                                     ▼
                            ┌──────────────────┐
                            │   LM Studio      │
                            │   (AI Model)     │
                            └──────────────────┘
```

## 1. Electron UI ↔ Backend (IPC)

Uses Electron's IPC (Inter-Process Communication).

### Available Channels

#### `detect-instances`
Detect running Minecraft instances.

**Request:**
```javascript
ipcRenderer.invoke('detect-instances')
```

**Response:**
```javascript
[
  {
    id: "instance_12345",
    pid: 12345,
    name: "Minecraft 1.21.5",
    platform: "Java Edition",
    version: "1.21.5"
  }
]
```

#### `connect-instance`
Connect to a specific Minecraft instance.

**Request:**
```javascript
ipcRenderer.invoke('connect-instance', instanceId)
```

**Response:**
```javascript
{
  success: true,
  instanceId: "instance_12345"
}
```

#### `send-prompt`
Send a command prompt to be processed by AI.

**Request:**
```javascript
ipcRenderer.invoke('send-prompt', prompt, settings)
```

Parameters:
- `prompt`: String - The user's command
- `settings`: Object - Configuration
  ```javascript
  {
    mode: 'hybrid',              // 'high-level', 'low-level', 'hybrid'
    lmStudioUrl: 'http://localhost:1234',
    temperature: 0.7,
    model: 'hermes-3-llama-3.1-8b'
  }
  ```

**Response:**
```javascript
{
  success: true,
  taskId: "task_67890"
}
```

#### `stop-task`
Stop the currently executing task.

**Request:**
```javascript
ipcRenderer.invoke('stop-task')
```

**Response:**
```javascript
{
  success: true
}
```

#### `get-status`
Get current system status.

**Request:**
```javascript
ipcRenderer.invoke('get-status')
```

**Response:**
```javascript
{
  status: 'processing',           // 'idle', 'processing', 'complete', 'error'
  currentTask: { /* task info */ },
  connectedInstances: ['instance_12345'],
  activeInstance: 'instance_12345'
}
```

## 2. Backend ↔ Minecraft Mod (WebSocket)

WebSocket connection on `ws://localhost:9876`

### Message Format

All messages are JSON with a `type` field:

```javascript
{
  type: "message_type",
  // ... other fields
}
```

### Message Types (Backend → Mod)

#### `registered`
Confirmation that mod successfully registered.

```javascript
{
  type: "registered",
  success: true
}
```

#### `execute_action`
Execute a specific action.

```javascript
{
  type: "execute_action",
  action: {
    id: "action_123",
    type: "goto",        // Action type
    x: 100,             // Action-specific parameters
    y: 64,
    z: 200
  }
}
```

**Action Types:**
- `goto`: Navigate to coordinates
  - Parameters: `x`, `y`, `z`
- `mine`: Mine a block type
  - Parameters: `target` (block name), `quantity`
- `place`: Place a block
  - Parameters: `block` (block name), `x`, `y`, `z`
- `craft`: Craft an item
  - Parameters: `recipe` (item name), `quantity`
- `smelt`: Smelt items
  - Parameters: `item` (item name), `quantity`
- `interact`: Interact with block/entity
  - Parameters: `target` (entity/block), `x`, `y`, `z`
- `chat`: Send chat message
  - Parameters: `message`

#### `stop`
Stop all current actions.

```javascript
{
  type: "stop"
}
```

### Message Types (Mod → Backend)

#### `register`
Mod registers itself with backend.

```javascript
{
  type: "register",
  instanceId: "minecraft_1234567890",
  version: "1.21.5"
}
```

#### `state_update`
Send current game state.

```javascript
{
  type: "state_update",
  instanceId: "minecraft_1234567890",
  data: {
    position: { x: 100, y: 64, z: 200 },
    health: 20,
    hunger: 20,
    saturation: 5.0,
    inventory: [
      { slot: 0, item: "minecraft:diamond_pickaxe", count: 1 },
      { slot: 1, item: "minecraft:torch", count: 64 }
    ],
    dimension: "minecraft:overworld",
    gameMode: "survival"
  }
}
```

#### `action_complete`
Action completed successfully.

```javascript
{
  type: "action_complete",
  instanceId: "minecraft_1234567890",
  actionId: "action_123",
  result: {
    success: true,
    // Action-specific result data
  }
}
```

#### `action_failed`
Action failed to complete.

```javascript
{
  type: "action_failed",
  instanceId: "minecraft_1234567890",
  actionId: "action_123",
  error: "Could not find path to destination"
}
```

#### `log`
Log message from mod.

```javascript
{
  type: "log",
  instanceId: "minecraft_1234567890",
  message: "Started mining diamond_ore"
}
```

## 3. Backend ↔ LM Studio (HTTP)

Uses LM Studio's OpenAI-compatible API.

### Endpoint

```
POST http://localhost:1234/v1/chat/completions
```

### Request Format

```javascript
{
  model: "hermes-3-llama-3.1-8b",
  messages: [
    {
      role: "system",
      content: "You are an AI assistant that controls a Minecraft player..."
    },
    {
      role: "user",
      content: "get full diamonds"
    }
  ],
  temperature: 0.7,
  max_tokens: 2000,
  stream: false
}
```

### Response Format

```javascript
{
  id: "chatcmpl-123",
  object: "chat.completion",
  created: 1234567890,
  model: "hermes-3-llama-3.1-8b",
  choices: [
    {
      index: 0,
      message: {
        role: "assistant",
        content: "{\"plan\": [{\"type\": \"goal\", \"description\": \"Find diamonds\"...}]}"
      },
      finish_reason: "stop"
    }
  ],
  usage: {
    prompt_tokens: 150,
    completion_tokens: 200,
    total_tokens: 350
  }
}
```

## 4. AI Response Format

The AI should respond with JSON containing actions.

### High-Level Mode

```javascript
{
  "commands": [
    { "action": "mine", "target": "diamond_ore", "quantity": 5 },
    { "action": "craft", "item": "diamond_pickaxe", "quantity": 1 }
  ]
}
```

### Low-Level Mode

```javascript
{
  "actions": [
    { "type": "goto", "x": 100, "y": 11, "z": 200 },
    { "type": "mine", "x": 100, "y": 11, "z": 200 },
    { "type": "open_inventory" },
    { "type": "craft", "recipe": "diamond_pickaxe" }
  ]
}
```

### Hybrid Mode

```javascript
{
  "plan": [
    {
      "type": "goal",
      "description": "Find and mine diamonds",
      "actions": [
        { "type": "goto", "target": "diamond_ore" },
        { "type": "mine", "target": "diamond_ore", "quantity": 5 }
      ]
    },
    {
      "type": "goal",
      "description": "Craft diamond pickaxe",
      "actions": [
        { "type": "craft", "recipe": "diamond_pickaxe" }
      ]
    }
  ]
}
```

## Error Handling

### WebSocket Errors

If WebSocket connection is lost:
1. Mod attempts to reconnect after 5 seconds
2. Backend keeps connection state for 30 seconds
3. After 30 seconds, connection is considered dead

### Action Timeouts

Each action has a timeout (default 60 seconds):
- If action doesn't complete, it's marked as failed
- Backend moves to next action
- Error is logged

### AI Errors

If LM Studio API fails:
- Error is shown in UI
- Task is marked as failed
- User can retry

## Security Considerations

### Current State
- WebSocket is unencrypted (localhost only)
- No authentication required
- All actions are allowed

### Future Enhancements
- Add API key authentication
- Encrypt WebSocket traffic
- Whitelist/blacklist actions
- Rate limiting

## Example Complete Flow

1. User types "get 5 diamonds" in UI
2. UI sends to backend via IPC
3. Backend queries LM Studio API
4. LM Studio returns action plan
5. Backend converts to action sequence
6. Backend sends actions to mod via WebSocket
7. Mod executes actions one by one
8. Mod sends progress updates back
9. Backend forwards to UI
10. UI shows progress in log
11. Task completes, UI shows success

---

For implementation details, see the source code in:
- `src/backend/server.js` - Backend server
- `src/renderer/renderer.js` - UI logic
- `minecraft-mod/src/main/java/com/helixyt2/mcaiagent/` - Mod code
