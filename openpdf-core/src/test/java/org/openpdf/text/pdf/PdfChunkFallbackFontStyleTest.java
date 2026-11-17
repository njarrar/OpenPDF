package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;

/**
 * Text with a character above U+00FF in a standard font is drawn with the bundled fallback font, of which only the
 * regular face exists. Bold and italic must then be simulated, as they are for any font given without such a face.
 */
class PdfChunkFallbackFontStyleTest {

    // "a" followed by Polish letters, which Helvetica's encoding does not hold.
    private static final String TEXT = "aąć";
    private static final String FILL_AND_STROKE = "2 Tr";
    private static final String SKEW = "0.21256";

    private boolean compress;

    @BeforeEach
    void writeContentUncompressed() {
        compress = Document.compress;
        Document.compress = false;
    }

    @AfterEach
    void restoreCompression() {
        Document.compress = compress;
    }

    private static String pageContent(int style) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph(TEXT, new Font(Font.HELVETICA, 20, style)));
        document.close();

        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            return new String(reader.getPageContent(1), StandardCharsets.ISO_8859_1);
        }
    }

    @Test
    void regularTextIsNeitherStrokedNorSkewed() throws Exception {
        assertThat(pageContent(Font.NORMAL)).doesNotContain(FILL_AND_STROKE).doesNotContain(SKEW);
    }

    @Test
    void boldIsSimulated() throws Exception {
        assertThat(pageContent(Font.BOLD)).contains(FILL_AND_STROKE).doesNotContain(SKEW);
    }

    @Test
    void italicIsSimulated() throws Exception {
        assertThat(pageContent(Font.ITALIC)).contains(SKEW).doesNotContain(FILL_AND_STROKE);
    }

    @Test
    void boldItalicIsSimulated() throws Exception {
        assertThat(pageContent(Font.BOLDITALIC)).contains(FILL_AND_STROKE).contains(SKEW);
    }
}
