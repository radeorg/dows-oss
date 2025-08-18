package org.dows.oss.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tangsm
 * @data 2025/8/18 星期一
 */
@Slf4j
public class DocxToMdConverterUtil {
    public static String convertToMarkdown(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        if (file.length() == 0) {
            throw new IOException("File is empty: " + filePath);
        }

        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".docx")) {
            return convertDocxToMarkdown(file);
        } else if (lowerPath.endsWith(".doc")) {
            return convertDocToMarkdown(file);
        }
        throw new IllegalArgumentException("Unsupported file format. Only .docx and .doc files are supported");
    }

    /**
     * 将DOCX文件转换为Markdown格式
     */
    private static String convertDocxToMarkdown(File docxFile) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(docxFile))) {
            StringBuilder markdown = new StringBuilder();

            // 处理段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                processParagraph(paragraph, markdown);
            }

            // 处理表格
            for (XWPFTable table : document.getTables()) {
                processTable(table, markdown);
            }

            // 处理图片
            for (XWPFPictureData picture : document.getAllPictures()) {
                markdown.append("\n![image](").append(picture.getFileName()).append(")\n");
            }

            return markdown.toString();
        }
    }

    /**
     * 改进的DOC文件转换方法
     */
    private static String convertDocToMarkdown(File docFile) throws IOException {
        try (HWPFDocument document = new HWPFDocument(new FileInputStream(docFile));
             WordExtractor extractor = new WordExtractor(document)) {

            StringBuilder markdown = new StringBuilder();
            String fullText = extractor.getText();

            // 按段落分割并处理
            String[] paragraphs = fullText.split("\r\n\r\n|\n\n");
            for (String paragraph : paragraphs) {
                String trimmedPara = paragraph.trim();
                if (!trimmedPara.isEmpty()) {
                    // 判断是否为标题（基于长度和首字母大写）
                    if (trimmedPara.length() < 50 && Character.isUpperCase(trimmedPara.charAt(0))) {
                        markdown.append("## ").append(trimmedPara).append("\n\n");
                    }
                    // 判断是否为列表项
                    else if (trimmedPara.startsWith("• ") || trimmedPara.matches("^\\d+\\.\\s.*")) {
                        markdown.append("- ").append(trimmedPara.substring(2)).append("\n");
                    }
                    // 普通段落
                    else {
                        // 处理超链接
                        String processedPara = processHyperlinks(trimmedPara);
                        markdown.append(processedPara).append("\n\n");
                    }
                }
            }

            return markdown.toString();
        }
    }

    /**
     * 处理超链接
     */
    private static String processHyperlinks(String text) {
        Pattern pattern = Pattern.compile("(https?://\\S+)");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String url = matcher.group(1);
            matcher.appendReplacement(sb, "[" + url + "](" + url + ")");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 处理DOCX段落并转换为Markdown格式
     */
    private static void processParagraph(XWPFParagraph paragraph, StringBuilder markdown) {
        String text = paragraph.getText();
        if (text.isEmpty()) {
            return;
        }

        // 根据段落样式确定标题级别
        int headingLevel = getHeadingLevel(paragraph);
        if (headingLevel > 0) {
            markdown.append("#".repeat(headingLevel)).append(" ").append(text).append("\n\n");
        }
        // 处理列表
        else if (isListItem(paragraph)) {
            String bullet = paragraph.getNumFmt().equals(STNumberFormat.BULLET.toString()) ? "- " : "1. ";
            markdown.append("  ".repeat(getListLevel(paragraph) - 1)).append(bullet).append(text).append("\n");
        }
        // 普通段落
        else {
            markdown.append(text).append("\n\n");
        }
    }

    /**
     * 判断段落是否为标题并返回级别
     */
    private static int getHeadingLevel(XWPFParagraph paragraph) {
        String styleName = paragraph.getStyle();
        if (styleName != null && styleName.startsWith("Heading")) {
            try {
                return Integer.parseInt(styleName.substring(7));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 判断段落是否为列表项
     */
    private static boolean isListItem(XWPFParagraph paragraph) {
        return paragraph.getNumID() != null;
    }

    /**
     * 获取列表项的层级
     */
    private static int getListLevel(XWPFParagraph paragraph) {
        if (paragraph.getNumIlvl() != null) {
            return paragraph.getNumIlvl().intValue() + 1;
        }
        return 1;
    }

    /**
     * 改进的表格处理
     */
    private static void processTable(XWPFTable table, StringBuilder markdown) {
        List<XWPFTableRow> rows = table.getRows();
        if (rows.isEmpty()) {
            return;
        }

        // 处理表头
        XWPFTableRow headerRow = rows.get(0);
        processTableRow(headerRow, markdown);

        // 添加表头分隔线
        markdown.append("|");
        markdown.append(":---|".repeat(headerRow.getTableCells().size()));
        markdown.append("\n");

        // 处理表格内容行
        for (int i = 1; i < rows.size(); i++) {
            processTableRow(rows.get(i), markdown);
        }

        markdown.append("\n");
    }

    /**
     * 处理表格行
     */
    private static void processTableRow(XWPFTableRow row, StringBuilder markdown) {
        markdown.append("|");
        for (XWPFTableCell cell : row.getTableCells()) {
            String cellText = cell.getText().replace("\n", "<br>");
            markdown.append(cellText).append("|");
        }
        markdown.append("\n");
    }

    // 测试方法
    public static void main(String[] args) {
        try {
            String markdown = convertToMarkdown("C:\\Users\\Administrator\\Pictures\\Saved Pictures\\【前端开发工程师_上海_8-13K】王太阳_5年.docx");
            System.out.println("转换结果:\n" + markdown);
        } catch (Exception e) {
           log.error("转换失败: {}", e.getMessage(), e);
        }
    }
}
