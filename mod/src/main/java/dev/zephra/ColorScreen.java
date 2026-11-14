package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Pick a color for each HUD module. */
public class ColorScreen extends Screen {
    static final String[] KEYS = {"fps", "cps", "ping", "keystrokes", "armor"};
    static final String[] LAB = {"FPS", "CPS", "Ping", "Keystrokes", "Armor"};
    static final int PW = 340, PH = 250, X = 22, SX = 118, ROW0 = 58, ROWH = 32;
    final Screen parent;

    public ColorScreen(Screen parent) { super(Text.literal("HUD colors")); this.parent = parent; }

    float sc() { return Math.min(1f, (height - 12f) / PH); }
    float ox() { return (width - PW * sc()) / 2f; }
    float oy() { return (height - PH * sc()) / 2f; }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x40000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        var mat = c.getMatrices();
        mat.push(); mat.translate(ox(), oy(), 0); mat.scale(sc(), sc(), 1f);
        Ui.rr(c, "rr_panel", 0, 0, PW, PH, 12);
        TitleArt.tex(c, TitleArt.LOGO, X, 11, 100, 24, 1000, 240);
        Ui.txt(c, textRenderer, "HUD COLORS", PW - X - Ui.tw(textRenderer, "HUD COLORS", 0.8f), 20, 0xFF8A8A8A, 0.8f);
        for (int i = 0; i < KEYS.length; i++) {
            int y = ROW0 + i * ROWH;
            Config.Mod m = Config.mods.get(KEYS[i]);
            Ui.txt(c, textRenderer, LAB[i], X, y + 8, m.color | 0xFF000000, 1f);
            for (int j = 0; j < Palette.COLORS.length; j++) {
                int sx = SX + j * 21;
                if (m.color == Palette.COLORS[j]) Ui.rr(c, "rr_white", sx - 3, y + 2, 22, 22, 11);
                else Ui.rr(c, "rr_dark", sx - 1, y + 4, 18, 18, 9);
                Ui.rr(c, "sw" + j, sx, y + 5, 16, 16, 8);
            }
        }
        Ui.txt(c, textRenderer, "Esc to go back", (PW - Ui.tw(textRenderer, "Esc to go back", 0.8f)) / 2, PH - 16, 0xFF6E6E6E, 0.8f);
        mat.pop();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            double lx = (mx - ox()) / sc(), ly = (my - oy()) / sc();
            for (int i = 0; i < KEYS.length; i++) {
                int y = ROW0 + i * ROWH;
                for (int j = 0; j < Palette.COLORS.length; j++) {
                    int sx = SX + j * 21;
                    if (lx >= sx - 2 && lx < sx + 19 && ly >= y + 3 && ly < y + 24) {
                        Config.mods.get(KEYS[i]).color = Palette.COLORS[j];
                        Config.save();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public void close() { Config.save(); client.setScreen(parent); }
}
