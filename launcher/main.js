const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');
const fs = require('fs');
const { Client } = require('minecraft-launcher-core');
const { Auth } = require('msmc');

const MC = '1.21.4';
const LOADER = '0.16.10';
const FABRIC_ID = `fabric-loader-${LOADER}-${MC}`;
const MODS = ['fabric-api', 'sodium', 'lithium', 'ferrite-core', 'immediatelyfast', 'entityculling'];

const ROOT = path.join(app.getPath('appData'), '.zephra');
const SETTINGS = path.join(ROOT, 'launcher.json');
fs.mkdirSync(ROOT, { recursive: true });

let win, mclc = null, profileName = null;
const defaultDir = () => process.platform === 'darwin' ? path.join(app.getPath('appData'), 'minecraft')
  : process.platform === 'win32' ? path.join(app.getPath('appData'), '.minecraft') : path.join(app.getPath('home'), '.minecraft');
const gameDir = () => read().gameDir || defaultDir();
const read = () => { try { return JSON.parse(fs.readFileSync(SETTINGS, 'utf8')); } catch { return {}; } };
const write = (o) => fs.writeFileSync(SETTINGS, JSON.stringify({ ...read(), ...o }));
const status = (text, pct) => win && win.webContents.send('status', { text, pct });
const UA = { 'User-Agent': 'zephra-launcher/0.1.0' };

async function useXbox(xbox) {
  const token = await xbox.getMinecraft();
  mclc = token.mclc();
  profileName = token.profile.name;
  write({ session: xbox.save() });
}

async function tryRestore() {
  const { session } = read();
  if (!session) return;
  try { await useXbox(await new Auth('select_account').refresh(session)); } catch { write({ session: null }); }
}

async function ensureFabric() {
  const dir = path.join(ROOT, 'versions', FABRIC_ID);
  const file = path.join(dir, `${FABRIC_ID}.json`);
  if (fs.existsSync(file)) return;
  status('Downloading Fabric…', 5);
  const r = await fetch(`https://meta.fabricmc.net/v2/versions/loader/${MC}/${LOADER}/profile/json`, { headers: UA });
  if (!r.ok) throw new Error('Could not download Fabric profile');
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(file, await r.text());
}

async function resolveMod(slug) {
  const q = `game_versions=${encodeURIComponent(JSON.stringify([MC]))}&loaders=${encodeURIComponent('["fabric"]')}`;
  const r = await fetch(`https://api.modrinth.com/v2/project/${slug}/version?${q}`, { headers: UA });
  const list = await r.json();
  if (!Array.isArray(list) || !list.length) throw new Error(`No ${MC} build of ${slug}`);
  const f = list[0].files.find((x) => x.primary) || list[0].files[0];
  return { name: f.filename, url: f.url };
}

async function ensureMods() {
  const dir = path.join(gameDir(), 'mods');
  fs.mkdirSync(dir, { recursive: true });
  const wanted = [];
  for (let i = 0; i < MODS.length; i++) { status(`Checking ${MODS[i]}…`, 10 + i * 4); wanted.push(await resolveMod(MODS[i])); }
  const own = path.join(process.resourcesPath, 'resources', 'zephra-client.jar');
  const ownJar = fs.existsSync(own) ? own : path.join(__dirname, 'resources', 'zephra-client.jar');
  // only ever delete jars this launcher installed itself
  const manifest = path.join(ROOT, 'managed.json');
  let old = []; try { old = JSON.parse(fs.readFileSync(manifest, 'utf8')); } catch {}
  const keep = new Set([...wanted.map((m) => m.name), 'zephra-client.jar']);
  for (const f of old) if (!keep.has(f)) try { fs.unlinkSync(path.join(dir, f)); } catch {}
  for (let i = 0; i < wanted.length; i++) {
    const dest = path.join(dir, wanted[i].name);
    if (fs.existsSync(dest)) continue;
    status(`Downloading ${wanted[i].name}…`, 35 + i * 4);
    const r = await fetch(wanted[i].url, { headers: UA });
    fs.writeFileSync(dest, Buffer.from(await r.arrayBuffer()));
  }
  if (fs.existsSync(ownJar)) fs.copyFileSync(ownJar, path.join(dir, 'zephra-client.jar'));
  else status('zephra-client.jar missing, launching without Zephra HUD', 60);
  fs.writeFileSync(manifest, JSON.stringify([...keep]));
}

ipcMain.handle('state', async () => {
  if (!mclc) await tryRestore();
  return { name: profileName, ram: read().ram || 4, dir: gameDir() };
});
ipcMain.handle('login', async () => {
  const xbox = await new Auth('select_account').launch('electron');
  await useXbox(xbox);
  return { name: profileName };
});
ipcMain.handle('logout', () => { mclc = null; profileName = null; write({ session: null }); });
ipcMain.handle('pickDir', async () => {
  const r = await dialog.showOpenDialog(win, { properties: ['openDirectory', 'createDirectory'], defaultPath: gameDir() });
  if (!r.canceled && r.filePaths[0]) write({ gameDir: r.filePaths[0] });
  return gameDir();
});
ipcMain.handle('ram', (_e, gb) => write({ ram: gb }));

ipcMain.handle('play', async () => {
  if (!mclc) throw new Error('Sign in first');
  fs.mkdirSync(gameDir(), { recursive: true });
  await ensureFabric();
  await ensureMods();
  const launcher = new Client();
  launcher.on('progress', (e) => status(`Downloading ${e.type}…`, 70 + Math.min(25, (e.task / e.total) * 25)));
  launcher.on('close', () => { status('Ready', 0); win.show(); });
  const ram = read().ram || 4;
  status('Starting Minecraft…', 95);
  await launcher.launch({
    authorization: mclc,
    root: ROOT,
    version: { number: MC, type: 'release', custom: FABRIC_ID },
    memory: { max: `${ram}G`, min: '1G' },
    overrides: { gameDirectory: gameDir() },
  });
  status('Playing', 100);
  win.minimize();
});

app.whenReady().then(() => {
  win = new BrowserWindow({
    width: 820, height: 540, resizable: false, backgroundColor: '#000000', title: 'Zephra',
    autoHideMenuBar: true, webPreferences: { preload: path.join(__dirname, 'preload.js') },
  });
  win.loadFile('index.html');
});
app.on('window-all-closed', () => app.quit());
