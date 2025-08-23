package org.dows.oss.util;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExtractUtil {
    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");

    public static List<String> getPhones(String text, String defaultRegion) {
        List<String> list = new ArrayList<>();
        for (PhoneNumberMatch m : PHONE_UTIL.findNumbers(text, defaultRegion)) {
            list.add(PHONE_UTIL.format(m.number(), PhoneNumberUtil.PhoneNumberFormat.E164));
        }
        return list;
    }


    public static List<String> getEmail(String text) {
        EmailValidator validator = EmailValidator.getInstance();
        List<String> list = new ArrayList<>();
        Matcher m = EMAIL_PATTERN.matcher(text);
        while (m.find()) {
            String e = m.group();
            if (validator.isValid(e)) list.add(e);   // 格式 + 域名校验
        }
        return list;
    }

    /*public static void main(String[] args) {
        String txt = "张三 138-1234-5678 李四 +86 159 8888 8888";
        System.out.println(getPhones(txt, "CN"));
        // 输出: [+8613812345678, +8615988888888]
    }*/
}