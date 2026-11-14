package dev.zephra;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

/** Smooth rounded shapes drawn from 64x64 textures (corner radius 16), plus text helpers. */
public class Ui {
    public static Identifier t(String n) { return Identifier.of("zephra", "textures/gui/" + n + ".png"); }

    static void blit(DrawContext c, Identifier id, int x, int y, int w, int h, int u, int v, int rw, int rh) {
        if (w <= 0 || h <= 0) return;
        c.drawTexture(RenderLayer::getGuiTextured, id, x, y, (float) u, (float) v, w, h, rw, rh, 64, 64);
    }

    public static void rr(DrawContext c, String tex, int x, int y, int w, int h, int r) {
        Identifier id = t(tex);
        r = Math.min(r, Math.min(w, h) / 2);
        blit(c, id, x, y, r, r, 0, 0, 16, 16);
        blit(c, id, x + w - r, y, r, r, 48, 0, 16, 16);
        blit(c, id, x, y + h - r, r, r, 0, 48, 16, 16);
        blit(c, id, x + w - r, y + h - r, r, r, 48, 48, 16, 16);
        blit(c, id, x + r, y, w - 2 * r, r, 16, 0, 32, 16);
        blit(c, id, x + r, y + h - r, w - 2 * r, r, 16, 48, 32, 16);
        blit(c, id, x, y + r, r, h - 2 * r, 0, 16, 16, 32);
        blit(c, id, x + w - r, y + r, r, h - 2 * r, 48, 16, 16, 32);
        blit(c, id, x + r, y + r, w - 2 * r, h - 2 * r, 16, 16, 32, 32);
    }

    public static void img(DrawContext c, String name, int x, int y, int w, int h) {
        c.drawTexture(RenderLayer::getGuiTextured, t(name), x, y, 0f, 0f, w, h, 64, 64, 64, 64);
    }

    public static void txt(DrawContext c, TextRenderer tr, String s, int x, int y, int col, float scale) {
        var m = c.getMatrices();
        m.push(); m.translate(x, y, 0); m.scale(scale, scale, 1f);
        c.drawText(tr, Fonts.t(s), 0, 0, col, false);
        m.pop();
    }

    public static int tw(TextRenderer tr, String s, float scale) { return (int) (tr.getWidth(Fonts.t(s)) * scale); }

    /** Switch: smooth pill and knob. anim goes 0..1. */
    public static void toggle(DrawContext c, int x, int y, boolean on, float anim) {
        rr(c, on ? "rr_white" : "rr_dark", x, y, 28, 14, 7);
        rr(c, on ? "rr_black" : "rr_gray", x + 2 + Math.round(anim * 14), y + 2, 10, 10, 5);
    }
}
