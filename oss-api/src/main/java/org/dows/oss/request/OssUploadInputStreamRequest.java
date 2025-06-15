package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "oss上传对象")
public class OssUploadInputStreamRequest {

    @Schema(description = "上传来源（uim/acc/hrm）")
    private String source;

    @Schema(description = "秘钥")
    private String secretKey;

    @Schema(description = "md5")
    private String md5;

    @Schema(description = "文件名")
    private String fileName;
}
