const { exec } = require('child_process');
const { promisify } = require('util');

const execAsync = promisify(exec);

class MinecraftDetector {
  constructor() {
    this.detectedInstances = [];
  }

  async detectInstances() {
    try {
      if (process.platform === 'win32') {
        return await this.detectWindowsInstances();
      } else if (process.platform === 'darwin') {
        return await this.detectMacInstances();
      } else {
        return await this.detectLinuxInstances();
      }
    } catch (error) {
      console.error('[Detector] Error detecting instances:', error);
      return [];
    }
  }

  async detectWindowsInstances() {
    try {
      // Use tasklist to find java.exe processes
      const { stdout } = await execAsync('tasklist /FI "IMAGENAME eq javaw.exe" /FO CSV /NH');
      
      // Also try to get window titles
      const windowManagerAvailable = await this.checkWindowManager();
      let windows = [];
      
      if (windowManagerAvailable) {
        try {
          const windowManager = require('node-window-manager');
          windows = windowManager.getWindows();
        } catch (error) {
          console.error('[Detector] Error loading window manager:', error);
        }
      }

      const instances = [];
      const lines = stdout.split('\n');
      
      for (const line of lines) {
        if (line.includes('javaw.exe')) {
          // Extract PID
          const parts = line.split(',');
          if (parts.length >= 2) {
            const pidStr = parts[1] ? parts[1].replace(/"/g, '').trim() : '';
            const pid = parseInt(pidStr);
            
            if (!isNaN(pid)) {
              // Try to find matching window
              let windowTitle = 'Unknown';
              if (windows.length > 0) {
                const matchingWindow = windows.find(w => {
                  try {
                    const title = w.getTitle();
                    return title && (
                      title.includes('Minecraft') || 
                      title.includes('minecraft') ||
                      title.includes('1.21')
                    );
                  } catch (error) {
                    return false;
                  }
                });
                
                if (matchingWindow) {
                  try {
                    windowTitle = matchingWindow.getTitle();
                  } catch (error) {
                    windowTitle = 'Minecraft';
                  }
                }
              }
              
              instances.push({
                id: `instance_${pid}`,
                pid: pid,
                name: windowTitle,
                platform: 'Java Edition',
                version: this.extractVersion(windowTitle)
              });
            }
          }
        }
      }

      return instances;
    } catch (error) {
      console.error('[Detector] Windows detection error:', error);
      return [];
    }
  }

  async detectMacInstances() {
    try {
      const { stdout } = await execAsync('ps aux | grep java | grep -i minecraft');
      // Parse output and create instance objects
      // Similar to Windows but adapted for macOS
      return [];
    } catch (error) {
      return [];
    }
  }

  async detectLinuxInstances() {
    try {
      const { stdout } = await execAsync('ps aux | grep java | grep -i minecraft');
      // Parse output and create instance objects
      return [];
    } catch (error) {
      return [];
    }
  }

  async checkWindowManager() {
    try {
      require.resolve('node-window-manager');
      return true;
    } catch {
      return false;
    }
  }

  extractVersion(windowTitle) {
    // Try to extract version from window title
    if (!windowTitle || typeof windowTitle !== 'string') {
      return 'Unknown';
    }
    const versionMatch = windowTitle.match(/\d+\.\d+(\.\d+)?/);
    return versionMatch ? versionMatch[0] : 'Unknown';
  }

  async findPrismInstances() {
    // Look for Prism Launcher instances specifically
    try {
      if (process.platform === 'win32') {
        const appData = process.env.APPDATA || '';
        const prismPath = appData + '/PrismLauncher';
        // Could check for running instances or instance configs here
      }
    } catch (error) {
      console.error('[Detector] Error finding Prism instances:', error);
    }
  }
}

module.exports = MinecraftDetector;
