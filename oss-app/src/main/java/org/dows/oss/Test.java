package org.dows.oss;

import cn.hutool.core.io.FileUtil;
import org.dows.oss.utils.FileParseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/**
 * @author tangsm
 * @data 2025/6/6 星期五
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String filePath = "C:\\Users\\Administrator\\radeorg\\uim\\250604\\【前端开发工程师_上海_8-13K】康奕伟_6年.pdf";
//        String parseContent = FileParseUtil.convertToMarkdown(filePath);
//        System.out.println(parseContent);


        try (InputStream is1 = new FileInputStream(filePath)) {
            String md5 = FileParseUtil.calculateMD5(is1);
            System.out.println(md5);

            File dest = new File("C:\\Users\\Administrator\\radeorg\\uim\\250604\\" + md5 + ".pdf");
            FileUtil.writeFromStream(is1, dest);
        }
    }
}
