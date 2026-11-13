package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Translucent black & white module table. Click a row to toggle; last row edits the HUD layout. */
public class ConfigScreen extends Screen {
    static final int W = 240, ROW = 22, TOP = 34;

    public ConfigScreen() { super(Text.literal("Zephra")); }

    int rows() { return Config.NAMES.length + 1; }
    int panelH() { return TOP + rows() * ROW + 26; }
    int left() { return (width - W) / 2; }
    int top() { return (height - panelH()) / 2; }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x44000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        Hud.renderAll(c, client, false);
        int x = left(), y = top(), h = panelH();
        c.fill(x - 1, y - 1, x + W + 1, y + h + 1, 0x77FFFFFF);
        c.fill(x, y, x + W, y + h, 0xA6000000);
        c.drawText(textRenderer, "ZEPHRA", x + 12, y + 13, 0xFFFFFFFF, false);
        for (int i = 0; i < rows(); i++) {
            int ry = y + TOP + i * ROW;
            boolean hover = mx >= x && mx < x + W && my >= ry && my < ry + ROW;
            if (hover) c.fill(x + 1, ry, x + W - 1, ry + ROW, 0x33FFFFFF);
            if (i == Config.NAMES.length) {
                c.drawText(textRenderer, "Edit HUD layout", x + 12, ry + 7, 0xFFFFFFFF, false);
                c.drawText(textRenderer, ">", x + W - 22, ry + 7, 0xFFFFFFFF, false);
                continue;
            }
            c.drawText(textRenderer, Config.LABELS[i], x + 12, ry + 7, 0xFFFFFFFF, false);
            boolean on = Config.mods.get(Config.NAMES[i]).enabled;
            int px = x + W - 46, py = ry + 4;
            if (on) { c.fill(px, py, px + 34, py + 14, 0xFFFFFFFF); c.drawText(textRenderer, "ON", px + 11, py + 3, 0xFF000000, false); }
            else { c.fill(px, py, px + 34, py + 14, 0xFF666666); c.fill(px + 1, py + 1, px + 33, py + 13, 0xFF000000); c.drawText(textRenderer, "OFF", px + 9, py + 3, 0xFF999999, false); }
        }
        c.drawText(textRenderer, "Right Shift closes", x + 12, y + h - 16, 0xFF999999, false);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        int x = left(), y = top();
        if (button == 0 && mx >= x && mx < x + W && my >= y + TOP) {
            int i = (int) ((my - (y + TOP)) / ROW);
            if (i == Config.NAMES.length) { client.setScreen(new HudEditorScreen(this)); return true; }
            if (i >= 0 && i < Config.NAMES.length) {
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

    @Override public void close() { Config.save(); super.close(); }
}
