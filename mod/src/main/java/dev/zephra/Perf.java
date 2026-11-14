package dev.zephra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/** FPS profile: much faster, but still a comfortable view distance. */
public class Perf {
    public static void apply(MinecraftClient mc) {
        var o = mc.options;
        o.getViewDistance().setValue(12);
        o.getSimulationDistance().setValue(8);
        o.getEntityDistanceScaling().setValue(0.8);
        o.getBiomeBlendRadius().setValue(2);
        o.getMaxFps().setValue(260);
        o.getEnableVsync().setValue(false);
        o.getParticles().setValue(ParticlesMode.DECREASED);
        o.getGraphicsMode().setValue(GraphicsMode.FAST);
        o.write();
    }
}
