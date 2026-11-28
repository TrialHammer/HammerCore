package ink.ziip.hammer.hammercore.feature.nexo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NexoShiftTextTest {
    record Part(String text, Key font) {}

    @Test
    void menuMovesOnlyThePrefixAndKeepsTheBitmapAndStyle() {
        Component title = Component.text("\uEB25\uEB29\uEC07", NamedTextColor.WHITE)
                .clickEvent(ClickEvent.suggestCommand("/kit"));
        Component styled = NexoShiftText.style(title);
        assertEquals(List.of(part("\uEB25\uEB29", "nexo:shift"), new Part("\uEC07", null)), parts(styled));
        assertEquals(title.color(), styled.color());
        assertEquals(title.clickEvent(), styled.clickEvent());
        assertSame(styled, NexoShiftText.style(styled));
    }

    @Test
    void tagsKeepTheirBitmapAndManaKeepsSeparateNamedFonts() {
        Component line = Component.text("\uEB28\uEB29\uED75\uEB28\uEB29 ")
                .append(Component.text("\uEB30\uEB2F\uEB2C\uEB66\uEB67\uEB68"));
        assertEquals(List.of(part("\uEB28\uEB29", "nexo:shift"), new Part("\uED75", null),
                part("\uEB28\uEB29", "nexo:shift"), new Part(" ", null),
                part("\uEB30\uEB2F\uEB2C", "nexo:shift"), part("\uEB66\uEB67\uEB68", "nexo:default")),
                parts(NexoShiftText.style(line)));
    }

    @Test
    void onlyRecognizesTargetMenuTitlesAndDoesNotTouchOtherText() {
        assertTrue(NexoShiftText.isTagDisplayMenu(Component.text("\uEB25\uEB29\uEC0B")));
        assertTrue(NexoShiftText.isTagDisplayMenu(Component.text("Innova | ").append(Component.text("个人信息"))));
        assertFalse(NexoShiftText.isTagDisplayMenu(Component.text("\uEB25\uEB29\uEC07")));
        var untouched = Component.text("spring \uED1D", NamedTextColor.GREEN).font(Key.key("nexo:default"));
        assertSame(untouched, NexoShiftText.style(untouched));
    }

    private static Part part(String content, String font) {
        return new Part(content, Key.key(font));
    }

    private static List<Part> parts(Component component) {
        var parts = new ArrayList<Part>();
        collect(component, null, parts);
        return parts;
    }

    private static void collect(Component component, Key parentFont, List<Part> result) {
        Key font = component.font() == null ? parentFont : component.font();
        if (component instanceof TextComponent text && !text.content().isEmpty()) {
            result.add(new Part(text.content(), font));
        }
        for (Component child : component.children()) collect(child, font, result);
    }
}
