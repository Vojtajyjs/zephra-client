package dev.zephra.mixin;

import dev.zephra.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs once per frame so the zoom can ease smoothly. */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void zephra$frame(CallbackInfo ci) { Zoom.frame(MinecraftClient.getInstance()); }
}
