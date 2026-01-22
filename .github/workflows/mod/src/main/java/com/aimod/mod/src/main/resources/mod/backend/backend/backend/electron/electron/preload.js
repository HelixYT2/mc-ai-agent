const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('api', {
  // We communicate with backend via HTTP/WebSocket directly from renderer
});
