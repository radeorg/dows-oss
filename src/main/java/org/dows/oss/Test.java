package org.dows.oss;

import org.dows.oss.utils.FileParseUtil;

/**
 * @author tangsm
 * @data 2025/6/6 星期五
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String filePath = "C:\\Users\\Administrator\\radeorg\\uim\\250604\\【前端开发工程师_上海_8-13K】康奕伟_6年.pdf";
        String parseContent = FileParseUtil.convertToMarkdown(filePath);
        System.out.println(parseContent);
    }
}
