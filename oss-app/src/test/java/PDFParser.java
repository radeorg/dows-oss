//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDResources;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.apache.pdfbox.text.TextPosition;
//
//import java.awt.geom.Rectangle2D;
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//public class PDFParser {
//
//
//
//    public static void main(String[] args) {
//
//
//        if (args.length != 1) {
//
//
//            System.err.println("Usage: java PDFParser <path-to-pdf-file>");
//            System.exit(1);
//        }
//
//        String pdfFilePath = args[0];
//
//        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
//
//
//            // 提取文本及其坐标
//            extractTextWithPositions(document);
//
//            // 提取图片及其坐标
//            extractImagesWithPositions(document);
//        } catch (IOException e) {
//
//
//            System.err.println("Error reading PDF file: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private static void extractTextWithPositions(PDDocument document) throws IOException {
//
//
//        PDFTextStripper stripper = new PDFTextStripper() {
//
//
//            @Override
//            protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
//
//
//                for (TextPosition text : textPositions) {
//
//
//                    System.out.println("Text: " + text.getUnicode() +
//                            ", X: " + text.getXDirAdj() +
//                            ", Y: " + text.getYDirAdj() +
//                            ", Font Size: " + text.getFontSizeInPt());
//                }
//            }
//        };
//
//        stripper.setSortByPosition(true);
//        stripper.setStartPage(0);
//        stripper.setEndPage(document.getNumberOfPages());
//        stripper.writeText(document, System.out);
//    }
//
//    private static void extractImagesWithPositions(PDDocument document) throws