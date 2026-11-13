package dev.zephra;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Text in the Zephra font (assets/zephra/font). */
public class Fonts {
    public static final Identifier ID = Identifier.of("zephra", "main");
    public static Text t(String s) { return Text.literal(s).setStyle(Style.EMPTY.withFont(ID)); }
}
