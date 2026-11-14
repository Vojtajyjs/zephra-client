package dev.zephra.mixin;

import dev.zephra.Hud;
import dev.zephra.Zoom;
import dev.zephra.ZephraClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void zephra$click(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1) Hud.click(button);
    }

    /** While zooming, the scroll wheel changes the zoom level instead of the hotbar. */
    @Inject(method = "onMouseScroll(JDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void zephra$scroll(long window, double h, double v, CallbackInfo ci) {
        if (ZephraClient.zooming()) { Zoom.scroll(v); ci.cancel(); }
    }
}
