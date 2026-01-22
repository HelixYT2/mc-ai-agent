package com.helixyt2.mcaiagent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClient extends WebSocketClient {
    private static final Gson gson = new Gson();
    private boolean isRegistered = false;
    private String instanceId;
    
    public WebSocketClient(String serverUri) {
        super(createURI(serverUri));
        this.instanceId = generateInstanceId();
    }
    
    private static URI createURI(String serverUri) {
        try {
            return new URI(serverUri);
        } catch (URISyntaxException e) {
            MCAIAgentMod.LOGGER.error("Invalid WebSocket URI: " + serverUri, e);
            throw new RuntimeException(e);
        }
    }
    
    private String generateInstanceId() {
        return "minecraft_" + System.currentTimeMillis();
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        MCAIAgentMod.LOGGER.info("Connected to backend server");
        register();
    }
    
    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();
            
            switch (type) {
                case "registered":
                    isRegistered = true;
                    MCAIAgentMod.LOGGER.info("Successfully registered with backend");
                    break;
                    
                case "execute_action":
                    JsonObject action = json.getAsJsonObject("action");
                    handleAction(action);
                    break;
                    
                case "stop":
                    MCAIAgentMod.getInstance().getActionExecutor().stopAll();
                    break;
                    
                default:
                    MCAIAgentMod.LOGGER.warn("Unknown message type: " + type);
            }
        } catch (Exception e) {
            MCAIAgentMod.LOGGER.error("Error processing message: " + message, e);
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        MCAIAgentMod.LOGGER.info("Disconnected from backend: " + reason);
        isRegistered = false;
        
        // Try to reconnect after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                reconnect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    @Override
    public void onError(Exception ex) {
        MCAIAgentMod.LOGGER.error("WebSocket error", ex);
    }
    
    private void register() {
        JsonObject message = new JsonObject();
        message.addProperty("type", "register");
        message.addProperty("instanceId", instanceId);
        message.addProperty("version", "1.21.5");
        send(message.toString());
    }
    
    private void handleAction(JsonObject action) {
        MCAIAgentMod.getInstance().getActionExecutor().queueAction(action);
    }
    
    public void sendStateUpdate(JsonObject state) {
        if (!isRegistered) return;
        
        JsonObject message = new JsonObject();
        message.addProperty("type", "state_update");
        message.addProperty("instanceId", instanceId);
        message.add("data", state);
        send(message.toString());
    }
    
    public void sendActionComplete(String actionId, JsonObject result) {
        if (!isRegistered) return;
        
        JsonObject message = new JsonObject();
        message.addProperty("type", "action_complete");
        message.addProperty("instanceId", instanceId);
        message.addProperty("actionId", actionId);
        message.add("result", result);
        send(message.toString());
    }
    
    public void sendActionFailed(String actionId, String error) {
        if (!isRegistered) return;
        
        JsonObject message = new JsonObject();
        message.addProperty("type", "action_failed");
        message.addProperty("instanceId", instanceId);
        message.addProperty("actionId", actionId);
        message.addProperty("error", error);
        send(message.toString());
    }
    
    public void sendLog(String logMessage) {
        if (!isRegistered) return;
        
        JsonObject message = new JsonObject();
        message.addProperty("type", "log");
        message.addProperty("instanceId", instanceId);
        message.addProperty("message", logMessage);
        send(message.toString());
    }
    
    public void disconnect() {
        if (isOpen()) {
            close();
        }
    }
}
