package dev.zephra;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

public class Hud {
    static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();
    public static final Map<String, int[]> BOUNDS = new LinkedHashMap<>();
    static final String[] HUD = {"fps", "cps", "ping", "keystrokes", "armor"};

    public static void click(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) LEFT.add(now); else if (button == 1) RIGHT.add(now);
    }

    static int cps(ArrayDeque<Long> q) {
        long now = System.currentTimeMillis();
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        return q.size();
    }

    public static void init() {
        HudRenderCallback.EVENT.register((ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden || mc.currentScreen != null) return;
            renderAll(ctx, mc, false);
            if (Config.on("crosshair") && mc.options.getPerspective().isFirstPerson())
                Crosshair.draw(ctx, ctx.getScaledWindowWidth() / 2, ctx.getScaledWindowHeight() / 2);
        });
    }

    /** Draws every enabled module at its saved position and scale, and records its bounds. */
    public static void renderAll(DrawContext ctx, MinecraftClient mc, boolean editing) {
        BOUNDS.clear();
        if (mc == null || mc.player == null) return;
        for (String n : HUD) {
            if (!Config.on(n)) continue;
            Config.Mod m = Config.mods.get(n);
            var mat = ctx.getMatrices();
            mat.push();
            mat.translate(m.x, m.y, 0);
            mat.scale(m.scale, m.scale, 1f);
            int[] sz = draw(n, ctx, mc);
            mat.pop();
            BOUNDS.put(n, new int[]{m.x, m.y, Math.round(sz[0] * m.scale), Math.round(sz[1] * m.scale)});
        }
    }

    static int[] draw(String n, DrawContext c, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        switch (n) {
            case "fps": return text(c, tr, mc.getCurrentFps() + " FPS");
            case "cps": return text(c, tr, cps(LEFT) + " | " + cps(RIGHT) + " CPS");
            case "ping": {
                var nh = mc.getNetworkHandler();
                var e = nh == null ? null : nh.getPlayerListEntry(mc.player.getUuid());
                return text(c, tr, (e == null || mc.isInSingleplayer() ? "--" : String.valueOf(e.getLatency())) + " ms");
            }
            case "keystrokes": keys(c, tr, mc); return new int[]{64, 64};
            default: return armor(c, tr, mc);
        }
    }

    static int[] text(DrawContext c, TextRenderer tr, String s) {
        var t = Fonts.t(s);
        c.drawTextWithShadow(tr, t, 0, 0, 0xFFFFFFFF);
        return new int[]{tr.getWidth(t), 9};
    }

    static void key(DrawContext c, TextRenderer tr, int x, int y, int w, int h, String label, boolean down) {
        c.fill(x, y, x + w, y + h, down ? 0xFFFFFFFF : 0x70000000);
        if (down) c.drawText(tr, Fonts.t(label), x + (w - tr.getWidth(Fonts.t(label))) / 2, y + (h - 8) / 2, 0xFF000000, false);
        else c.drawText(tr, Fonts.t(label), x + (w - tr.getWidth(Fonts.t(label))) / 2, y + (h - 8) / 2, 0xFFFFFFFF, true);
    }

    static void keys(DrawContext c, TextRenderer tr, MinecraftClient mc) {
        var o = mc.options; int s = 20, g = 2;
        key(c, tr, s + g, 0, s, s, "W", o.forwardKey.isPressed());
        key(c, tr, 0, s + g, s, s, "A", o.leftKey.isPressed());
        key(c, tr, s + g, s + g, s, s, "S", o.backKey.isPressed());
        key(c, tr, 2 * (s + g), s + g, s, s, "D", o.rightKey.isPressed());
        int w = (3 * s + 2 * g - g) / 2;
        key(c, tr, 0, 2 * (s + g), w, s, "LMB", o.attackKey.isPressed());
        key(c, tr, w + g, 2 * (s + g), w, s, "RMB", o.useKey.isPressed());
    }

    static int[] armor(DrawContext c, TextRenderer tr, MinecraftClient mc) {
        int y = 0;
        for (int i = 3; i >= 0; i--) {
            ItemStack st = mc.player.getInventory().getArmorStack(i);
            if (st.isEmpty()) continue;
            c.drawItem(st, 0, y);
            if (st.isDamageable()) c.drawTextWithShadow(tr, Fonts.t(String.valueOf(st.getMaxDamage() - st.getDamage())), 20, y + 4, 0xFFFFFFFF);
            y += 18;
        }
        return new int[]{52, Math.max(y, 18)};
    }
}
