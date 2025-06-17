package org.dows.oss.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件内容提前
 */
@Data
public class FileContentExtractResponse {

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "邮箱")
    private String email;
}
