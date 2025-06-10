package org.dows.oss.pojo.enums;

import lombok.Getter;

@Getter
public enum OssUploaderCallBackStateEnum {

    WAIT_CALLBACK(0, "待回调"),
    COMPLETE_CALLBACK(1, "已回调"),
    FAILED_CALLBACK(2, "回调失败"),
    ;

    private final int code;
    private final String description;

    OssUploaderCallBackStateEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OssUploaderCallBackStateEnum getByCode(int code) {
        for (OssUploaderCallBackStateEnum type : OssUploaderCallBackStateEnum.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OssUploaderStateCodeEnum code: " + code);
    }
}
