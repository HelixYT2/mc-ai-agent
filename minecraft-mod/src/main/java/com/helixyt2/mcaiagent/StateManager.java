package com.helixyt2.mcaiagent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class StateManager {
    private int updateInterval = 20; // Update every second (20 ticks)
    private int ticksSinceUpdate = 0;
    
    public void tick(MinecraftClient client) {
        ticksSinceUpdate++;
        
        if (ticksSinceUpdate >= updateInterval) {
            sendStateUpdate(client);
            ticksSinceUpdate = 0;
        }
    }
    
    private void sendStateUpdate(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        
        JsonObject state = new JsonObject();
        
        // Player position
        JsonObject position = new JsonObject();
        position.addProperty("x", player.getX());
        position.addProperty("y", player.getY());
        position.addProperty("z", player.getZ());
        state.add("position", position);
        
        // Player health and hunger
        state.addProperty("health", player.getHealth());
        state.addProperty("hunger", player.getHungerManager().getFoodLevel());
        state.addProperty("saturation", player.getHungerManager().getSaturationLevel());
        
        // Player inventory
        JsonArray inventory = new JsonArray();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                JsonObject item = new JsonObject();
                item.addProperty("slot", i);
                item.addProperty("item", stack.getItem().toString());
                item.addProperty("count", stack.getCount());
                inventory.add(item);
            }
        }
        state.add("inventory", inventory);
        
        // Dimension
        state.addProperty("dimension", player.getWorld().getRegistryKey().getValue().toString());
        
        // Game mode
        if (client.interactionManager != null) {
            state.addProperty("gameMode", client.interactionManager.getCurrentGameMode().getName());
        }
        
        // Send to backend
        MCAIAgentMod.getInstance().getWebSocketClient().sendStateUpdate(state);
    }
    
    public void setUpdateInterval(int ticks) {
        this.updateInterval = ticks;
    }
}
