const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const BackendServer = require('../backend/server');
const MinecraftDetector = require('../backend/minecraft-detector');

let mainWindow;
let backendServer;
let minecraftDetector;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
      enableRemoteModule: true
    },
    icon: path.join(__dirname, '../../assets/icon.png'),
    title: 'Minecraft AI Agent'
  });

  mainWindow.loadFile(path.join(__dirname, '../renderer/index.html'));

  if (process.argv.includes('--dev')) {
    mainWindow.webContents.openDevTools();
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

app.whenReady().then(async () => {
  // Initialize backend server
  backendServer = new BackendServer();
  await backendServer.start();
  
  // Initialize Minecraft detector
  minecraftDetector = new MinecraftDetector();
  
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    if (backendServer) {
      backendServer.stop();
    }
    app.quit();
  }
});

// IPC handlers
ipcMain.handle('detect-instances', async () => {
  return await minecraftDetector.detectInstances();
});

ipcMain.handle('connect-instance', async (event, instanceId) => {
  return await backendServer.connectToInstance(instanceId);
});

ipcMain.handle('send-prompt', async (event, prompt, settings) => {
  return await backendServer.processPrompt(prompt, settings);
});

ipcMain.handle('stop-task', async () => {
  return await backendServer.stopCurrentTask();
});

ipcMain.handle('get-status', async () => {
  return await backendServer.getStatus();
});

ipcMain.on('log', (event, message) => {
  console.log('[Renderer]', message);
});
