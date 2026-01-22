package com.helixyt2.mcaiagent.automation;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class CraftingAutomation {
    private MinecraftClient client;
    private int tickCounter = 0;
    private CraftingState state = CraftingState.IDLE;
    private String targetRecipe;
    private int targetQuantity;
    private int craftedCount = 0;
    
    private enum CraftingState {
        IDLE,
        OPENING_INVENTORY,
        OPENING_CRAFTING_TABLE,
        CRAFTING,
        COLLECTING_RESULT,
        COMPLETE
    }
    
    public CraftingAutomation(MinecraftClient client) {
        this.client = client;
    }
    
    public void startCrafting(String recipe, int quantity) {
        this.targetRecipe = recipe;
        this.targetQuantity = quantity;
        this.craftedCount = 0;
        this.state = CraftingState.OPENING_INVENTORY;
        this.tickCounter = 0;
    }
    
    public boolean tick() {
        tickCounter++;
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        
        switch (state) {
            case IDLE:
                return false;
                
            case OPENING_INVENTORY:
                // Open inventory/crafting table
                if (tickCounter > 5) {
                    openCraftingInterface();
                    state = CraftingState.CRAFTING;
                    tickCounter = 0;
                }
                break;
                
            case CRAFTING:
                // Perform the craft
                if (tickCounter > 10) {
                    boolean success = performCraft();
                    if (success) {
                        craftedCount++;
                        if (craftedCount >= targetQuantity) {
                            state = CraftingState.COMPLETE;
                        } else {
                            tickCounter = 0;
                        }
                    } else {
                        state = CraftingState.COMPLETE;
                    }
                }
                break;
                
            case COMPLETE:
                closeCraftingInterface();
                state = CraftingState.IDLE;
                return true;
        }
        
        return false;
    }
    
    private void openCraftingInterface() {
        // Try to open inventory for 2x2 crafting
        // For 3x3, would need to interact with crafting table
        ClientPlayerEntity player = client.player;
        if (player != null && client.currentScreen == null) {
            // Open inventory
            player.openInventory();
        }
    }
    
    private void closeCraftingInterface() {
        if (client.currentScreen != null) {
            client.currentScreen.close();
        }
    }
    
    private boolean performCraft() {
        // This is a simplified version
        // Real implementation would:
        // 1. Find the recipe
        // 2. Check if materials are available
        // 3. Place materials in crafting grid
        // 4. Take result
        
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        
        // Look up recipe
        Optional<RecipeEntry<?>> recipeOpt = findRecipe(targetRecipe);
        if (recipeOpt.isEmpty()) {
            return false;
        }
        
        // Check materials
        if (!hasMaterials(recipeOpt.get())) {
            return false;
        }
        
        // Simulate crafting (actual implementation would interact with slots)
        // This would use screen handlers and slot clicking
        
        return true;
    }
    
    private Optional<RecipeEntry<?>> findRecipe(String recipeName) {
        Identifier recipeId = Identifier.tryParse(recipeName);
        if (recipeId == null) {
            // Try to find by item name
            recipeId = new Identifier("minecraft", recipeName);
        }
        
        if (client.world == null) return Optional.empty();
        
        return client.world.getRecipeManager()
            .listAllOfType(RecipeType.CRAFTING)
            .stream()
            .filter(entry -> {
                ItemStack result = entry.value().getResult(client.world.getRegistryManager());
                return result.getItem().toString().contains(recipeName);
            })
            .findFirst();
    }
    
    private boolean hasMaterials(RecipeEntry<?> recipe) {
        // Check if player has required materials
        // Simplified version - would need to check recipe ingredients
        return true;
    }
    
    public boolean isActive() {
        return state != CraftingState.IDLE;
    }
    
    public JsonObject getProgress() {
        JsonObject progress = new JsonObject();
        progress.addProperty("state", state.name());
        progress.addProperty("craftedCount", craftedCount);
        progress.addProperty("targetQuantity", targetQuantity);
        return progress;
    }
}
