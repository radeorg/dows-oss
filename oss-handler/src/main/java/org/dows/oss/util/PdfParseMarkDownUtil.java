package org.dows.oss.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class PdfParseMarkDownUtil {
    // 中英日多语言页眉页脚识别正则
    private static final Map<Locale, List<Pattern>> LOCALE_PATTERNS = Map.of(
            Locale.CHINA, Arrays.asList(
                    Pattern.compile("(?m)^(页码|第\\s*\\d+\\s*页|页眉).*$"),
                    Pattern.compile("(?m).*(机密|保密|公司文件|草稿|样本|COPY|水印|WATERMARK|CONFIDENTIAL|\\d{4}).*$"),
                    Pattern.compile("(?m).*(7a3fc722425671571n1_3tm0ElVVwPI6E9URfmSXOWNOAKiLmPRXYEPSxUdrM).*$")
            ),
            Locale.US, Arrays.asList(
                    Pattern.compile("(?m)^(Page \\d+|Footer).*$"),
                    Pattern.compile("(?m).*(CONFIDENTIAL|Copyright|Draft|Sample|Watermark|Copy|Internal Use).*$")
            ),
            Locale.JAPAN, List.of(
                    Pattern.compile("(?m)^(ページ\\s*\\d+|ヘッダー).*$")
            )
    );
    // 水印
    private static final Pattern WATERMARK_PATTERN = Pattern.compile(
            "(7a3fc722[\\w\\d]+|试用版|样本|SAMPLE|WPS|Evaluation Copy)"
    );

    /**
     * 解析文档并转为markdown格式
     */
    public static String convertToMarkdown(String pdfPath) throws IOException {
        return parseToMarkdown(extractCleanText(pdfPath));
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

    private static String extractCleanText(String pdfPath) throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, java.util.List<TextPosition> textPositions) throws IOException {
                    if (!isWatermarkText(text)) {
                        super.writeString(text, textPositions);
                    }
                }
            };
            String rawText = stripper.getText(document);
            Locale docLocale = detectLocale(rawText);
            return cleanWatermarkText(rawText, docLocale);
        }
    }

    private static boolean isWatermarkText(String text) {
        return text.trim().isEmpty() || WATERMARK_PATTERN.matcher(text).matches();
    }

    /**
     *语言检测逻辑
     */
    private static Locale detectLocale(String text) throws IOException {
        if (text.matches(".*[\\u4e00-\\u9fa5].*")) {
            return Locale.CHINA;
        } else if (text.contains("ページ") ||
                text.contains("です") ||
                text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF].*")) {
            return Locale.JAPAN;
        }
        return Locale.US;
    }

    private static String cleanWatermarkText(String text, Locale docLocale) {
        // 先处理语言特定的页眉页脚
        List<Pattern> patterns = LOCALE_PATTERNS.getOrDefault(
                docLocale, LOCALE_PATTERNS.get(Locale.US)
        );

        String result = text;
        for (Pattern p : patterns) {
            result = p.matcher(result).replaceAll("");
        }

        // 再处理通用水印
        String cleaned = WATERMARK_PATTERN.matcher(result).replaceAll("");
        cleaned = cleaned.replaceAll("(?m)^\\s*[\\p{Punct}\\dA-Za-z]{1,3}\\s*$", "");

        // 最后清理空行
        return cleaned.replaceAll("(?m)(^\\s*$[\n\r]+){2,}", "\n");
    }

    public static void main(String[] args) throws IOException {
        System.out.println(convertToMarkdown("C:\\Users\\Administrator\\Pictures\\Saved Pictures\\东软-前端0506-上海太保-杜运涛.pdf"));
    }
}
