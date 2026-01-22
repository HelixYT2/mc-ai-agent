const { ipcRenderer } = require('electron');

// State
let selectedInstance = null;
let isConnected = false;
let isProcessing = false;
let pollTimeout = null; // Track polling timeout for cleanup
let isConnecting = false; // Prevent race conditions in instance selection

// DOM elements - with null checks
const refreshBtn = document.getElementById('refresh-btn');
const instancesList = document.getElementById('instances-list');
const sendBtn = document.getElementById('send-btn');
const stopBtn = document.getElementById('stop-btn');
const promptInput = document.getElementById('prompt-input');
const statusIndicator = document.getElementById('status-indicator');
const instanceInfo = document.getElementById('instance-info');
const logsContainer = document.getElementById('logs-container');
const aiMode = document.getElementById('ai-mode');
const lmStudioUrl = document.getElementById('lm-studio-url');
const temperature = document.getElementById('temperature');
const temperatureValue = document.getElementById('temperature-value');

// Verify critical elements exist
if (!refreshBtn || !instancesList || !sendBtn || !stopBtn || !promptInput || 
    !statusIndicator || !instanceInfo || !logsContainer || !aiMode || 
    !lmStudioUrl || !temperature || !temperatureValue) {
  console.error('[Renderer] Critical DOM elements missing!');
  throw new Error('Required DOM elements not found');
}

// Event listeners
refreshBtn.addEventListener('click', detectInstances);
sendBtn.addEventListener('click', sendPrompt);
stopBtn.addEventListener('click', stopTask);
temperature.addEventListener('input', (e) => {
  temperatureValue.textContent = e.target.value;
});

// Initialize
detectInstances();
updateStatus('disconnected');

// Functions
async function detectInstances() {
  addLog('Detecting Minecraft instances...', 'info');
  refreshBtn.disabled = true;
  refreshBtn.textContent = '🔄 Scanning...';

  try {
    const instances = await ipcRenderer.invoke('detect-instances');
    
    if (instances.length === 0) {
      instancesList.innerHTML = '<p class="info-text">No Minecraft instances found. Make sure Minecraft is running with the mod installed.</p>';
      addLog('No instances detected', 'warning');
    } else {
      displayInstances(instances);
      addLog(`Found ${instances.length} instance(s)`, 'success');
    }
  } catch (error) {
    addLog(`Error detecting instances: ${error.message}`, 'error');
  } finally {
    refreshBtn.disabled = false;
    refreshBtn.textContent = '🔄 Refresh';
  }
}

function displayInstances(instances) {
  instancesList.innerHTML = '';
  
  instances.forEach(instance => {
    const item = document.createElement('div');
    item.className = 'instance-item';
    item.innerHTML = `
      <span class="instance-name">${instance.name}</span>
      <div class="instance-details">
        <small>PID: ${instance.pid} | ${instance.platform} ${instance.version}</small>
      </div>
    `;
    
    item.addEventListener('click', () => selectInstance(instance, item));
    instancesList.appendChild(item);
  });
}

async function selectInstance(instance, element) {
  // Prevent multiple simultaneous connections
  if (isConnecting) {
    addLog('Connection already in progress, please wait...', 'warning');
    return;
  }

  isConnecting = true;

  // Remove previous selection
  document.querySelectorAll('.instance-item').forEach(item => {
    item.classList.remove('selected');
  });
  
  // Select new instance
  element.classList.add('selected');
  selectedInstance = instance;
  
  addLog(`Connecting to instance: ${instance.name}...`, 'info');
  
  try {
    const result = await ipcRenderer.invoke('connect-instance', instance.id);
    
    if (result.success) {
      isConnected = true;
      updateStatus('connected');
      instanceInfo.textContent = `Connected: ${instance.name}`;
      sendBtn.disabled = false;
      addLog(`Connected to ${instance.name}`, 'success');
    } else {
      addLog(`Failed to connect: ${result.error}`, 'error');
      updateStatus('disconnected');
    }
  } catch (error) {
    addLog(`Connection error: ${error.message}`, 'error');
    updateStatus('disconnected');
  } finally {
    isConnecting = false;
  }
}

