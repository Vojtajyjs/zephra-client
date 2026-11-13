package dev.zephra.mixin;

import dev.zephra.TitleArt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the Minecraft title logo with Zephra. Optional: skipped silently if the target changes. */
@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {
    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void zephra$a(DrawContext c, int w, float alpha, CallbackInfo ci) { TitleArt.logo(c, w, alpha, 30); ci.cancel(); }

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void zephra$b(DrawContext c, int w, float alpha, int y, CallbackInfo ci) { TitleArt.logo(c, w, alpha, y); ci.cancel(); }
}
