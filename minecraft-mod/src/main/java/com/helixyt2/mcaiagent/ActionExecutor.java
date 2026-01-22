package com.helixyt2.mcaiagent;

import com.google.gson.JsonObject;
import com.helixyt2.mcaiagent.automation.BaritoneIntegration;
import com.helixyt2.mcaiagent.automation.CraftingAutomation;
import com.helixyt2.mcaiagent.automation.SmeltingAutomation;
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
    
    private final BaritoneIntegration baritone;
    private final CraftingAutomation crafting;
    private final SmeltingAutomation smelting;
    private final BaritoneIntegration baritone;
    private final CraftingAutomation crafting;
    private final SmeltingAutomation smelting;
    
    public ActionExecutor(MinecraftClient client) {
        this.baritone = new BaritoneIntegration(client);
        this.crafting = new CraftingAutomation(client);
        this.smelting = new SmeltingAutomation(client);
    }
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
        if (tickCounter == 1) { // Start pathfinding on first tick
            int x = action.has("x") ? action.get("x").getAsInt() : 0;
            int y = action.has("y") ? action.get("y").getAsInt() : 64;
            int z = action.has("z") ? action.get("z").getAsInt() : 0;
            
            BlockPos target = new BlockPos(x, y, z);
            baritone.goToPosition(target);
            MCAIAgentMod.LOGGER.info("Started pathfinding to " + target);
        }
        
        // Check if we've arrived
        if (!baritone.isActive()) {
            return true; // Baritone finished
        }
        
        return tickCounter > 600; // Timeout after 30 seconds
    }
    
    private boolean executeMine(MinecraftClient client, JsonObject action) {
        // Mining logic using Baritone
        if (tickCounter == 1) {
            String blockType = action.has("target") ? action.get("target").getAsString() : "stone";
            int quantity = action.has("quantity") ? action.get("quantity").getAsInt() : 1;
            
            baritone.mineBlock(blockType, quantity);
            MCAIAgentMod.LOGGER.info("Started mining " + quantity + " " + blockType);
        }
        
        // Check if mining is complete
        if (!baritone.isActive()) {
            return true;
        }
        
        return tickCounter > 1200; // Timeout after 60 seconds
    }
    
    private boolean executePlace(MinecraftClient client, JsonObject action) {
        // Block placement logic
        MCAIAgentMod.LOGGER.info("Place action");
        return tickCounter > 10;
    }
    
    private boolean executeCraft(MinecraftClient client, JsonObject action) {
        // Crafting automation
        String recipe = action.has("recipe") ? action.get("recipe").getAsString() : "unknown";
        int quantity = action.has("quantity") ? action.get("quantity").getAsInt() : 1;
        
        if (tickCounter == 1) {
            crafting.startCrafting(recipe, quantity);
            MCAIAgentMod.LOGGER.info("Started crafting " + quantity + " " + recipe);
        }
        
        // Tick the crafting automation
        if (crafting.isActive()) {
            return crafting.tick();
        }
        
        return tickCounter > 600; // Timeout
    }
    
    private boolean executeSmelt(MinecraftClient client, JsonObject action) {
        // Smelting automation
        String item = action.has("item") ? action.get("item").getAsString() : "iron_ore";
        int quantity = action.has("quantity") ? action.get("quantity").getAsInt() : 1;
        
        if (tickCounter == 1) {
            smelting.startSmelting(item, quantity);
            MCAIAgentMod.LOGGER.info("Started smelting " + quantity + " " + item);
        }
        
        // Tick the smelting automation
        if (smelting.isActive()) {
            return smelting.tick();
        }
        
        return tickCounter > 1200; // Timeout
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
        
        // Stop automation systems
        baritone.stop();
        
        MCAIAgentMod.LOGGER.info("All actions stopped");
    }
    
    public boolean isExecuting() {
        return isExecuting;
    }
    
    public int getQueueSize() {
        return actionQueue.size();
    }
}
