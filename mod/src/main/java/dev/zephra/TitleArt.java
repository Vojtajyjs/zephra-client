package dev.zephra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class TitleArt {
    static final Identifier LOGO = Identifier.of("zephra", "textures/gui/logo.png");
    static final Identifier BG = Identifier.of("zephra", "textures/gui/bg.png");

    static void tex(DrawContext c, Identifier id, int x, int y, int w, int h, int tw, int th) {
        c.drawTexture(RenderLayer::getGuiTextured, id, x, y, 0f, 0f, w, h, tw, th, tw, th);
    }

    public static void background(DrawContext c) {
        var w = MinecraftClient.getInstance().getWindow();
        tex(c, BG, 0, 0, w.getScaledWidth(), w.getScaledHeight(), 1280, 720);
    }

    public static void logo(DrawContext c, int screenWidth, float alpha, int y) {
        tex(c, LOGO, (screenWidth - 240) / 2, y, 240, 58, 1000, 240);
    }
}
