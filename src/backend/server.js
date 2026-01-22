const WebSocket = require('ws');
const axios = require('axios');
const EventEmitter = require('events');

class BackendServer extends EventEmitter {
  constructor() {
    super();
    this.wss = null;
    this.modConnections = new Map(); // instanceId -> WebSocket
    this.currentTask = null;
    this.taskQueue = [];
    this.status = 'idle';
    this.port = 9876;
    this.lmStudioUrl = 'http://localhost:1234/v1'; // Default LM Studio port
  }

  async start() {
    this.wss = new WebSocket.Server({ port: this.port });
    
    this.wss.on('connection', (ws) => {
      console.log('[Backend] Mod connected');
      
      ws.on('message', (data) => {
        this.handleModMessage(ws, data);
      });

      ws.on('close', () => {
        console.log('[Backend] Mod disconnected');
        // Remove from connections
        for (let [id, conn] of this.modConnections) {
          if (conn === ws) {
            this.modConnections.delete(id);
            break;
          }
        }
      });

      ws.on('error', (error) => {
        console.error('[Backend] WebSocket error:', error);
      });
    });

    console.log(`[Backend] WebSocket server started on port ${this.port}`);
  }

  handleModMessage(ws, data) {
    try {
      const message = JSON.parse(data);
      
      switch (message.type) {
        case 'register':
          // Mod is registering itself with instance info
          this.modConnections.set(message.instanceId, ws);
          ws.instanceId = message.instanceId;
          ws.send(JSON.stringify({ type: 'registered', success: true }));
          console.log(`[Backend] Registered instance: ${message.instanceId}`);
          break;
          
        case 'state_update':
          // Mod is sending state information
          this.emit('state_update', message.instanceId, message.data);
          break;
          
        case 'action_complete':
          // Mod completed an action
          this.handleActionComplete(message);
          break;
          
        case 'action_failed':
          // Action failed
          this.handleActionFailed(message);
          break;
          
        case 'log':
          console.log(`[Mod ${message.instanceId}]`, message.message);
          this.emit('log', message);
          break;
          
        default:
          console.warn('[Backend] Unknown message type:', message.type);
      }
    } catch (error) {
      console.error('[Backend] Error handling message:', error);
    }
  }

  async connectToInstance(instanceId) {
    const connection = this.modConnections.get(instanceId);
    if (!connection) {
      return { success: false, error: 'Instance not found or mod not running' };
    }
    
    this.activeInstance = instanceId;
    return { success: true, instanceId };
  }

  async processPrompt(prompt, settings = {}) {
    if (!this.activeInstance) {
      return { success: false, error: 'No instance connected' };
    }

    this.status = 'processing';
    this.currentTask = {
      prompt,
      settings,
      startTime: Date.now(),
      actions: []
    };

    try {
      // Send prompt to LM Studio
      const aiResponse = await this.queryAI(prompt, settings);
      
      // Parse AI response into actions
      const actions = this.parseAIResponse(aiResponse, settings);
      
      // Send actions to mod
      await this.executeActions(actions);
      
      return { success: true, taskId: this.currentTask.id };
    } catch (error) {
      console.error('[Backend] Error processing prompt:', error);
      this.status = 'error';
      return { success: false, error: error.message };
    }
  }

  async queryAI(prompt, settings) {
    try {
      const systemPrompt = this.buildSystemPrompt(settings);
      
      const response = await axios.post(`${this.lmStudioUrl}/chat/completions`, {
        model: settings.model || 'hermes-3-llama-3.1-8b',
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: prompt }
        ],
        temperature: settings.temperature || 0.7,
        max_tokens: settings.maxTokens || 2000,
        stream: false
      });

