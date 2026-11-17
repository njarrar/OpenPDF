package org.openpdf.text.pdf.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openpdf.text.Document;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

/**
 * "World" is placed right where "Hello" ends and "Again" some way further on, so the extracted text has one space,
 * before "Again". That only comes out right if the width of the text is scaled as the Tz operator says.
 */
class PdfTextExtractorHorizontalScalingTest {

    private static final float SIZE = 12;

    private static byte[] pdf(Float scaling) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        float factor = scaling == null ? 1 : scaling / 100;
        float x = 50;
        PdfContentByte cb = writer.getDirectContent();
        cb.beginText();
        cb.setFontAndSize(font, SIZE);
        if (scaling != null) {
            cb.setHorizontalScaling(scaling);
        }
        cb.setTextMatrix(x, 700);
        cb.showText("Hello");
        x += font.getWidthPoint("Hello", SIZE) * factor;
        cb.setTextMatrix(x, 700);
        cb.showText("World");
        x += font.getWidthPoint("World", SIZE) * factor + 20;
        cb.setTextMatrix(x, 700);
        cb.showText("Again");
        cb.endText();
        document.close();
        return out.toByteArray();
    }

    private static String text(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        }
    }

    @Test
    void withoutScaling() throws Exception {
        assertThat(text(pdf(null))).isEqualTo("HelloWorld Again");
    }

    @ParameterizedTest(name = "{0} Tz")
    @ValueSource(floats = {100, 50, 150})
    void withScaling(float scaling) throws Exception {
        assertThat(text(pdf(scaling))).isEqualTo("HelloWorld Again");
    }
}
