package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "查询本地过期上传文件对象")
public class QuerySchedulerOssUploadRequest {

    @Schema(description = "状态码（默认9个0，每个0代表未执行，1代表已执行；目前第一位代表上传，第二位代表解析）")
    private String stateCode;

    @Schema(description = "状态码查询类型，eq等于，leftLike左模糊，rightLike右模糊")
    private String stateCodeType;

    @Schema(description = "触发器[OTT-解析]")
    private String trigger;

    @Schema(description = "业务系统回调状态（0待回调，1已回调，2回调失败）")
    private Integer callbackState;

    @Schema(description = "过期开始时间")
    private Date startTime;

    @Schema(description = "过期截止时间")
    private Date endTime;

    @Schema(description = "分页大小")
    private Integer pageSize;

    @Schema(description = "当前页数")
    private Integer pageNum;
}
