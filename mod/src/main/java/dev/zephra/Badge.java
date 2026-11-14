package dev.zephra;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Zephra logo glyph placed in front of a player's name. */
public class Badge {
    static final Identifier FONT = Identifier.of("zephra", "badge");
    static final String GLYPH = "\uE000";

    public static Text prefix(Text name) {
        if (name.getString().startsWith(GLYPH)) return name;
        return Text.empty().append(Text.literal(GLYPH).setStyle(Style.EMPTY.withFont(FONT))).append(Text.literal(" ")).append(name);
    }
}
