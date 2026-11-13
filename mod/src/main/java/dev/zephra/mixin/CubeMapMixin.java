package dev.zephra.mixin;

import net.minecraft.client.gui.RotatingCubeMapRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the vanilla panorama so the Zephra background shows. */
@Mixin(RotatingCubeMapRenderer.class)
public class CubeMapMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void zephra$noPanorama(CallbackInfo ci) { ci.cancel(); }
}
