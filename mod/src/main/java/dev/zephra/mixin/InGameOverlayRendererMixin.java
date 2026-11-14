package dev.zephra.mixin;

import dev.zephra.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Low fire: moves the fire overlay down. Optional, skipped silently if the method differs. */
@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), require = 0)
    private static void zephra$lowFireA(MatrixStack matrices, VertexConsumerProvider vc, CallbackInfo ci) {
        if (Config.on("lowfire")) matrices.translate(0.0, -0.3, 0.0);
    }

    @Inject(method = "renderFireOverlay(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"), require = 0)
    private static void zephra$lowFireB(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (Config.on("lowfire")) matrices.translate(0.0, -0.3, 0.0);
    }
}
