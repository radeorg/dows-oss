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
            "(?<!\\d)(?:\\+86\\s?|0086\\s?)?1[3-9]\\d(?:[ -]?\\d{4}){2}(?!\\d)"
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
        String text = "用户提供的号码列表：+8613211111111、0086 13211111111、132 1111 1111、132-1111-1111、TEL:13812345678、联系电话 13987654321、13233968042";
        System.out.println(CommonUtil.extractPattern(text, PatternConstant.PHONE_PATTERN));
        System.out.println(CommonUtil.extractPatterns("18916137726@163.com", EMAIL_REGEXES));
        System.out.println(CommonUtil.extractPatterns("user+tag@example.com", EMAIL_REGEXES));
        System.out.println(CommonUtil.extractPatterns("john.doe@sub.domain.co.uk", EMAIL_REGEXES));
        System.out.println(CommonUtil.extractPatterns(" | 18111111111@qq.com", EMAIL_REGEXES));
    }
}
