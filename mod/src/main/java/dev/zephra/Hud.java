package dev.zephra;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import java.util.ArrayDeque;

public class Hud {
    static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();

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
            TextRenderer tr = mc.textRenderer;
            Config.Mod m;
            if (Config.on("fps")) { m = Config.mods.get("fps"); ctx.drawTextWithShadow(tr, mc.getCurrentFps() + " FPS", m.x, m.y, 0xFFFFFFFF); }
            if (Config.on("cps")) { m = Config.mods.get("cps"); ctx.drawTextWithShadow(tr, cps(LEFT) + " | " + cps(RIGHT) + " CPS", m.x, m.y, 0xFFFFFFFF); }
            if (Config.on("coords")) { m = Config.mods.get("coords"); var p = mc.player; ctx.drawTextWithShadow(tr, "XYZ " + p.getBlockX() + " " + p.getBlockY() + " " + p.getBlockZ() + "  " + p.getHorizontalFacing().asString().toUpperCase(), m.x, m.y, 0xFFFFFFFF); }
            if (Config.on("keystrokes")) keys(ctx, tr, mc, Config.mods.get("keystrokes"));
            if (Config.on("armor")) armor(ctx, tr, mc, Config.mods.get("armor"));
        });
    }

    static void key(DrawContext c, TextRenderer tr, int x, int y, int w, int h, String label, boolean down) {
        c.fill(x, y, x + w, y + h, down ? 0xB0FFFFFF : 0x70000000);
        c.drawCenteredTextWithShadow(tr, label, x + w / 2, y + (h - 8) / 2, down ? 0xFF000000 : 0xFFFFFFFF);
    }

    static void keys(DrawContext c, TextRenderer tr, MinecraftClient mc, Config.Mod m) {
        var o = mc.options; int s = 20, g = 2, x = m.x, y = m.y;
        key(c, tr, x + s + g, y, s, s, "W", o.forwardKey.isPressed());
        key(c, tr, x, y + s + g, s, s, "A", o.leftKey.isPressed());
        key(c, tr, x + s + g, y + s + g, s, s, "S", o.backKey.isPressed());
        key(c, tr, x + 2 * (s + g), y + s + g, s, s, "D", o.rightKey.isPressed());
        int w = (3 * s + 2 * g - g) / 2;
        key(c, tr, x, y + 2 * (s + g), w, s, "LMB", o.attackKey.isPressed());
        key(c, tr, x + w + g, y + 2 * (s + g), w, s, "RMB", o.useKey.isPressed());
    }

    static void armor(DrawContext c, TextRenderer tr, MinecraftClient mc, Config.Mod m) {
        int y = m.y;
        for (int i = 3; i >= 0; i--) {
            ItemStack st = mc.player.getInventory().getArmorStack(i);
            if (st.isEmpty()) continue;
            c.drawItem(st, m.x, y);
            if (st.isDamageable()) c.drawTextWithShadow(tr, String.valueOf(st.getMaxDamage() - st.getDamage()), m.x + 20, y + 4, 0xFFFFFFFF);
            y += 18;
        }
    }
}
