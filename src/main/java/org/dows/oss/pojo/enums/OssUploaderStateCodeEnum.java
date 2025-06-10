package org.dows.oss.pojo.enums;

import lombok.Getter;

@Getter
public enum OssUploaderStateCodeEnum {

    WAIT_HANDLE(0, "待处理"),
    COMPLETE_HANDLE(1, "已完成"),
    FAILED_HANDLE(2, "处理失败"),
    ;

    private final int code;
    private final String description;

    OssUploaderStateCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OssUploaderStateCodeEnum getByCode(int code) {
        for (OssUploaderStateCodeEnum type : OssUploaderStateCodeEnum.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OssUploaderStateCodeEnum code: " + code);
    }
}
