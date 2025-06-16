package org.dows.oss.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CallbackResponse {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String message;
}
