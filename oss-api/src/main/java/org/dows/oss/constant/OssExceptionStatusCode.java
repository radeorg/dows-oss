package org.dows.oss.constant;


import lombok.Getter;
import org.dows.rade.status.StatusCode;

@Getter
public enum OssExceptionStatusCode implements StatusCode {
    OSS_FILE_NOT_FOUND("OSS00001", "文件不存在"),
    OSS_DETAIL_NOT_FOUND("OSS00002", "文件详情不存在");

    private final String code;
    private final String describe;

    OssExceptionStatusCode(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }

}
