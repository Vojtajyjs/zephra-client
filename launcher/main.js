const { app, BrowserWindow, ipcMain, dialog, safeStorage } = require('electron');
const os = require('os');
const path = require('path');
const { execFile } = require('child_process');
const fs = require('fs');
const { Client } = require('minecraft-launcher-core');
const { Auth } = require('msmc');

// Set this to your deployed Zephra server (https://...workers.dev) to enable badges and capes.
const API_URL = '';
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
const maxRam = () => Math.max(2, Math.min(16, Math.floor((os.totalmem() / 1073741824) * 0.6)));
const gameDir = () => read().gameDir || defaultDir();
const read = () => { try { return JSON.parse(fs.readFileSync(SETTINGS, 'utf8')); } catch { return {}; } };
const write = (o) => fs.writeFileSync(SETTINGS, JSON.stringify({ ...read(), ...o }));
const status = (text, pct) => win && win.webContents.send('status', { text, pct });
const UA = { 'User-Agent': 'zephra-launcher/0.1.0' };

function saveSession(str) {
  try {
    if (safeStorage.isEncryptionAvailable()) write({ session: safeStorage.encryptString(str).toString('base64'), enc: true });
    else write({ session: str, enc: false });
  } catch {}
}
function loadSession() {
  const o = read();
  if (!o.session) return null;
  try { return o.enc ? safeStorage.decryptString(Buffer.from(o.session, 'base64')) : o.session; } catch { return null; }
}

async function useXbox(xbox) {
  const token = await xbox.getMinecraft();
  mclc = token.mclc();
  profileName = token.profile.name;
  saveSession(xbox.save());
}

async function tryRestore() {
  const session = loadSession();
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
  return { name: profileName, ram: Math.min(read().ram || 4, maxRam()), maxRam: maxRam(), dir: gameDir(), api: !!API_URL, network: read().network };
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
ipcMain.handle('network', (_e, v) => write({ network: !!v }));
ipcMain.handle('ram', (_e, gb) => write({ ram: gb }));

const run = (cmd, args) => new Promise((res, rej) => execFile(cmd, args, (e, so, se) => (e ? rej(new Error(se || e.message)) : res(so))));

function findJava(dir) {
  const exe = process.platform === 'win32' ? 'java.exe' : 'java';
  const walk = (d, depth) => {
    if (depth > 6) return null;
    let ents; try { ents = fs.readdirSync(d, { withFileTypes: true }); } catch { return null; }
    for (const e of ents) {
      const p = path.join(d, e.name);
      if (e.isFile() && e.name === exe && path.basename(d) === 'bin') return p;
      if (e.isDirectory()) { const r = walk(p, depth + 1); if (r) return r; }
    }
    return null;
  };
  return walk(dir, 0);
}

// Own Java 21 runtime, so players do not need to install anything.
async function ensureJava() {
  const dir = path.join(ROOT, 'java21');
  let j = fs.existsSync(dir) ? findJava(dir) : null;
  if (j) return j;
  const osName = { darwin: 'mac', win32: 'windows', linux: 'linux' }[process.platform];
  const arch = process.arch === 'arm64' ? 'aarch64' : 'x64';
  status('Downloading Java 21 (one time only)…', 3);
  const r = await fetch(`https://api.adoptium.net/v3/binary/latest/21/ga/${osName}/${arch}/jre/hotspot/normal/eclipse`, { headers: UA });
  if (!r.ok) throw new Error('Could not download Java');
  const tmp = path.join(ROOT, osName === 'windows' ? 'java21.zip' : 'java21.tar.gz');
  fs.writeFileSync(tmp, Buffer.from(await r.arrayBuffer()));
  fs.rmSync(dir, { recursive: true, force: true });
  fs.mkdirSync(dir, { recursive: true });
  await run('tar', ['-xf', tmp, '-C', dir]);
  fs.unlinkSync(tmp);
  j = findJava(dir);
  if (!j) throw new Error('Java install failed');
  if (process.platform !== 'win32') fs.chmodSync(j, 0o755);
  return j;
}

ipcMain.handle('play', async () => {
  if (!mclc) throw new Error('Sign in first');
  fs.mkdirSync(gameDir(), { recursive: true });
  const java = await ensureJava();
  await ensureFabric();
  await ensureMods();
  const logFile = path.join(ROOT, 'latest.log');
  fs.writeFileSync(logFile, '');
  const home = os.homedir();
  const clean = (t) => String(t).replace(/(--(?:accessToken|uuid|xuid|clientId|username)\s+)\S+/g, '$1***').split(home).join('~');
  const log = (d) => fs.appendFileSync(logFile, clean(d) + '\n');
  const launcher = new Client();
  launcher.on('progress', (e) => status(`Downloading ${e.type}…`, 70 + Math.min(25, (e.task / e.total) * 25)));
  launcher.on('debug', log);
  launcher.on('data', log);
  launcher.on('close', (code) => {
    status(code ? `Game closed with an error (code ${code}). Log: ${logFile}` : 'Ready', 0);
    win.show();
  });
  const ram = Math.min(read().ram || 4, maxRam());
  status('Starting Minecraft…', 95);
  const proc = await launcher.launch({
    authorization: mclc,
    root: ROOT,
    javaPath: java,
    version: { number: MC, type: 'release', custom: FABRIC_ID },
    memory: { max: `${ram}G`, min: '1G' },
    overrides: { gameDirectory: gameDir() },
    customArgs: ['-Dzephra.network=' + (read().network === true), ...(API_URL ? ['-Dzephra.api=' + API_URL] : [])],
  });
  if (!proc) throw new Error(`Could not start the game. Log: ${logFile}`);
  status('Playing', 100);
  win.minimize();
});

app.whenReady().then(() => {
  win = new BrowserWindow({
    width: 820, height: 540, resizable: false, backgroundColor: '#000000', title: 'Zephra',
    autoHideMenuBar: true, webPreferences: { preload: path.join(__dirname, 'preload.js') },
  });
  win.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
  win.loadFile('index.html');
});
app.on('window-all-closed', () => app.quit());
