package org.dows.oss.constant;

import org.dows.oss.utils.CommonUtil;

import java.util.regex.Pattern;

/**
 * @author tangsm
 * @data 2025/6/17 星期二
 */
public class PatternConstant {
    // 支持带分隔符（-或空格）及纯数字的手机号正则
    public static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b1(?:3[0-9]|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])([-\\s]?\\d{4}){2}\\b"
    );

    // 邮箱正则（RFC 5322 标准）
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9][A-Za-z0-9._%+-]{0,63}@[A-Za-z0-9-]+\\.[A-Za-z0-9-]+(?:\\.[A-Za-z]{2,})?\\b"
    );

    public static void main(String[] args) {
        System.out.println(CommonUtil.extractPattern("192-3396-8804", PatternConstant.PHONE_PATTERN));
        System.out.println(CommonUtil.extractPattern("18916137726@163.com", EMAIL_PATTERN));
        System.out.println(CommonUtil.extractPattern("user+tag@example.com", EMAIL_PATTERN));
        System.out.println(CommonUtil.extractPattern("john.doe@sub.domain.co.uk", EMAIL_PATTERN));
    }
}
