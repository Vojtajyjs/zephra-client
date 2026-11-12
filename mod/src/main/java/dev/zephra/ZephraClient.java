package dev.zephra;

import dev.zephra.mixin.SimpleOptionAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZephraClient implements ClientModInitializer {
    static KeyBinding menuKey, zoomKey;
    static boolean fb;
    static double savedGamma = 1.0;

    public static boolean zooming() { return Config.on("zoom") && zoomKey != null && zoomKey.isPressed(); }

    static void gamma(MinecraftClient mc, double v) {
        ((SimpleOptionAccessor) (Object) mc.options.getGamma()).zephra$setValue(v);
    }

    @Override public void onInitializeClient() {
        Config.load();
        Hud.init();
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Zephra menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "Zephra"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "Zephra"));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) mc.setScreen(new ConfigScreen());
            if (Config.on("togglesprint") && mc.player != null && mc.currentScreen == null
                && mc.options.forwardKey.isPressed() && !mc.player.isSneaking() && !mc.player.horizontalCollision) {
                mc.player.setSprinting(true);
            }
            if (Config.on("fullbright")) {
                if (!fb) { savedGamma = mc.options.getGamma().getValue(); fb = true; }
                gamma(mc, 16.0);
            } else if (fb) { gamma(mc, savedGamma); fb = false; }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> { if (fb) { gamma(mc, savedGamma); fb = false; } });
    }
}
