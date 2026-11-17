package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;

class BarcodeInter25Test {

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

    private static String pageContent(String code) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        BarcodeInter25 barcode = new BarcodeInter25();
        barcode.setCode(code);
        barcode.setGenerateChecksum(true);
        barcode.setChecksumText(true);
        barcode.placeBarcode(writer.getDirectContent(), null, null);
        document.close();

        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            return new String(reader.getPageContent(1), StandardCharsets.ISO_8859_1);
        }
    }

    @Test
    void checksumOfDigits() {
        // Weighted 3, 1, 3, ... from the right the digits add up to 59, so the check digit is 1.
        assertThat(BarcodeInter25.getChecksum("411200076041001")).isEqualTo('1');
    }

    @Test
    void textShowsTheCheckDigitTheBarsEncode() throws Exception {
        assertThat(pageContent("411200076041001")).contains("(4112000760410011)");
    }

    @Test
    void otherCharactersInTheCodeDoNotChangeTheCheckDigitShown() throws Exception {
        assertThat(pageContent("41-1200076041001")).contains("(41-12000760410011)");
    }
}
