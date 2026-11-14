const { contextBridge, ipcRenderer } = require('electron');
contextBridge.exposeInMainWorld('zephra', {
  state: () => ipcRenderer.invoke('state'),
  login: () => ipcRenderer.invoke('login'),
  logout: () => ipcRenderer.invoke('logout'),
  pickDir: () => ipcRenderer.invoke('pickDir'),
  setNetwork: (v) => ipcRenderer.invoke('network', v),
  setRam: (gb) => ipcRenderer.invoke('ram', gb),
  play: () => ipcRenderer.invoke('play'),
  onStatus: (cb) => ipcRenderer.on('status', (_e, s) => cb(s)),
});
