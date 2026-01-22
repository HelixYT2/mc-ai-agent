package com.helixyt2.mcaiagent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCAIAgentMod implements ClientModInitializer {
    public static final String MOD_ID = "mc-ai-agent-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static MCAIAgentMod instance;
    private WebSocketClient wsClient;
    private ActionExecutor actionExecutor;
    private StateManager stateManager;
    
    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing Minecraft AI Agent Mod");
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Initialize components
        actionExecutor = new ActionExecutor(client);
        stateManager = new StateManager();
        
        // Start WebSocket client
        wsClient = new WebSocketClient("ws://localhost:9876");
        wsClient.connect();
        
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(tickClient -> {
            if (tickClient.player != null) {
                // Update state periodically
                stateManager.tick(tickClient);
                
                // Execute queued actions
                actionExecutor.tick(tickClient);
            }
        });
        
        LOGGER.info("Minecraft AI Agent Mod initialized successfully");
    }
    
    public static MCAIAgentMod getInstance() {
        return instance;
    }
    
    public WebSocketClient getWebSocketClient() {
        return wsClient;
    }
    
    public ActionExecutor getActionExecutor() {
        return actionExecutor;
    }
    
    public StateManager getStateManager() {
        return stateManager;
    }
    
    public void shutdown() {
        if (wsClient != null) {
            wsClient.disconnect();
        }
    }
}
