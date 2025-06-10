package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "查询待处理的上传文件对象")
public class QueryWaitProcessRequest {

    @Schema(description = "状态码（0待处理，1已完成，2处理失败）")
    private Integer state;

    @Schema(description = "触发器[OTT-解析]、[UPLOAD-上传]")
    private String trigger;

    @Schema(description = "分页大小")
    private Integer pageSize;

    @Schema(description = "当前页数")
    private Integer pageNum;
}
