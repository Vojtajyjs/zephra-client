package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Crosshair editor: smooth controls on the left, big live preview on the right. */
public class CrosshairScreen extends Screen {
    static final int PW = 440, PH = 300, X = 22, TX = 100, TW = 120;
    static final int R_EN = 58, R_STYLE = 92, R_COLOR = 126, R_OUT = 160;
    static final int[] SY = {196, 224, 252};
    static final String[] SL = {"Size", "Thickness", "Gap"};
    static final int[] MIN = {2, 1, 0}, MAX = {20, 4, 8};
    final Screen parent;
    int drag = -1;
    float enA, outA;

    public CrosshairScreen(Screen parent) {
        super(Text.literal("Crosshair"));
        this.parent = parent;
        enA = m().enabled ? 1f : 0f; outA = m().outline ? 1f : 0f;
    }

    Config.Mod m() { return Config.mods.get("crosshair"); }
    float sc() { return Math.min(1f, (height - 12f) / PH); }
    float ox() { return (width - PW * sc()) / 2f; }
    float oy() { return (height - PH * sc()) / 2f; }
    static boolean in(double x, double y, int rx, int ry, int rw, int rh) { return x >= rx && x < rx + rw && y >= ry && y < ry + rh; }
    int val(int i) { var m = m(); return i == 0 ? m.size : i == 1 ? m.thick : m.gap; }
    void set(int i, int v) { var m = m(); if (i == 0) m.size = v; else if (i == 1) m.thick = v; else m.gap = v; }
    int colorIndex() { for (int i = 0; i < Crosshair.COLORS.length; i++) if (Crosshair.COLORS[i] == m().color) return i; return 0; }
    void slide(int i, double lx) {
        double f = Math.max(0, Math.min(1, (lx - TX) / TW));
        set(i, MIN[i] + (int) Math.round(f * (MAX[i] - MIN[i])));
    }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x40000000); }

    void label(DrawContext c, String s, int row) { Ui.txt(c, textRenderer, s, X, row + 7, 0xFFFFFFFF, 1f); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        float sc = sc(), ox = ox(), oy = oy();
        double lx = (mx - ox) / sc, ly = (my - oy) / sc;
        var mat = c.getMatrices();
        mat.push(); mat.translate(ox, oy, 0); mat.scale(sc, sc, 1f);
        Config.Mod m = m();

        Ui.rr(c, "rr_panel", 0, 0, PW, PH, 12);
        TitleArt.tex(c, TitleArt.LOGO, X, 11, 100, 24, 1000, 240);
        Ui.txt(c, textRenderer, "CROSSHAIR", PW - X - Ui.tw(textRenderer, "CROSSHAIR", 0.8f), 20, 0xFF8A8A8A, 0.8f);

        enA += ((m.enabled ? 1f : 0f) - enA) * 0.3f; outA += ((m.outline ? 1f : 0f) - outA) * 0.3f;
        label(c, "Custom crosshair", R_EN); Ui.toggle(c, 226, R_EN + 5, m.enabled, enA);
        label(c, "Style", R_STYLE);
        Ui.rr(c, "rr_card", TX, R_STYLE + 1, 156, 22, 11);
        for (int i = 0; i < 3; i++) {
            boolean sel = m.style == i;
            if (sel) Ui.rr(c, "rr_white", TX + i * 52, R_STYLE + 1, 52, 22, 11);
            String s = Crosshair.STYLES[i];
            Ui.txt(c, textRenderer, s, TX + i * 52 + (52 - Ui.tw(textRenderer, s, 1f)) / 2, R_STYLE + 8, sel ? 0xFF000000 : 0xFFBBBBBB, 1f);
        }
        label(c, "Color", R_COLOR);
        for (int i = 0; i < Crosshair.COLORS.length; i++) {
            int sx = TX + i * 22;
            if (colorIndex() == i) Ui.rr(c, "rr_white", sx - 3, R_COLOR + 1, 22, 22, 11);
            else Ui.rr(c, "rr_dark", sx - 1, R_COLOR + 3, 18, 18, 9);
            Ui.rr(c, "sw" + i, sx, R_COLOR + 4, 16, 16, 8);
        }
        label(c, "Outline", R_OUT); Ui.toggle(c, 226, R_OUT + 5, m.outline, outA);
        for (int i = 0; i < 3; i++) {
            int y = SY[i];
            label(c, SL[i], y);
            double f = (val(i) - MIN[i]) / (double) (MAX[i] - MIN[i]);
            Ui.rr(c, "rr_dark", TX, y + 10, TW, 6, 3);
            Ui.rr(c, "rr_white", TX, y + 10, Math.max(6, (int) (TW * f)), 6, 3);
            Ui.rr(c, "rr_white", TX + (int) (TW * f) - 6, y + 7, 12, 12, 6);
            Ui.txt(c, textRenderer, String.valueOf(val(i)), 232, y + 7, 0xFFBBBBBB, 1f);
        }

        Ui.rr(c, "rr_card", 280, 58, 138, 150, 10);
        mat.push(); mat.translate(349, 130, 0); mat.scale(3f, 3f, 1f);
        Crosshair.draw(c, 0, 0);
        mat.pop();
        Ui.txt(c, textRenderer, "Preview", 280 + (138 - Ui.tw(textRenderer, "Preview", 0.8f)) / 2, 192, 0xFF8A8A8A, 0.8f);
        boolean hov = in(lx, ly, 280, 220, 138, 28);
        Ui.rr(c, hov ? "rr_cardhi" : "rr_card", 280, 220, 138, 28, 10);
        Ui.txt(c, textRenderer, "Reset to default", 280 + (138 - Ui.tw(textRenderer, "Reset to default", 1f)) / 2, 230, 0xFFFFFFFF, 1f);
        Ui.txt(c, textRenderer, "Esc to go back", (PW - Ui.tw(textRenderer, "Esc to go back", 0.8f)) / 2, PH - 16, 0xFF6E6E6E, 0.8f);
        mat.pop();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        double lx = (mx - ox()) / sc(), ly = (my - oy()) / sc();
        Config.Mod m = m();
        if (in(lx, ly, X, R_EN, 236, 24)) { m.enabled = !m.enabled; return true; }
        if (in(lx, ly, X, R_OUT, 236, 24)) { m.outline = !m.outline; return true; }
        if (in(lx, ly, TX, R_STYLE, 156, 24)) { m.style = Math.max(0, Math.min(2, (int) ((lx - TX) / 52))); return true; }
        for (int i = 0; i < Crosshair.COLORS.length; i++)
            if (in(lx, ly, TX + i * 22 - 2, R_COLOR, 20, 24)) { m.color = Crosshair.COLORS[i]; return true; }
        for (int i = 0; i < 3; i++)
            if (in(lx, ly, TX - 6, SY[i], TW + 12, 26)) { drag = i; slide(i, lx); return true; }
        if (in(lx, ly, 280, 220, 138, 28)) { m.style = 0; m.size = 6; m.thick = 2; m.gap = 3; m.color = 0xFFFFFFFF; m.outline = true; return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drag >= 0) { slide(drag, (mx - ox()) / sc()); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean mouseReleased(double mx, double my, int button) {
        if (drag >= 0) { drag = -1; Config.save(); return true; }
        return super.mouseReleased(mx, my, button);
    }

    @Override public void close() { Config.save(); client.setScreen(parent); }
}
