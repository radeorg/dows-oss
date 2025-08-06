package org.dows.oss.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Schema(name = "oss上传对象")
public class OssUploadRequest {

    @Schema(description = "上传来源（uim/acc/hrm）")
    private String source;

    @Schema(description = "秘钥Id")
    private String secretId;

    @Schema(description = "秘钥")
    private String secretKey;

    @Schema(description = "文件信息")
    private List<OssUploadInfo> infos;

    @Data
    public static class OssUploadInfo {

        @Schema(description = "md5")
        private String md5;

        @Schema(description = "文件")
        MultipartFile file;

        @Schema(description = "应用ID")
        String appId;

        @Schema(description = "原始文件是否已存在（可能在另一个企业已上传）")
        Boolean isExist;
    }
}
