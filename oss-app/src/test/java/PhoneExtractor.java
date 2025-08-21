import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

public class PhoneExtractor {
    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    public static List<String> getPhones(String text, String defaultRegion) {
        List<String> list = new ArrayList<>();
        for (PhoneNumberMatch m : PHONE_UTIL.findNumbers(text, defaultRegion)) {
            list.add(PHONE_UTIL.format(m.number(), PhoneNumberUtil.PhoneNumberFormat.E164));
        }
        return list;
    }

    public static void main(String[] args) {
        String txt = "张三 138-1234-5678 李四 +86 159 8888 8888";
        System.out.println(getPhones(txt, "CN"));
        // 输出: [+8613812345678, +8615988888888]
    }
}