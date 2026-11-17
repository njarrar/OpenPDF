package org.openpdf.text.html.simpleparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Chunk;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.html.Markup;

class StyleSheetTest {

    private static StyleSheet styleSheet() {
        StyleSheet styles = new StyleSheet();
        styles.loadTagStyle("p", "color", "#000000");
        styles.loadTagStyle("p", "size", "12");
        styles.loadStyle("white-text", "color", "#ffffff");
        return styles;
    }

    private static Map<String, String> props(String... keysAndValues) {
        Map<String, String> props = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            props.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return props;
    }

    @Test
    void classStyleOverridesTagStyle() {
        Map<String, String> props = props(Markup.HTML_ATTR_CSS_CLASS, "white-text");

        styleSheet().applyStyle("p", props);

        assertThat(props)
                .containsEntry("color", "#ffffff")
                .containsEntry("size", "12")
                .doesNotContainKey(Markup.HTML_ATTR_CSS_CLASS);
    }

    @Test
    void elementPropertyOverridesClassAndTagStyle() {
        Map<String, String> props = props(Markup.HTML_ATTR_CSS_CLASS, "white-text", "color", "#ff0000");

        styleSheet().applyStyle("p", props);

        assertThat(props).containsEntry("color", "#ff0000");
    }

    @Test
    void tagStyleAppliesWithoutClass() {
        Map<String, String> props = props();

        styleSheet().applyStyle("p", props);

        assertThat(props).containsEntry("color", "#000000").containsEntry("size", "12");
    }

    @Test
    void unknownClassIsKept() {
        Map<String, String> props = props(Markup.HTML_ATTR_CSS_CLASS, "unknown");

        styleSheet().applyStyle("p", props);

        assertThat(props)
                .containsEntry("color", "#000000")
                .containsEntry(Markup.HTML_ATTR_CSS_CLASS, "unknown");
    }

    @Test
    void parsedHtmlUsesClassColor() throws Exception {
        List<Element> elements = HTMLWorker.parseToList(
                new StringReader("<p class=\"white-text\">Text</p>"), styleSheet());

        Chunk chunk = (Chunk) ((Paragraph) elements.get(0)).get(0);

        assertThat(chunk.getFont().getColor()).isEqualTo(Color.WHITE);
    }
}
