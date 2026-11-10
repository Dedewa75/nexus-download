// NEXUS CONTROL pour Windows : une fenêtre dédiée qui ouvre ton NEXUS en ligne.
// Premier lancement : setup.html demande l'adresse et la mémorise.
const { app, BrowserWindow, shell, Menu } = require('electron');
const path = require('path');

const SETUP = path.join(__dirname, 'setup.html');

function createWindow() {
  const win = new BrowserWindow({
    width: 1400, height: 900, minWidth: 380, minHeight: 600,
    backgroundColor: '#060a10', title: 'NEXUS', icon: path.join(__dirname, 'icon.png'),
    autoHideMenuBar: true,
    webPreferences: { contextIsolation: true, nodeIntegration: false, sandbox: true },
  });
  Menu.setApplicationMenu(null);

  const isNexus = (url) => {
    try { const cur = new URL(win.webContents.getURL()); const u = new URL(url); return cur.protocol === 'file:' || u.host === cur.host; } catch { return false; }
  };
  // liens externes (GitHub, sites clients…) → navigateur par défaut
  win.webContents.setWindowOpenHandler(({ url }) => {
    if (/^https?:/i.test(url)) shell.openExternal(url);
    return { action: 'deny' };
  });
  win.webContents.on('will-navigate', (e, url) => {
    if (url.startsWith('file:') || isNexus(url)) return;
    e.preventDefault();
    shell.openExternal(url);
  });
  // adresse fausse (« Not Found ») ou serveur injoignable → on redemande l'adresse
  win.webContents.on('did-navigate', (_e, url, code) => {
    if (!url.startsWith('file:') && code === 404) win.loadFile(SETUP, { query: { error: '404' } });
  });
  win.webContents.on('did-fail-load', (_e, code, _desc, url, isMainFrame) => {
    if (isMainFrame && code !== -3 && !String(url).startsWith('file:')) win.loadFile(SETUP, { query: { error: 'offline' } });
  });
  // F5 = actualiser, Ctrl+Maj+A = changer d'adresse
  win.webContents.on('before-input-event', (e, input) => {
    if (input.type !== 'keyDown') return;
    if (input.key === 'F5') { win.webContents.reload(); e.preventDefault(); }
    if (input.control && input.shift && input.key.toLowerCase() === 'a') { win.loadFile(SETUP, { query: { error: 'change' } }); e.preventDefault(); }
  });
  win.loadFile(SETUP);
}

if (!app.requestSingleInstanceLock()) app.quit();
else {
  app.on('second-instance', () => { const w = BrowserWindow.getAllWindows()[0]; if (w) { if (w.isMinimized()) w.restore(); w.focus(); } });
  app.whenReady().then(createWindow);
  app.on('window-all-closed', () => app.quit());
}
