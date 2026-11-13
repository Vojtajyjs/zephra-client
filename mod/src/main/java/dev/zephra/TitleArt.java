package dev.zephra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

public class TitleArt {
    /** Filled pointy-top hexagon drawn with 1px rows. */
    public static void hex(DrawContext c, int cx, int cy, int r, int color) {
        double hwMax = r * 0.866;
        for (int dy = -r; dy <= r; dy++) {
            double ady = Math.abs(dy);
            double hw = ady <= r / 2.0 ? hwMax : hwMax * (r - ady) / (r / 2.0);
            int h = (int) hw;
            if (h > 0) c.fill(cx - h, cy + dy, cx + h, cy + dy + 1, color);
        }
    }

    public static void background(DrawContext c) {
        var w = MinecraftClient.getInstance().getWindow();
        int sw = w.getScaledWidth(), sh = w.getScaledHeight();
        c.fillGradient(0, 0, sw, sh, 0xFF000000, 0xFF0F0F0F);
        hex(c, sw * 82 / 100, sh * 48 / 100, sh * 55 / 100, 0xFF0B0B0B);
        hex(c, sw * 14 / 100, sh * 88 / 100, sh * 26 / 100, 0xFF090909);
    }

    public static void logo(DrawContext c, int screenWidth, float alpha, int y) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int a = Math.max(0, Math.min(255, (int) (alpha * 255))) << 24;
        int white = a | 0xFFFFFF, black = a;
        String txt = "Z E P H R A";
        int scale = 3, textW = tr.getWidth(txt) * scale, r = 24, markW = (int) (r * 1.732), gap = 16;
        int total = markW + gap + textW;
        int x0 = (screenWidth - total) / 2, cx = x0 + markW / 2, cy = y + r + 4;
        hex(c, cx, cy, r, white);
        hex(c, cx, cy, r - 3, black);
        var m = c.getMatrices();
        m.push(); m.translate(cx - 6, cy - 8, 0); m.scale(2, 2, 1);
        c.drawText(tr, "Z", 0, 0, white, false);
        m.pop();
        m.push(); m.translate(x0 + markW + gap, cy - 13, 0); m.scale(scale, scale, 1);
        c.drawText(tr, txt, 0, 0, white, false);
        m.pop();
    }
}
