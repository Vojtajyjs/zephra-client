// Zephra API: Cloudflare Worker + D1.
// Stores only: Minecraft UUID, last-seen time, chosen cape. No IP addresses are stored.
const CAPES = ["none", "black", "white", "grad"];
const enc = new TextEncoder();
const hex = (b) => [...new Uint8Array(b)].map((x) => x.toString(16).padStart(2, "0")).join("");
const json = (o, s = 200) => new Response(JSON.stringify(o), { status: s, headers: { "content-type": "application/json" } });

async function hmac(secret, msg) {
  const k = await crypto.subtle.importKey("raw", enc.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  return hex(await crypto.subtle.sign("HMAC", k, enc.encode(msg)));
}

async function auth(req, env) {
  const h = req.headers.get("authorization") || "";
  const [uuid, exp, sig] = h.replace("Bearer ", "").split(".");
  if (!uuid || !exp || !sig || Date.now() > Number(exp)) return null;
  return (await hmac(env.SECRET, "t" + uuid + "." + exp)) === sig ? uuid : null;
}

export default {
  async fetch(req, env) {
    const p = new URL(req.url).pathname;
    try {
      if (p === "/" ) return json({ ok: true, service: "zephra" });

      if (p === "/v1/nonce" && req.method === "GET") {
        const ts = Date.now().toString(16).padStart(12, "0");
        const rnd = hex(crypto.getRandomValues(new Uint8Array(8)));
        const mac = (await hmac(env.SECRET, "n" + ts + rnd)).slice(0, 16);
        return json({ nonce: ts + rnd + mac });
      }

      if (p === "/v1/login" && req.method === "POST") {
        const { name, nonce } = await req.json();
        if (!/^\w{1,16}$/.test(name || "") || !/^[0-9a-f]{44}$/.test(nonce || "")) return json({ error: "bad request" }, 400);
        const ts = parseInt(nonce.slice(0, 12), 16);
        const mac = (await hmac(env.SECRET, "n" + nonce.slice(0, 28))).slice(0, 16);
        if (mac !== nonce.slice(28) || Math.abs(Date.now() - ts) > 90000) return json({ error: "nonce" }, 401);
        const r = await fetch(`https://sessionserver.mojang.com/session/minecraft/hasJoined?username=${encodeURIComponent(name)}&serverId=${nonce}`);
        if (r.status !== 200) return json({ error: "auth" }, 401);
        const prof = await r.json();
        const exp = Date.now() + 3600e3;
        const sig = await hmac(env.SECRET, "t" + prof.id + "." + exp);
        return json({ token: `${prof.id}.${exp}.${sig}`, uuid: prof.id });
      }

      const uuid = await auth(req, env);
      if (!uuid) return json({ error: "unauthorized" }, 401);

      if (p === "/v1/sync" && req.method === "POST") {
        const now = Date.now();
        await env.DB.prepare("INSERT INTO presence(uuid,seen) VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET seen=excluded.seen").bind(uuid, now).run();
        if (Math.random() < 0.02) await env.DB.prepare("DELETE FROM presence WHERE seen<?").bind(now - 600000).run();
        const rows = await env.DB.prepare(
          "SELECT p.uuid AS u, COALESCE(f.cape,'none') AS c FROM presence p LEFT JOIN prefs f ON f.uuid=p.uuid WHERE p.seen>? LIMIT 2000"
        ).bind(now - 150000).all();
        return json({ users: rows.results });
      }

      if (p === "/v1/cape" && req.method === "POST") {
        const { cape } = await req.json();
        if (!CAPES.includes(cape)) return json({ error: "unknown cape" }, 400);
        await env.DB.prepare("INSERT INTO prefs(uuid,cape) VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET cape=excluded.cape").bind(uuid, cape).run();
        return json({ ok: true });
      }
      return json({ error: "not found" }, 404);
    } catch (e) {
      return json({ error: "server error" }, 500);
    }
  },
};
