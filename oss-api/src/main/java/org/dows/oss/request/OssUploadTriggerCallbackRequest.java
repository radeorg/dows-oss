package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OssUploadTriggerCallbackRequest {

    @Schema(description = "文件上传ID")
    private Long ossFileId;

    @Schema(description = "文件触发详情ID")
    private Long ossDetailId;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "触发器（[UPLOAD-上传，OTT-解析]）")
    private String trigger;

    @Schema(description = "文件md5")
    private String md5;

    @Schema(description = "文件全量路径")
    private String fileLink;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件扩展表")
    private String fileExt;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "存储通道[local,oss,cos,qiniu...]")
    private String storeType;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "操作人ID")
    private Long operatorId;
}
