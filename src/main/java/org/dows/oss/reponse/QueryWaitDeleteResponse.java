package org.dows.oss.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class QueryWaitDeleteResponse {

    @Schema(description = "文件上传ID")
    private Long ossUploaderId;

    @Schema(description = "文件上传到本地的临时路径，删除本地文件时需要")
    private String fileTempPath;

    @Schema(description = "文件后缀")
    private String fileExt;

    @Schema(description = "文件内容md5")
    private String fileMd5;

    @Schema(description = "过期时间")
    private Date expireDate;
}
