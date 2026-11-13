package dev.zephra;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

/** Saved in .minecraft/config/zephra.json (editable by hand). */
public class Config {
    public static class Mod {
        public boolean enabled; public int x, y; public float scale = 1f;
        public Mod() {}
        public Mod(boolean enabled, int x, int y) { this.enabled = enabled; this.x = x; this.y = y; }
    }
    public static final String[] NAMES = {"fps", "cps", "coords", "keystrokes", "armor", "zoom", "fullbright", "togglesprint"};
    public static final String[] LABELS = {"FPS", "CPS", "Coordinates", "Keystrokes", "Armor", "Zoom (hold Z)", "Fullbright", "Toggle sprint"};
    public static Map<String, Mod> mods = new LinkedHashMap<>();
    static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zephra.json");
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static void defaults() {
        mods.put("fps", new Mod(true, 6, 6));
        mods.put("cps", new Mod(true, 6, 18));
        mods.put("coords", new Mod(true, 6, 30));
        mods.put("keystrokes", new Mod(true, 6, 50));
        mods.put("armor", new Mod(true, 6, 140));
        mods.put("zoom", new Mod(true, 0, 0));
        mods.put("fullbright", new Mod(false, 0, 0));
        mods.put("togglesprint", new Mod(false, 0, 0));
    }

    public static void load() {
        defaults();
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                for (String n : NAMES) if (o.has(n)) { Mod m = GSON.fromJson(o.get(n), Mod.class); if (m != null) { if (m.scale < 0.4f || m.scale > 4f) m.scale = 1f; mods.put(n, m); } }
            }
        } catch (Exception ignored) {}
        save();
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(mods)); } catch (Exception ignored) {}
    }

    public static boolean on(String n) { Mod m = mods.get(n); return m != null && m.enabled; }
}
