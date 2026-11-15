// Asks Mojang whether a player joined with the given serverId. Stores nothing.
module.exports = async function handler(req, res) {
  if (req.headers["x-verify-key"] !== process.env.VERIFY_KEY) return res.status(401).json({ error: "key" });
  const { name, serverId } = req.query;
  if (!/^\w{1,16}$/.test(name || "") || !/^[0-9a-f]{1,41}$/.test(serverId || "")) return res.status(400).json({ error: "bad request" });
  const r = await fetch("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encodeURIComponent(name) + "&serverId=" + serverId, {
    headers: { "User-Agent": "ZephraVerifier/1.0" },
  });
  if (r.status !== 200) return res.status(401).json({ error: "mojang", status: r.status });
  const p = await r.json();
  return res.status(200).json({ id: p.id, name: p.name });
};
