package dev.zephra.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void zephra$title(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(cir.getReturnValue().replace("Minecraft*", "Zephra").replace("Minecraft", "Zephra"));
    }
}
