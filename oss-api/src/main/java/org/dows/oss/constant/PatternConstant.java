package org.dows.oss.constant;

import java.util.regex.Pattern;

/**
 * @author tangsm
 * @data 2025/6/17 星期二
 */
public class PatternConstant {
    // 中国大陆手机号正则（严格匹配）
    public static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b1(?:3[0-9]|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}\\b"
    );

    // 邮箱正则（RFC 5322 标准）
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );
}
