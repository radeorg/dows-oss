package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "查询带回调文件对象")
public class QueryWaitCallbackRequest {

    @Schema(description = "状态码（0待处理，1已完成）")
    private Integer state;

    @Schema(description = "业务系统回调状态（0待回调，1已回调，2回调失败）")
    private Integer callbackState;

    @Schema(description = "分页大小")
    private Integer pageSize;

    @Schema(description = "当前页数")
    private Integer pageNum;
}
