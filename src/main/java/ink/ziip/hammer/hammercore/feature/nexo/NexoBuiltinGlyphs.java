package ink.ziip.hammer.hammercore.feature.nexo;

import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.api.events.resourcepack.NexoPostPackGenerateEvent;
import com.nexomc.nexo.glyphs.Glyph;
import com.nexomc.nexo.glyphs.RequiredGlyph;
import com.nexomc.nexo.glyphs.Shift;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.creative.font.Font;
import team.unnamed.creative.font.ReferenceFontProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Bridges only Nexo's hard-coded glyph and shift provider to Innova's resource-pack layout. */
final class NexoBuiltinGlyphs implements Listener {
    private static final Key NEXO_DEFAULT = Key.key("nexo:default");
    private static final Key NEXO_SHIFT = Key.key("nexo:shift");
    private static final char INNOVA_MENU = '\uEB00';
    private static final char NEXO_REQUIRED_ORIGINAL = '\uA40F';
    private static final char INNOVA_REQUIRED = '\uEB04';
    private static final char FIRST_SHIFT = '\uEB20';

    static boolean isInnovaLayout() {
        return "menu_items".equals(NexoPlugin.instance().fontManager().getUnicodeGlyphMap().get(INNOVA_MENU));
    }

    static void install(JavaPlugin core) throws ReflectiveOperationException {
        var manager = NexoPlugin.instance().fontManager();
        var registry = manager.getUnicodeGlyphMap();
        // Do not reserve Innova code points on servers using an unmodified Nexo layout.
        if (!isInnovaLayout()) return;

        var required = RequiredGlyph.INSTANCE;
        String target = Character.toString(INNOVA_REQUIRED);
        String factory = Character.toString(NEXO_REQUIRED_ORIGINAL);
        var current = required.getUnicodes();
        if (!current.equals(List.of(factory)) && !current.equals(List.of(target))) {
            throw new IllegalStateException("Unknown Nexo built-in required glyph layout");
        }
        String targetOwner = registry.get(INNOVA_REQUIRED);
        if (targetOwner != null && !targetOwner.equals(required.getId())) {
            throw new IllegalStateException("Nexo required glyph collides at U+EB04 with " + targetOwner);
        }

        Shift shift = Shift.INSTANCE;
        var defaultProvider = shift.getDefaultShiftProvider();
        Map<String, Integer> widths = new LinkedHashMap<>();
        if (defaultProvider.advances().size() != 20) {
            throw new IllegalStateException("Unexpected Nexo shift table size");
        }
        var expected = new TreeSet<Integer>();
        for (int i = 1; i <= 512; i *= 2) {
            expected.add(i);
            expected.add(-i);
        }
        if (!expected.equals(new TreeSet<>(defaultProvider.advances().values()))) {
            throw new IllegalStateException("Unexpected Nexo shift widths");
        }
        for (int width : expected) {
            char code = (char) (FIRST_SHIFT + widths.size());
            if (registry.containsKey(code)) {
                throw new IllegalStateException("Nexo shift glyph collides at U+" + Integer.toHexString(code));
            }
            widths.put(Character.toString(code), width);
        }

        var unicodesField = Glyph.class.getDeclaredField("unicodes");
        var fontField = Glyph.class.getDeclaredField("font");
        unicodesField.setAccessible(true);
        fontField.setAccessible(true);
        if (current.equals(List.of(factory))) {
            unicodesField.set(required, List.of(target));
            fontField.set(required, NEXO_DEFAULT);
        }
        if (!target.equals(required.getFormattedUnicodes())
                || required.getChars().length != 1 || required.getChars()[0] != INNOVA_REQUIRED
                || !NEXO_DEFAULT.equals(required.getFont())) {
            throw new IllegalStateException("Nexo built-in glyph was already cached with a different layout");
        }
        registry.remove(NEXO_REQUIRED_ORIGINAL, required.getId());
        if (!required.getId().equals(registry.get(INNOVA_REQUIRED))) manager.registerGlyph(required);

        shift.setFontProvider(defaultProvider.advances(widths));
        for (int pixels = -1023; pixels <= 1023; pixels++) {
            int actual = 0;
            for (char code : shift.of(pixels).toCharArray()) {
                Integer width = widths.get(Character.toString(code));
                if (width == null) throw new IllegalStateException("Unexpected Nexo shift glyph");
                actual += width;
            }
            if (actual != pixels) throw new IllegalStateException("Nexo shift width changed: " + pixels);
        }
        core.getServer().getPluginManager().registerEvents(new NexoBuiltinGlyphs(), core);
        core.getLogger().info("Innova Nexo built-in glyph and 20 exact shift widths installed.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPack(NexoPostPackGenerateEvent event) {
        var pack = event.getResourcePack();
        var inherited = pack.font(NEXO_DEFAULT);
        if (inherited == null || inherited.providers().stream()
                .filter(p -> p instanceof ReferenceFontProvider ref && ref.id().equals(NEXO_SHIFT)).count() != 1) {
            throw new IllegalStateException("Nexo default font must inherit exactly one Nexo shift font");
        }
        for (Key key : List.of(Key.key("minecraft:default"), Key.key("minecraft:uniform"))) {
            var font = pack.font(key);
            if (font == null || font.providers().stream()
                    .noneMatch(p -> p instanceof ReferenceFontProvider ref && ref.id().equals(NEXO_DEFAULT))) continue;
            var providers = font.providers().stream()
                    .filter(p -> !(p instanceof ReferenceFontProvider ref && ref.id().equals(NEXO_SHIFT))).toList();
            if (providers.size() == font.providers().size()) continue;
            pack.removeFont(key);
            pack.font(Font.font(key, providers));
        }
    }
}
