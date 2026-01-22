package com.helixyt2.mcaiagent;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionExecutor {
    private final Queue<JsonObject> actionQueue = new ConcurrentLinkedQueue<>();
    private JsonObject currentAction = null;
    private boolean isExecuting = false;
    private int tickCounter = 0;
    
    public void queueAction(JsonObject action) {
        actionQueue.add(action);
        MCAIAgentMod.LOGGER.info("Action queued: " + action.get("type").getAsString());
    }
    
    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        
        // If not currently executing, get next action
        if (!isExecuting && !actionQueue.isEmpty()) {
            currentAction = actionQueue.poll();
            isExecuting = true;
            tickCounter = 0;
            MCAIAgentMod.LOGGER.info("Starting action: " + currentAction.get("type").getAsString());
        }
        
        // Execute current action
        if (isExecuting && currentAction != null) {
            boolean completed = executeAction(client, currentAction);
            
            if (completed) {
                String actionId = currentAction.has("id") ? currentAction.get("id").getAsString() : "unknown";
                JsonObject result = new JsonObject();
                result.addProperty("success", true);
                
                MCAIAgentMod.getInstance().getWebSocketClient().sendActionComplete(actionId, result);
                MCAIAgentMod.LOGGER.info("Action completed: " + currentAction.get("type").getAsString());
                
                isExecuting = false;
                currentAction = null;
            }
        }
        
        tickCounter++;
    }
    
    private boolean executeAction(MinecraftClient client, JsonObject action) {
        String type = action.get("type").getAsString();
        ClientPlayerEntity player = client.player;
        
        try {
            switch (type) {
                case "goto":
                    return executeGoto(client, action);
                    
                case "mine":
                    return executeMine(client, action);
                    
                case "place":
                    return executePlace(client, action);
                    
                case "craft":
                    return executeCraft(client, action);
                    
                case "smelt":
                    return executeSmelt(client, action);
                    
                case "interact":
                    return executeInteract(client, action);
                    
                case "chat":
                    return executeChat(client, action);
                    
                default:
                    MCAIAgentMod.LOGGER.warn("Unknown action type: " + type);
                    return true; // Skip unknown actions
            }
        } catch (Exception e) {
            MCAIAgentMod.LOGGER.error("Error executing action: " + type, e);
            String actionId = action.has("id") ? action.get("id").getAsString() : "unknown";
            MCAIAgentMod.getInstance().getWebSocketClient().sendActionFailed(actionId, e.getMessage());
            return true; // Move to next action
        }
    }
    
    private boolean executeGoto(MinecraftClient client, JsonObject action) {
        // Use Baritone for pathfinding
        // This would integrate with Baritone API
        // For now, just a placeholder
        MCAIAgentMod.LOGGER.info("Goto action - would use Baritone here");
        return tickCounter > 20; // Simulate completion after 1 second
    }
    
    private boolean executeMine(MinecraftClient client, JsonObject action) {
        // Mining logic
        // This would use Baritone's mining capabilities
        MCAIAgentMod.LOGGER.info("Mine action - would use Baritone mining");
        return tickCounter > 40; // Simulate completion
    }
    
    private boolean executePlace(MinecraftClient client, JsonObject action) {
        // Block placement logic
        MCAIAgentMod.LOGGER.info("Place action");
        return tickCounter > 10;
    }
    
    private boolean executeCraft(MinecraftClient client, JsonObject action) {
        // Crafting automation
        String recipe = action.has("recipe") ? action.get("recipe").getAsString() : "unknown";
        MCAIAgentMod.LOGGER.info("Craft action: " + recipe);
        
        // Would integrate with CraftingAutomation class
        return tickCounter > 60;
    }
    
    private boolean executeSmelt(MinecraftClient client, JsonObject action) {
        // Smelting automation
        MCAIAgentMod.LOGGER.info("Smelt action");
        
        // Would integrate with SmeltingAutomation class
        return tickCounter > 100;
    }
    
    private boolean executeInteract(MinecraftClient client, JsonObject action) {
        // Entity/block interaction
        MCAIAgentMod.LOGGER.info("Interact action");
        return tickCounter > 5;
    }
    
    private boolean executeChat(MinecraftClient client, JsonObject action) {
        // Send chat message
        String message = action.has("message") ? action.get("message").getAsString() : "";
        if (client.player != null && !message.isEmpty()) {
            client.player.networkHandler.sendChatMessage(message);
        }
        return true; // Instant completion
    }
    
    public void stopAll() {
        actionQueue.clear();
        currentAction = null;
        isExecuting = false;
        MCAIAgentMod.LOGGER.info("All actions stopped");
    }
    
    public boolean isExecuting() {
        return isExecuting;
    }
    
    public int getQueueSize() {
        return actionQueue.size();
    }
}
