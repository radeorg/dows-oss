import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileExtractor {
    public static void main(String[] args) throws Exception {
        // 处理包含特殊字符的路径
        String filePath = "E:\\logs\\【产品经理_上海_13-26K】王磊磊_11年.pdf";

        PDDocument doc = Loader.loadPDF(new File(filePath));

        //

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);      // ← 关键：按视觉顺序重排
        stripper.setShouldSeparateByBeads(true);
        stripper.setAddMoreFormatting(true);   // 段落换行更友好
        String txt = stripper.getText(doc);
        //String txt1 = new PDFLayoutTextStripper().getText(doc);
        doc.close();



       // System.out.println("========="+txt1);


// 再跑正则去掉水印
//        txt = txt.replaceAll("(?i)[0-9a-f]{30,}|(?i)[a-z]{2,3}_\\s*[a-z]{3,4}\\s*j", " ");
//        System.out.println("@@@@@@@@@@@@@@@@@@@@"+txt);


        List<String> cn = ExtractUtil.getPhones(txt, "CN");
        List<String> email = ExtractUtil.getEmail(txt);

        for (String s : email) {
            System.out.println("=========="+ email);
        }
        for (String s : cn) {
            System.out.println("======================="+s);
        }


    }


    public static String extractTextFromPDF(File file) throws TikaException, IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            Tika tika = new Tika();
            // 可以设置较大的最大内容长度
            tika.setMaxStringLength(1024 * 1024); // 1MB

            String text = tika.parseToString(inputStream);

            System.out.println("手机：" + ExtractUtil.getPhones(text, "CN"));
//            System.out.println("邮箱：" + EmailExtractor.getEmails(text));
            System.out.println(text);
            return text;
        }
    }
}