package dev.zephra;

import net.minecraft.client.MinecraftClient;

/** Smooth zoom: eases the FOV every frame, scroll wheel changes the zoom level. */
public class Zoom {
    static double factor = 1, level = 4, savedSens = 0.5;
    static int savedFov = 70;
    static boolean on;
    static long last = System.nanoTime();

    public static void scroll(double v) { level = Math.max(1.5, Math.min(12, level * (v > 0 ? 1.15 : 1 / 1.15))); }

    public static void frame(MinecraftClient mc) {
        long now = System.nanoTime();
        double dt = Math.min(0.1, (now - last) / 1e9);
        last = now;
        double target = ZephraClient.zooming() ? level : 1.0;
        factor += (target - factor) * (1 - Math.exp(-dt * 14));
        if (Math.abs(target - factor) < 0.003) factor = target;
        if (factor > 1.001) {
            if (!on) { savedFov = mc.options.getFov().getValue(); savedSens = mc.options.getMouseSensitivity().getValue(); on = true; }
            ZephraClient.raw(mc.options.getFov(), Integer.valueOf((int) Math.max(5, Math.round(savedFov / factor))));
            ZephraClient.raw(mc.options.getMouseSensitivity(), Double.valueOf(Math.max(0.0, savedSens / factor)));
        } else if (on) restore(mc);
    }

    public static void restore(MinecraftClient mc) {
        if (!on) return;
        ZephraClient.raw(mc.options.getFov(), Integer.valueOf(savedFov));
        ZephraClient.raw(mc.options.getMouseSensitivity(), Double.valueOf(savedSens));
        on = false; factor = 1;
    }
}
