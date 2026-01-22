# System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MINECRAFT AI AGENT SYSTEM                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                              USER INTERFACE LAYER                             │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │              ELECTRON DESKTOP APP (Windows .exe)                   │    │
│   │                                                                    │    │
│   │  ┌────────────────────┐  ┌────────────────────┐  ┌─────────────┐ │    │
│   │  │  Instance Selector │  │   Prompt Input     │  │  Settings   │ │    │
│   │  │  - Detect running  │  │  - Natural lang.   │  │  - AI Mode  │ │    │
│   │  │  - Select instance │  │  - Execute button  │  │  - Temp     │ │    │
│   │  │  - Status display  │  │  - Stop button     │  │  - URLs     │ │    │
│   │  └────────────────────┘  └────────────────────┘  └─────────────┘ │    │
│   │                                                                    │    │
│   │  ┌────────────────────┐  ┌─────────────────────────────────────┐ │    │
│   │  │   Video Display    │  │      Activity Log                    │ │    │
│   │  │   (Placeholder)    │  │      - Real-time updates             │ │    │
│   │  │                    │  │      - Color-coded messages          │ │    │
│   │  └────────────────────┘  └─────────────────────────────────────┘ │    │
│   │                                                                    │    │
│   │   Files: src/renderer/index.html, styles.css, renderer.js        │    │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                        │                                     │
│                                        │ IPC (Electron)                      │
│                                        ▼                                     │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                  MAIN PROCESS (Electron)                           │   │
│   │                                                                     │   │
│   │   - Window management                                              │   │
│   │   - IPC handlers                                                   │   │
│   │   - Backend server initialization                                  │   │
│   │                                                                     │   │
│   │   File: src/main/main.js                                           │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                               │
└──────────────────────────────────────────────────────────────────────────────┘
                                         │
                                         │ In-Process
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           ORCHESTRATION LAYER                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │               NODE.JS BACKEND SERVER                                │   │
│   │                                                                     │   │
│   │  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │   │
│   │  │  WebSocket       │  │  LM Studio       │  │  Task Planner   │ │   │
│   │  │  Server          │  │  API Client      │  │                 │ │   │
│   │  │  (Port 9876)     │  │                  │  │  - Parse AI     │ │   │
│   │  │                  │  │  - POST /chat    │  │  - Convert to   │ │   │
│   │  │  - Accept mods   │  │  - Parse JSON    │  │    actions      │ │   │
│   │  │  - Send actions  │  │  - Stream resp.  │  │  - Queue mgmt   │ │   │
│   │  └──────────────────┘  └──────────────────┘  └─────────────────┘ │   │
│   │                                                                     │   │
│   │  ┌──────────────────┐  ┌──────────────────────────────────────┐  │   │
│   │  │  Instance        │  │  State Manager                        │  │   │
│   │  │  Detector        │  │  - Track connections                  │  │   │
│   │  │                  │  │  - Monitor progress                   │  │   │
│   │  │  - Scan processes│  │  - Handle errors                      │  │   │
│   │  │  - Find Minecraft│  │                                        │  │   │
│   │  └──────────────────┘  └──────────────────────────────────────┘  │   │
│   │                                                                     │   │
│   │   Files: src/backend/server.js, minecraft-detector.js             │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                               │
└──────────────────────────────────────────────────────────────────────────────┘
                │                                      │
                │ WebSocket                            │ HTTP
                │ (ws://localhost:9876)                │ (http://localhost:1234)
                ▼                                      ▼
┌────────────────────────────────┐    ┌────────────────────────────────────────┐
│      GAME EXECUTION LAYER       │    │         AI MODEL LAYER                │
├────────────────────────────────┤    ├────────────────────────────────────────┤
│                                 │    │                                        │
│  ┌──────────────────────────┐  │    │  ┌──────────────────────────────────┐ │
│  │  MINECRAFT FABRIC MOD    │  │    │  │        LM STUDIO                 │ │
│  │  (Minecraft 1.21.5)      │  │    │  │                                  │ │
│  │                          │  │    │  │  ┌────────────────────────────┐ │ │
│  │  ┌────────────────────┐ │  │    │  │  │  hermes-3-llama-3.1-8b     │ │ │
│  │  │  WebSocket Client  │ │  │    │  │  │                            │ │ │
│  │  │  - Connect to      │ │  │    │  │  │  - Natural language        │ │ │
│  │  │    backend         │ │  │    │  │  │  - Task decomposition      │ │ │
│  │  │  - Receive actions │ │  │    │  │  │  - JSON output             │ │ │
│  │  │  - Send state      │ │  │    │  │  └────────────────────────────┘ │ │
│  │  └────────────────────┘ │  │    │  │                                  │ │
│  │                          │  │    │  │  OpenAI-compatible API           │ │
│  │  ┌────────────────────┐ │  │    │  │  - /v1/chat/completions          │ │
│  │  │  Action Executor   │ │  │    │  └──────────────────────────────────┘ │
│  │  │                    │ │  │    │                                        │
│  │  │  - Queue actions   │ │  │    │  Running locally on user machine     │
│  │  │  - Execute tasks   │ │  │    │  Port: 1234 (default)                │
│  │  │  - Report progress │ │  │    │                                        │
│  │  └────────────────────┘ │  │    └────────────────────────────────────────┘
│  │           │              │  │
│  │           ▼              │  │
│  │  ┌────────────────────┐ │  │
│  │  │  Automation        │ │  │
│  │  │  Systems           │ │  │
│  │  │                    │ │  │
│  │  │  ┌──────────────┐ │ │  │
│  │  │  │  Baritone    │ │ │  │
│  │  │  │  Integration │ │ │  │
│  │  │  │  - Pathfind  │ │ │  │
│  │  │  │  - Mining    │ │ │  │
│  │  │  └──────────────┘ │ │  │
│  │  │                    │ │  │
│  │  │  ┌──────────────┐ │ │  │
│  │  │  │  Crafting    │ │ │  │
│  │  │  │  Automation  │ │ │  │
│  │  │  └──────────────┘ │ │  │
│  │  │                    │ │  │
│  │  │  ┌──────────────┐ │ │  │
│  │  │  │  Smelting    │ │ │  │
│  │  │  │  Automation  │ │ │  │
│  │  │  └──────────────┘ │ │  │
│  │  └────────────────────┘ │  │
│  │                          │  │
│  │  ┌────────────────────┐ │  │
│  │  │  State Manager     │ │  │
│  │  │  - Position        │ │  │
│  │  │  - Health/Hunger   │ │  │
│  │  │  - Inventory       │ │  │
│  │  │  - Game mode       │ │  │
│  │  └────────────────────┘ │  │
│  │                          │  │
│  │  Files:                  │  │
│  │  - MCAIAgentMod.java     │  │
│  │  - ActionExecutor.java   │  │
│  │  - WebSocketClient.java  │  │
│  │  - StateManager.java     │  │
│  │  - automation/*.java     │  │
│  └──────────────────────────┘  │
│                                 │
│  Installed in Minecraft         │
│  mods folder as .jar            │
│                                 │
└─────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════
                               DATA FLOW
═══════════════════════════════════════════════════════════════════════════════

1. USER types "get full diamonds" in Electron UI
   ↓
2. UI sends to Main Process via IPC
   ↓
3. Main Process forwards to Backend Server
   ↓
4. Backend queries LM Studio API (HTTP POST)
   ↓
5. LM Studio processes with AI model
   ↓
6. AI returns action plan in JSON
   {
     "plan": [
       {"type": "goto", "target": "diamond_ore"},
       {"type": "mine", "target": "diamond_ore", "quantity": 5}
     ]
   }
   ↓
7. Backend converts to action sequence
   ↓
8. Backend sends to Minecraft Mod via WebSocket
   {
     "type": "execute_action",
     "action": {"type": "goto", ...}
   }
   ↓
9. Mod receives action and queues it
   ↓
10. ActionExecutor processes queue
    ↓
11. Calls Baritone for pathfinding
    ↓
12. Baritone moves player in game
    ↓
13. Mod sends progress updates back
    {
      "type": "state_update",
      "data": {"position": {...}, ...}
    }
    ↓
14. Backend forwards to UI
    ↓
15. UI displays in Activity Log
    ↓
16. Task completes, success message shown


═══════════════════════════════════════════════════════════════════════════════
                            TECHNOLOGY STACK
═══════════════════════════════════════════════════════════════════════════════

Frontend:        HTML5, CSS3, Vanilla JavaScript
Desktop:         Electron 28.0
Backend:         Node.js with ws (WebSocket) and axios
Build:           Electron Builder (for .exe)
Mod Platform:    Fabric 1.21.5 (Minecraft Java Edition)
Mod Language:    Java 21
Mod Build:       Gradle
AI Model:        hermes-3-llama-3.1-8b via LM Studio
Pathfinding:     Baritone (optional dependency)
Communication:   WebSocket (mod↔backend), IPC (UI↔main), HTTP (backend↔AI)


═══════════════════════════════════════════════════════════════════════════════
                              FILE COUNT
═══════════════════════════════════════════════════════════════════════════════

Documentation:   6 files (README, QUICKSTART, BUILD, CONFIG, PROTOCOL, SUMMARY)
JavaScript:      4 files (main, renderer, server, detector)
Java:            7 files (mod classes + automation modules)
Config:          5 files (package.json, build.gradle, etc.)
Total LOC:       ~1,857 lines of code
HTML/CSS:        3 files (UI layout and styling)


═══════════════════════════════════════════════════════════════════════════════
