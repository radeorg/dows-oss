package org.dows.oss.request;

import lombok.Data;

import java.util.List;

/**
 * MinerU 文档解析请求参数
 */
@Data
public class MineruParseRequest {

    /** 文件 URL，支持 .pdf/.doc/.docx/.ppt/.pptx/.png/.jpg/.jpeg 多种格式（必填） */
    private String url;

    /** 是否启动 OCR 功能，默认 false */
    private Boolean isOcr = false;

    /** 是否开启公式识别，默认 true */
    private Boolean enableFormula = true;

    /** 是否开启表格识别，默认 true */
    private Boolean enableTable = true;

    /**
     * 指定文档语言，默认 "ch"。
     * 可设为 "auto" 让模型自动识别，其他取值见：
     * https://paddlepaddle.github.io/PaddleOCR/latest/ppocr/blog/multi_languages.html#5
     */
    private String language = "ch";

    /**
     * 解析对象对应的数据 ID（选填）。
     * 由大小写英文字母、数字、下划线（_）、短划线（-）、英文句号（.）组成，
     * 不超过 128 个字符，可用于唯一标识业务数据。
     */
    private String dataId;

    /**
     * 解析结果回调通知 URL（选填）。
     * 为空时必须定时轮询解析结果。
     */
    private String callback;

    /**
     * 随机字符串（选填，但使用 callback 时必须提供）。
     * 用于在接收回调通知时校验请求来源。
     */
    private String seed;

    /**
     * 额外导出格式（选填）。
     * markdown、json 为默认导出格式，无需设置。
     * 支持 docx、html、latex 中的一个或多个。
     */
    private List<String> extraFormats;

    /**
     * 指定页码范围（选填）。
     * 格式示例："2,4-6"、"2--2"（从第 2 页到倒数第 2 页）。
     */
    private String pageRanges;

    /** 模型版本，v1 或 v2，默认 v1 */
    private String modelVersion = "v1";
}