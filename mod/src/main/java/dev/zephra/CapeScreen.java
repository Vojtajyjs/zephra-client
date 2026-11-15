package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import java.util.UUID;

/** Cape picker. Capes are only visible to other Zephra users. */
public class CapeScreen extends Screen {
    static final int PW = 360, PH = 230, CW = 76, CHT = 124, GAP = 10, X0 = 13, Y0 = 56;
    static final String[] IDS = {"none", "black", "white", "grad"};
    static final String[] NAMES = {"None", "Black", "White", "Gradient"};
    final Screen parent;

    public CapeScreen(Screen parent) { super(Text.literal("Capes")); this.parent = parent; }

    float sc() { return Math.min(1f, (height - 12f) / PH); }
    float ox() { return (width - PW * sc()) / 2f; }
    float oy() { return (height - PH * sc()) / 2f; }

    String current() {
        UUID me = client.getSession().getUuidOrNull();
        String c = me == null ? null : ZephraNet.users.get(me);
        return c == null ? "none" : c;
    }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x40000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        float sc = sc();
        double lx = (mx - ox()) / sc, ly = (my - oy()) / sc;
        var mat = c.getMatrices();
        mat.push(); mat.translate(ox(), oy(), 0); mat.scale(sc, sc, 1f);
        Ui.rr(c, "rr_panel", 0, 0, PW, PH, 12);
        TitleArt.tex(c, TitleArt.LOGO, 14, 11, 100, 24, 1000, 240);
        Ui.txt(c, textRenderer, "CAPES", PW - 14 - Ui.tw(textRenderer, "CAPES", 0.8f), 20, 0xFF8A8A8A, 0.8f);
        String cur = current();
        for (int i = 0; i < IDS.length; i++) {
            int x = X0 + i * (CW + GAP), y = Y0;
            boolean sel = IDS[i].equals(cur), hov = lx >= x && lx < x + CW && ly >= y && ly < y + CHT;
            Ui.rr(c, sel ? "rr_white" : hov ? "rr_cardhi" : "rr_card", x, y, CW, CHT, 8);
            int fg = sel ? 0xFF000000 : 0xFFFFFFFF;
            if (i == 0) Ui.txt(c, textRenderer, "No cape", x + (CW - Ui.tw(textRenderer, "No cape", 1f)) / 2, y + 48, sel ? 0xFF555555 : 0xFF8A8A8A, 1f);
            else c.drawTexture(RenderLayer::getGuiTextured, Capes.id(IDS[i]), x + (CW - 40) / 2, y + 12, 4f, 4f, 40, 64, 40, 64, 256, 128);
            Ui.txt(c, textRenderer, NAMES[i], x + (CW - Ui.tw(textRenderer, NAMES[i], 1f)) / 2, y + CHT - 20, fg, 1f);
        }
        String note = ZephraNet.API.isEmpty() ? "The Zephra network is not set up yet."
            : !ZephraNet.ALLOWED ? "Turn on Zephra network in the launcher settings to use capes."
            : ZephraNet.status;
        Ui.txt(c, textRenderer, note, (PW - Ui.tw(textRenderer, note, 0.8f)) / 2, PH - 30, 0xFF8A8A8A, 0.8f);
        Ui.txt(c, textRenderer, "Esc to go back", (PW - Ui.tw(textRenderer, "Esc to go back", 0.8f)) / 2, PH - 16, 0xFF6E6E6E, 0.8f);
        mat.pop();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            double lx = (mx - ox()) / sc(), ly = (my - oy()) / sc();
            for (int i = 0; i < IDS.length; i++) {
                int x = X0 + i * (CW + GAP);
                if (lx >= x && lx < x + CW && ly >= Y0 && ly < Y0 + CHT) { ZephraNet.setCape(IDS[i]); return true; }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public void close() { client.setScreen(parent); }
}
