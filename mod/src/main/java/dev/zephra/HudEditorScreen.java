package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Drag modules to move them, scroll over a module to resize it. */
public class HudEditorScreen extends Screen {
    final Screen parent;
    String drag; int offX, offY;

    public HudEditorScreen(Screen parent) { super(Text.literal("Zephra HUD")); this.parent = parent; }

    static boolean inside(int[] b, double mx, double my) { return mx >= b[0] - 2 && mx <= b[0] + b[2] + 2 && my >= b[1] - 2 && my <= b[1] + b[3] + 2; }

    String pick(double mx, double my) {
        String hit = null;
        for (var e : Hud.BOUNDS.entrySet()) if (inside(e.getValue(), mx, my)) hit = e.getKey();
        return hit;
    }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x66000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        Hud.renderAll(c, client, true);
        for (var e : Hud.BOUNDS.entrySet()) {
            int[] b = e.getValue();
            boolean hot = e.getKey().equals(drag) || (drag == null && e.getKey().equals(pick(mx, my)));
            int col = hot ? 0xFFFFFFFF : 0x77FFFFFF;
            int x1 = b[0] - 2, y1 = b[1] - 2, x2 = b[0] + b[2] + 2, y2 = b[1] + b[3] + 2;
            c.fill(x1, y1, x2, y1 + 1, col); c.fill(x1, y2 - 1, x2, y2, col);
            c.fill(x1, y1, x1 + 1, y2, col); c.fill(x2 - 1, y1, x2, y2, col);
        }
        String hint = "Drag to move   |   Scroll to resize   |   R to reset   |   Esc to finish";
        c.drawText(textRenderer, Fonts.t(hint), (width - textRenderer.getWidth(Fonts.t(hint))) / 2, height - 22, 0xFFFFFFFF, false);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        String n = pick(mx, my);
        if (button == 0 && n != null) {
            drag = n; Config.Mod m = Config.mods.get(n);
            offX = (int) mx - m.x; offY = (int) my - m.y;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drag != null && button == 0) {
            Config.Mod m = Config.mods.get(drag);
            m.x = Math.max(0, Math.min(width - 10, (int) mx - offX));
            m.y = Math.max(0, Math.min(height - 10, (int) my - offY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean mouseReleased(double mx, double my, int button) {
        if (drag != null) { drag = null; Config.save(); return true; }
        return super.mouseReleased(mx, my, button);
    }

    @Override public boolean mouseScrolled(double mx, double my, double hx, double vy) {
        String n = pick(mx, my);
        if (n != null) {
            Config.Mod m = Config.mods.get(n);
            m.scale = Math.max(0.5f, Math.min(3f, Math.round((m.scale + (vy > 0 ? 0.1f : -0.1f)) * 10) / 10f));
            return true;
        }
        return super.mouseScrolled(mx, my, hx, vy);
    }

    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_R) { Config.resetLayout(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    @Override public void close() { Config.save(); client.setScreen(parent); }
}
