package org.dows.oss.utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author tangm
 * @data 2025/6/5 星期四
 */
@Slf4j
public class FileParseUtil {

    // 中英日多语言页眉页脚识别正则
    private static final Map<Locale, List<Pattern>> LOCALE_PATTERNS = Map.of(
            Locale.CHINA, Arrays.asList(
                    Pattern.compile("(?m)^(页码|第\\s*\\d+\\s*页|页眉).*$"),
                    Pattern.compile("(?m).*(机密|保密|公司文件).*$")
            ),
            Locale.US, Arrays.asList(
                    Pattern.compile("(?m)^(Page \\d+|Footer).*$"),
                    Pattern.compile("(?m).*(CONFIDENTIAL|Copyright).*$")
            ),
            Locale.JAPAN, List.of(
                    Pattern.compile("(?m)^(ページ\\s*\\d+|ヘッダー).*$")
            )
    );

    /**
     * 解析文档并转为markdown格式
     */
    public static String convertToMarkdown(String pdfPath) throws IOException {
        StringBuilder content = new StringBuilder();
        try {
            PdfReader reader = new PdfReader(pdfPath);
            Locale docLocale = detectLocale(reader);
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                String text = PdfTextExtractor.getTextFromPage(reader, i);
                content.append(removeHeaderFooter(text, docLocale));
            }
            return parseToMarkdown(content.toString());
        } catch (Exception e) {
            log.error("PDF解析失败", e);
            throw e;
        }
    }

    /**
     *语言检测逻辑
     */
    private static Locale detectLocale(PdfReader reader) throws IOException {
        String text = PdfTextExtractor.getTextFromPage(reader, 1);
        if (text.matches(".*[\\u4e00-\\u9fa5].*")) {
            return Locale.CHINA;
        } else if (text.contains("ページ")) {
            return Locale.JAPAN;
        }
        return Locale.US;
    }

    /**
     * 带语言检测的页眉页脚清除
     */
    private static String removeHeaderFooter(String text, Locale locale) {
        List<Pattern> patterns = LOCALE_PATTERNS.getOrDefault(
                locale, LOCALE_PATTERNS.get(Locale.US)
        );

        String result = text;
        for (Pattern p : patterns) {
            result = p.matcher(result).replaceAll("");
        }
        return result.replaceAll("(?m)^\\s*$[\n\r]{1,}", "\n\n");
    }

    /**
     * 转为Markdown格式
     */
    private static String parseToMarkdown(String text) {
        Parser parser = Parser.builder()
                .extensions(List.of(TablesExtension.create()))
                .build();

        Node document = parser.parse(text);
        return HtmlRenderer.builder()
                .extensions(List.of(TablesExtension.create()))
                .build()
                .render(document);
    }
}
