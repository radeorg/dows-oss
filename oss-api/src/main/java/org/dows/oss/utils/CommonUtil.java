package org.dows.oss.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
