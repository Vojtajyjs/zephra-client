package dev.zephra;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.IntConsumer;

/** Crosshair editor with a live preview in the middle of the screen. */
public class CrosshairScreen extends Screen {
    final Screen parent;

    public CrosshairScreen(Screen parent) { super(Text.literal("Crosshair")); this.parent = parent; }

    Config.Mod m() { return Config.mods.get("crosshair"); }
    static Text label(String a, String b) { return Fonts.t(a + ": " + b); }
    int colorIndex() { for (int i = 0; i < Crosshair.COLORS.length; i++) if (Crosshair.COLORS[i] == m().color) return i; return 0; }

    static class Slide extends SliderWidget {
        final String label; final int min, max; final IntConsumer set;
        Slide(int x, int y, int w, int h, String label, int min, int max, int val, IntConsumer set) {
            super(x, y, w, h, Text.empty(), (val - min) / (double) (max - min));
            this.label = label; this.min = min; this.max = max; this.set = set;
            updateMessage();
        }
        int cur() { return min + (int) Math.round(value * (max - min)); }
        @Override protected void updateMessage() { if (label != null) setMessage(Fonts.t(label + ": " + cur())); }
        @Override protected void applyValue() { set.accept(cur()); }
    }

    @Override protected void init() {
        int x = 24, y = 30, w = 170, h = 20, step = 24;
        addDrawableChild(ButtonWidget.builder(label("Custom crosshair", m().enabled ? "ON" : "OFF"), b -> {
            m().enabled = !m().enabled; b.setMessage(label("Custom crosshair", m().enabled ? "ON" : "OFF"));
        }).dimensions(x, y, w, h).build()); y += step;
        addDrawableChild(ButtonWidget.builder(label("Style", Crosshair.STYLES[m().style]), b -> {
            m().style = (m().style + 1) % Crosshair.STYLES.length; b.setMessage(label("Style", Crosshair.STYLES[m().style]));
        }).dimensions(x, y, w, h).build()); y += step;
        addDrawableChild(ButtonWidget.builder(label("Color", Crosshair.COLOR_NAMES[colorIndex()]), b -> {
            int i = (colorIndex() + 1) % Crosshair.COLORS.length; m().color = Crosshair.COLORS[i]; b.setMessage(label("Color", Crosshair.COLOR_NAMES[i]));
        }).dimensions(x, y, w, h).build()); y += step;
        addDrawableChild(ButtonWidget.builder(label("Outline", m().outline ? "ON" : "OFF"), b -> {
            m().outline = !m().outline; b.setMessage(label("Outline", m().outline ? "ON" : "OFF"));
        }).dimensions(x, y, w, h).build()); y += step;
        addDrawableChild(new Slide(x, y, w, h, "Size", 2, 20, m().size, v -> m().size = v)); y += step;
        addDrawableChild(new Slide(x, y, w, h, "Thickness", 1, 4, m().thick, v -> m().thick = v)); y += step;
        addDrawableChild(new Slide(x, y, w, h, "Gap", 0, 8, m().gap, v -> m().gap = v)); y += step + 6;
        addDrawableChild(ButtonWidget.builder(Fonts.t("Done"), b -> close()).dimensions(x, y, w, h).build());
    }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) { c.fill(0, 0, width, height, 0x40000000); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        super.render(c, mx, my, d);
        c.drawText(textRenderer, Fonts.t("CROSSHAIR"), 24, 14, 0xFFFFFFFF, false);
        int cx = width / 2, cy = height / 2;
        c.fill(cx - 44, cy - 44, cx + 44, cy + 44, 0x66000000);
        Crosshair.draw(c, cx, cy);
        Text t = Fonts.t("Preview");
        c.drawText(textRenderer, t, cx - textRenderer.getWidth(t) / 2, cy + 50, 0xFF8A8A8A, false);
    }

    @Override public void close() { Config.save(); client.setScreen(parent); }
}