      return response.data.choices[0].message.content;
    } catch (error) {
      console.error('[Backend] LM Studio API error:', error.message);
      throw new Error('Failed to communicate with LM Studio: ' + error.message);
    }
  }

  buildSystemPrompt(settings) {
    const mode = settings.mode || 'hybrid';
    
    let systemPrompt = `You are an AI assistant that controls a Minecraft player. Your goal is to help complete tasks in Minecraft.

Available capabilities:
- Movement and pathfinding (using Baritone)
- Mining blocks
- Placing blocks
- Crafting items
- Smelting items
- Managing inventory
- Interacting with entities and blocks

`;

    if (mode === 'high-level') {
      systemPrompt += `Output Format: Provide high-level commands in JSON format.
Example:
{
  "commands": [
    {"action": "mine", "target": "diamond_ore", "quantity": 5},
    {"action": "craft", "item": "diamond_pickaxe", "quantity": 1}
  ]
}`;
    } else if (mode === 'low-level') {
      systemPrompt += `Output Format: Provide detailed step-by-step actions in JSON format.
Example:
{
  "actions": [
    {"type": "goto", "x": 100, "y": 64, "z": 200},
    {"type": "mine", "x": 100, "y": 64, "z": 200},
    {"type": "open_inventory"},
    {"type": "craft", "recipe": "diamond_pickaxe"}
  ]
}`;
    } else {
      systemPrompt += `Output Format: Provide a mix of high-level goals and specific actions as needed in JSON format.
Example:
{
  "plan": [
    {"type": "goal", "description": "Find and mine 5 diamond ore", "actions": [
      {"type": "goto", "target": "diamond_ore"},
      {"type": "mine", "target": "diamond_ore", "quantity": 5}
    ]},
    {"type": "goal", "description": "Craft diamond pickaxe", "actions": [
      {"type": "craft", "recipe": "diamond_pickaxe"}
    ]}
  ]
}`;
    }

    return systemPrompt;
  }

  parseAIResponse(response, settings) {
    try {
      // Extract JSON from response
      const jsonMatch = response.match(/\{[\s\S]*\}/);
      if (!jsonMatch) {
        throw new Error('No valid JSON found in AI response');
      }
      
      const parsed = JSON.parse(jsonMatch[0]);
      
      // Convert to standardized action format
      if (parsed.commands) {
        return parsed.commands;
      } else if (parsed.actions) {
        return parsed.actions;
      } else if (parsed.plan) {
        // Flatten plan into actions
        return parsed.plan.flatMap(goal => goal.actions || []);
      }
      
      return [];
    } catch (error) {
      console.error('[Backend] Error parsing AI response:', error);
      throw new Error('Failed to parse AI response into actions');
    }
  }

  async executeActions(actions) {
    const connection = this.modConnections.get(this.activeInstance);
    if (!connection) {
      throw new Error('Lost connection to Minecraft instance');
    }

    for (const action of actions) {
      if (this.status === 'stopped') {
        console.log('[Backend] Task stopped by user');
        break;
      }

      await this.sendActionToMod(connection, action);
      
      // Wait for action to complete
      await this.waitForActionComplete(action);
    }

    this.status = 'complete';
    this.currentTask = null;
  }

  async sendActionToMod(connection, action) {
    return new Promise((resolve, reject) => {
      const message = {
        type: 'execute_action',
        action: action
      };

      connection.send(JSON.stringify(message), (error) => {
        if (error) {
          reject(error);
        } else {
          resolve();
        }
      });
    });
  }

  async waitForActionComplete(action, timeout = 60000) {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        reject(new Error('Action timeout'));
      }, timeout);

      const handler = (message) => {
        if (message.actionId === action.id) {
          clearTimeout(timer);
          this.removeListener('action_complete', handler);
          resolve();
        }
      };

      this.on('action_complete', handler);
    });
  }

  handleActionComplete(message) {
    console.log('[Backend] Action completed:', message.actionId);
    this.emit('action_complete', message);
    this.emit('progress', {
      type: 'action_complete',
      action: message.actionId,
      result: message.result
    });
  }

  handleActionFailed(message) {
    console.error('[Backend] Action failed:', message.actionId, message.error);
    this.emit('action_failed', message);
    this.emit('progress', {
      type: 'action_failed',
      action: message.actionId,
      error: message.error
    });
  }

  async stopCurrentTask() {
    if (this.currentTask) {
      this.status = 'stopped';
      
      // Send stop command to mod
      const connection = this.modConnections.get(this.activeInstance);
      if (connection) {
        connection.send(JSON.stringify({ type: 'stop' }));
      }
      
      this.currentTask = null;
      return { success: true };
    }
    return { success: false, error: 'No task running' };
  }

  async getStatus() {
    return {
      status: this.status,
      currentTask: this.currentTask,
      connectedInstances: Array.from(this.modConnections.keys()),
      activeInstance: this.activeInstance
    };
  }

  stop() {
    if (this.wss) {
      this.wss.close();
    }
  }
}

module.exports = BackendServer;
