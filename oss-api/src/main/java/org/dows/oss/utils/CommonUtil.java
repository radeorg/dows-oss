package org.dows.oss.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CommonUtil {

    /**
     * 日期转字符串
     * @param date 日期
     * @param format 转换格式
     * @return 字符串
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 字符转日期
     */
    public static Date parseDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            log.error("日期转换有误" + e.getMessage(), e);
        }

        return null;
    }

    /**
     * 使用正则表达式提取匹配项
     */
    public static String extractPattern(String content, Pattern pattern) {
        if (StringUtils.isNotEmpty(content)) {
            List<String> results = new ArrayList<>();
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                results.add(matcher.group());
            }
            if(!CollectionUtils.isEmpty(results)){
                return results.get(0);
            }
        }
        return "";
    }

    /**
     * 使用正则表达式提取匹配项
     */
    public static String extractPatterns(String content, String[] patterns) {
        if (StringUtils.isNotEmpty(content) && patterns != null) {
            List<String> results = new ArrayList<>();
            for (String regex : patterns) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    results.add(matcher.group());
                }
                if(!CollectionUtils.isEmpty(results)){
                    return results.get(0);
                }
            }
            return null;
        }
        return "";
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExt(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
