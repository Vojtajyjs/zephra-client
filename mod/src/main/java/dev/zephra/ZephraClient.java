package dev.zephra;

import dev.zephra.mixin.SimpleOptionAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ZephraClient implements ClientModInitializer {
    static KeyBinding menuKey, zoomKey;
    static boolean fb, sprintForced;
    static double savedGamma = 1.0;

    public static boolean zooming() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.on("zoom") || zoomKey == null || mc.player == null || mc.currentScreen != null) return false;
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(zoomKey).getCode());
    }

    /** Sets an option's value directly, bypassing the slider limits. */
    static void raw(SimpleOption<?> opt, Object v) { ((SimpleOptionAccessor) (Object) opt).zephra$setValue(v); }

    static boolean physicalSprint(MinecraftClient mc) {
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(mc.options.sprintKey).getCode());
    }

    static void restore(MinecraftClient mc) {
        if (fb) { raw(mc.options.getGamma(), savedGamma); fb = false; }
        Zoom.restore(mc);
    }

    @Override public void onInitializeClient() {
        Config.load();
        Hud.init();
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Zephra menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "Zephra"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "Zephra"));

        // Toggle sprint: hold the vanilla sprint key for you, so hunger, blindness etc. behave exactly like vanilla.
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (mc.player == null) return;
            boolean want = Config.on("togglesprint") && mc.currentScreen == null && mc.options.forwardKey.isPressed();
            if (want) { mc.options.sprintKey.setPressed(true); sprintForced = true; }
            else if (sprintForced) { mc.options.sprintKey.setPressed(physicalSprint(mc)); sprintForced = false; }
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) mc.setScreen(new ConfigScreen(null));
            if (Config.on("fullbright")) {
                if (!fb) { savedGamma = mc.options.getGamma().getValue(); fb = true; }
                raw(mc.options.getGamma(), 16.0);
            } else if (fb) { raw(mc.options.getGamma(), savedGamma); fb = false; }
            ZephraNet.tick(mc);
        });

        ScreenEvents.AFTER_INIT.register((client, screen, sw, sh) -> {
            if (screen instanceof GameMenuScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Zephra Settings"), b -> client.setScreen(new ConfigScreen(screen)))
                    .dimensions(sw - 116, 6, 110, 20).build());
            } else if (screen instanceof TitleScreen) {
                int gx = sw - 28, gy = 8;
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> {
                    boolean hov = mx >= gx && mx < gx + 22 && my >= gy && my < gy + 22;
                    Ui.rr(ctx, hov ? "rr_cardhi" : "rr_card", gx, gy, 22, 22, 8);
                    Ui.img(ctx, "gear", gx + 4, gy + 4, 14, 14);
                });
                ScreenMouseEvents.allowMouseClick(screen).register((s, mx, my, btn) -> {
                    if (btn == 0 && mx >= gx && mx < gx + 22 && my >= gy && my < gy + 22) { client.setScreen(new ConfigScreen(s)); return false; }
                    return true;
                });
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> mc.options.getHideSplashTexts().setValue(true));
        ClientLifecycleEvents.CLIENT_STOPPING.register(ZephraClient::restore);
    }
}
