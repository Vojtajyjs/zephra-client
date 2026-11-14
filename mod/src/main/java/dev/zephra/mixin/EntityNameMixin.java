package dev.zephra.mixin;

import dev.zephra.Badge;
import dev.zephra.ZephraNet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityNameMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
    private void zephra$badge(CallbackInfoReturnable<Text> cir) {
        if ((Object) this instanceof PlayerEntity p && ZephraNet.has(p.getUuid())) cir.setReturnValue(Badge.prefix(cir.getReturnValue()));
    }
}
