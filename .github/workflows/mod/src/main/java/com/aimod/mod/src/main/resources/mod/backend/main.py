#!/usr/bin/env python3
"""
backend/main.py - FastAPI backend

- Generates/stores an auth token in backend/config.json on first run.
- Discovers mod WebSocket servers on localhost (ports 42060-42090).
- Connects to a mod instance and relays messages between LM Studio and the mod.
- Exposes /ws/frontend for Electron to receive streaming events.
"""
import asyncio
import json
import os
import uuid
from typing import Dict, Any

import httpx
import psutil
import websockets
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "config.json")
DEFAULT_LM_URL = "http://10.5.0.2:1234"  # default LM Studio URL (editable)
MOD_PORTS_RANGE = list(range(42060, 42090))

app = FastAPI()
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

# state
mod_connections: Dict[int, websockets.WebSocketClientProtocol] = {}
frontend_connections: Dict[str, WebSocket] = {}

def load_config():
    if os.path.exists(CONFIG_PATH):
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            return json.load(f)
    cfg = {"auth_token": str(uuid.uuid4()), "lm_url": DEFAULT_LM_URL}
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        json.dump(cfg, f, indent=2)
    return cfg

config = load_config()

@app.get("/config")
async def get_config():
    return {"auth_token": config["auth_token"], "lm_url": config["lm_url"]}

@app.get("/instances")
async def list_instances():
    found = []
    async def try_port(p: int):
        uri = f"ws://127.0.0.1:{p}"
        try:
            async with websockets.connect(uri, ping_interval=None) as ws:
                try:
                    await ws.send(json.dumps({"type":"handshake","auth":config["auth_token"]}))
                    resp = await asyncio.wait_for(ws.recv(), timeout=1.0)
                    found.append({"port": p, "handshake": json.loads(resp)})
                except Exception:
                    found.append({"port": p, "handshake": None})
        except Exception:
            pass
    await asyncio.gather(*[try_port(p) for p in MOD_PORTS_RANGE])
    mc_procs = []
    for p in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            name = (p.info.get('name') or "").lower()
            cmd = " ".join(p.info.get('cmdline') or [])
            if "java" in name and ("fabric" in cmd.lower() or "minecraft" in cmd.lower()):
                mc_procs.append({"pid": p.info["pid"], "cmd": cmd[:200]})
        except Exception:
            pass
    return {"mods": found, "mc_processes": mc_procs}

async def connect_to_mod(port: int):
    if port in mod_connections:
        return mod_connections[port]
    uri = f"ws://127.0.0.1:{port}"
    ws = await websockets.connect(uri, ping_interval=None)
    await ws.send(json.dumps({"type":"handshake","auth":config["auth_token"]}))
    mod_connections[port] = ws
    asyncio.create_task(mod_reader_loop(port, ws))
    return ws

async def mod_reader_loop(port: int, ws: websockets.WebSocketClientProtocol):
    try:
        async for msg in ws:
            try:
                data = json.loads(msg)
            except Exception:
                data = {"raw": msg}
            await broadcast_to_frontends({"source":"mod","port":port,"payload":data})
    except Exception as e:
        await broadcast_to_frontends({"source":"server","message":f"mod connection closed {port}: {e}"})
        mod_connections.pop(port, None)

async def broadcast_to_frontends(obj: Dict[str, Any]):
    dead = []
    text = json.dumps(obj)
    for k, ws in list(frontend_connections.items()):
        try:
            await ws.send_text(text)
        except Exception:
            dead.append(k)
    for k in dead:
        frontend_connections.pop(k, None)

@app.post("/attach")
async def attach(payload: Dict[str, Any]):
    port = int(payload.get("port", 0))
    if port <= 0:
        return JSONResponse({"error":"port_required"}, status_code=400)
    try:
        await connect_to_mod(port)
        return {"status":"connected","port":port}
    except Exception as e:
        return JSONResponse({"error": str(e)}, status_code=500)

@app.post("/screenshot_control")
async def screenshot_control(payload: Dict[str, Any]):
    port = int(payload.get("port",0))
    cmd = payload.get("command","start")
    fps = int(payload.get("fps",1))
    if port not in mod_connections:
        try:
            await connect_to_mod(port)
        except Exception as e:
            return JSONResponse({"error":"attach_failed","detail":str(e)}, status_code=500)
    ws = mod_connections[port]
    if cmd == "start":
        await ws.send(json.dumps({"type":"screenshot","fps":fps,"auth":config["auth_token"]}))
        return {"status":"started","port":port,"fps":fps}
    else:
        await ws.send(json.dumps({"type":"screenshot","auth":config["auth_token"],"stop":True}))
        return {"status":"stopped","port":port}

