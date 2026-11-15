package dev.zephra;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Optional Zephra network: shows the Zephra badge and capes between Zephra users.
 * Off unless the launcher passes -Dzephra.api=... and the player agreed (-Dzephra.network=true).
 * Your Minecraft access token never leaves the game: it is only used to talk to Mojang.
 */
public class ZephraNet {
    public static final String API = System.getProperty("zephra.api", "");
    public static final boolean ALLOWED = Boolean.getBoolean("zephra.network");
    public static volatile Map<UUID, String> users = Map.of();
    public static volatile String status = "Join a world to connect.";
    static volatile String token;
    static volatile long tokenExp, last, lastBeat;
    static volatile boolean busy;
    static Object lastWorld;
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public static boolean enabled() { return ALLOWED && !API.isEmpty(); }
    public static boolean has(UUID id) { return users.containsKey(id); }

    static UUID parse(String s) {
        try { return UUID.fromString(s.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")); }
        catch (Exception e) { return null; }
    }

    /** Syncs right after joining a world, then refreshes the list every 20 s and announces itself every minute. */
    public static void tick(MinecraftClient mc) {
        if (!enabled()) return;
        if (mc.world == null) { lastWorld = null; return; }
        if (busy) return;
        long now = System.currentTimeMillis();
        if (mc.world != lastWorld) { lastWorld = mc.world; last = 0; }
        if (now - last < 20_000) return;
        last = now; busy = true;
        boolean beat = now - lastBeat >= 55_000;
        Thread t = new Thread(() -> {
            try {
                sync(mc, beat);
                if (beat) lastBeat = System.currentTimeMillis();
                status = "Connected. " + users.size() + " Zephra player" + (users.size() == 1 ? "" : "s") + " online.";
            } catch (Exception e) {
                token = null;
                status = "Could not reach the Zephra server (" + e.getMessage() + ").";
            } finally { busy = false; }
        }, "zephra-net");
        t.setDaemon(true); t.start();
    }

    static void sync(MinecraftClient mc, boolean beat) throws Exception {
        if (token == null || System.currentTimeMillis() > tokenExp) login(mc);
        JsonObject req = new JsonObject();
        req.addProperty("beat", beat);
        JsonObject o = JsonParser.parseString(send("POST", "/v1/sync", req.toString(), token)).getAsJsonObject();
        Map<UUID, String> m = new HashMap<>();
        for (JsonElement e : o.getAsJsonArray("users")) {
            JsonObject u = e.getAsJsonObject();
            UUID id = parse(u.get("u").getAsString());
            if (id != null) m.put(id, u.get("c").getAsString());
        }
        users = m;
    }

    static void login(MinecraftClient mc) throws Exception {
        String nonce = JsonParser.parseString(send("GET", "/v1/nonce", null, null)).getAsJsonObject().get("nonce").getAsString();
        var s = mc.getSession();
        mc.getSessionService().joinServer(s.getUuidOrNull(), s.getAccessToken(), nonce);
        JsonObject body = new JsonObject();
        body.addProperty("name", s.getUsername());
        body.addProperty("nonce", nonce);
        JsonObject r = JsonParser.parseString(send("POST", "/v1/login", body.toString(), null)).getAsJsonObject();
        token = r.get("token").getAsString();
        tokenExp = System.currentTimeMillis() + 50 * 60_000L;
    }

    static String send(String method, String path, String body, String bearer) throws Exception {
        var b = HttpRequest.newBuilder(URI.create(API + path)).timeout(Duration.ofSeconds(8))
            .header("Content-Type", "application/json").header("User-Agent", "ZephraClient");
        if (bearer != null) b.header("Authorization", "Bearer " + bearer);
        if (method.equals("GET")) b.GET(); else b.POST(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
        var r = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new IOException("HTTP " + r.statusCode());
        return r.body();
    }

    public static void setCape(String id) {
        if (!enabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        UUID me = mc.getSession().getUuidOrNull();
        if (me != null) { Map<UUID, String> m = new HashMap<>(users); m.put(me, id); users = m; }
        Thread t = new Thread(() -> {
            try {
                if (token == null || System.currentTimeMillis() > tokenExp) login(mc);
                JsonObject body = new JsonObject(); body.addProperty("cape", id);
                send("POST", "/v1/cape", body.toString(), token);
                last = 0;
            } catch (Exception e) { token = null; }
        }, "zephra-net");
        t.setDaemon(true); t.start();
    }
}