async function sendPrompt() {
  const prompt = promptInput.value.trim();
  
  if (!prompt) {
    addLog('Please enter a prompt', 'warning');
    return;
  }
  
  if (!isConnected) {
    addLog('No instance connected', 'error');
    return;
  }

  isProcessing = true;
  updateStatus('processing');
  sendBtn.disabled = true;
  stopBtn.disabled = false;
  
  addLog(`Executing: "${prompt}"`, 'info');
  
  const settings = {
    mode: aiMode.value,
    lmStudioUrl: lmStudioUrl.value,
    temperature: parseFloat(temperature.value),
    model: 'hermes-3-llama-3.1-8b'
  };

  try {
    const result = await ipcRenderer.invoke('send-prompt', prompt, settings);
    
    if (result.success) {
      addLog('Task started successfully', 'success');
      // Poll for status updates
      pollStatus();
    } else {
      addLog(`Task failed: ${result.error}`, 'error');
      isProcessing = false;
      updateStatus('connected');
      sendBtn.disabled = false;
      stopBtn.disabled = true;
    }
  } catch (error) {
    addLog(`Error: ${error.message}`, 'error');
    isProcessing = false;
    updateStatus('connected');
    sendBtn.disabled = false;
    stopBtn.disabled = true;
  }
}

async function stopTask() {
  addLog('Stopping task...', 'warning');
  
  // Clear polling timeout
  if (pollTimeout) {
    clearTimeout(pollTimeout);
    pollTimeout = null;
  }

  try {
    const result = await ipcRenderer.invoke('stop-task');
    
    if (result.success) {
      addLog('Task stopped', 'warning');
    } else {
      addLog(`Failed to stop: ${result.error}`, 'error');
    }
  } catch (error) {
    addLog(`Error stopping task: ${error.message}`, 'error');
  } finally {
    isProcessing = false;
    updateStatus('connected');
    sendBtn.disabled = false;
    stopBtn.disabled = true;
  }
}

async function pollStatus() {
  // Clear any existing timeout
  if (pollTimeout) {
    clearTimeout(pollTimeout);
    pollTimeout = null;
  }

  if (!isProcessing) return;
  
  try {
    const status = await ipcRenderer.invoke('get-status');
    
    if (status.status === 'complete') {
      addLog('Task completed successfully! ✅', 'success');
      isProcessing = false;
      updateStatus('connected');
      sendBtn.disabled = false;
      stopBtn.disabled = true;
      return;
    } else if (status.status === 'error') {
      addLog('Task failed ❌', 'error');
      isProcessing = false;
      updateStatus('connected');
      sendBtn.disabled = false;
      stopBtn.disabled = true;
      return;
    }
    
    // Continue polling only if still processing
    if (isProcessing) {
      pollTimeout = setTimeout(pollStatus, 1000);
    }
  } catch (error) {
    console.error('Status poll error:', error);
    // Continue polling even on error, but only if still processing
    if (isProcessing) {
      pollTimeout = setTimeout(pollStatus, 1000);
    }
  }
}

function updateStatus(status) {
  statusIndicator.className = `status-badge ${status}`;
  
  const statusText = {
    'connected': 'Connected',
    'disconnected': 'Disconnected',
    'processing': 'Processing...'
  };
  
  statusIndicator.textContent = statusText[status] || status;
}

function addLog(message, type = 'info') {
  const entry = document.createElement('div');
  entry.className = `log-entry ${type}`;
  
  const now = new Date();
  const time = now.toTimeString().split(' ')[0];
  
  // Sanitize message to prevent XSS
  const sanitizedMessage = String(message)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
  
  entry.innerHTML = `
    <span class="log-time">[${time}]</span>
    <span class="log-message">${sanitizedMessage}</span>
  `;
  
  logsContainer.appendChild(entry);
  logsContainer.scrollTop = logsContainer.scrollHeight;
  
  // Log to main process too
  ipcRenderer.send('log', message);
}

// Listen for backend events
window.addEventListener('DOMContentLoaded', () => {
  addLog('Minecraft AI Agent initialized', 'success');
  addLog('Make sure LM Studio is running on http://localhost:1234', 'info');
  addLog('Make sure the mod is installed in your Minecraft instance', 'info');
});
