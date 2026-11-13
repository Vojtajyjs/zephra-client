package dev.zephra.mixin;

import dev.zephra.TitleArt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void zephra$bg(DrawContext c, int mx, int my, float d, CallbackInfo ci) { TitleArt.background(c); }
}
