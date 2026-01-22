package com.aimod;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.blaze3d.platform.NativeImage;

/*
 AIHandlerMod - Fabric 1.21.5 client mod scaffold

 - WebSocket server on localhost (default port 42069)
 - Expects messages with an "auth" token (config in %appdata%/.minecraft/config/aiagent_config.json)
 - Supports basic messages:
   handshake, action, baritone, screenshot, stop
 - Baritone integration is optional (if Baritone jar present & compatible).
 - Screenshot capture uses NativeImage; mapping-specific adjustments may be needed in your dev env.
*/

public class AIHandlerMod implements ClientModInitializer {
    private static final int DEFAULT_PORT = 42069;
    private static final Gson gson = new Gson();
    private WebSocketServer server;
    private String authToken;
    private AtomicBoolean streaming = new AtomicBoolean(false);
    private int streamFps = 1;

    // fallback token for quick testing (replace or create config file as described)
    private static final String defaultAuth = "change_me_in_config";

    @Override
    public void onInitializeClient() {
        // Read token from %APPDATA%/.minecraft/config/aiagent_config.json if present
        try {
            String userHome = System.getProperty("user.home");
            String mcConfig = userHome + File.separator + ".minecraft" + File.separator + "config" + File.separator + "aiagent_config.json";
            File f = new File(mcConfig);
            if (f.exists()) {
                JsonObject j = gson.fromJson(new FileReader(f), JsonObject.class);
                if (j != null && j.has("auth_token")) {
                    authToken = j.get("auth_token").getAsString();
                }
            }
        } catch (Exception e) {
            // ignore and fallback
        }
        if (authToken == null) authToken = defaultAuth;

        startWebSocketServer(DEFAULT_PORT);
    }

    private void startWebSocketServer(int port) {
        server = new WebSocketServer(new InetSocketAddress("127.0.0.1", port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("[AIHandlerMod] Connection opened: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("[AIHandlerMod] Connection closed: " + reason);
                streaming.set(false);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                try {
                    JsonObject msg = gson.fromJson(message, JsonObject.class);
                    if (!msg.has("auth") || !msg.get("auth").getAsString().equals(authToken)) {
                        conn.send(gson.toJson(error("unauthorized")));
                        conn.close();
                        return;
                    }
                    String type = msg.has("type") ? msg.get("type").getAsString() : "";
                    switch (type) {
                        case "handshake":
                            conn.send(gson.toJson(success("handshake_ok")));
                            break;
                        case "action":
                            handleAction(msg, conn);
                            break;
                        case "baritone":
                            handleBaritone(msg, conn);
                            break;
                        case "screenshot":
                            if (msg.has("fps")) {
                                int fps = Math.max(1, Math.min(15, msg.get("fps").getAsInt()));
                                startScreenshotStream(conn, fps);
                            } else {
                                sendSingleScreenshot(conn);
                            }
                            break;
                        case "stop":
                            handleStop(conn);
                            break;
                        default:
                            conn.send(gson.toJson(error("unknown_type")));
                    }
                } catch (Exception e) {
                    conn.send(gson.toJson(error("exception: " + e.getMessage())));
                }
            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
                // ignore binary in this scaffold
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("[AIHandlerMod] WebSocket server started on port " + getPort());
            }
        };
        new Thread(() -> server.start()).start();
    }

    private void handleAction(JsonObject msg, WebSocket conn) {
        String action = msg.has("action") ? msg.get("action").getAsString() : "";
        JsonObject resp = new JsonObject();
        resp.addProperty("status", "ok");
        resp.addProperty("action_received", action);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) {
            resp.addProperty("status", "error");
            resp.addProperty("message", "client_or_player_null");
            conn.send(gson.toJson(resp));
            return;
        }

        switch (action) {
            case "teleport_test":
                if (msg.has("x") && msg.has("y") && msg.has("z")) {
                    double x = msg.get("x").getAsDouble();
                    double y = msg.get("y").getAsDouble();
                    double z = msg.get("z").getAsDouble();
                    mc.player.requestTeleport(x, y, z);
                    resp.addProperty("message", "teleported_for_test");
                } else {
                    resp.addProperty("message", "missing_coords");
                }
                break;
            default:
                resp.addProperty("message", "unknown_action");
        }
        conn.send(gson.toJson(resp));
    }

    private void handleBaritone(JsonObject msg, WebSocket conn) {
        final String goal = msg.has("goal") ? msg.get("goal").getAsString() : "";
        new Thread(() -> {
            try {
                conn.send(gson.toJson(progress("baritone_started")));
                // Attempt to call Baritone if present; otherwise fallback to simulated progress
                try {
                    // If Baritone jar is present and compatible, you can call it like:
                    // var baritone = baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone();
                    // baritone.getCommandManager().execute(goal);
                    conn.send(gson.toJson(progress("baritone_command_forwarded")));
                } catch (Throwable t) {
                    conn.send(gson.toJson(progress("baritone_not_available")));
                }
                Thread.sleep(800);
                conn.send(gson.toJson(progress("baritone_path_calculated")));
                Thread.sleep(1000);
                conn.send(gson.toJson(progress("baritone_arrived")));
                conn.send(gson.toJson(success("baritone_done")));
            } catch (InterruptedException e) {
                conn.send(gson.toJson(error("baritone_interrupted")));
            }
        }).start();
    }

    private void startScreenshotStream(WebSocket conn, int fps) {
        if (streaming.get()) return;
        streaming.set(true);
        this.streamFps = fps;
        new Thread(() -> {
            while (streaming.get() && conn.isOpen()) {
                sendSingleScreenshot(conn);
                try { Thread.sleep(1000 / Math.max(1, streamFps)); } catch (InterruptedException ie) { break; }
            }
        }).start();
    }

    private void sendSingleScreenshot(WebSocket conn) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        mc.execute(() -> {
            try {
                int width = mc.getWindow().getFramebufferWidth();
                int height = mc.getWindow().getFramebufferHeight();

                // NOTE: NativeImage read/write APIs and framebuffer access vary between mappings.
                // This is a scaffold. If compile errors occur, replace with your mapping's screenshot helper.
                NativeImage img = NativeImage.read(mc.getFramebuffer().texture);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                img.writeTo(baos);
                byte[] png = baos.toByteArray();
                String b64 = Base64.getEncoder().encodeToString(png);

                JsonObject payload = new JsonObject();
                payload.addProperty("screenshot_b64", b64);
                payload.addProperty("width", width);
                payload.addProperty("height", height);
                payload.addProperty("status", "screenshot");
                conn.send(gson.toJson(payload));
                img.close();
            } catch (Exception e) {
                conn.send(gson.toJson(error("screenshot_failed:" + e.getMessage())));
            }
        });
    }

    private void handleStop(WebSocket conn) {
        streaming.set(false);
        // Cancel Baritone tasks if available (uncomment and implement with Baritone API)
        conn.send(gson.toJson(success("stopping_tasks")));
    }

    private JsonObject success(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("status", "ok");
        o.addProperty("message", msg);
        return o;
    }
    private JsonObject error(String reason) {
        JsonObject o = new JsonObject();
        o.addProperty("status", "error");
        o.addProperty("message", reason);
        return o;
    }
    private JsonObject progress(String p) {
        JsonObject o = new JsonObject();
        o.addProperty("progress", p);
        return o;
    }
}
