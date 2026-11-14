package dev.zephra;

import net.minecraft.util.Identifier;

public class Capes {
    public static Identifier id(String name) {
        if (name == null) return null;
        return switch (name) {
            case "black", "white", "grad" -> Identifier.of("zephra", "textures/capes/" + name + ".png");
            default -> null;
        };
    }
}
