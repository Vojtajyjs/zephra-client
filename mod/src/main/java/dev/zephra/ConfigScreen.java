package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Zephra mod menu: translucent panel, rounded cards, animated switches. */
public class ConfigScreen extends Screen {
    static final int PW = 330, PAD = 14, HEAD = 46, CH = 36, GAP = 6, FOOT = 22;
    static final int N = Config.NAMES.length, CELLS = N + 2, ROWS = (CELLS + 1) / 2;
    static final int PH = HEAD + ROWS * CH + (ROWS - 1) * GAP + FOOT;
    static final int CW = (PW - 2 * PAD - GAP) / 2;
    static final String[] SPEC_T = {"Edit HUD layout", "Crosshair editor"};
    static final String[] SPEC_D = {"Move and resize", "Style, size, color"};
    final float[] anim = new float[N];
    final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Zephra"));
        this.parent = parent;
        for (int i = 0; i < N; i++) anim[i] = Config.mods.get(Config.NAMES[i]).enabled ? 1f : 0f;
    }

    float sc() { return Math.min(1f, (height - 12f) / PH); }
    float ox() { return (width - PW * sc()) / 2f; }
    float oy() { return (height - PH * sc()) / 2f; }
    static int[] cell(int i) { return new int[]{PAD + (i % 2) * (CW + GAP), HEAD + (i / 2) * (CH + GAP)}; }
    static boolean in(int[] p, double x, double y) { return x >= p[0] && x < p[0] + CW && y >= p[1] && y < p[1] + CH; }

    /** Filled rounded rectangle drawn as 1px rows, so translucent colors never overlap. */
    static void rr(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int dy = i < r ? r - i : (i >= h - r ? i - (h - r) + 1 : 0);
            int inset = 0;
            if (dy > 0) {
                double dx = Math.sqrt(Math.max(0.0, (double) r * r - (dy - 0.5) * (dy - 0.5)));
                inset = r - (int) Math.round(dx);
            }
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
        }
    }

    void txt(DrawContext c, String s, int x, int y, int col, float scale) {
        var m = c.getMatrices();
        m.push(); m.translate(x, y, 0); m.scale(scale, scale, 1f);
        c.drawText(textRenderer, Fonts.t(s), 0, 0, col, false);
        m.pop();
    }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x40000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        Hud.renderAll(c, client, false);
        float sc = sc(), ox = ox(), oy = oy();
        double lx = (mx - ox) / sc, ly = (my - oy) / sc;
        var m = c.getMatrices();
        m.push(); m.translate(ox, oy, 0); m.scale(sc, sc, 1f);

        rr(c, 0, 0, PW, PH, 10, 0xB00A0A0A);
        TitleArt.tex(c, TitleArt.LOGO, PAD, 11, 100, 24, 1000, 240);
        String sub = "MOD MENU";
        txt(c, sub, PW - PAD - (int) (textRenderer.getWidth(Fonts.t(sub)) * 0.8f), 20, 0xFF8A8A8A, 0.8f);

        for (int i = 0; i < CELLS; i++) {
            int[] p = cell(i);
            boolean hover = in(p, lx, ly);
            if (i >= N) {
                rr(c, p[0], p[1], CW, CH, 7, hover ? 0xFFE6E6E6 : 0xFFFFFFFF);
                txt(c, SPEC_T[i - N], p[0] + 10, p[1] + 8, 0xFF000000, 1f);
                txt(c, SPEC_D[i - N], p[0] + 10, p[1] + 21, 0xFF555555, 0.8f);
                txt(c, ">", p[0] + CW - 16, p[1] + 14, 0xFF000000, 1f);
                continue;
            }
            Config.Mod mod = Config.mods.get(Config.NAMES[i]);
            rr(c, p[0], p[1], CW, CH, 7, hover ? 0x3AFFFFFF : 0x24FFFFFF);
            txt(c, Config.LABELS[i], p[0] + 10, p[1] + 8, 0xFFFFFFFF, 1f);
            txt(c, Config.DESCS[i], p[0] + 10, p[1] + 21, 0xFF9A9A9A, 0.8f);
            anim[i] += ((mod.enabled ? 1f : 0f) - anim[i]) * 0.3f;
            int sx = p[0] + CW - 38, sy = p[1] + (CH - 14) / 2;
            rr(c, sx, sy, 28, 14, 7, mod.enabled ? 0xFFFFFFFF : 0xFF3C3C3C);
            rr(c, sx + 2 + Math.round(anim[i] * 14), sy + 2, 10, 10, 5, mod.enabled ? 0xFF000000 : 0xFFB4B4B4);
        }
        String hint = "Right Shift or Esc to close";
        txt(c, hint, (PW - (int) (textRenderer.getWidth(Fonts.t(hint)) * 0.8f)) / 2, PH - 16, 0xFF6E6E6E, 0.8f);
        m.pop();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            double lx = (mx - ox()) / sc(), ly = (my - oy()) / sc();
            for (int i = 0; i < CELLS; i++) {
                if (!in(cell(i), lx, ly)) continue;
                if (i == N) { client.setScreen(new HudEditorScreen(this)); return true; }
                if (i == N + 1) { client.setScreen(new CrosshairScreen(this)); return true; }
                Config.Mod m = Config.mods.get(Config.NAMES[i]);
                m.enabled = !m.enabled;
                Config.save();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    @Override public void close() { Config.save(); client.setScreen(parent); }
}
