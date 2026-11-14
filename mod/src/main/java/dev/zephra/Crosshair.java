package dev.zephra;

import net.minecraft.client.gui.DrawContext;

public class Crosshair {
    public static final int[] COLORS = {0xFFFFFFFF, 0xFFFF4D4D, 0xFF4DFF88, 0xFF4DD2FF, 0xFFFFE14D, 0xFFFF7AD9, 0xFF000000};
    public static final String[] COLOR_NAMES = {"White", "Red", "Green", "Cyan", "Yellow", "Pink", "Black"};
    public static final String[] STYLES = {"Cross", "Dot", "Ring"};

    static void ring(DrawContext c, int cx, int cy, int ro, int ri, int col) {
        for (int dy = -ro; dy <= ro; dy++) {
            int ox = (int) Math.round(Math.sqrt(Math.max(0, ro * ro - dy * dy)));
            if (Math.abs(dy) < ri) {
                int ix = (int) Math.round(Math.sqrt(ri * ri - dy * dy));
                c.fill(cx - ox, cy + dy, cx - ix, cy + dy + 1, col);
                c.fill(cx + ix, cy + dy, cx + ox, cy + dy + 1, col);
            } else c.fill(cx - ox, cy + dy, cx + ox, cy + dy + 1, col);
        }
    }

    public static void draw(DrawContext c, int cx, int cy) {
        Config.Mod m = Config.mods.get("crosshair");
        int s = m.size, t = m.thick, g = m.gap, col = m.color, half = t / 2;
        if (m.style == 2) {
            int ri = Math.max(0, s - t);
            if (m.outline) ring(c, cx, cy, s + 1, Math.max(0, ri - 1), 0xFF000000);
            ring(c, cx, cy, s, ri, col);
            return;
        }
        int[][] r;
        if (m.style == 1) r = new int[][]{{cx - t, cy - t, cx + t, cy + t}};
        else r = new int[][]{
            {cx - g - s, cy - half, cx - g, cy - half + t}, {cx + g, cy - half, cx + g + s, cy - half + t},
            {cx - half, cy - g - s, cx - half + t, cy - g}, {cx - half, cy + g, cx - half + t, cy + g + s}};
        if (m.outline) for (int[] a : r) c.fill(a[0] - 1, a[1] - 1, a[2] + 1, a[3] + 1, 0xFF000000);
        for (int[] a : r) c.fill(a[0], a[1], a[2], a[3], col);
    }
}
