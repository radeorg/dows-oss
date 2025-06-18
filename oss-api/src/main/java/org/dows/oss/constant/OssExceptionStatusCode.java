package org.dows.oss.constant;

import lombok.Getter;
import org.dows.rade.status.StatusCode;

@Getter
public enum OssExceptionStatusCode implements StatusCode {
    FILE_EXIST("OSS00001", "文件重复"),
    OSS_FILE_NOT_FOUND("OSS00002", "文件不存在"),
    OSS_DETAIL_NOT_FOUND("OSS00003", "文件详情不存在"),
    OSS_IDENTIFIER_NOT_FOUND("OSS0004", "未找到对应的OSS配置"),
    OSS_SECRET_NOT_FOUND("OSS00005", "秘钥不正确");

    private final String code;
    private final String describe;

    OssExceptionStatusCode(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }
}
