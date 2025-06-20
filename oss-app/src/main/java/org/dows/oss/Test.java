package org.dows.oss;

import org.dows.oss.utils.FileParseUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tangsm
 * @data 2025/6/6 星期五
 */
public class Test {
    public static void main(String[] args) throws Exception {
//        String filePath = "C:\\Users\\Administrator\\radeorg\\uim\\250604\\【前端开发工程师_上海_8-13K】康奕伟_6年.pdf";
//        String parseContent = FileParseUtil.convertToMarkdown(filePath);
//        System.out.println(parseContent);

        String fileName = "【猎聘_前端开发工程师_上海_8-13K】康奕伟_6年_BL_100000.pdf";
        String nameWithoutExtension;
        // 去除文件后缀名
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExtension = fileName.substring(0, lastDotIndex);
            Pattern pattern = Pattern.compile("BL_[A-Z0-9]{6}");
            Matcher matcher = pattern.matcher(nameWithoutExtension);
            System.out.println(matcher.find());
            if (matcher.find()) {
                System.out.println(matcher.find());
            }
        }
    }
}
