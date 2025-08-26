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
    //private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}");




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
            if (validator.isValid(e)) list.add(e);
        }
        return list;
    }

    /*public static void main(String[] args) {
        String dd = "男|年龄：31岁| 13886069063| 445751137@qq.com9年工作经验|求职意向：前端开发工程师|期望薪资：12-20K|期望城市：上海";
        List<String> email = getEmail(dd);
        System.out.println( email);
    }*/
}