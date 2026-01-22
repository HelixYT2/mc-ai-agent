const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const fs = require('fs');

let mainWindow;
let backendProcess;

function createWindow () {
  mainWindow = new BrowserWindow({
    width: 1100,
    height: 900,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true
    }
  });
  mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}

app.whenReady().then(() => {
  // Spawn packaged backend if present, else fallback to uvicorn dev run (assumes Python)
  const packagedBackend = path.join(process.resourcesPath || __dirname, '..', 'backend', 'backend.exe');
  if (fs.existsSync(packagedBackend)) {
    backendProcess = spawn(packagedBackend, [], { detached: false });
  } else {
    backendProcess = spawn('uvicorn', ['main:app', '--host', '127.0.0.1', '--port', '8000'], { cwd: path.join(__dirname, '..','backend') });
  }

  backendProcess.stdout.on('data', (d) => console.log(`[backend] ${d}`));
  backendProcess.stderr.on('data', (d) => console.error(`[backend] ${d}`));

  createWindow();
});

app.on('window-all-closed', () => {
  if (backendProcess) backendProcess.kill();
  if (process.platform !== 'darwin') app.quit();
});
