package org.dows.oss;

import com.qcloud.cos.utils.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;
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


        // 方案1：重新打开流（推荐）
        try (InputStream isForMd5 = new FileInputStream(filePath)){

            // 使用字节数组缓存（适合小文件）
            byte[] fileBytes = IOUtils.toByteArray(isForMd5); // 先完整读取流
            String md5 = DigestUtils.md5Hex(fileBytes); // 计算MD5

            String path = "C:\\Users\\Administrator\\radeorg\\uim\\250604\\" + md5 + ".pdf";
            File dest = new File(path);

            // 写入文件
            FileParseUtil.writeByteArrayToFile(dest, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
