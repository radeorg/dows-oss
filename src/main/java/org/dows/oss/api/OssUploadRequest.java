package org.dows.oss.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "oss上传对象")
public class OssUploadRequest {

    @Schema(description = "业务系统唯一标识ID")
    private List<Long> bizIds;

    @Schema(description = "文件内容加密后的数据")
    private List<String> md5s;

    @Schema(description = "上传到什么平台，例：COS")
    private String channel;

    @Schema(description = "上传来源（uim/acc/hrm）")
    private String source;

    @Schema(description = "指定文件上传路径")
    private String filePath;

    @Schema(description = "指定文件解析上传路径")
    private String txtPath;

    @Schema(description = "触发器（需要触发的动作，例：OTT解析、ZIP压缩）")
    private List<String> trigger;

    @Schema(description = "回调路径（文件上传成功之后，将上传的文件地址通过回调地址返回给业务系统）")
    private String callbackUrl;

    @Schema(description = "过期时间，秒，到期会删除临时文件")
    private Long expireTime;
}
