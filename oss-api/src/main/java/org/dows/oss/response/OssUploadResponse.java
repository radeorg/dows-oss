package org.dows.oss.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OssUploadResponse {

    @Schema(description = "文件上传ID")
    private Long ossUploaderId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "应用ID")
    private Long appId;
}
