package ink.ziip.hammer.hammercore.feature.nexo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;

/** Restyles Innova's reserved characters without reparsing legacy colors or changing text. */
final class NexoShiftText {
    private static final Key SHIFT = Key.key("nexo:shift");
    private static final Key MANA = Key.key("nexo:default");
    private static final TextReplacementConfig FONTS = TextReplacementConfig.builder()
            .match("[\uEB20-\uEB33]+|[\uEB66-\uEB68]+")
            .replacement((match, text) -> text.font(match.group().charAt(0) <= '\uEB33' ? SHIFT : MANA))
            .build();

    private NexoShiftText() {}

    static Component style(Component original) {
        if (original == null) return null;
        Component result = original.replaceText(FONTS);
        return result.equals(original) ? original : result;
    }

    static boolean isTagDisplayMenu(Component title) {
        if (title == null) return false;
        // DeluxeTags' title bitmap and the DeluxeMenus profile containing the current tag.
        for (Component part : title.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (part instanceof TextComponent text
                    && (text.content().indexOf('\uEC0B') >= 0 || text.content().contains("个人信息"))) {
                return true;
            }
        }
        return false;
    }
}
