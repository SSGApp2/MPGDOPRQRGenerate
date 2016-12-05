package net.glxn.qrgen.javase;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.glxn.qrgen.core.AbstractQRCode;
import net.glxn.qrgen.core.exception.QRGenerationException;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.core.scheme.VCard;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QRCode extends AbstractQRCode {

    public static final MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig();

    protected final String text;
    protected MatrixToImageConfig matrixToImageConfig = DEFAULT_CONFIG;

    private String fileName = "/tmp/xxx.txt";

    protected QRCode(String text) {
        this.text = text;
        qrWriter = new QRCodeWriter();
    }

    protected QRCode(String text, String fileName) {
        this.text = text;
        this.fileName = fileName;
        qrWriter = new QRCodeWriter();
    }

    /**
     * Create a QR code from the given text.    <br><br>
     * <p>
     * There is a size limitation to how much you can put into a QR code. This has been tested to work with up to a length of
     * 2950
     * characters.<br><br>
     * </p>
     * <p>
     * The QRCode will have the following defaults:     <br> {size: 100x100}<br>{imageType:PNG}  <br><br>
     * </p>
     * Both size and imageType can be overridden:   <br> Image type override is done by calling {@link
     * QRCode#to(ImageType)} e.g. QRCode.from("hello world").to(JPG) <br> Size override is done
     * by calling
     * {@link QRCode#withSize} e.g. QRCode.from("hello world").to(JPG).withSize(125, 125)  <br>
     *
     * @param text the text to encode to a new QRCode, this may fail if the text is too large. <br>
     * @return the QRCode object    <br>
     */
    public static QRCode from(String text) {
        return new QRCode(text);
    }

    public static QRCode from(String text, String fileName) {
        return new QRCode(text, fileName);
    }

    /**
     * Creates a a QR Code from the given {@link VCard}.
     * <p>
     * The QRCode will have the following defaults:     <br> {size: 100x100}<br>{imageType:PNG}  <br><br>
     * </p>
     *
     * @param vcard the vcard to encode as QRCode
     * @return the QRCode object
     */
    public static QRCode from(VCard vcard) {
        return new QRCode(vcard.toString());
    }

    /**
     * Overrides the imageType from its default {@link net.glxn.qrgen.core.image.ImageType#PNG}
     *
     * @param imageType the {@link net.glxn.qrgen.core.image.ImageType} you would like the resulting QR to be
     * @return the current QRCode object
     */
    public QRCode to(ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    /**
     * Overrides the size of the qr from its default 125x125
     *
     * @param width  the width in pixels
     * @param height the height in pixels
     * @return the current QRCode object
     */
    public QRCode withSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Overrides the default charset by supplying a {@link com.google.zxing.EncodeHintType#CHARACTER_SET} hint to {@link
     * com.google.zxing.qrcode.QRCodeWriter#encode}
     *
     * @param charset the charset as string, e.g. UTF-8
     * @return the current QRCode object
     */
    public QRCode withCharset(String charset) {
        return withHint(EncodeHintType.CHARACTER_SET, charset);
    }

    /**
     * Overrides the default error correction by supplying a {@link com.google.zxing.EncodeHintType#ERROR_CORRECTION} hint to
     * {@link com.google.zxing.qrcode.QRCodeWriter#encode}
     *
     * @param level the error correction level to use by {@link com.google.zxing.qrcode.QRCodeWriter#encode}
     * @return the current QRCode object
     */
    public QRCode withErrorCorrection(ErrorCorrectionLevel level) {
        return withHint(EncodeHintType.ERROR_CORRECTION, level);
    }

    /**
     * Sets hint to {@link com.google.zxing.qrcode.QRCodeWriter#encode}
     *
     * @param hintType the hintType to set
     * @param value    the concrete value to set
     * @return the current QRCode object
     */
    public QRCode withHint(EncodeHintType hintType, Object value) {
        hints.put(hintType, value);
        return this;
    }

    @Override
    public File file() {
        File file;
        try {
            //file = createTempFile();
            //SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");
            file = new File(this.fileName);
            System.out.println("Temp file : " + file.getAbsolutePath());
            MatrixToImageWriter.writeToPath(createMatrix(text), imageType.toString(), file.toPath(), matrixToImageConfig);
        } catch (Exception e) {
            throw new QRGenerationException("Failed to create QR image from text due to underlying exception", e);
        }

        return file;
    }

    @Override
    public File file(String name) {
        File file;
        try {
            file = createTempFile(name);
            MatrixToImageWriter.writeToPath(createMatrix(text), imageType.toString(), file.toPath(), matrixToImageConfig);
        } catch (Exception e) {
            throw new QRGenerationException("Failed to create QR image from text due to underlying exception", e);
        }

        return file;
    }

    @Override
    protected void writeToStream(OutputStream stream) throws IOException, WriterException {
        MatrixToImageWriter.writeToStream(createMatrix(text), imageType.toString(), stream, matrixToImageConfig);
    }

    private File createTempSvgFile() throws IOException {
        return createTempSvgFile("QRCode");
    }

    private File createTempSvgFile(String name) throws IOException {
        File file = File.createTempFile(name, ".svg");
        file.deleteOnExit();
        return file;
    }

    public QRCode withColor(int onColor, int offColor) {
        matrixToImageConfig = new MatrixToImageConfig(onColor, offColor);
        return this;
    }

    public static List<String> readFile(String fileName) {
        List<String> result = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                result.add(line);
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<String> readExcelFile(String fileName) {
        List<String> result = new ArrayList<>();

        try {
            FileInputStream file = new FileInputStream(new File(fileName));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                StringBuffer sb = new StringBuffer();

                if (fileName.indexOf("EQListfo") >= 0) {
                    sb.append("EQ/");
                } else if (fileName.indexOf("FLListfo") >= 0) {
                    sb.append("FL/");
                } else {
                }

                int colCheck = 1;

                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            //System.out.print(cell.getNumericCellValue());
                            sb.append("" + cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            //System.out.print(cell.getStringCellValue());
                            sb.append("" + cell.getStringCellValue());
                            break;
                    }

                    if(colCheck==4){
                        sb.append(",");
                    }else if(colCheck<=3){
                        sb.append("/");
                    }
                    colCheck++;
                }
                if (sb.toString().indexOf("Description") < 0) {
                    String ret = sb.toString().replace(".0", "");
//                    ret = ret.substring(0, ret.lastIndexOf("/"));
//                    ret = ret.substring(0, ret.lastIndexOf("/")) + "," + ret.split("/")[ret.split("/").length - 1];
                    System.out.println(ret);
                    result.add(ret);
                }

            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }


    public static void main(String args[]) {

        if (args.length < 2) {
            System.out.println("Usage : java -jar GENQR.jar QR [EQ/BEB1/1000/0/EPL-AGT-EVLIM00001,Machine 001-001] [destination]");
            System.out.println("Usage : java -jar GENQR.jar FILE [list-of-fl-eq-file] [destination]");
        } else {
            if("QR".equalsIgnoreCase(args[0])){
                String text = "";
                String file = "";
                try {

                        text = args[1];
                        file = text.split("/")[2]+"-"+text.split("/")[3]+text.split("/")[4].split(",")[0]+"-"+text.split("/")[4].split(",")[1];
                        //file = createTempFile();
                        System.out.println("file name :" + file);
                        String fileName = args[2] + file + ".png";
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
                } catch (Exception e) {
                    System.out.print("error machine :"+text);
                }

            }else if("FILE".equalsIgnoreCase(args[0])){
                List<String> FLEQlist = QRCode.readExcelFile(args[1]);//QRCode.readFile("/Users/nancom/Workspace/QRGen/javase/fl-eq-plant.txt");
                System.out.println("Size :" + FLEQlist.size());
                for (String buffer : FLEQlist) {
                    String text = "";
                    String file = "";
                    try {
                        System.out.println("code :" + buffer);

                        if (buffer.length() > 0) {
                            text = buffer;//"EQ/BEB1/1000/0/EPL-AGT-EVLIM00001,Machine 001-001";
                            file = text.split("/")[2]+"-"+text.split("/")[3]+text.split("/")[4].split(",")[0]+"-"+text.split("/")[4].split(",")[1];
                            //file = createTempFile();
                            System.out.println("file name :" + text.split("/")[4].split(",")[0]);
                            String fileName = args[2] + file + ".png";//"/Users/nancom/Temp/"+file+".png";
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
                                try {
                                    g.drawString(text.split(",")[1], 50, 385);
                                }catch(Exception e){
                                    System.out.print("no machine name :"+text);
                                }
                                g.dispose();
                                ImageIO.write(image, "png", new File(fileName));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        System.out.print("error machine :"+text);
                    }

                }
            }else{
                System.out.println("Usage : java -jar GENQR.jar QR [EQ/BEB1/1000/0/EPL-AGT-EVLIM00001,Machine 001-001] [destination]");
                System.out.println("Usage : java -jar GENQR.jar FILE [list-of-fl-eq-file] [destination]");
            }

        }

    }
}
