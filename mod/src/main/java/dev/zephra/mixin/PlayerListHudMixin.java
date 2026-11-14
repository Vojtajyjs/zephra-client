package dev.zephra.mixin;

import dev.zephra.Badge;
import dev.zephra.ZephraNet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.UUID;

/** Badge in the tab list. */
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Inject(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true, require = 0)
    private void zephra$tab(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        var nh = MinecraftClient.getInstance().getNetworkHandler();
        if (nh == null || ZephraNet.users.isEmpty()) return;
        for (UUID id : ZephraNet.users.keySet())
            if (nh.getPlayerListEntry(id) == entry) { cir.setReturnValue(Badge.prefix(cir.getReturnValue())); return; }
    }
}
