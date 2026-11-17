package org.openpdf.text.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class XMLUtilTest {

    // U+1F600, a character outside the Basic Multilingual Plane
    private static final String GRINNING_FACE = "😀";

    @Test
    void escapesMarkupCharacters() {
        assertThat(XMLUtil.escapeXML("<a href=\"x\">Tom & 'Jerry'</a>", false))
                .isEqualTo("&lt;a href=&quot;x&quot;&gt;Tom &amp; &apos;Jerry&apos;&lt;/a&gt;");
    }

    @Test
    void keepsSupplementaryCharacters() {
        assertThat(XMLUtil.escapeXML("Smile " + GRINNING_FACE + " end", false))
                .isEqualTo("Smile " + GRINNING_FACE + " end");
    }

    @Test
    void escapesSupplementaryCharactersAsOneReference() {
        assertThat(XMLUtil.escapeXML("Smile " + GRINNING_FACE + " end", true))
                .isEqualTo("Smile &#128512; end");
    }

    @Test
    void escapesNonAsciiOnlyWhenAsked() {
        assertThat(XMLUtil.escapeXML("café", false)).isEqualTo("café");
        assertThat(XMLUtil.escapeXML("café", true)).isEqualTo("caf&#233;");
    }

    @Test
    void dropsCharactersXmlDoesNotAllow() {
        assertThat(XMLUtil.escapeXML("a\u0000b\uD83Dc￿d", false)).isEqualTo("abcd");
    }
}
