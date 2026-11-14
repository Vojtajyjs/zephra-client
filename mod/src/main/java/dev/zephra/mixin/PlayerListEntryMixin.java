package dev.zephra.mixin;

import dev.zephra.Capes;
import dev.zephra.ZephraNet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.UUID;

/** Custom Zephra capes, shown only on clients that run Zephra. */
@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Unique private SkinTextures zephra$orig, zephra$out;
    @Unique private Identifier zephra$cape;

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void zephra$cape(CallbackInfoReturnable<SkinTextures> cir) {
        if (ZephraNet.users.isEmpty()) return;
        var nh = MinecraftClient.getInstance().getNetworkHandler();
        if (nh == null) return;
        for (Map.Entry<UUID, String> e : ZephraNet.users.entrySet()) {
            if (nh.getPlayerListEntry(e.getKey()) != (Object) this) continue;
            Identifier cape = Capes.id(e.getValue());
            if (cape == null) return;
            SkinTextures o = cir.getReturnValue();
            if (o != zephra$orig || cape != zephra$cape || zephra$out == null) {
                zephra$orig = o; zephra$cape = cape;
                zephra$out = new SkinTextures(o.texture(), o.textureUrl(), cape, o.elytraTexture(), o.model(), o.secure());
            }
            cir.setReturnValue(zephra$out);
            return;
        }
    }
}
