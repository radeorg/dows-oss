package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OssUploadHandlerRequest {

    @Schema(description = "md5")
    private String md5;

    @Schema(description = "文件扩展名")
    private String fileExt;

    @Schema(description = "文件本地路径")
    private String fileLocalPath;

    @Schema(description = "文件服务器路径")
    private String filePath;

    @Schema(description = "存储通道[cos,oss,qiniu]")
    private String channel;
}
