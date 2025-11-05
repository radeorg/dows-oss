package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OssUploadCallbackRequest {

    @Schema(description = "文件上传ID")
    private Long ossFileId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "md5")
    private String md5;

    @Schema(description = "上传类型（0手动上传，1邮箱上传）")
    private Integer uploadType;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "操作人ID")
    private Long operatorId;
}
