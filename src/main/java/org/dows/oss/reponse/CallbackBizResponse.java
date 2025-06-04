package org.dows.oss.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CallbackBizResponse {

    @Schema(description = "文件上传ID")
    private Long ossUploaderId;

    @Schema(description = "业务系统唯一标识ID")
    private Long bizId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "来源：uim/hrm")
    private String source;

    @Schema(description = "触发器（[解析、压缩]）")
    private String trigger;

    @Schema(description = "文件内容md5")
    private String fileMd5;

    @Schema(description = "文件全量路径")
    private String fileLink;

    @Schema(description = "文本全量路径")
    private String txtLink;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "回调业务系统路径")
    private String callbackUrl;
}
