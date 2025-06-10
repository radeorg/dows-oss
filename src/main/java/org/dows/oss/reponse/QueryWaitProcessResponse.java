package org.dows.oss.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class QueryWaitProcessResponse {

    @Schema(description = "文件上传ID")
    private Long ossUploaderId;

    @Schema(description = "文件存储路径（业务系统告知）")
    private String filePath;

    @Schema(description = "文件上传到本地的临时路径，删除本地文件时需要")
    private String fileTempPath;

    @Schema(description = "文件后缀")
    private String fileExt;

    @Schema(description = "文件内容md5")
    private String fileMd5;

    @Schema(description = "文本存储路径（业务系统告知）")
    private String txtPath;

    @Schema(description = "状态码")
    private String stateCode;

    @Schema(description = "文件上传过程ID")
    private Long ossUploaderProcessId;

}
