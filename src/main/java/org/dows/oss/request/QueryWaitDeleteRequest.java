package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "查询本地过期上传文件对象")
public class QueryWaitDeleteRequest {

    @Schema(description = "状态码（0待处理，1已完成）")
    private Integer state;

    @Schema(description = "过期开始时间")
    private Date startTime;

    @Schema(description = "过期截止时间")
    private Date endTime;

    @Schema(description = "分页大小")
    private Integer pageSize;

    @Schema(description = "当前页数")
    private Integer pageNum;
}
