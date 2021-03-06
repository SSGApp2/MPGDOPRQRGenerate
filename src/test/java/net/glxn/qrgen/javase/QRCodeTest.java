package net.glxn.qrgen.javase;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.glxn.qrgen.core.exception.QRGenerationException;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.core.scheme.VCard;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class QRCodeTest {

    @Test
    public void createNormalQR() {
        String text = "EQ/BEB1/1000/0/EPL-AGT-EVLIM00001,ทดสอบภาษาไทย สำเนา ล้ำ เย็น";
        String file = text.split("/")[4].split(",")[0];
        //file = createTempFile();
        System.out.println("file name :"+text.split("/")[4].split(",")[0]);
        String fileName = "/Users/nancom/Temp/"+file+".png";
        QRCode.from(text.split(",")[0], fileName).withSize(400, 400).file();
        try {
            BufferedImage image = ImageIO.read(new File(fileName));
            Graphics g = image.getGraphics();
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            Font font = g.getFont();
            font.deriveFont(30f);

            g.setFont(font);
            g.drawString(text.split("/")[0], 20, 200);
            g.drawString(text.split(",")[0], 50, 365);
            g.setFont(new Font("TH SarabunPSK", Font.BOLD, 20));
            g.drawString(text.split(",")[1], 50, 385);

            g.dispose();
            ImageIO.write(image, "png", new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

        @Test
    public void readExcelFile() {
        java.util.List<String> FLEQlist = QRCode.readExcelFile("/Users/nancom/Desktop/MPG/DOPR/FLEQ/EQListforGenerateQRCodeMPKS.xlsx");
    }

    @Test
    public void shouldGetFileFromVCardWithDefaults() throws Exception {
        VCard johnDoe = new VCard("John Doe")
                .setName("John Doe")
                .setEmail("john.doe@example.org")
                .setAddress("John Doe Street 1, 5678 Berlin")
                .setTitle("Mister")
                .setCompany("John Doe Inc.")
                .setPhoneNumber("1234")
                .setWebsite("www.example.org");
        File file = QRCode.from(johnDoe).file();
        Assert.assertNotNull(file);
    }

    @Test
    public void shouldGetFileFromVCardWithExtendedChars() throws Exception {
        VCard johnDoe = new VCard("John Doe")
                .setName("Björkelundsvägen")
                .setEmail("john.doe@example.org")
                .setAddress("John Doe Street 1, 5678 Gråbo")
                .setTitle("Mister")
                .setCompany("John Doe Inc.")
                .setPhoneNumber("1234")
                .setWebsite("www.Björkelundsvägen.org");
        File file = QRCode.from(johnDoe).file();
        Assert.assertNotNull(file);
    }

    @Test
    public void shouldGetBitmapFileFromText() throws Exception {
        File file = QRCode.from("www.example.org").to(ImageType.BMP).file();
        Assert.assertNotNull(file);
    }

    @Test
    public void shouldGetFileFromTextWithDefaults() throws Exception {
        File file = QRCode.from("Hello World").file();
        Assert.assertNotNull(file);
    }

    @Test
    public void shouldGetFileWithNameFromTextWithDefaults() throws Exception {
        File file = QRCode.from("Hello World").file("Hello World");
        Assert.assertNotNull(file);
        Assert.assertTrue(file.getName().startsWith("Hello World"));
    }

    @Test
    public void shouldGetSTREAMFromTextWithDefaults() throws Exception {
        ByteArrayOutputStream stream = QRCode.from("Hello World").stream();
        Assert.assertNotNull(stream);
    }

    @Test
    public void shouldHandleLargeString() throws Exception {
        int length = 2950;
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = 'a';
        }
        String text = new String(chars);
        Assert.assertEquals(length, text.length());

        File file = QRCode.from(text).to(ImageType.PNG).file();
        Assert.assertNotNull(file);
    }

    @Test
    public void shouldGetFileFromTextWithImageTypeOverrides() throws Exception {
        File jpg = QRCode.from("Hello World").to(ImageType.JPG).file();
        Assert.assertNotNull(jpg);
        File gif = QRCode.from("Hello World").to(ImageType.GIF).file();
        Assert.assertNotNull(gif);
    }

    @Test
    public void shouldGetFileWithNameFromTextWithImageTypeOverrides() throws Exception {
        File jpg = QRCode.from("Hello World").to(ImageType.JPG).file("Hello World");
        Assert.assertNotNull(jpg);
        Assert.assertTrue(jpg.getName().startsWith("Hello World"));
        File gif = QRCode.from("Hello World").to(ImageType.GIF).file("Hello World");
        Assert.assertNotNull(gif);
        Assert.assertTrue(gif.getName().startsWith("Hello World"));
    }

    @Test
    public void shouldGetStreamFromText() throws Exception {
        ByteArrayOutputStream stream = QRCode.from("Hello World").to(ImageType.PNG).stream();
        Assert.assertNotNull(stream);
        File tempFile = File.createTempFile("test", ".tmp");
        long lengthBefore = tempFile.length();
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        stream.writeTo(fileOutputStream);
        Assert.assertTrue(lengthBefore < tempFile.length());
    }

    @Test
    public void shouldWriteToSuppliedStream() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        QRCode.from("Hello World").writeTo(stream);

        Assert.assertNotNull(stream);
        File tempFile = File.createTempFile("test", ".tmp");
        long lengthBefore = tempFile.length();
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        stream.writeTo(fileOutputStream);
        Assert.assertTrue(lengthBefore < tempFile.length());
    }

    @Test
    public void shouldBeAbleToOverrideDimensionsToFile() throws Exception {
        long defaultSize = QRCode.from("Hello World").to(ImageType.PNG).file().length();
        long defaultSize2 = QRCode.from("Hello World").to(ImageType.PNG).file().length();
        File file = QRCode.from("Hello World").to(ImageType.PNG).withSize(250, 250).file();
        Assert.assertNotNull(file);
        Assert.assertTrue(defaultSize == defaultSize2);
        Assert.assertTrue(defaultSize < file.length());
    }

    @Test
    public void shouldBeAbleToOverrideDimensionsToFileWithName() throws Exception {
        long defaultSize = QRCode.from("Hello World").to(ImageType.PNG).file("Hello World").length();
        long defaultSize2 = QRCode.from("Hello World").to(ImageType.PNG).file("Hello World").length();
        File file = QRCode.from("Hello World").to(ImageType.PNG).withSize(250, 250).file("Hello World");
        Assert.assertNotNull(file);
        Assert.assertTrue(defaultSize == defaultSize2);
        Assert.assertTrue(defaultSize < file.length());
        Assert.assertTrue(file.getName().startsWith("Hello World"));
    }

    @Test
    public void shouldBeAbleToSupplyEncodingHint() throws Exception {
        String expected = "UTF-8";
        final Object[] capture = new Object[1];
        try {
            final QRCode from = QRCode.from("Jour férié");
            from.setQrWriter(writerWithCapture(capture));
            from.to(ImageType.PNG).withCharset(expected).stream();
        } catch (QRGenerationException ignored) {
        }
        assertCapturedHint(expected, capture, EncodeHintType.CHARACTER_SET);
    }

    @Test
    public void shouldBeAbleToSupplyErrorCorrectionHint() throws Exception {
        ErrorCorrectionLevel expected = ErrorCorrectionLevel.L;
        final Object[] capture = new Object[1];
        try {
            final QRCode from = QRCode.from("Jour férié");
            from.setQrWriter(writerWithCapture(capture));
            from.to(ImageType.PNG).withErrorCorrection(ErrorCorrectionLevel.L).stream();
        } catch (QRGenerationException ignored) {
        }
        assertCapturedHint(expected, capture, EncodeHintType.ERROR_CORRECTION);
    }

    @Test
    public void shouldBeAbleToSupplyAnyHint() throws Exception {
        String expected = "a hint";
        EncodeHintType[] hintTypes = EncodeHintType.values();
        for (EncodeHintType type : hintTypes) {
            final Object[] capture = new Object[1];
            try {
                final QRCode from = QRCode.from("Jour férié");
                from.setQrWriter(writerWithCapture(capture));
                from.to(ImageType.PNG).withHint(type, expected).stream();
            } catch (QRGenerationException ignored) {
            }
            assertCapturedHint(expected, capture, type);
        }
    }

    @Test
    public void shouldColorOutput() throws IOException {
        File file = QRCode.from("Hello World").withColor(0xFFFF0000, 0xFFFFFFAA).file();
        File tempFile = File.createTempFile("qr_", ".png");
        Files.copy(file.toPath(), new FileOutputStream(tempFile));
        System.out.println(tempFile.getAbsoluteFile());
    }

    @SuppressWarnings("unchecked")
    private void assertCapturedHint(Object expected, Object[] capture, EncodeHintType type) {
        Assert.assertEquals(expected, ((Map<EncodeHintType, ?>) capture[0]).get(type));
    }

    private Writer writerWithCapture(final Object[] capture) {
        return new Writer() {
            @Override
            public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public BitMatrix encode(String c, BarcodeFormat f, int w, int h, Map<EncodeHintType, ?> hs) throws WriterException {
                capture[0] = hs;
                return new BitMatrix(0);
            }
        };
    }
}
