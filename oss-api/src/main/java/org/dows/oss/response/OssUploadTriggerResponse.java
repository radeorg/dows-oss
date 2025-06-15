package org.dows.oss.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OssUploadTriggerResponse {

    @Schema(description = "文件上传ID")
    private Long ossUploaderId;

    @Schema(description = "文件触发详情ID")
    private Long ossDetailId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "触发器（[UPLOAD-上传，OTT-解析]）")
    private String trigger;

    @Schema(description = "文件md5")
    private String md5;

    @Schema(description = "文件全量路径")
    private String fileLink;

    @Schema(description = "文件路径")
    private String fileBasePath;

    @Schema(description = "文件扩展表")
    private String fileExt;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "存储类型[local,oss,cos,qiniu...]")
    private String storeType;
}
