```text
good-name (private) - MC AI Agent repository

This repository contains:
- mod/: Fabric client mod source (1.21.5) that exposes a local WebSocket API
- backend/: Python FastAPI backend that discovers mod instances and connects to LM Studio
- electron/: Electron UI for interacting with mod & backend
- build_scripts/: PowerShell helper scripts to build & install locally
- .github/workflows/ci-windows.yml: CI that builds mod, backend.exe and Electron installer on Windows runners

Quick local dev (Windows) steps

1) Create a private GitHub repo named "good-name" and push this code into it, or initialize locally:
   git init
   git add .
   git commit -m "initial"
   git branch -M main
   git remote add origin <your-private-repo-url>
   git push -u origin main

2) Run backend (dev)
   cd backend
   python -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   uvicorn main:app --reload --host 127.0.0.1 --port 8000
   Check backend/config.json for the generated auth_token.

3) Install mod (dev)
   - Option A: Use IntelliJ + Fabric Loom, import mod/ as Gradle project, run client (recommended for development).
   - Option B: Build jar: cd mod; gradlew build; copy build/libs/AIHandlerMod-1.0.0.jar -> %APPDATA%\.minecraft\mods
   - Create %APPDATA%\.minecraft\config\aiagent_config.json with: { "auth_token": "<token-from-backend/config.json>" }

4) Run Electron UI (dev)
   cd electron
   npm ci
   npm start
   Use the UI to Refresh -> Attach -> Start screenshot stream -> Send prompts.

5) Build locally (optional)
   Run build_scripts\build_all.ps1 to build mod, backend.exe, and package Electron (requires toolchain installed).

CI (GitHub Actions)
- After pushing to your private repo, open Actions and allow the workflow to run (it will run on push).
- If successful, download artifacts from the workflow run: mod jar, backend.exe, and the Windows installer from electron/dist.

LM Studio
- The backend defaults to LM Studio URL http://10.5.0.2:1234. You can change backend/config.json after first run or use the UI LM field.

Baritone
- Baritone is optional. To enable full Baritone automation add a compatible Baritone jar to mod/libs and uncomment dependency usage.
- CI attempts to fetch Baritone optionally; if not compatible you can add your own Baritone build.

If you want I can:
- produce a zip of these files for easy copy/paste
- or walk you through pushing to your private GitHub and starting the first CI run
```
