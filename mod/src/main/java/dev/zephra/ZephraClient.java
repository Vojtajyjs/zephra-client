package dev.zephra;

import dev.zephra.mixin.SimpleOptionAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZephraClient implements ClientModInitializer {
    static KeyBinding menuKey, zoomKey;
    static boolean fb, zoomOn;
    static double savedGamma = 1.0;
    static int savedFov = 70;

    static boolean zooming() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.on("zoom") || zoomKey == null || mc.player == null || mc.currentScreen != null) return false;
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(zoomKey).getCode());
    }

    /** Sets an option's value directly, bypassing the slider limits. */
    static void raw(SimpleOption<?> opt, Object v) {
        ((SimpleOptionAccessor) (Object) opt).zephra$setValue(v);
    }

    static void restore(MinecraftClient mc) {
        if (fb) { raw(mc.options.getGamma(), savedGamma); fb = false; }
        if (zoomOn) { raw(mc.options.getFov(), Integer.valueOf(savedFov)); zoomOn = false; }
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
                raw(mc.options.getGamma(), 16.0);
            } else if (fb) { raw(mc.options.getGamma(), savedGamma); fb = false; }
            if (zooming()) {
                if (!zoomOn) { savedFov = mc.options.getFov().getValue(); zoomOn = true; }
                raw(mc.options.getFov(), Integer.valueOf(Math.max(5, savedFov / 4)));
            } else if (zoomOn) { raw(mc.options.getFov(), Integer.valueOf(savedFov)); zoomOn = false; }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(ZephraClient::restore);
    }
}