@app.post("/send_prompt")
async def send_prompt(payload: Dict[str, Any]):
    port = int(payload.get("port",0))
    prompt = payload.get("prompt","")
    mode = payload.get("mode","high")
    if port not in mod_connections:
        try:
            await connect_to_mod(port)
        except Exception as e:
            return JSONResponse({"error":f"attach_failed: {e}"}, status_code=500)
    ws = mod_connections[port]

    lm_url = config.get("lm_url", DEFAULT_LM_URL)
    ws_streamed = False
    try:
        lm_ws_uri = lm_url.replace("http://","ws://").replace("https://","wss://")
        candidates = [f"{lm_ws_uri}/stream", f"{lm_ws_uri}/ws", f"{lm_ws_uri}/api/v1/stream"]
        for candidate in candidates:
            try:
                async with websockets.connect(candidate, ping_interval=None) as lmws:
                    await lmws.send(json.dumps({"model":"hermes-3-llama-3.1-8b","prompt":prompt,"stream":True}))
                    await broadcast_to_frontends({"source":"lm","event":"stream_started","endpoint":candidate})
                    ws_streamed = True
                    async for chunk in lmws:
                        try:
                            c = json.loads(chunk)
                        except Exception:
                            c = {"chunk": chunk}
                        await broadcast_to_frontends({"source":"lm","chunk":c})
                        if mode == "low" and isinstance(c, dict) and c.get("action"):
                            await ws.send(json.dumps({"type":"action", **c["action"], "auth":config["auth_token"]}))
                    break
            except Exception:
                continue
    except Exception:
        ws_streamed = False

    if not ws_streamed:
        try:
            async with httpx.AsyncClient(timeout=None) as client:
                gen_url = f"{lm_url}/api/v1/generate"
                r = await client.post(gen_url, json={"model":"hermes-3-llama-3.1-8b","input":prompt,"stream":True})
                async for chunk in r.aiter_text():
                    if not chunk:
                        continue
                    try:
                        parsed = json.loads(chunk)
                    except Exception:
                        parsed = {"text": chunk}
                    await broadcast_to_frontends({"source":"lm","chunk": parsed})
                    if mode == "low" and isinstance(parsed, dict) and parsed.get("type") == "actions":
                        for act in parsed.get("actions", []):
                            await ws.send(json.dumps({"type":"action", **act, "auth": config["auth_token"]}))
                text = await r.aread()
                if text:
                    try:
                        j = json.loads(text)
                        await broadcast_to_frontends({"source":"lm","final": j})
                    except Exception:
                        await broadcast_to_frontends({"source":"lm","final_text": text[:1000]})
        except Exception as e:
            await broadcast_to_frontends({"source":"server","message": f"lm_fallback_error: {e}"})
            return JSONResponse({"error": str(e)}, status_code=500)

    if mode == "high":
        try:
            await ws.send(json.dumps({"type":"baritone", "goal": prompt, "auth": config["auth_token"]}))
        except Exception as e:
            await broadcast_to_frontends({"source":"server","message": f"failed_to_send_to_mod: {e}"})

    return {"status":"prompt_handled","mode":mode}

@app.post("/stop")
async def stop_task(payload: Dict[str, Any]):
    port = int(payload.get("port",0))
    if port not in mod_connections:
        return JSONResponse({"error":"not_attached"}, status_code=400)
    ws = mod_connections[port]
    await ws.send(json.dumps({"type":"stop","auth":config["auth_token"]}))
    return {"status":"stop_sent"}

@app.websocket("/ws/frontend")
async def frontend_ws(websocket: WebSocket):
    await websocket.accept()
    client_id = str(uuid.uuid4())
    frontend_connections[client_id] = websocket
    try:
        await websocket.send_text(json.dumps({"source":"server","message":"connected","client_id":client_id}))
        while True:
            data = await websocket.receive_text()
            await websocket.send_text(json.dumps({"source":"server","echo": data}))
    except WebSocketDisconnect:
        frontend_connections.pop(client_id, None)
    except Exception:
        frontend_connections.pop(client_id, None)
