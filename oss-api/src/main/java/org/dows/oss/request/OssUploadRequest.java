package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "oss上传对象")
public class OssUploadRequest {

    @Schema(description = "上传来源（uim/acc/hrm）")
    private String source;

    @Schema(description = "秘钥")
    private String key;
}
