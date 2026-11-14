package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Zephra mod menu: translucent panel, smooth rounded cards, animated switches. */
public class ConfigScreen extends Screen {
    static final int PW = 330, PAD = 14, HEAD = 46, CH = 36, GAP = 6, FOOT = 22;
    static final int N = Config.NAMES.length, CELLS = N + 4, ROWS = (CELLS + 1) / 2;
    static final int PH = HEAD + ROWS * CH + (ROWS - 1) * GAP + FOOT;
    static final int CW = (PW - 2 * PAD - GAP) / 2;
    static final String[] SPEC_T = {"Edit HUD layout", "Crosshair editor", "HUD colors", "Optimize FPS"};
    static final String[] SPEC_D = {"Move and resize", "Style, size, color", "Text and keys", "Faster, still smooth"};
    final float[] anim = new float[N];
    final Screen parent;
    long appliedAt;

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

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x40000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        Hud.renderAll(c, client, false);
        float sc = sc(), ox = ox(), oy = oy();
        double lx = (mx - ox) / sc, ly = (my - oy) / sc;
        var m = c.getMatrices();
        m.push(); m.translate(ox, oy, 0); m.scale(sc, sc, 1f);

        Ui.rr(c, "rr_panel", 0, 0, PW, PH, 12);
        TitleArt.tex(c, TitleArt.LOGO, PAD, 11, 100, 24, 1000, 240);
        Ui.txt(c, textRenderer, "MOD MENU", PW - PAD - Ui.tw(textRenderer, "MOD MENU", 0.8f), 20, 0xFF8A8A8A, 0.8f);

        for (int i = 0; i < CELLS; i++) {
            int[] p = cell(i);
            boolean hover = in(p, lx, ly);
            if (i >= N) {
                Ui.rr(c, hover ? "rr_whitehi" : "rr_white", p[0], p[1], CW, CH, 8);
                Ui.txt(c, textRenderer, SPEC_T[i - N], p[0] + 10, p[1] + 8, 0xFF000000, 1f);
                Ui.txt(c, textRenderer, i == N + 3 && System.currentTimeMillis() - appliedAt < 2500 ? "Applied!" : SPEC_D[i - N], p[0] + 10, p[1] + 21, 0xFF555555, 0.8f);
                Ui.txt(c, textRenderer, ">", p[0] + CW - 16, p[1] + 14, 0xFF000000, 1f);
                continue;
            }
            Config.Mod mod = Config.mods.get(Config.NAMES[i]);
            Ui.rr(c, hover ? "rr_cardhi" : "rr_card", p[0], p[1], CW, CH, 8);
            Ui.txt(c, textRenderer, Config.LABELS[i], p[0] + 10, p[1] + 8, 0xFFFFFFFF, 1f);
            Ui.txt(c, textRenderer, Config.DESCS[i], p[0] + 10, p[1] + 21, 0xFF9A9A9A, 0.8f);
            anim[i] += ((mod.enabled ? 1f : 0f) - anim[i]) * 0.3f;
            Ui.toggle(c, p[0] + CW - 38, p[1] + (CH - 14) / 2, mod.enabled, anim[i]);
        }
        String hint = "Right Shift or Esc to close";
        Ui.txt(c, textRenderer, hint, (PW - Ui.tw(textRenderer, hint, 0.8f)) / 2, PH - 16, 0xFF6E6E6E, 0.8f);
        m.pop();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            double lx = (mx - ox()) / sc(), ly = (my - oy()) / sc();
            for (int i = 0; i < CELLS; i++) {
                if (!in(cell(i), lx, ly)) continue;
                if (i == N) client.setScreen(new HudEditorScreen(this));
                else if (i == N + 1) client.setScreen(new CrosshairScreen(this));
                else if (i == N + 2) client.setScreen(new ColorScreen(this));
                else if (i == N + 3) { Perf.apply(client); appliedAt = System.currentTimeMillis(); }
                else { Config.Mod m = Config.mods.get(Config.NAMES[i]); m.enabled = !m.enabled; Config.save(); }
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
