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

    // 6种不同严格程度的邮箱正则表达式
    public static final String[] EMAIL_REGEXES = {
            // 基础格式（RFC标准）
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
            // 国际化域名支持
            "\\b[\\w.%-]+@[\\p{L}0-9.-]+\\.[\\p{L}]{2,}\\b",
            // 中国企业邮箱常见格式
            "\\b[\\w-]+(?:\\.[\\w-]+)*@(?:[\\w-]+\\.)+(com|cn|net|org|gov|edu)(\\.cn)?\\b",
            // 带子域名的复杂格式
            "\\b[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@(?:(?:[\\w-]+\\.)*[\\w-]+)\\.\\w{2,}\\b",
            // 短域名特殊处理（如user@localhost）
            "\\b[\\w.%-]+@[\\w-]{1,20}\\b",
            // 包含中文的邮箱格式
            "\\b[\\u4e00-\\u9fa5\\w]+(?:[.\\-+][\\u4e00-\\u9fa5\\w]+)*@[\\u4e00-\\u9fa5\\w]+(?:\\.[\\u4e00-\\u9fa5\\w]+)+\\b"
    };

    public static void main(String[] args) {
        System.out.println(CommonUtil.extractPattern("192-3396-8804", PatternConstant.PHONE_PATTERN));
        System.out.println(CommonUtil.extractPatterns("18916137726@163.com", EMAIL_REGEXES));
        System.out.println(CommonUtil.extractPatterns("user+tag@example.com", EMAIL_REGEXES));
        System.out.println(CommonUtil.extractPatterns("john.doe@sub.domain.co.uk", EMAIL_REGEXES));
        System.out.println(CommonUtil.extractPatterns(" | 18111111111@qq.com", EMAIL_REGEXES));
    }
}
